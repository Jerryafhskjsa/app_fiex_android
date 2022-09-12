package com.black.base.view

import android.app.Activity
import android.graphics.drawable.PaintDrawable
import android.text.TextUtils
import android.view.*
import android.widget.*
import com.black.base.R
import com.black.base.model.AssetTransfer
import com.black.base.model.wallet.SupportAccount
import com.black.base.widget.SpanTextView
import skin.support.content.res.SkinCompatResources

//划转记录筛选弹窗
class TransferRecordFilterControllerWindow(private val activity: Activity, title: String?,
                                           fromObject: SupportAccount?,
                                           toObject: SupportAccount?,
                                           all:Boolean?,
                                           private val onReturnListener: OnReturnListener?) : View.OnClickListener{
    private val COLOR_DEFAULT: Int = SkinCompatResources.getColor(activity, R.color.T1)
    private val COLOR_SELECT: Int = SkinCompatResources.getColor(activity, R.color.C1)
    private val COLOR_BG: Int = SkinCompatResources.getColor(activity, R.color.B2)
    private var density: Float
    private val popupWindow: PopupWindow
    private val titleView: TextView
    private val selectedImg:ImageView
    private var mAll = all
    private val mFrom:SupportAccount? = fromObject
    private val mTo:SupportAccount? = toObject

    private var fromTextView:SpanTextView? = null
    private var toTextView:SpanTextView? = null
    private var selectedAll:ImageView? = null
    private var linALl:RelativeLayout? = null


    init {
        val dm = activity.resources.displayMetrics
        density = dm.density
        val contentView = LayoutInflater.from(activity).inflate(R.layout.view_transfer_record_wallet_chooser, null)
        popupWindow = PopupWindow(contentView,
                dm.widthPixels,
                WindowManager.LayoutParams.WRAP_CONTENT)
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(PaintDrawable())
        popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED
        popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        popupWindow.setOnDismissListener {
            val lp = activity.window.attributes
            lp.alpha = 1f
            activity.window.attributes = lp
        }
        titleView = contentView.findViewById(R.id.title)
        if (TextUtils.isEmpty(title)) {
            titleView.visibility = View.GONE
        } else {
            titleView.visibility = View.VISIBLE
            titleView.text = title
        }
        selectedImg = contentView.findViewById(R.id.img_all)
        updateSelectedAll(mAll)
        fromTextView = contentView.findViewById(R.id.tv_from_account)
        toTextView = contentView.findViewById(R.id.tv_to_account)
        selectedAll = contentView.findViewById(R.id.img_all)
        linALl = contentView.findViewById(R.id.lin_all)
        linALl?.setOnClickListener(this)
        contentView.findViewById<View>(R.id.btn_cancel).setOnClickListener(this)
        contentView.findViewById<View>(R.id.rel_from).setOnClickListener(this)
        contentView.findViewById<View>(R.id.rel_to).setOnClickListener(this)
        contentView.findViewById<View>(R.id.btn_confirm).setOnClickListener(this)
    }

    private fun updateSelectedAll(all: Boolean?){
        if(all!!){
            selectedImg.visibility = View.VISIBLE
        }else{
            selectedImg.visibility = View.GONE
        }
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.btn_cancel -> dismiss()
            R.id.rel_from ->{
                onReturnListener?.onWalletTypeChoose(this,mFrom,1)
            }
            R.id.rel_to ->{
                onReturnListener?.onWalletTypeChoose(this,mTo,2)
            }
            R.id.btn_confirm ->{
                onReturnListener?.onConfirm(this,mAll,mFrom,mTo)
            }
            R.id.lin_all ->{
                mAll = !mAll!!
                updateSelectedAll(mAll)
                onReturnListener?.onSelectedAll(this,mAll)
            }
        }

    }

    fun show() {
        val lp = activity.window.attributes
        lp.alpha = 0.6f
        activity.window.attributes = lp
        popupWindow.showAtLocation(activity.window.decorView, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 0)
    }

    fun dismiss() {
        popupWindow.dismiss()
    }

    fun getFromView():SpanTextView?{
        return fromTextView

    }
    fun getToView():SpanTextView?{
        return toTextView
    }

    interface OnReturnListener{
        fun onConfirm(
            dialog: TransferRecordFilterControllerWindow, all:Boolean?, from: SupportAccount?,
            to: SupportAccount?
        )

        /**
         * 1 from
         * 2 to
         */
        fun  onWalletTypeChoose(dialog: TransferRecordFilterControllerWindow, item: SupportAccount?,type:Int?)
        fun onSelectedAll(dialog: TransferRecordFilterControllerWindow,selected:Boolean?)
    }
}