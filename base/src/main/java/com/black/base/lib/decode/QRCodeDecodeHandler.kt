package com.black.base.lib.decode

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.black.base.R
import com.black.lib.camera.CameraManager
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import java.lang.Boolean
import java.util.*

/**
 * 二维码是扫描
 */
internal class QRCodeDecodeHandler(activity: MipcaActivityCapture, hints: Hashtable<DecodeHintType?, Any?>?) : Handler() {
    companion object {
        private val TAG = QRCodeDecodeHandler::class.java.simpleName
    }

    private val activity: MipcaActivityCapture
    private val reader: Reader
    private val mHints: MutableMap<DecodeHintType, Any?>

    init {
        reader = QRCodeReader()
        this.activity = activity
        mHints = Hashtable()
        mHints[DecodeHintType.CHARACTER_SET] = "utf-8"
        mHints[DecodeHintType.TRY_HARDER] = Boolean.TRUE
        mHints[DecodeHintType.POSSIBLE_FORMATS] = BarcodeFormat.QR_CODE
    }

    override fun handleMessage(message: Message) {
        if (message.what == R.id.decode) { ////Log.d(TAG, "Got decode message");
            decode(message.obj as ByteArray, message.arg1, message.arg2)
        } else if (message.what == R.id.quit) {
            Looper.myLooper()?.quit()
        }
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
     * reuse the same reader objects from one decode to the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private fun decode(data: ByteArray, width: Int, height: Int) {
        var width1 = width
        var height1 = height
        val start = System.currentTimeMillis()
        var rawResult: Result? = null
        //modify here
        val rotatedData = ByteArray(data.size)
        for (y in 0 until height1) {
            for (x in 0 until width1) rotatedData[x * height1 + height1 - y - 1] = data[x + y * width1]
        }
        val tmp = width1 // Here we are swapping, that's the difference to #11
        width1 = height1
        height1 = tmp
        val source = CameraManager.get()!!.buildLuminanceSource(rotatedData, width1, height1)
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        try {
            rawResult = reader.decode(bitmap, mHints)
        } catch (re: ReaderException) { // continue
        } finally {
            reader.reset()
        }
        if (rawResult != null) {
            val end = System.currentTimeMillis()
            val message = Message.obtain(activity.getHandler(), R.id.decode_succeeded, rawResult)
            val bundle = Bundle()
            bundle.putParcelable(DecodeThread.BARCODE_BITMAP, source.renderCroppedGreyscaleBitmap())
            message.data = bundle
            ////Log.d(TAG, "Sending decode succeeded message...");
            message.sendToTarget()
        } else {
            val message = Message.obtain(activity.getHandler(), R.id.decode_failed)
            message.sendToTarget()
        }
    }
}
