package com.black.base.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.black.base.R

class LoadingDialog(context: Context) : Dialog(context, R.style.loading_dialog) {
    private var loadingCircleView: ImageView? = null
    private var animation: Animation? = null

    //    private LottieAnimationView animationView;
    init {
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        if (window != null) {
            window.setDimAmount(0f)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loading_dialog)
        loadingCircleView = findViewById(R.id.loading_circle)
        animation = AnimationUtils.loadAnimation(context, R.anim.anim_loading_circle)
        //        animation.setRepeatCount(Animation.INFINITE);
//        animation.setRepeatMode(Animation.RESTART);
//        animation.setDuration(2000);
        loadingCircleView?.animation = animation
        //        animationView = findViewById(R.id.animation_view);
//        animationView.setRepeatCount(Integer.MAX_VALUE);
    }

    override fun show() {
        super.show()
        loadingCircleView?.startAnimation(animation)
        //        if (animationView != null) {
//            animationView.playAnimation();
//        }
    }

    override fun dismiss() {
        super.dismiss()
        loadingCircleView?.clearAnimation()
        //        if (animationView != null) {
//            animationView.cancelAnimation();
//        }
    }
}
