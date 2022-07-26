package com.black.base.lib.decode

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Message
import android.os.Parcelable
import com.black.base.R
import com.black.lib.camera.CameraManager
import com.black.lib.view.ViewfinderResultPointCallback
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import java.util.*

/**
 * This class handles all the messaging which comprises the state machine for capture.
 */
class CaptureActivityHandler(private val activity: MipcaActivityCapture, decodeFormats: Vector<BarcodeFormat?>?,
                             characterSet: String?) : Handler() {
    companion object {
        private val TAG = CaptureActivityHandler::class.java.simpleName
    }

    private val decodeThread: DecodeThread = DecodeThread(activity, decodeFormats, characterSet,
            ViewfinderResultPointCallback(activity.viewfinderView!!))
    private var state: State

    private enum class State {
        PREVIEW, SUCCESS, DONE
    }

    init {
        decodeThread.start()
        state = State.SUCCESS
        // Start ourselves capturing previews and decoding.
        CameraManager.get()?.startPreview()
        restartPreviewAndDecode()
    }

    override fun handleMessage(message: Message) {
        if (message.what == R.id.auto_focus) { ////Log.d(TAG, "Got auto-focus message");
// When one auto focus pass finishes, start another. This is the closest thing to
// continuous AF. It does seem to hunt a bit, but I'm not sure what else to do.
            if (state == State.PREVIEW) {
                CameraManager.get()?.requestAutoFocus(this, R.id.auto_focus)
            }
        } else if (message.what == R.id.restart_preview) {
            restartPreviewAndDecode()
        } else if (message.what == R.id.decode_succeeded) {
            state = State.SUCCESS
            val bundle = message.data
            /** */
            val barcode = if (bundle == null) null else bundle.getParcelable<Parcelable>(DecodeThread.BARCODE_BITMAP) as Bitmap //���ñ����߳�
            activity.handleDecode((message.obj as Result), barcode) //���ؽ��?        /***********************************************************************/
        } else if (message.what == R.id.decode_failed) { // We're decoding as fast as possible, so when one decode fails, start another.
            state = State.PREVIEW
            CameraManager.get()?.requestPreviewFrame(decodeThread.getHandler(), R.id.decode)
        } else if (message.what == R.id.return_scan_result) {
            activity.setResult(Activity.RESULT_OK, message.obj as Intent)
            activity.finish()
        } else if (message.what == R.id.launch_product_query) {
            val url = message.obj as String
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
            activity.startActivity(intent)
        }
    }

    fun quitSynchronously() {
        state = State.DONE
        CameraManager.get()?.stopPreview()
        val quit = Message.obtain(decodeThread.getHandler(), R.id.quit)
        quit.sendToTarget()
        try {
            decodeThread.join()
        } catch (e: InterruptedException) { // continue
        }
        // Be absolutely sure we don't send any queued up messages
        removeMessages(R.id.decode_succeeded)
        removeMessages(R.id.decode_failed)
    }

    private fun restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW
            CameraManager.get()?.requestPreviewFrame(decodeThread.getHandler(), R.id.decode)
            CameraManager.get()?.requestAutoFocus(this, R.id.auto_focus)
            activity.drawViewfinder()
        }
    }
}
