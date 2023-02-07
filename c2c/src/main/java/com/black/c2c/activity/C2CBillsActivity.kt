package com.black.c2c.activity

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.C2CApiServiceHelper
import com.black.base.model.C2CADData
import com.black.base.model.HttpRequestResultData
import com.black.base.model.NormalCallback
import com.black.base.model.c2c.C2CBills
import com.black.base.model.c2c.C2CMainAD
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.c2c.BR
import com.black.c2c.R
import com.black.c2c.adapter.C2CBillsAdapter
import com.black.c2c.databinding.ActivityC2cBillsBinding
import com.black.c2c.databinding.ActivitySellerChooseBinding
import com.black.c2c.databinding.ViewFirstC2cBinding
import com.black.c2c.databinding.ViewSecondC2cBinding
import com.black.lib.refresh.QRefreshLayout
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import skin.support.content.res.SkinCompatResources
import java.util.ArrayList

@Route(value = [RouterConstData.C2C_BILLS])
class C2CBillsActivity: BaseActionBarActivity(),   QRefreshLayout.OnRefreshListener,
    QRefreshLayout.OnLoadListener, QRefreshLayout.OnLoadMoreCheckListener,View.OnClickListener{
    companion object {
        private val TAB_TITLES = arrayOfNulls<String>(4) //标题
    }
    private var binding: ActivityC2cBillsBinding? = null
    private var fragmentList: ArrayList<Fragment>? = null
    private var adapter: C2CBillsAdapter? = null
    private var currentPage = 1
    private var total = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_bills)
        binding?.numOne?.setOnClickListener(this)
        binding?.numTwo?.setOnClickListener(this)
        binding?.numThree?.setOnClickListener(this)
        binding?.numFour?.setOnClickListener(this)
        binding?.numWan?.setOnClickListener(this)
        binding?.numJin?.setOnClickListener(this)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(this, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.6 * 255).toInt()
        decoration.setDrawable(drawable)
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter = C2CBillsAdapter(mContext, BR.listItemC2COrderListModel, null)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setEmptyView(binding?.emptyView?.root)
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.isFocusable = false
        binding?.recyclerView?.layoutManager = layoutManager
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.num_one){
            binding?.barA?.visibility = View.VISIBLE
            binding?.barB?.visibility = View.GONE
            binding?.barC?.visibility = View.GONE
            binding?.barD?.visibility = View.GONE
            binding?.numOne?.isChecked = true
            binding?.numTwo?.isChecked = false
            binding?.numThree?.isChecked = false
            binding?.numFour?.isChecked = false
        }
        else if (id == R.id.num_two){
            binding?.barB?.visibility = View.VISIBLE
            binding?.barA?.visibility = View.GONE
            binding?.barC?.visibility = View.GONE
            binding?.barD?.visibility = View.GONE
            binding?.numTwo?.isChecked = true
            binding?.numOne?.isChecked = false
            binding?.numThree?.isChecked = false
            binding?.numFour?.isChecked = false
        }
        else if (id == R.id.num_three){
            binding?.barC?.visibility = View.VISIBLE
            binding?.barB?.visibility = View.GONE
            binding?.barA?.visibility = View.GONE
            binding?.barD?.visibility = View.GONE
            binding?.numThree?.isChecked = true
            binding?.numTwo?.isChecked = false
            binding?.numOne?.isChecked = false
            binding?.numFour?.isChecked = false
        }
        if (id == R.id.num_four){
            binding?.barD?.visibility = View.VISIBLE
            binding?.barB?.visibility = View.GONE
            binding?.barC?.visibility = View.GONE
            binding?.barA?.visibility = View.GONE
            binding?.numFour?.isChecked = true
            binding?.numTwo?.isChecked = false
            binding?.numThree?.isChecked = false
            binding?.numOne?.isChecked = false
        }
        if (id == R.id.num_wan){
            binding?.numJin?.isChecked = true
            binding?.numWan?.isChecked = false
        }
        if (id == R.id.num_jin){
            binding?.numJin?.isChecked = false
            binding?.numWan?.isChecked = true
        }
    }
    override fun onLoadMoreCheck(): Boolean {
        return total > (adapter?.count ?: 0)
    }

    override fun onRefresh() {
        currentPage = 1
        getC2CADData(false)
    }

    override fun onLoad() {
        if (total > (adapter?.count ?: 0)) {
            currentPage += 1
            getC2CADData(true)
        }
        else{
            binding?.refreshLayout?.setLoading(false)
        }
    }

    private fun onRefreshEnd() {
        binding?.refreshLayout?.setRefreshing(false)
        binding?.refreshLayout?.setLoading(false)
    }


    private fun getC2CADData(isShowLoading: Boolean) {
        C2CApiServiceHelper.getC2COL(mContext, isShowLoading,null,null,  object : NormalCallback<HttpRequestResultData<C2CADData<C2CBills?>?>?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                onRefreshEnd()
                showData(null)
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultData<C2CADData<C2CBills?>?>?) {
                onRefreshEnd()
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    total = returnData.data?.total ?: 0
                    var dataList = returnData.data?.data
                    showData(dataList)
                } else {
                    showData(null)
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }
    private fun showData(dataList: ArrayList<C2CBills?>?) {
        onRefreshEnd()
        if (currentPage == 1) {
            adapter?.data = dataList
        } else {
            adapter?.addAll(dataList)
        }
        adapter?.notifyDataSetChanged()
    }
}