package com.black.lib.camera

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.hardware.Camera
import android.os.Build
import android.os.Handler
import android.view.SurfaceHolder
import java.io.IOException

/**
 * This object wraps the Camera service object and expects to be the only one talking to it. The
 * implementation encapsulates the steps needed to take preview-sized images, which are used for
 * both preview and decoding.
 */
class CameraManager private constructor(val context: Context) {
    companion object {
        private val TAG = CameraManager::class.java.simpleName
        private const val MIN_FRAME_WIDTH = 240
        private const val MIN_FRAME_HEIGHT = 240
        private const val MAX_FRAME_WIDTH = 480
        private const val MAX_FRAME_HEIGHT = 360
        private var cameraManager: CameraManager? = null
        var SDK_INT // Later we can use Build.VERSION.SDK_INT
                = 0

        /**
         * Initializes this static object with the Context of the calling Activity.
         *
         * @param context The Activity which wants to use the camera.
         */
        fun init(context: Context) {
            if (cameraManager == null) {
                cameraManager = CameraManager(context)
            }
        }

        /**
         * Gets the CameraManager singleton instance.
         *
         * @return A reference to the CameraManager singleton.
         */
        fun get(): CameraManager? {
            return cameraManager
        }

        init {
            val sdkInt: Int = try {
                Build.VERSION.SDK.toInt()
            } catch (nfe: NumberFormatException) { // Just to be safe
                10000
            }
            SDK_INT = sdkInt
        }
    }

    private val configManager: CameraConfigurationManager = CameraConfigurationManager(context)
    private var camera: Camera? = null
    //      int width = screenResolution.x * 3 / 4;
    //      if (width < MIN_FRAME_WIDTH) {
    //        width = MIN_FRAME_WIDTH;
    //      } else if (width > MAX_FRAME_WIDTH) {
    //        width = MAX_FRAME_WIDTH;
    //      }
    //      int height = screenResolution.y * 3 / 4;
    //      if (height < MIN_FRAME_HEIGHT) {
    //        height = MIN_FRAME_HEIGHT;
    //      } else if (height > MAX_FRAME_HEIGHT) {
    //        height = MAX_FRAME_HEIGHT;
    //      }
    /**
     * Calculates the framing rect which the UI should draw to show the user where to place the
     * barcode. This target helps with alignment as well as forces the user to hold the device
     * far enough away to ensure the image will be in focus.
     *
     * @return The rectangle to draw on screen in window coordinates.
     */
    var framingRect: Rect? = null
        get() {
            val screenResolution = configManager.screenResolution
            if (field == null) {
                if (camera == null) {
                    return null
                }
                val density = context.resources.displayMetrics.density
                val width = (240 * density).toInt()
                //      int width = screenResolution.x * 3 / 4;
                //      if (width < MIN_FRAME_WIDTH) {
                //        width = MIN_FRAME_WIDTH;
                //      } else if (width > MAX_FRAME_WIDTH) {
                //        width = MAX_FRAME_WIDTH;
                //      }
                val height = (200 * density).toInt()
                //      int height = screenResolution.y * 3 / 4;
                //      if (height < MIN_FRAME_HEIGHT) {
                //        height = MIN_FRAME_HEIGHT;
                //      } else if (height > MAX_FRAME_HEIGHT) {
                //        height = MAX_FRAME_HEIGHT;
                //      }
                val leftOffset = ((screenResolution?.x ?: 0) - width) / 2
                val topOffset = ((screenResolution?.y ?: 0) - height) / 4
                field = Rect(leftOffset, topOffset, leftOffset + width, topOffset + height)
            }
            return field
        }
        private set
    //modify here
    //      rect.left = rect.left * cameraResolution.x / screenResolution.x;
    //      rect.right = rect.right * cameraResolution.x / screenResolution.x;
    //      rect.top = rect.top * cameraResolution.y / screenResolution.y;
    //      rect.bottom = rect.bottom * cameraResolution.y / screenResolution.y;
    /**
     * Like [.getFramingRect] but coordinates are in terms of the preview frame,
     * not UI / screen.
     */
    private var framingRectInPreview: Rect? = null
        get() {
            if (field == null) {
                val rect = Rect(framingRect)
                val cameraResolution = configManager.cameraResolution
                val screenResolution = configManager.screenResolution
                //modify here
                //      rect.left = rect.left * cameraResolution.x / screenResolution.x;
                //      rect.right = rect.right * cameraResolution.x / screenResolution.x;
                //      rect.top = rect.top * cameraResolution.y / screenResolution.y;
                //      rect.bottom = rect.bottom * cameraResolution.y / screenResolution.y;
                rect.left = rect.left * cameraResolution.y / screenResolution.x
                rect.right = rect.right * cameraResolution.y / screenResolution.x
                rect.top = rect.top * cameraResolution.x / screenResolution.y
                rect.bottom = rect.bottom * cameraResolution.x / screenResolution.y
                field = rect
            }
            return field
        }
        private set
    private var initialized = false
    private var previewing = false
    private val useOneShotPreviewCallback: Boolean
    /**
     * Preview frames are delivered here, which we pass on to the registered handler. Make sure to
     * clear the handler so it will only receive one message.
     */
    private val previewCallback: PreviewCallback
    /**
     * Autofocus callbacks arrive here, and are dispatched to the Handler which requested them.
     */
    private val autoFocusCallback: AutoFocusCallback

    init {
        // Camera.setOneShotPreviewCallback() has a race condition in Cupcake, so we use the older
        // Camera.setPreviewCallback() on 1.5 and earlier. For Donut and later, we need to use
        // the more efficient one shot callback, as the older one can swamp the system and cause it
        // to run out of memory. We can't use SDK_INT because it was introduced in the Donut SDK.
        //useOneShotPreviewCallback = Integer.parseInt(Build.VERSION.SDK) > Build.VERSION_CODES.CUPCAKE;
        useOneShotPreviewCallback = Build.VERSION.SDK.toInt() > 3 // 3 = Cupcake
        previewCallback = PreviewCallback(configManager, useOneShotPreviewCallback)
        autoFocusCallback = AutoFocusCallback()
    }

    /**
     * Opens the camera driver and initializes the hardware parameters.
     *
     * @param holder The surface object which the camera will draw preview frames into.
     * @throws IOException Indicates the camera driver failed to open.
     */
    @Throws(IOException::class)
    fun openDriver(holder: SurfaceHolder?) {
        if (camera == null) {
            camera = Camera.open()
            if (camera == null) {
                throw IOException()
            }
            camera!!.setPreviewDisplay(holder)
            if (!initialized) {
                initialized = true
                configManager.initFromCameraParameters(camera!!)
            }
            configManager.setDesiredCameraParameters(camera!!)
            //FIXME
            //     SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            //�Ƿ�ʹ��ǰ��
            //      if (prefs.getBoolean(PreferencesActivity.KEY_FRONT_LIGHT, false)) {
            //        FlashlightManager.enableFlashlight();
            //      }
            FlashlightManager.enableFlashlight()
        }
    }

    /**
     * Closes the camera driver if still in use.
     */
    fun closeDriver() {
        if (camera != null) {
            FlashlightManager.disableFlashlight()
            camera!!.release()
            camera = null
        }
    }

    /**
     * Asks the camera hardware to begin drawing preview frames to the screen.
     */
    fun startPreview() {
        if (camera != null && !previewing) {
            camera!!.startPreview()
            previewing = true
        }
    }

    /**
     * Tells the camera to stop drawing preview frames.
     */
    fun stopPreview() {
        if (camera != null && previewing) {
            if (!useOneShotPreviewCallback) {
                camera!!.setPreviewCallback(null)
            }
            camera!!.stopPreview()
            previewCallback.setHandler(null, 0)
            autoFocusCallback.setHandler(null, 0)
            previewing = false
        }
    }

    /**
     * A single preview frame will be returned to the handler supplied. The data will arrive as byte[]
     * in the message.obj field, with width and height encoded as message.arg1 and message.arg2,
     * respectively.
     *
     * @param handler The handler to send the message to.
     * @param message The what field of the message to be sent.
     */
    fun requestPreviewFrame(handler: Handler?, message: Int) {
        if (camera != null && previewing) {
            previewCallback.setHandler(handler, message)
            if (useOneShotPreviewCallback) {
                camera!!.setOneShotPreviewCallback(previewCallback)
            } else {
                camera!!.setPreviewCallback(previewCallback)
            }
        }
    }

    /**
     * Asks the camera hardware to perform an autofocus.
     *
     * @param handler The Handler to notify when the autofocus completes.
     * @param message The message to deliver.
     */
    fun requestAutoFocus(handler: Handler?, message: Int) {
        if (camera != null && previewing) {
            autoFocusCallback.setHandler(handler, message)
            ////Log.d(TAG, "Requesting auto-focus callback");
            camera!!.autoFocus(autoFocusCallback)
        }
    }

    /**
     * Converts the result points from still resolution coordinates to screen coordinates.
     *
     * @param points The points returned by the Reader subclass through Result.getResultPoints().
     * @return An array of Points scaled to the size of the framing rect and offset appropriately
     * so they can be drawn in screen coordinates.
     */
    /*
      public Point[] convertResultPoints(ResultPoint[] points) {
        Rect frame = getFramingRectInPreview();
        int count = points.length;
        Point[] output = new Point[count];
        for (int x = 0; x < count; x++) {
          output[x] = new Point();
          output[x].x = frame.left + (int) (points[x].getX() + 0.5f);
          output[x].y = frame.top + (int) (points[x].getY() + 0.5f);
        }
        return output;
      }
    */
    /**
     * A factory method to build the appropriate LuminanceSource object based on the format
     * of the preview buffers, as described by Camera.Parameters.
     *
     * @param data   A preview frame.
     * @param width  The width of the image.
     * @param height The height of the image.
     * @return A PlanarYUVLuminanceSource instance.
     */
    fun buildLuminanceSource(data: ByteArray, width: Int, height: Int): PlanarYUVLuminanceSource {
        val rect = framingRectInPreview
        val previewFormat = configManager.previewFormat
        val previewFormatString = configManager.previewFormatString
        when (previewFormat) {
            PixelFormat.YCbCr_420_SP, PixelFormat.YCbCr_422_SP -> return PlanarYUVLuminanceSource(data, width, height, rect!!.left, rect.top,
                    rect.width(), rect.height())
            else ->
                // The Samsung Moment incorrectly uses this variant instead of the 'sp' version.
                // Fortunately, it too has all the Y data up front, so we can read it.
                if ("yuv420p" == previewFormatString) {
                    return PlanarYUVLuminanceSource(data, width, height, rect!!.left, rect.top,
                            rect.width(), rect.height())
                }
        }
        throw IllegalArgumentException("Unsupported picture format: " +
                previewFormat + '/' + previewFormatString)
    }
}
