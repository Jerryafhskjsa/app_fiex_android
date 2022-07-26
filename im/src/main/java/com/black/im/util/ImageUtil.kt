package com.black.im.util

import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.text.TextUtils
import com.black.im.util.FileUtil.getPathFromUri
import java.io.*

object ImageUtil {
    /**
     * @param outFile 图片的目录路径
     * @param bitmap
     * @return
     */
    fun storeBitmap(outFile: File, bitmap: Bitmap?): File { // 检测是否达到存放文件的上限
        if (!outFile.exists() || outFile.isDirectory) {
            outFile.parentFile.mkdirs()
        }
        var fOut: FileOutputStream? = null
        try {
            outFile.deleteOnExit()
            outFile.createNewFile()
            fOut = FileOutputStream(outFile)
            bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
            fOut.flush()
        } catch (e1: IOException) {
            outFile.deleteOnExit()
        } finally {
            if (null != fOut) {
                try {
                    fOut.close()
                } catch (e: IOException) {
                    outFile.deleteOnExit()
                }
            }
        }
        return outFile
    }

    fun getBitmapFormPath(uri: Uri?): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            var input = TUIKit.appContext.contentResolver.openInputStream(uri)
            val onlyBoundsOptions = BitmapFactory.Options()
            onlyBoundsOptions.inJustDecodeBounds = true
            onlyBoundsOptions.inDither = true //optional
            onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888 //optional
            BitmapFactory.decodeStream(input, null, onlyBoundsOptions)
            input.close()
            val originalWidth = onlyBoundsOptions.outWidth
            val originalHeight = onlyBoundsOptions.outHeight
            if (originalWidth == -1 || originalHeight == -1) return null
            //图片分辨率以480x800为标准
            var hh = 800f //这里设置高度为800f
            var ww = 480f //这里设置宽度为480f
            val degree = getBitmapDegree(uri)
            if (degree == 90 || degree == 270) {
                hh = 480f
                ww = 800f
            }
            //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
            var be = 1 //be=1表示不缩放
            if (originalWidth > originalHeight && originalWidth > ww) { //如果宽度大的话根据宽度固定大小缩放
                be = (originalWidth / ww).toInt()
            } else if (originalWidth < originalHeight && originalHeight > hh) { //如果高度高的话根据宽度固定大小缩放
                be = (originalHeight / hh).toInt()
            }
            if (be <= 0) be = 1
            //比例压缩
            val bitmapOptions = BitmapFactory.Options()
            bitmapOptions.inSampleSize = be //设置缩放比例
            bitmapOptions.inDither = true //optional
            bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888 //optional
            input = TUIKit.appContext.contentResolver.openInputStream(uri)
            bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions)
            input.close()
            compressImage(bitmap)
            bitmap = rotateBitmapByDegree(bitmap, degree)
        } catch (e: Exception) {
        }
        return bitmap //再进行质量压缩
    }

    fun getBitmapFormPath(path: String?): Bitmap? {
        return if (TextUtils.isEmpty(path)) {
            null
        } else getBitmapFormPath(Uri.fromFile(File(path)))
    }

    fun compressImage(image: Bitmap?): Bitmap {
        val baos = ByteArrayOutputStream()
        image!!.compress(Bitmap.CompressFormat.JPEG, 100, baos) //质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        var options = 100
        while (baos.toByteArray().size / 1024 > 100) { //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset() //重置baos即清空baos
            //第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差  ，第三个参数：保存压缩后的数据的流
            image.compress(Bitmap.CompressFormat.JPEG, options, baos) //这里压缩options%，把压缩后的数据存放到baos中
            options -= 10 //每次都减少10
        }
        val isBm = ByteArrayInputStream(baos.toByteArray()) //把压缩后的数据baos存放到ByteArrayInputStream中
        return BitmapFactory.decodeStream(isBm, null, null)
    }

    /**
     * 读取图片的旋转的角度
     */
    fun getBitmapDegree(uri: Uri?): Int {
        var degree = 0
        try { // 从指定路径下读取图片，并获取其EXIF信息
            val exifInterface = ExifInterface(getPathFromUri(uri!!))
            // 获取图片的旋转信息
            val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL)
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
            }
        } catch (e: IOException) {
        }
        return degree
    }

    /**
     * 读取图片的旋转的角度
     */
    fun getBitmapDegree(fileName: String?): Int {
        var degree = 0
        try { // 从指定路径下读取图片，并获取其EXIF信息
            val exifInterface = ExifInterface(fileName)
            // 获取图片的旋转信息
            val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL)
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
            }
        } catch (e: IOException) {
        }
        return degree
    }

    /**
     * 将图片按照某个角度进行旋转
     *
     * @param bm     需要旋转的图片
     * @param degree 旋转角度
     * @return 旋转后的图片
     */
    fun rotateBitmapByDegree(bm: Bitmap?, degree: Int): Bitmap? {
        var returnBm: Bitmap? = null
        // 根据旋转角度，生成旋转矩阵
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        try { // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm!!.width, bm.height, matrix, true)
        } catch (e: OutOfMemoryError) {
        }
        if (returnBm == null) {
            returnBm = bm
        }
        if (bm != returnBm) {
            bm!!.recycle()
        }
        return returnBm
    }

    fun getImageSize(path: String?): IntArray {
        val size = IntArray(2)
        try {
            val onlyBoundsOptions = BitmapFactory.Options()
            onlyBoundsOptions.inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, onlyBoundsOptions)
            val originalWidth = onlyBoundsOptions.outWidth
            val originalHeight = onlyBoundsOptions.outHeight
            //size[0] = originalWidth;
//size[1] = originalHeight;
            val degree = getBitmapDegree(path)
            if (degree == 0) {
                size[0] = originalWidth
                size[1] = originalHeight
            } else { //图片分辨率以480x800为标准
                var hh = 800f //这里设置高度为800f
                var ww = 480f //这里设置宽度为480f
                if (degree == 90 || degree == 270) {
                    hh = 480f
                    ww = 800f
                }
                //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
                var be = 1 //be=1表示不缩放
                if (originalWidth > originalHeight && originalWidth > ww) { //如果宽度大的话根据宽度固定大小缩放
                    be = (originalWidth / ww).toInt()
                } else if (originalWidth < originalHeight && originalHeight > hh) { //如果高度高的话根据宽度固定大小缩放
                    be = (originalHeight / hh).toInt()
                }
                if (be <= 0) be = 1
                val bitmapOptions = BitmapFactory.Options()
                bitmapOptions.inSampleSize = be //设置缩放比例
                bitmapOptions.inDither = true //optional
                bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888 //optional
                var bitmap = BitmapFactory.decodeFile(path, bitmapOptions)
                bitmap = rotateBitmapByDegree(bitmap, degree)
                size[0] = bitmap!!.width
                size[1] = bitmap.height
            }
        } catch (e: Exception) {
        }
        return size
    }

    fun getImageSize(uri: Uri?): IntArray {
        val size = IntArray(2)
        try {
            var `is` = TUIKit.appContext.contentResolver
                    .openInputStream(uri)
            val onlyBoundsOptions = BitmapFactory.Options()
            onlyBoundsOptions.inJustDecodeBounds = true
            BitmapFactory.decodeStream(`is`, null, onlyBoundsOptions)
            val originalWidth = onlyBoundsOptions.outWidth
            val originalHeight = onlyBoundsOptions.outHeight
            //size[0] = originalWidth;
//size[1] = originalHeight;
            val degree = getBitmapDegree(uri)
            if (degree == 0) {
                size[0] = originalWidth
                size[1] = originalHeight
            } else { //图片分辨率以480x800为标准
                var hh = 800f //这里设置高度为800f
                var ww = 480f //这里设置宽度为480f
                if (degree == 90 || degree == 270) {
                    hh = 480f
                    ww = 800f
                }
                //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
                var be = 1 //be=1表示不缩放
                if (originalWidth > originalHeight && originalWidth > ww) { //如果宽度大的话根据宽度固定大小缩放
                    be = (originalWidth / ww).toInt()
                } else if (originalWidth < originalHeight && originalHeight > hh) { //如果高度高的话根据宽度固定大小缩放
                    be = (originalHeight / hh).toInt()
                }
                if (be <= 0) be = 1
                val bitmapOptions = BitmapFactory.Options()
                bitmapOptions.inSampleSize = be //设置缩放比例
                bitmapOptions.inDither = true //optional
                bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888 //optional
                `is` = TUIKit.appContext.contentResolver
                        .openInputStream(uri)
                var bitmap = BitmapFactory.decodeStream(`is`, null, bitmapOptions)
                bitmap = rotateBitmapByDegree(bitmap, degree)
                size[0] = bitmap!!.width
                size[1] = bitmap.height
            }
        } catch (e: Exception) {
        }
        return size
    }

    fun copyImage(path: String?, dir: String): CopyImageInfo? {
        val info = CopyImageInfo()
        if (null == path) {
            return null
        }
        try {
            val index = path.lastIndexOf(".")
            var fileType = ""
            if (index >= 0) {
                fileType = path.substring(index + 1)
            }
            val newFileName = dir + File.separator + System.currentTimeMillis() + "." + fileType
            var `is`: InputStream = FileInputStream(File(path))
            val degree = getBitmapDegree(path)
            val file = File(newFileName)
            val onlyBoundsOptions = BitmapFactory.Options()
            onlyBoundsOptions.inJustDecodeBounds = true
            BitmapFactory.decodeStream(`is`, null, onlyBoundsOptions)
            info.width = onlyBoundsOptions.outWidth
            info.height = onlyBoundsOptions.outHeight
            //没有旋转，直接copy
            if (degree == 0) {
                `is` = FileInputStream(File(path))
                val os: OutputStream = FileOutputStream(file)
                val bt = ByteArray(1024 * 10)
                var c: Int
                while (`is`.read(bt).also { c = it } > 0) {
                    os.write(bt, 0, c)
                }
                `is`.close()
                os.close()
            } else {
                var ww = 400
                var hh = 800
                if (degree == 90 || degree == 270) {
                    ww = 800
                    hh = 400
                }
                var be = 1 //be=1表示不缩放
                if (info.width > info.height && info.width > ww) { //如果宽度大的话根据宽度固定大小缩放
                    be = info.width / ww
                } else if (info.width < info.height && info.height > hh) { //如果高度高的话根据宽度固定大小缩放
                    be = info.height / hh
                }
                if (be <= 0) {
                    be = 1
                }
                //比例压缩
                val bitmapOptions = BitmapFactory.Options()
                bitmapOptions.inSampleSize = be //设置缩放比例
                bitmapOptions.inDither = true //optional
                bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888 //optional
                `is` = FileInputStream(File(path))
                var bitmap = BitmapFactory.decodeStream(`is`, null, bitmapOptions)
                bitmap = rotateBitmapByDegree(bitmap, degree)
                info.width = bitmap!!.width
                info.height = bitmap.height
                storeBitmap(file, bitmap)
            }
            info.path = newFileName
            return info
        } catch (e: FileNotFoundException) {
        } catch (e: IOException) {
        }
        return null
    }

    /**
     * 转换图片成圆形
     *
     * @param bitmap 传入Bitmap对象
     * @return
     */
    fun toRoundBitmap(bitmap: Bitmap): Bitmap {
        var width = bitmap.width
        var height = bitmap.height
        val roundPx: Float
        val left: Float
        val top: Float
        val right: Float
        val bottom: Float
        val dst_left: Float
        val dst_top: Float
        val dst_right: Float
        val dst_bottom: Float
        if (width <= height) {
            roundPx = width / 2.toFloat()
            left = 0f
            top = 0f
            right = width.toFloat()
            bottom = width.toFloat()
            height = width
            dst_left = 0f
            dst_top = 0f
            dst_right = width.toFloat()
            dst_bottom = width.toFloat()
        } else {
            roundPx = height / 2.toFloat()
            val clip = (width - height) / 2.toFloat()
            left = clip
            right = width - clip
            top = 0f
            bottom = height.toFloat()
            width = height
            dst_left = 0f
            dst_top = 0f
            dst_right = height.toFloat()
            dst_bottom = height.toFloat()
        }
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val color = -0xbdbdbe
        val paint = Paint()
        val src = Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        val dst = Rect(dst_left.toInt(), dst_top.toInt(), dst_right.toInt(), dst_bottom.toInt())
        val rectF = RectF(dst)
        paint.isAntiAlias = true // 设置画笔无锯齿
        canvas.drawARGB(0, 0, 0, 0) // 填充整个Canvas
        paint.color = color
        // 以下有两种方法画圆,drawRounRect和drawCircle
// canvas.drawRoundRect(rectF, roundPx, roundPx, paint);// 画圆角矩形，第一个参数为图形显示区域，第二个参数和第三个参数分别是水平圆角半径和垂直圆角半径。
        canvas.drawCircle(roundPx, roundPx, roundPx, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN) // 设置两张图片相交时的模式
        canvas.drawBitmap(bitmap, src, dst, paint) //以Mode.SRC_IN模式合并bitmap和已经draw了的Circle
        return output
    }

    class CopyImageInfo {
        var path: String? = null
        var width = 0
        var height = 0
    }
}