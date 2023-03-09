package com.black.c2c.activity

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.C2CApiServiceHelper
import com.black.base.api.UserApiServiceHelper
import com.black.base.model.HttpRequestResultData
import com.black.base.model.LoginVO
import com.black.base.model.ProTokenResult
import com.black.base.model.user.UserInfo
import com.black.base.util.ConstData
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.view.DeepControllerWindow
import com.black.base.widget.SpanTextView
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cMainBinding
import com.black.c2c.fragment.C2CCustomerBuyFragment
import com.black.c2c.fragment.C2CCustomerBuyItemFragment
import com.black.c2c.fragment.C2CCustomerSaleFragment
import com.black.net.HttpCookieUtil
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.Callback
import java.util.*

@Route(value = [RouterConstData.C2C_NEW])
 class C2CNewActivity : BaseActionBarActivity(), View.OnClickListener{
    companion object {
        private val TAB_TITLES = arrayOfNulls<String>(2)
        private val TAB_SELF = "自选区"
        private val TAB_QUCILK = "快捷区"

    }

    private var binding: ActivityC2cMainBinding? = null
    private var userInfo: UserInfo? = null
    private var actionType = ConstData.TAB_EXCHANGE
    private var tab = TAB_SELF
    private var fManager: FragmentManager? = null
    private var typeList: MutableList<String>? = null
    private var fragmentList: java.util.ArrayList<Fragment>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userInfo = CookieUtil.getUserInfo(mContext)
        if (userInfo == null) {
            finish()
            return
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_main)
        binding?.c2cOneKey?.setOnClickListener(this)
        binding?.c2cCustomer?.setOnClickListener(this)
        binding?.areaChoose?.setOnClickListener(this)
        binding?.actionBarBack?.setOnClickListener(this)
        binding?.bills?.setOnClickListener(this)
        binding?.rate?.setOnClickListener(this)
         binding?.settings?.setOnClickListener(this)
        binding?.person?.setOnClickListener(this)
        fManager = supportFragmentManager
        typeList = ArrayList()
        typeList!!.add(TAB_SELF)
        typeList!!.add(TAB_QUCILK)
        TAB_TITLES[0] = "B"
        TAB_TITLES[1] = "S"
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
                override fun restoreState(state: Parcelable?, loader: ClassLoader?) {

                }
            }
        binding?.viewPager?.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                refreshCurrentType(position)
            }

        })
        refreshCurrentType(actionType)
        changeFragment(if (actionType == ConstData.TAB_EXCHANGE) 0 else 1)
    }

    override fun isStatusBarDark(): Boolean {
        return false
    }

    override fun onClick(v: View) {
        val id = v.id
        when (id) {
            R.id.c2c_one_key -> {
                if (ConstData.TAB_EXCHANGE != actionType) {
                    actionType = ConstData.TAB_EXCHANGE
                    refreshCurrentType(actionType)
                    changeFragment(0)
                }
            }
            R.id.c2c_customer -> {
                    if (ConstData.TAB_WITHDRAW != actionType) {
                        actionType = ConstData.TAB_WITHDRAW
                        refreshCurrentType(actionType)
                        changeFragment(1)
                    }
            }
            R.id.settings -> {
                settingsDialog()
            }
            R.id.action_bar_back -> {
                finish()
                return
            }
            R.id.area_choose -> {
                DeepControllerWindow(mContext as Activity, null, tab , typeList, object : DeepControllerWindow.OnReturnListener<String> {
                    override fun onReturn(window: DeepControllerWindow<String>, item: String) {
                        window.dismiss()
                        tab = item
                        when(item){
                            TAB_SELF -> {
                                binding?.areaChoose?.setText(R.string.self_selection_area)
                            }
                            TAB_QUCILK -> {
                                BlackRouter.getInstance().build(RouterConstData.C2C_QIULK).go(mContext)
                            }
                        }
                    }

                }).show()
            }
            R.id.rate -> {

            }
            R.id.bills -> {
                BlackRouter.getInstance().build(RouterConstData.C2C_BILLS).go(this)
            }
            R.id.person -> {
                BlackRouter.getInstance().build(RouterConstData.C2C_MINE).go(mContext)
            }
        }
    }
    private fun settingsDialog() {
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.bills_dialog, null)
        val dialog = Dialog(mContext!!, R.style.AlertDialog)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            params.gravity = Gravity.TOP
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            params.y = 80
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = resources.displayMetrics
        val layoutParams = ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(contentView, layoutParams)
        dialog.findViewById<SpanTextView>(R.id.merchant).setOnClickListener{
            v ->
            BlackRouter.getInstance().build(RouterConstData.C2C_APPLY1).go(this)
            dialog.dismiss()
        }
        dialog.findViewById<SpanTextView>(R.id.settings).setOnClickListener{
                v ->
            BlackRouter.getInstance().build(RouterConstData.C2C_PAY).go(this)
            dialog.dismiss()
        }
        dialog.findViewById<SpanTextView>(R.id.btn_bills).setOnClickListener{
                v ->
            BlackRouter.getInstance().build(RouterConstData.C2C_BILLS).go(this)
            dialog.dismiss()
        }
        dialog.show()
    }
    private fun refreshCurrentType(type: Int) {
        if (ConstData.TAB_EXCHANGE == type) {
            binding?.c2cOneKey?.isChecked = false
            binding?.c2cCustomer?.isChecked = true
        } else if (ConstData.TAB_WITHDRAW == type) {
            binding?.c2cOneKey?.isChecked = true
            binding?.c2cCustomer?.isChecked = false
        }
    }

    private fun changeFragment(position: Int) {
        binding?.viewPager?.setCurrentItem(position, true)
    }
    private fun init(){
        if (fragmentList == null) {
            fragmentList = ArrayList()
        }

        fragmentList?.clear()
        fragmentList?.add(C2CCustomerBuyFragment().also {
            val bundle = Bundle()
            val direction = "B"
            bundle.putString(ConstData.COIN_TYPE,direction)
            it.arguments = bundle
        })
        fragmentList?.add(C2CCustomerSaleFragment().also {
            val bundle = Bundle()
            val direction = "S"
            bundle.putString(ConstData.COIN_TYPE,direction)
            it.arguments = bundle
//            assetsWalletFragment = it
//            assetsWalletFragment?.setEventListener(this)
        })

    }
}