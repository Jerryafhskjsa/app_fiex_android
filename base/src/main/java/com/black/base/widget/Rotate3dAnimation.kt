package com.black.base.widget

import android.graphics.Camera
import android.view.animation.Animation
import android.view.animation.Transformation

/**
 * An animation that rotates the view on the Y axis between two specified
 * angles. This animation also adds a translation on the Z axis (depth) to
 * improve the effect.
 */
class Rotate3dAnimation
/**
 * Creates a new 3D rotation on the Y axis. The rotation is defined by its
 * start angle and its end angle. Both angles are in degrees. The rotation
 * is performed around a center point on the 2D space, definied by a pair of
 * X and Y coordinates, called centerX and centerY. When the animation
 * starts, a translation on the Z axis (depth) is performed. The length of
 * the translation can be specified, as well as whether the translation
 * should be reversed in time.
 *
 * @param fromDegrees the start angle of the 3D rotation
 * @param toDegrees   the end angle of the 3D rotation
 * @param centerX     the X center of the 3D rotation
 * @param centerY     the Y center of the 3D rotation
 * @param reverse     true if the translation should be reversed, false otherwise
 */(private val mFromDegrees: Float, private val mToDegrees: Float, private val mCenterX: Float, private val mCenterY: Float,
    private val mDepthZ: Float, private val mReverse: Boolean) : Animation() {
    private var mCamera: Camera? = null
    override fun initialize(width: Int, height: Int, parentWidth: Int, parentHeight: Int) {
        super.initialize(width, height, parentWidth, parentHeight)
        mCamera = Camera()
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        val fromDegrees = mFromDegrees
        val degrees = fromDegrees + (mToDegrees - fromDegrees) * interpolatedTime
        val centerX = mCenterX
        val centerY = mCenterY
        val camera = mCamera
        val matrix = t.matrix
        camera!!.save()
        //z轴上的景深
        if (mReverse) {
            camera.translate(0.0f, 0.0f, mDepthZ * interpolatedTime)
        } else {
            camera.translate(0.0f, 0.0f, mDepthZ * (1.0f - interpolatedTime))
        }
        camera.rotateY(degrees)
        camera.getMatrix(matrix)
        camera.restore()
        //camera.rotateY(degrees);其实围绕y轴旋转的坐标点是在（0,0）；
        //为了让其在围绕（centerX, centerY）点
        //preTranslate 作用为在rotateY开始之前先把坐标（centerX, centerY）移到中心点
        matrix.preTranslate(-centerX, -centerY)
        //postTranslate 当执行完rotateY后再把中心点移动回来
        matrix.postTranslate(centerX, centerY)
    }

}