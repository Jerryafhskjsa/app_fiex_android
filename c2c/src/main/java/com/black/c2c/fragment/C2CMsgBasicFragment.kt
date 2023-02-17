package com.black.c2c.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.black.base.api.C2CApiServiceHelper
import com.black.base.fragment.BaseFragment
import com.black.base.model.HttpRequestResultData
import com.black.base.model.NormalCallback
import com.black.base.model.c2c.C2CSMSG
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.c2c.R
import com.black.c2c.databinding.FragmentMsgBasicBinding
import com.black.net.HttpRequestResult
import kotlinx.android.synthetic.main.list_item_c2c_seller_buy.*



class C2CMsgBasicFragment: BaseFragment(), View.OnClickListener {
    private var binding: FragmentMsgBasicBinding? = null
    private var merchantId: Int? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (binding != null) {
            return binding?.root
        }
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_msg_basic, container, false)
        binding?.black?.setOnClickListener(this)
        getSellerMsg()
        return binding?.root
    }
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.black){
            finish
        }

    }

    private fun getSellerMsg() {
        merchantId = arguments?.getInt(ConstData.PAIR)
        C2CApiServiceHelper.getSellerMsg(mContext, merchantId,  object : NormalCallback<HttpRequestResultData<C2CSMSG?>?>(context!!) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultData<C2CSMSG?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    binding?.order?.setText(returnData.data?.merchantCompleted30Days.toString())
                    binding?.amount?.setText(returnData.data?.merchantCompletedTotal.toString())
                    binding?.orderConfirm?.setText(returnData.data?.completionRate + "%")
                    binding?.time?.setText(if(returnData.data?.avgReleaseTime == null ) nullAmount else (returnData.data?.avgReleaseTime!! / 60).toString() + "min")
                    binding?.payTime?.setText(if(returnData.data?.avgReleaseTime == null ) nullAmount else (returnData.data?.avgReleaseTime!! / 60).toString() + "min")

                } else {

                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

}