package com.black.im.video

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.black.im.video.listener.CaptureListener
import com.black.im.video.listener.ClickListener
import com.black.im.video.listener.ReturnListener
import com.black.im.video.listener.TypeListener

/**
 * 集成各个控件的布局
 */
class CaptureLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    private var captureLisenter: CaptureListener? = null  //拍照按钮监听
    private var typeLisenter: TypeListener? = null  //拍照或录制后接结果按钮监听
    private var returnListener: ReturnListener? = null  //退出按钮监听
    private var leftClickListener: ClickListener? = null //左边按钮监听
    private var rightClickListener: ClickListener? = null //右边按钮监听
    private var btn_capture: CaptureButton? = null //拍照按钮
    private var btn_confirm: TypeButton? = null //确认按钮
    private var btn_cancel: TypeButton? = null //取消按钮
    private var btn_return: ReturnButton? = null //返回按钮
    private var iv_custom_left: ImageView? = null //左边自定义按钮
    private var iv_custom_right: ImageView? = null //右边自定义按钮
    private var txt_tip: TextView? = null //提示文本
    private var layout_width = 0
    private val layout_height: Int
    private val button_size: Int
    private var iconLeft = 0
    private var iconRight = 0
    private var isFirst = true

    init {
        val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        manager.defaultDisplay.getMetrics(outMetrics)
        layout_width = if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            outMetrics.widthPixels
        } else {
            outMetrics.widthPixels / 2
        }
        button_size = (layout_width / 4.5f).toInt()
        layout_height = button_size + button_size / 5 * 2 + 100
        initView()
        initEvent()
    }

    fun setTypeLisenter(typeLisenter: TypeListener?) {
        this.typeLisenter = typeLisenter
    }

    fun setCaptureLisenter(captureLisenter: CaptureListener?) {
        this.captureLisenter = captureLisenter
    }

    fun setReturnLisenter(returnListener: ReturnListener?) {
        this.returnListener = returnListener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(layout_width, layout_height)
    }

    fun initEvent() {
        //默认Typebutton为隐藏
        iv_custom_right?.visibility = View.GONE
        btn_cancel?.visibility = View.GONE
        btn_confirm?.visibility = View.GONE
    }

    fun startTypeBtnAnimator() {
        //拍照录制结果后的动画
        if (iconLeft != 0) iv_custom_left?.visibility = View.GONE else btn_return?.visibility = View.GONE
        if (iconRight != 0) iv_custom_right?.visibility = View.GONE
        btn_capture?.visibility = View.GONE
        btn_cancel?.visibility = View.VISIBLE
        btn_confirm?.visibility = View.VISIBLE
        btn_cancel?.isClickable = false
        btn_confirm?.isClickable = false
        val animator_cancel = ObjectAnimator.ofFloat(btn_cancel!!, "translationX", layout_width / 4.toFloat(), 0f)
        val animator_confirm = ObjectAnimator.ofFloat(btn_confirm!!, "translationX", -layout_width / 4.toFloat(), 0f)
        val set = AnimatorSet()
        set.playTogether(animator_cancel, animator_confirm)
        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                btn_cancel?.isClickable = true
                btn_confirm?.isClickable = true
            }
        })
        set.duration = 200
        set.start()
    }

    private fun initView() {
        setWillNotDraw(false)
        //拍照按钮
        btn_capture = CaptureButton(context, button_size)
        val btn_capture_param = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        btn_capture_param.gravity = Gravity.CENTER
        btn_capture?.layoutParams = btn_capture_param
        btn_capture?.setCaptureLisenter(object : CaptureListener {
            override fun takePictures() {
                if (captureLisenter != null) {
                    captureLisenter?.takePictures()
                }
            }

            override fun recordShort(time: Long) {
                if (captureLisenter != null) {
                    captureLisenter?.recordShort(time)
                }
                startAlphaAnimation()
            }

            override fun recordStart() {
                if (captureLisenter != null) {
                    captureLisenter?.recordStart()
                }
                startAlphaAnimation()
            }

            override fun recordEnd(time: Long) {
                if (captureLisenter != null) {
                    captureLisenter?.recordEnd(time)
                }
                startAlphaAnimation()
                startTypeBtnAnimator()
            }

            override fun recordZoom(zoom: Float) {
                if (captureLisenter != null) {
                    captureLisenter?.recordZoom(zoom)
                }
            }

            override fun recordError() {
                if (captureLisenter != null) {
                    captureLisenter?.recordError()
                }
            }
        })
        //取消按钮
        btn_cancel = TypeButton(context, TypeButton.TYPE_CANCEL, button_size)
        val btn_cancel_param = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        btn_cancel_param.gravity = Gravity.CENTER_VERTICAL
        btn_cancel_param.setMargins(layout_width / 4 - button_size / 2, 0, 0, 0)
        btn_cancel?.layoutParams = btn_cancel_param
        btn_cancel?.setOnClickListener {
            if (typeLisenter != null) {
                typeLisenter?.cancel()
            }
            startAlphaAnimation()
            //                resetCaptureLayout();
        }
        //确认按钮
        btn_confirm = TypeButton(context, TypeButton.TYPE_CONFIRM, button_size)
        val btn_confirm_param = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        btn_confirm_param.gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT
        btn_confirm_param.setMargins(0, 0, layout_width / 4 - button_size / 2, 0)
        btn_confirm?.layoutParams = btn_confirm_param
        btn_confirm?.setOnClickListener {
            if (typeLisenter != null) {
                typeLisenter?.confirm()
            }
            startAlphaAnimation()
            //                resetCaptureLayout();
        }
        //返回按钮
        btn_return = ReturnButton(context, (button_size / 2.5f).toInt())
        val btn_return_param = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        btn_return_param.gravity = Gravity.CENTER_VERTICAL
        btn_return_param.setMargins(layout_width / 6, 0, 0, 0)
        btn_return?.layoutParams = btn_return_param
        btn_return?.setOnClickListener {
            if (leftClickListener != null) {
                leftClickListener?.onClick()
            }
        }
        //左边自定义按钮
        iv_custom_left = ImageView(context)
        val iv_custom_param_left = LayoutParams((button_size / 2.5f).toInt(), (button_size / 2.5f).toInt())
        iv_custom_param_left.gravity = Gravity.CENTER_VERTICAL
        iv_custom_param_left.setMargins(layout_width / 6, 0, 0, 0)
        iv_custom_left?.layoutParams = iv_custom_param_left
        iv_custom_left?.setOnClickListener {
            if (leftClickListener != null) {
                leftClickListener?.onClick()
            }
        }
        //右边自定义按钮
        iv_custom_right = ImageView(context)
        val iv_custom_param_right = LayoutParams((button_size / 2.5f).toInt(), (button_size / 2.5f).toInt())
        iv_custom_param_right.gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT
        iv_custom_param_right.setMargins(0, 0, layout_width / 6, 0)
        iv_custom_right?.layoutParams = iv_custom_param_right
        iv_custom_right?.setOnClickListener {
            if (rightClickListener != null) {
                rightClickListener?.onClick()
            }
        }
        txt_tip = TextView(context)
        val txt_param = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        txt_param.gravity = Gravity.CENTER_HORIZONTAL
        txt_param.setMargins(0, 0, 0, 0)
        txt_tip?.text = "轻触拍照，长按摄像"
        txt_tip?.setTextColor(-0x1)
        txt_tip?.gravity = Gravity.CENTER
        txt_tip?.layoutParams = txt_param
        this.addView(btn_capture)
        this.addView(btn_cancel)
        this.addView(btn_confirm)
        this.addView(btn_return)
        this.addView(iv_custom_left)
        this.addView(iv_custom_right)
        this.addView(txt_tip)
    }

    /**************************************************
     * 对外提供的API                      *
     */
    fun resetCaptureLayout() {
        btn_capture?.resetState()
        btn_cancel?.visibility = View.GONE
        btn_confirm?.visibility = View.GONE
        btn_capture?.visibility = View.VISIBLE
        if (iconLeft != 0) iv_custom_left?.visibility = View.VISIBLE else btn_return?.visibility = View.VISIBLE
        if (iconRight != 0) iv_custom_right?.visibility = View.VISIBLE
    }

    fun startAlphaAnimation() {
        if (isFirst) {
            val animator_txt_tip = ObjectAnimator.ofFloat(txt_tip!!, "alpha", 1f, 0f)
            animator_txt_tip.duration = 500
            animator_txt_tip.start()
            isFirst = false
        }
    }

    fun setTextWithAnimation(tip: String?) {
        txt_tip?.text = tip
        val animator_txt_tip = ObjectAnimator.ofFloat(txt_tip!!, "alpha", 0f, 1f, 1f, 0f)
        animator_txt_tip.duration = 2500
        animator_txt_tip.start()
    }

    fun setDuration(duration: Int) {
        btn_capture?.setDuration(duration)
    }

    fun setButtonFeatures(state: Int) {
        btn_capture?.setButtonFeatures(state)
    }

    fun setTip(tip: String?) {
        txt_tip?.text = tip
    }

    fun showTip() {
        txt_tip?.visibility = View.VISIBLE
    }

    fun setIconSrc(iconLeft: Int, iconRight: Int) {
        this.iconLeft = iconLeft
        this.iconRight = iconRight
        if (this.iconLeft != 0) {
            iv_custom_left?.setImageResource(iconLeft)
            iv_custom_left?.visibility = View.VISIBLE
            btn_return?.visibility = View.GONE
        } else {
            iv_custom_left?.visibility = View.GONE
            btn_return?.visibility = View.VISIBLE
        }
        if (this.iconRight != 0) {
            iv_custom_right?.setImageResource(iconRight)
            iv_custom_right?.visibility = View.VISIBLE
        } else {
            iv_custom_right?.visibility = View.GONE
        }
    }

    fun setLeftClickListener(leftClickListener: ClickListener?) {
        this.leftClickListener = leftClickListener
    }

    fun setRightClickListener(rightClickListener: ClickListener?) {
        this.rightClickListener = rightClickListener
    }
}