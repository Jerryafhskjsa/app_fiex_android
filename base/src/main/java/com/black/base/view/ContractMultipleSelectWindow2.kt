package com.black.base.view

import android.app.Activity
import android.os.Build
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import com.black.base.R
import com.black.base.api.FutureApiServiceHelper
import com.black.base.model.ContractMultiChooseBean
import com.black.base.model.FryingLinesConfig
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.socket.Deep
import com.black.base.util.FryingUtil
import com.black.base.widget.SpanCheckedTextView
import com.black.base.widget.SpanMaterialEditText
import com.black.base.widget.SpanTextView
import com.black.util.Callback
import com.black.util.NumberUtil
import skin.support.content.res.SkinCompatResources
import skin.support.widget.SkinCompatCheckBox
import skin.support.widget.SkinCompatSeekBar
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

//合约方向和倍数选择弹窗
@RequiresApi(Build.VERSION_CODES.N)
class ContractMultipleSelectWindow2(
    private val activity: Activity,
    title: String?,
    private var beishu: Int,
    private val onReturnListener: OnReturnListener
) : View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private val COLOR_DEFAULT: Int = SkinCompatResources.getColor(activity, R.color.T1)
    private val COLOR_SELECT: Int = SkinCompatResources.getColor(activity, R.color.T13)
    private val COLOR_BG: Int = SkinCompatResources.getColor(activity, R.color.B2)
    private var density: Float
    private val popupWindow: PopupWindow
    private val contentView: View
    private val titleView: TextView
    private val btnFiexible: SpanCheckedTextView
    private val btnAll: SpanCheckedTextView
    private val btnCancel: SpanTextView
    private val btnCommit: SpanTextView
    private val tvDirection: SpanTextView
    private var btnMulitSub: ImageView
    private var btnMultiAdd: ImageView
    private val editMulti: SpanMaterialEditText
    private val countBar: SkinCompatSeekBar
    private val progressBar: ProgressBar
    private val zeroView: SkinCompatCheckBox
    private val twentyView: SkinCompatCheckBox
    private val fourtyView: SkinCompatCheckBox
    private val sixtyView: SkinCompatCheckBox
    private val eightyView: SkinCompatCheckBox
    private val allView: SkinCompatCheckBox

    init {
        val dm = activity.resources.displayMetrics
        density = dm.density
        contentView =
            LayoutInflater.from(activity).inflate(R.layout.view_contract_mulitple_chooser, null)
        btnFiexible = contentView.findViewById(R.id.btn_fiexible)
        btnAll = contentView.findViewById(R.id.btn_all)
        btnCancel = contentView.findViewById(R.id.btn_cancel)
        btnCommit = contentView.findViewById(R.id.btn_commit)
        tvDirection = contentView.findViewById(R.id.direction)
        editMulti = contentView.findViewById(R.id.editMultiple)
        editMulti?.setText(beishu.toString())
        editMulti.addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if(s.isNotEmpty()){
                    beishu = s.toString().toInt()
                }
            }

            override fun afterTextChanged(s: Editable) {
                if(s.isNotEmpty()){
                    editMulti.setSelection(s.toString().length)
                }
            }
        })
        btnMulitSub = contentView.findViewById(R.id.mulit_sub)
        btnMultiAdd = contentView.findViewById(R.id.multi_add)
        countBar = contentView.findViewById(R.id.count_bar)
        countBar.max = 100
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            countBar.min = 1
        }
        countBar?.setProgress(beishu,true)
        countBar.setOnSeekBarChangeListener(this)
        progressBar = contentView.findViewById(R.id.count_progress)
        zeroView = contentView.findViewById(R.id.amount_zero)
        twentyView = contentView.findViewById(R.id.amount_twenty)
        fourtyView = contentView.findViewById(R.id.amount_fourty)
        sixtyView = contentView.findViewById(R.id.amount_sixty)
        eightyView = contentView.findViewById(R.id.amount_eighty)
        allView = contentView.findViewById(R.id.amount_all)

        popupWindow = PopupWindow(
            contentView,
            dm.widthPixels,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        popupWindow.isFocusable = true
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

        btnFiexible.setOnClickListener(this)
        btnAll.setOnClickListener(this)
        btnCancel.setOnClickListener(this)
        btnCommit.setOnClickListener(this)
        btnMulitSub.setOnClickListener(this)
        btnMultiAdd.setOnClickListener(this)
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        progressBar.progress = progress
        editMulti.setText(progress.toString())
        onCountProgressClick(progress * 5 / 100)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}


    private fun onCountProgressClick(type: Int) {
        when (type) {
            0 -> {
                zeroView.isChecked = true
                twentyView.isChecked = false
                fourtyView.isChecked = false
                sixtyView.isChecked = false
                eightyView.isChecked = false
                allView.isChecked = false
            }
            1 -> {
                zeroView.isChecked = true
                twentyView.isChecked = true
                fourtyView.isChecked = false
                sixtyView.isChecked = false
                eightyView.isChecked = false
                allView.isChecked = false
            }
            2 -> {
                zeroView.isChecked = true
                twentyView.isChecked = true
                fourtyView.isChecked = true
                sixtyView.isChecked = false
                eightyView.isChecked = false
                allView.isChecked = false
            }
            3 -> {
                zeroView.isChecked = true
                twentyView.isChecked = true
                fourtyView.isChecked = true
                sixtyView.isChecked = true
                eightyView.isChecked = false
                allView.isChecked = false
            }
            4 -> {
                zeroView.isChecked = true
                twentyView.isChecked = true
                fourtyView.isChecked = true
                sixtyView.isChecked = true
                eightyView.isChecked = true
                allView.isChecked = false
            }
            5 -> {
                zeroView.isChecked = true
                twentyView.isChecked = true
                fourtyView.isChecked = true
                sixtyView.isChecked = true
                eightyView.isChecked = true
                allView.isChecked = true
            }
        }
    }



    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_cancel -> dismiss()
            R.id.btn_commit -> {
                onReturnListener.onReturn(beishu)
                dismiss()
            }
            R.id.btn_fiexible -> {
                btnFiexible.isChecked = true
                btnAll.isChecked = false
            }
            R.id.btn_all -> {
                btnFiexible.isChecked = false
                btnAll.isChecked = true
            }
            R.id.mulit_sub -> {
                val text = editMulti.text.toString()
                var count = 0
                if(text.isNotEmpty()){
                    count = text.toInt()
                    count -= 1
                    if(count < 0){
                        return
                    }
                }
                editMulti.setText(count.toString())
            }
            R.id.multi_add -> {
                val text = editMulti.text.toString()
                var count = 0
                if(text.isNotEmpty()){
                    count = text.toInt()
                    count += 1
                    if(count > 100){
                        return
                    }
                }
                editMulti.setText(count.toString())
            }
        }
    }

    fun show() {
        val lp = activity.window.attributes
        lp.alpha = 0.6f
        activity.window.attributes = lp
        popupWindow.showAtLocation(
            activity.window.decorView,
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL,
            0,
            0
        )
    }

    fun dismiss() {
        popupWindow.dismiss()
    }

    interface OnReturnListener{
        fun onReturn(beishu: Int)
    }
}