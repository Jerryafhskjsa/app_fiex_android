package com.black.clutter.view

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.lib.share.ShareAdapter
import com.black.base.lib.share.ShareWindow
import com.black.base.model.clutter.Forum
import com.black.base.service.DownloadServiceHelper
import com.black.base.util.FryingUtil
import com.black.clutter.R
import com.black.clutter.databinding.ViewShareForumBinding
import com.black.util.Callback
import com.black.util.CommonUtil
import skin.support.content.res.SkinCompatResources

class ForumShareWindow(activity: BaseActionBarActivity, private val forum: Forum) : ShareWindow(activity) {
    private val wxNo: String? = if (forum == null) "" else forum.channelAccount
    private var binding: ViewShareForumBinding? = null
    var wxNoBitmap: Bitmap? = null

    override fun getShareContent(): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.view_share_forum, null, false)
        initShareContent()
        return binding?.root
    }

    override fun getShareResult(): ShareAdapter {
        return object : ImageShare() {
            override fun getShareBitmap(): Bitmap {
                return createShareBitmap()
            }
        }
    }

    override fun onClick(v: View) {
        super.onClick(v)
        if (v.id == R.id.btn_forum_copy) {
            if (CommonUtil.copyText(activity, wxNo)) {
                FryingUtil.showToast(activity, activity.getString(R.string.copy_text_success))
            } else {
                FryingUtil.showToast(activity, activity.getString(R.string.copy_text_failed))
            }
        }
    }

    override fun initShareContent() {
        if (forum == null) {
            return
        }
        binding?.coinType?.text = activity.getString(R.string.forum_add, forum.channelName)
        if (forum.orCodeUrl != null) {
            DownloadServiceHelper.downloadImage(activity, forum.orCodeUrl, false, object : Callback<Bitmap?>() {
                override fun error(type: Int, error: Any) {}
                override fun callback(returnData: Bitmap?) {
                    if (returnData != null) {
                        wxNoBitmap = returnData
                        binding?.qrcode?.setImageBitmap(returnData)
                    }
                }
            })
        }
        binding?.text?.setText(activity.getString(R.string.forum_info, forum.channelName, wxNo))
        binding?.btnForumCopy?.setOnClickListener(this)
    }

    private fun createShareBitmap(): Bitmap {
        val displayMetrics = activity.resources.displayMetrics
        val scale = 375.0f * displayMetrics.density / displayMetrics.widthPixels
        val width = 375
        val headerHeight = 0
        val contentHeight = (binding?.root?.height ?: 0) * 375 / (binding?.root?.width ?: 1)
        val height = headerHeight + contentHeight
        val bitmap = Bitmap.createBitmap((width * displayMetrics.density).toInt(), (height * displayMetrics.density).toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(SkinCompatResources.getColor(activity, R.color.white))
        canvas.save()
        canvas.restore()
        canvas.translate(0f, headerHeight.toFloat())
        canvas.save()
        canvas.scale(scale, scale)
        binding?.btnForumCopy?.visibility = View.GONE
        binding?.root?.draw(canvas)
        binding?.btnForumCopy?.visibility = View.VISIBLE
        canvas.restore()
        return bitmap
    }
}