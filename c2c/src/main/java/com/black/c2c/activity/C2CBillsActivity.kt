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
import com.black.base.util.ConstData
import com.black.base.util.RouterConstData
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cBillsBinding
import com.black.c2c.databinding.ActivitySellerChooseBinding
import com.black.c2c.databinding.ViewFirstC2cBinding
import com.black.c2c.databinding.ViewSecondC2cBinding
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import skin.support.content.res.SkinCompatResources
import java.util.ArrayList

@Route(value = [RouterConstData.C2C_BILLS])
class C2CBillsActivity: BaseActionBarActivity(), View.OnClickListener{
    companion object {
        private val TAB_TITLES = arrayOfNulls<String>(4) //标题
    }
    private var binding: ActivityC2cBillsBinding? = null
    private var fragmentList: ArrayList<Fragment>? = null

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
        //adapter = Adapter(mContext, BR.listItemWalletBillModel, null)
        //adapter?.setOnItemClickListener(this)
       // binding?.recyclerView?.adapter = adapter
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
}