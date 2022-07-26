package com.black.wallet.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActionBarActivity
import com.black.base.adapter.interfaces.OnItemClickListener
import com.black.base.util.ConstData
import com.black.base.util.RouterConstData
import com.black.base.util.RxJavaHelper
import com.black.base.util.SocketDataContainer
import com.black.lib.view.SwipeItemLayout
import com.black.router.annotation.Route
import com.black.wallet.BR
import com.black.wallet.R
import com.black.wallet.adapter.LeverPairAdapter
import com.black.wallet.databinding.ActivityLeverPairChooseBinding
import io.reactivex.Observable
import skin.support.content.res.SkinCompatResources

@Route(value = [RouterConstData.LEVER_PAIR_CHOOSE])
class LeverPairChooseActivity : BaseActionBarActivity(), OnItemClickListener {
    private var binding: ActivityLeverPairChooseBinding? = null
    private var adapter: LeverPairAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_lever_pair_choose)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(this, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.6 * 255).toInt()
        decoration.setDrawable(drawable)
        binding?.recyclerView?.addItemDecoration(decoration)
        binding?.recyclerView?.addOnItemTouchListener(SwipeItemLayout.OnSwipeItemTouchListener(this))
        adapter = LeverPairAdapter(this, BR.listItemLeverPairModel, null)
        adapter?.setOnItemClickListener(this)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.setEmptyView(binding?.emptyView?.root)
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.isFocusable = false

        getLeverPair()
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return "选择账户"
    }

    override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
        val pair = adapter?.getItem(position)
        pair?.run {
            val intent = Intent()
            intent.putExtra(ConstData.PAIR, pair)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun getLeverPair() {
        Observable.just(SocketDataContainer.getAllLeverPair(this) ?: ArrayList())
                .compose(RxJavaHelper.observeOnMainThread())
                .subscribe {
                    adapter?.data = it
                    adapter?.notifyDataSetChanged()
                }
                .run { }
    }
}