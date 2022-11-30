package com.black.wallet.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.WalletApiService
import com.black.base.api.WalletApiServiceHelper
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.model.SuccessObserver
import com.black.base.model.wallet.Wallet
import com.black.base.model.wallet.WalletConfig
import com.black.base.model.wallet.WalletLever
import com.black.base.model.wallet.WalletLeverDetail
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.*
import com.black.base.view.DeepControllerWindow
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.black.wallet.R
import com.black.wallet.databinding.ActivityWalletTransferBinding
import io.reactivex.Observer
import java.math.BigDecimal
import java.math.RoundingMode

@Route(value = [RouterConstData.WALLET_TRANSFER], beforePath = RouterConstData.LOGIN)
class WalletTransferActivity : BaseActionBarActivity(), View.OnClickListener {
    private var binding: ActivityWalletTransferBinding? = null

    private var fromType = 1
    private var toType = 2

    private var pair: String? = null
    private var coinName: String? = null
    private var setName: String? = null
    private var coinType: String? = null
    private val coinTypeList = ArrayList<String>()

    private var wallet: Wallet? = null
    private val walletCache: HashMap<String, Wallet?> = HashMap()
    private val leverWalletCache: HashMap<String, Wallet?> = HashMap()

    private var leverDetail: WalletLeverDetail? = null
    private var leverDetailObserver: Observer<WalletLeverDetail?>? = createLeverDetailObserver()
    private var userInfoObserver: Observer<String?>? = createUserInfoObserver()
    private var userLeverObserver: Observer<String?>? = createUserLeverObserver()

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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_wallet_transfer)

        binding?.reverse?.setOnClickListener(this)
        binding?.coinType?.setOnClickListener(this)
        binding?.total?.setOnClickListener(this)
        binding?.btnTransfer?.setOnClickListener(this)
        binding?.btnTransaction?.setOnClickListener(this)

        onCoinTypeChanged()
        onTypeChanged()
//        getAllWallet()
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return "划转"
    }

    override fun initToolbarViews(toolbar: Toolbar) {
        val bnFilter = toolbar.findViewById<View>(R.id.filter_layout)
        bnFilter?.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.filter_layout -> {
                val bundle = Bundle()
                bundle.putString(ConstData.PAIR, pair)
                BlackRouter.getInstance().build(RouterConstData.WALLET_TRANSFER_RECORD).with(bundle).go(this)
            }
            R.id.reverse -> {
                val temp = fromType
                fromType = toType
                toType = temp
                onTypeChanged()
            }
            R.id.coin_type -> {
                DeepControllerWindow<String>(this, null, coinType, coinTypeList,
                        object : DeepControllerWindow.OnReturnListener<String> {
                            override fun onReturn(window: DeepControllerWindow<String>, item: String) {
                                coinType = item
                                onCoinTypeChanged()
                            }

                        }).show()
            }
            R.id.total -> {
                if (fromType == 1) {
                    wallet?.run {
                        binding?.transferAmount?.setText(NumberUtil.formatNumberNoGroupScale(wallet?.coinAmount, RoundingMode.FLOOR, 0, 8))
                    }
                } else {
                    leverDetail?.also {
                        if (TextUtils.equals(coinName, coinType) && leverDetail?.coinMaxTransfer != null) {
                            binding?.transferAmount?.setText(NumberUtil.formatNumberNoGroupScale(leverDetail?.coinMaxTransfer, RoundingMode.FLOOR, 0, 8))
                        } else if (TextUtils.equals(setName, coinType) && leverDetail?.afterCoinMaxTransfer != null) {
                            binding?.transferAmount?.setText(NumberUtil.formatNumberNoGroupScale(leverDetail?.afterCoinMaxTransfer, RoundingMode.FLOOR, 0, 8))
                        }
                    }
                }
            }
            R.id.btn_transfer -> {
                val amountString = binding?.transferAmount?.text.toString()
                val amount = CommonUtil.parseDouble(amountString)
                if (amount == null) {
                    FryingUtil.showToastError(this, "请填写正确的数量")
                    return
                }
                ApiManager.build(this).getService(WalletApiService::class.java)
                        ?.walletTransfer(coinType, amountString, fromType.toString(), pair)
                        ?.compose(RxJavaHelper.observeOnMainThread())
                        ?.subscribe(HttpCallbackSimple(this, true, object : NormalCallback<HttpRequestResultString?>(mContext!!) {
                            override fun callback(returnData: HttpRequestResultString?) {
                                if (returnData?.code != null && returnData.code != null && returnData.code == HttpRequestResult.SUCCESS) {
                                    FryingUtil.showToast(mContext, "划转成功")
//                                    getAllWallet()
                                    getWalletLeverDetail(true)
                                } else {
                                    FryingUtil.showToast(mContext, if (returnData?.msg == null) "null" else returnData.msg)
                                }
                            }

                        }))
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
        }
    }

    override fun onResume() {
        super.onResume()
        getWalletLeverDetail(false)
        if (userInfoObserver == null) {
            userInfoObserver = createUserInfoObserver()
        }
        SocketDataContainer.subscribeUserInfoObservable(userInfoObserver)
        if (userLeverObserver == null) {
            userLeverObserver = createUserLeverObserver()
        }
    }

    override fun onStop() {
        super.onStop()
        if (userInfoObserver != null) {
            SocketDataContainer.removeUserInfoObservable(userInfoObserver)
        }
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

    private fun createUserInfoObserver(): Observer<String?> {
        return object : SuccessObserver<String?>() {
            override fun onSuccess(value: String?) {
                onUserInfoChanged()
            }
        }
    }

    private fun createUserLeverObserver(): Observer<String?> {
        return object : SuccessObserver<String?>() {
            override fun onSuccess(value: String?) {
                onUserInfoChanged()
            }
        }
    }

    private fun onTypeChanged() {
        binding?.from?.setText(if (fromType == 1) "币币账户" else String.format("%s逐仓账户", pair?.replace("_", "/")))
        binding?.to?.setText(if (toType == 1) "币币账户" else String.format("%s逐仓账户", pair?.replace("_", "/")))
        val transferHint = if (fromType == 1) "只有将资产划转到对应账户才可进行交易。账户间的划转不收取手续费。"
        else String.format("只有将资产划转到对应账户才可进行交易。账户间的划转不收取手续费。\n" +
                "杠杆账户转出资金后的风险率不能低于%s%%", if (leverDetail?.outRate == null) nullAmount else NumberUtil.formatNumberNoGroupHardScale(leverDetail?.outRate!! * BigDecimal(100), 2))
        binding?.transferHint?.setText(transferHint)
        refreshUsable()
    }

    private fun onCoinTypeChanged() {
        binding?.transferCoinType?.setText(coinType)
        binding?.coinType?.setText(coinType)
        refreshUsable()
    }

    private fun refreshUsable() {
        if (fromType == 1) {
            wallet = walletCache[coinType]
            binding?.usable?.setText(String.format("可划转 %s %s", if (wallet == null || wallet?.coinAmount == null) nullAmount else NumberUtil.formatNumberNoGroupScale(wallet?.coinAmount, RoundingMode.FLOOR, 0, 8), coinType))
        } else {
            if (TextUtils.equals(coinName, coinType)) {
                binding?.usable?.setText(String.format("可划转 %s %s", if (leverDetail == null || leverDetail?.coinMaxTransfer == null) nullAmount else NumberUtil.formatNumberNoGroupScale(leverDetail?.coinMaxTransfer, RoundingMode.FLOOR, 0, 8), coinType))
            } else if (TextUtils.equals(setName, coinType)) {
                binding?.usable?.setText(String.format("可划转 %s %s", if (leverDetail == null || leverDetail?.afterCoinMaxTransfer == null) nullAmount else NumberUtil.formatNumberNoGroupScale(leverDetail?.afterCoinMaxTransfer, RoundingMode.FLOOR, 0, 8), coinType))
            }
        }
    }

    private fun getAllWallet() {
        WalletApiServiceHelper.getWalletAll(this, HttpCallbackSimple(this, false, object : Callback<HttpRequestResultData<WalletConfig?>?>() {
            override fun callback(returnData: HttpRequestResultData<WalletConfig?>?) {
                if (returnData?.code == HttpRequestResult.SUCCESS && returnData.data != null) {
                    val normalWalletList: ArrayList<Wallet?>? = returnData.data?.userCoinAccountVO
                    normalWalletList?.run {
                        var coinWallet: Wallet? = null
                        var setWallet: Wallet? = null
                        val coin = coinTypeList[0]
                        val set = coinTypeList[1]
                        for (wallet in normalWalletList) {
                            if (TextUtils.equals(wallet?.coinType, coin)) {
                                coinWallet = wallet
                                walletCache[coin] = wallet
                            }
                            if (TextUtils.equals(wallet?.coinType, set)) {
                                setWallet = wallet
                                walletCache[set] = wallet
                            }
                            if (coinWallet != null && setWallet != null) {
                                break
                            }
                        }
                    }
                    val leverWalletList: ArrayList<WalletLever?>? = returnData.data?.userCoinAccountLeverVO
                    leverWalletList?.run {
                        for (wallet in leverWalletList) {
                            if (TextUtils.equals(wallet?.pair, pair)) {
                                wallet?.coinType?.also {
                                    leverWalletCache[it] = wallet.createCoinWallet()
                                }
                                wallet?.afterCoinType?.also {
                                    leverWalletCache[it] = wallet.createSetWallet()
                                }
                                break
                            }
                        }
                    }
                }
            }

            override fun error(type: Int, error: Any?) {
            }

        }))
//        ApiManager.build(this).getService(WalletApiService::class.java)
//                .getWallet(null)
//                .flatMap(object : Function<HttpRequestResultData<WalletConfig?>?, Observable<Int>> {
//                    override fun apply(returnData: HttpRequestResultData<WalletConfig?>): Observable<Int> {
//                        if (returnData.code == HttpRequestResult.SUCCESS && returnData.data != null) {
//                            val normalWalletList: ArrayList<Wallet?>? = returnData.data?.userCoinAccountVO
//                            normalWalletList?.run {
//                                var coinWallet: Wallet? = null
//                                var setWallet: Wallet? = null
//                                val coin = coinTypeList[0]
//                                val set = coinTypeList[1]
//                                for (wallet in normalWalletList) {
//                                    if (TextUtils.equals(wallet?.coinType, coin)) {
//                                        coinWallet = wallet
//                                        walletCache[coin] = wallet
//                                    }
//                                    if (TextUtils.equals(wallet?.coinType, set)) {
//                                        setWallet = wallet
//                                        walletCache[set] = wallet
//                                    }
//                                    if (coinWallet != null && setWallet != null) {
//                                        break
//                                    }
//                                }
//                            }
//                            val leverWalletList: ArrayList<WalletLever?>? = returnData.data?.userCoinAccountLeverVO
//                            leverWalletList?.run {
//                                for (wallet in leverWalletList) {
//                                    if (TextUtils.equals(wallet?.pair, pair)) {
//                                        wallet?.coinType?.also {
//                                            leverWalletCache[it] = wallet.createCoinWallet()
//                                        }
//                                        wallet?.afterCoinType?.also {
//                                            leverWalletCache[it] = wallet.createSetWallet()
//                                        }
//                                        break
//                                    }
//                                }
//                            }
//                            return Observable.just(1)
//                        } else {
//                            return Observable.just(null)
//                        }
//                    }
//
//                })
//                .compose(RxJavaHelper.observeOnMainThread())
//                .subscribe {
//                    refreshUsable()
//                }
//                .run { }
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
        refreshUsable()
    }

    private fun onUserInfoChanged() {
//        getAllWallet()
    }
}