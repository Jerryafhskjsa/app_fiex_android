package com.black.community.activity

import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActionBarActivity
import com.black.base.adapter.interfaces.OnItemClickListener
import com.black.base.api.CommunityApiServiceHelper
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.SuccessObserver
import com.black.base.model.community.FactionItem
import com.black.base.util.*
import com.black.community.BR
import com.black.community.R
import com.black.community.adapter.FactionListAdapter
import com.black.community.databinding.ActivityFactionListBinding
import com.black.lib.refresh.QRefreshLayout
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import io.reactivex.Observer
import org.json.JSONObject
import java.util.*

//笑傲江湖帮派列表
@Route(value = [RouterConstData.FACTION_LIST], beforePath = RouterConstData.LOGIN)
class FactionListActivity : BaseActionBarActivity(), OnItemClickListener, QRefreshLayout.OnRefreshListener, View.OnClickListener {
    private var binding: ActivityFactionListBinding? = null

    private var adapter: FactionListAdapter? = null

    private var listRefreshTime: Long = 0
    private val handler = Handler()
    private var timerCommand: TimerCommand? = null

    //门派变更
    private var factionUpdateObserver: Observer<JSONObject?>? = createFactionUpdateObserver()

    private fun createFactionUpdateObserver(): Observer<JSONObject?> {
        return object : SuccessObserver<JSONObject?>() {
            override fun onSuccess(value: JSONObject?) {
                getFactionList(false)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_faction_list)
        val layoutManager = GridLayoutManager(this, 3)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        val displayMetrics = resources.displayMetrics
        val decoration = GridDividerItemDecoration((0.5 * displayMetrics.density).toInt(), -0x363637)
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter = FactionListAdapter(this, BR.listItemFactionListModel, null)
        adapter?.setOnItemClickListener(this)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setEmptyView(binding?.emptyView?.root)
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.isFocusable = false

        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(this))
        binding?.refreshLayout?.setOnRefreshListener(this)

        binding?.btnFactionRule?.setOnClickListener(this)
    }

    override fun isStatusBarDark(): Boolean {
        return true
    }

    override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
        val factionItem = adapter?.getItem(position)
        if (factionItem?.id == null) {
            return
        }
        val bundle = Bundle()
        bundle.putLong(ConstData.FACTION_ID, factionItem.id!!)
        fryingHelper.checkUserAndDoing(Runnable {
            val userInfo = CookieUtil.getUserInfo(mContext)
            if (userInfo == null) {
                FryingUtil.showToast(mContext, "请先登录系统")
            } else {
                BlackRouter.getInstance().build(RouterConstData.FACTION_DETAIL).with(bundle).go(mContext)
                //                    String userIdHeader = IMHelper.getUserIdHeader(mContext);
//                    String userId = userInfo.id;
//                    IMHelper.startWithIMGroupActivity(FactionListActivity.this, FactionListActivity.this, userIdHeader + userId, "@TGS#3TJVTVBGJ", RouterConstData.FACTION_DETAIL, bundle, null, null);
            }
        }, 0)
        //                BlackRouter.getInstance().build(RouterConstData.FACTION_DETAIL).with(bundle).go(this);
    }

    override fun onClick(v: View) {
        if (v.id == R.id.btn_faction_rule) {
            val bundle = Bundle()
            bundle.putString(ConstData.TITLE, "笑傲江湖")
            bundle.putString(ConstData.URL, UrlConfig.getFactionRuleUrl(mContext))
            BlackRouter.getInstance().build(RouterConstData.WEB_VIEW).with(bundle).go(mContext)
        }
    }

    override fun onRefresh() {
        getFactionList(false)
    }

    override fun onResume() {
        super.onResume()
        sendSocketCommandChangedBroadcast(SocketUtil.COMMAND_FACTION_OPEN)
        if (factionUpdateObserver == null) {
            factionUpdateObserver = createFactionUpdateObserver()
        }
        getFactionList(false)
        checkListAddTimer()
    }

    override fun onStop() {
        super.onStop()
        sendSocketCommandChangedBroadcast(SocketUtil.COMMAND_FACTION_CLOSE)
        if (timerCommand != null) {
            handler.removeCallbacks(timerCommand)
            timerCommand = null
        }
    }

    private fun getFactionList(isShowLoading: Boolean) {
        CommunityApiServiceHelper.getFactionList(this, isShowLoading, object : NormalCallback<HttpRequestResultDataList<FactionItem?>?>() {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
                binding?.refreshLayout?.setRefreshing(false)
            }

            override fun callback(returnData: HttpRequestResultDataList<FactionItem?>?) {
                binding?.refreshLayout?.setRefreshing(false)
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    showFactionList(returnData.data)
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    private fun showFactionList(list: ArrayList<FactionItem?>?) {
        if (list != null && list.isNotEmpty()) {
            val thisTime = System.currentTimeMillis()
            for (factionItem in list) {
                factionItem?.thisTime = thisTime
            }
        }
        adapter?.setLoseTime(0)
        adapter?.data = list
        adapter?.notifyDataSetChanged()
        listRefreshTime = SystemClock.elapsedRealtime()
        checkListAddTimer()
    }

    private inner class TimerCommand : Runnable {
        override fun run() {
            if (refreshCountdown()) {
                handler.postDelayed(this, 1000)
            }
        }
    }

    private fun checkListAddTimer() { //查询是否存在募集中的项目，如果存在，需要开启定时器，刷新倒计时
        checkStatusChanged()
        val hasNew = checkNeedTimer()
        if (hasNew) {
            timerCommand = TimerCommand()
            handler.post(timerCommand)
        }
    }

    //刷新倒计时
    private fun checkNeedTimer(): Boolean {
        val list = adapter?.data
        if (list != null) {
            for (i in list.indices) {
                val faction = list[i]
                val statusCode = faction?.ownerStatusCode
                if (statusCode == -1 || statusCode == 1 || statusCode == 3) {
                    continue
                }
                val endTime = if (statusCode == 0) (faction.nextOwnerChangeTime
                        ?: 0) else if (statusCode == 2) (faction.nextOwnerChangeFinishedTime
                        ?: 0) else 0
                val thisTime = (faction?.thisTime ?: 0) + (adapter?.getLoseTime()
                        ?: 0)
                if (thisTime > endTime) {
                    return true
                }
            }
        }
        return false
    }

    //刷新倒计时
    private fun checkStatusChanged(): List<Int> {
        val list = adapter?.data
        val updatePositions: MutableList<Int> = ArrayList()
        if (list != null) {
            for (i in list.indices) {
                val faction = list[i]
                var statusCode = faction?.ownerStatusCode
                if (statusCode == -1 || statusCode == 1 || statusCode == 3) {
                    continue
                }
                updatePositions.add(i)
                val thisTime = (faction?.thisTime ?: 0) + (adapter?.getLoseTime()
                        ?: 0)
                if (statusCode == 0) {
                    //未开始判断开始时间和当前时间
                    val startTime = faction?.nextOwnerChangeTime ?: 0
                    if (startTime <= thisTime) {
                        faction?.status = 2
                        statusCode = 2
                    }
                }
            }
        }
        return updatePositions
    }

    //刷新倒计时
    private fun refreshCountdown(): Boolean {
        val loseTime = SystemClock.elapsedRealtime() - listRefreshTime
        adapter?.setLoseTime(loseTime)
        val updatePositions = checkStatusChanged()
        if (updatePositions.isNotEmpty()) {
            for (i in updatePositions) {
                adapter?.notifyItemChanged(i, FactionListAdapter.STATUS)
            }
        }
        val check = checkNeedTimer()
        if (!check) {
            if (timerCommand != null) {
                handler.removeCallbacks(timerCommand)
                timerCommand = null
            }
        }
        return check
    }
}