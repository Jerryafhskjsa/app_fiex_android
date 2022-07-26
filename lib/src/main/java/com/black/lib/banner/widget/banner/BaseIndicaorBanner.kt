package com.black.lib.banner.widget.banner

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.Interpolator
import android.widget.ImageView
import android.widget.LinearLayout
import com.black.lib.R
import com.black.lib.banner.anim.BaseAnimator
import com.black.util.CommonUtil
import skin.support.content.res.SkinCompatResources
import java.util.*
import kotlin.math.abs

abstract class BaseIndicaorBanner<E, T : BaseIndicaorBanner<E, T>?> @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : BaseBanner<E, T>(context, attrs, defStyle) {
    companion object {
        const val STYLE_DRAWABLE_RESOURCE = 0
        const val STYLE_CORNER_RECTANGLE = 1
    }

    private val indicatorViews = ArrayList<ImageView>()
    private var indicatorStyle: Int
    private var indicatorWidth: Int
    private var indicatorHeight: Int
    private var indicatorGap: Int
    private var indicatorCornerRadius: Int
    private var selectDrawable: Drawable? = null
    private var unSelectDrawable: Drawable? = null
    private var selectColor: Int
    private var unselectColor: Int
    private var selectAnimClass: Class<out BaseAnimator?>? = null
    private var unselectAnimClass: Class<out BaseAnimator?>? = null
    private val ll_indicators: LinearLayout

    init {
        val ta = context.obtainStyledAttributes(attrs,
                R.styleable.BaseIndicaorBanner)
        indicatorStyle = ta.getInt(
                R.styleable.BaseIndicaorBanner_bib_indicatorStyle,
                STYLE_CORNER_RECTANGLE)
        indicatorWidth = ta.getDimensionPixelSize(
                R.styleable.BaseIndicaorBanner_bib_indicatorWidth, dp2px(6f))
        indicatorHeight = ta.getDimensionPixelSize(
                R.styleable.BaseIndicaorBanner_bib_indicatorHeight, dp2px(6f))
        indicatorGap = ta.getDimensionPixelSize(
                R.styleable.BaseIndicaorBanner_bib_indicatorGap, dp2px(6f))
        indicatorCornerRadius = ta.getDimensionPixelSize(
                R.styleable.BaseIndicaorBanner_bib_indicatorCornerRadius,
                dp2px(3f))
        selectColor = ta.getColor(
                R.styleable.BaseIndicaorBanner_bib_indicatorSelectColor,
                Color.parseColor("#ffffff"))
        unselectColor = ta.getColor(
                R.styleable.BaseIndicaorBanner_bib_indicatorUnselectColor,
                Color.parseColor("#88ffffff"))
        val selectRes = ta.getResourceId(
                R.styleable.BaseIndicaorBanner_bib_indicatorSelectRes, 0)
        val unselectRes = ta.getResourceId(
                R.styleable.BaseIndicaorBanner_bib_indicatorUnselectRes, 0)
        ta.recycle()
        // create indicator container
        ll_indicators = LinearLayout(context)
        ll_indicators.gravity = Gravity.CENTER
        setIndicatorSelectorRes(unselectRes, selectRes)
    }

    override fun onCreateIndicator(): View {
        if (indicatorStyle == STYLE_CORNER_RECTANGLE) { // rectangle
            unSelectDrawable = getDrawable(unselectColor,
                    indicatorCornerRadius.toFloat())
            selectDrawable = getDrawable(selectColor,
                    indicatorCornerRadius.toFloat())
        }
        val size = list?.size ?: 0
        indicatorViews.clear()
        ll_indicators.removeAllViews()
        for (i in 0 until size) {
            val iv = ImageView(mContext)
            iv.setImageDrawable(if (i == currentPositon) selectDrawable else unSelectDrawable)
            val lp = LinearLayout.LayoutParams(
                    indicatorWidth, indicatorHeight)
            lp.leftMargin = if (i == 0) 0 else indicatorGap
            ll_indicators.addView(iv, lp)
            indicatorViews.add(iv)
        }
        setCurrentIndicator(currentPositon)
        return ll_indicators
    }

    override fun setCurrentIndicator(position: Int) {
        for (i in indicatorViews.indices) {
            indicatorViews[i].setImageDrawable(
                    if (i == position) selectDrawable else unSelectDrawable)
        }
        try {
            // //Log.d(TAG, "position--->" + position);
            // //Log.d(TAG, "lastPositon--->" + lastPositon);
            if (selectAnimClass != null) {
                if (position == lastPositon) {
                    selectAnimClass?.newInstance()?.playOn(indicatorViews[position])
                } else {
                    selectAnimClass?.newInstance()?.playOn(indicatorViews[position])
                    if (unselectAnimClass == null) {
                        selectAnimClass?.newInstance()?.interpolator(ReverseInterpolator())?.playOn(indicatorViews[lastPositon])
                    } else {
                        unselectAnimClass?.newInstance()?.playOn(
                                indicatorViews[lastPositon])
                    }
                }
            }
        } catch (e: Exception) {
            CommonUtil.printError(getContext(), e)
        }
    }

    /**
     * set indicator style,STYLE_DRAWABLE_RESOURCE or STYLE_CORNER_RECTANGLE
     */
    fun setIndicatorStyle(indicatorStyle: Int): T {
        this.indicatorStyle = indicatorStyle
        return this as T
    }

    /**
     * set indicator width, unit dp,default 6dp
     */
    fun setIndicatorWidth(indicatorWidth: Float): T {
        this.indicatorWidth = dp2px(indicatorWidth)
        return this as T
    }

    /**
     * set indicator height,unit dp,default 6dp
     */
    fun setIndicatorHeight(indicatorHeight: Float): T {
        this.indicatorHeight = dp2px(indicatorHeight)
        return this as T
    }

    /**
     * set gap between two indicators,unit dp,default 6dp
     */
    fun setIndicatorGap(indicatorGap: Float): T {
        this.indicatorGap = dp2px(indicatorGap)
        return this as T
    }

    /**
     * set indicator select color for STYLE_CORNER_RECTANGLE,default "#ffffff"
     */
    fun setIndicatorSelectColor(selectColor: Int): T {
        this.selectColor = selectColor
        return this as T
    }

    /**
     * set indicator unselect color for STYLE_CORNER_RECTANGLE,default
     * "#88ffffff"
     */
    fun setIndicatorUnselectColor(unselectColor: Int): T {
        this.unselectColor = unselectColor
        return this as T
    }

    /**
     * set indicator corner raduis for STYLE_CORNER_RECTANGLE,unit dp,default
     * 3dp
     */
    fun setIndicatorCornerRadius(indicatorCornerRadius: Float): T {
        this.indicatorCornerRadius = dp2px(indicatorCornerRadius)
        return this as T
    }

    /**
     * set indicator select and unselect drawable resource for
     * STYLE_DRAWABLE_RESOURCE
     */
    private fun setIndicatorSelectorRes(unselectedRes: Int, selectRes: Int): T {
        try {
            if (indicatorStyle == STYLE_DRAWABLE_RESOURCE) {
                if (selectRes != 0) {
                    selectDrawable = SkinCompatResources.getDrawable(getContext(), selectRes)
                }
                if (unselectedRes != 0) {
                    unSelectDrawable = SkinCompatResources.getDrawable(getContext(), unselectedRes)
                }
            }
        } catch (e: Resources.NotFoundException) {
            CommonUtil.printError(getContext(), e)
        }
        return this as T
    }

    /**
     * set indicator select anim
     */
    fun setSelectAnimClass(selectAnimClass: Class<out BaseAnimator?>?): T {
        this.selectAnimClass = selectAnimClass
        return this as T
    }

    /**
     * set indicator unselect anim
     */
    fun setUnselectAnimClass(
            unselectAnimClass: Class<out BaseAnimator?>?): T {
        this.unselectAnimClass = unselectAnimClass
        return this as T
    }

    private inner class ReverseInterpolator : Interpolator {
        override fun getInterpolation(value: Float): Float {
            return abs(1.0f - value)
        }
    }

    private fun getDrawable(color: Int, radius: Float): GradientDrawable {
        val drawable = GradientDrawable()
        drawable.cornerRadius = radius
        drawable.setColor(color)
        return drawable
    }
}