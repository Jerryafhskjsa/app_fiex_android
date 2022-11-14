package com.black.money.activity

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.MoneyApiServiceHelper
import com.black.base.api.WalletApiServiceHelper
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.listener.OnHandlerListener
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.model.SuccessObserver
import com.black.base.model.money.CloudPowerProject
import com.black.base.model.wallet.Wallet
import com.black.base.util.*
import com.black.base.widget.ObserveScrollView
import com.black.lib.refresh.QRefreshLayout
import com.black.money.BR
import com.black.money.R
import com.black.money.adpter.CloudPowerProjectListAdapter
import com.black.money.adpter.CloudPowerProjectListAdapter.OnCloudPowerProjectHandleListener
import com.black.money.databinding.ActivityCloudPowerProjectBinding
import com.black.money.view.CloudPowerProjectBuyWidget
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import io.reactivex.Observer
import skin.support.content.res.SkinCompatResources
import java.util.*

@Route(value = [RouterConstData.CLOUD_POWER_PROJECT])
class CloudPowerProjectActivity : BaseActionBarActivity(), View.OnClickListener, ObserveScrollView.ScrollListener, OnCloudPowerProjectHandleListener, QRefreshLayout.OnRefreshListener {
    companion object {
        private const val DELAYED_TIME: Long = 5000
    }

    private var bgC1: Drawable? = null
    private var bgB2: Drawable? = null
    private var bgDefault: Drawable? = null
    private var btnBackDefault: Drawable? = null
    private var btnBackNormal: Drawable? = null
    private var colorDefault = 0
    private var colorT1 = 0
    private var btnNoticeImgDefault: Drawable? = null
    private var btnNoticeImgScroll: Drawable? = null
    private var btnRecordImgDefault: Drawable? = null
    private var btnRecordImgScroll: Drawable? = null

    private var binding: ActivityCloudPowerProjectBinding? = null

    private var headView: View? = null
    private var headTitleView: TextView? = null
    private var btnBack: ImageButton? = null
    private var btnInfo: ImageButton? = null
    private var btnRecord: ImageButton? = null

    private var adapter: CloudPowerProjectListAdapter? = null

    private val walletCache: MutableMap<String, Wallet?> = HashMap()
    private var usdtWallet: Wallet? = null
    private val listRefreshTime: Long = 0

    private val handler = Handler()
    private var timerCommand: TimerCommand? = null

    private val userInfoObserver = createUserInfoObserver()
    private fun createUserInfoObserver(): Observer<String?> {
        return object : SuccessObserver<String?>() {
            override fun onSuccess(value: String?) {
                onUserInfoChanged()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bgC1 = ColorDrawable(SkinCompatResources.getColor(this, R.color.C1))
        bgB2 = ColorDrawable(SkinCompatResources.getColor(this, R.color.B2))
        bgDefault = ColorDrawable(SkinCompatResources.getColor(this, R.color.C1))
        btnBackDefault = SkinCompatResources.getDrawable(this, R.drawable.btn_back_white)
        btnBackNormal = SkinCompatResources.getDrawable(this, R.drawable.btn_back)
        colorDefault = SkinCompatResources.getColor(this, R.color.white)
        colorT1 = SkinCompatResources.getColor(this, R.color.T1)
        btnNoticeImgDefault = SkinCompatResources.getDrawable(mContext, R.drawable.icon_demand_notice)
        btnNoticeImgScroll = SkinCompatResources.getDrawable(mContext, R.drawable.icon_mine_info)
        btnRecordImgDefault = SkinCompatResources.getDrawable(mContext, R.drawable.icon_demand_record)
        btnRecordImgScroll = SkinCompatResources.getDrawable(mContext, R.drawable.icon_promotions_record)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_cloud_power_project)

        binding?.scrollView?.addScrollListener(this)
        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        val decoration = HeightDividerItemDecoration(mContext, DividerItemDecoration.VERTICAL)
        val displayMetrics = resources.displayMetrics
        decoration.setDividerHeight((displayMetrics.density * 8).toInt())
        decoration.setDrawable(SkinCompatResources.getDrawable(mContext, R.drawable.bg_divider_default))
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter = CloudPowerProjectListAdapter(mContext, BR.listItemCloudPowerProjectModel, null)
        adapter?.setOnPromotionsHandleListener(this)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.emptyView?.root?.setBackgroundColor(SkinCompatResources.getColor(this, R.color.B1))
        binding?.recyclerView?.setEmptyView(binding?.emptyView?.root)
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.isFocusable = false

        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(this))
        binding?.refreshLayout?.setOnRefreshListener(this)
        resetActionBarRes(0f)
    }

    override fun isStatusBarDark(): Boolean {
        return false
    }

    override fun getActionBarLayoutId(): Int {
        return R.layout.action_bar_cloud_power_project
    }

    override fun initActionBarView(view: View) {
        headView = view
        btnBack = view.findViewById(R.id.action_bar_back)
        view.findViewById<TextView>(R.id.action_bar_title).also { headTitleView = it }.text = "云算力"
        view.findViewById<ImageButton>(R.id.head_notice).also { btnInfo = it }.setOnClickListener(this)
        view.findViewById<ImageButton>(R.id.head_record).also { btnRecord = it }.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.head_notice) {
            val bundle = Bundle()
            bundle.putString(ConstData.TITLE, "运算力说明")
            bundle.putString(ConstData.URL, UrlConfig.getUrlCloudPowerRule(mContext))
            BlackRouter.getInstance().build(RouterConstData.WEB_VIEW).with(bundle).go(mContext)
        } else if (id == R.id.head_record) {
            BlackRouter.getInstance().build(RouterConstData.CLOUD_POWER_RECORD).go(this)
        }
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        if (binding?.scrollView != null && binding?.topLayout != null && binding?.topLayout?.height != 0) {
            var alpha = binding?.scrollView?.scrollY?.toFloat()!! / binding?.topLayout?.height!!
            alpha = if (alpha < 0) 0.toFloat() else if (alpha > 1) 1.toFloat() else alpha
            resetActionBarRes(alpha)
        }
    }

    override fun onResume() {
        super.onResume()
        getWalletList(null)
        isStop = false
        isLoadData = false
        getCloudPowerProjectList(false)
    }

    override fun onStop() {
        super.onStop()
        isStop = true
        if (timerCommand != null) {
            handler.removeCallbacks(timerCommand)
            timerCommand = null
        }
    }

    override fun onRush(project: CloudPowerProject) {
        //先判断状态是否能够购买
        if (project.statusCode != 1) {
            return
        }
        fryingHelper.checkUserAndDoing(Runnable {
            usdtWallet = walletCache[project.coinType]
            if (usdtWallet == null) {
                getWalletList(Runnable { showBuyWidget(project) })
            } else {
                showBuyWidget(project)
            }
        }, 0)
    }

    override fun onRefresh() {
        getCloudPowerProjectList(false)
    }

    fun showBuyWidget(project: CloudPowerProject) {
        usdtWallet?.let {
            CloudPowerProjectBuyWidget(mContext, it, project).setOnHandlerListener(object : OnHandlerListener<CloudPowerProjectBuyWidget> {
                override fun onCancel(widget: CloudPowerProjectBuyWidget) {
                    widget.dismiss()
                }

                override fun onConfirm(widget: CloudPowerProjectBuyWidget) {
                    val amountText = widget.amountText
                    val amount = CommonUtil.parseDouble(amountText)
                    if (amount == null) {
                        FryingUtil.showToast(mContext, getString(R.string.alert_c2c_create_amount_error, ""))
                        return
                    }
                    if (!(project.buyMinNum == null || amount >= project.buyMinNum!!)) {
                        FryingUtil.showToast(mContext, String.format("最小购买%s %s",
                                NumberUtil.formatNumberDynamicScaleNoGroup(project.buyMinNum, 9, 0, 8),
                                if (project.distributionCoinType == null) nullAmount else project.distributionCoinType))
                        return
                    }
                    MoneyApiServiceHelper.buyCloudPower(mContext, if (project.id == null) null else NumberUtil.formatNumberNoGroup(project.id), amountText, object : NormalCallback<HttpRequestResultString?>(mContext!!) {
                        override fun error(type: Int, error: Any?) {
                            super.error(type, error)
                            if (type == ConstData.ERROR_MISS_MONEY_PASSWORD) {
                                widget.dismiss()
                            }
                        }

                        override fun callback(returnData: HttpRequestResultString?) {
                            if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                                FryingUtil.showToast(mContext, "购买成功")
                                widget.dismiss()
                                getWalletList(null)
                            } else {
                                FryingUtil.showToast(mContext, if (returnData?.msg == null) "null" else returnData.msg)
                            }
                        }
                    })
                }
            }).show()
        }
    }

    private fun resetActionBarRes(alpha: Float) {
        headTitleView?.alpha = if (alpha < 0) 0.toFloat() else if (alpha > 1) 1.toFloat() else alpha
        if (alpha < 1) {
            headView?.background = bgDefault
            binding?.rootView?.background = bgDefault
            btnBack?.setImageDrawable(btnBackDefault)
            btnInfo?.setImageDrawable(btnNoticeImgDefault)
            btnRecord?.setImageDrawable(btnRecordImgDefault)
            headTitleView?.setTextColor(colorDefault)
            resetStatusBarTheme(false)
        } else {
            headView?.background = bgB2
            binding?.rootView?.background = bgB2
            btnBack?.setImageDrawable(btnBackNormal)
            btnInfo?.setImageDrawable(btnNoticeImgScroll)
            btnRecord?.setImageDrawable(btnRecordImgScroll)
            headTitleView?.setTextColor(colorT1)
            resetStatusBarTheme(!CookieUtil.getNightMode(mContext))
        }
    }

    var isLoadData = false
    private fun getCloudPowerProjectList(isShowLoading: Boolean) {
        if (isLoadData) {
            return
        }
        isLoadData = true
        MoneyApiServiceHelper.getCloudPowerConfig(this, isShowLoading, object : NormalCallback<HttpRequestResultDataList<CloudPowerProject?>?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
                showCloudPowerProject(null)
            }

            override fun callback(returnData: HttpRequestResultDataList<CloudPowerProject?>?) {
                if (returnData?.code != null && returnData.code == HttpRequestResult.SUCCESS) {
                    showCloudPowerProject(returnData.data)
                } else {
                    showCloudPowerProject(null)
                    FryingUtil.showToast(mContext, if (returnData?.msg == null) "null" else returnData.msg)
                }
            }
        })
    }

    private fun showCloudPowerProject(list: ArrayList<CloudPowerProject?>?) {
        adapter?.data = list
        adapter?.notifyDataSetChanged()
        binding?.refreshLayout?.setRefreshing(false)
        binding?.refreshLayout?.setLoading(false)
        if (!isStop) {
            if (timerCommand == null) {
                timerCommand = TimerCommand()
            }
            isLoadData = false
            handler.postDelayed(timerCommand, DELAYED_TIME)
        }
    }

    //用户信息被修改，刷新钱包
    private fun onUserInfoChanged() {
        getWalletList(null)
    }

    private fun getWalletList(callback: Runnable?) {
        WalletApiServiceHelper.getWalletList(this, callback != null, object : Callback<ArrayList<Wallet?>?>() {
            override fun error(type: Int, error: Any) {}
            override fun callback(returnData: ArrayList<Wallet?>?) {
                if (returnData == null || returnData.isEmpty()) {
                    return
                }
                for (wallet in returnData) {
                    wallet?.coinType?.let {
                        walletCache[it] = wallet
                    }
                }
                callback?.run()
            }
        })
    }

    private var isStop = false

    private inner class TimerCommand : Runnable {
        override fun run() {
            if (!isStop) {
                getCloudPowerProjectList(false)
            }
        }
    }
}