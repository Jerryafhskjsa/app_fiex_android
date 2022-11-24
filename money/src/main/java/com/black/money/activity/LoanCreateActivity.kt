package com.black.money.activity

import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.MoneyApiServiceHelper
import com.black.base.api.WalletApiServiceHelper
import com.black.base.filter.NumberFilter
import com.black.base.filter.PointLengthFilter
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.model.SuccessObserver
import com.black.base.model.money.LoanConfig
import com.black.base.model.money.LoanConfigStage
import com.black.base.model.money.LoanConfigSub
import com.black.base.model.wallet.Wallet
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.util.SocketDataContainer
import com.black.base.view.DeepControllerWindow
import com.black.money.R
import com.black.money.databinding.ActivityLoanCreateBinding
import com.black.net.HttpRequestResult
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import io.reactivex.Observer
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

@Route(value = [RouterConstData.LOAN_CREATE], beforePath = RouterConstData.LOGIN)
class LoanCreateActivity : BaseActionBarActivity(), View.OnClickListener {
    private var loanConfigList: ArrayList<LoanConfig?>? = null

    private var binding: ActivityLoanCreateBinding? = null

    //异步获取数据
    private var handlerThread: HandlerThread? = null
    private var socketHandler: Handler? = null

    var borrowPrecision = 0
    var mortgagePrecision = 0

    private var currentWallet: Wallet? = null
    private val walletCache: MutableMap<String, Wallet?> = HashMap()

    private var userInfoObserver: Observer<String?>? = createUserInfoObserver()
    private fun createUserInfoObserver(): Observer<String?> {
        return object : SuccessObserver<String?>() {
            override fun onSuccess(value: String?) {
                getWalletList(Runnable {
                    if (currentWallet != null && currentWallet!!.coinType != null) {
                        getWallet(currentWallet!!.coinType!!)
                    }
                })
            }
        }
    }

    private val watcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            checkClickable()
        }

        override fun afterTextChanged(s: Editable) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loanConfigList = intent.getParcelableArrayListExtra(ConstData.LOAN_CONFIG_LIST)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_loan_create)

        binding?.mortgageCoinTypeLayout?.setOnClickListener(this)
        binding?.loanCoinTypeLayout?.setOnClickListener(this)
        binding?.mortgageAmount?.addTextChangedListener(watcher)
        binding?.loanAmount?.addTextChangedListener(watcher)
        binding?.rateLayout?.setOnClickListener(this)
        binding?.btnCreate?.setOnClickListener(this)
        var defaultIndex = 0
        val defaultCoinType = intent.getStringExtra(ConstData.COIN_TYPE)
        if (!TextUtils.isEmpty(defaultCoinType) && loanConfigList != null && loanConfigList!!.isNotEmpty()) {
            for (i in loanConfigList!!.indices) {
                val loanConfig = loanConfigList!![i]
                if (loanConfig != null && TextUtils.equals(defaultCoinType, loanConfig.coinType)) {
                    defaultIndex = i
                    break
                }
            }
        }
        onLoanConfigChanged(CommonUtil.getItemFromList(loanConfigList, defaultIndex))
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return "我要借贷"
    }

    override fun initToolbarViews(toolbar: Toolbar) {}
    override fun onResume() {
        super.onResume()
        if (userInfoObserver == null) {
            userInfoObserver = createUserInfoObserver()
        }
        SocketDataContainer.subscribeUserInfoObservable(userInfoObserver)
        getWalletList(null)
        handlerThread = HandlerThread(ConstData.SOCKET_HANDLER, Process.THREAD_PRIORITY_BACKGROUND)
        handlerThread!!.start()
        socketHandler = Handler(handlerThread!!.looper)
    }

    public override fun onStop() {
        super.onStop()
        if (userInfoObserver != null) {
            SocketDataContainer.removeUserInfoObservable(userInfoObserver)
        }
        if (socketHandler != null) {
            socketHandler!!.removeMessages(0)
            socketHandler = null
        }
        if (handlerThread != null) {
            handlerThread!!.quit()
        }
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.mortgage_coin_type_layout) {
            val configObj = binding?.mortgageCoinType?.tag
            val loanConfig = if (configObj is LoanConfig) configObj else null
            if (loanConfig != null) {
                DeepControllerWindow<LoanConfig?>(this, "选择抵押币种", loanConfig, loanConfigList,
                        object : DeepControllerWindow.OnReturnListener<LoanConfig?> {
                            override fun onReturn(window: DeepControllerWindow<LoanConfig?>, item: LoanConfig?) {
                                onLoanConfigChanged(item)
                            }

                        }).show()
            }
        } else if (id == R.id.loan_coin_type_layout) {
            val configObj = binding?.mortgageCoinType?.tag
            val loanConfig = if (configObj is LoanConfig) configObj else null
            val configSubObj = binding?.loanCoinType?.tag
            val loanConfigSub = if (configSubObj is LoanConfigSub) configSubObj else null
            if (loanConfig != null) {
                DeepControllerWindow(this, "选择借入币种", loanConfigSub, loanConfig.borrowCoinTypeList,
                        object : DeepControllerWindow.OnReturnListener<LoanConfigSub?> {
                            override fun onReturn(window: DeepControllerWindow<LoanConfigSub?>, item: LoanConfigSub?) {
                                onLoanConfigSubChanged(item)
                            }

                        }).show()
            }
        } else if (id == R.id.rate_layout) {
            val configSubObj = binding?.loanCoinType?.tag
            val loanConfigSub = if (configSubObj is LoanConfigSub) configSubObj else null
            val configStageObj = binding?.rateLayout?.tag
            val loanConfigStage = if (configStageObj is LoanConfigStage) configStageObj else null
            if (loanConfigSub != null) {
                DeepControllerWindow(this, "选择借贷周期", loanConfigStage, loanConfigSub.stage,
                        object : DeepControllerWindow.OnReturnListener<LoanConfigStage?> {
                            override fun onReturn(window: DeepControllerWindow<LoanConfigStage?>, item: LoanConfigStage?) {
                                onLoanConfigStageChanged(item)
                            }

                        }).show()
            }
        } else if (id == R.id.btn_create) {
            createLoan()
        }
    }

    private fun onLoanConfigChanged(loanConfig: LoanConfig?) {
        binding?.mortgageCoinType?.tag = loanConfig
        binding?.loanCoinType?.tag = null
        binding?.rateLayout?.tag = null
        binding?.mortgageCoinType?.text = if (loanConfig?.coinType == null) nullAmount else loanConfig.coinType
        if (loanConfig?.coinType != null) {
            getWallet(loanConfig.coinType!!)
        }
        checkClickable()
        onLoanConfigSubChanged(if (loanConfig == null) null else CommonUtil.getItemFromList(loanConfig.borrowCoinTypeList, 0))
    }

    private fun onLoanConfigSubChanged(loanConfigSub: LoanConfigSub?) {
        binding?.rateLayout?.tag = null
        binding?.loanCoinType?.tag = loanConfigSub
        binding?.loanCoinType?.text = if (loanConfigSub?.borrowCoinType == null) nullAmount else loanConfigSub.borrowCoinType
        borrowPrecision = loanConfigSub?.borrowPrecision ?: 0
        mortgagePrecision = loanConfigSub?.mortgagePrecision ?: 0
        binding?.mortgageAmount?.setText("")
        binding?.mortgageAmount?.filters = arrayOf(NumberFilter(), PointLengthFilter(mortgagePrecision))
        binding?.loanAmount?.setText("")
        binding?.loanAmount?.filters = arrayOf(NumberFilter(), PointLengthFilter(borrowPrecision))
        binding?.mortgageAmount?.hint = String.format("最小抵押数量：%s", if (loanConfigSub?.minMortgageAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(loanConfigSub.minMortgageAmount, 9, 0, mortgagePrecision))
        onWalletChanged(currentWallet)
        checkClickable()
        onLoanConfigStageChanged(if (loanConfigSub == null) null else CommonUtil.getItemFromList(loanConfigSub.stage, 0))
    }

    private fun onLoanConfigStageChanged(loanConfigStage: LoanConfigStage?) {
        binding?.rateLayout?.tag = loanConfigStage
        binding?.days?.text = String.format("%s天", if (loanConfigStage?.numberDays == null) nullAmount else NumberUtil.formatNumberNoGroup(loanConfigStage.numberDays))
        binding?.rate?.text = String.format("年化利率：%s%%", if (loanConfigStage?.rate == null) nullAmount else NumberUtil.formatNumberNoGroup(loanConfigStage.rate!! * 100, 2, 2))
        binding?.loanWarning?.text = getString(R.string.loan_warning, if (loanConfigStage?.defaultRate == null) nullAmount else NumberUtil.formatNumberNoGroup(loanConfigStage.defaultRate!! * 100))
        checkClickable()
    }

    private fun getWallet(coinType: String) {
        val wallet = walletCache[coinType]
        if (wallet != null) {
            onWalletChanged(wallet)
        } else {
            getWalletList(Runnable { onWalletChanged(walletCache[coinType]) })
        }
    }

    private fun onWalletChanged(wallet: Wallet?) {
        currentWallet = wallet
        val usableAmount: BigDecimal = currentWallet?.coinAmount ?: BigDecimal.ZERO
        binding?.useAmount?.text = String.format("可用余额 %s", if (currentWallet == null || currentWallet!!.coinAmount == null) nullAmount else NumberUtil.formatNumberNoGroupScale(currentWallet!!.coinAmount, RoundingMode.FLOOR, 0, mortgagePrecision))
        val coinType = wallet?.coinType
        val obj = binding?.loanCoinType?.tag
        val loanConfigSub = if (obj is LoanConfigSub) obj else null
        val loanCoinType = loanConfigSub?.borrowCoinType
        if (coinType != null && loanCoinType != null) {
            CommonUtil.postHandleTask(socketHandler) {
                var computeScale: Double? = null
                val loanScale = loanConfigSub.borrowingMortgageScale
                if (TextUtils.equals("USDT", coinType)) {
                    if (TextUtils.equals("USDT", loanCoinType)) {
                        computeScale = loanScale
                    } else {
                        //USDT 直接取出相应交易对计算价格
                        val loanUsdtPairStatus = SocketDataContainer.getPairStatusSync(mContext,ConstData.PairStatusType.SPOT, loanCoinType + "_" + coinType)
                        if (loanUsdtPairStatus != null) {
                            computeScale = if (loanScale == null || loanUsdtPairStatus.currentPrice == 0.0) null else loanScale / loanUsdtPairStatus.currentPrice
                        }
                    }
                } else {
                    if (TextUtils.equals("USDT", coinType)) {
                        computeScale = loanScale
                    } else if (TextUtils.equals("USDT", loanCoinType)) {
                        //借入USDT 直接取出相应交易对计算价格
                        val mortgageUsdtPairStatus = SocketDataContainer.getPairStatusSync(mContext, ConstData.PairStatusType.SPOT,coinType + "_" + loanCoinType)
                        if (mortgageUsdtPairStatus != null) {
                            computeScale = if (loanScale == null) null else loanScale * mortgageUsdtPairStatus.currentPrice
                        }
                    } else {
                        //抵押和借入都不是usdt,取出两种价格进行计算
                        val loanUsdtPairStatus = SocketDataContainer.getPairStatusSync(mContext,ConstData.PairStatusType.SPOT, coinType + "_USDT")
                        val mortgageUsdtPairStatus = SocketDataContainer.getPairStatusSync(mContext, ConstData.PairStatusType.SPOT,loanCoinType + "_USDT")
                        if (loanUsdtPairStatus != null && mortgageUsdtPairStatus != null) {
                            computeScale = if (loanScale == null || loanUsdtPairStatus.currentPrice == 0.0) null else loanScale * (mortgageUsdtPairStatus.currentPrice / loanUsdtPairStatus.currentPrice)
                        }
                    }
                }
                val finalComputeScale = computeScale
                runOnUiThread {
                    binding?.maxLoanAmount?.text = String.format("最多借入 %s", if (finalComputeScale == null) nullAmount else NumberUtil.formatNumberNoGroupScale(usableAmount * BigDecimal(finalComputeScale), RoundingMode.FLOOR, 0, borrowPrecision))
                    binding?.loanAmount?.hint = String.format("最小借入数量：%s", if (loanConfigSub.minMortgageAmount == null || finalComputeScale == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(loanConfigSub.minMortgageAmount!! * finalComputeScale, 9, 0, borrowPrecision))
                }
            }
        } else {
            binding?.loanAmount?.hint = String.format("最小借入数量：%s", nullAmount)
            binding?.maxLoanAmount?.text = String.format("最多借入 %s", nullAmount)
        }
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

    private fun checkClickable() {
        val configObj = binding?.mortgageCoinType?.tag
        val loanConfig = if (configObj is LoanConfig) configObj else null
        val configSubObj = binding?.loanCoinType?.tag
        val loanConfigSub = if (configSubObj is LoanConfigSub) configSubObj else null
        val configStageObj = binding?.rateLayout?.tag
        val loanConfigStage = if (configStageObj is LoanConfigStage) configStageObj else null
        val mortgageAmountText = binding?.mortgageAmount?.text.toString().trim { it <= ' ' }
        val mortgageAmount = CommonUtil.parseDouble(mortgageAmountText)
        val lanAmountText = binding?.loanAmount?.text.toString().trim { it <= ' ' }
        val lanAmount = CommonUtil.parseDouble(lanAmountText)
        binding?.btnCreate?.isEnabled = loanConfig != null && loanConfigSub != null && loanConfigStage != null && mortgageAmount != null && lanAmount != null
    }

    private fun createLoan() {
        val configObj = binding?.mortgageCoinType?.tag
        val loanConfig = if (configObj is LoanConfig) configObj else null
        val configSubObj = binding?.loanCoinType?.tag
        val loanConfigSub = if (configSubObj is LoanConfigSub) configSubObj else null
        val configStageObj = binding?.rateLayout?.tag
        val loanConfigStage = if (configStageObj is LoanConfigStage) configStageObj else null
        val mortgageAmountText = binding?.mortgageAmount?.text.toString().trim { it <= ' ' }
        val mortgageAmount = CommonUtil.parseDouble(mortgageAmountText)
        val lanAmountText = binding?.loanAmount?.text.toString().trim { it <= ' ' }
        val lanAmount = CommonUtil.parseDouble(lanAmountText)
        MoneyApiServiceHelper.createLoan(this,
                loanConfig?.coinType,
                loanConfigSub?.borrowCoinType,
                mortgageAmountText,
                lanAmountText,
                if (loanConfigStage?.numberDays == null) null else NumberUtil.formatNumberNoGroup(loanConfigStage.numberDays),
                object : NormalCallback<HttpRequestResultString?>(mContext!!) {
                    override fun callback(returnData: HttpRequestResultString?) {
                        if (returnData?.code != null && returnData.code == HttpRequestResult.SUCCESS) {
                            FryingUtil.showToast(mContext, "借入成功")
                        } else {
                            FryingUtil.showToast(mContext, if (returnData?.msg == null) "null" else returnData.msg)
                        }
                    }
                })
    }
}