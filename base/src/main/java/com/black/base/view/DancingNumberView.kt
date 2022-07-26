package com.black.base.view

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.black.base.R
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import skin.support.widget.SkinCompatBackgroundHelper
import skin.support.widget.SkinCompatSupportable
import skin.support.widget.SkinCompatTextHelper
import java.util.*
import java.util.regex.Pattern

class DancingNumberView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : TextView(context, attrs, defStyleAttr), SkinCompatSupportable {
    /**
     * 获取跳动持续时长
     *
     * @return 持续时长 单位ms
     */
    /**
     * 设置跳动持续时长
     *
     * @param duration 持续时长 单位ms
     */
    /**
     * 从数字开始跳动到结束跳动显示原值的持续时间,单位是ms
     */
    var duration: Int
    /**
     * 获取数字显示的格式
     *
     * @return 数字显示的格式
     */
    /**
     * 获取数字显示的格式
     *
     * @param format 数字显示的格式
     */
    /**
     * 跳动时数字显示的格式
     */
    var format = "%.0f"
    /**
     * 获取算数因子
     *
     * @return 算数因子
     *///            if (formatCommand != null) {
//                valueFormat = formatCommand.format(numberTemp[i]);
//            } else {
//                valueFormat = String.format(format, numberTemp[i]);
//            }
    /**
     * 设置算数因子,为ObjectAnimator调用
     *
     * @param factor 算数因子
     * @see ObjectAnimator
     */
    /**
     * 算数因子
     */
    private var factor = 0f

    /**
     * 文本中数字原值
     */
//    private ArrayList<Double> numbers;
    /**
     * 保存跳动数字的数组
     */
//    private double[] numberTemp;
    private var formatNumbers: Array<FormatNumber>? = null
    /**
     * 文本原值
     */
    private var text: String? = null
    /**
     * 文本去除数字的样式
     */
    private var textPattern: String? = null
    private val mTextHelper: SkinCompatTextHelper?
    private val mBackgroundTintHelper: SkinCompatBackgroundHelper?

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.DancingNumberView)
        duration = ta.getInteger(R.styleable.DancingNumberView_dnv_duration, 1500)
        if (ta.hasValue(R.styleable.DancingNumberView_dnv_format)) {
            format = ta.getString(R.styleable.DancingNumberView_dnv_format)
        }
        mBackgroundTintHelper = SkinCompatBackgroundHelper(this)
        mBackgroundTintHelper.loadFromAttributes(attrs, defStyleAttr)
        mTextHelper = SkinCompatTextHelper.create(this)
        mTextHelper.loadFromAttributes(attrs, defStyleAttr)
    }

    override fun setBackgroundResource(@DrawableRes resId: Int) {
        super.setBackgroundResource(resId)
        mBackgroundTintHelper?.onSetBackgroundResource(resId)
    }

    override fun setTextAppearance(resId: Int) {
        setTextAppearance(context, resId)
    }

    override fun setTextAppearance(context: Context, resId: Int) {
        super.setTextAppearance(context, resId)
        mTextHelper?.onSetTextAppearance(context, resId)
    }

    override fun setCompoundDrawablesRelativeWithIntrinsicBounds(
            @DrawableRes start: Int, @DrawableRes top: Int, @DrawableRes end: Int, @DrawableRes bottom: Int) {
        super.setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom)
        mTextHelper?.onSetCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom)
    }

    override fun setCompoundDrawablesWithIntrinsicBounds(
            @DrawableRes left: Int, @DrawableRes top: Int, @DrawableRes right: Int, @DrawableRes bottom: Int) {
        super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom)
        mTextHelper?.onSetCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom)
    }

    override fun applySkin() {
        mBackgroundTintHelper?.applySkin()
        mTextHelper?.applySkin()
    }


    /**
     * 获取算数因子
     *
     * @return 算数因子
     */
    fun getFactor(): Float {
        return factor
    }

    /**
     * 设置算数因子,为ObjectAnimator调用
     *
     * @param factor 算数因子
     * @see ObjectAnimator
     */
    fun setFactor(factor: Float) {
        var textNow = textPattern!!
        this.factor = factor
        for (i in formatNumbers!!.indices) {
            val formatNumber = formatNumbers!![i]!!
            val value = formatNumber.value!! * factor
            val valueFormat = NumberUtil.formatNumberDynamicScaleNoGroup(value, formatNumber.length, formatNumber.dotLength, formatNumber.dotLength)
            //            if (formatCommand != null) {
//                valueFormat = formatCommand.format(numberTemp[i]);
//            } else {
//                valueFormat = String.format(format, numberTemp[i]);
//            }
            textNow = textNow.replaceFirst(PLACEHOLDER.toRegex(), valueFormat)
        }
        setText(textNow)
    }

    /**
     * 文本中的数字开始跳动
     */
    fun dance() {
        text = getText().toString()
        val numbers = ArrayList<Double>()
        val pattern = Pattern.compile("\\d+(\\.\\d+)?")
        val matcher = pattern.matcher(text)
        while (matcher.find()) {
            numbers.add(matcher.group().toDouble())
        }
        textPattern = text?.replace("\\d+(\\.\\d+)?".toRegex(), PLACEHOLDER)
        //        numberTemp = new double[numbers.size()];
        formatNumbers = Array(numbers.size) {
            FormatNumber()
        }
        for (i in formatNumbers!!.indices) {
            val number = FormatNumber()
            number.value = numbers[i]
            number.length = CommonUtil.getNumberStringLength(number.value)
            number.dotLength = CommonUtil.getDotLength(number.value)
            formatNumbers!![i] = number
        }
        val objectAnimator = ObjectAnimator.ofFloat(this, "factor", 0f, 1f)
        objectAnimator.duration = duration.toLong()
        objectAnimator.interpolator = AccelerateDecelerateInterpolator()
        objectAnimator.start()
    }

    private var formatCommand: FormatCommand? = null
    fun setFormatCommand(formatCommand: FormatCommand?) {
        this.formatCommand = formatCommand
    }

    internal inner class FormatNumber {
        var value: Double? = null
        var length = 0
        var dotLength = 0
    }

    interface FormatCommand {
        fun format(value: Double): String?
    }

    companion object {
        const val PLACEHOLDER = "@@@"
    }
}