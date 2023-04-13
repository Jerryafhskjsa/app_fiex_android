package com.black.c2c.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.api.C2CApiServiceHelper
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.model.c2c.C2CBills
import com.black.base.util.FryingUtil
import com.black.base.util.TimeUtil
import com.black.c2c.R
import com.black.c2c.activity.C2CNewActivity
import com.black.c2c.databinding.ListC2cBillsBinding
import com.black.net.HttpRequestResult
import com.black.util.NumberUtil
import skin.support.content.res.SkinCompatResources
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class C2CBillsAdapter(context: Context, variableId: Int, data: ArrayList<C2CBills?>?) : BaseRecycleDataBindAdapter<C2CBills?, ListC2cBillsBinding>(context, variableId, data) {
    private var c1 = 0
    private var t5 = 0
    private var t1 = 0
    private var totalTime : Long = 15*60*1000 //总时长 15min
    private var countDownTimer: CountDownTimer? = null
    override fun getResourceId(): Int {
        return R.layout.list_c2c_bills
    }
    override fun resetSkinResources() {
        super.resetSkinResources()
        c1 = SkinCompatResources.getColor(context, R.color.T7)
        t5 = SkinCompatResources.getColor(context, R.color.T5)
        t1 = SkinCompatResources.getColor(context, R.color.C5)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: BaseViewHolder<ListC2cBillsBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val c2CBills = getItem(position)
        val viewHolder = holder.dataBing
        if (c2CBills?.direction == "B") {
            viewHolder?.buy?.setText(getString(R.string.buy_02))
            viewHolder?.buy?.setTextColor(c1)
        }
        if (c2CBills?.direction == "S") {
            viewHolder?.buy?.setText(getString(R.string.sell))
            viewHolder?.buy?.setTextColor(t5)
        }
        val time = TimeUtil.getTime(c2CBills?.createTime)
        viewHolder?.time?.setText(time)
        viewHolder?.coinType?.setText(c2CBills?.coinType)
        viewHolder?.account?.setText(String.format("%s", NumberUtil.formatNumberDynamicScaleNoGroup(c2CBills?.price, 8, 2, 8)))
        viewHolder?.amount?.setText(String.format("%s", NumberUtil.formatNumberDynamicScaleNoGroup(c2CBills?.amount, 8, 2, 8)))
        viewHolder?.money?.setText("￥" + String.format("%s", NumberUtil.formatNumberDynamicScaleNoGroup(c2CBills?.price!! * c2CBills.amount!!, 8, 2, 8)))
        viewHolder?.status?.setText(c2CBills?.getStatusText(context))
        viewHolder?.billsNum?.setText(c2CBills?.id)
        if (c2CBills?.status == -1){

        }
        if (c2CBills?.status == 3){

        }
        if (c2CBills?.status == 4){}
        if (c2CBills?.status == 5){}
        if (c2CBills?.status == 2){
            viewHolder?.status?.setTextColor(t1)
            viewHolder?.lastTime?.setTextColor(t1)
            val time1 = c2CBills.validTime?.time
            val calendar: Calendar = Calendar.getInstance()
            val time2 = calendar.time.time
            totalTime = time1!!.minus(time2)
            countDownTimer = object : CountDownTimer(totalTime,1000){//1000ms运行一次onTick里面的方法
            override fun onFinish(){
                viewHolder?.lastTime?.setText("00:00")
            }
                override fun onTick(millisUntilFinished: Long) {
                        val minute = millisUntilFinished / 1000 / 60 % 60
                        val second = millisUntilFinished / 1000 % 60
                        viewHolder?.lastTime?.setText("$minute:$second")
                    }
            }.start()
        }
    }

}