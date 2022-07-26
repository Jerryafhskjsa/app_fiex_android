package com.black.base.lib.banner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.viewpager.widget.ViewPager
import com.black.base.R
import com.black.base.model.clutter.Banner
import com.black.base.util.ImageLoader
import com.black.lib.banner.LoopBannerAdapterWrapper
import com.black.util.Callback
import skin.support.content.res.SkinCompatResources
import java.lang.ref.SoftReference
import java.util.*

class FryingUrlImageNormalBanner(context: Context) : FryingBanner<Banner?>(context) {
    companion object {
        private const val TAG = "FryingUrlImageBanner"
    }

    private val bannerCache: Map<String, SoftReference<Bitmap>> = HashMap()
    private val downloadImageStatus: MutableMap<String, Boolean> = HashMap()
    private val colorDrawable: ColorDrawable = ColorDrawable(Color.parseColor("#555555"))
    private val imageLoader: ImageLoader = ImageLoader(context)
    private var adapterWrapper: LoopBannerAdapterWrapper? = null
    private var mPreviousOffset = -1f

    override fun onCreateItemView(model: Banner?): View {
        val inflate = inflater.inflate(R.layout.adapter_simple_image, null)
        val iv = inflate.findViewById<ImageView>(R.id.iv)
        putBitmap(model, iv)
        //        imageLoader.loadImage(iv, model.imageUrl);
        return inflate
    }

    override fun setData(data: List<Banner?>?) {
        super.setData(data)
        if (data == null || data.isEmpty()) {
            return
        }
        if (data.size > 1) {
            adapterWrapper = LoopBannerAdapterWrapper(adapter)
            adapterWrapper!!.setBoundaryCaching(true)
            pager.offscreenPageLimit = adapterWrapper!!.count
            pager.adapter = adapterWrapper
            pager.currentItem = 2
            selectPoint(getRealPosition(2))
        } else {
            pager.offscreenPageLimit = adapter.count
            pager.adapter = adapter
            pager.currentItem = 0
            selectPoint(getRealPosition(0))
        }
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
        if (adapterWrapper != null) {
            val realPosition = adapterWrapper!!.toRealPosition(position)
            if (positionOffset == 0f && mPreviousOffset == 0f && (position <= 1 || position >= adapterWrapper!!.count - 2)) {
                pager.setCurrentItem(realPosition + 2, false)
            }
            mPreviousOffset = positionOffset
        }
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

    //    private Bitmap getCacheBitmap(String key) {
//        SoftReference<Bitmap> softReference = bannerCache.get(key);
//        return softReference == null ? null : softReference.get();
//    }
    private fun putBitmap(item: Banner?, iv: ImageView) {
        if (item?.imageUrl != null) {
            val key = iv.toString() + item.imageUrl
            val isDownloading = if (downloadImageStatus[key] == null) false else downloadImageStatus[key]!!
            if (!isDownloading) {
                downloadImageStatus[key] = true
                imageLoader.getBitmap(item.imageUrl, width, height, object : Callback<Bitmap?>() {
                    override fun error(type: Int, error: Any) {
                        putBitmap(item, iv)
                        downloadImageStatus[key] = false
                    }

                    override fun callback(returnData: Bitmap?) {
                        try {
                            iv.setImageBitmap(returnData)
                        } catch (e: Exception) {
                            putBitmap(item, iv)
                        }
                        downloadImageStatus[key] = false
                    }
                })
            }
//            Bitmap cachedBitmap = getCacheBitmap(item.imageUrl);
//            if (cachedBitmap != null) {
//                iv.setImageBitmap(cachedBitmap);
//            } else {
//                String key = iv + item.imageUrl;
//                boolean isDownloading = downloadImageStatus.get(key) == null ? false : downloadImageStatus.get(key);
//                DownloadServiceHelper.downloadImage(context, item.imageUrl, false, new Callback<Bitmap>() {
//                    @Override
//                    public void error(int type, Object error) {
//                        putBitmap(item, iv);
//                        downloadImageStatus.put(key, false);
//                    }
//
//                    @Override
//                    public void callback(Bitmap returnData) {
//                        try {
//                            iv.setImageBitmap(returnData);
//                            bannerCache.put(key, new SoftReference<>(returnData));
//                        } catch (Exception e) {
//                            putBitmap(item, iv);
//                        }
//                        downloadImageStatus.put(key, false);
//                    }
//                });
//            }
        } else {
            iv.setImageDrawable(colorDrawable)
        }
    }
}