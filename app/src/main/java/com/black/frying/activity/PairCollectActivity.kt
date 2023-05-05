package com.black.frying.activity

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Parcelable
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.black.base.activity.BaseActionBarActivity
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.frying.adapter.HomeMainRiseFallAdapter
import com.black.frying.fragment.HomePageQuotationDetailFragment
import com.black.router.annotation.Route
import com.black.util.CommonUtil
import com.black.wallet.R
import com.black.wallet.databinding.ActivitySpotBillBinding
import com.black.wallet.fragment.BillEmptyFragment
import com.black.wallet.fragment.FinancialExtractRecordFragment
import com.black.wallet.fragment.FinancialRechargeRecordFragment
import com.black.wallet.fragment.TransferBillFragment
import com.fbsex.exchange.databinding.ActivityPairCollectBinding
import com.google.android.material.tabs.TabLayout
import skin.support.content.res.SkinCompatResources

@Route(value = [RouterConstData.PAIR_COLLECT])
class PairCollectActivity: BaseActionBarActivity(), View.OnClickListener {
    companion object{
        var TAB_TITLE = ArrayList<String>(2)
        var TAB_SPOT = null
        var TAB_FUTURES = null
    }

    private var binding: ActivityPairCollectBinding? = null
    private var fragmentList: java.util.ArrayList<Fragment>? = null
    private var rechargeFragment: FinancialRechargeRecordFragment? = null
    private var extractFragment: FinancialExtractRecordFragment? = null
    private var adapter: HomeMainRiseFallAdapter? = null
    private var transferFragment: TransferBillFragment? = null
    private var totalFragment: BillEmptyFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, com.fbsex.exchange.R.layout.activity_pair_collect)
        binding?.appBarLayout?.findViewById<TextView>(R.id.text_action_bar_right)?.text = getString(R.string.baocun)
        /*val drawable = ColorDrawable()
        drawable.color = SkinCompatResources.getColor(mContext, com.fbsex.exchange.R.color.B2)
        binding!!.riseFallListView.divider = drawable
        binding!!.riseFallListView.dividerHeight = 15
        adapter = HomeMainRiseFallAdapter(mContext, 0, null)
        binding!!.riseFallListView.adapter = adapter

         */

    }

    override fun onClick(v: View) {
        when (v.id) {
        }
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.bianji_zixuan)
    }


}