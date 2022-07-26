package com.black.c2c.activity

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.model.c2c.C2CDetail
import com.black.base.model.c2c.C2COrder
import com.black.base.util.ConstData
import com.black.base.util.CookieUtil
import com.black.base.util.RouterConstData
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cOrderDetailTalkNewBinding
import com.black.im.fragment.ChatFragment
import com.black.im.model.chat.ChatInfo
import com.black.im.util.AudioPlayer
import com.black.im.util.IMAvatarUtil
import com.black.im.util.IMConstData
import com.black.im.util.IMHelper
import com.black.im.widget.ChatLayout
import com.black.router.annotation.Route
import com.black.util.NumberUtil
import com.google.gson.Gson
import com.tencent.imsdk.TIMConversationType
import skin.support.content.res.SkinCompatResources

@Route(value = [RouterConstData.C2C_ORDER_DETAIL_TALK_NEW], beforePath = RouterConstData.LOGIN)
class C2COrderDetailTalkNewActivity : BaseActionBarActivity(), View.OnClickListener {
    private var orderId: String? = null
    private var direction: String? = null
    private var c2COrder: C2CDetail? = null

    private var bgCircleGreen: Drawable? = null
    private var bgCircleRed: Drawable? = null

    private var binding: ActivityC2cOrderDetailTalkNewBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_order_detail_talk_new)
        orderId = intent.getStringExtra(ConstData.C2C_ORDER_ID)
        direction = intent.getStringExtra(ConstData.C2C_DIRECTION)
        if (c2COrder == null) {
            finish()
            return
        }
        bgCircleGreen = SkinCompatResources.getDrawable(this, R.drawable.bg_circle_green)
        bgCircleRed = SkinCompatResources.getDrawable(this, R.drawable.bg_circle_green)

        val isBuy = C2COrder.ORDER_BUY == direction
        binding?.moneyTitle?.setText(if (isBuy) "应付款" else "应收款")
        refreshOrderDetail(c2COrder)
        initChatView()
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getActionBarLayoutId(): Int {
        return R.layout.action_bar_left_back
    }

    override fun initActionBarView(view: View) {
        val c2cOrderData = intent.getStringExtra(ConstData.C2C_ORDER_DATA)
        if (c2cOrderData != null) {
            c2COrder = Gson().fromJson(c2cOrderData, C2CDetail::class.java)
        }
        if (c2COrder == null) {
            finish()
        }
        val headTitleView = view.findViewById<TextView>(R.id.action_bar_title)
        //        headTitleView.setText((C2COrder.ORDER_BUY.equals(c2COrder.direction) ? getString(R.string.c2c_bill_detail_sale) : getString(R.string.c2c_bill_detail_buy)));
        headTitleView.text = c2COrder?.getStatusDisplay(this)
    }

    override fun onClick(v: View) {
        val i = v.id
    }

    public override fun onPause() {
        super.onPause()
        AudioPlayer.instance.stopPlay()
    }

    public override fun onDestroy() {
        super.onDestroy()
    }

    private fun initChatView() {
        val userInfo = CookieUtil.getUserInfo(this)
        val chatInfo = ChatInfo()
        chatInfo.type = TIMConversationType.C2C
        val orderUserId = if (c2COrder == null) null else c2COrder?.userId
        val userIdHeader = IMHelper.getUserIdHeader(this)
        val userId = if (c2COrder == null || userInfo == null) null else userIdHeader + if (TextUtils.equals(userInfo.id, orderUserId)) c2COrder?.merchantUserId else userInfo.id
        //        userId = userIdHeader + "65";
        chatInfo.id = userId
        val chatFragment = ChatFragment()
        val bundle = Bundle()
        bundle.putSerializable(IMConstData.CHAT_INFO, chatInfo)
        chatFragment.arguments = bundle
        chatFragment.setOnChatLayoutListener(object : ChatFragment.OnChatLayoutListener {
            override fun onInitChatLayout(chatLayout: ChatLayout?) {
                chatLayout?.messageLayout?.setLeftNameHard(c2COrder?.merchantName)
                chatLayout?.messageLayout?.setLeftNameVisibility(View.GONE)
                chatLayout?.messageLayout?.setLeftDefaultNameAvatar(IMAvatarUtil.getCacheIcon(c2COrder?.merchantName))
                chatLayout?.messageLayout?.setRightNameHard(if (userInfo == null) "" else userInfo.realName)
                chatLayout?.titleBar?.visibility = View.GONE
                val displayMetrics = resources.displayMetrics
                chatLayout?.messageLayout?.setAvatarRadius((displayMetrics.density * 20).toInt())
                chatLayout?.messageLayout?.setLeftIconVisibility(View.VISIBLE)
                chatLayout?.messageLayout?.setRightIconVisibility(View.VISIBLE)
                chatLayout?.inputLayout?.setInputHint("输入文字联系商户")
                chatLayout?.inputLayout?.disableSendFileAction(true)
            }

        })
        supportFragmentManager.beginTransaction().replace(R.id.chat_container, chatFragment).commitAllowingStateLoss()
    }

    fun refreshOrderDetail(c2COrder: C2CDetail?) {
        if (c2COrder == null) {
            return
        }
        binding?.firstLetter?.text = if (TextUtils.isEmpty(c2COrder.merchantName)) "?" else c2COrder.merchantName!![0].toString()
        binding?.firstLetter?.background = if (C2COrder.ORDER_BUY == c2COrder.direction) bgCircleGreen else bgCircleRed
        binding?.name?.text = c2COrder.merchantName
        val isBuy = C2COrder.ORDER_BUY == direction
        binding?.moneyTitle?.text = if (isBuy) "应付款" else "应收款"
        binding?.money?.text = String.format("%s %s", NumberUtil.formatNumberNoGroupHardScale(c2COrder.totalPrice, 2), getString(R.string.cny))
    }
}