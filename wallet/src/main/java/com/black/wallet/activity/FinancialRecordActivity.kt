package com.black.wallet.activity

import android.os.Bundle
import android.os.Parcelable
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.black.base.activity.BaseActionBarActivity
import com.black.base.lib.filter.FilterEntity
import com.black.base.lib.filter.FilterResult
import com.black.base.lib.filter.FilterWindow
import com.black.base.model.filter.DateFilter
import com.black.base.model.filter.FinancialType
import com.black.base.model.wallet.Wallet
import com.black.base.util.ConstData
import com.black.base.util.RouterConstData
import com.black.router.annotation.Route
import com.black.wallet.R
import com.black.wallet.databinding.ActivityFinancialRecordBinding
import com.black.wallet.fragment.FinancialExtractRecordFragment
import com.black.wallet.fragment.FinancialRechargeRecordFragment
import java.util.*

//财务记录
@Route(value = [RouterConstData.FINANCIAL_RECORD])
class FinancialRecordActivity : BaseActionBarActivity(), View.OnClickListener {
    companion object {
        private val TAB_TITLES = arrayOfNulls<String>(2) //标题
    }

    private var actionType = ConstData.TAB_EXCHANGE
    private var wallet: Wallet? = null

    private var headTitleView: TextView? = null

    private var binding: ActivityFinancialRecordBinding? = null

    private var fragmentList: ArrayList<Fragment>? = null
    private var rechargeFragment: FinancialRechargeRecordFragment? = null
    private var extractFragment: FinancialExtractRecordFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallet = intent.getParcelableExtra(ConstData.WALLET)
        actionType = intent.getIntExtra(ConstData.WALLET_HANDLE_TYPE, ConstData.TAB_EXCHANGE)
        if (wallet == null || wallet!!.coinType == null) {
            return
        }
        TAB_TITLES[0] = "充币记录"
        TAB_TITLES[1] = "提币记录"
        binding = DataBindingUtil.setContentView(this, R.layout.activity_financial_record)

        binding!!.extractRecord.setOnClickListener(this)
        binding!!.rechargeRecord.setOnClickListener(this)

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

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.history_record)
    }

    override fun initToolbarViews(toolbar: Toolbar) {
        headTitleView = findViewById(R.id.action_bar_title)
        toolbar.findViewById<View>(R.id.filter_layout).visibility = View.GONE
        toolbar.findViewById<View>(R.id.filter_layout).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.filter_layout) {
            openFilterWindow()
        } else if (i == R.id.recharge_record) {
            if (ConstData.TAB_EXCHANGE != actionType) {
                actionType = ConstData.TAB_EXCHANGE
                refreshCurrentType(actionType)
                changeFragment(0)
            }
        } else if (i == R.id.extract_record) {
            if (ConstData.TAB_WITHDRAW != actionType) {
                actionType = ConstData.TAB_WITHDRAW
                refreshCurrentType(actionType)
                changeFragment(1)
            }
        }
    }

    private fun refreshCurrentType(type: Int) {
        if (ConstData.TAB_EXCHANGE == type) {
            binding!!.extractRecord.isChecked = false
            binding!!.extractRecord.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.text_size_20).toFloat())
            binding!!.rechargeRecord.isChecked = true
            binding!!.rechargeRecord.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.text_size_28).toFloat())
            headTitleView?.text = "充币记录"
        } else if (ConstData.TAB_WITHDRAW == type) {
            binding!!.extractRecord.isChecked = true
            binding!!.extractRecord.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.text_size_28).toFloat())
            binding!!.rechargeRecord.isChecked = false
            binding!!.rechargeRecord.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.text_size_20).toFloat())
            headTitleView?.text = "提币记录"
        }
    }

    private fun changeFragment(position: Int) {
        binding?.viewPager?.setCurrentItem(position, true)
    }

    private fun init() {
        if (fragmentList == null) {
            fragmentList = ArrayList()
        }
        fragmentList?.clear()
        fragmentList?.add(FinancialRechargeRecordFragment().also {
            val bundle = Bundle()
            bundle.putParcelable(ConstData.WALLET, wallet)
            it.arguments = bundle
            rechargeFragment = it
        })
        fragmentList?.add(FinancialExtractRecordFragment().also {
            val bundle = Bundle()
            bundle.putParcelable(ConstData.WALLET, wallet)
            it.arguments = bundle
            extractFragment = it
        })
    }

    private var financialType: FinancialType = FinancialType.ALL
    private var dateFilter: DateFilter = DateFilter.ALL
    private fun openFilterWindow() {
        val data: MutableList<FilterEntity<*>> = ArrayList()
        data.add(FinancialType.getDefaultFilterEntity(financialType))
        data.add(DateFilter.getDefaultFilterEntity(dateFilter))
        FilterWindow(this, data)
                .show(object : FilterWindow.OnFilterSelectListener {
                    override fun onFilterSelect(filterWindow: FilterWindow?, selectResult: List<FilterResult<*>>) {
                        for (filterResult in selectResult) {
                            if (FinancialType.KEY.equals(filterResult.key, ignoreCase = true)) {
                                financialType = filterResult.data as FinancialType
                            } else if (DateFilter.KEY.equals(filterResult.key, ignoreCase = true)) {
                                dateFilter = filterResult.data as DateFilter
                            }
                        }
                        //currentPage = 1
                        //getFinancialRecord(true)
                    }

                })
    }

}