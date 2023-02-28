package com.black.c2c.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.C2CApiServiceHelper
import com.black.base.model.*
import com.black.base.model.c2c.C2COrderDetails
import com.black.base.model.c2c.C2CSellerMsg
import com.black.base.model.c2c.PayInfo
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.c2c.BR
import com.black.c2c.R
import com.black.c2c.adapter.C2CBillsAdapter
import com.black.c2c.adapter.PayMethodsAdapter

import com.black.c2c.databinding.ActivityPayMethodBinding
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import skin.support.content.res.SkinCompatResources

@Route(value = [RouterConstData.C2C_PAY])
class C2CPayMethodActivity : BaseActionBarActivity(), View.OnClickListener{
    private var binding: ActivityPayMethodBinding? = null
    private var list: ArrayList<PayInfo?>? = null
    private var adapter: PayMethodsAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pay_method)
        binding?.btnConfirm?.setOnClickListener(this)
        binding?.btnConfirmNew?.setOnClickListener(this)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(this, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.6 * 255).toInt()
        decoration.setDrawable(drawable)
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter = PayMethodsAdapter(mContext, BR.listItemC2COrderListModel, null)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setEmptyView(binding?.emptyView?.root)
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.isFocusable = false
        binding?.recyclerView?.layoutManager = layoutManager
        getAllPay()
    }

    override fun getTitleText(): String? {
        return super.getString(R.string.get_method)
    }
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btn_confirm){
            BlackRouter.getInstance().build(RouterConstData.C2C_PAY2).go(this)
        }
        if (id == R.id.btn_confirm_new){
            BlackRouter.getInstance().build(RouterConstData.C2C_PAY2).go(this)
        }
    }
    private fun getAllPay(){
        C2CApiServiceHelper.getAllPay(mContext, object : NormalCallback<HttpRequestResultDataList<PayInfo?>?>(mContext) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun callback(returnData: HttpRequestResultDataList<PayInfo?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    binding?.btnConfirmNew?.visibility = View.VISIBLE
                    binding?.btnConfirm?.visibility = View.GONE
                    binding?.adviceNull?.visibility = View.VISIBLE
                    binding?.tiShi?.visibility = View.GONE
                    list = returnData.data
                    adapter?.addAll(list)
                    adapter?.notifyDataSetChanged()
                } else {
                    binding?.btnConfirm?.visibility = View.VISIBLE
                    binding?.btnConfirmNew?.visibility = View.GONE
                    binding?.tiShi?.visibility = View.VISIBLE
                    binding?.adviceNull?.visibility = View.GONE
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }



}