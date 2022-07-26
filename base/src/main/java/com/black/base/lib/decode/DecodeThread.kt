package com.black.base.lib.decode

import android.os.Handler
import android.os.Looper
import com.google.zxing.BarcodeFormat
import com.google.zxing.DecodeHintType
import com.google.zxing.ResultPointCallback
import java.util.*
import java.util.concurrent.CountDownLatch

/**
 * This thread does all the heavy lifting of decoding the images.
 * �����߳�
 */
internal class DecodeThread(activity: MipcaActivityCapture,
                            decodeFormats: Vector<BarcodeFormat?>?,
                            characterSet: String?,
                            resultPointCallback: ResultPointCallback?) : Thread() {
    companion object {
        const val BARCODE_BITMAP = "barcode_bitmap"
    }

    private val activity: MipcaActivityCapture
    private val hints: Hashtable<DecodeHintType?, Any?>
    private var handler: Handler? = null
    private val handlerInitLatch: CountDownLatch

    init {
        var formats = decodeFormats
        this.activity = activity
        handlerInitLatch = CountDownLatch(1)
        hints = Hashtable(3)
        if (formats == null || formats.isEmpty()) {
            formats = Vector()
            formats.addAll(DecodeFormatManager.ONE_D_FORMATS)
            formats.addAll(DecodeFormatManager.QR_CODE_FORMATS)
            formats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS)
        }
        hints[DecodeHintType.POSSIBLE_FORMATS] = formats
        if (characterSet != null) {
            hints[DecodeHintType.CHARACTER_SET] = characterSet
        }
        hints[DecodeHintType.NEED_RESULT_POINT_CALLBACK] = resultPointCallback
    }

    fun getHandler(): Handler? {
        try {
            handlerInitLatch.await()
        } catch (ie: InterruptedException) { // continue?
        }
        return handler
    }

    override fun run() {
        Looper.prepare()
        //        handler = new DecodeHandler(activity, hints);
        handler = QRCodeDecodeHandler(activity, hints)
        handlerInitLatch.countDown()
        Looper.loop()
    }
}