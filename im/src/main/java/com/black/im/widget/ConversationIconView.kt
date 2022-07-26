package com.black.im.widget

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import com.black.im.R
import com.black.im.model.ConversationInfo
import com.black.im.util.ImageUtil.toRoundBitmap
import com.black.im.util.ScreenUtil.getPxByDp
import com.black.im.view.DynamicConversationIconView

/**
 * 会话列表头像View
 */
class ConversationIconView : RelativeLayout {
    companion object {
        private val icon_size = getPxByDp(50)
    }

    private var mIconView: ImageView? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    fun invokeInformation(conversationInfo: ConversationInfo, infoView: DynamicConversationIconView) {
        infoView.setLayout(this)
        infoView.setMainViewId(R.id.profile_icon_group)
        infoView.parseInformation(conversationInfo)
    }

    private fun init() {
        View.inflate(context, R.layout.profile_icon_view, this)
        mIconView = findViewById(R.id.profile_icon)
        (mIconView as SynthesizedImageView?)!!.defaultImage(R.drawable.default_user_icon)
    }

    fun setProfileImageView(iconView: ImageView?) {
        mIconView = iconView
        val params = LayoutParams(icon_size, icon_size)
        params.addRule(CENTER_IN_PARENT)
        addView(mIconView, params)
    }

    /**
     * 设置会话头像的url
     *
     * @param iconUrls 头像url,最多只取前9个
     */
    fun setIconUrls(iconUrls: List<String>?) {
        if (mIconView is SynthesizedImageView) {
            (mIconView as SynthesizedImageView).displayImage(iconUrls).load()
        }
    }

    fun setDefaultImageResId(resId: Int) {
        val bd = context.resources.getDrawable(resId) as BitmapDrawable
        mIconView!!.setImageBitmap(bd.bitmap)
    }

    fun setBitmapResId(resId: Int) {
        val bd = context.resources.getDrawable(resId) as BitmapDrawable
        val bitmap = toRoundBitmap(bd.bitmap)
        mIconView!!.setImageBitmap(bitmap)
    }
}