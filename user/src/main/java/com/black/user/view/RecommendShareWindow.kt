package com.black.user.view

import android.graphics.Bitmap
import android.view.View
import android.view.View.MeasureSpec
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.activity.BaseActivity
import com.black.base.lib.share.ShareAdapter
import com.black.base.lib.share.ShareWindow
import com.black.base.model.user.RecommendInfo
import com.black.base.model.user.UserInfo
import com.black.base.util.ImageLoader
import com.black.user.R
import com.black.user.databinding.ViewShareRecommendBinding

class RecommendShareWindow : ShareWindow {
    private val userInfo: UserInfo?
    private val info: RecommendInfo?
    private var imageLoader: ImageLoader

    private var binding: ViewShareRecommendBinding? = null

    constructor(activity: BaseActivity, userInfo: UserInfo?, info: RecommendInfo?) : super(activity) {
        this.userInfo = userInfo
        this.info = info
        imageLoader = ImageLoader(activity)
    }

    constructor(activity: BaseActionBarActivity, userInfo: UserInfo?, info: RecommendInfo?) : super(activity) {
        this.userInfo = userInfo
        this.info = info
        imageLoader = ImageLoader(activity)
    }

    override fun initShareContent() {
        if (userInfo == null || info == null) {
            return
        }
        imageLoader.loadImage(binding?.icon, info.iconUrl)
        binding?.title?.setText(info.title)
        binding?.text?.setText(info.describe)
    }

    override fun getShareContent(): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.view_share_recommend, null, false)
        initShareContent()
        return binding?.root
    }

    override fun getShareResult(): ShareAdapter {
        return object : LinkShare() {
            override fun getShareTitle(): String? {
                return binding?.title?.text.toString()
            }

            override fun getShareText(): String? {
                return binding?.text?.text.toString()
            }

            override fun getShareIcon(): Bitmap? {
                binding?.icon?.isDrawingCacheEnabled = true
                binding?.icon?.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
                binding?.icon?.layout(0, 0, binding?.icon?.measuredWidth!!, binding?.icon?.measuredHeight!!)
                binding?.icon?.buildDrawingCache(true)
                val bitmapCache = binding?.icon?.drawingCache
                val b = if (bitmapCache == null) null else Bitmap.createBitmap(bitmapCache)
                binding?.icon?.isDrawingCacheEnabled = false // clear drawing cache
                return b
            }

            override fun getShareLink(): String? {
                return info!!.link
            }
        }
    }
}