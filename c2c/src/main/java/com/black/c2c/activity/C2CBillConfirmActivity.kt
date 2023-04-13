package com.black.c2c.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.C2CApiServiceHelper
import com.black.base.model.HttpRequestResultData
import com.black.base.model.NormalCallback
import com.black.base.model.c2c.C2COrderDetails
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.util.TimeUtil
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cBillConfirmBinding
import com.black.c2c.databinding.ActivityC2cConfirmBinding
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import skin.support.content.res.SkinCompatResources
import java.util.*

@Route(value = [RouterConstData.C2C_BILL_CONFRIM])
class C2CBillConfirmActivity: BaseActionBarActivity(), View.OnClickListener{
    private var binding: ActivityC2cBillConfirmBinding? = null
    private var id: String? = null
    private var adid: String? = null
    private var totalTime : Long = 24*60*60*1000 //总时长 24h
    private var countDownTimer : CountDownTimer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_bill_confirm)
        id = intent.getStringExtra(ConstData.BUY_PRICE)
        binding?.orderAgain?.setOnClickListener(this)
        binding?.time?.setOnClickListener(this)
        binding?.actionBarBack?.setOnClickListener(this)
        binding?.wallet?.setOnClickListener(this)
        binding?.msg?.setOnClickListener(this)
        binding?.wallet?.getPaint()?.setFlags(Paint.FAKE_BOLD_TEXT_FLAG)
        getC2COIV2()
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.time) {
            cancelDialog()
        }
        if (id == R.id.action_bar_back) {
            val intent = Intent(this, C2CBillsActivity::class.java)
            startActivity(intent)
            finish()
        }
        if (id == R.id.order_again){
            getC2COIV2()
            val extras = Bundle()
            extras.putString(ConstData.PAIR, adid)
            BlackRouter.getInstance().build(RouterConstData.C2C_BUY).with(extras).go(mContext)
        }

        if (id == R.id.wallet) {
            BlackRouter.getInstance().build(RouterConstData.C2C_MINE).go(mContext)
        }    }
    private fun cancelDialog() {
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.shen_shu_dialog, null)
        val dialog = Dialog(mContext, R.style.AlertDialog)
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
        dialog.findViewById<View>(R.id.btn_confirm).setOnClickListener { v ->
            FryingUtil.showToast(mContext, "申述已提交")
            dialog.dismiss()
        }
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { v ->

            dialog.dismiss()
        }
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
                        adid = returnData.data?.advertisingId
                        binding?.id?.setText(id)
                        binding?.coinType?.setText(returnData.data?.coinType)
                        binding?.amount?.setText(returnData.data?.amount.toString() + returnData.data?.coinType)
                        binding?.price?.setText(returnData.data?.price.toString())
                        binding?.total?.setText((returnData.data?.amount!! * returnData.data?.price!!).toString())
                        val time = TimeUtil.getTime(returnData.data?.createTime)
                        binding?.createTime?.setText(time)
                        binding?.realName?.setText(returnData.data?.otherSideRealName)
                        binding?.realNameName?.setText(returnData.data?.payEeRealName)
                        val c1 = SkinCompatResources.getColor(context, R.color.T13)
                        val t5 = SkinCompatResources.getColor(context, R.color.T13)
                        if (returnData.data?.direction == "B")
                        {
                            binding?.orderAgain?.visibility = View.VISIBLE
                            binding?.direction?.setText(getString(R.string.buy_02))
                            binding?.direction?.setTextColor(c1)
                        }
                        if (returnData.data?.direction == "S") {
                            binding?.time?.visibility = View.VISIBLE
                            binding?.direction?.setText(getString(R.string.sell))
                            binding?.direction?.setTextColor(t5)
                            val time1 = returnData.data?.canAllegeEndTime?.time
                            val calendar: Calendar = Calendar.getInstance()
                            val time2 = calendar.time.time
                            totalTime = time1!!.minus(time2)
                            countDownTimer = object : CountDownTimer(totalTime, 1000) {
                                //1000ms运行一次onTick里面的方法
                                override fun onFinish() {
                                    binding?.time?.visibility = View.GONE
                                }

                                override fun onTick(millisUntilFinished: Long) {
                                    val hour = millisUntilFinished / 1000 / 60 / 60 % 24
                                    val minute = millisUntilFinished / 1000 / 60 % 60
                                    val second = millisUntilFinished / 1000 % 60
                                    binding?.time?.setText(getString(R.string.state) + "$hour:$minute:$second")
                                }
                            }.start()
                        }
                        val payMethod = returnData.data?.payMethod
                        if (payMethod == 0) {
                            binding?.payFor?.setText(getString(R.string.cards))
                        }
                        else if (payMethod == 2){
                            binding?.payFor?.setText(getString(R.string.wei_xin))
                        }
                        else {
                            binding?.payFor?.setText(getString(R.string.id_pay))
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