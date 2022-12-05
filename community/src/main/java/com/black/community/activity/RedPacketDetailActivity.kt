package com.black.community.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.CommunityApiServiceHelper
import com.black.base.model.HttpRequestResultData
import com.black.base.model.NormalCallback
import com.black.base.model.community.RedPacketDetail
import com.black.base.model.community.RedPacketPub
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.ImageLoader
import com.black.base.util.RouterConstData
import com.black.community.BR
import com.black.community.R
import com.black.community.adapter.RedPacketGotAdapter
import com.black.community.databinding.ActivityRedPacketDetailBinding
import com.black.net.HttpRequestResult
import com.black.router.annotation.Route
import com.black.util.NumberUtil

@Route(value = [RouterConstData.RED_PACKET_DETAIL], beforePath = RouterConstData.LOGIN)
class RedPacketDetailActivity : BaseActionBarActivity(), View.OnClickListener {
    private var imageLoader: ImageLoader? = null
    private var redPacketPub: RedPacketPub? = null

    private var binding: ActivityRedPacketDetailBinding? = null

    private var adapter: RedPacketGotAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        redPacketPub = intent.getParcelableExtra(ConstData.RED_PACKET)
        if (redPacketPub == null) {
            finish()
            return
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_red_packet_detail)
        imageLoader = ImageLoader(this)

        imageLoader!!.loadImage(binding?.avatar, redPacketPub!!.avatar, R.drawable.icon_avatar, false)
        binding?.sender?.setText(if (redPacketPub!!.userName == null) nullAmount else redPacketPub!!.userName)
        binding?.title?.setText(if (redPacketPub!!.title == null) nullAmount else redPacketPub!!.title)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        adapter = RedPacketGotAdapter(this, BR.listItemRedPacketGotModel, null)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.isFocusable = false
        redPacketDetail
    }

    override fun isStatusBarDark(): Boolean {
        return false
    }

    override fun onClick(v: View) {}
    private val redPacketDetail: Unit
        get() {
            CommunityApiServiceHelper.getRedPacketDetail(this, redPacketPub!!.id, object : NormalCallback<HttpRequestResultData<RedPacketDetail?>?>(mContext!!) {
                override fun callback(returnData: HttpRequestResultData<RedPacketDetail?>?) {
                    if (returnData?.code != null && returnData.code == HttpRequestResult.SUCCESS) {
                        showRedPacketDetail(returnData.data)
                    } else {
                        FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                    }
                }
            })
        }

    private fun showRedPacketDetail(redPacketDetail: RedPacketDetail?) {
        imageLoader!!.loadImage(binding?.avatar, redPacketDetail?.avatar, R.drawable.icon_avatar, false)
        binding?.sender?.text = if (redPacketDetail?.userName == null) nullAmount else redPacketDetail.userName
        binding?.title?.text = if (redPacketDetail?.title == null) nullAmount else redPacketDetail.title
        val myRecord = redPacketDetail?.userRecord
        if (myRecord == null) {
            binding?.gotLayout?.visibility = View.GONE
        } else {
            binding?.gotLayout?.visibility = View.VISIBLE
            binding?.gotAmount?.text = if (myRecord.amount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(myRecord.amount, 9, 0, 8)
            binding?.coinType?.text = if (myRecord.coinType == null) nullAmount else myRecord.coinType
        }
        val quantity = redPacketDetail?.quantity ?: 0
        val amount = if (redPacketDetail?.amount == null) null else redPacketDetail.amount
        if (TextUtils.equals(RedPacketPub.NORMAL, redPacketDetail?.type)) {
            binding?.status?.text = String.format("%s个红包，总金额%s%s",
                    NumberUtil.formatNumberNoGroup(quantity),
                    if (amount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(amount * quantity, 9, 0, 8),
                    if (redPacketDetail?.coinType == null) nullAmount else redPacketDetail.coinType)
        } else { //运气红包直接显示
            binding?.status?.text = String.format("%s个红包，总金额%s%s",
                    NumberUtil.formatNumberNoGroup(quantity),
                    if (amount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(amount, 9, 0, 8),
                    if (redPacketDetail?.coinType == null) nullAmount else redPacketDetail.coinType)
        }
        adapter?.data = redPacketDetail?.records
        adapter?.notifyDataSetChanged()
    }
}