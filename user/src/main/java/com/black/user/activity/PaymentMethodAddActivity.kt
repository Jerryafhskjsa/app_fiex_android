package com.black.user.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.C2CApiServiceHelper
import com.black.base.api.UserApiServiceHelper
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.model.user.PaymentMethod
import com.black.base.model.user.PaymentMethodType
import com.black.base.util.ConstData
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.view.DeepControllerWindow
import com.black.base.view.ImageSelector
import com.black.base.view.ImageSelectorHelper
import com.black.net.HttpRequestResult
import com.black.router.annotation.Route
import com.black.user.R
import com.black.user.databinding.ActivityPaymentMothodAddBinding
import com.black.util.Callback
import com.black.util.CommonUtil
import java.io.File
import java.util.*

@Route(value = [RouterConstData.PAYMENT_METHOD_ADD], beforePath = RouterConstData.LOGIN)
class PaymentMethodAddActivity : BaseActionBarActivity(), View.OnClickListener {
    private var titleView: TextView? = null
    private var titleBigView: TextView? = null
    private var paymentMethodTypeList: MutableList<PaymentMethodType?>? = null
    private var paymentMethodType: PaymentMethodType? = null
    private var alipayQrcodeImageSelector: ImageSelector? = null
    private var wechatQrcodeImageSelector: ImageSelector? = null

    private var binding: ActivityPaymentMothodAddBinding? = null

    private val watcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            checkClickable()
        }

        override fun afterTextChanged(s: Editable) {}
    }

    private var imageSelectorHelper: ImageSelectorHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userInfo = CookieUtil.getUserInfo(this)
        if (userInfo == null) {
            finish()
            return
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_payment_mothod_add)

        imageSelectorHelper = ImageSelectorHelper(this)

        binding?.typeLayout?.setOnClickListener(this)
        binding?.btnSubmit?.setOnClickListener(this)
        binding?.name?.addTextChangedListener(watcher)
        binding?.bankCardNo?.addTextChangedListener(watcher)
        binding?.bankName?.addTextChangedListener(watcher)
        binding?.bankAddress?.addTextChangedListener(watcher)
        binding?.alipayAccount?.addTextChangedListener(watcher)
        binding?.wechatAccount?.addTextChangedListener(watcher)
        binding?.name?.setText(userInfo.realName)
        binding?.name?.isEnabled = false
        initPaymentMethodType()
        paymentMethodType = CommonUtil.getItemFromList(paymentMethodTypeList, 0)
        onPaymentMethodTypeChanged()
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return "添加收款方式"
    }

    override fun initToolbarViews(toolbar: Toolbar) {
        titleView = findViewById(R.id.action_bar_title)
        titleBigView = findViewById(R.id.action_bar_title_big)
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.type_layout) {
            DeepControllerWindow(this as Activity, "", paymentMethodType, paymentMethodTypeList, object : DeepControllerWindow.OnReturnListener<PaymentMethodType?> {
                override fun onReturn(window: DeepControllerWindow<PaymentMethodType?>, item: PaymentMethodType?) {
                    window.dismiss()
                    if (item != paymentMethodType) {
                        paymentMethodType = item
                        onPaymentMethodTypeChanged()
                    }
                }

            }).show()
        } else if (id == R.id.btn_submit) {
            if (paymentMethodType == null) {
                FryingUtil.showToast(mContext, "请选择支付方式")
            } else {
                doAddPaymentMethod()
                //                addPaymentMethod();
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imageSelectorHelper!!.onActivityResult(this, requestCode, resultCode, data)
    }

    private fun initPaymentMethodType() {
        paymentMethodTypeList = ArrayList()
        var paymentMethodType = PaymentMethodType()
        paymentMethodType.type = PaymentMethod.BANK
        paymentMethodTypeList?.add(paymentMethodType)
        paymentMethodType = PaymentMethodType()
        paymentMethodType.type = PaymentMethod.ALIPAY
        paymentMethodTypeList?.add(paymentMethodType)
        paymentMethodType = PaymentMethodType()
        paymentMethodType.type = PaymentMethod.WECHAT
        paymentMethodTypeList?.add(paymentMethodType)
    }

    private fun onPaymentMethodTypeChanged() {
        if (paymentMethodType == null) {
            checkClickable()
            return
        }
        val titleText = String.format("添加%s", paymentMethodType!!.getPayTypeText(this))
        titleView!!.text = titleText
        titleBigView!!.text = titleText
        binding?.typeIcon?.setImageResource(paymentMethodType!!.payIconRes)
        binding?.typeName?.text = paymentMethodType!!.getPayTypeText(this)
        imageSelectorHelper!!.clear()
        if (TextUtils.equals(paymentMethodType!!.type, PaymentMethod.ALIPAY)) {
            alipayQrcodeImageSelector = ImageSelector(this, this, binding?.alipayQrcode, ConstData.TEMP_IMG_NAME_01)
            imageSelectorHelper!!.addImageSelector(alipayQrcodeImageSelector)
        }
        if (TextUtils.equals(paymentMethodType!!.type, PaymentMethod.WECHAT)) {
            wechatQrcodeImageSelector = ImageSelector(this, this, binding?.wechatQrcode, ConstData.TEMP_IMG_NAME_01)
            imageSelectorHelper!!.addImageSelector(wechatQrcodeImageSelector)
        }
        binding?.bankLayout?.visibility = if (TextUtils.equals(paymentMethodType!!.type, PaymentMethod.BANK)) View.VISIBLE else View.GONE
        binding?.alipayLayout?.visibility = if (TextUtils.equals(paymentMethodType!!.type, PaymentMethod.ALIPAY)) View.VISIBLE else View.GONE
        binding?.wechatLayout?.visibility = if (TextUtils.equals(paymentMethodType!!.type, PaymentMethod.WECHAT)) View.VISIBLE else View.GONE
        checkClickable()
    }

    private fun checkClickable() {
        if (TextUtils.isEmpty(binding?.name?.text.toString().trim { it <= ' ' }) || paymentMethodType == null) {
            binding?.btnSubmit?.isEnabled = false
        } else {
            when {
                TextUtils.equals(paymentMethodType!!.type, PaymentMethod.BANK) -> {
                    binding?.btnSubmit?.isEnabled = !(TextUtils.isEmpty(binding?.bankCardNo?.text.toString().trim { it <= ' ' })
                            || TextUtils.isEmpty(binding?.bankName?.text.toString().trim { it <= ' ' })
                            || TextUtils.isEmpty(binding?.bankAddress?.text.toString().trim { it <= ' ' }))
                }
                TextUtils.equals(paymentMethodType!!.type, PaymentMethod.ALIPAY) -> {
                    binding?.btnSubmit?.isEnabled = !TextUtils.isEmpty(binding?.alipayAccount?.text.toString().trim { it <= ' ' })
                }
                TextUtils.equals(paymentMethodType!!.type, PaymentMethod.WECHAT) -> {
                    binding?.btnSubmit?.isEnabled = !TextUtils.isEmpty(binding?.wechatAccount?.text.toString().trim { it <= ' ' })
                }
            }
        }
    }

    private fun doAddPaymentMethod() {
        val callback: Callback<HttpRequestResultString?> = object : NormalCallback<HttpRequestResultString?>(mContext!!) {
            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    FryingUtil.showToast(mContext, "添加成功")
                    finish()
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) getString(R.string.data_error) else returnData.msg)
                }
            }
        }
        val type = paymentMethodType!!.type
        val userName = binding?.name?.text.toString()
        if (TextUtils.equals(paymentMethodType!!.type, PaymentMethod.BANK)) {
            C2CApiServiceHelper.addPaymentMethod(this, userName, type,
                    binding?.bankCardNo?.text.toString(),
                    binding?.bankName?.text.toString(),
                    binding?.bankAddress?.text.toString(),
                    null,
                    callback)
        } else if (TextUtils.equals(paymentMethodType!!.type, PaymentMethod.ALIPAY)) {
            val imagePath = alipayQrcodeImageSelector!!.path
            if (TextUtils.isEmpty(imagePath)) {
                C2CApiServiceHelper.addPaymentMethod(this, userName, type,
                        binding?.alipayAccount?.text.toString(),
                        null,
                        null,
                        null,
                        callback)
            } else {
                UserApiServiceHelper.uploadPublic(this, "file", File(imagePath), object : NormalCallback<HttpRequestResultString?>(mContext!!) {
                    override fun callback(returnData: HttpRequestResultString?) {
                        if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) { //上传成功
                            C2CApiServiceHelper.addPaymentMethod(mContext, userName, type,
                                    binding?.alipayAccount?.text.toString(),
                                    null,
                                    null,
                                    returnData.data,
                                    callback)
                        } else {
                            FryingUtil.showToast(mContext, returnData?.msg)
                        }
                    }
                })
            }
        } else if (TextUtils.equals(paymentMethodType!!.type, PaymentMethod.WECHAT)) {
            val imagePath = wechatQrcodeImageSelector!!.path
            if (TextUtils.isEmpty(imagePath)) {
                C2CApiServiceHelper.addPaymentMethod(this, userName, type,
                        binding?.wechatAccount?.text.toString(),
                        null,
                        null,
                        null,
                        callback)
            } else {
                UserApiServiceHelper.uploadPublic(this, "file", File(imagePath), object : NormalCallback<HttpRequestResultString?>(mContext!!) {
                    override fun callback(returnData: HttpRequestResultString?) {
                        if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) { //上传成功
                            C2CApiServiceHelper.addPaymentMethod(mContext, userName, type,
                                    binding?.wechatAccount?.text.toString(),
                                    null,
                                    null,
                                    returnData.data,
                                    callback)
                        } else {
                            FryingUtil.showToast(mContext, returnData?.msg)
                        }
                    }
                })
            }
        }
    }
}
