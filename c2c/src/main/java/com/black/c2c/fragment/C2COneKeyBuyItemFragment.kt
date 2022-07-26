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
import com.black.base.filter.NumberFilter
import com.black.base.filter.PointLengthFilter
import com.black.base.fragment.BaseFragment
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.HttpRequestResultString
import com.black.base.model.c2c.C2COrder
import com.black.base.model.c2c.C2CSeller
import com.black.base.model.c2c.C2CSupportCoin
import com.black.base.model.user.UserInfo
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.c2c.R
import com.black.c2c.databinding.FragmentC2cOneKeyBuyItemBinding
import com.black.c2c.util.C2CHandleCheckHelper
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import java.math.RoundingMode

class C2COneKeyBuyItemFragment : BaseFragment(), View.OnClickListener {
    companion object {
        private const val TYPE_MONEY = 0
        private const val TYPE_AMOUNT = 1
    }

    private var supportCoin: C2CSupportCoin? = null
    private var precision = 0

    private var binding: FragmentC2cOneKeyBuyItemBinding? = null

    private var currentType = TYPE_AMOUNT
    private var c2CSeller: C2CSeller? = null
    private var c2CHandleCheckHelper: C2CHandleCheckHelper? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val bundle = arguments
        if (bundle != null) {
            supportCoin = bundle.getParcelable(ConstData.C2C_SUPPORT_COIN)
        }
        precision = if (supportCoin == null || supportCoin?.precision == null) 0 else supportCoin?.precision!!
        c2CHandleCheckHelper = if (mContext == null) null else C2CHandleCheckHelper(mContext!!, mContext!!, mContext!!.fryingHelper)

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_c2c_one_key_buy_item, container, false)
        binding?.amount?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                refreshAmountViews()
            }

            override fun afterTextChanged(s: Editable) {}
        })
        binding?.c2cOneKeyTypeChange?.setOnClickListener(this)
        binding?.btnOneKeyBuy?.setOnClickListener(this)
        refreshBuyType()
        refreshSeller()
        return binding?.root
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.c2c_one_key_type_change) {
            changeBuyType()
        } else if (id == R.id.btn_one_key_buy) {
            if (c2CSeller == null) {
                return
            }
            //            new Test2(mContext).test();
            var amountText = binding?.amount?.text.toString().trim { it <= ' ' }
            val amount = CommonUtil.parseDouble(amountText)
            if (amount == null) {
                FryingUtil.showToast(mContext, mContext?.getString(R.string.alert_c2c_create_amount_error, mContext?.getString(R.string.c2c_buy)))
                return
            }
            if (currentType == TYPE_MONEY) {
                amountText = NumberUtil.formatNumberNoGroup(if (c2CSeller?.price == 0.0) 0 else amount / c2CSeller?.price!!, RoundingMode.FLOOR, 0, precision)
            }
            val finalAmountText = amountText
            c2CHandleCheckHelper?.run {
                checkLoginUser(Runnable {
                    checkRealName(Runnable {
                        checkC2CAgree(Runnable {
                            checkBindPaymentMethod(Runnable {
                                C2CApiServiceHelper.createC2COrderBuy(mContext, if (c2CSeller == null) null else c2CSeller?.coinType, C2COrder.ORDER_BUY, finalAmountText, if (c2CSeller == null) null else c2CSeller?.id, "1", object : NormalCallback<HttpRequestResultString?>() {
                                    override fun callback(returnData: HttpRequestResultString?) {
                                        if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                                            val bundle = Bundle()
                                            bundle.putString(ConstData.C2C_ORDER_ID, returnData.data)
                                            bundle.putString(ConstData.C2C_DIRECTION, C2COrder.ORDER_BUY)
                                            BlackRouter.getInstance().build(RouterConstData.C2C_ORDER_DETAIL).with(bundle).go(mFragment)
                                        } else {
                                            FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                                        }
                                    }
                                })
                            })
                        })
                    })
                })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getUserInfo(object : Callback<UserInfo?>() {
            override fun error(type: Int, error: Any) {}
            override fun callback(returnData: UserInfo?) {}
        })
        getSellerList(false)
    }

    private fun changeBuyType() {
        currentType = (currentType + 1) % 2
        refreshBuyType()
        refreshSeller()
    }

    private fun refreshBuyType() {
        if (currentType == TYPE_MONEY) {
            binding?.amountTitle?.text = "购买金额"
            binding?.c2cOneKeyTypeChange?.text = "按数量购买"
        } else if (currentType == TYPE_AMOUNT) {
            binding?.amountTitle?.text = "购买数量"
            binding?.c2cOneKeyTypeChange?.text = "按金额购买"
        }
    }

    private fun getSellerList(isShowLoading: Boolean) {
        if (supportCoin == null) {
            return
        }
        C2CApiServiceHelper.getC2CSellerFastList(activity, isShowLoading, if (supportCoin == null) null else supportCoin?.coinType, C2COrder.ORDER_BUY, 1, 10, object : NormalCallback<HttpRequestResultDataList<C2CSeller?>?>() {
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
        binding?.btnOneKeyBuy?.isEnabled = c2CSeller != null
        binding?.amount?.setText("")
        val minAmount: Double = supportCoin?.minOrderAmount ?: 0.0
        val price: Double = c2CSeller?.price ?: 0.0
        if (currentType == TYPE_MONEY) {
            binding?.amount?.hint = String.format("购买金额最小%s", NumberUtil.formatNumberNoGroup(minAmount * price, precision))
            binding?.coinType?.setText(R.string.cny)
        } else {
            binding?.amount?.hint = String.format("购买数量最小%s", NumberUtil.formatNumberNoGroup(minAmount, precision))
            binding?.coinType?.text = if (c2CSeller == null || c2CSeller?.coinType == null) nullAmount else c2CSeller?.coinType
        }
        refreshAmountViews()
        binding?.amount?.filters = arrayOf(NumberFilter(), PointLengthFilter(precision))
        binding?.price?.text = String.format("单价约 %s CNY/%s",
                if (c2CSeller == null || c2CSeller?.price == null) nullAmount else NumberUtil.formatNumberNoGroup(c2CSeller?.price, 0, precision),
                if (c2CSeller == null || c2CSeller?.coinType == null) nullAmount else c2CSeller?.coinType)
    }

    private fun refreshAmountViews() {
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