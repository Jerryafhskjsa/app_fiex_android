package com.black.base.service

import android.content.Context
import android.graphics.Bitmap
import com.black.base.manager.ApiManager
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.ConstData
import com.black.lib.permission.PermissionHelper
import com.black.net.DownloadListener
import com.black.net.DownloadManager
import com.black.util.Callback
import com.black.util.ImageUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File

object DownloadServiceHelper {
    fun downloadImage(context: Context?, url: String?, showLoading: Boolean, callback: Callback<Bitmap?>?) {
        if (context == null || url == null || callback == null) {
            return
        }
        ApiManager.build(context, true).getService(DownloadService::class.java)
                ?.downLoad(url)
                ?.subscribeOn(Schedulers.io()) //在新线程中实现该方法
                ?.map { responseBody -> ImageUtil.getBitmapBySteam(context, responseBody.byteStream()) }
                ?.observeOn(AndroidSchedulers.mainThread()) //在Android主线程中展示
                ?.subscribe(HttpCallbackSimple(context, showLoading, callback))
    }

    fun downloadImage(context: Context?, url: String?, maxWidth: Int, maxHeight: Int, showLoading: Boolean, callback: Callback<Bitmap?>?) {
        if (context == null || url == null || callback == null || maxWidth < 1 || maxHeight < 1) {
            return
        }
        ApiManager.build(context, true).getService(DownloadService::class.java)
                ?.downLoad(url)
                ?.subscribeOn(Schedulers.io()) //在新线程中实现该方法
                ?.map { responseBody -> ImageUtil.getBitmapBySteam(responseBody.byteStream(), maxWidth, maxHeight) }
                ?.observeOn(AndroidSchedulers.mainThread()) //在Android主线程中展示
                ?.subscribe(HttpCallbackSimple(context, showLoading, callback))
    }

    fun downloadImage(context: Context?, url: String?, maxLength: Long, showLoading: Boolean, callback: Callback<Bitmap?>?) {
        if (context == null || url == null || callback == null || maxLength < 1) {
            return
        }
        ApiManager.build(context, true).getService(DownloadService::class.java)
                ?.downLoad(url)
                ?.subscribeOn(Schedulers.io()) //在新线程中实现该方法
                ?.map { responseBody -> ImageUtil.getBitmapBySteam(responseBody.byteStream(), maxLength) }
                ?.observeOn(AndroidSchedulers.mainThread()) //在Android主线程中展示
                ?.subscribe(HttpCallbackSimple(context, showLoading, callback))
    }

    fun downloadFile(context: Context?, cachePath: String?, url: String?, file: File?, downloadCallback: DownloadListener?) {
        if (context == null || url == null) {
            return
        }
        val downloadCommand = Runnable { DownloadManager.getInstance(cachePath).download(url, file, downloadCallback) }
        if (context is PermissionHelper) {
            (context as PermissionHelper).requestStoragePermissions(downloadCommand)
        }
    }

    fun cancelDownload(url: String?, cachePath: String?) {
        if (url == null) {
            return
        }
        DownloadManager.getInstance(cachePath).cancel(url)
    }

    fun clearDownload() {
        DownloadManager.getInstance(ConstData.CACHE_PATH).clear()
    }
}
