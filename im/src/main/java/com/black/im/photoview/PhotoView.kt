package com.black.im.photoview

import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.view.GestureDetector.OnDoubleTapListener
import androidx.appcompat.widget.AppCompatImageView

/**
 * A zoomable ImageView. See [PhotoViewAttacher] for most of the details on how the zooming
 * is accomplished
 */
class PhotoView @JvmOverloads constructor(context: Context?, attr: AttributeSet? = null, defStyle: Int = 0) : AppCompatImageView(context, attr, defStyle) {
    /**
     * Get the current [PhotoViewAttacher] for this view. Be wary of holding on to references
     * to this attacher, as it has a reference to this view, which, if a reference is held in the
     * wrong place, can cause memory leaks.
     *
     * @return the attacher.
     */
    var attacher: PhotoViewAttacher? = null
        private set
    private var pendingScaleType: ScaleType? = null

    init {
        init()
    }

    private fun init() {
        attacher = PhotoViewAttacher(this)
        //We always pose as a Matrix scale type, though we can change to another scale type
//via the attacher
        super.setScaleType(ScaleType.MATRIX)
        //apply the previously applied scale type
        if (pendingScaleType != null) {
            setScaleType(pendingScaleType!!)
            pendingScaleType = null
        }
    }

    override fun getScaleType(): ScaleType? {
        return attacher?.scaleType
    }

    override fun setScaleType(scaleType: ScaleType) {
        if (attacher == null) {
            pendingScaleType = scaleType
        } else {
            attacher?.scaleType = scaleType
        }
    }

    override fun getImageMatrix(): Matrix? {
        return attacher?.imageMatrix
    }

    override fun setOnLongClickListener(l: OnLongClickListener) {
        attacher?.setOnLongClickListener(l)
    }

    override fun setOnClickListener(l: OnClickListener) {
        attacher?.setOnClickListener(l)
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        // setImageBitmap calls through to this method
        if (attacher != null) {
            attacher?.update()
        }
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        if (attacher != null) {
            attacher?.update()
        }
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        if (attacher != null) {
            attacher?.update()
        }
    }

    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        val changed = super.setFrame(l, t, r, b)
        if (changed) {
            attacher?.update()
        }
        return changed
    }

    fun setRotationTo(rotationDegree: Float) {
        attacher?.setRotationTo(rotationDegree)
    }

    fun setRotationBy(rotationDegree: Float) {
        attacher?.setRotationBy(rotationDegree)
    }

    var isZoomable: Boolean
        get() = attacher?.isZoomable ?: false
        set(zoomable) {
            attacher?.isZoomable = zoomable
        }

    val displayRect: RectF?
        get() = attacher?.displayRect

    fun getDisplayMatrix(matrix: Matrix) {
        attacher?.getDisplayMatrix(matrix)
    }

    fun setDisplayMatrix(finalRectangle: Matrix?): Boolean {
        return attacher?.setDisplayMatrix(finalRectangle) ?: false
    }

    fun getSuppMatrix(matrix: Matrix) {
        attacher?.getSuppMatrix(matrix)
    }

    fun setSuppMatrix(matrix: Matrix): Boolean {
        return attacher?.setDisplayMatrix(matrix) ?: false
    }

    var minimumScale: Float
        get() = attacher?.minimumScale ?: 0.toFloat()
        set(minimumScale) {
            attacher?.minimumScale = minimumScale
        }

    var mediumScale: Float
        get() = attacher?.mediumScale?: 0.toFloat()
        set(mediumScale) {
            attacher?.mediumScale = mediumScale
        }

    var maximumScale: Float
        get() = attacher?.maximumScale?: 0.toFloat()
        set(maximumScale) {
            attacher?.maximumScale = maximumScale
        }

    var scale: Float
        get() = attacher?.scale?: 0.toFloat()
        set(scale) {
            attacher?.scale = scale
        }

    fun setAllowParentInterceptOnEdge(allow: Boolean) {
        attacher?.setAllowParentInterceptOnEdge(allow)
    }

    fun setScaleLevels(minimumScale: Float, mediumScale: Float, maximumScale: Float) {
        attacher?.setScaleLevels(minimumScale, mediumScale, maximumScale)
    }

    fun setOnMatrixChangeListener(listener: OnMatrixChangedListener?) {
        attacher?.setOnMatrixChangeListener(listener)
    }

    fun setOnPhotoTapListener(listener: OnPhotoTapListener?) {
        attacher?.setOnPhotoTapListener(listener)
    }

    fun setOnOutsidePhotoTapListener(listener: OnOutsidePhotoTapListener?) {
        attacher?.setOnOutsidePhotoTapListener(listener)
    }

    fun setOnViewTapListener(listener: OnViewTapListener?) {
        attacher?.setOnViewTapListener(listener)
    }

    fun setOnViewDragListener(listener: OnViewDragListener?) {
        attacher?.setOnViewDragListener(listener)
    }

    fun setScale(scale: Float, animate: Boolean) {
        attacher?.setScale(scale, animate)
    }

    fun setScale(scale: Float, focalX: Float, focalY: Float, animate: Boolean) {
        attacher?.setScale(scale, focalX, focalY, animate)
    }

    fun setZoomTransitionDuration(milliseconds: Int) {
        attacher?.setZoomTransitionDuration(milliseconds)
    }

    fun setOnDoubleTapListener(onDoubleTapListener: OnDoubleTapListener?) {
        attacher?.setOnDoubleTapListener(onDoubleTapListener)
    }

    fun setOnScaleChangeListener(onScaleChangedListener: OnScaleChangedListener?) {
        attacher?.setOnScaleChangeListener(onScaleChangedListener)
    }

    fun setOnSingleFlingListener(onSingleFlingListener: OnSingleFlingListener?) {
        attacher?.setOnSingleFlingListener(onSingleFlingListener)
    }
}