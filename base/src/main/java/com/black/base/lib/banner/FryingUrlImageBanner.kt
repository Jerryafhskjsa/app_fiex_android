package com.black.base.lib.banner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.viewpager.widget.ViewPager
import com.black.base.R
import com.black.base.model.clutter.Banner
import com.black.base.service.DownloadServiceHelper
import com.black.base.util.ImageLoader
import com.black.lib.banner.BannerTransformer
import com.black.lib.banner.LoopBannerAdapterWrapper
import com.black.util.Callback
import skin.support.content.res.SkinCompatResources
import java.lang.ref.SoftReference
import java.util.*

class FryingUrlImageBanner(context: Context) : FryingBanner<Banner?>(context) {
    companion object {
        private const val TAG = "FryingUrlImageBanner"
    }

    private val bannerCache: MutableMap<String, SoftReference<Bitmap?>> = HashMap()
    private val downloadImageStatus: MutableMap<String, Boolean> = HashMap()
    private val colorDrawable: ColorDrawable
    private val imageLoader: ImageLoader
    private var mPreviousOffset = -1f
    private var adapterWrapper: LoopBannerAdapterWrapper? = null

    init {
        //        new CoverFlow.Builder()
        //                .with(pager)
        //                .scale(0.1f)
        //                .pagerMargin(0f)
        //                .spaceSize(0f)
        //                .build();
        val width = dm.widthPixels - 32 * dm.density
        val height = width * 0.393f
        var containerParams = pagerContainer.layoutParams
        if (containerParams == null) {
            containerParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height.toInt())
        } else {
            containerParams.height = height.toInt()
        }
        pagerContainer.layoutParams = containerParams
        var params = pager.layoutParams as FrameLayout.LayoutParams?
        if (params == null) {
            params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        params.leftMargin = (12.0 * dm.density).toInt()
        params.rightMargin = (12.0 * dm.density).toInt()
        params.height = height.toInt()
        pager.layoutParams = params
        pager.setPageTransformer(false, BannerTransformer(0f, 15f / 135))
        colorDrawable = ColorDrawable(Color.parseColor("#555555"))
        imageLoader = ImageLoader(context)
        //        setScale(0.36f);
    }

    override fun setScale(scale: Float): FryingBanner<*> {
        return this
    }

    override fun onCreateItemView(model: Banner?): View {
        val inflate = inflater.inflate(R.layout.adapter_simple_image_padding, null)
        val iv = inflate.findViewById<ImageView>(R.id.iv)
        var itemWidth = pager.width
        if (itemWidth == 0) {
            itemWidth = (dm.widthPixels - 32 * dm.density).toInt()
        }
        //        int itemHeight = (int) (itemWidth * 360 * 1.0f / 640);
        //        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.layoutParams = LinearLayout.LayoutParams(itemWidth, LinearLayout.LayoutParams.WRAP_CONTENT)
        putBitmap(model, iv)
        //        imageLoader.loadImage(iv, model.imageUrl);
        return inflate
    }

    override fun setData(data: List<Banner?>?) {
        super.setData(data)
        adapterWrapper = LoopBannerAdapterWrapper(adapter)
        adapterWrapper!!.setBoundaryCaching(true)
        pager.offscreenPageLimit = adapterWrapper!!.count
        pager.adapter = adapterWrapper
        pager.currentItem = 2
        selectPoint(getRealPosition(2))
    }

    override fun addPoints(count: Int) {
        for (i in 0 until count) {
            val pointView = ImageView(context)
            pointView.setImageDrawable(SkinCompatResources.getDrawable(context, R.drawable.banner_point_def))
            val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            //点的间隔
            layoutParams.leftMargin = (dm.density * 5).toInt()
            pointView.layoutParams = layoutParams
            //把点添加到容器中
//            indicator.addView(pointView);
        }
    }

    override fun onPointSelected(pointView: ImageView?) {
        pointView!!.setImageResource(R.drawable.banner_point_select)
    }

    override fun onPointUnselected(pointView: ImageView?) {
        pointView!!.setImageResource(R.drawable.banner_point_def)
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        val realPosition = adapterWrapper!!.toRealPosition(position)
        if (positionOffset == 0f && mPreviousOffset == 0f && (position <= 1 || position >= adapterWrapper!!.count - 2)) {
            pager.setCurrentItem(realPosition + 2, false)
        }
        mPreviousOffset = positionOffset
    }

    override fun onPageScrollStateChanged(state: Int) {
        if (adapterWrapper != null) {
            val position = pager.currentItem
            val realPosition = adapterWrapper!!.toRealPosition(position)
            if (state == ViewPager.SCROLL_STATE_IDLE && (position <= 1 || position >= adapterWrapper!!.count - 2)) {
                pager.setCurrentItem(realPosition + 2, false)
            }
        }
    }

    override fun onPageSelected(position: Int) {
        selectPoint(getRealPosition(position))
    }

    private fun getRealPosition(position: Int): Int {
        return if (adapterWrapper == null) position else adapterWrapper!!.toRealPosition(position)
    }

    private fun getCacheBitmap(key: String): Bitmap? {
        val softReference = bannerCache[key]
        return softReference?.get()
    }

    fun putBitmap(item: Banner?, iv: ImageView) {
        val imageUrl = item?.imageUrl
        if (imageUrl != null) {
            val cachedBitmap = getCacheBitmap(imageUrl)
            if (cachedBitmap != null) {
                iv.setImageBitmap(cachedBitmap)
            } else {
                val key = iv.toString() + imageUrl
                val isDownloading = if (downloadImageStatus[key] == null) false else downloadImageStatus[key]!!
                if (!isDownloading) {
                    downloadImageStatus[key] = true
                    DownloadServiceHelper.downloadImage(context, imageUrl, false, object : Callback<Bitmap?>() {
                        override fun error(type: Int, error: Any) {
                            putBitmap(item, iv)
                            downloadImageStatus[key] = false
                        }

                        override fun callback(returnData: Bitmap?) {
                            try {
                                iv.setImageBitmap(returnData)
                                bannerCache[key] = SoftReference(returnData)
                            } catch (e: Exception) {
                                putBitmap(item, iv)
                            }
                            downloadImageStatus[imageUrl] = false
                        }
                    })
                }
            }
        } else {
            iv.setImageDrawable(colorDrawable)
        }
    }
}