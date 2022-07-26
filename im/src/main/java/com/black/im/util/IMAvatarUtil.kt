package com.black.im.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextUtils
import android.view.Gravity
import com.black.im.R
import com.black.lib.typeface.TypefaceTextPaintHelper
import com.black.util.MemoryCache
import skin.support.content.res.SkinCompatResources

object IMAvatarUtil {
    private val avatarImageCache = MemoryCache()
    fun putCacheIcon(key: String?) {
        val firstLetter = if (TextUtils.isEmpty(key)) null else key!![0].toString()
        if (!TextUtils.isEmpty(firstLetter)) {
            avatarImageCache.put(firstLetter, createDefaultAvatar(firstLetter, TUIKit.defaultAvatarSize))
        }
    }

    fun getCacheIcon(key: String?): Bitmap? {
        if (key == null)
            return null
        val firstLetter = if (TextUtils.isEmpty(key)) null else key[0].toString()
        if (!TextUtils.isEmpty(firstLetter)) {
            var bitmap = avatarImageCache[firstLetter]
            if (bitmap == null) {
                putCacheIcon(firstLetter)
                bitmap = avatarImageCache[firstLetter]
            }
            return bitmap
        }
        return null
    }

    fun clear() {
        avatarImageCache.clear()
    }

    //创建默认头像，使用第一个字和背景创建bitmap
    fun createDefaultAvatar(userName: String?, size: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(SkinCompatResources.getColor(TUIKit.appContext, R.color.transparent))
        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.textSize = TUIKit.appContext.resources.getDimensionPixelSize(R.dimen.text_size_16).toFloat()
        paint.color = SkinCompatResources.getColor(TUIKit.appContext, R.color.T4)
        val bg = SkinCompatResources.getDrawable(TUIKit.appContext, R.drawable.bg_circle_green)
        bg.setBounds(0, 0, size, size)
        bg.draw(canvas)
        val firstLetter = if (TextUtils.isEmpty(userName)) null else userName!![0].toString()
        if (!TextUtils.isEmpty(firstLetter)) {
            val ma30Helper = TypefaceTextPaintHelper(TUIKit.appContext, paint, Typeface.BOLD, firstLetter)
            ma30Helper.draw(canvas, size / 2.toFloat(), size / 2.toFloat(), Gravity.CENTER)
        }
        return bitmap
    }
}