package com.black.c2c.activity

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.C2CApiServiceHelper
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.model.c2c.C2COrderDetails
import com.black.base.model.c2c.OtcReceiptModel
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.widget.SpanCheckedTextView
import com.black.base.widget.SpanTextView
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cSellerConfirmBinding
import com.black.c2c.databinding.ActivityC2cSellerWaitBinding
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route

@Route(value = [RouterConstData.C2C_WAITE2])
class C2CWattingConfirmActivity: BaseActionBarActivity(), View.OnClickListener{
    private var binding: ActivityC2cSellerConfirmBinding? = null
    private var id: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_seller_confirm)
        id = intent.getStringExtra(ConstData.BUY_PRICE)
        binding?.btnCancel?.setOnClickListener(this)
        binding?.aboveBar?.setOnClickListener(this)
        binding?.bottomBar?.setOnClickListener(this)
        binding?.num1?.setOnClickListener(this)
        binding?.num2?.setOnClickListener(this)
        binding?.num3?.setOnClickListener(this)
        getC2COIV2()
        /*binding?.root?.findViewById<SpanTextView>(R.id.img_action_bar_mail)?.setOnClickListener{}
        binding?.root?.findViewById<SpanTextView>(R.id.img_action_bar_phone)?.setOnClickListener{}*/
    }

    override fun getTitleText(): String? {
        return super.getString(R.string.waite_confirm)
    }
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btn_cancel){
            cancelDialog()
        }
        if (id == R.id.bottom_bar){
            binding?.above?.visibility = View.VISIBLE
            binding?.bottom?.visibility = View.GONE
        }
        if (id == R.id.above_bar){
            binding?.above?.visibility = View.GONE
            binding?.bottom?.visibility = View.VISIBLE
        }
        if (id == R.id.num1){
        }
        if (id == R.id.num2){
        }
        if (id == R.id.num3){
        }
        if (id == R.id.bottom_bar){
        }
    }
    private fun cancelDialog() {
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.confirm_dialog, null)
        val dialog = Dialog(mContext!!, R.style.AlertDialog)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            //设置背景昏暗度
            params.dimAmount = 0.2f
            params.gravity = Gravity.CENTER
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = resources.displayMetrics
        val layoutParams =
            ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(contentView, layoutParams)
        dialog.show()
        dialog.findViewById<SpanCheckedTextView>(R.id.range).setOnClickListener { v ->
            dialog.findViewById<SpanCheckedTextView>(R.id.range).isChecked = dialog.findViewById<SpanCheckedTextView>(R.id.range).isChecked == false
        }
        dialog.findViewById<View>(R.id.btn_confirm).setOnClickListener { v ->
            if(dialog.findViewById<SpanCheckedTextView>(R.id.range).isChecked) {
                getConfirm()
            }
            else{
                FryingUtil.showToast(mContext,"请先确认是否收到该笔款项")
            }
        }
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { v ->

            dialog.dismiss()
        }
    }
    private fun getConfirm(){
        C2CApiServiceHelper.getConfirmGet(mContext, id , object : NormalCallback<HttpRequestResultString?>(mContext) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    FryingUtil.showToast(mContext,"放币成功")
                    val extras = Bundle()
                    extras.putString(ConstData.BUY_PRICE,id)
                    BlackRouter.getInstance().build(RouterConstData.C2C_CONFRIM).with(extras).go(context)
                } else {

                    FryingUtil.showToast(context, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    //订单详情
    fun getC2COIV2(){
        C2CApiServiceHelper.getC2COIV2(
            mContext,
            id,
            object : NormalCallback<HttpRequestResultData<C2COrderDetails?>?>(mContext) {
                override fun error(type: Int, error: Any?) {
                    super.error(type, error)
                }

                override fun callback(returnData: HttpRequestResultData<C2COrderDetails?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        binding?.id?.setText(id)
                        binding?.coinType?.setText(returnData.data?.coinType)
                        binding?.amount?.setText(returnData.data?.amount.toString() + returnData.data?.coinType)
                        binding?.price?.setText(returnData.data?.price.toString())
                        binding?.total?.setText((returnData.data?.amount!! * returnData.data?.price!!).toString())
                        binding?.time?.setText(returnData.data?.createTime)
                        binding?.realName?.setText(returnData.data?.otherSideRealName)
                        binding?.realNameName?.setText(returnData.data?.payEeRealName)
                        binding?.name1?.setText(returnData.data?.payEeRealName)
                        binding?.name2?.setText(returnData.data?.payEeRealName)
                        val payMethod = returnData.data?.payMethod
                        if (payMethod == 2) {
                            binding?.name3?.setText(returnData.data?.receiptInfo?.name)
                            binding?.cardsNum?.setText(returnData.data?.receiptInfo?.account)
                            binding?.cpy?.setText(returnData.data?.receiptInfo?.depositBank)
                            binding?.otherCmy?.setText(returnData.data?.receiptInfo?.subbranch)
                        }
                        else if (payMethod == 1){
                            binding?.name4?.setText(returnData.data?.receiptInfo?.name)
                            binding?.aliNum?.setText(returnData.data?.receiptInfo?.account)
                            val maTwo = returnData.data?.receiptInfo?.receiptImage
                        }
                        else {
                            val maOne = returnData.data?.receiptInfo?.receiptImage
                            binding?.name5?.setText(returnData.data?.receiptInfo?.name)
                            binding?.weiXinNum?.setText(returnData.data?.receiptInfo?.account)
                        }
                    } else {
                        FryingUtil.showToast(
                            mContext,
                            if (returnData == null) "null" else returnData.msg
                        )
                    }
                }
            })
    }
}