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
import com.black.base.model.NormalCallback
import com.black.base.model.SuccessObserver
import com.black.base.model.filter.RegularRecordStatus
import com.black.base.model.money.Regular
import com.black.base.model.money.RegularConfig
import com.black.base.model.wallet.Wallet
import com.black.base.util.*
import com.black.base.widget.ObserveScrollView
import com.black.lib.refresh.QRefreshLayout
import com.black.money.R
import com.black.money.databinding.ActivityRegularDetailBinding
import com.black.money.view.RegularChangeInWidget
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import io.reactivex.Observer
import skin.support.content.res.SkinCompatResources
import java.util.*

@Route(value = [RouterConstData.REGULAR_DETAIL], beforePath = RouterConstData.LOGIN)
class RegularDetailActivity : BaseActionBarActivity(), View.OnClickListener, ObserveScrollView.ScrollListener, QRefreshLayout.OnRefreshListener {
    private var bgC1: Drawable? = null
    private var bgB2: Drawable? = null
    private var bgDefault: Drawable? = null
    private var btnBackDefault: Drawable? = null
    private var btnBackNormal: Drawable? = null
    private var colorDefault = 0
    private var colorT1 = 0
    private var btnRecordImgDefault: Drawable? = null
    private var btnRecordImgScroll: Drawable? = null

    private var regular: Regular? = null

    private var binding: ActivityRegularDetailBinding? = null

    private var headView: View? = null
    private var btnBack: ImageButton? = null
    private var headTitleView: TextView? = null
    private var btnRecord: TextView? = null

    private var imageLoader: ImageLoader? = null
    private var regularCoins: ArrayList<String>? = null
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
        regular = intent.getParcelableExtra(ConstData.REGULAR)
        regularCoins = intent.getStringArrayListExtra(ConstData.REGULAR_COINS)
        if (regular == null) {
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

        binding = DataBindingUtil.setContentView(this, R.layout.activity_regular_detail)

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
        headTitleView?.text = "定利宝"
        view.findViewById<TextView>(R.id.action_bar_extras).also { btnRecord = it }.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.amount_layout -> {
                val bundle = Bundle()
                bundle.putInt(ConstData.MONEY_RECORD_TYPE, ConstData.TAB_REGULAR)
                bundle.putStringArrayList(ConstData.REGULAR_COINS, regularCoins)
                bundle.putParcelable(ConstData.REGULAR, regular)
                bundle.putParcelable(ConstData.REGULAR_STATUS, RegularRecordStatus.INTO)
                BlackRouter.getInstance().build(RouterConstData.MONEY_RECORD).with(bundle).go(this)
            }
            R.id.action_bar_extras -> {
                val bundle = Bundle()
                bundle.putInt(ConstData.MONEY_RECORD_TYPE, ConstData.TAB_REGULAR)
                bundle.putStringArrayList(ConstData.REGULAR_COINS, regularCoins)
                bundle.putParcelable(ConstData.REGULAR, regular)
                bundle.putParcelable(ConstData.REGULAR_STATUS, RegularRecordStatus.INTO)
                BlackRouter.getInstance().build(RouterConstData.MONEY_RECORD).with(bundle).go(this)
            }
            R.id.btn_change_in -> {
                if (regular == null || regular?.coinType == null) {
                    return
                }
                fryingHelper.checkUserAndDoing(Runnable {
                    val wallet = if (regular?.coinType == null) null else walletCache[regular?.coinType!!]
                    if (wallet == null) {
                        getWalletList(Runnable {
                            val newWallet = if (regular?.coinType == null) null else walletCache[regular?.coinType!!]
                            if (newWallet != null) {
                                showRegularChangeInWidget(newWallet, regular!!)
                            }
                        })
                    } else {
                        showRegularChangeInWidget(wallet, regular!!)
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
        refreshRegularData()
        getWalletList(null)
    }

    public override fun onStop() {
        super.onStop()
        if (userInfoObserver != null) {
            SocketDataContainer.removeUserInfoObservable(userInfoObserver)
        }
    }

    override fun onRefresh() {
        refreshRegularData()
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
        binding?.coinType?.text = String.format("定利宝-%s", if (regular?.coinType == null) nullAmount else regular?.coinType)
        binding?.rate?.text = String.format("%s%%", if (regular == null || regular?.annualrate == null) nullAmount else NumberUtil.formatNumberNoGroupHardScale(regular?.annualrate!! * 100, 2))
        binding?.rateDescription?.text = if (regular?.rateText == null) nullAmount else regular?.rateText
        if (regular != null && regular?.rateImg != null) {
            imageLoader?.loadImage(binding?.rateImage, UrlConfig.getHost(this) + regular?.rateImg)
        }
        binding?.rewordRule?.text = if (regular == null || regular?.interestText == null) nullAmount else regular?.interestText
        binding?.breakRule?.text = if (regular == null || regular?.defaultRateText == null) nullAmount else regular?.defaultRateText
        binding?.totalAmountTitle?.text = String.format("资产总额 (%s) ", if (regular == null || regular?.coinType == null) nullAmount else regular?.coinType)
        binding?.totalAmount?.text = if (regular == null || regular?.sumLockAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(regular?.sumLockAmount, 15, 0, 12)
        binding?.totalRewardTitle?.text = String.format("累计收益 (%s) ", if (regular == null || regular?.coinType == null) nullAmount else regular?.coinType)
        binding?.totalReward?.text = if (regular == null || regular?.totalInterestAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(regular?.totalInterestAmount, 15, 0, 12)
        binding?.lockDay?.text = String.format("锁定期：%s天", if (regular == null || regular?.day == null) nullAmount else NumberUtil.formatNumberNoGroup(regular?.day))
        binding?.btnChangeIn?.isEnabled = regular != null && regular?.status != null && regular?.status != 0
    }

    private fun refreshRegularData() {
        MoneyApiServiceHelper.getRegularConfig(mContext, object : NormalCallback<HttpRequestResultData<RegularConfig?>?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
                binding?.refreshLayout?.setRefreshing(false)
            }

            override fun callback(returnData: HttpRequestResultData<RegularConfig?>?) {
                binding?.refreshLayout?.setRefreshing(false)
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    val demands = if (returnData.data == null) null else returnData.data?.coinTypeConf
                    if (demands != null) {
                        for (temp in demands) {
                            if (temp != null && TextUtils.equals(temp.coinType, regular?.coinType)
                                    && temp.id != null && regular?.id != null && regular?.id == temp.id) {
                                regular = temp
                                refreshDemand()
                                break
                            }
                        }
                    }
                }
            }
        })
    }

    private fun showRegularChangeInWidget(wallet: Wallet, regular: Regular) {
        val regularChangeInWidget = RegularChangeInWidget(mContext, wallet, regular)
        regularChangeInWidget.setOnHandlerListener(object : OnHandlerListener<RegularChangeInWidget> {
            override fun onCancel(widget: RegularChangeInWidget) {
                widget.dismiss()
            }

            override fun onConfirm(widget: RegularChangeInWidget) {
                val amountText = widget.amountText
                val amount = CommonUtil.parseDouble(amountText)
                if (amount == null) {
                    FryingUtil.showToast(mContext, getString(R.string.alert_c2c_create_amount_error, ""))
                    return
                }
                MoneyApiServiceHelper.postRegularChangeIn(mContext, widget.amountText, NumberUtil.formatNumberNoGroup(regular.id), object : NormalCallback<HttpRequestResultString?>(mContext!!) {
                    override fun callback(returnData: HttpRequestResultString?) {
                        if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                            FryingUtil.showToast(mContext, "存入成功")
                            widget.dismiss()
                            refreshRegularData()
                            getWalletList(null)
                        } else {
                            FryingUtil.showToast(mContext, if (returnData?.msg == null) "null" else returnData.msg)
                        }
                    }
                })
            }
        })
        regularChangeInWidget.show()
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