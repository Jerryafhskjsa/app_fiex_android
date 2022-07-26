package com.black.c2c.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.black.base.api.C2CApiServiceHelper
import com.black.base.api.WalletApiServiceHelper
import com.black.base.filter.NumberFilter
import com.black.base.filter.PointLengthFilter
import com.black.base.fragment.BaseFragment
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.HttpRequestResultString
import com.black.base.model.SuccessObserver
import com.black.base.model.c2c.C2COrder
import com.black.base.model.c2c.C2CSeller
import com.black.base.model.c2c.C2CSupportCoin
import com.black.base.model.user.UserInfo
import com.black.base.model.wallet.Wallet
import com.black.base.util.*
import com.black.c2c.R
import com.black.c2c.databinding.FragmentC2cOneKeySaleItemBinding
import com.black.c2c.util.C2CHandleCheckHelper
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import io.reactivex.Observer
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class C2COneKeySaleItemFragment : BaseFragment(), View.OnClickListener {
    companion object {
        private const val TYPE_MONEY = 0
        private const val TYPE_AMOUNT = 1
    }

    private var supportCoin: C2CSupportCoin? = null
    private var precision = 0

    private var binding: FragmentC2cOneKeySaleItemBinding? = null

    private var currentType = TYPE_AMOUNT
    private var c2CSeller: C2CSeller? = null
    private var c2CHandleCheckHelper: C2CHandleCheckHelper? = null
    private var userInfo: UserInfo? = null
    private val walletCache: MutableMap<String, Wallet?> = HashMap()
    private var userInfoObserver: Observer<String?>? = createUserInfoObserver()
    private fun createUserInfoObserver(): Observer<String?> {
        return object : SuccessObserver<String?>() {
            override fun onSuccess(value: String?) {
                getWalletList(null)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val bundle = arguments
        if (bundle != null) {
            supportCoin = bundle.getParcelable(ConstData.C2C_SUPPORT_COIN)
        }
        precision = supportCoin?.precision ?: 0
        c2CHandleCheckHelper = if (mContext == null) null else C2CHandleCheckHelper(mContext!!, mContext!!, mContext!!.fryingHelper)

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_c2c_one_key_sale_item, container, false)
        binding?.amount?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                refreshAmountViews()
            }

            override fun afterTextChanged(s: Editable) {}
        })
        binding?.c2cOneKeySaleTotal?.setOnClickListener(this)
        binding?.c2cOneKeyTypeChange?.setOnClickListener(this)
        binding?.btnOneKeySale?.setOnClickListener(this)
        refreshBuyType()
        refreshSeller()
        getWalletList(null)
        return binding?.root
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.c2c_one_key_type_change -> {
                changeBuyType()
            }
            R.id.c2c_one_key_sale_total -> {
                if (c2CSeller == null) {
                    return
                }
                fryingHelper.checkUserAndDoing(Runnable {
                    val wallet = walletCache[c2CSeller?.coinType]
                    if (wallet == null) {
                        getWalletList(Runnable {
                            val newWallet = walletCache[c2CSeller?.coinType]
                            if (newWallet != null) {
                                val maxInAmount: BigDecimal = newWallet.coinAmount
                                        ?: BigDecimal.ZERO
                                binding?.amount?.setText(NumberUtil.formatNumberNoGroup(maxInAmount, RoundingMode.FLOOR, 0, precision))
                            }
                        })
                    } else {
                        val maxInAmount: BigDecimal = wallet.coinAmount ?: BigDecimal.ZERO
                        binding?.amount?.setText(NumberUtil.formatNumberNoGroup(maxInAmount, RoundingMode.FLOOR, 0, precision))
                    }
                }, DEMAND_INDEX)
            }
            R.id.btn_one_key_sale -> {
                if (c2CSeller == null) {
                    return
                }
                var amountText = binding?.amount?.text.toString().trim { it <= ' ' }
                val amount = CommonUtil.parseDouble(amountText)
                if (amount == null) {
                    FryingUtil.showToast(mContext, mContext?.getString(R.string.alert_c2c_create_amount_error, mContext?.getString(R.string.c2c_buy)))
                    return
                }
                if (currentType == TYPE_MONEY) {
                    amountText = NumberUtil.formatNumberNoGroup(if (c2CSeller?.price == null || c2CSeller?.price == 0.0) 0 else amount / c2CSeller?.price!!, RoundingMode.FLOOR, 0, 2)
                }
                val finalAmountText = amountText
                c2CHandleCheckHelper?.run {
                    checkLoginUser(Runnable {
                        checkRealName(Runnable {
                            checkMoneyPassword(Runnable {
                                checkC2CAgree(Runnable {
                                    checkBindBankPaymentMethod(Runnable {
                                        createOrder(finalAmountText)
                                    })
                                })
                            })
                        })
                    })
                }
            }
        }
    }

    private fun createOrder(finalAmountText: String) {
        C2CApiServiceHelper.createC2COrderSell(mContext, if (c2CSeller == null) null else c2CSeller?.coinType, C2COrder.ORDER_SELL, finalAmountText, if (c2CSeller == null) null else c2CSeller?.id, "1", object : NormalCallback<HttpRequestResultString?>() {
            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    val bundle = Bundle()
                    bundle.putString(ConstData.C2C_ORDER_ID, returnData.data)
                    bundle.putString(ConstData.C2C_DIRECTION, C2COrder.ORDER_SELL)
                    BlackRouter.getInstance().build(RouterConstData.C2C_ORDER_DETAIL).with(bundle).go(mFragment)
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        userInfo = if (mContext == null) null else CookieUtil.getUserInfo(mContext!!)
        getUserInfo(object : Callback<UserInfo?>() {
            override fun error(type: Int, error: Any) {}
            override fun callback(returnData: UserInfo?) {
                userInfo = returnData
            }
        })
        if (userInfoObserver == null) {
            userInfoObserver = createUserInfoObserver()
        }
        SocketDataContainer.subscribeUserInfoObservable(userInfoObserver)
        getSellerList(false)
    }

    override fun onStop() {
        super.onStop()
        if (userInfoObserver != null) {
            SocketDataContainer.removeUserInfoObservable(userInfoObserver)
        }
    }

    private fun changeBuyType() {
        currentType = (currentType + 1) % 2
        refreshBuyType()
        refreshSeller()
    }

    private fun refreshBuyType() {
        if (currentType == TYPE_MONEY) {
            binding?.amountTitle?.text = "出售金额"
            binding?.c2cOneKeyTypeChange?.text = "按数量出售"
        } else if (currentType == TYPE_AMOUNT) {
            binding?.amountTitle?.text = "出售数量"
            binding?.c2cOneKeyTypeChange?.text = "按金额出售"
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

    private fun getSellerList(isShowLoading: Boolean) {
        if (supportCoin == null) {
            return
        }
        C2CApiServiceHelper.getC2CSellerFastList(activity, isShowLoading, if (supportCoin == null) null else supportCoin?.coinType, C2COrder.ORDER_SELL, 1, 10, object : NormalCallback<HttpRequestResultDataList<C2CSeller?>?>() {
            override fun error(type: Int, error: Any) {
                super.error(type, error)
                c2CSeller = null
                refreshSeller()
            }

            override fun callback(returnData: HttpRequestResultDataList<C2CSeller?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    c2CSeller = CommonUtil.getItemFromList(returnData.data, 0)
                    refreshSeller()
                } else {
                    FryingUtil.showToast(activity, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    private fun refreshSeller() {
        binding?.btnOneKeySale?.isEnabled = c2CSeller != null
        binding?.amount?.setText("")
        val precision = supportCoin?.precision ?: 0
        val minAmount: Double = supportCoin?.minOrderAmount ?: 0.0
        val price: Double = c2CSeller?.price ?: 0.0
        if (currentType == TYPE_MONEY) {
            binding?.amount?.hint = String.format("出售金额最小%s", NumberUtil.formatNumberNoGroup(minAmount * price, precision))
            binding?.coinType?.setText(R.string.cny)
        } else {
            binding?.amount?.hint = String.format("出售数量最小%s", NumberUtil.formatNumberNoGroup(minAmount, precision))
            binding?.coinType?.text = if (c2CSeller == null || c2CSeller?.coinType == null) nullAmount else c2CSeller?.coinType
        }
        refreshAmountViews()
        binding?.amount?.filters = arrayOf(NumberFilter(), PointLengthFilter(precision))
        binding?.price?.text = String.format("≈ %s CNY/%s",
                if (c2CSeller == null || c2CSeller?.price == null) nullAmount else NumberUtil.formatNumberNoGroup(c2CSeller?.price, 0, precision),
                if (c2CSeller == null || c2CSeller?.coinType == null) nullAmount else c2CSeller?.coinType)
    }

    private fun refreshAmountViews() {
        val precision = supportCoin?.precision ?: 0
        val price: Double = c2CSeller?.price ?: 0.0
        val totalAmount: Double
        val totalMoney: Double
        if (currentType == TYPE_MONEY) {
            val totalMoneyText = binding?.amount?.text.toString()
            val totalMoneyDouble = if (TextUtils.isEmpty(totalMoneyText)) null else CommonUtil.parseDouble(totalMoneyText)
            totalMoney = totalMoneyDouble ?: 0.0
            totalAmount = if (price == 0.0) 0.0 else totalMoney / price
            binding?.totalAmount?.text = String.format("交易数量 %s", NumberUtil.formatNumberNoGroup(totalAmount, precision))
            binding?.totalMoney?.text = String.format("%s %s", NumberUtil.formatNumberNoGroup(totalMoney, precision), getString(R.string.cny))
        } else {
            val totalAmountText = binding?.amount?.text.toString()
            val totalAmountDouble = if (TextUtils.isEmpty(totalAmountText)) null else CommonUtil.parseDouble(totalAmountText)
            totalAmount = totalAmountDouble ?: 0.0
            totalMoney = totalAmount * price
            binding?.totalAmount?.text = String.format("交易数量 %s", NumberUtil.formatNumberNoGroup(totalAmount, precision))
            binding?.totalMoney?.text = String.format("%s %s", NumberUtil.formatNumberNoGroup(totalMoney, precision), getString(R.string.cny))
        }
    }
}