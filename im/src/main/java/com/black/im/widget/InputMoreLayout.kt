package com.black.im.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.viewpager.widget.ViewPager
import com.black.im.R
import com.black.im.adapter.ActionsPagerAdapter
import com.black.im.model.InputMoreActionUnit

class InputMoreLayout : LinearLayout {
    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        View.inflate(context, R.layout.chat_inputmore_layout, this)
    }

    // 初始化更多布局adapter
    fun init(actions: List<InputMoreActionUnit>) {
        val viewPager: ViewPager = findViewById(R.id.viewPager)
        val indicator = findViewById<ViewGroup>(R.id.actions_page_indicator)
        val adapter = ActionsPagerAdapter(viewPager, actions)
        viewPager.adapter = adapter
        initPageListener(indicator, adapter.count, viewPager)
    }

    // 初始化更多布局PageListener
    private fun initPageListener(indicator: ViewGroup, count: Int, viewPager: ViewPager) {
        viewPager.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                setIndicator(indicator, count, position)
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}
        })
        setIndicator(indicator, count, 0)
    }

    /**
     * 设置页码
     */
    private fun setIndicator(indicator: ViewGroup, total: Int, current: Int) {
        if (total <= 1) {
            indicator.removeAllViews()
        } else {
            indicator.removeAllViews()
            for (i in 0 until total) {
                val imgCur = ImageView(indicator.context)
                imgCur.id = i
                // 判断当前页码来更新
                if (i == current) {
                    imgCur.setBackgroundResource(R.drawable.page_selected)
                } else {
                    imgCur.setBackgroundResource(R.drawable.page_unselected)
                }
                indicator.addView(imgCur)
            }
        }
    }
}