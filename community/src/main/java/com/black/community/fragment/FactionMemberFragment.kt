package com.black.community.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.api.CommunityApiServiceHelper
import com.black.base.api.WalletApiServiceHelper
import com.black.base.fragment.BaseFragment
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.listener.OnHandlerListener
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultString
import com.black.base.model.community.FactionConfig
import com.black.base.model.community.FactionItem
import com.black.base.model.community.FactionMember
import com.black.base.model.community.FactionUserInfo
import com.black.base.model.wallet.Wallet
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.view.ConfirmDialog
import com.black.base.view.ConfirmDialog.OnConfirmCallback
import com.black.community.BR
import com.black.community.R
import com.black.community.activity.FactionDetailActivity
import com.black.community.adapter.FactionMemberListAdapter
import com.black.community.databinding.FragmentFactionMemberListBinding
import com.black.community.view.FactionAddCoinWidget
import com.black.lib.refresh.QRefreshLayout
import com.black.net.HttpRequestResult
import com.black.util.NumberUtil
import skin.support.content.res.SkinCompatResources
import java.util.*

class FactionMemberFragment : BaseFragment(), View.OnClickListener, QRefreshLayout.OnRefreshListener {
    private var binding: FragmentFactionMemberListBinding? = null

    private var adapter: FactionMemberListAdapter? = null

    private var ownerAvatarUrl: String? = null
    private var memberAvatarUrl: String? = null
    private var factionItem: FactionItem? = null
    private var loseTime: Long = 0
    private var memberList: ArrayList<FactionMember?>? = null
    private var userInfo: FactionUserInfo? = null
    private val walletCache: MutableMap<String, Wallet?> = HashMap()
    private var fbsWallet: Wallet? = null
    private var factionConfig: FactionConfig? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (binding != null) {
            return binding?.root
        }
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_faction_member_list, null, false)
        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        val decoration = DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(mContext, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.6 * 255).toInt()
        decoration.setDrawable(drawable)
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter = FactionMemberListAdapter(mContext!!, BR.listItemFactionMemberModel, memberList)
        adapter?.setMemberAvatarUrl(memberAvatarUrl)
        adapter?.setOwnerAvatarUrl(ownerAvatarUrl)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setEmptyView(binding?.emptyView?.root)
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.isFocusable = false

        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(mContext!!))
        binding?.refreshLayout?.setOnRefreshListener(this)

        binding?.btnBecome?.setOnClickListener(this)
        binding?.btnKeep?.setOnClickListener(this)
        binding?.btnJoin?.setOnClickListener(this)
        refreshUserInfo(userInfo)
        return binding?.root
    }

    override fun onRefresh() {
        if (mContext is FactionDetailActivity) {
            (mContext as FactionDetailActivity).factionMemberList
        }
    }

    internal abstract inner class FactionAddCoinCommand : Runnable {
        var amount: String? = null
        var factionAddCoinWidget: FactionAddCoinWidget? = null
        var confirmDialog: ConfirmDialog? = null
            private set

        fun setFactionAddCoinWidget(confirmDialog: ConfirmDialog?) {
            this.confirmDialog = confirmDialog
        }
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btn_become) {
            val hint = "输入竞选金额,最小" + NumberUtil.formatNumberNoGroup(factionItem!!.ownerPrice, 0, 2)
            requestToAddCoin("竞选掌门", "竞选金额", hint, object : FactionAddCoinCommand() {
                override fun run() {
                    CommunityApiServiceHelper.postFactionBecome(mContext, if (factionItem == null) null else NumberUtil.formatNumberNoGroup(factionItem!!.id), amount, object : NormalCallback<HttpRequestResultString?>() {
                        override fun callback(returnData: HttpRequestResultString?) {
                            if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                                if (factionAddCoinWidget != null) {
                                    factionAddCoinWidget!!.dismiss()
                                }
                                FryingUtil.showToast(mContext, "操作成功")
                                if (mContext is FactionDetailActivity) {
                                    (mContext as FactionDetailActivity).refreshAllInfo()
                                }
                            } else {
                                FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                            }
                        }
                    })
                }
            })
        } else if (id == R.id.btn_keep) { //判断当前锁仓是否足够继任，如果不足，需要继续加入足够金额,否则直接继任
            val factionAddCoinCommand: FactionAddCoinCommand = object : FactionAddCoinCommand() {
                override fun run() {
                    CommunityApiServiceHelper.postFactionKeep(mContext, if (factionItem == null) null else NumberUtil.formatNumberNoGroup(factionItem!!.id), amount, object : NormalCallback<HttpRequestResultString?>() {
                        override fun callback(returnData: HttpRequestResultString?) {
                            if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                                if (confirmDialog != null) {
                                    confirmDialog!!.dismiss()
                                }
                                FryingUtil.showToast(mContext, "操作成功")
                                if (mContext is FactionDetailActivity) {
                                    (mContext as FactionDetailActivity).refreshAllInfo()
                                }
                            } else {
                                FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                            }
                        }
                    })
                }
            }
            if (factionItem != null && userInfo != null && factionItem!!.ownerPrice != null && userInfo!!.lockAmount != null) {
                val ownerPrice = factionItem!!.ownerPrice!!
                val lockAmount = userInfo!!.lockAmount!!
                if (lockAmount >= ownerPrice) {
                    factionAddCoinCommand.amount = "0"
                    factionAddCoinCommand.run()
                } else {
                    mContext?.let {
                        ConfirmDialog(it, "提示", String.format("连任掌门将加仓 %s FBS,确认连任掌门？", NumberUtil.formatNumberNoGroup(ownerPrice - lockAmount, 0, 2)),
                                object : OnConfirmCallback {
                                    override fun onConfirmClick(confirmDialog: ConfirmDialog) {
                                        factionAddCoinCommand.amount = NumberUtil.formatNumberNoGroup(ownerPrice - lockAmount)
                                        factionAddCoinCommand.run()
                                    }

                                }).show()
                    }
                }
            }
        } else if (id == R.id.btn_join) {
            val hint = "输入竞选金额,最小" + NumberUtil.formatNumberNoGroup(factionConfig!!.minAmount, 0, 2)
            requestToAddCoin("加入门派", "加入门派金额", hint, object : FactionAddCoinCommand() {
                override fun run() {
                    CommunityApiServiceHelper.postFactionLock(mContext, if (factionItem == null) null else NumberUtil.formatNumberNoGroup(factionItem!!.id), amount, object : NormalCallback<HttpRequestResultString?>() {
                        override fun callback(returnData: HttpRequestResultString?) {
                            if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                                if (factionAddCoinWidget != null) {
                                    factionAddCoinWidget!!.dismiss()
                                }
                                FryingUtil.showToast(mContext, "操作成功")
                                if (mContext is FactionDetailActivity) {
                                    (mContext as FactionDetailActivity).refreshAllInfo()
                                }
                            } else {
                                FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                            }
                        }
                    })
                }
            })
        }
    }

    private fun requestToAddCoin(title: String, amountTitle: String, hint: String, nextAction: FactionAddCoinCommand) {
        when {
            fbsWallet == null -> {
                WalletApiServiceHelper.getWalletList(mContext, true, object : NormalCallback<ArrayList<Wallet?>?>() {
                    override fun callback(returnData: ArrayList<Wallet?>?) {
                        if (returnData == null || returnData.isEmpty()) {
                            return
                        }
                        for (wallet in returnData) {
                            wallet?.coinType?.also {
                                walletCache[it] = wallet
                            }
                        }
                        fbsWallet = walletCache["FBS"]
                        if (fbsWallet != null) {
                            requestToAddCoin(title, amountTitle, hint, nextAction)
                        }
                    }
                })
            }
            factionConfig == null -> {
                CommunityApiServiceHelper.getFactionConfig(mContext, object : NormalCallback<HttpRequestResultData<FactionConfig?>?>() {
                    override fun callback(returnData: HttpRequestResultData<FactionConfig?>?) {
                        if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                            factionConfig = returnData.data
                            if (factionConfig != null) {
                                requestToAddCoin(title, amountTitle, hint, nextAction)
                            }
                        } else {
                            FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                        }
                    }
                })
            }
            else -> {
                showAddCoin(title, amountTitle, hint, fbsWallet!!, factionConfig!!, nextAction)
            }
        }
    }

    private fun showAddCoin(title: String, amountTitle: String, hint: String, wallet: Wallet, factionConfig: FactionConfig, nextAction: FactionAddCoinCommand?) {
        if (mContext == null) {
            return
        }
        val factionAddCoinWidget = FactionAddCoinWidget(mContext!!, wallet, factionConfig)
        factionAddCoinWidget.setTitle(title)
        factionAddCoinWidget.setAmountTitle(amountTitle)
        factionAddCoinWidget.setAmountHint(hint)
        factionAddCoinWidget.setOnHandlerListener(object : OnHandlerListener<FactionAddCoinWidget> {
            override fun onCancel(widget: FactionAddCoinWidget) {
                widget.dismiss()
            }

            override fun onConfirm(widget: FactionAddCoinWidget) {
                val amount = widget.amount
                if (amount == null) {
                    FryingUtil.showToast(activity, getString(R.string.alert_c2c_create_amount_error, ""))
                    return
                }
                if (nextAction != null) {
                    nextAction.amount = widget.amountText
                    nextAction.factionAddCoinWidget = widget
                    nextAction.run()
                }
            }
        })
        factionAddCoinWidget.show()
    }

    fun refreshFactionConfig(factionConfig: FactionConfig?) {
        this.factionConfig = factionConfig
    }

    fun refreshFactionWallet(wallet: Wallet?) {
        fbsWallet = wallet
    }

    fun refreshFaction(factionItem: FactionItem?, loseTime: Long) {
        this.factionItem = factionItem
        this.loseTime = loseTime
        refreshUserInfo(userInfo)
    }

    fun refreshMemberList(data: ArrayList<FactionMember?>?) {
        if (binding?.refreshLayout != null) {
            binding?.refreshLayout?.setRefreshing(false)
        }
        memberList = data
        if (adapter != null) {
            adapter?.data = data
            adapter?.notifyDataSetChanged()
        }
    }

    fun refreshUserInfo(userInfo: FactionUserInfo?) {
        this.userInfo = userInfo
        binding?.bottomLayout?.visibility = View.GONE
        binding?.btnBecome?.visibility = View.GONE
        binding?.btnKeep?.visibility = View.GONE
        binding?.btnJoin?.visibility = View.GONE
        if (binding == null || factionItem == null || this.userInfo == null) {
            return
        }
        if (userInfo == null || TextUtils.isEmpty(userInfo.leagueId)) {
            binding?.userInfo?.visibility = View.GONE
        } else {
            binding?.userInfo?.visibility = View.VISIBLE
            binding?.userInfo?.text = String.format("名号：%s 本人存币量：%s",
                    if (userInfo.userName == null) nullAmount else userInfo.userName,
                    if (userInfo.lockAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(userInfo.lockAmount, 8, 0, 2))
        }
        val loginUserInfo = if (mContext == null) null else CookieUtil.getUserInfo(mContext!!)
        if (factionItem != null && factionItem!!.isChoosing((factionItem!!.thisTime
                        ?: 0) + loseTime)) {
            binding?.bottomLayout?.visibility = View.VISIBLE
            binding?.btnBecome?.visibility = View.VISIBLE
            if (loginUserInfo != null && TextUtils.equals(factionItem!!.lastTempOwnerId, loginUserInfo.id)) {
                binding?.btnBecome?.text = "当前我是最高价"
                binding?.btnBecome?.isEnabled = false
            } else {
                binding?.btnBecome?.text = "竞选掌门"
                binding?.btnBecome?.isEnabled = true
            }
        }
        if (factionItem != null && factionItem!!.canLock == 1 && (userInfo == null || TextUtils.isEmpty(userInfo.leagueId))) { //未加入门派 并且可以加入门派
            binding?.bottomLayout?.visibility = View.VISIBLE
            binding?.btnJoin?.visibility = View.VISIBLE
        }
        if (userInfo != null && userInfo.isOwner && factionItem != null && factionItem!!.isKeepDoing((factionItem!!.thisTime
                        ?: 0) + loseTime)) {
            binding?.bottomLayout?.visibility = View.VISIBLE
            binding?.btnKeep?.visibility = View.VISIBLE
        }
    }

    fun setAvatarUrl(memberAvatarUrl: String?, ownerAvatarUrl: String?) {
        this.memberAvatarUrl = memberAvatarUrl
        this.ownerAvatarUrl = ownerAvatarUrl
        if (adapter != null) {
            adapter?.setMemberAvatarUrl(memberAvatarUrl)
            adapter?.setOwnerAvatarUrl(ownerAvatarUrl)
            adapter?.notifyDataSetChanged()
        }
    }
}