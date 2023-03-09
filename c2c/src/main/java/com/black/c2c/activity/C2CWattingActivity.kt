package com.black.c2c.activity

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.C2CApiServiceHelper
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.model.c2c.C2COrderDetails
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.util.TimeUtil
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cSellerWaitBinding
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import java.util.*

@Route(value = [RouterConstData.C2C_WAITE1])
class C2CWattingActivity: BaseActionBarActivity(), View.OnClickListener{
    private var binding: ActivityC2cSellerWaitBinding? = null
    private var id: String? = null
    private var totalTime : Long = 15*60*1000 //总时长 15min
    private var countDownTimer: CountDownTimer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_seller_wait)
        id = intent.getStringExtra(ConstData.BUY_PRICE)
        binding?.idNum?.setText(id)
        binding?.btnCancel?.setOnClickListener(this)
        binding?.num?.setOnClickListener(this)
        getC2COIV2()
       /* binding?.root?.findViewById<SpanTextView>(R.id.img_action_bar_mail)?.setOnClickListener{}
        binding?.root?.findViewById<SpanTextView>(R.id.img_action_bar_phone)?.setOnClickListener{}*/
    }

    override fun getTitleText(): String? {
        return super.getString(R.string.waite)
    }
    override fun onClick(v: View) {
        val id2 = v.id
        if (id2 == R.id.btn_cancel){
            val extras = Bundle()
            extras.putString(ConstData.BUY_PRICE,id)
            BlackRouter.getInstance().build(RouterConstData.C2C_WAITE2).with(extras).go(this)
        }
        if (id2 == R.id.num){
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
                        binding?.coinType?.setText(returnData.data?.coinType)
                        binding?.account?.setText(returnData.data?.amount.toString() + returnData.data?.coinType)
                        binding?.price?.setText(returnData.data?.price.toString())
                        binding?.total?.setText((returnData.data?.amount!! * returnData.data?.price!!).toString())
                        val time = TimeUtil.getTime(returnData.data?.createTime)
                        binding?.createTime?.setText(time)
                        val time1 = returnData.data?.validTime?.time
                        val calendar: Calendar = Calendar.getInstance()
                        val time2 = calendar.time.time
                        totalTime = time1!!.minus(time2)
                        countDownTimer = object : CountDownTimer(totalTime,1000){//1000ms运行一次onTick里面的方法
                        override fun onFinish(){
                            binding?.time?.setText("00:00")
                            FryingUtil.showToast(mContext,"订单已取消")
                            finish()
                        }
                            override fun onTick(millisUntilFinished: Long) {
                                val minute = millisUntilFinished / 1000 / 60 % 60
                                val second = millisUntilFinished / 1000 % 60
                                binding?.time?.setText("$minute:$second")
                            }
                        }.start()
                    } else {

                        FryingUtil.showToast(
                            mContext,
                            if (returnData == null) "null" else returnData.msg
                        )
                    }
                }
            })
    }

    //撤单
    private fun getC2cCancel(id: String?){

        C2CApiServiceHelper.getC2COrderCancel(mContext, id,  object : NormalCallback<HttpRequestResultString?>(mContext) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    FryingUtil.showToast(mContext,"取消订单成功")
                    val intent = Intent(this@C2CWattingActivity, C2CNewActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {

                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }
}