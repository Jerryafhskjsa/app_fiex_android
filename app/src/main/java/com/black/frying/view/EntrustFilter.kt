package com.black.frying.view

import android.app.Activity
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.adapter.interfaces.OnItemClickListener
import com.black.base.api.PairApiServiceHelper
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.QuotationSet
import com.black.base.model.filter.EntrustType
import com.black.base.util.SocketDataContainer
import com.black.base.util.SpacesItemDecoration
import com.black.frying.adapter.EntrustFilterSetAdapter
import com.black.frying.model.EntrustFilterSet
import com.black.frying.viewmodel.TransactionViewModel
import com.black.net.HttpRequestResult
import com.black.util.Callback
import com.black.util.CommonUtil
import com.fbsex.exchange.BR
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ViewEntrustFilterBinding
import java.util.*
import kotlin.collections.ArrayList

class EntrustFilter(private val activity: Activity, private val parentView: View, defaultCoinType: String?, private val defaultSet: String?,
                    private val leverType: String?, defaultEntrustType: EntrustType?) : PopupWindow.OnDismissListener, View.OnClickListener {
    private var binding: ViewEntrustFilterBinding? = null

    private val adapter: EntrustFilterSetAdapter
    private var onEntrustFilterListener: OnEntrustFilterListener? = null

    init {
        val inflater = LayoutInflater.from(activity)
        binding = DataBindingUtil.inflate(inflater, R.layout.view_entrust_filter, null, false)
        binding?.root?.visibility = View.GONE
        val position = IntArray(2)
        parentView.getLocationOnScreen(position)
        val dm = activity.resources.displayMetrics
        val height = dm.heightPixels - position[1].toFloat()
        val winContent = activity.findViewById<FrameLayout>(android.R.id.content)
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height.toInt())
        params.topMargin = position[1] + parentView.height
        winContent.addView(binding?.root, params)
        binding?.coinType?.setText(defaultCoinType ?: "")
        binding?.setName?.text = defaultSet ?: ""
        binding?.setName?.setOnClickListener(this)
        binding?.all?.tag = EntrustType.ALL
        binding?.all?.setOnClickListener(this)
        binding?.coin?.tag = EntrustType.COIN
        binding?.coin?.setOnClickListener(this)
        binding?.lever?.tag = EntrustType.LEVER
        binding?.lever?.setOnClickListener(this)
        binding?.blank?.setOnClickListener(this)
        refreshEntrustTypeViews(defaultEntrustType)
        binding?.btnReset?.setOnClickListener(this)
        binding?.btnConfirm?.setOnClickListener(this)
        val gridLayoutManager = GridLayoutManager(activity, 3)
        gridLayoutManager.orientation = RecyclerView.VERTICAL
        gridLayoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = gridLayoutManager
        val decoration = SpacesItemDecoration((dm.density * 10).toInt())
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter = EntrustFilterSetAdapter(activity, BR.listItemEntrustFilterSetModel, null)
        adapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
                adapter.check(position)
                binding?.setName?.text = adapter.getItem(position)?.set
                hideSetLayout()
            }

        })
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.isFocusable = false
        initSetData()
        hideSetLayout()
    }

    val isShowing: Boolean
        get() = binding?.root?.visibility == View.VISIBLE

    fun dismiss() {
        if (isShowing) {
            val animation = AnimationUtils.loadAnimation(activity, R.anim.anim_top_out)
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    binding?.root?.visibility = View.GONE
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            binding?.workSpace?.startAnimation(animation)
            //            contentView.setVisibility(View.GONE);
        }
    }

    fun show() {
        val position = IntArray(2)
        parentView.getLocationOnScreen(position)
        //        popupWindow.showAtLocation(parentView, Gravity.NO_GRAVITY, 0,
//                position[1] + parentView.getHeight());
        binding?.root?.visibility = View.VISIBLE
        val animation = AnimationUtils.loadAnimation(activity, R.anim.anim_top_in)
        binding?.workSpace?.startAnimation(animation)
        //        popupWindow.showAtLocation(parentView, Gravity.TOP, 0, 0);
//        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
//        lp.alpha = 0.6f;
//        activity.getWindow().setAttributes(lp);
    }

    override fun onDismiss() { //        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
//        lp.alpha = 1f;
//        activity.getWindow().setAttributes(lp);
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.set_name -> if (binding?.recyclerView?.visibility == View.VISIBLE) {
                binding?.setName?.isChecked = false
                hideSetLayout()
            } else {
                binding?.setName?.isChecked = true
                displaySetLayout()
            }
            R.id.all -> refreshEntrustTypeViews(EntrustType.ALL)
            R.id.coin -> refreshEntrustTypeViews(EntrustType.COIN)
            R.id.lever -> refreshEntrustTypeViews(EntrustType.LEVER)
            R.id.btn_reset -> {
                binding?.coinType?.setText("")
                binding?.setName?.text = ""
                adapter.check(-1)
                refreshEntrustTypeViews(null)
            }
            R.id.btn_confirm -> if (onEntrustFilterListener != null) {
                var entrustType = EntrustType.ALL
                if (true == binding?.coin?.isChecked) {
                    entrustType = EntrustType.COIN
                } else if (true == binding?.lever?.isChecked) {
                    entrustType = EntrustType.LEVER
                }
                onEntrustFilterListener!!.onSelected(this, binding?.coinType?.text.toString(), binding?.setName?.text.toString(), entrustType)
            }
            R.id.blank -> dismiss()
        }
    }

    private fun refreshEntrustTypeViews(entrustType: EntrustType?) {
        binding?.all?.isChecked = CommonUtil.equals(entrustType, EntrustType.ALL)
        binding?.coin?.isChecked = CommonUtil.equals(entrustType, EntrustType.COIN)
        binding?.lever?.isChecked = CommonUtil.equals(entrustType, EntrustType.LEVER)
    }

    fun setOnEntrustFilterListener(onEntrustFilterListener: OnEntrustFilterListener?) {
        this.onEntrustFilterListener = onEntrustFilterListener
    }

    private fun initSetData() {
        if (TransactionViewModel.LEVER_TYPE_LEVER == leverType) {
            val leverPairs = SocketDataContainer.getAllLeverPair(activity)
            var sets: ArrayList<String?>? = null
            if (leverPairs != null && leverPairs.isNotEmpty()) {
                sets = ArrayList()
                for (i in leverPairs.indices) {
                    val pair = leverPairs[i]
                    val set = getPairSetName(pair)
                    if (set != null && !sets.contains(set)) {
                        sets.add(set)
                    }
                }
            }
            showSetData(sets)
        } else {
            PairApiServiceHelper.getTradeSets(activity, false, object : Callback<HttpRequestResultDataList<QuotationSet?>?>() {
                override fun error(type: Int, error: Any) {
                    showSetData(null)
                }

                override fun callback(returnData: HttpRequestResultDataList<QuotationSet?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        var setS:ArrayList<String> = ArrayList()
                        var dataSet = returnData.data
                        for (i in dataSet?.indices!!){
                            dataSet[i]?.name?.let { setS.add(it) }
                        }
                        showSetData(setS)
                    } else {
                        showSetData(null)
                    }
                }
            })
        }
    }

    private fun showSetData(setList: List<String?>?) {
        val dataList = ArrayList<EntrustFilterSet?>()
        var checkedIndex = -1
        if (setList != null) {
            for (i in setList.indices) {
                val set = EntrustFilterSet()
                set.set = setList[i]
                if (defaultSet != null && TextUtils.equals(set.set, defaultSet)) {
                    checkedIndex = i
                }
                dataList.add(set)
            }
        }
        adapter.data = (dataList)
        if (checkedIndex != -1) {
            adapter.check(checkedIndex)
        }
        adapter.notifyDataSetChanged()
    }

    private fun hideSetLayout() {
        binding?.recyclerView?.visibility = View.GONE
    }

    private fun displaySetLayout() {
        binding?.recyclerView?.visibility = View.VISIBLE
    }

    interface OnEntrustFilterListener {
        fun onSelected(entrustFilter: EntrustFilter, coinType: String?, set: String?, entrustType: EntrustType?)
    }

    companion object {
        private fun getPairSetName(pair: String?): String? {
            var set: String? = null
            if (pair != null) {
                val arr = pair.split("_").toTypedArray()
                if (arr.size > 1) {
                    set = arr[1]
                }
            }
            return set
        }
    }
}