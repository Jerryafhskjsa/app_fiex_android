package com.black.im.view

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.text.TextUtils
import android.widget.ImageView
import com.black.im.imageEngine.impl.GlideEngine
import com.black.im.interfaces.Synthesizer
import com.black.im.util.ImageUtil.storeBitmap
import com.black.im.util.MD5Utils.getMD5String
import com.black.im.util.TUIKitConstants
import java.io.File
import java.util.concurrent.ExecutionException

class TeamHeadSynthesizer(var mContext: Context, var imageView: ImageView) : Synthesizer {
    //当前多图合成的唯一ID，用来做缓存处理，以及判断合成图片是否需要变更，如果多个url有变动，currentTargetID也会发生变动，需要重新生成
    var currentTargetID: String? = null
    /**
     * 多图片数据
     */
    var multiImageData: MultiImageData = MultiImageData()
    var targetImageSize: Int = 0 //目标图片宽高
    var maxWidth = 0
    var maxHeight = 0 //最大宽度，最大高度
    var bgColor = Color.parseColor("#cfd3d8")
    var loadOk = false //加载完毕
    var callback: Callback = object : Callback {
        override fun onCall(obj: Any?, targetID: String?, complete: Boolean) {
            //判断回调结果的任务id是否为同一批次的任务
            if (!TextUtils.equals(currentTargetID, targetID)) return
            if (obj is File) {
                if (complete) loadOk = true
                imageView.setImageBitmap(BitmapFactory.decodeFile(obj.absolutePath))
            } else if (obj is Bitmap) {
                if (complete) loadOk = true
                imageView.setImageBitmap(obj as Bitmap?)
            }
        }
    }
    private var mRowCount = 0 //行数
    private var mColumnCount = 0  //列数
    var gap = 6 //宫格间距

    fun setMaxWidthHeight(maxWidth: Int, maxHeight: Int) {
        this.maxWidth = maxWidth
        this.maxHeight = maxHeight
    }

    var defaultImage: Int
        get() = multiImageData.defaultImageResId
        set(defaultImageResId) {
            multiImageData.defaultImageResId = defaultImageResId
        }

    var defaultImageBitmap: Bitmap?
        get() = multiImageData.defaultImageBitmap
        set(defaultImageBitmap) {
            multiImageData.defaultImageBitmap = defaultImageBitmap
        }

    fun setDefaultImageUrl(url: String?) {
        multiImageData.defaultImageUrl = url
    }

    /**
     * 设置宫格参数
     *
     * @param imagesSize 图片数量
     * @return 宫格参数 gridParam[0] 宫格行数 gridParam[1] 宫格列数
     */
    protected fun calculateGridParam(imagesSize: Int): IntArray {
        val gridParam = IntArray(2)
        when {
            imagesSize < 3 -> {
                gridParam[0] = 1
                gridParam[1] = imagesSize
            }
            imagesSize <= 4 -> {
                gridParam[0] = 2
                gridParam[1] = 2
            }
            else -> {
                gridParam[0] = imagesSize / 3 + if (imagesSize % 3 == 0) 0 else 1
                gridParam[1] = 3
            }
        }
        return gridParam
    }

    override fun synthesizeImageList(): Bitmap? {
        val mergeBitmap = Bitmap.createBitmap(maxWidth, maxHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mergeBitmap)
        drawDrawable(canvas)
        canvas.save()
        canvas.restore()
        return mergeBitmap
    }

    override fun asyncLoadImageList(): Boolean {
        var loadSuccess = true
        val imageUrls = multiImageData.imageUrls ?: ArrayList()
        for (i in imageUrls.indices) {
            val imageUrl = imageUrls[i]
            if (TextUtils.isEmpty(imageUrl)) { //图片链接不存在
                continue
            } else { //下载图片
                try {
                    val bitmap = asyncLoadImage(imageUrl, targetImageSize)
                    multiImageData.putBitmap(bitmap!!, i)
                } catch (e: InterruptedException) {
                    multiImageData.putBitmap(null, i)
                    loadSuccess = false
                } catch (e: ExecutionException) {
                    multiImageData.putBitmap(null, i)
                    loadSuccess = false
                }
            }
        }
        //下载完毕
        return loadSuccess
    }

    override fun drawDrawable(canvas: Canvas?) { //画背景
        canvas?.drawColor(bgColor)
        //画组合图片
        val size = multiImageData.size()
        val t_center = (maxHeight + gap) / 2 //中间位置以下的顶点（有宫格间距）
        val b_center = (maxHeight - gap) / 2 //中间位置以上的底部（有宫格间距）
        val l_center = (maxWidth + gap) / 2 //中间位置以右的左部（有宫格间距）
        val r_center = (maxWidth - gap) / 2 //中间位置以左的右部（有宫格间距）
        val center = (maxHeight - targetImageSize) / 2 //中间位置以上顶部（无宫格间距）
        for (i in 0 until size) {
            val rowNum = i / mColumnCount //当前行数
            val columnNum = i % mColumnCount //当前列数
            val left = (targetImageSize.toDouble() * (if (mColumnCount == 1) (columnNum + 0.5).toFloat() else columnNum.toFloat()) + gap * (columnNum + 1)).toInt()
            val top = (targetImageSize.toFloat() * (if (mColumnCount == 1) (rowNum + 0.5).toFloat() else rowNum.toFloat()) + gap * (rowNum + 1)).toInt()
            val right = left + targetImageSize
            val bottom = top + targetImageSize
            val bitmap = multiImageData.getBitmap(i)
            if (size == 1) {
                drawBitmapAtPosition(canvas, left, top, right, bottom, bitmap)
            } else if (size == 2) {
                drawBitmapAtPosition(canvas, left, center, right, center + targetImageSize, bitmap)
            } else if (size == 3) {
                if (i == 0) {
                    drawBitmapAtPosition(canvas, center, top, center + targetImageSize, bottom, bitmap)
                } else {
                    drawBitmapAtPosition(canvas, gap * i + targetImageSize * (i - 1), t_center, gap * i + targetImageSize * i, t_center + targetImageSize, bitmap)
                }
            } else if (size == 4) {
                drawBitmapAtPosition(canvas, left, top, right, bottom, bitmap)
            } else if (size == 5) {
                when (i) {
                    0 -> {
                        drawBitmapAtPosition(canvas, r_center - targetImageSize, r_center - targetImageSize, r_center, r_center, bitmap)
                    }
                    1 -> {
                        drawBitmapAtPosition(canvas, l_center, r_center - targetImageSize, l_center + targetImageSize, r_center, bitmap)
                    }
                    else -> {
                        drawBitmapAtPosition(canvas, gap * (i - 1) + targetImageSize * (i - 2), t_center, gap * (i - 1) + targetImageSize * (i - 1), t_center +
                                targetImageSize, bitmap)
                    }
                }
            } else if (size == 6) {
                if (i < 3) {
                    drawBitmapAtPosition(canvas, gap * (i + 1) + targetImageSize * i, b_center - targetImageSize, gap * (i + 1) + targetImageSize * (i + 1), b_center, bitmap)
                } else {
                    drawBitmapAtPosition(canvas, gap * (i - 2) + targetImageSize * (i - 3), t_center, gap * (i - 2) + targetImageSize * (i - 2), t_center +
                            targetImageSize, bitmap)
                }
            } else if (size == 7) {
                when (i) {
                    0 -> {
                        drawBitmapAtPosition(canvas, center, gap, center + targetImageSize, gap + targetImageSize, bitmap)
                    }
                    in 1..3 -> {
                        drawBitmapAtPosition(canvas, gap * i + targetImageSize * (i - 1), center, gap * i + targetImageSize * i, center + targetImageSize, bitmap)
                    }
                    else -> {
                        drawBitmapAtPosition(canvas, gap * (i - 3) + targetImageSize * (i - 4), t_center + targetImageSize / 2, gap * (i - 3) + targetImageSize * (i - 3), t_center + targetImageSize / 2 + targetImageSize, bitmap)
                    }
                }
            } else if (size == 8) {
                when (i) {
                    0 -> {
                        drawBitmapAtPosition(canvas, r_center - targetImageSize, gap, r_center, gap + targetImageSize, bitmap)
                    }
                    1 -> {
                        drawBitmapAtPosition(canvas, l_center, gap, l_center + targetImageSize, gap + targetImageSize, bitmap)
                    }
                    in 2..4 -> {
                        drawBitmapAtPosition(canvas, gap * (i - 1) + targetImageSize * (i - 2), center, gap * (i - 1) + targetImageSize * (i - 1), center + targetImageSize, bitmap)
                    }
                    else -> {
                        drawBitmapAtPosition(canvas, gap * (i - 4) + targetImageSize * (i - 5), t_center + targetImageSize / 2, gap * (i - 4) + targetImageSize * (i - 4), t_center + targetImageSize / 2 + targetImageSize, bitmap)
                    }
                }
            } else if (size == 9) {
                drawBitmapAtPosition(canvas, left, top, right, bottom, bitmap)
            }
        }
    }

    /**
     * 根据坐标画图
     *
     * @param canvas
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @param bitmap
     */
    fun drawBitmapAtPosition(canvas: Canvas?, left: Int, top: Int, right: Int, bottom: Int, bitmap: Bitmap?) {
        var bitmapNew = bitmap
        if (null == bitmapNew) { //图片为空用默认图片
            if (multiImageData.defaultImageResId > 0) { //设置过默认id
                bitmapNew = BitmapFactory.decodeResource(mContext.resources, multiImageData.defaultImageResId)
            }
        }
        if (null != bitmapNew) {
            val rect = Rect(left, top, right, bottom)
            canvas?.drawBitmap(bitmapNew, null, rect, null)
        }
    }

    /**
     * 同步加载图片
     *
     * @param imageUrl
     * @param targetImageSize
     * @return
     */
    @Throws(ExecutionException::class, InterruptedException::class)
    private fun asyncLoadImage(imageUrl: String, targetImageSize: Int): Bitmap? {
        return GlideEngine.loadBitmap(imageUrl, multiImageData.defaultImageBitmap, targetImageSize)
    }

    fun load() {
        if (multiImageData.size() == 0) {
            val defaultImageUrl = multiImageData.defaultImageUrl
            imageView.setImageResource(defaultImage)
            if (multiImageData.defaultImageBitmap != null) {
                imageView.setImageBitmap(multiImageData.defaultImageBitmap)
            }
            if (!TextUtils.isEmpty(defaultImageUrl)) { //                new ImageLoader(mContext).loadImage(imageView, defaultImageUrl, R.drawable.default_user_icon, multiImageData.getDefaultImageBitmap());
                GlideEngine.loadImage(imageView, multiImageData.defaultImageBitmap, Uri.parse(defaultImageUrl))
            }
            return
        }
        if (multiImageData.size() == 1) {
            imageView.setImageResource(defaultImage)
            if (multiImageData.defaultImageBitmap != null) {
                imageView.setImageBitmap(multiImageData.defaultImageBitmap)
            }
            var imageUrl: String? = multiImageData.imageUrls!![0]
            val defaultImageUrl = multiImageData.defaultImageUrl
            imageUrl = if (TextUtils.isEmpty(imageUrl)) defaultImageUrl else imageUrl
            //            new ImageLoader(mContext).loadImage(imageView, imageUrl, R.drawable.default_user_icon, multiImageData.getDefaultImageBitmap());
            GlideEngine.loadImage(imageView, multiImageData.defaultImageBitmap, Uri.parse(imageUrl))
            return
        }
        val newTargetID = buildTargetSynthesizedId()
        /*if (loadOk && null != imageView.getDrawable() && TextUtils.equals(currentTargetID, newTargetID)) {
            //两次加载的图片是一样的，而且已经加载成功了，图片没有被回收,此时无需重复加载
            return;
        }*/currentTargetID = newTargetID
        //初始化图片信息
        val gridParam = calculateGridParam(multiImageData.size())
        mRowCount = gridParam[0]
        mColumnCount = gridParam[1]
        targetImageSize = (maxWidth - (mColumnCount + 1) * gap) / if (mColumnCount == 1) 2 else mColumnCount //图片尺寸
        //imageView.setImageResource(multiImageData.getDefaultImageResId());
        object : Thread() {
            override fun run() {
                super.run()
                val targetID = currentTargetID
                //根据id获取存储的文件路径
                val absolutePath = mContext.filesDir.absolutePath
                val file = File(TUIKitConstants.IMAGE_BASE_DIR + currentTargetID)
                var cacheBitmapExists = false
                if (file.exists() && file.isFile) { //文件存在，加载到内存
                    val options = BitmapFactory.Options()
                    options.inJustDecodeBounds = true
                    BitmapFactory.decodeFile(file.path, options)
                    if (options.outWidth > 0 && options.outHeight > 0) { //当前文件是图片
                        cacheBitmapExists = true
                    }
                }
                if (!cacheBitmapExists) { //缓存文件不存在，需要加载读取
                    val loadSuccess = asyncLoadImageList()
                    val bitmap = synthesizeImageList()
                    //保存合成的图片文件
                    if (loadSuccess) { //所有图片加载成功，则保存合成图片
                        storeBitmap(file, bitmap)
                    }
                    //执行回调
//判断当前图片的多个小图是否全部加载完全的，如果加载完全的，complete=true;
                    imageView.post { callback.onCall(bitmap, targetID, loadSuccess) }
                } else {
                    imageView.post { callback.onCall(file, targetID, true) }
                }
            }
        }.start()
    }

    /**
     * 生成合成图片的id，保证唯一性
     */
    fun buildTargetSynthesizedId(): String {
        val size = multiImageData.size()
        val buffer = StringBuffer()
        for (i in 0 until size) {
            val imageUrl = multiImageData.imageUrls!![i]
            buffer.append(i.toString() + imageUrl)
        }
        return getMD5String(buffer.toString())
    }

    interface Callback {
        fun onCall(obj: Any?, targetID: String?, complete: Boolean)
    }
}