package com.black.base.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import android.widget.ImageView
import com.black.base.service.DownloadServiceHelper.downloadImage
import com.black.util.*
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

class ImageLoader(private val context: Context) {
    companion object {
        const val MAX_WIDTH = 640
        const val MAX_HEIGHT = 960
    }

    private val memoryCache = MemoryCache()
    private val fileCache: FileCache = FileCache(context)

    fun loadImageSetLoading(imageView: ImageView?, url: String?, showLoading: Boolean) {
        loadImage(imageView, url, 0, false, showLoading)
    }

    @JvmOverloads
    fun loadImage(imageView: ImageView?, url: String?, replaceNew: Boolean = false) {
        loadImage(imageView, url, 0, replaceNew)
    }

    @JvmOverloads
    fun loadImage(imageView: ImageView?, url: String?, placeholderId: Int, replaceNew: Boolean = false, showLoading: Boolean = false) {
        if (imageView == null) {
            return
        }
        var replaceNew1 = replaceNew
        imageView.tag = url
        val bitmap = ImageUtil.getCacheIcon(url)
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap)
        } else {
            if (placeholderId != 0) {
                imageView.setImageResource(placeholderId)
            }
            replaceNew1 = true
        }
        if (replaceNew1) {
            if (TextUtils.isEmpty(url)) {
                if (placeholderId != 0) {
                    imageView.setImageResource(placeholderId)
                }
            } else {
                downloadImage(context, url, showLoading, object : Callback<Bitmap?>() {
                    override fun error(type: Int, error: Any) {
                        if (placeholderId != 0) {
                            imageView.setImageResource(placeholderId)
                        }
                    }

                    override fun callback(returnData: Bitmap?) {
                        if (returnData == null) {
                            if (placeholderId != 0 && url == imageView.tag) {
                                imageView.setImageResource(placeholderId)
                            }
                        } else {
                            ImageUtil.putCacheIcon(url, returnData)
                            if (url == imageView.tag) {
                                imageView.setImageBitmap(returnData)
                            }
                        }
                    }
                })
            }
        }
    }

    @JvmOverloads
    fun loadImage(imageView: ImageView?, url: String?, placeholderId: Int, placeholderBitmap: Bitmap?, replaceNew: Boolean = false, showLoading: Boolean = false) {
        if (imageView == null) {
            return
        }
        var replaceNew1 = replaceNew
        imageView.tag = url
        val bitmap = ImageUtil.getCacheIcon(url)
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap)
        } else {
            if (placeholderBitmap != null) {
                imageView.setImageBitmap(placeholderBitmap)
            } else if (placeholderId != 0) {
                imageView.setImageResource(placeholderId)
            }
            replaceNew1 = true
        }
        if (replaceNew1) {
            if (TextUtils.isEmpty(url)) {
                if (placeholderId != 0) {
                    imageView.setImageResource(placeholderId)
                }
            } else {
                downloadImage(context, url, showLoading, object : Callback<Bitmap?>() {
                    override fun error(type: Int, error: Any) {
                        if (placeholderBitmap != null && url == imageView.tag) {
                            imageView.setImageBitmap(placeholderBitmap)
                        } else if (placeholderId != 0 && url == imageView.tag) {
                            imageView.setImageResource(placeholderId)
                        }
                    }

                    override fun callback(returnData: Bitmap?) {
                        if (returnData == null) {
                            if (placeholderBitmap != null && url == imageView.tag) {
                                imageView.setImageBitmap(placeholderBitmap)
                            } else if (placeholderId != 0 && url == imageView.tag) {
                                imageView.setImageResource(placeholderId)
                            }
                        } else {
                            ImageUtil.putCacheIcon(url, returnData)
                            if (url == imageView.tag) {
                                imageView.setImageBitmap(returnData)
                            }
                        }
                    }
                })
            }
        }
    }

    fun loadImage(imageView: ImageView?, url: String?, maxLength: Long) {
        loadImage(imageView, url, false, maxLength)
    }

    fun loadImage(imageView: ImageView?, url: String?, replaceNew: Boolean, maxLength: Long) {
        loadImage(imageView, url, 0, replaceNew, maxLength)
    }

    fun loadImage(imageView: ImageView?, url: String?, placeholderId: Int, replaceNew: Boolean, maxLength: Long) {
        if (imageView == null) {
            return
        }
        var replaceNew1 = replaceNew
        imageView.tag = url
        val bitmap = ImageUtil.getCacheIcon(url)
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap)
        } else {
            if (placeholderId != 0) {
                imageView.setImageResource(placeholderId)
            }
            replaceNew1 = true
        }
        if (replaceNew1) {
            downloadImage(context, url, maxLength, false, object : Callback<Bitmap?>() {
                override fun error(type: Int, error: Any) {
                    if (placeholderId != 0) {
                        imageView.setImageResource(placeholderId)
                    }
                }

                override fun callback(returnData: Bitmap?) {
                    if (returnData == null) {
                        if (placeholderId != 0 && url == imageView.tag) {
                            imageView.setImageResource(placeholderId)
                        }
                    } else {
                        ImageUtil.putCacheIcon(url, returnData)
                        if (url == imageView.tag) {
                            imageView.setImageBitmap(returnData)
                        }
                    }
                }
            })
        }
    }

    fun getBitmap(url: String?, callback: CallbackObject<Bitmap?>?) {
        val bitmap = ImageUtil.getCacheIcon(url)
        if (bitmap != null) {
            callback?.callback(bitmap)
        } else {
            downloadImage(context, url, false, object : Callback<Bitmap?>() {
                override fun error(type: Int, error: Any) {
                    callback?.callback(null)
                }

                override fun callback(returnData: Bitmap?) {
                    callback?.callback(returnData)
                }
            })
        }
    }

    fun getBitmap(url: String?, maxWidth: Int, maxHeight: Int, callback: CallbackObject<Bitmap?>?) {
        val bitmap = ImageUtil.getCacheIcon(url)
        if (bitmap != null) {
            callback?.callback(bitmap)
        } else {
            downloadImage(context, url, maxWidth, maxHeight, false, object : Callback<Bitmap?>() {
                override fun error(type: Int, error: Any) {
                    callback?.callback(null)
                }

                override fun callback(returnData: Bitmap?) {
                    ImageUtil.putCacheIcon(url, returnData)
                    callback?.callback(returnData)
                }
            })
        }
    }

    fun getFile(url: String?): File {
        return fileCache.getFile(url)
    }

    fun onDecodeFile(file: File?, maxWidth: Int, maxHeight: Int): Bitmap? {
        try { // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(FileInputStream(file), null, options)
            // 调用上面定义的方法计算inSampleSize值
            options.inSampleSize = ImageUtil.calculateInSampleSize(options, maxWidth, maxHeight)
            // 使用获取到的inSampleSize值再次解析图片
            options.inJustDecodeBounds = false
            return BitmapFactory.decodeStream(FileInputStream(file), null, options)
        } catch (e: FileNotFoundException) {
        }
        return null
    }

    /**
     * 解码图像用来减少内存消耗
     *
     * @param f
     * @return
     */
    fun decodeFile(f: File?): Bitmap? {
        try { // 解码图像大小
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            BitmapFactory.decodeStream(FileInputStream(f), null, o)
            // 找到正确的刻度值，它应该是2的幂。
            val REQUIRED_SIZE = 70
            var width_tmp = o.outWidth
            var height_tmp = o.outHeight
            var scale = 1
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) break
                width_tmp /= 2
                height_tmp /= 2
                scale *= 2
            }
            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            return BitmapFactory.decodeStream(FileInputStream(f), null, o2)
        } catch (e: FileNotFoundException) {
        }
        return null
    }

    fun getBitmap(url: String?, maxWidth: Int, maxHeight: Int): Bitmap? {
        return try {
            val file = fileCache.getFile(url)
            // 从sd卡
            val b = onDecodeFile(file, maxWidth, maxHeight)
            if (b != null) return b
            // 从网络
            var bitmap: Bitmap?
            HttpUtil.copyStream(url, file)
            bitmap = onDecodeFile(file, maxWidth, maxHeight)
            bitmap?.let {
                if (maxWidth > 0) {
                    val bitmapWidth = it.width
                    val bitmapHeight = it.height
                    if (bitmapWidth > maxWidth) {
                        val scale = maxWidth.toFloat() / bitmapWidth
                        bitmap = ImageUtil.zoomBitmap(bitmap, (bitmapWidth * scale).toInt(), (bitmapHeight * scale).toInt())
                    }
                }
            }
            bitmap
        } catch (ex: Exception) {
            null
        }
    }

}