package com.black.im.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.AttributeSet
import com.black.im.R
import com.black.im.view.TeamHeadSynthesizer

/**
 * 合成头像
 */
class SynthesizedImageView : ShadeImageView {
    /**
     * 群聊头像合成器
     */
    var teamHeadSynthesizer: TeamHeadSynthesizer? = null
    var imageSize = 100
    var synthesizedBg = Color.parseColor("#cfd3d8")
    var defaultImageResId = 0
    var imageGap = 6

    constructor(context: Context?) : super(context) {
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initAttrs(attrs)
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initAttrs(attrs)
        init(context)
    }

    private fun initAttrs(attributeSet: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attributeSet, R.styleable.SynthesizedImageView)
        if (null != ta) {
            synthesizedBg = ta.getColor(R.styleable.SynthesizedImageView_synthesized_image_bg, synthesizedBg)
            defaultImageResId = ta.getResourceId(R.styleable.SynthesizedImageView_synthesized_default_image, defaultImageResId)
            imageSize = ta.getDimensionPixelSize(R.styleable.SynthesizedImageView_synthesized_image_size, imageSize)
            imageGap = ta.getDimensionPixelSize(R.styleable.SynthesizedImageView_synthesized_image_gap, imageGap)
            ta.recycle()
        }
    }

    private fun init(context: Context?) {
        teamHeadSynthesizer = TeamHeadSynthesizer(context!!, this)
        teamHeadSynthesizer?.setMaxWidthHeight(imageSize, imageSize)
        teamHeadSynthesizer?.defaultImage = defaultImageResId
        teamHeadSynthesizer?.bgColor = synthesizedBg
        teamHeadSynthesizer?.gap = imageGap
    }

    fun displayImage(imageUrls: List<String>?): SynthesizedImageView {
        teamHeadSynthesizer?.multiImageData?.imageUrls = imageUrls
        return this
    }

    fun defaultImage(defaultImage: Int): SynthesizedImageView {
        teamHeadSynthesizer?.defaultImage = defaultImage
        return this
    }

    fun defaultImageBitmap(bitmap: Bitmap?): SynthesizedImageView {
        teamHeadSynthesizer?.defaultImageBitmap = bitmap
        return this
    }

    fun setDefaultImageUrl(url: String?) {
        teamHeadSynthesizer?.setDefaultImageUrl(url)
    }

    fun synthesizedWidthHeight(maxWidth: Int, maxHeight: Int): SynthesizedImageView {
        teamHeadSynthesizer?.setMaxWidthHeight(maxWidth, maxHeight)
        return this
    }

    fun load() {
        teamHeadSynthesizer?.load()
    }
}
