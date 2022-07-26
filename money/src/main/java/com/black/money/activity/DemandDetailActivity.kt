package com.black.money.activity

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.MoneyApiServiceHelper
import com.black.base.api.WalletApiServiceHelper
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.listener.OnHandlerListener
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultString
import com.black.base.model.SuccessObserver
import com.black.base.model.filter.DemandRecordStatus
import com.black.base.model.money.Demand
import com.black.base.model.money.DemandConfig
import com.black.base.model.wallet.Wallet
import com.black.base.util.*
import com.black.base.widget.ObserveScrollView
import com.black.lib.refresh.QRefreshLayout
import com.black.money.R
import com.black.money.databinding.ActivityDemandDetailBinding
import com.black.money.view.DemandChangeInWidget
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import io.reactivex.Observer
import skin.support.content.res.SkinCompatResources
import java.util.*

@Route(value = [RouterConstData.DEMAND_DETAIL], beforePath = RouterConstData.LOGIN)
class DemandDetailActivity : BaseActionBarActivity(), View.OnClickListener, ObserveScrollView.ScrollListener, QRefreshLayout.OnRefreshListener {
    private var bgC1: Drawable? = null
    private var bgB2: Drawable? = null
    private var bgDefault: Drawable? = null
    private var btnBackDefault: Drawable? = null
    private var btnBackNormal: Drawable? = null
    private var colorDefault = 0
    private var colorT1 = 0
    private var btnRecordImgDefault: Drawable? = null
    private var btnRecordImgScroll: Drawable? = null

    private var demandCoins: ArrayList<String>? = null
    private var demand: Demand? = null

    private var binding: ActivityDemandDetailBinding? = null

    private var headView: View? = null
    private var btnBack: ImageButton? = null
    private var headTitleView: TextView? = null
    private var btnRecord: TextView? = null

    private var imageLoader: ImageLoader? = null
    private val walletCache: MutableMap<String, Wallet?> = HashMap()
    private var userInfoObserver: Observer<String?>? = createUserInfoObserver()
    private fun createUserInfoObserver(): Observer<String?> {
        return object : SuccessObserver<String?>() {
            override fun onSuccess(value: String?) {
                getWalletList(null)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        demandCoins = intent.getStringArrayListExtra(ConstData.DEMAND_COINS)
        demand = intent.getParcelableExtra(ConstData.DEMAND)
        if (demand == null) {
            finish()
            return
        }
        imageLoader = ImageLoader(this)

        bgC1 = ColorDrawable(SkinCompatResources.getColor(this, R.color.C1))
        bgB2 = ColorDrawable(SkinCompatResources.getColor(this, R.color.B2))
        bgDefault = ColorDrawable(SkinCompatResources.getColor(this, R.color.C1))
        btnBackDefault = SkinCompatResources.getDrawable(this, R.drawable.btn_back_white)
        btnBackNormal = SkinCompatResources.getDrawable(this, R.drawable.btn_back)
        colorDefault = SkinCompatResources.getColor(this, R.color.T4)
        colorT1 = SkinCompatResources.getColor(this, R.color.T1)
        btnRecordImgDefault = SkinCompatResources.getDrawable(mContext, R.drawable.icon_demand_record)
        btnRecordImgScroll = SkinCompatResources.getDrawable(mContext, R.drawable.icon_promotions_record)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_demand_detail)
        binding?.scrollView?.addScrollListener(this)
        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(this))
        binding?.refreshLayout?.setOnRefreshListener(this)
        binding?.btnChangeIn?.setOnClickListener(this)
        refreshDemand()
        resetActionBarRes(0f)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getActionBarLayoutId(): Int {
        return R.layout.action_bar_left_back_text
    }

    override fun initActionBarView(view: View) {
        headView = view
        btnBack = view.findViewById(R.id.action_bar_back)
        headTitleView = view.findViewById(R.id.action_bar_title)
        headTitleView?.text = "活利宝"
        view.findViewById<TextView>(R.id.action_bar_extras).also { btnRecord = it }.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.amount_layout -> {
                val bundle = Bundle()
                bundle.putInt(ConstData.MONEY_RECORD_TYPE, ConstData.TAB_DEMAND)
                bundle.putStringArrayList(ConstData.DEMAND_COINS, demandCoins)
                bundle.putParcelable(ConstData.DEMAND, demand)
                bundle.putParcelable(ConstData.DEMAND_STATUS, DemandRecordStatus.INTO)
                BlackRouter.getInstance().build(RouterConstData.MONEY_RECORD).with(bundle).go(this)
            }
            R.id.reward_layout -> {
                val bundle = Bundle()
                bundle.putParcelable(ConstData.REGULAR, demand)
                BlackRouter.getInstance().build(RouterConstData.DEMAND_RECORD).with(bundle).go(this)
            }
            R.id.action_bar_extras -> {
                val bundle = Bundle()
                bundle.putInt(ConstData.MONEY_RECORD_TYPE, ConstData.TAB_DEMAND)
                bundle.putStringArrayList(ConstData.DEMAND_COINS, demandCoins)
                bundle.putParcelable(ConstData.DEMAND, demand)
                bundle.putParcelable(ConstData.DEMAND_STATUS, DemandRecordStatus.INTO)
                BlackRouter.getInstance().build(RouterConstData.MONEY_RECORD).with(bundle).go(this)
            }
            R.id.btn_change_in -> {
                if (demand == null || demand?.coinType == null) {
                    return
                }
                fryingHelper.checkUserAndDoing(Runnable {
                    val wallet = if (demand?.coinType == null) null else walletCache[demand?.coinType!!]
                    if (wallet == null) {
                        getWalletList(Runnable {
                            val newWallet = if (demand?.coinType == null) null else walletCache[demand?.coinType!!]
                            if (newWallet != null) {
                                showRegularChangeInWidget(newWallet, demand!!)
                            }
                        })
                    } else {
                        showRegularChangeInWidget(wallet, demand!!)
                    }
                }, 0)
            }
        }
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        if (binding?.scrollView != null && binding?.topLayout != null && binding?.topLayout?.height != 0) {
            var alpha = binding?.scrollView?.scrollY!!.toFloat() / binding?.topLayout?.height!!
            alpha = if (alpha < 0) 0.toFloat() else if (alpha > 1) 1.toFloat() else alpha
            resetActionBarRes(alpha)
        }
    }

    override fun onResume() {
        super.onResume()
        if (userInfoObserver == null) {
            userInfoObserver = createUserInfoObserver()
        }
        SocketDataContainer.subscribeUserInfoObservable(userInfoObserver)
        refreshDemandData()
        getWalletList(null)
    }

    public override fun onStop() {
        super.onStop()
        if (userInfoObserver != null) {
            SocketDataContainer.removeUserInfoObservable(userInfoObserver)
        }
    }

    override fun onRefresh() {
        refreshDemandData()
    }

    private fun resetActionBarRes(alpha: Float) {
        if (alpha < 1) {
            headView?.background = bgDefault
            binding?.rootView?.background = bgDefault
            btnBack?.setImageDrawable(btnBackDefault)
            headTitleView?.setTextColor(colorDefault)
            btnRecord?.setTextColor(colorDefault)
        } else {
            headView?.background = bgB2
            binding?.rootView?.background = bgB2
            btnBack?.setImageDrawable(btnBackNormal)
            headTitleView?.setTextColor(colorT1)
            btnRecord?.setTextColor(colorT1)
        }
        resetHeaderButtonImages(alpha)
    }

    private fun resetHeaderButtonImages(alpha: Float) {
        if (alpha < 1) {
            CommonUtil.setTextViewCompoundDrawable(btnRecord, btnRecordImgDefault, 0)
        } else {
            CommonUtil.setTextViewCompoundDrawable(btnRecord, btnRecordImgScroll, 0)
        }
    }

    private fun refreshDemand() {
        binding?.coinType?.text = String.format("活利宝-%s", if (demand?.coinType == null) nullAmount else demand?.coinType)
        val demandRate = if (demand == null) null else CommonUtil.getItemFromList(demand?.rateConfDto, 0)
        binding?.rate?.text = String.format("%s%%", if (demandRate?.rate == null) nullAmount else NumberUtil.formatNumberNoGroupHardScale(demandRate.rate!! * 100, 2))
        binding?.rateDescription?.text = if (demand?.rateText == null) nullAmount else demand?.rateText
        if (demand != null && demand?.rateImg != null) {
            imageLoader?.loadImage(binding?.rateImage, UrlConfig.getHost(this) + demand?.rateImg)
        }
        binding?.rewordRule?.text = if (demand == null || demand?.interestText == null) nullAmount else demand?.interestText
        binding?.totalAmountTitle?.text = String.format("资产总额 (%s) ", if (demand == null || demand?.coinType == null) nullAmount else demand?.coinType)
        binding?.totalAmount?.text = if (demand == null || demand?.lockAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(demand?.lockAmount, 15, 0, 12)
        binding?.totalRewardTitle?.text = String.format("累计收益 (%s) ", if (demand == null || demand?.distributionCoinType == null) nullAmount else demand?.distributionCoinType)
        binding?.totalReward?.text = if (demand == null || demand?.totalInterestAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(demand?.totalInterestAmount, 15, 0, 12)
        binding?.btnChangeIn?.isEnabled = demand?.status ?: false
    }

    private fun refreshDemandData() {
        MoneyApiServiceHelper.getDemandConfig(mContext, object : NormalCallback<HttpRequestResultData<DemandConfig?>?>() {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
                binding?.refreshLayout?.setRefreshing(false)
            }

            override fun callback(returnData: HttpRequestResultData<DemandConfig?>?) {
                binding?.refreshLayout?.setRefreshing(false)
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    val demands = if (returnData.data == null) null else returnData.data?.coinTypeConf
                    if (demands != null) {
                        for (temp in demands) {
                            if (temp != null && TextUtils.equals(temp.coinType, demand?.coinType)) {
                                demand = temp
                                refreshDemand()
                                break
                            }
                        }
                    }
                }
            }
        })
    }

    private fun showRegularChangeInWidget(wallet: Wallet, demand: Demand) {
        val demandChangeInWidget = DemandChangeInWidget(mContext, wallet, demand)
        demandChangeInWidget.setOnHandlerListener(object : OnHandlerListener<DemandChangeInWidget> {
            override fun onCancel(widget: DemandChangeInWidget) {
                widget.dismiss()
            }

            override fun onConfirm(widget: DemandChangeInWidget) {
                val amountText = widget.amountText
                val amount = CommonUtil.parseDouble(amountText)
                if (amount == null) {
                    FryingUtil.showToast(mContext, getString(R.string.alert_c2c_create_amount_error, ""))
                    return
                }
                MoneyApiServiceHelper.postDemandChangeIn(mContext, demand.coinType, widget.amountText, object : NormalCallback<HttpRequestResultString?>() {
                    override fun callback(returnData: HttpRequestResultString?) {
                        if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                            FryingUtil.showToast(mContext, "存入成功")
                            widget.dismiss()
                            refreshDemandData()
                            getWalletList(null)
                        } else {
                            FryingUtil.showToast(mContext, if (returnData?.msg == null) "null" else returnData.msg)
                        }
                    }
                })
            }
        })
        demandChangeInWidget.show()
    }

    private fun getWalletList(callback: Runnable?) {
        WalletApiServiceHelper.getWalletList(mContext, false, object : Callback<ArrayList<Wallet?>?>() {
            override fun error(type: Int, error: Any) {
                callback?.run()
            }

            override fun callback(returnData: ArrayList<Wallet?>?) {
                if (returnData == null || returnData.isEmpty()) {
                    callback?.run()
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
}