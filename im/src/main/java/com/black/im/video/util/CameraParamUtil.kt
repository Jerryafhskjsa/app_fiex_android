package com.black.im.video.util

import android.content.Context
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.view.Surface
import android.view.WindowManager
import com.black.im.util.TUIKitLog
import java.util.*

class CameraParamUtil private constructor() {
    companion object {
        private val TAG: String = CameraParamUtil::class.java.getSimpleName()
        private var cameraParamUtil: CameraParamUtil? = null
        val instance: CameraParamUtil
            get() = if (cameraParamUtil == null) {
                cameraParamUtil = CameraParamUtil()
                cameraParamUtil!!
            } else {
                cameraParamUtil!!
            }
    }

    private val sizeComparator = CameraSizeComparator()
    fun getPreviewSize(list: List<Camera.Size>, th: Int, rate: Float): Camera.Size {
        Collections.sort(list, sizeComparator)
        var i = 0
        for (s in list) {
            if (s.width > th && equalRate(s, rate)) {
                TUIKitLog.i(TAG, "MakeSure Preview :w = " + s.width + " h = " + s.height)
                break
            }
            i++
        }
        return if (i == list.size) {
            getBestSize(list, rate)
        } else {
            list[i]
        }
    }

    fun getPictureSize(list: List<Camera.Size>, th: Int, rate: Float): Camera.Size {
        Collections.sort(list, sizeComparator)
        var i = 0
        for (s in list) {
            if (s.width > th && equalRate(s, rate)) {
                TUIKitLog.i(TAG, "MakeSure Picture :w = " + s.width + " h = " + s.height)
                break
            }
            i++
        }
        return if (i == list.size) {
            getBestSize(list, rate)
        } else {
            list[i]
        }
    }

    private fun getBestSize(list: List<Camera.Size>, rate: Float): Camera.Size {
        var previewDisparity = 100f
        var index = 0
        for (i in list.indices) {
            val cur = list[i]
            val prop = cur.width.toFloat() / cur.height.toFloat()
            if (Math.abs(rate - prop) < previewDisparity) {
                previewDisparity = Math.abs(rate - prop)
                index = i
            }
        }
        return list[index]
    }

    private fun equalRate(s: Camera.Size, rate: Float): Boolean {
        val r = s.width.toFloat() / s.height.toFloat()
        return Math.abs(r - rate) <= 0.2
    }

    fun isSupportedFocusMode(focusList: List<String>, focusMode: String): Boolean {
        for (i in focusList.indices) {
            if (focusMode == focusList[i]) {
                TUIKitLog.i(TAG, "FocusMode supported $focusMode")
                return true
            }
        }
        TUIKitLog.i(TAG, "FocusMode not supported $focusMode")
        return false
    }

    fun isSupportedPictureFormats(supportedPictureFormats: List<Int>, jpeg: Int): Boolean {
        for (i in supportedPictureFormats.indices) {
            if (jpeg == supportedPictureFormats[i]) {
                TUIKitLog.i(TAG, "Formats supported $jpeg")
                return true
            }
        }
        TUIKitLog.i(TAG, "Formats not supported $jpeg")
        return false
    }

    fun getCameraDisplayOrientation(context: Context, cameraId: Int): Int {
        val info = CameraInfo()
        Camera.getCameraInfo(cameraId, info)
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val rotation = wm.defaultDisplay.rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        var result: Int
        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360 // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360
        }
        return result
    }

    private inner class CameraSizeComparator : Comparator<Camera.Size> {
        override fun compare(lhs: Camera.Size, rhs: Camera.Size): Int {
            return if (lhs.width == rhs.width) {
                0
            } else if (lhs.width > rhs.width) {
                1
            } else {
                -1
            }
        }
    }
}