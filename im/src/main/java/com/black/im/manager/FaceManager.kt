package com.black.im.manager

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.util.DisplayMetrics
import android.util.LruCache
import android.widget.EditText
import android.widget.TextView
import com.black.im.R
import com.black.im.model.face.Emoji
import com.black.im.model.face.FaceGroup
import com.black.im.util.ScreenUtil.getPxByDp
import com.black.im.util.TUIKit.appContext
import com.black.im.util.TUIKit.configs
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.regex.Pattern

object FaceManager {
    private val drawableWidth = getPxByDp(32)
    val emojiList = ArrayList<Emoji?>()
    private val drawableCache: LruCache<String?, Bitmap?> = LruCache(1024)
    private val context = appContext
    private val emojiFilters = context.resources.getStringArray(R.array.emoji_filter)
    val customFaceList = ArrayList<FaceGroup>()

    fun getCustomBitmap(groupId: Int, name: String): Bitmap? {
        for (i in customFaceList.indices) {
            val group = customFaceList[i]
            if (group.groupId == groupId) {
                val faces = group.faces
                faces?.let {
                    for (j in faces.indices) {
                        val face = faces[j]
                        if (face?.filter == name) {
                            return face.icon
                        }
                    }
                }
            }
        }
        return null
    }

    fun loadFaceFiles() {
        object : Thread() {
            override fun run() {
                for (i in emojiFilters.indices) {
                    loadAssetBitmap(emojiFilters[i], "emoji/" + emojiFilters[i] + "@2x.png", true)
                }
                val config = configs.customFaceConfig ?: return
                val groups = config.faceGroups ?: return
                for (i in groups.indices) {
                    val groupConfigs = groups[i]
                    val groupInfo = FaceGroup()
                    groupInfo.groupId = groupConfigs.faceGroupId
                    groupInfo.desc = groupConfigs.faceIconName
                    groupInfo.pageColumnCount = groupConfigs.pageColumnCount
                    groupInfo.pageRowCount = groupConfigs.pageRowCount
                    groupInfo.groupIcon = if (groupConfigs.faceIconName == null || groupConfigs.faceIconPath == null) null else
                        loadAssetBitmap(groupConfigs.faceIconName!!, groupConfigs.faceIconPath!!, false)!!.icon
                    val customFaceArray = groupConfigs.customFaceList
                    val faceList = ArrayList<Emoji?>()
                    for (j in customFaceArray.indices) {
                        val face = customFaceArray[j]
                        val emoji = if (face.faceName == null || face.assetPath == null) null else
                            loadAssetBitmap(face.faceName!!, face.assetPath!!, false)
                        emoji?.width = face.faceWidth
                        emoji?.height = face.faceHeight
                        faceList.add(emoji)
                    }
                    groupInfo.faces = faceList
                    customFaceList.add(groupInfo)
                }
            }
        }.start()
    }

    private fun loadAssetBitmap(filter: String, assetPath: String, isEmoji: Boolean): Emoji? {
        var `is`: InputStream? = null
        try {
            val emoji = Emoji()
            val resources = context.resources
            val options = BitmapFactory.Options()
            options.inDensity = DisplayMetrics.DENSITY_XXHIGH
            options.inScreenDensity = resources.displayMetrics.densityDpi
            options.inTargetDensity = resources.displayMetrics.densityDpi
            context.assets.list("")
            `is` = context.assets.open(assetPath)
            val bitmap = BitmapFactory.decodeStream(`is`, Rect(0, 0, drawableWidth, drawableWidth), options)
            if (bitmap != null) {
                drawableCache.put(filter, bitmap)
                emoji.icon = bitmap
                emoji.filter = filter
                if (isEmoji) {
                    emojiList.add(emoji)
                }
            }
            return emoji
        } catch (e: Exception) {
        } finally {
            if (`is` != null) {
                try {
                    `is`.close()
                } catch (e: IOException) {
                }
            }
        }
        return null
    }

    fun calculateInSampleSize(options: BitmapFactory.Options,
                              reqWidth: Int, reqHeight: Int): Int { // 源图片的高度和宽度
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) { // 计算出实际宽高和目标宽高的比率
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
// 一定都会大于等于目标的宽和高。
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        return inSampleSize
    }

    fun decodeSampledBitmapFromResource(res: Resources?, resId: Int,
                                        reqWidth: Int, reqHeight: Int): Bitmap { // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeResource(res, resId, options)
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeResource(res, resId, options)
    }

    fun dip2px(context: Context, dipValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }

    fun isFaceChar(faceChar: String?): Boolean {
        return drawableCache[faceChar] != null
    }

    fun handlerEmojiText(comment: TextView?, content: String?) {
        val sb = SpannableStringBuilder(content)
        val regex = "\\[(\\S+?)\\]"
        val p = Pattern.compile(regex)
        val m = p.matcher(content)
        while (m.find()) {
            val emojiName = m.group()
            val bitmap = drawableCache[emojiName]
            if (bitmap != null) {
                sb.setSpan(ImageSpan(context, bitmap),
                        m.start(), m.end(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            }
        }
        val selection = comment?.selectionStart ?: 0
        comment?.text = sb
        if (comment is EditText) {
            comment.setSelection(selection)
        }
    }

    fun getEmoji(name: String?): Bitmap? {
        return drawableCache[name]
    }
}