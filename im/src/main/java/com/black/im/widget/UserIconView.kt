package com.black.im.widget

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.black.im.R
import com.black.im.model.chat.MessageInfo
import com.black.im.view.DynamicChatUserIconView

class UserIconView : RelativeLayout {
    private var mIconView: SynthesizedImageView? = null
    private var mDynamicView: DynamicChatUserIconView? = null
    private var mDefaultImageResId = 0
    private var mIconRadius = 0

    constructor(context: Context?) : super(context) {
        init(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    fun setDynamicChatIconView(dynamicView: DynamicChatUserIconView?) {
        mDynamicView = dynamicView
        mDynamicView?.setLayout(this)
        mDynamicView?.setMainViewId(R.id.profile_icon_group)
        if (mDynamicView?.iconRadius ?: 0 >= 0) {
            mIconView?.radius = mDynamicView?.iconRadius ?: 0
        }
    }

    fun invokeInformation(messageInfo: MessageInfo?) {
        mIconView?.load()
        if (mDynamicView != null) {
            mDynamicView?.parseInformation(messageInfo)
        }
    }

    private fun init(attributeSet: AttributeSet?) {
        View.inflate(context, R.layout.profile_icon_view, this)
        if (attributeSet != null) {
            val ta = context.obtainStyledAttributes(attributeSet, R.styleable.UserIconView)
            if (null != ta) {
                mDefaultImageResId = ta.getResourceId(R.styleable.UserIconView_default_image, mDefaultImageResId)
                mIconRadius = ta.getDimensionPixelSize(R.styleable.UserIconView_image_radius, mIconRadius)
                ta.recycle()
            }
        }
        mIconView = findViewById(R.id.profile_icon)
        if (mDefaultImageResId > 0) {
            mIconView?.defaultImage(mDefaultImageResId)
        }
        if (mIconRadius > 0) {
            mIconView?.radius = mIconRadius
        }
    }

    fun setDefaultImageResId(resId: Int) {
        mDefaultImageResId = resId
        mIconView?.defaultImage(resId)
    }

    fun setDefaultImageBitmap(bitmap: Bitmap?) {
        mIconView?.defaultImageBitmap(bitmap)
    }

    fun setDefaultImageUrl(url: String?) {
        mIconView?.setDefaultImageUrl(url)
    }

    fun setRadius(radius: Int) {
        mIconRadius = radius
        mIconView?.radius = mIconRadius
    }

    fun setIconUrls(iconUrls: List<String>?) {
        mIconView?.displayImage(iconUrls)?.load()
    }
}