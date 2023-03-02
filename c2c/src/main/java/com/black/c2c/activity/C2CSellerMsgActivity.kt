package com.black.c2c.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.C2CApiServiceHelper
import com.black.base.model.HttpRequestResultData
import com.black.base.model.NormalCallback
import com.black.base.model.c2c.C2CMainAD
import com.black.base.model.c2c.C2CSMSG
import com.black.base.model.c2c.SellerAD
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.c2c.BR
import com.black.c2c.R
import com.black.c2c.adapter.C2CSellerBuyAdapter
import com.black.c2c.databinding.ActivitySellerMsgBinding
import com.black.c2c.fragment.C2CMsgBasicFragment
import com.black.c2c.fragment.C2CSADFragment
import com.black.net.HttpRequestResult
import com.black.router.annotation.Route
import skin.support.content.res.SkinCompatResources


@Route(value = [RouterConstData.C2C_SELLER])
class C2CSellerMsgActivity: BaseActionBarActivity(), View.OnClickListener {
    companion object {
        private val TAB_TITLES = arrayOfNulls<String>(2) //标题
    }
    private var actionType = ConstData.TAB_EXCHANGE
    private var binding: ActivitySellerMsgBinding? = null
    private var merchantId: Int? = null
    private var dataList:ArrayList<C2CMainAD?>? = ArrayList()
    private var sellList:ArrayList<C2CMainAD?>? = ArrayList()
    private var fragmentList: java.util.ArrayList<Fragment>? = null
    private var c2CMsgBasicFragment: C2CMsgBasicFragment? = null
    private var c2CSADFragment: C2CSADFragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_seller_msg)
        merchantId = intent.getIntExtra(ConstData.BIRTH,0)
        binding?.msg?.setOnClickListener(this)
        binding?.ad?.setOnClickListener(this)
        TAB_TITLES[0] = "信息"
        TAB_TITLES[1] = "广告"
        getC2CAD(false)
        getSellerMsg()
        init()

        binding!!.viewPager.adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment {
                return fragmentList!![position]
            }

            override fun getCount(): Int {
                return fragmentList!!.size
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return TAB_TITLES[position]
            }

            override fun restoreState(state: Parcelable?, loader: ClassLoader?) {}
        }
        binding?.viewPager?.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                refreshCurrentType(position)
            }

        })
        refreshCurrentType(actionType)
        changeFragment(if (actionType == ConstData.TAB_EXCHANGE) 0 else 1)
    }
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.msg){
            actionType = 0
            refreshCurrentType(actionType)
            changeFragment(0)
        }
        if (id == R.id.ad){
            binding?.barA?.visibility = View.VISIBLE
            binding?.barB?.visibility = View.GONE
            binding!!.msg.isChecked = false
            binding!!.ad.isChecked = true
            actionType = 1
            refreshCurrentType(actionType)
            changeFragment(1)
        }
    }

    private fun refreshCurrentType(type: Int){
        if (type == 0){
            binding!!.ad.isChecked = false
            binding?.barA?.visibility = View.GONE
            binding?.barB?.visibility = View.VISIBLE
            binding!!.msg.isChecked = true
        }
        else{
            binding?.barA?.visibility = View.VISIBLE
            binding?.barB?.visibility = View.GONE
            binding!!.msg.isChecked = false
            binding!!.ad.isChecked = true
        }
    }

    private fun changeFragment(position: Int) {
        binding?.viewPager?.setCurrentItem(position, true)
    }

    private fun init() {
        if (fragmentList == null) {
            fragmentList = java.util.ArrayList()
        }
        fragmentList?.clear()
        fragmentList?.add(C2CMsgBasicFragment().also {
            val bundle = Bundle()
            bundle.putInt(ConstData.PAIR, merchantId!!)
            it.arguments = bundle
            c2CMsgBasicFragment = it
        })
        fragmentList?.add(C2CSADFragment().also {
            val bundle = Bundle()
            bundle.putInt(ConstData.PAIR, merchantId!!)
            it.arguments = bundle
            c2CSADFragment = it
        })
    }

    private fun getSellerMsg() {
        C2CApiServiceHelper.getSellerMsg(mContext, merchantId,  object : NormalCallback<HttpRequestResultData<C2CSMSG?>?>(mContext) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultData<C2CSMSG?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    binding?.name1?.setText(returnData.data?.name!![0].toString())
                    binding?.name?.setText(returnData.data?.name)
                    if (returnData.data?.merchantAuthentication == true){
                        binding?.barTrue?.visibility = View.VISIBLE
                        binding?.barFalse?.visibility = View.GONE
                    }
                    else{
                        binding?.barTrue?.visibility = View.GONE
                        binding?.barFalse?.visibility = View.VISIBLE
                    }
                    if (returnData.data?.emailAuthentication == true)
                    {
                        binding?.barMail?.visibility = View.VISIBLE
                        binding?.barMailFalse?.visibility = View.GONE
                    }
                    else{
                        binding?.barMail?.visibility = View.GONE
                        binding?.barMailFalse?.visibility = View.VISIBLE
                    }
                    if (returnData.data?.phoneAuthentication == true)
                    {
                        binding?.barPhone?.visibility = View.VISIBLE
                        binding?.barPhoneFalse?.visibility = View.GONE
                    }
                    else{
                        binding?.barPhoneFalse?.visibility = View.VISIBLE
                        binding?.barPhone?.visibility = View.GONE
                    }
                    if (returnData.data?.authentication == true)
                    {
                        binding?.barPerson?.visibility = View.VISIBLE
                        binding?.barPersonFalse?.visibility = View.GONE
                    }
                    else{
                        binding?.barPersonFalse?.visibility = View.GONE
                        binding?.barPerson?.visibility = View.VISIBLE
                    }
                } else {

                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }
    private fun getC2CAD(isShowLoading: Boolean) {
        C2CApiServiceHelper.getC2CSellerAD(mContext, isShowLoading, merchantId,  object : NormalCallback<HttpRequestResultData<SellerAD<C2CMainAD?>?>?>(mContext) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            @SuppressLint("SetTextI18n")
            override fun callback(returnData: HttpRequestResultData<SellerAD<C2CMainAD?>?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    val data = returnData.data
                    dataList = data?.buy
                    sellList = data?.sell
                    val num1: Int? = dataList?.size
                    val num2: Int? = sellList?.size
                    val num3: Int = if (num1 == null && num2 == null) 0 else if (num1 == null && num2 != null) num2 else if (num1 != null && num2 == null) num1 else num1!! + num2!!
                    binding?.ad?.text = "广告($num3)"
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }
}