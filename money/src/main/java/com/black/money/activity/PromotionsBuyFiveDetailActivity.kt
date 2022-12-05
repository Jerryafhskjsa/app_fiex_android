package com.black.money.activity

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.*
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActivity
import com.black.base.adapter.interfaces.OnItemClickListener
import com.black.base.api.MoneyApiServiceHelper
import com.black.base.api.WalletApiServiceHelper
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.model.SuccessObserver
import com.black.base.model.money.PromotionsBuyFive
import com.black.base.model.money.PromotionsBuyFiveDetail
import com.black.base.model.money.SupportCoin
import com.black.base.model.wallet.Wallet
import com.black.base.util.*
import com.black.lib.refresh.QRefreshLayout
import com.black.money.BR
import com.black.money.R
import com.black.money.adpter.PromotionsBuyCoinAdapter
import com.black.money.databinding.ActivityPromotionsBuyFiveDetailBinding
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import io.reactivex.Observer
import skin.support.content.res.SkinCompatResources
import java.math.BigDecimal
import java.util.*

@Route(value = [RouterConstData.PROMOTIONS_BUY_FIVE_DETAIL], beforePath = RouterConstData.LOGIN)
class PromotionsBuyFiveDetailActivity : BaseActivity(), View.OnClickListener, QRefreshLayout.OnRefreshListener {
    private var dm: DisplayMetrics? = null
    private var promotionsBuyFive: PromotionsBuyFive? = null

    private var binding: ActivityPromotionsBuyFiveDetailBinding? = null

    private var imageLoader: ImageLoader? = null
    private var promotionsBuyDetail: PromotionsBuyFiveDetail? = null
    private var listRefreshTime: Long = 0
    private val handler = Handler()
    private var timerCommand: TimerCommand? = null
    private val walletCache: MutableMap<String, Wallet?> = HashMap()

    private var userInfoObserver: Observer<String?>? = createUserInfoObserver()
    private fun createUserInfoObserver(): Observer<String?> {
        return object : SuccessObserver<String?>() {
            override fun onSuccess(value: String?) {
                onUserInfoChanged()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        promotionsBuyFive = intent.getParcelableExtra(ConstData.PROMOTIONS_BUY_FIVE)
        if (promotionsBuyFive == null || promotionsBuyFive?.id == null) {
            finish()
            return
        }
        imageLoader = ImageLoader(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_promotions_buy_five_detail)

        binding?.actionBarTitle?.text = getString(R.string.purchase_five_title2, promotionsBuyFive?.coinType)
        binding?.promotionsRecord?.setOnClickListener(this)
        dm = resources.displayMetrics
        var params = binding?.bannerImage?.layoutParams
        if (params == null) {
            params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (dm!!.widthPixels * 0.56).toInt())
        } else {
            params.height = (dm!!.widthPixels * 0.56).toInt()
        }
        binding?.bannerImage?.layoutParams = params
        initInnerWebView(binding?.webView!!)
        binding?.btnBuy?.setOnClickListener(this)
        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(this))
        binding?.refreshLayout?.setOnRefreshListener(this)
        getPromotionsBuyDetail(true)
        walletList
    }

    private val walletList: Unit
        get() {
            WalletApiServiceHelper.getWalletList(this, true, object : Callback<ArrayList<Wallet?>?>() {
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
                }
            })
        }

    override fun onClick(view: View) {
        val id = view.id
        if (id == R.id.promotions_record) {
            val bundle = Bundle()
            bundle.putParcelable(ConstData.PROMOTIONS_BUY_FIVE, promotionsBuyFive)
            BlackRouter.getInstance().build(RouterConstData.PROMOTIONS_BUY_FIVE_RECORD).with(bundle).go(this)
        } else if (id == R.id.btn_buy) {
            showBuyDialog(promotionsBuyDetail)
        }
    }

    override fun onRefresh() {
        getPromotionsBuyDetail(false)
    }

    override fun onResume() {
        super.onResume()
        if (userInfoObserver == null) {
            userInfoObserver = createUserInfoObserver()
        }
        SocketDataContainer.subscribeUserInfoObservable(userInfoObserver)
        checkListAddTimer()
    }

    override fun onStop() {
        super.onStop()
        if (userInfoObserver != null) {
            SocketDataContainer.removeUserInfoObservable(userInfoObserver)
        }
        if (timerCommand != null) {
            handler.removeCallbacks(timerCommand)
            timerCommand = null
        }
    }

    //用户信息被修改，刷新钱包
    private fun onUserInfoChanged() {
        walletList
    }

    private fun getPromotionsBuyDetail(isShowLoading: Boolean) {
        MoneyApiServiceHelper.getPromotionsBuyFiveDetail(this, NumberUtil.formatNumberNoGroup(promotionsBuyFive?.id), isShowLoading, object : NormalCallback<HttpRequestResultData<PromotionsBuyFiveDetail?>?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
                binding?.refreshLayout?.setRefreshing(false)
                promotionsBuyDetail = null
                showPromotionsBuyDetail()
            }

            override fun callback(returnData: HttpRequestResultData<PromotionsBuyFiveDetail?>?) {
                binding?.refreshLayout?.setRefreshing(false)
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    promotionsBuyDetail = returnData.data
                    if (promotionsBuyDetail != null) {
                        promotionsBuyDetail?.purchaseId = NumberUtil.formatNumberNoGroup(promotionsBuyFive?.id)
                    }
                } else {
                    promotionsBuyDetail = null
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
                showPromotionsBuyDetail()
            }
        })
    }

    private fun showPromotionsBuyDetail() {
        if (promotionsBuyDetail != null) {
            promotionsBuyDetail?.systemTime = System.currentTimeMillis()
        }
        imageLoader?.loadImage(binding?.bannerImage, if (promotionsBuyDetail == null) null else UrlConfig.getHost(this) + promotionsBuyDetail?.bannerUrl)
        //        imageLoader.loadImage(infoView, promotionsBuyDetail == null ? null : (UrlConfig.getHost(this) + promotionsBuyDetail.infoImageUrl), ((long) (dm.density * 1.5d * 1024 * 1024)));
        if (promotionsBuyDetail != null && promotionsBuyDetail?.ruleText != null) {
            val ruleText = CommonUtil.setWebViewDataDefaultTextColor(promotionsBuyDetail?.ruleText, SkinCompatResources.getColor(this, R.color.T2))
            binding?.webView?.loadDataWithBaseURL(null, ruleText, "text/html", "utf-8", null)
        }
        binding?.amount?.text = String.format("%s/%s",
                if (promotionsBuyDetail == null || promotionsBuyDetail?.nowAmountResult == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(promotionsBuyDetail?.nowAmountResult, 8, 0, 0),
                if (promotionsBuyDetail == null || promotionsBuyDetail?.totalAmountResult == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(promotionsBuyDetail?.totalAmountResult, 8, 0, 0))
        binding?.personCount?.text = if (promotionsBuyDetail?.personNum == null) nullAmount else NumberUtil.formatNumberNoGroupHardScale(promotionsBuyDetail?.personNum, 0)
        binding?.price?.text = if (promotionsBuyDetail == null) nullAmount else promotionsBuyDetail?.priceDisplay
        binding?.startDate?.text = if (promotionsBuyDetail == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm", promotionsBuyDetail?.startTime
                ?: 0)
        binding?.endDate?.text = if (promotionsBuyDetail == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm", promotionsBuyDetail?.endTime
                ?: 0)
        listRefreshTime = SystemClock.elapsedRealtime()
        checkListAddTimer()
        refreshButton()
    }

    private fun refreshButton() {
        if (promotionsBuyDetail != null) {
            when (promotionsBuyDetail?.statusCode) {
                1 -> {
                    binding?.btnBuy?.isEnabled = true
                    val thisTime = (promotionsBuyDetail?.systemTime
                            ?: 0) + (SystemClock.elapsedRealtime() - listRefreshTime)
                    binding?.btnBuy?.text = getString(R.string.buy_immediately, getTimeDisplay(promotionsBuyDetail?.endTime
                            ?: 0, thisTime))
                }
                2 -> {
                    binding?.btnBuy?.isEnabled = false
                    binding?.btnBuy?.setText(R.string.promotions_done)
                }
                3 -> {
                    binding?.btnBuy?.isEnabled = false
                    binding?.btnBuy?.setText(R.string.promotions_finish)
                }
                else -> {
                    binding?.btnBuy?.isEnabled = false
                    val thisTime = (promotionsBuyDetail?.systemTime
                            ?: 0) + (SystemClock.elapsedRealtime() - listRefreshTime)
                    binding?.btnBuy?.text = getString(R.string.start_wait, getTimeDisplay(promotionsBuyDetail?.startTime
                            ?: 0, thisTime))
                }
            }
        }
    }

    private fun getTimeDisplay(finishTime: Long, thisTime: Long): String {
        var result = "00:00:00"
        var rangeTime = finishTime - thisTime
        if (rangeTime <= 0) {
            return "00:00:00"
        }
        if (rangeTime > 1000) {
            rangeTime /= 1000
            val d = (rangeTime / 24 / 3600).toInt()
            rangeTime %= (24 * 3600)
            val sb = StringBuilder()
            if (d > 0) {
                sb.append(d).append(" ")
            }
            val h = (rangeTime / 3600).toInt()
            rangeTime %= 3600
            val m = (rangeTime / 60).toInt()
            rangeTime %= 60
            val s = rangeTime.toInt()
            sb.append(CommonUtil.twoBit(h)).append(":").append(CommonUtil.twoBit(m)).append(":").append(CommonUtil.twoBit(s))
            result = sb.toString()
        }
        return result
    }

    private inner class TimerCommand : Runnable {
        override fun run() {
            refreshButton()
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
        var hasNew = false
        if (promotionsBuyDetail != null) {
            val statusCode = promotionsBuyDetail?.statusCode
            if (statusCode == 0) {
                //未开始判断开始时间和当前时间
                val startTime = promotionsBuyDetail?.startTime ?: 0
                val thisTime = (promotionsBuyDetail?.systemTime
                        ?: 0) + (SystemClock.elapsedRealtime() - listRefreshTime)
                if (startTime > thisTime) {
                    hasNew = true
                }
            }
            if (statusCode == 1) {
                //未开始判断开始时间和当前时间
                val endTime = promotionsBuyDetail?.endTime ?: 0
                val thisTime = (promotionsBuyDetail?.systemTime
                        ?: 0) + (SystemClock.elapsedRealtime() - listRefreshTime)
                if (endTime > thisTime) {
                    hasNew = true
                }
            }
        }
        return hasNew
    }

    //刷新倒计时
    private fun checkStatusChanged(): Boolean {
        val hasNew = false
        if (promotionsBuyDetail != null) {
            var statusCode = promotionsBuyDetail?.statusCode
            if (statusCode == 0) {
                //未开始判断开始时间和当前时间
                val startTime = promotionsBuyDetail?.startTime ?: 0
                val thisTime = (promotionsBuyDetail?.systemTime
                        ?: 0) + (SystemClock.elapsedRealtime() - listRefreshTime)
                if (startTime <= thisTime) {
                    promotionsBuyDetail?.status = 1
                    statusCode = 1
                }
            }
            if (statusCode == 1) {
                //未开始判断开始时间和当前时间
                val endTime = promotionsBuyDetail?.endTime ?: 0
                val thisTime = (promotionsBuyDetail?.systemTime
                        ?: 0) + (SystemClock.elapsedRealtime() - listRefreshTime)
                if (endTime <= thisTime) {
                    promotionsBuyDetail?.status = 2
                }
            }
        }
        return hasNew
    }

    //刷新倒计时
    private fun refreshCountdown(): Boolean {
        checkStatusChanged()
        val check = checkNeedTimer()
        if (!check) {
            if (timerCommand != null) {
                handler.removeCallbacks(timerCommand)
                timerCommand = null
            }
        }
        return check
    }

    private fun showBuyDialog(promotions: PromotionsBuyFiveDetail?) {
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_promotions_buy_five, null)
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
        val coinTypeView = contentView.findViewById<TextView>(R.id.coin_type)
        coinTypeView.text = if (promotions?.coinType == null) "" else promotions.coinType
        val priceView = contentView.findViewById<TextView>(R.id.price)
        val amountHintView = contentView.findViewById<TextView>(R.id.amount_hint)
        val amountView = contentView.findViewById<EditText>(R.id.amount)
        val useAmountView = contentView.findViewById<TextView>(R.id.use_amount)
        val recyclerView: RecyclerView = contentView.findViewById(R.id.recycler_view)
        val gridLayoutManager = GridLayoutManager(this, 3)
        gridLayoutManager.orientation = RecyclerView.VERTICAL
        gridLayoutManager.isSmoothScrollbarEnabled = true
        recyclerView.layoutManager = gridLayoutManager
        val dm = resources.displayMetrics
        val decoration = SpacesItemDecoration((dm.density * 10).toInt())
        recyclerView.addItemDecoration(decoration)
        val minAmount: Double = promotions?.minAmount ?: 1.0
        val adapter = PromotionsBuyCoinAdapter(mContext, BR.listItemPromotionsBuyCoinModel, promotions?.supportCoin)
        adapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
                val supportCoinSelected: SupportCoin? = adapter.getItem(position)
                val priceString = ((if (supportCoinSelected?.price == null) nullAmount else supportCoinSelected.price)
                        + (if (supportCoinSelected?.coin == null) nullAmount else supportCoinSelected.coin)
                        + " = "
                        + 1
                        + if (promotions?.coinType == null) "" else promotions.coinType)
                priceView.text = priceString
                val amountText = binding?.amount?.text.toString()
                val amount = CommonUtil.parseDouble(amountText)
                if (amount == null) {
                    amountHintView.text = ""
                } else {
                    val price = if (supportCoinSelected == null) null else CommonUtil.parseDouble(supportCoinSelected.price)
                    if (price == null) {
                        amountHintView.text = ""
                    } else {
                        val amountHint = String.format("（%s %s ≈ %s %s）", amountText, "份",
                                NumberUtil.formatNumberDynamicScaleNoGroup(price * amount * minAmount, 8, 0, 8),
                                if (supportCoinSelected?.coin == null) nullAmount else supportCoinSelected.coin)
                        amountHintView.text = amountHint
                    }
                }
                adapter.setCheckedIndex(position)
                adapter.notifyDataSetChanged()
                val priceDouble = if (supportCoinSelected == null) null else CommonUtil.parseDouble(supportCoinSelected.price)
                val wallet = if (supportCoinSelected == null) null else walletCache[supportCoinSelected.coin]
                val useAmount = if (supportCoinSelected == null) nullAmount else String.format("%s 份", if (wallet?.coinAmount == null || priceDouble == null) nullAmount else NumberUtil.formatNumberNoGroupHardScale(wallet.coinAmount!! / BigDecimal(priceDouble), 2))
                useAmountView.text = useAmount
            }

        })
        adapter.setCheckedIndex(0)
        val supportCoin = if (promotions?.supportCoin == null || promotions.supportCoin!!.isEmpty()) null else promotions.supportCoin!![0]
        val priceDouble = CommonUtil.parseDouble(supportCoin?.price)
        val price = String.format("%s%s = %s%s",
                if (priceDouble == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(priceDouble * minAmount, 8, 0, 2),
                if (supportCoin?.coin == null) nullAmount else supportCoin.coin,
                "1",
                "份")
        priceView.text = price
        val wallet = if (supportCoin == null) null else walletCache[supportCoin.coin]
        val useAmount = if (supportCoin == null) nullAmount else String.format("%s 份", if (wallet?.coinAmount == null || priceDouble == null) nullAmount else NumberUtil.formatNumberNoGroupHardScale(wallet.coinAmount!! / BigDecimal(priceDouble), 2))
        useAmountView.text = useAmount
        recyclerView.adapter = adapter
        //解决数据加载不完的问题
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.setHasFixedSize(true)
        //解决数据加载完成后, 没有停留在顶部的问题
        recyclerView.isFocusable = false
        amountView.hint = promotions?.getCopyHintText(this) ?: nullAmount
        amountView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                val amountText = amountView.text.toString()
                val amount = CommonUtil.parseDouble(amountText)
                if (amount == null) {
                    amountHintView.text = ""
                } else {
                    val supportCoinSelected = adapter.getItem(adapter.getCheckedIndex())
                    val priceSelected = if (supportCoinSelected == null) null else CommonUtil.parseDouble(supportCoinSelected.price)
                    if (priceSelected == null) {
                        amountHintView.text = ""
                    } else {
                        val amountHint = String.format("（%s %s ≈ %s %s）", amountText, "份",
                                NumberUtil.formatNumberDynamicScaleNoGroup(priceSelected * amount * minAmount, 8, 0, 8),
                                if (supportCoinSelected?.coin == null) nullAmount else supportCoinSelected.coin)
                        amountHintView.text = amountHint
                    }
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
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
            val coinType = adapter.getItem(adapter.getCheckedIndex())?.coin
            MoneyApiServiceHelper.promotionsBuyFiveCreate(mContext, amountText, coinType, promotions?.purchaseId.toString(), object : NormalCallback<HttpRequestResultString?>(mContext!!) {
                override fun callback(returnData: HttpRequestResultString?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        FryingUtil.showToast(mContext, getString(R.string.promotions_buy_success))
                        alertDialog.dismiss()
                        getPromotionsBuyDetail(true)
                    } else {
                        FryingUtil.showToast(mContext, if (returnData?.msg == null) getString(R.string.promotions_buy_failed) else returnData.msg)
                    }
                }
            })
        })
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}