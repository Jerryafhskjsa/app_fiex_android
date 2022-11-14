package com.black.money.activity

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActivity
import com.black.base.adapter.interfaces.OnItemClickListener
import com.black.base.api.MoneyApiServiceHelper
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.model.money.PromotionsConfig
import com.black.base.model.money.PromotionsRush
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.lib.refresh.QRefreshLayout
import com.black.money.BR
import com.black.money.R
import com.black.money.adpter.PromotionsListAdapter
import com.black.money.databinding.ActivityPromotionsListBinding
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import skin.support.content.res.SkinCompatResources
import java.util.*

@Route(value = [RouterConstData.PROMOTIONS], beforePath = RouterConstData.LOGIN)
class PromotionsListActivity : BaseActivity(), View.OnClickListener, QRefreshLayout.OnRefreshListener, OnItemClickListener, PromotionsListAdapter.OnPromotionsRushListener {
    private var binding: ActivityPromotionsListBinding? = null

    private var adapter: PromotionsListAdapter? = null
    private var promotionsConfig: PromotionsConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_promotions_list)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(this, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.6 * 255).toInt()
        decoration.setDrawable(drawable)
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter = PromotionsListAdapter(this, BR.listItemPromotionsRushModel, null)
        adapter?.setOnPromotionsRushListener(this)
        adapter?.setOnItemClickListener(this)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setEmptyView(binding?.emptyView?.root)
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.isFocusable = false

        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(this))
        binding?.refreshLayout?.setOnRefreshListener(this)
        getPromotionsList(true)
    }

    override fun needGeeTest(): Boolean {
        return true
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.rush_buy_fbs)
    }

    override fun initToolbarViews(toolbar: Toolbar) {
        findViewById<View>(R.id.promotions_record).setOnClickListener(this)
    }

    override fun onClick(view: View) {
        val id = view.id
        if (id == R.id.promotions_record) {
            BlackRouter.getInstance().build(RouterConstData.PROMOTIONS_RECORD).go(this)
        }
    }

    override fun onRefresh() {
        getPromotionsList(false)
    }

    override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {}

    override fun onResume() {
        super.onResume()
        checkListAddTimer()
    }

    override fun onStop() {
        super.onStop()
        if (timerCommand != null) {
            handler.removeCallbacks(timerCommand)
            timerCommand = null
        }
    }

    private fun getPromotionsList(isShowLoading: Boolean) {
        MoneyApiServiceHelper.getPromotionsList(this, isShowLoading, object : NormalCallback<HttpRequestResultData<PromotionsConfig?>?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
                binding?.refreshLayout?.setRefreshing(false)
                promotionsConfig = null
                showPromotionsList()
            }

            override fun callback(returnData: HttpRequestResultData<PromotionsConfig?>?) {
                binding?.refreshLayout?.setRefreshing(false)
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    promotionsConfig = returnData.data
                } else {
                    promotionsConfig = null
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
                showPromotionsList()
            }
        })
    }

    private fun showPromotionsList() {
        val list = if (promotionsConfig == null) null else promotionsConfig!!.list
        if (list != null && list.isNotEmpty()) {
            val thisTime = System.currentTimeMillis()
            for (promotionsRush in list) {
                promotionsRush?.thisTime = thisTime
            }
        }
        adapter?.setLoseTime(0)
        adapter?.data = list
        adapter?.notifyDataSetChanged()
        listRefreshTime = SystemClock.elapsedRealtime()
        checkListAddTimer()
    }

    private var listRefreshTime: Long = 0
    private val handler = Handler()
    private var timerCommand: TimerCommand? = null

    override fun onRush(promotions: PromotionsRush?) {
        promotions?.let {
            showRushDialog(promotions)
        }
    }

    private fun showRushDialog(promotions: PromotionsRush) {
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_promotions_rush, null)
        val alertDialog = Dialog(mContext, R.style.AlertDialog)
        val window = alertDialog.window
        if (window != null) {
            val params = window.attributes
            //设置背景昏暗度
            params.dimAmount = 0.2f
            params.gravity = Gravity.BOTTOM
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            //设置dialog动画
            window.setWindowAnimations(R.style.anim_bottom_in_out)
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = mContext.resources.displayMetrics
        val layoutParams = ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        alertDialog.setContentView(contentView, layoutParams)
        val titleView = contentView.findViewById<TextView>(R.id.rush_buy_title)
        titleView.text = String.format("抢购 %s", if (promotions.distributionCoinType == null) nullAmount else promotions.distributionCoinType)
        val priceView = contentView.findViewById<TextView>(R.id.price)
        val price = (1.toString() + (if (promotions.distributionCoinType == null) "" else promotions.distributionCoinType)
                + " = "
                + NumberUtil.formatNumberDynamicScaleNoGroup(if (promotions.price == null) 0 else promotions.price, 6, 4, 4)
                + if (promotions.coinType == null) "" else promotions.coinType)
        priceView.text = price
        val maxAmountView = contentView.findViewById<TextView>(R.id.max_amount)
        maxAmountView.text = getString(R.string.your_max_rush_amount, NumberUtil.formatNumberDynamicScaleNoGroup(if (promotions.userTotalAmout == null) 0 else promotions.userTotalAmout, 6, 0, 4))
        val amountView = contentView.findViewById<EditText>(R.id.amount)
        val financingAmountView = contentView.findViewById<EditText>(R.id.financing_amount)
        financingAmountView.setText(NumberUtil.formatNumberDynamicScaleNoGroup(if (promotions.userFinancingAmount == null) 0 else promotions.userFinancingAmount, 6, 0, 4))
        financingAmountView.isEnabled = false
        val coinTypeView = contentView.findViewById<TextView>(R.id.coin_type)
        val coinType02View = contentView.findViewById<TextView>(R.id.coin_type_02)
        coinTypeView.text = if (promotions.distributionCoinType == null) "" else promotions.distributionCoinType
        coinType02View.text = if (promotions.distributionCoinType == null) "" else promotions.distributionCoinType
        val btnCancel = contentView.findViewById<View>(R.id.btn_cancel)
        btnCancel.setOnClickListener { alertDialog.dismiss() }
        val btnConfirm = contentView.findViewById<View>(R.id.btn_buy_confirm)
        btnConfirm.setOnClickListener(View.OnClickListener {
            val amountText = amountView.text.toString()
            val amount = CommonUtil.parseDouble(amountText)
            if (amount == null) {
                FryingUtil.showToast(mContext, getString(R.string.alert_c2c_create_amount_error, ""))
                return@OnClickListener
            }
            MoneyApiServiceHelper.rushPromotions(mContext, promotions.id.toString(), amountText, object : NormalCallback<HttpRequestResultString?>(mContext!!) {
                override fun callback(returnData: HttpRequestResultString?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        FryingUtil.showToast(mContext, getString(R.string.rush_success))
                        alertDialog.dismiss()
                        getPromotionsList(true)
                    } else {
                        FryingUtil.showToast(mContext, if (returnData?.msg == null) getString(R.string.rush_failed) else returnData.msg)
                    }
                }
            })
        })
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private inner class TimerCommand : Runnable {
        override fun run() {
            if (refreshCountdown()) {
                handler.postDelayed(this, 1000)
            }
        }
    }

    private fun checkListAddTimer() {
        //查询是否存在募集中的项目，如果存在，需要开启定时器，刷新倒计时
        checkStatusChanged()
        val hasNew = checkNeedTimer()
        if (hasNew) {
            timerCommand = TimerCommand()
            handler.post(timerCommand)
        }
    }

    //刷新倒计时
    private fun checkNeedTimer(): Boolean {
        var hasNew = false
        val list = adapter?.data
        if (list != null) {
            for (i in list.indices) {
                val promotions = list[i]
                if (promotions is PromotionsRush) {
                    val statusCode = promotions.statusCode
                    if (statusCode == 2 || statusCode == 3) {
                        continue
                    }
                    if (statusCode == 0) {
                        //未开始判断开始时间和当前时间
                        val startTime = promotions.startTime ?: 0
                        val thisTime = (promotions.thisTime ?: 0) + (adapter?.getLoseTime()
                                ?: 0)
                        if (startTime > thisTime) {
                            hasNew = true
                            break
                        }
                    }
                    if (statusCode == 1) {
                        //未开始判断开始时间和当前时间
                        val endTime = promotions.endTime ?: 0
                        val thisTime = (promotions.thisTime ?: 0) + (adapter?.getLoseTime()
                                ?: 0)
                        if (endTime > thisTime) {
                            hasNew = true
                            break
                        }
                    }
                }
            }
        }
        return hasNew
    }

    //刷新倒计时
    private fun checkStatusChanged(): List<Int> {
        val list = adapter?.data
        val updatePositions: MutableList<Int> = ArrayList()
        if (list != null) {
            for (i in list.indices) {
                val promotions = list[i]
                if (promotions is PromotionsRush) {
                    var statusCode = promotions.statusCode
                    if (statusCode == 2 || statusCode == 3) {
                        continue
                    }
                    updatePositions.add(i)
                    if (statusCode == 0) {
                        //未开始判断开始时间和当前时间
                        val startTime = promotions.startTime ?: 0
                        val thisTime = (promotions.thisTime ?: 0) + (adapter?.getLoseTime()
                                ?: 0)
                        if (startTime <= thisTime) {
                            promotions.status = 1
                            statusCode = 1
                        }
                    }
                    if (statusCode == 1) {
                        //未开始判断开始时间和当前时间
                        val endTime = promotions.endTime ?: 0
                        val thisTime = (promotions.thisTime ?: 0) + (adapter?.getLoseTime()
                                ?: 0)
                        if (endTime <= thisTime) {
                            promotions.status = 2
                        }
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
                adapter?.notifyItemChanged(i, PromotionsListAdapter.STATUS)
            }
        }
        //        adapter.notifyDataSetChanged();
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