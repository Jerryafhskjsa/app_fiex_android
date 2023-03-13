package com.black.c2c.activity

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Parcelable
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.C2CApiServiceHelper
import com.black.base.model.HttpRequestResultData
import com.black.base.model.NormalCallback
import com.black.base.model.c2c.C2COrderDetails
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cBuyerOderBinding
import com.black.c2c.fragment.C2CBuyOrderFragment
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import java.util.*
import kotlin.collections.ArrayList

@Route(value = [RouterConstData.C2C_BUYER])
class C2CBuyerOderActivity: BaseActionBarActivity(), View.OnClickListener {
    private var binding: ActivityC2cBuyerOderBinding? = null
    private var sellerName: String? = "帅"
    private var id2: String? = null
    private var payChain: String? = null
    private var fragmentList: ArrayList<Fragment>? = null
    private var totalTime: Long = 15*60*1000 //总时长 15min
    private var countDownTimer: CountDownTimer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_buyer_oder)
        id2 = intent.getStringExtra(ConstData.BUY_PRICE)
        payChain = intent.getStringExtra(ConstData.USER_YES)
        binding?.btnConfirm?.setOnClickListener(this)
        getPayChoose()
        init()
        binding!!.viewPager.adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment {
                return fragmentList!![position]
            }

            override fun getCount(): Int {
                return fragmentList!!.size
            }

            override fun restoreState(state: Parcelable?, loader: ClassLoader?) {

            }
        }

    }

    override fun getTitleText(): String? {
        return  sellerName
    }
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btn_confirm) {
            val extras = Bundle()
            extras.putString(ConstData.BUY_PRICE, id2)
            extras.putString(ConstData.USER_YES, payChain)
            BlackRouter.getInstance().build(RouterConstData.C2C_PAY_FOR).with(extras).go(mContext)
        }
    }
    private fun init(){
        if (fragmentList == null) {
            fragmentList = java.util.ArrayList()
        }
        fragmentList?.clear()
        fragmentList?.add(C2CBuyOrderFragment().also {
            val bundle = Bundle()
            bundle.putString(ConstData.COIN_TYPE, id2)
            it.arguments = bundle
        })
    }
    //获取总价
    private fun getPayChoose() {
        C2CApiServiceHelper.getC2CDetails(
            mContext,
            id2,
            object : NormalCallback<HttpRequestResultData<C2COrderDetails?>?>(mContext) {
                override fun error(type: Int, error: Any?) {
                    super.error(type, error)
                }

                override fun callback(returnData: HttpRequestResultData<C2COrderDetails?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        binding?.money?.setText("￥" + (returnData.data?.amount!! * returnData.data?.price!!).toString())
                        val time1 = returnData.data?.validTime?.time
                        val calendar: Calendar = Calendar.getInstance()
                        val time2 = calendar.time.time
                        totalTime = time1!!.minus(time2)
                        countDownTimer = object : CountDownTimer(totalTime, 1000) {
                            //1000ms运行一次onTick里面的方法
                            override fun onFinish() {
                                binding?.time?.setText("00:00")
                                FryingUtil.showToast(mContext, "订单已取消")
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


}