package com.black.frying.view

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.adapter.interfaces.OnItemClickListener
import com.black.base.model.socket.PairStatus
import com.black.frying.adapter.PairChooseAdapter
import com.black.util.CommonUtil
import com.fbsex.exchange.BR
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ViewChooseCoinTypeBinding
import skin.support.content.res.SkinCompatResources

class PairChoosePopup(private val activity: Activity, pairs: List<PairStatus?>?, currentPair: String?) : PopupWindow.OnDismissListener, OnItemClickListener {
    private val popupWindow: PopupWindow?
    private val dm: DisplayMetrics
    private val margin: Float
    private val adapter: PairChooseAdapter
    private var onPairChooseListener: OnPairChooseListener? = null

    private var binding: ViewChooseCoinTypeBinding? = null

    init {
        val inflater = LayoutInflater.from(activity)
        binding = DataBindingUtil.inflate(inflater, R.layout.view_choose_coin_type, null, false)
        dm = activity.resources.displayMetrics
        margin = 5 * dm.density
        popupWindow = PopupWindow(binding?.root, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        popupWindow.isFocusable = true
        val drawable = ColorDrawable(SkinCompatResources.getColor(activity, R.color.transparent))
        popupWindow.setBackgroundDrawable(drawable)
        popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        popupWindow.setOnDismissListener(this)
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        adapter = PairChooseAdapter(activity, BR.listItemChoosePairModel, currentPair, pairs)
        adapter.setOnItemClickListener(this)
        binding?.recyclerView?.adapter = adapter
    }

    override fun onDismiss() {
        val lp = activity.window.attributes
        lp.alpha = 1f
        activity.window.attributes = lp
    }

    private val isShowing: Boolean
        get() = popupWindow != null && popupWindow.isShowing

    private fun dismiss() {
        if (isShowing) {
            popupWindow!!.dismiss()
        }
    }

    fun show(view: View) {
        val lp = activity.window.attributes
        lp.alpha = 0.6f
        activity.window.attributes = lp
        CommonUtil.measureView(binding?.root)
        val offsetX = view.width - (binding?.root?.measuredWidth ?: 0)
        popupWindow!!.showAsDropDown(view, offsetX, 0, Gravity.LEFT)
    }

    override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
        if (onPairChooseListener != null) {
            onPairChooseListener!!.onPairChoose(adapter.getItem(position))
        }
        dismiss()
    }

    fun setOnPairChooseListener(onPairChooseListener: OnPairChooseListener?): PairChoosePopup {
        this.onPairChooseListener = onPairChooseListener
        return this
    }

    interface OnPairChooseListener {
        fun onPairChoose(pairStatus: PairStatus?)
    }

}