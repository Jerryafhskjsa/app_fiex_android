package com.black.c2c.activity

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.C2CApiServiceHelper
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.model.c2c.OtcReceiptDTO
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.c2c.R
import com.black.c2c.databinding.ActicityC2cCardsBinding
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.CommonUtil

@Route(value = [RouterConstData.C2C_CARDS])
class C2CCardsActivity: BaseActionBarActivity(), View.OnClickListener {
    private var binding: ActicityC2cCardsBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.acticity_c2c_cards)
        binding?.cards?.setOnClickListener(this)
        binding?.name?.setOnClickListener(this)
        binding?.cardsCmy?.setOnClickListener(this)
        binding?.otherCmy?.setOnClickListener(this)
        binding?.btnSubmit?.setOnClickListener(this)
        binding?.notUse?.setOnClickListener(this)
        binding?.googleCode?.setOnClickListener(this)
        binding?.googleCodeCopy?.setOnClickListener(this)
        binding?.root?.findViewById<ImageButton>(R.id.img_action_bar_right)?.visibility = View.VISIBLE
        binding?.root?.findViewById<ImageButton>(R.id.img_action_bar_right)?.setOnClickListener{
            v ->
        }
    }

    override fun getTitleText(): String? {
        return super.getString(R.string.pay_add)
    }
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btn_submit){
            getReceipt()
        }
        if (id == R.id.not_use){
        }
        if (id == R.id.google_code_copy){
            CommonUtil.pasteText(mContext, object : Callback<String?>() {
                override fun error(type: Int, error: Any) {}
                override fun callback(returnData: String?) {
                    binding?.googleCode?.setText(returnData ?: "")
                }
            })
        }
    }

    private fun getReceipt(){
        val  otcReceiptDTO: OtcReceiptDTO? = null
        otcReceiptDTO?.name = binding?.name?.text?.trim{ it <= ' '}.toString()
        otcReceiptDTO?.account = binding?.cards?.text?.trim{ it <= ' '}.toString()
        otcReceiptDTO?.googleCode = binding?.googleCode?.text?.trim{ it <= ' '}.toString()
        otcReceiptDTO?.depositBank = binding?.cardsCmy?.text?.trim{ it <= ' '}.toString()
        otcReceiptDTO?.subbranch = binding?.otherCmy?.text?.trim{ it <= ' '}.toString()
        C2CApiServiceHelper.getReceipt(mContext, otcReceiptDTO , object : NormalCallback<HttpRequestResultString?>(mContext) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                } else {

                    FryingUtil.showToast(context, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }
}