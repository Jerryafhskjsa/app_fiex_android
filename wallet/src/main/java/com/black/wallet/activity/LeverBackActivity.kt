package com.black.wallet.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.WalletApiService
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.model.SuccessObserver
import com.black.base.model.wallet.WalletLeverDetail
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.*
import com.black.base.view.DeepControllerWindow
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.black.wallet.R
import com.black.wallet.databinding.ActivityLeverBackBinding
import io.reactivex.Observer
import java.math.BigDecimal
import java.math.RoundingMode

@Route(value = [RouterConstData.LEVER_BACK], beforePath = RouterConstData.LOGIN)
class LeverBackActivity : BaseActionBarActivity(), View.OnClickListener {
    private var pair: String? = null

    private var coinName: String? = null
    private var setName: String? = null
    private var coinType: String? = null
    private val coinTypeList = ArrayList<String>()

    private var leverDetail: WalletLeverDetail? = null

    private var binding: ActivityLeverBackBinding? = null

    private val watcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            checkClickable()
        }

        override fun afterTextChanged(s: Editable) {}
    }

    private var leverDetailObserver: Observer<WalletLeverDetail?>? = createLeverDetailObserver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pair = intent.getStringExtra(ConstData.PAIR)
        val coinArray = pair?.split("_")
        if (coinArray == null || coinArray.size != 2) {
            finish()
            return
        } else {
            coinName = coinArray[0]
            setName = coinArray[1]
            coinType = coinArray[0]
            coinTypeList.add(coinArray[0])
            coinTypeList.add(coinArray[1])
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_lever_back)

        binding?.type?.setText(String.format("%s 逐仓账户", pair?.replace("_", "/") ?: nullAmount))
        binding?.amount?.addTextChangedListener(watcher)

        binding?.coinTypeLayout?.setOnClickListener(this)
        binding?.total?.setOnClickListener(this)
        binding?.btnSubmit?.setOnClickListener(this)
        binding?.btnTransaction?.setOnClickListener(this)

        refreshCoinTypeExtra()
        refreshUsable()
        checkClickable()
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun initToolbarViews(toolbar: Toolbar) {
        val bnFilter = toolbar!!.findViewById<View>(R.id.filter_layout)
        bnFilter?.setOnClickListener(this)
    }

    override fun getTitleText(): String {
        pair = intent.getStringExtra(ConstData.PAIR)
        return "归还"
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.filter_layout -> {
                val bundle = Bundle()
                bundle.putString(ConstData.PAIR, pair)
                BlackRouter.getInstance().build(RouterConstData.LEVER_BACK_RECORD).with(bundle).go(this)
            }
            R.id.coin_type_layout -> {
                DeepControllerWindow<String>(this, null, coinType, coinTypeList,
                        object :DeepControllerWindow.OnReturnListener<String> {
                            override fun onReturn(window: DeepControllerWindow<String>, item: String) {
                                coinType = item
                                onCoinTypeChanged()
                            }

                        }).show()
            }
            R.id.total -> {
                leverDetail?.also {
                    if (TextUtils.equals(coinName, coinType)) {
                        var backTotal = if (leverDetail?.coinBorrow == null && leverDetail?.coinInterest == null) null else
                            (BigDecimal.ZERO + ((leverDetail?.coinBorrow
                                    ?: BigDecimal.ZERO)) + ((leverDetail?.coinInterest
                                    ?: BigDecimal.ZERO)))
                        backTotal?.also {
                            if (leverDetail?.coinAmount != null && leverDetail?.coinAmount!! < backTotal) {
                                backTotal = leverDetail?.coinAmount
                            }
                            binding?.amount?.setText(NumberUtil.formatNumberNoGroupScale(backTotal, RoundingMode.FLOOR, 0, 8))
                        }
                    } else if (TextUtils.equals(setName, coinType)) {
                        var backTotal = if (leverDetail?.afterCoinBorrow == null && leverDetail?.afterCoinInterest == null) null else
                            (BigDecimal.ZERO + ((leverDetail?.afterCoinBorrow
                                    ?: BigDecimal.ZERO)) + ((leverDetail?.afterCoinInterest
                                    ?: BigDecimal.ZERO)))
                        backTotal?.also {
                            if (leverDetail?.afterCoinAmount != null && leverDetail?.afterCoinAmount!! < backTotal) {
                                backTotal = leverDetail?.afterCoinAmount
                            }
                            binding?.amount?.setText(NumberUtil.formatNumberNoGroupScale(backTotal, RoundingMode.FLOOR, 0, 8))
                        }
                    }
                }
            }
            R.id.btn_transaction -> {
                val bundle = Bundle()
                bundle.putInt(ConstData.HOME_FRAGMENT_INDEX, 2)
                bundle.putInt(ConstData.TRANSACTION_INDEX, 1)
                bundle.putInt(ConstData.TRANSACTION_TYPE, ConstData.TAB_LEVER)
                BlackRouter.getInstance().build(RouterConstData.TRANSACTION)
                        .with(bundle)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .go(this) { routeResult, error ->
                            if (error != null) {
                                FryingUtil.printError(error)
                            }
                            if (routeResult) {
                                finish()
                            }
                        }
            }
            R.id.btn_submit -> {
                backLever()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getWalletLeverDetail(false)
        if (leverDetailObserver == null) {
            leverDetailObserver = createLeverDetailObserver()
        }
        SocketDataContainer.subscribeUserLeverDetailObservable(leverDetailObserver)
        val bundle = Bundle()
        bundle.putString(ConstData.PAIR, pair)
        SocketUtil.sendSocketCommandBroadcast(this, SocketUtil.COMMAND_LEVER_DETAIL_START, bundle)
    }

    override fun onStop() {
        super.onStop()
        if (leverDetailObserver != null) {
            SocketDataContainer.removeUserLeverDetailObservable(leverDetailObserver)
        }
        SocketUtil.sendSocketCommandBroadcast(this, SocketUtil.COMMAND_LEVER_DETAIL_FINISH)
    }

    private fun createLeverDetailObserver(): Observer<WalletLeverDetail?>? {
        return object : SuccessObserver<WalletLeverDetail?>() {
            override fun onSuccess(value: WalletLeverDetail?) {
                if (value != null && TextUtils.equals(value.pair, pair)) {
                    leverDetail = value
                    showDetail()
                }
            }
        }
    }

    private fun onCoinTypeChanged() {
        refreshCoinTypeExtra()
        refreshDetail()
        refreshUsable()
    }

    private fun refreshCoinTypeExtra() {
        binding?.coinType?.setText(coinType)
        binding?.amountCoinType?.setText(coinType)
    }

    private fun refreshUsable() {
        if (TextUtils.equals(coinName, coinType)) {
            binding?.usable?.setText(String.format("可用 %s %s", if (leverDetail == null || leverDetail?.coinAmount == null) nullAmount else NumberUtil.formatNumberNoGroupScale(leverDetail?.coinAmount, RoundingMode.FLOOR, 0, 8), coinType))
        } else if (TextUtils.equals(setName, coinType)) {
            binding?.usable?.setText(String.format("可用 %s %s", if (leverDetail == null || leverDetail?.afterCoinAmount == null) nullAmount else NumberUtil.formatNumberNoGroupScale(leverDetail?.afterCoinAmount, RoundingMode.FLOOR, 0, 8), coinType))
        }
    }

    private fun getWalletLeverDetail(force: Boolean) {
        SocketDataContainer.getWalletLeverDetail(this, pair, force)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(this, false, object : NormalCallback<WalletLeverDetail>(mContext!!) {

                    override fun callback(returnData: WalletLeverDetail?) {
                        leverDetail = returnData
                        showDetail()
                    }

                }))
    }

    private fun showDetail() {
        refreshDetail()
        refreshUsable()
    }

    private fun refreshDetail() {
        if (TextUtils.equals(coinName, coinType)) {
            binding?.backBorrow?.setText(String.format("%s%s",
                    if (leverDetail?.coinBorrow == null) nullAmount else NumberUtil.formatNumberNoGroupScale(leverDetail?.coinBorrow, RoundingMode.FLOOR, 0, 8),
                    coinType))
            binding?.backInterest?.setText(String.format("%s%s",
                    if (leverDetail?.coinInterest == null) nullAmount else NumberUtil.formatNumberNoGroupScale(leverDetail?.coinInterest, RoundingMode.FLOOR, 0, 8),
                    coinType))
            val backTotal = if (leverDetail?.coinBorrow == null && leverDetail?.coinInterest == null) null else
                (BigDecimal.ZERO.add((leverDetail?.coinBorrow
                        ?: BigDecimal.ZERO)).add((leverDetail?.coinInterest
                        ?: BigDecimal.ZERO)))
            binding?.backTotal?.setText(String.format("%s%s",
                    if (backTotal == null) nullAmount else NumberUtil.formatNumberNoGroupScale(backTotal, RoundingMode.FLOOR, 0, 8),
                    coinType))
        } else if (TextUtils.equals(setName, coinType)) {
            binding?.backBorrow?.setText(String.format("%s%s",
                    if (leverDetail?.afterCoinBorrow == null) nullAmount else NumberUtil.formatNumberNoGroupScale(leverDetail?.afterCoinBorrow, RoundingMode.FLOOR, 0, 8),
                    coinType))
            binding?.backInterest?.setText(String.format("%s%s",
                    if (leverDetail?.afterCoinInterest == null) nullAmount else NumberUtil.formatNumberNoGroupScale(leverDetail?.afterCoinInterest, RoundingMode.FLOOR, 0, 8),
                    coinType))
            val backTotal = if (leverDetail?.afterCoinBorrow == null && leverDetail?.afterCoinInterest == null) null else
                (BigDecimal.ZERO.add((leverDetail?.afterCoinBorrow
                        ?: BigDecimal.ZERO)).add((leverDetail?.afterCoinInterest
                        ?: BigDecimal.ZERO)))
            binding?.backTotal?.setText(String.format("%s%s",
                    if (backTotal == null) nullAmount else NumberUtil.formatNumberNoGroupScale(backTotal, RoundingMode.FLOOR, 0, 8),
                    coinType))
        }
    }

    private fun checkClickable() {
        val amountDouble = CommonUtil.parseDouble(binding?.amount?.text.toString().trim { it <= ' ' })
        binding?.btnSubmit?.isEnabled = amountDouble != null && leverDetail != null
    }

    private fun backLever() {
        val amountString = binding?.amount?.text.toString()
        val amount = CommonUtil.parseDouble(amountString)
        if (amount == null) {
            FryingUtil.showToastError(this, "请填写正确的数量")
            return
        }
        ApiManager.build(this).getService(WalletApiService::class.java)
                ?.walletLeverBorrow(amountString, coinType, pair, "REPAYMENT")
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(this, true, object : NormalCallback<HttpRequestResultString?>(mContext!!) {
                    override fun callback(returnData: HttpRequestResultString?) {
                        if (returnData?.code != null && returnData.code != null && returnData.code == HttpRequestResult.SUCCESS) {
                            FryingUtil.showToast(mContext, "归还成功")
                            getWalletLeverDetail(true)
                        } else {
                            FryingUtil.showToast(mContext, if (returnData?.msg == null) "null" else returnData.msg)
                        }
                    }

                }))
    }
}