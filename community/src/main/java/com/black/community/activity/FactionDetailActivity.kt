package com.black.community.activity

import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.os.SystemClock
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.CommunityApiServiceHelper
import com.black.base.api.WalletApiServiceHelper
import com.black.base.listener.OnHandlerListener
import com.black.base.model.*
import com.black.base.model.community.*
import com.black.base.model.wallet.Wallet
import com.black.base.util.*
import com.black.base.view.DeepControllerWindow
import com.black.base.view.FloatAdView
import com.black.community.R
import com.black.community.databinding.ActivityFactionDetailBinding
import com.black.community.fragment.FactionMemberFragment
import com.black.community.fragment.FactionNoticeFragment
import com.black.community.view.FactionAddCoinWidget
import com.black.im.util.IMHelper
import com.black.net.HttpRequestResult
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.ImageUtil
import com.black.util.NumberUtil
import com.google.android.material.tabs.TabLayout
import io.reactivex.Observer
import org.json.JSONException
import org.json.JSONObject
import java.util.*

//笑傲江湖帮派列表
@Route(value = [RouterConstData.FACTION_DETAIL])
class FactionDetailActivity : BaseActionBarActivity(), View.OnClickListener {
    companion object {
        private val TAB_TITLES = arrayOfNulls<String>(2) //标题
    }

    private var factionId: Long = 0
    private var imageLoader: ImageLoader? = null
    private var dm: DisplayMetrics? = null

    private var binding: ActivityFactionDetailBinding? = null

    private var fragmentList: MutableList<Fragment>? = null
    //    private ChatFragment chatFragment = new ChatFragment();
    private val factionMemberFragment = FactionMemberFragment()
    private val factionNoticeFragment = FactionNoticeFragment()
    private val addJoin = "加入"
    private val exit = "退出"
    private var factionItem: FactionItem? = null
    private var userInfo: FactionUserInfo? = null
    private val walletCache: MutableMap<String?, Wallet?> = HashMap()
    private var fbsWallet: Wallet? = null
    private var factionConfig: FactionConfig? = null
    private var listRefreshTime: Long = 0
    private val handler = Handler()
    private var timerCommand: TimerCommand? = null
    private var userInfoObserver: Observer<String?>? = createUserInfoObserver()
    private fun createUserInfoObserver(): Observer<String?> {
        return object : SuccessObserver<String?>() {
            override fun onSuccess(value: String?) {
                onUserInfoChanged()
            }
        }
    }

    //成员变更
    private var memberUpdateObserver: Observer<Long?>? = createMemberUpdateObserver()

    private fun createMemberUpdateObserver(): Observer<Long?> {
        return object : SuccessObserver<Long?>() {
            override fun onSuccess(value: Long?) {
                if (value != null && factionId == value) {
                    factionMemberList
                }
            }
        }
    }

    //门派变更
    private var factionUpdateObserver: Observer<JSONObject?>? = createFactionUpdateObserver()

    private fun createFactionUpdateObserver(): Observer<JSONObject?> {
        return object : SuccessObserver<JSONObject?>() {
            override fun onSuccess(value: JSONObject?) {
                val jsonId = value?.optLong("id", -1) ?: -1
                if (jsonId != -1L && factionId == jsonId) {
                    factionDetail
                    factionUserInfo
                }
            }
        }
    }

    //掌门变更
    private var ownerUpdateObserver: Observer<Long?>? = createOwnerUpdateObserver()

    private fun createOwnerUpdateObserver(): Observer<Long?> {
        return object : SuccessObserver<Long?>() {
            override fun onSuccess(value: Long?) {
                if (value != null && factionId == value) {
                    factionMemberList
                    factionDetail
                    factionUserInfo
                }
            }
        }
    }

    private var chatFloatAdView: FloatAdView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TAB_TITLES[0] = "帮派成员"
        TAB_TITLES[1] = "帮派公告"
        factionId = intent.getLongExtra(ConstData.FACTION_ID, -1)
        if (factionId == -1L) {
            finish()
            return
        }
        imageLoader = ImageLoader(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_faction_detail)
        binding?.factionMore?.setOnClickListener(this)
        dm = resources.displayMetrics
        var params = binding?.bannerLayout?.layoutParams
        if (params == null) {
            params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (dm?.widthPixels!! * 0.56).toInt())
        } else {
            params.height = (dm?.widthPixels!! * 0.56).toInt()
        }
        binding?.bannerLayout?.layoutParams = params
        params = binding?.bannerImage?.layoutParams
        if (params == null) {
            params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (dm?.widthPixels!! * 0.56).toInt())
        } else {
            params.height = (dm?.widthPixels!! * 0.56).toInt()
        }
        binding?.bannerImage?.layoutParams = params
        binding?.tab?.setSelectedTabIndicatorHeight(0)
        binding?.tab?.tabMode = TabLayout.MODE_FIXED
        for (i in TAB_TITLES.indices) {
            val tab = binding?.tab?.newTab()?.setText(TAB_TITLES[i])
            tab?.setCustomView(R.layout.view_faction_detail_tab)
            val textView = tab?.customView!!.findViewById<View>(android.R.id.text1) as TextView
            textView.text = TAB_TITLES[i]
            binding?.tab?.addTab(tab)
        }
        binding?.tab?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val view = tab.customView
                val textView = if (view == null) null else view.findViewById<View>(android.R.id.text1) as TextView
                if (textView != null) {
                    textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD), Typeface.BOLD)
                    textView.postInvalidate()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val view = tab.customView
                val textView = if (view == null) null else view.findViewById<View>(android.R.id.text1) as TextView
                if (textView != null) {
                    textView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL), Typeface.NORMAL)
                    textView.postInvalidate()
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        initFragmentList()
        binding?.viewPager?.adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment {
                return fragmentList!![position]
            }

            override fun getCount(): Int {
                return fragmentList!!.size
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return TAB_TITLES[position]
            }

            override fun restoreState(state: Parcelable?, loader: ClassLoader?) {}
        }
        binding?.viewPager?.offscreenPageLimit = 2
        //        binding?.viewPager?.setAdapter(new BaseFragmentViewPagerAdapter(this, getSupportFragmentManager(), fragmentList));
        CommonUtil.joinTabLayoutViewPager(binding?.tab, binding?.viewPager)
        showChatFloatAdView()
    }

    override fun isStatusBarDark(): Boolean {
        return true
    }

    override fun onResume() {
        super.onResume()
        sendSocketCommandChangedBroadcast(SocketUtil.COMMAND_FACTION_OPEN)
        if (userInfoObserver == null) {
            userInfoObserver = createUserInfoObserver()
        }
        SocketDataContainer.subscribeUserInfoObservable(userInfoObserver)
        if (factionUpdateObserver == null) {
            factionUpdateObserver = createFactionUpdateObserver()
        }
        factionNotice
        factionDetail
        factionMemberList
        factionUserInfo
        getFactionConfig()
        walletList
        checkListAddTimer()
    }

    override fun onStop() {
        super.onStop()
        sendSocketCommandChangedBroadcast(SocketUtil.COMMAND_FACTION_CLOSE)
        if (userInfoObserver != null) {
            SocketDataContainer.removeUserInfoObservable(userInfoObserver)
        }
        if (timerCommand != null) {
            handler.removeCallbacks(timerCommand)
            timerCommand = null
        }
    }

    override fun onClick(v: View) {
        if (v.id == R.id.faction_more) {
            if (userInfo != null && userInfo!!.leagueId != null) {
                val json = JSONObject()
                try {
                    json.accumulate("id", "" + factionId)
                } catch (e: JSONException) {
                }
                SocketDataContainer.onFactionUpdate(json)
                //已加入才显示弹出窗口
                val item = ArrayList<String?>()
                if (factionItem != null) {
                    if (factionItem!!.canLock != null && factionItem!!.canLock == 1) {
                        item.add(addJoin)
                    }
                    if (factionItem!!.status != null && factionItem!!.status == 1) {
                        item.add(exit)
                    }
                }
                DeepControllerWindow(this, null, null, item, object : DeepControllerWindow.OnReturnListener<String?> {
                    override fun onReturn(window: DeepControllerWindow<String?>, item: String?) {
                        window.dismiss()
                        if (TextUtils.equals(addJoin, item)) {
                            addJoin()
                        } else if (TextUtils.equals(exit, item)) {
                            exit()
                        }
                    }

                }).show()
            }
        }
    }

    private fun initFragmentList() {
        if (fragmentList == null) {
            fragmentList = ArrayList()
        }
        fragmentList!!.clear()
        //        ChatInfo chatInfo = new ChatInfo();
//        chatInfo.setType(TIMConversationType.Group);
//        chatInfo.setId("@TGS#3TJVTVBGJ");
//        chatInfo.setChatName("测试实施");
//        Bundle bundle = new Bundle();
//        bundle.putSerializable(IMConstData.CHAT_INFO, chatInfo);
//        chatFragment.setArguments(bundle);
//        chatFragment.setOnChatLayoutListener(new ChatFragment.OnChatLayoutListener() {
//            @Override
//            public void onInitChatLayout(ChatLayout chatLayout) {
//                chatLayout.getTitleBar().setVisibility(View.GONE);
//                chatLayout.getMessageLayout().setLeftNameVisibility(View.VISIBLE);
//                chatLayout.getMessageLayout().setLeftIconVisibility(View.VISIBLE);
//                chatLayout.getMessageLayout().setRightIconVisibility(View.VISIBLE);
//                chatLayout.getInputLayout().disableSendFileAction(true);
//            }
//        });
//        fragmentList.add(chatFragment);
        fragmentList!!.add(factionMemberFragment)
        fragmentList!!.add(factionNoticeFragment)
    }

    fun refreshAllInfo() {
        factionDetail
        factionMemberList
        factionUserInfo
        getFactionConfig()
        walletList
    }

    //补充加入锁币
    private fun addJoin() {
        if (fbsWallet == null || factionConfig == null) {
            return
        }
        val factionAddCoinWidget = FactionAddCoinWidget(mContext, fbsWallet!!, factionConfig!!)
        factionAddCoinWidget.setTitle("加入")
        factionAddCoinWidget.setAmountTitle("加入金额")
        factionAddCoinWidget.setAmountHint("输入加入金额")
        factionAddCoinWidget.setOnHandlerListener(object : OnHandlerListener<FactionAddCoinWidget> {
            override fun onCancel(widget: FactionAddCoinWidget) {
                widget.dismiss()
            }

            override fun onConfirm(widget: FactionAddCoinWidget) {
                val amount = widget.amount
                if (amount == null) {
                    FryingUtil.showToast(mContext, getString(R.string.alert_c2c_create_amount_error, ""))
                    return
                }
                CommunityApiServiceHelper.postFactionLock(mContext, if (factionItem == null) null else NumberUtil.formatNumberNoGroup(factionItem!!.id), widget.amountText, object : NormalCallback<HttpRequestResultString?>(mContext!!) {
                    override fun callback(returnData: HttpRequestResultString?) {
                        if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                            widget.dismiss()
                            refreshAllInfo()
                        } else {
                            FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                        }
                    }
                })
            }
        })
        factionAddCoinWidget.show()
    }

    //退出
    private fun exit() {
        CommunityApiServiceHelper.postFactionUnLock(this, if (factionId == -1L) "" else NumberUtil.formatNumberNoGroup(factionId), object : NormalCallback<HttpRequestResultString?>(mContext!!) {
            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    factionNotice
                    factionDetail
                    factionMemberList
                    factionUserInfo
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    private val walletList: Unit
        get() {
            WalletApiServiceHelper.getWalletList(this, true, object : Callback<ArrayList<Wallet?>?>() {
                override fun error(type: Int, error: Any) {}
                override fun callback(returnData: ArrayList<Wallet?>?) {
                    if (returnData == null || returnData.isEmpty()) {
                        return
                    }
                    for (wallet in returnData) {
                        walletCache.let {
                            walletCache[wallet?.coinType] = wallet
                        }
                    }
                    fbsWallet = walletCache["FBS"]
                    factionMemberFragment.refreshFactionWallet(fbsWallet)
                }
            })
        }

    private fun getFactionConfig() {
        CommunityApiServiceHelper.getFactionConfig(mContext, object : NormalCallback<HttpRequestResultData<FactionConfig?>?>(mContext!!) {
            override fun callback(returnData: HttpRequestResultData<FactionConfig?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    factionConfig = returnData.data
                    factionMemberFragment.refreshFactionConfig(factionConfig)
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    private val factionDetail: Unit
        get() {
            CommunityApiServiceHelper.getFactionDetail(this, if (factionId == -1L) "" else NumberUtil.formatNumberNoGroup(factionId), object : NormalCallback<HttpRequestResultData<FactionItem?>?>(mContext!!) {
                override fun callback(returnData: HttpRequestResultData<FactionItem?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        factionItem = returnData.data
                        if (factionItem != null) {
                            factionItem!!.thisTime = System.currentTimeMillis()
                        }
                        listRefreshTime = SystemClock.elapsedRealtime()
                        imageLoader?.loadImage(binding?.bannerImage, if (factionItem == null) null else factionItem!!.mobileBanner)
                        factionMemberFragment.refreshFaction(factionItem, loseTime)
                        factionMemberFragment.setAvatarUrl(if (factionItem == null) null else factionItem!!.memberAvatar, if (factionItem == null) null else factionItem!!.ownerAvatar)
                        checkListAddTimer()
                    } else {
                        FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                    }
                }
            })
        }

    private val factionNotice: Unit
        get() {
            CommunityApiServiceHelper.getFactionNotice(this, 1, 55, object : NormalCallback<HttpRequestResultData<FactionNotice?>?>(mContext!!) {
                override fun callback(returnData: HttpRequestResultData<FactionNotice?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        factionNoticeFragment.setWebViewData(returnData.data?.text)
                    } else {
                        FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                    }
                }
            })
        }

    val factionMemberList: Unit
        get() {
            CommunityApiServiceHelper.getFactionMemberList(this, if (factionId == -1L) "" else NumberUtil.formatNumberNoGroup(factionId), object : NormalCallback<HttpRequestResultDataList<FactionMember?>?>(mContext!!) {
                override fun error(type: Int, error: Any?) {
                    super.error(type, error)
                    factionMemberFragment.refreshMemberList(null)
                }

                override fun callback(returnData: HttpRequestResultDataList<FactionMember?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        factionMemberFragment.refreshMemberList(returnData.data)
                    } else {
                        FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                    }
                }
            })
        }

    private val factionUserInfo: Unit
        get() {
            CommunityApiServiceHelper.getFactionUserInfo(this, if (factionId == -1L) "" else NumberUtil.formatNumberNoGroup(factionId), object : NormalCallback<HttpRequestResultData<FactionUserInfo?>?>(mContext!!) {
                override fun callback(returnData: HttpRequestResultData<FactionUserInfo?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        userInfo = returnData.data
                        if (userInfo == null || TextUtils.isEmpty(userInfo!!.leagueId)) {
                            binding?.factionMore?.visibility = View.GONE
                        } else {
                            binding?.factionMore?.visibility = View.VISIBLE
                        }
                        factionMemberFragment.refreshUserInfo(userInfo)
                        checkListAddTimer()
                    } else {
                        FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                    }
                }
            })
        }

    private fun checkListAddTimer() {
        val hasNew = checkAndStartCountDown()
        if (hasNew) {
            timerCommand = TimerCommand()
            handler.post(timerCommand)
        }
    }

    //检查是否开启倒计时
    private fun checkAndStartCountDown(): Boolean {
        if (isFinishing || isDestroyed) {
            return false
        }
        if (factionItem == null) {
            binding?.countDown?.visibility = View.GONE
            return false
        }
        val status = factionItem!!.ownerStatusCode
        val thisTime = (factionItem!!.thisTime ?: 0L) + loseTime
        if (factionItem!!.isWaiting(thisTime) || factionItem!!.isChoosing(thisTime)) {
            binding?.countDown?.visibility = View.VISIBLE
            val endTime = if (status == 0) (factionItem!!.nextOwnerChangeTime
                    ?: 0) else if (status == 2) (factionItem!!.nextOwnerChangeFinishedTime
                    ?: 0) else 0
            if (factionItem!!.isWaiting(thisTime)) {
                binding?.countDown?.text = String.format("竞选 %s 后开始", getTimeDisplay(thisTime, endTime))
            } else if (factionItem!!.isChoosing(thisTime)) {
                binding?.countDown?.text = String.format("竞选 %s 后结束", getTimeDisplay(thisTime, endTime))
            }
            return true
        } else if (factionItem!!.isKeepDoing(thisTime) && userInfo != null && userInfo!!.isOwner) {
            binding?.countDown?.visibility = View.VISIBLE
            val endTime = if (status == 3) (factionItem!!.nextOwnerEndTime ?: 0) else 0
            if (factionItem!!.isKeepDoing(thisTime)) {
                binding?.countDown?.text = String.format("连任掌门 %s 后结束", getTimeDisplay(thisTime, endTime))
            }
            return true
        }
        binding?.countDown?.visibility = View.GONE
        return false
    }

    private val loseTime: Long
        get() = SystemClock.elapsedRealtime() - listRefreshTime

    private fun getTimeDisplay(thisTime: Long, finishTime: Long): String {
        var result = "00:00:00"
        var rangeTime = finishTime - thisTime
        if (rangeTime <= 0) {
            return "00:00:00"
        }
        if (rangeTime > 1000) {
            rangeTime /= 1000
            val d = (rangeTime / 24 / 3600).toInt()
            rangeTime %= (24 * 3600)
            val sb = StringBuilder()
            if (d > 0) {
                return d.toString() + "天"
            }
            val h = (rangeTime / 3600).toInt()
            rangeTime %= 3600
            val m = (rangeTime / 60).toInt()
            rangeTime %= 60
            val s = rangeTime.toInt()
            sb.append(CommonUtil.twoBit(h)).append(":").append(CommonUtil.twoBit(m)).append(":").append(CommonUtil.twoBit(s))
            result = sb.toString()
        }
        return result
    }

    //用户信息被修改，刷新钱包
    private fun onUserInfoChanged() {
        walletList
    }

    //弹窗悬浮聊天界面
    private fun showChatFloatAdView() {
        if (chatFloatAdView == null) {
            chatFloatAdView = FloatAdView(this, 0, 0)
            chatFloatAdView!!.setOnClickListener(View.OnClickListener {
                fryingHelper.checkUserAndDoing(Runnable {
                    val loginUserInfo = CookieUtil.getUserInfo(mContext)
                    if (loginUserInfo == null) {
                        FryingUtil.showToast(mContext, "请先登录后使用")
                    } else {
                        val userIdHeader = IMHelper.getUserIdHeader(mContext)
                        val userId = loginUserInfo.id
                        //线上： fbs_REAL_League_{id}
                        //测试、stage: fbs_DEV_League_{id}
                        //                                String groupId = (FryingUtil.isReal(mContext) ? "fbs_REAL_League_" : "fbs_DEV_League_") + CommonUtil.formatNumberNoGroup(factionId);
                        val groupId = if (factionItem == null) nullAmount else factionItem!!.timRoomId
                        val groupName = if (factionItem == null) nullAmount else factionItem!!.name
                        val bundle = Bundle()
                        bundle.putString(ConstData.IM_GROUP_ID, groupId)
                        bundle.putString(ConstData.IM_GROUP_NAME, groupName)
                        bundle.putParcelable(ConstData.FACTION_ITEM, factionItem)
                        bundle.putParcelable(ConstData.FACTION_USER_INFO, userInfo)
                        bundle.putLong(ConstData.FACTION_ID, factionId)
                        IMHelper.startWithIMGroupActivity(this@FactionDetailActivity, mContext, userIdHeader + userId, groupId, RouterConstData.FACTION_CHAT, bundle, null, null)
                    }
                }, 0)
            })
            val bitmap = ImageUtil.getBitmapFromRes(applicationContext, R.drawable.icon_chat_faction)
            chatFloatAdView!!.setLottieBitmap(bitmap)
        }
        chatFloatAdView!!.show(binding?.rootView)
    }

    private fun closeChatFloatAdView() {
        if (chatFloatAdView != null) {
            chatFloatAdView!!.close(binding?.rootView)
        }
    }

    private inner class TimerCommand : Runnable {
        override fun run() {
            if (checkAndStartCountDown()) {
                factionMemberFragment.refreshFaction(factionItem, loseTime)
                handler.postDelayed(this, 1000)
            }
        }
    }
}