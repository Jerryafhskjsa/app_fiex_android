package com.black.im.interfaces

import android.graphics.Bitmap
import android.graphics.Canvas

interface Synthesizer {
    /**
     * 图片合成
     */
    fun synthesizeImageList(): Bitmap?

    /**
     * 异步下载图片列表
     */
    fun asyncLoadImageList(): Boolean

    /**
     * 画合成的图片
     *
     * @param canvas
     */
    fun drawDrawable(canvas: Canvas?)
}