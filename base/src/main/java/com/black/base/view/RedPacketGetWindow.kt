package com.black.base.view

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import com.black.base.R
import com.black.base.api.CommunityApiServiceHelper.openRedPacket
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.model.community.RedPacket
import com.black.base.util.FryingUtil.showToast
import com.black.base.util.ImageLoader
import com.black.base.widget.Rotate3dAnimation
import com.black.net.HttpRequestResult

class RedPacketGetWindow(private val context: Context, private val redPacket: RedPacket?) : View.OnClickListener {
    private val imageLoader: ImageLoader = ImageLoader(context)
    private val alertDialog: AlertDialog?
    private val contentView: View
    private var btnOpen: View? = null
    private var onRedPacketOpenListener: OnRedPacketOpenListener? = null

    init {
        val dm = context.resources.displayMetrics
        contentView = LayoutInflater.from(context).inflate(R.layout.dialog_red_packet_get, null)
        alertDialog = AlertDialog.Builder(context, R.style.AlertDialog)
                .setView(contentView)
                .create()
        val lp = alertDialog.window?.attributes
        lp?.width = (dm.widthPixels - dm.density * 70).toInt() //定义宽度
        lp?.height = WindowManager.LayoutParams.WRAP_CONTENT //定义高度
        alertDialog.window?.attributes = lp
        initViews()
    }

    private fun initViews() {
        val avatarView = contentView.findViewById<ImageView>(R.id.avatar)
        if (redPacket != null) {
            imageLoader.loadImage(avatarView, redPacket.sendAvatar, R.drawable.icon_avatar, false)
        }
        val sendNameView = contentView.findViewById<TextView>(R.id.send_name)
        sendNameView.text = if (redPacket?.sendName == null) "" else redPacket.sendName
        val titleView = contentView.findViewById<TextView>(R.id.title)
        titleView.text = if (redPacket?.title == null) "" else redPacket.title
        contentView.findViewById<View>(R.id.btn_open).also { btnOpen = it }.setOnClickListener(this)
        contentView.findViewById<View>(R.id.btn_close).setOnClickListener(this)
        contentView.findViewById<View>(R.id.btn_detail).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btn_close) {
            dismiss()
        } else if (id == R.id.btn_detail) {
            if (onRedPacketOpenListener != null) {
                onRedPacketOpenListener?.onOpenDetail(this@RedPacketGetWindow)
            }
        } else if (id == R.id.btn_open) {
            opeRedPacket()
        }
    }

    fun show() {
        if (alertDialog != null && !alertDialog.isShowing) {
            alertDialog.show()
        }
    }

    fun dismiss() {
        if (alertDialog != null && alertDialog.isShowing) {
            alertDialog.dismiss()
        }
    }

    private fun opeRedPacket() { // 获取布局的中心点位置，作为旋转的中心点
        val centerX = (btnOpen?.width ?: 0) / 2f
        val centerY = (btnOpen?.height ?: 0) / 2f
        // 构建3D旋转动画对象，旋转角度为360到270度，这使得ImageView将会从可见变为不可见，并且旋转的方向是相反的
        val rotation = Rotate3dAnimation(0f, 360f, centerX,
                centerY, 0f, true)
        // 动画持续时间500毫秒
        rotation.duration = 1000
        // 动画完成后保持完成的状态
        rotation.fillAfter = true
        rotation.interpolator = LinearInterpolator()
        rotation.repeatCount = Animation.INFINITE
        rotation.repeatMode = Animation.RESTART
        btnOpen?.startAnimation(rotation)
        openRedPacket(context, redPacket?.packetId, object : NormalCallback<HttpRequestResultString?>(context) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
                btnOpen?.clearAnimation()
            }

            override fun callback(returnData: HttpRequestResultString?) {
                btnOpen?.clearAnimation()
                if (returnData?.code != null && returnData.code == HttpRequestResult.SUCCESS) {
                    if (onRedPacketOpenListener != null) {
                        onRedPacketOpenListener?.onOpenResult(this@RedPacketGetWindow, RedPacket.IS_OPEN)
                    }
                } else {
                    showToast(context, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    fun setOnRedPacketOpenListener(onRedPacketOpenListener: OnRedPacketOpenListener?) {
        this.onRedPacketOpenListener = onRedPacketOpenListener
    }

    interface OnRedPacketOpenListener {
        fun onOpenResult(window: RedPacketGetWindow, result: Int)
        fun onOpenDetail(window: RedPacketGetWindow)
    }
}
