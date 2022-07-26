package com.black.im.video.state

import android.graphics.Bitmap
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import com.black.im.util.TUIKitLog
import com.black.im.video.CameraInterface
import com.black.im.video.JCameraView

/**
 * 空闲状态
 */
internal class PreviewState(private val machine: CameraMachine) : State {
    override fun start(holder: SurfaceHolder?, screenProp: Float) {
        CameraInterface.instance.doStartPreview(holder, screenProp)
    }

    override fun stop() {
        CameraInterface.instance.doStopPreview()
    }

    override fun foucs(x: Float, y: Float, callback: CameraInterface.FocusCallback) {
        TUIKitLog.i(TAG, "preview state foucs")
        if (machine.view.handlerFoucs(x, y)) {
            CameraInterface.instance.handleFocus(machine.context, x, y, callback)
        }
    }

    override fun swtich(holder: SurfaceHolder?, screenProp: Float) {
        CameraInterface.instance.switchCamera(holder, screenProp)
    }

    override fun restart() {}
    override fun capture() {
        CameraInterface.instance.takePicture(object : CameraInterface.TakePictureCallback {
            override fun captureResult(bitmap: Bitmap?, isVertical: Boolean) {
                bitmap?.let {
                    machine.view.showPicture(bitmap, isVertical)
                    machine.state = machine.borrowPictureState
                    TUIKitLog.i(TAG, "capture")
                }
            }
        })
    }

    override fun record(surface: Surface?, screenProp: Float) {
        CameraInterface.instance.startRecord(surface, screenProp, null)
    }

    override fun stopRecord(isShort: Boolean, time: Long) {
        CameraInterface.instance.stopRecord(isShort, object : CameraInterface.StopRecordCallback {
            override fun recordResult(url: String?, firstFrame: Bitmap?) {
                if (isShort) {
                    machine.view.resetState(JCameraView.TYPE_SHORT)
                } else {
                    if (url != null && firstFrame != null) {
                        machine.view.playVideo(firstFrame, url)
                    }
                    machine.state = machine.borrowVideoState
                }
            }
        })
    }

    override fun cancle(holder: SurfaceHolder?, screenProp: Float) {
        TUIKitLog.i(TAG, "浏览状态下,没有 cancle 事件")
    }

    override fun confirm() {
        TUIKitLog.i(TAG, "浏览状态下,没有 confirm 事件")
    }

    override fun zoom(zoom: Float, type: Int) {
        TUIKitLog.i(TAG, "zoom")
        CameraInterface.instance.setZoom(zoom, type)
    }

    override fun flash(mode: String?) {
        CameraInterface.instance.setFlashMode(mode)
    }

    companion object {
        const val TAG = "PreviewState"
    }

}