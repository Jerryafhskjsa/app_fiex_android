package com.black.community.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.*
import android.text.style.AbsoluteSizeSpan
import android.view.View
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.CommunityApiServiceHelper
import com.black.base.api.WalletApiServiceHelper
import com.black.base.filter.NumberFilter
import com.black.base.filter.PointLengthFilter
import com.black.base.model.HttpRequestResultString
import com.black.base.model.SuccessObserver
import com.black.base.model.community.RedPacketPub
import com.black.base.model.wallet.CoinInfo
import com.black.base.model.wallet.Wallet
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.util.SocketDataContainer
import com.black.base.view.ConfirmDialog
import com.black.base.view.ConfirmDialog.OnConfirmCallback
import com.black.community.R
import com.black.community.databinding.ActivitySendRedPacketBinding
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
import kotlin.collections.ArrayList

@Route(value = [RouterConstData.RED_PACKET_CREATE], beforePath = RouterConstData.LOGIN)
open class SendRedPacketActivity : BaseActionBarActivity(), View.OnClickListener {
    companion object {
        private const val TYPE_RANDOM = 0
        private const val TYPE_NORMAL = 1
    }

    private var type = -1

    private var binding: ActivitySendRedPacketBinding? = null

    private var coinInfoCache: MutableMap<String?, CoinInfo?>? = null
    private var currentCoinInfo: CoinInfo? = null
    private var currentWallet: Wallet? = null
    private var walletList: ArrayList<Wallet?>? = ArrayList()
    private var walletCache: MutableMap<String, Wallet?>? = null

    private val watcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            checkClickable()
            refreshTotalAmount()
            refreshHint()
        }

        override fun afterTextChanged(s: Editable) {}
    }
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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_send_red_packet)
        binding?.chooseCoin?.setOnClickListener(this)
        binding?.amount?.addTextChangedListener(watcher)
        binding?.type?.setOnClickListener(this)
        binding?.count?.addTextChangedListener(watcher)
        binding?.btnSend?.setOnClickListener(this)
        setType(TYPE_RANDOM)
        initUSDTWallet()
        checkClickable()
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getActionBarLayoutId(): Int {
        return R.layout.action_bar_left_back
    }

    override fun initActionBarView(view: View) {
        view.setBackgroundColor(SkinCompatResources.getColor(this, R.color.B1))
        val titleView = view.findViewById<TextView>(R.id.action_bar_title)
        titleView.text = "发红包"
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.choose_coin -> {
                getCoinInfoCache(Runnable {
                    val supportCoinList = ArrayList<String>()
                    if (coinInfoCache != null) {
                        for (entry in coinInfoCache!!.entries) {
                            val coinType = entry.key
                            val coinInfo = entry.value
                            if (coinType != null && coinInfo != null) {
                                if (CoinInfo.FILTER_RED_PACKET.filter(coinInfo)) {
                                    supportCoinList.add(coinType)
                                }
                            }
                        }
                        if (currentCoinInfo != null && currentCoinInfo!!.redPacketAmountPrecision != null) {
                            binding?.amount?.filters = arrayOf(NumberFilter(), PointLengthFilter(currentCoinInfo!!.redPacketAmountPrecision!!))
                        }
                    }
                    if (supportCoinList.isEmpty()) {
                        FryingUtil.showToast(mContext, "暂无支持的红包币种！")
                        return@Runnable
                    }
                    val extras = Bundle()
                    extras.putParcelableArrayList(ConstData.WALLET_LIST, walletList)
                    extras.putStringArrayList(ConstData.SUPPORT_COIN_LIST, supportCoinList)
                    BlackRouter.getInstance().build(RouterConstData.WALLET_CHOOSE_COIN)
                            .withRequestCode(ConstData.CHOOSE_COIN)
                            .with(extras)
                            .go(mContext)
                })
            }
            R.id.type -> {
                setType((type + 1) % 2)
            }
            R.id.btn_send -> {
                sendRedPacket()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (userInfoObserver == null) {
            userInfoObserver = createUserInfoObserver()
        }
        SocketDataContainer.subscribeUserInfoObservable(userInfoObserver)
    }

    override fun onStop() {
        super.onStop()
        if (userInfoObserver != null) {
            SocketDataContainer.removeUserInfoObservable(userInfoObserver)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ConstData.CHOOSE_COIN -> {
                    val chooseWallet: Wallet? = data?.getParcelableExtra(ConstData.WALLET)
                    if (chooseWallet != null) {
                        //查找出选中的币种，并做相应跳转
                        if (walletCache != null && walletCache!![chooseWallet.coinType].also { currentWallet = it } != null) {
                            refreshWallet()
                        } else {
                            chooseWallet.coinType?.let {
                                getWalletAndRefresh(it)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkClickable() {
        val amount = CommonUtil.parseDouble(binding?.amount?.text.toString())
        val count = CommonUtil.parseInt(binding?.count?.text.toString())
        binding?.btnSend?.isEnabled = currentWallet != null && amount != null && count != null && amount > 0 && count > 0
    }

    private fun setType(type: Int) {
        if (this.type != type) {
            this.type = type
            onTypeChanged()
        }
    }

    private fun onTypeChanged() {
        if (type == TYPE_NORMAL) {
            binding?.amountTitle?.text = "单个金额"
            CommonUtil.setTextViewCompoundDrawable(binding?.amountTitle, SkinCompatResources.getDrawable(this, R.drawable.icon_red_packet_normal), 0)
            val typeText = String.format("%s%s", "当前为普通红包，", FryingUtil.translateToHtmlTextAddColor(this, "改为拼手气红包", R.color.C1))
            FryingUtil.setHtmlText(binding?.type, typeText)
        } else {
            binding?.amountTitle?.text = "总金额"
            CommonUtil.setTextViewCompoundDrawable(binding?.amountTitle, SkinCompatResources.getDrawable(this, R.drawable.icon_red_packet_random), 0)
            val typeText = String.format("%s%s", "当前为拼手气红包，", FryingUtil.translateToHtmlTextAddColor(this, "改为普通红包", R.color.C1))
            FryingUtil.setHtmlText(binding?.type, typeText)
        }
        refreshTotalAmount()
        refreshHint()
    }

    private fun getCoinInfoCache(callback: Runnable) {
        if (coinInfoCache != null) {
            callback.run()
            return
        }
        WalletApiServiceHelper.getCoinInfoList(this, object : Callback<ArrayList<CoinInfo?>?>() {
            override fun error(type: Int, error: Any) {
                callback.run()
            }

            override fun callback(returnData: ArrayList<CoinInfo?>?) {
                if (returnData != null && returnData.isNotEmpty()) {
                    if (coinInfoCache == null) {
                        coinInfoCache = HashMap()
                    }
                    coinInfoCache!!.clear()
                    for (coinInfo in returnData) {
                        if (coinInfo != null && !TextUtils.isEmpty(coinInfo.coinType)) {
                            coinInfoCache!![coinInfo.coinType] = coinInfo
                        }
                    }
                    callback.run()
                }
            }
        })
    }

    private val coinInfo: Unit
        get() {
            if (currentWallet == null) {
                return
            }
            getCoinInfoCache(Runnable {
                if (coinInfoCache != null) {
                    currentCoinInfo = coinInfoCache!![currentWallet!!.coinType]
                    if (currentCoinInfo != null && currentCoinInfo!!.redPacketAmountPrecision != null) {
                        binding?.amount?.filters = arrayOf(NumberFilter(), PointLengthFilter(currentCoinInfo!!.redPacketAmountPrecision!!))
                    }
                }
                refreshTotalAmount()
                refreshHint()
            })
        }

    private fun refreshWallet() {
        if (currentWallet != null) {
            binding?.coinType?.text = currentWallet!!.coinType
            binding?.amountCoinType?.text = currentWallet!!.coinType
            binding?.amount?.setText("")
            coinInfo
        }
        refreshTotalAmount()
        refreshHint()
    }

    private fun refreshTotalAmount() {
        val totalAmount: Double?
        totalAmount = if (type == TYPE_NORMAL) {
            val oneAmount = CommonUtil.parseDouble(binding?.amount?.text.toString())
            val count = CommonUtil.parseInt(binding?.count?.text.toString())
            if (oneAmount == null || count == null) null else oneAmount * count
        } else {
            CommonUtil.parseDouble(binding?.amount?.text.toString())
        }
        val totalAmountString = if (totalAmount == null) "0" else NumberUtil.formatNumberDynamicScaleNoGroup(totalAmount, 9, 0, currentCoinInfo?.redPacketAmountPrecision
                ?: 0)
        val amountUnit = " " + if (currentWallet == null || currentWallet!!.coinType == null) nullAmount else currentWallet!!.coinType
        val holeAmountString = totalAmountString + amountUnit
        val totalAmountSpan = SpannableStringBuilder(holeAmountString)
        totalAmountSpan.setSpan(AbsoluteSizeSpan(16, true), totalAmountString.length, holeAmountString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding?.totalAmount?.text = totalAmountSpan
    }

    private fun refreshHint() {
        val minAmount = currentCoinInfo?.minRedPacketAmount
        val maxAmount = currentCoinInfo?.maxRedPacketAmount
        var hintAmount: Double? = null
        var less = true
        if (minAmount == null || maxAmount == null) {
            hintAmount = null
        } else {
            binding?.hint?.visibility = View.VISIBLE
            val amount = CommonUtil.parseDouble(binding?.amount?.text.toString())
            if (amount == null) {
                hintAmount = null
            } else {
                if (type == TYPE_NORMAL) {
                    if (amount > maxAmount) {
                        hintAmount = maxAmount
                        less = false
                    } else if (amount < minAmount) {
                        hintAmount = minAmount
                        less = true
                    }
                } else {
                    val count = CommonUtil.parseInt(binding?.count?.text.toString())
                    if (count != null && count != 0) {
                        val oneAmount = amount / count
                        if (oneAmount > maxAmount) {
                            hintAmount = maxAmount
                            less = false
                        } else if (oneAmount < minAmount) {
                            hintAmount = minAmount
                            less = true
                        }
                    }
                }
            }
        }
        if (hintAmount == null) {
            binding?.hint?.visibility = View.GONE
        } else {
            binding?.hint?.text = String.format("单个红包不能" + (if (less) "小" else "大") + "于%s %s",
                    NumberUtil.formatNumberDynamicScaleNoGroup(hintAmount, 9, 0, currentCoinInfo?.redPacketAmountPrecision
                            ?: 0),
                    currentWallet?.coinType ?: nullAmount)
        }
    }

    private fun initUSDTWallet() {
        if (currentWallet == null || !TextUtils.equals(currentWallet!!.coinType, "USDT")) {
            getWalletAndRefresh("USDT")
        } else {
            refreshWallet()
        }
    }

    private fun getWalletAndRefresh(coinType: String) {
        getWalletList(coinType, object : Callback<Wallet?>() {
            override fun error(type: Int, error: Any) {}
            override fun callback(returnData: Wallet?) {
                currentWallet = returnData
                refreshWallet()
            }
        })
    }

    private fun getWalletList(coinType: String?, callback: Callback<Wallet?>?) {
        WalletApiServiceHelper.getWalletList(this, false, object : Callback<ArrayList<Wallet?>?>() {
            override fun error(type: Int, error: Any) {
                callback?.callback(null)
            }

            override fun callback(returnData: ArrayList<Wallet?>?) {
                if (returnData == null || returnData.isEmpty()) {
                    return
                }
                walletList = returnData
                if (walletCache == null) {
                    walletCache = HashMap()
                }
                for (wallet in returnData) {
                    walletCache?.let {
                        wallet?.coinType?.let {
                            walletCache!![it] = wallet
                        }
                    }
                }
                if (coinType != null) {
                    currentWallet = walletCache!![coinType]
                    callback?.callback(currentWallet)
                }
            }
        })
    }

    //用户信息被修改，刷新委托信息和钱包
    private fun onUserInfoChanged() {
        getWalletList(if (currentWallet == null) "USDT" else currentWallet!!.coinType, null)
    }

    private fun sendRedPacket() {
        val redPacketType = if (type == TYPE_RANDOM) RedPacketPub.LUCKY else RedPacketPub.NORMAL
        val amount = binding?.amount?.text.toString().trim { it <= ' ' }
        val amountDouble = CommonUtil.parseDouble(amount)
        if (amountDouble == null) {
            FryingUtil.showToast(this, resources.getString(R.string.alert_amount_error, ""))
            return
        }
        val count = CommonUtil.parseInt(binding?.count?.text.toString())
        if (count == null) {
            FryingUtil.showToast(this, resources.getString(R.string.alert_c2c_create_amount_error, ""))
            return
        }
        if (TextUtils.equals(RedPacketPub.LUCKY, redPacketType)) {
            if (currentWallet != null && BigDecimal(amountDouble) > currentWallet!!.coinAmount) {
                FryingUtil.showToast(this, "余额不足")
                return
            }
            //            if (currentCoinInfo != null && currentCoinInfo.maxRedPacketAmount != null && amountDouble > currentCoinInfo.maxRedPacketAmount) {
//                FryingUtil.showToast(this, "超过红包单次最大金额");
//                return;
//            }
//            if (currentCoinInfo != null && currentCoinInfo.minRedPacketAmount != null && amountDouble < currentCoinInfo.minRedPacketAmount) {
//                FryingUtil.showToast(this, "小于红包单次最小金额");
//                return;
//            }
        } else {
            if (currentWallet != null && BigDecimal(amountDouble * count) > currentWallet!!.coinAmount) {
                FryingUtil.showToast(this, "余额不足")
                return
            }
            //            if (currentCoinInfo != null && currentCoinInfo.maxRedPacketAmount != null && amountDouble > currentCoinInfo.maxRedPacketAmount) {
//                FryingUtil.showToast(this, "超过红包单次最大金额");
//                return;
//            }
//            if (currentCoinInfo != null && currentCoinInfo.minRedPacketAmount != null && amountDouble < currentCoinInfo.minRedPacketAmount) {
//                FryingUtil.showToast(this, "小于红包单次最小金额");
//                return;
//            }
        }
        val hintText = binding?.hint?.text?.toString()
        if (!TextUtils.isEmpty(hintText)) {
            FryingUtil.showToast(this, hintText)
            return
        }
        var packetText = binding?.text?.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(packetText)) {
            packetText = "恭喜发财，万事如意！"
        }
        val finalPacketText = packetText
        val totalAmount: Double?
        totalAmount = if (type == TYPE_NORMAL) {
            val oneAmount = CommonUtil.parseDouble(binding?.amount?.text.toString())
            if (oneAmount == null) null else oneAmount * count
        } else {
            CommonUtil.parseDouble(binding?.amount?.text.toString())
        }
        val toastText = String.format("红包总金额 %s %s， 确认发红包！",
                if (totalAmount == null) "0" else NumberUtil.formatNumberDynamicScaleNoGroup(totalAmount, 9, 0, currentCoinInfo?.redPacketAmountPrecision
                        ?: 0),
                if (currentWallet == null || currentWallet!!.coinType == null) nullAmount else currentWallet!!.coinType)
        ConfirmDialog(this, "提示", toastText, object : OnConfirmCallback {
            override fun onConfirmClick(confirmDialog: ConfirmDialog) {
                CommunityApiServiceHelper.sendRedPacket(mContext, redPacketType, currentWallet!!.coinType, amount, count, finalPacketText, object : NormalCallback<HttpRequestResultString?>() {
                    override fun error(type: Int, error: Any?) {
                        super.error(type, error)
                        if (type == ConstData.ERROR_MISS_MONEY_PASSWORD) {
                            confirmDialog.dismiss()
                        }
                    }

                    override fun callback(returnData: HttpRequestResultString?) {
                        if (returnData?.code != null && returnData.code == HttpRequestResult.SUCCESS) {
                            confirmDialog.dismiss()
                            val intent = Intent()
                            intent.putExtra(ConstData.RED_PACKET_ID, returnData.data)
                            intent.putExtra(ConstData.RED_PACKET_TEXT, finalPacketText)
                            setResult(Activity.RESULT_OK, intent)
                            finish()
                        } else {
                            FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                        }
                    }
                })
            }

        }).show()
    }
}