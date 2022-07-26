package com.black.im.video.state

import android.view.Surface
import android.view.SurfaceHolder
import com.black.im.util.TUIKitLog
import com.black.im.video.CameraInterface
import com.black.im.video.JCameraView

class BorrowPictureState(private val machine: CameraMachine) : State {
    companion object {
        private val TAG = BorrowPictureState::class.java.simpleName
    }

    override fun start(holder: SurfaceHolder?, screenProp: Float) {
        CameraInterface.instance.doStartPreview(holder, screenProp)
        machine.state = machine.previewState
    }

    override fun stop() {}
    override fun foucs(x: Float, y: Float, callback: CameraInterface.FocusCallback) {}
    override fun swtich(holder: SurfaceHolder?, screenProp: Float) {}
    override fun restart() {}
    override fun capture() {}
    override fun record(surface: Surface?, screenProp: Float) {}
    override fun stopRecord(isShort: Boolean, time: Long) {}
    override fun cancle(holder: SurfaceHolder?, screenProp: Float) {
        CameraInterface.instance.doStartPreview(holder, screenProp)
        machine.view.resetState(JCameraView.TYPE_PICTURE)
        machine.state = machine.previewState
    }

    override fun confirm() {
        machine.view.confirmState(JCameraView.TYPE_PICTURE)
        machine.state = machine.previewState
    }

    override fun zoom(zoom: Float, type: Int) {
        TUIKitLog.i(TAG, "zoom")
    }

    override fun flash(mode: String?) {}
}