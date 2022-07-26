package com.black.lib.view

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Movie
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import androidx.annotation.FloatRange
import androidx.appcompat.widget.AppCompatImageView
import com.black.lib.R
import java.math.BigDecimal

class GifImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = -1) : AppCompatImageView(context, attrs, defStyle) {
    private var mScaleW = 1.0f
    private var mScaleH = 1.0f
    private var mScale = 1.0f
    private var movie: Movie? = null
    //播放开始时间点
    private var mMovieStart: Long = 0
    //播放暂停时间点
    private var mMoviePauseTime: Long = 0
    //播放暂停时间
    private var offsetTime: Long = 0
    //播放完成进度
    @FloatRange(from = 0.toDouble(), to = 1.0)
    private var percent = 0f
    //播放次数，-1为循环播放
    private var counts = -1
    @Volatile
    private var reverse = false
    @Volatile
    var isPaused = false
        private set
    @Volatile
    private var hasStart = false
    private var mVisible = true
    private var mOnPlayListener: OnPlayListener? = null
    private var movieDuration = 0
    private var endLastFrame = false
    private fun setViewAttributes(context: Context, attrs: AttributeSet?, defStyle: Int) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.GifImageView, defStyle, 0)
        val srcID = a.getResourceId(R.styleable.GifImageView_gif_src, 0)
        val authPlay = a.getBoolean(R.styleable.GifImageView_auth_play, true)
        counts = a.getInt(R.styleable.GifImageView_play_count, -1)
        endLastFrame = a.getBoolean(R.styleable.GifImageView_end_last_frame, false)
        if (srcID > 0) {
            setGifResource(srcID, null)
            if (authPlay) play(counts)
        }
        a.recycle()
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    private fun setGifResource(movieResourceId: Int, onPlayListener: OnPlayListener?) {
        if (onPlayListener != null) {
            mOnPlayListener = onPlayListener
        }
        reset()
        movie = Movie.decodeStream(resources.openRawResource(movieResourceId))
        if (movie == null) { //如果movie为空，那么就不是gif文件，尝试转换为bitmap显示
            val bitmap = BitmapFactory.decodeResource(resources, movieResourceId)
            if (bitmap != null) {
                setImageBitmap(bitmap)
                return
            }
        }
        movieDuration = if ((movie?.duration() ?: 0) == 0) DEFAULT_DURATION else (movie?.duration()
                ?: 0)
        requestLayout()
    }

    fun setGifResource(movieResourceId: Int) {
        setGifResource(movieResourceId, null)
    }

    fun setGifResource(path: String?, onPlayListener: OnPlayListener?) {
        movie = Movie.decodeFile(path)
        mOnPlayListener = onPlayListener
        reset()
        if (movie == null) {
            val bitmap = BitmapFactory.decodeFile(path)
            if (bitmap != null) {
                setImageBitmap(bitmap)
                return
            }
        }
        movieDuration = if ((movie?.duration() ?: 0) == 0) DEFAULT_DURATION else (movie?.duration()
                ?: 0)
        requestLayout()
        if (mOnPlayListener != null) {
            mOnPlayListener!!.onPlayStart()
        }
    }

    //从新开始播放
    fun playOver() {
        if (movie != null) {
            play(-1)
        }
    }

    //倒叙播放
    fun playReverse() {
        if (movie != null) {
            reset()
            reverse = true
            if (mOnPlayListener != null) {
                mOnPlayListener!!.onPlayStart()
            }
            invalidate()
        }
    }

    private fun play(counts: Int) {
        this.counts = counts
        reset()
        if (mOnPlayListener != null) {
            mOnPlayListener!!.onPlayStart()
        }
        invalidate()
    }

    private fun reset() {
        reverse = false
        mMovieStart = SystemClock.uptimeMillis()
        isPaused = false
        hasStart = true
        mMoviePauseTime = 0
        offsetTime = 0
    }

    fun play() {
        if (movie == null) return
        if (hasStart) {
            if (isPaused && mMoviePauseTime > 0) {
                isPaused = false
                offsetTime = offsetTime + SystemClock.uptimeMillis() - mMoviePauseTime
                invalidate()
                if (mOnPlayListener != null) {
                    mOnPlayListener!!.onPlayRestart()
                }
            }
        } else {
            play(-1)
        }
    }

    fun pause() {
        if (movie != null && !isPaused && hasStart) {
            isPaused = true
            invalidate()
            mMoviePauseTime = SystemClock.uptimeMillis()
            if (mOnPlayListener != null) {
                mOnPlayListener!!.onPlayPause(true)
            }
        } else {
            if (mOnPlayListener != null) {
                mOnPlayListener!!.onPlayPause(false)
            }
        }
    }

    private val currentFrameTime: Int
        get() {
            if (movieDuration == 0) return 0
            val now = SystemClock.uptimeMillis() - offsetTime
            val nowCount = ((now - mMovieStart) / movieDuration).toInt()
            if (counts != -1 && nowCount >= counts) {
                hasStart = false
                if (mOnPlayListener != null) {
                    mOnPlayListener!!.onPlayEnd()
                }
                return if (endLastFrame) movieDuration else 0
            }
            val currentTime = (now - mMovieStart) % movieDuration.toFloat()
            percent = currentTime / movieDuration
            if (mOnPlayListener != null && hasStart) {
                val mData: BigDecimal = BigDecimal(percent.toDouble()).setScale(2, BigDecimal.ROUND_HALF_UP)
                var f1: Double = mData.toDouble()
                f1 = if (f1 == 0.99) 1.0 else f1
                mOnPlayListener!!.onPlaying(f1.toFloat())
            }
            return currentTime.toInt()
        }

    fun setPercent(percent: Float) {
        if (movie != null && movieDuration > 0) {
            this.percent = percent
            movie!!.setTime((movieDuration * percent).toInt())
            invalidateView()
            if (mOnPlayListener != null) {
                mOnPlayListener!!.onPlaying(percent)
            }
        }
    }

    val isPlaying: Boolean
        get() = !isPaused && hasStart

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (movie != null) {
            if (!isPaused && hasStart) {
                if (reverse) {
                    movie!!.setTime(movieDuration - currentFrameTime)
                } else {
                    movie!!.setTime(currentFrameTime)
                }
                drawMovieFrame(canvas)
                invalidateView()
            } else {
                drawMovieFrame(canvas)
            }
        }
    }

    /**
     * 画出gif帧
     */
    private fun drawMovieFrame(canvas: Canvas) {
        canvas.save()
        canvas.scale(1 / mScale, 1 / mScale)
        movie!!.draw(canvas, 0.0f, 0.0f)
        canvas.restore()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val sizeWidth = MeasureSpec.getSize(widthMeasureSpec)
        val sizeHeight = MeasureSpec.getSize(heightMeasureSpec)
        if (movie != null) {
            val movieWidth = movie!!.width()
            val movieHeight = movie!!.height()
            if (widthMode == MeasureSpec.EXACTLY) {
                mScaleW = movieWidth.toFloat() / sizeWidth
            }
            if (heightMode == MeasureSpec.EXACTLY) {
                mScaleH = movieHeight.toFloat() / sizeHeight
            }
            mScale = Math.max(mScaleW, mScaleH)
            setMeasuredDimension(if (widthMode == MeasureSpec.EXACTLY) sizeWidth else movieWidth, if (heightMode == MeasureSpec.EXACTLY) sizeHeight else movieHeight)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    private fun invalidateView() {
        if (mVisible) {
            postInvalidateOnAnimation()
        }
    }

    val duration: Int
        get() = if (movie != null) {
            movie!!.duration()
        } else 0

    override fun onScreenStateChanged(screenState: Int) {
        super.onScreenStateChanged(screenState)
        mVisible = screenState == View.SCREEN_STATE_ON
        invalidateView()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        mVisible = visibility == View.VISIBLE
        invalidateView()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        mVisible = visibility == View.VISIBLE
        invalidateView()
    }

    fun recycle() {}
    interface OnPlayListener {
        fun onPlayStart()
        fun onPlaying(@FloatRange(from = 0.0, to = 1.0) percent: Float)
        fun onPlayPause(pauseSuccess: Boolean)
        fun onPlayRestart()
        fun onPlayEnd()
    }

    companion object {
        private const val DEFAULT_DURATION = 1000
    }

    init {
        setViewAttributes(context, attrs, defStyle)
    }
}