package com.black.lib.camera

import android.content.Context
import android.graphics.Point
import android.hardware.Camera
import android.os.Build
import android.view.WindowManager
import java.lang.Boolean
import java.lang.reflect.Method
import java.util.regex.Pattern
import kotlin.math.abs

internal class CameraConfigurationManager(private val context: Context) {
    companion object {
        private val TAG = CameraConfigurationManager::class.java.simpleName
        private const val TEN_DESIRED_ZOOM = 27
        const val desiredSharpness = 30
        private val COMMA_PATTERN = Pattern.compile(",")
        private fun getCameraResolution(parameters: Camera.Parameters, screenResolution: Point): Point {
            var previewSizeValueString = parameters["preview-size-values"]
            // saw this on Xperia
            if (previewSizeValueString == null) {
                previewSizeValueString = parameters["preview-size-value"]
            }
            var cameraResolution: Point? = null
            if (previewSizeValueString != null) {
                cameraResolution = findBestPreviewSizeValue(previewSizeValueString, screenResolution)
            }
            if (cameraResolution == null) { // Ensure that the camera resolution is a multiple of 8, as the screen may not be.
                cameraResolution = Point(
                        screenResolution.x shr 3 shl 3,
                        screenResolution.y shr 3 shl 3)
            }
            return cameraResolution
        }

        private fun findBestPreviewSizeValue(previewSizeValueString: CharSequence, screenResolution: Point): Point? {
            var bestX = 0
            var bestY = 0
            var diff = Int.MAX_VALUE
            for (previewSize in COMMA_PATTERN.split(previewSizeValueString)) {
                val size = previewSize.trim { it <= ' ' }
                val dimPosition = size.indexOf('x')
                if (dimPosition < 0) {
                    continue
                }
                var newX: Int
                var newY: Int
                try {
                    newX = size.substring(0, dimPosition).toInt()
                    newY = size.substring(dimPosition + 1).toInt()
                } catch (nfe: NumberFormatException) {
                    continue
                }
                val newDiff = abs(newX - screenResolution.x) + abs(newY - screenResolution.y)
                if (newDiff == 0) {
                    bestX = newX
                    bestY = newY
                    break
                } else if (newDiff < diff) {
                    bestX = newX
                    bestY = newY
                    diff = newDiff
                }
            }
            return if (bestX > 0 && bestY > 0) {
                Point(bestX, bestY)
            } else null
        }

        private fun findBestMotZoomValue(stringValues: CharSequence, tenDesiredZoom: Int): Int {
            var tenBestValue = 0
            for (stringValue in COMMA_PATTERN.split(stringValues)) {
                val stringValue1 = stringValue.trim { it <= ' ' }
                var value: Double
                value = try {
                    stringValue1.toDouble()
                } catch (nfe: NumberFormatException) {
                    return tenDesiredZoom
                }
                val tenValue = (10.0 * value).toInt()
                if (abs(tenDesiredZoom - value) < abs(tenDesiredZoom - tenBestValue)) {
                    tenBestValue = tenValue
                }
            }
            return tenBestValue
        }

    }

    lateinit var screenResolution: Point
        private set
    lateinit var cameraResolution: Point
        private set
    var previewFormat = 0
        private set
    var previewFormatString: String? = null
        private set

    /**
     * Reads, one time, values from the camera that are needed by the app.
     */
    fun initFromCameraParameters(camera: Camera) {
        val parameters = camera.parameters
        previewFormat = parameters.previewFormat
        previewFormatString = parameters["preview-format"]
        val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = manager.defaultDisplay
        screenResolution = Point(display.width, display.height)
        cameraResolution = getCameraResolution(parameters, screenResolution!!)
    }

    /**
     * Sets the camera up to take preview images which are used for both preview and decoding.
     * We detect the preview format here so that buildLuminanceSource() can build an appropriate
     * LuminanceSource subclass. In the future we may want to force YUV420SP as it's the smallest,
     * and the planar Y can be used for barcode scanning without a copy in some cases.
     */
    fun setDesiredCameraParameters(camera: Camera) {
        val parameters = camera.parameters
        parameters.setPreviewSize(cameraResolution!!.x, cameraResolution!!.y)
        setFlash(parameters)
        setZoom(parameters)
        //setSharpness(parameters);
//modify here
//    camera.setDisplayOrientation(90);
//����2.1
        setDisplayOrientation(camera, 90)
        camera.parameters = parameters
    }

    private fun setFlash(parameters: Camera.Parameters) { // FIXME: This is a hack to turn the flash off on the Samsung Galaxy.
// And this is a hack-hack to work around a different value on the Behold II
// Restrict Behold II check to Cupcake, per Samsung's advice
//if (Build.MODEL.contains("Behold II") &&
//    CameraManager.SDK_INT == Build.VERSION_CODES.CUPCAKE) {
        if (Build.MODEL.contains("Behold II") && CameraManager.SDK_INT == 3) { // 3 = Cupcake
            parameters["flash-value"] = 1
        } else {
            parameters["flash-value"] = 2
        }
        // This is the standard setting to turn the flash off that all devices should honor.
        parameters["flash-mode"] = "off"
    }

    private fun setZoom(parameters: Camera.Parameters) {
        val zoomSupportedString = parameters["zoom-supported"]
        if (zoomSupportedString != null && !Boolean.parseBoolean(zoomSupportedString)) {
            return
        }
        var tenDesiredZoom = TEN_DESIRED_ZOOM
        val maxZoomString = parameters["max-zoom"]
        if (maxZoomString != null) {
            try {
                val tenMaxZoom = (10.0 * maxZoomString.toDouble()).toInt()
                if (tenDesiredZoom > tenMaxZoom) {
                    tenDesiredZoom = tenMaxZoom
                }
            } catch (nfe: NumberFormatException) {
            }
        }
        val takingPictureZoomMaxString = parameters["taking-picture-zoom-max"]
        if (takingPictureZoomMaxString != null) {
            try {
                val tenMaxZoom = takingPictureZoomMaxString.toInt()
                if (tenDesiredZoom > tenMaxZoom) {
                    tenDesiredZoom = tenMaxZoom
                }
            } catch (nfe: NumberFormatException) {
            }
        }
        val motZoomValuesString = parameters["mot-zoom-values"]
        if (motZoomValuesString != null) {
            tenDesiredZoom = findBestMotZoomValue(motZoomValuesString, tenDesiredZoom)
        }
        val motZoomStepString = parameters["mot-zoom-step"]
        if (motZoomStepString != null) {
            try {
                val motZoomStep = motZoomStepString.trim { it <= ' ' }.toDouble()
                val tenZoomStep = (10.0 * motZoomStep).toInt()
                if (tenZoomStep > 1) {
                    tenDesiredZoom -= tenDesiredZoom % tenZoomStep
                }
            } catch (nfe: NumberFormatException) { // continue
            }
        }
        // Set zoom. This helps encourage the user to pull back.
// Some devices like the Behold have a zoom parameter
        if (maxZoomString != null || motZoomValuesString != null) {
            parameters["zoom"] = (tenDesiredZoom / 10.0).toString()
        }
        // Most devices, like the Hero, appear to expose this zoom parameter.
// It takes on values like "27" which appears to mean 2.7x zoom
        if (takingPictureZoomMaxString != null) {
            parameters["taking-picture-zoom"] = tenDesiredZoom
        }
    }

    /**
     * compatible  1.6
     *
     * @param camera
     * @param angle
     */
    protected fun setDisplayOrientation(camera: Camera, angle: Int) {
        val downPolymorphic: Method?
        try {
            downPolymorphic = camera.javaClass.getMethod("setDisplayOrientation", Int::class.javaPrimitiveType)
            downPolymorphic?.invoke(camera, angle)
        } catch (e1: Exception) {
        }
    }
}