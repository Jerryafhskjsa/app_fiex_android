package com.black.im.view

import android.graphics.Bitmap
import java.util.*

/**
 * 多张图片数据
 */
class MultiImageData {
    //图片地址链接
    var imageUrls: List<String>? = null
    //默认的图片ID
    var defaultImageResId = 0
    //默认的图片ID
    var defaultImageBitmap: Bitmap? = null
    //默认的图片地址
    var defaultImageUrl: String? = null
    //下载下来的图片地址
    var bitmapMap: MutableMap<Int, Bitmap?>? = null

    constructor() {}
    constructor(defaultImageResId: Int) {
        this.defaultImageResId = defaultImageResId
    }

    constructor(imageUrls: List<String>?, defaultImageResId: Int) {
        this.imageUrls = imageUrls
        this.defaultImageResId = defaultImageResId
    }

    fun putBitmap(bitmap: Bitmap?, position: Int) {
        if (null != bitmapMap) {
            synchronized(bitmapMap!!) { bitmapMap!!.put(position, bitmap) }
        } else {
            bitmapMap = HashMap()
            synchronized(bitmapMap!!) { bitmapMap!!.put(position, bitmap) }
        }
    }

    fun getBitmap(position: Int): Bitmap? {
        if (null != bitmapMap) {
            synchronized(bitmapMap!!) { return bitmapMap!![position] }
        }
        return null
    }

    fun size(): Int {
        return if (null != imageUrls) {
            if (imageUrls!!.size > maxSize) maxSize else imageUrls!!.size
        } else {
            0
        }
    }

    companion object {
        const val maxSize = 9
    }
}