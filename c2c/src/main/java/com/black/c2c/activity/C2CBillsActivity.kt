package com.black.c2c.activity

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
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
import com.black.base.model.C2CADData
import com.black.base.model.HttpRequestResultData
import com.black.base.model.NormalCallback
import com.black.base.model.ProTokenResult
import com.black.base.model.c2c.C2CBills
import com.black.base.model.c2c.C2CMainAD
import com.black.base.model.user.UserInfo
import com.black.base.util.ConstData
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.c2c.BR
import com.black.c2c.R
import com.black.c2c.adapter.C2CBillsAdapter
import com.black.c2c.databinding.ActivityC2cBillsBinding
import com.black.c2c.databinding.ActivitySellerChooseBinding
import com.black.c2c.databinding.ViewFirstC2cBinding
import com.black.c2c.databinding.ViewSecondC2cBinding
import com.black.c2c.fragment.C2CBillsFragment
import com.black.c2c.fragment.C2CBillsJinFragment
import com.black.c2c.fragment.C2CCustomerBuyFragment
import com.black.c2c.fragment.C2CCustomerSaleFragment
import com.black.lib.refresh.QRefreshLayout
import com.black.net.HttpCookieUtil
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.Callback
import skin.support.content.res.SkinCompatResources
import java.util.ArrayList

@Route(value = [RouterConstData.C2C_BILLS])
class C2CBillsActivity: BaseActionBarActivity(),View.OnClickListener{
    companion object {
        private val TAB_TITLES = arrayOfNulls<String>(2) //标题
    }
    private var userInfo: UserInfo? = null
    private var binding: ActivityC2cBillsBinding? = null
    private var fragmentList: ArrayList<Fragment>? = null
    private var actionType = ConstData.TAB_EXCHANGE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userInfo = CookieUtil.getUserInfo(mContext)
        if (userInfo == null) {
            finish()
            return
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_bills)
        binding?.numWan?.setOnClickListener(this)
        binding?.numJin?.setOnClickListener(this)
        TAB_TITLES[0] = getString(R.string.pending)
        TAB_TITLES[1] = getString(R.string.complet)
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
        return !super.isStatusBarDark()
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.num_jin) {
            if (ConstData.TAB_EXCHANGE != actionType) {
                actionType = ConstData.TAB_EXCHANGE
                refreshCurrentType(actionType)
                changeFragment(0)
            }
        }
        if (id == R.id.num_wan) {
            if (ConstData.TAB_WITHDRAW != actionType) {
                actionType = ConstData.TAB_WITHDRAW
                refreshCurrentType(actionType)
                changeFragment(1)
            }

        }
    }
    private fun refreshCurrentType(type: Int) {
        if (ConstData.TAB_WITHDRAW == type) {
            binding?.numWan?.isChecked = false
            binding?.numJin?.isChecked = true
        } else if (ConstData.TAB_EXCHANGE == type) {
            binding?.numWan?.isChecked = true
            binding?.numJin?.isChecked = false
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
        fragmentList?.add(C2CBillsFragment().also {
            val bundle = Bundle()
            it.arguments = bundle
        })
        fragmentList?.add(C2CBillsJinFragment().also {
            val bundle = Bundle()
            it.arguments = bundle
//            assetsWalletFragment = it
//            assetsWalletFragment?.setEventListener(this)
        })

    }
}