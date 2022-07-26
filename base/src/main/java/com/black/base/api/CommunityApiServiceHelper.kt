package com.black.base.api

import android.content.Context
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.HttpRequestResultString
import com.black.base.model.community.*
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.RxJavaHelper
import com.black.util.Callback

object CommunityApiServiceHelper {
    fun getFactionList(context: Context?, isShowLoading: Boolean, callback: Callback<HttpRequestResultDataList<FactionItem?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(CommunityApiService::class.java)
                ?.getFactionList()
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    fun getFactionDetail(context: Context?, leagueId: String?, callback: Callback<HttpRequestResultData<FactionItem?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(CommunityApiService::class.java)
                ?.getFactionDetail(leagueId)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, false, callback))
    }

    fun getFactionNotice(context: Context?, language: Int, type: Int, callback: Callback<HttpRequestResultData<FactionNotice?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(CommunityApiService::class.java)
                ?.getFactionNotice(language, type)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, false, callback))
    }

    fun getFactionMemberList(context: Context?, leagueId: String?, callback: Callback<HttpRequestResultDataList<FactionMember?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(CommunityApiService::class.java)
                ?.getFactionMemberList(leagueId)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, false, callback))
    }

    fun getFactionUserInfo(context: Context?, leagueId: String?, callback: Callback<HttpRequestResultData<FactionUserInfo?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(CommunityApiService::class.java)
                ?.getFactionUserInfo(leagueId)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, false, callback))
    }

    fun getFactionConfig(context: Context?, callback: Callback<HttpRequestResultData<FactionConfig?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(CommunityApiService::class.java)
                ?.getFactionConfig()
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, false, callback))
    }

    fun postFactionBecome(context: Context?, leagueId: String?, amount: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(CommunityApiService::class.java)
                ?.postFactionBecome(leagueId, amount)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun postFactionKeep(context: Context?, leagueId: String?, amount: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(CommunityApiService::class.java)
                ?.postFactionKeep(leagueId, amount)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun postFactionLock(context: Context?, leagueId: String?, amount: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(CommunityApiService::class.java)
                ?.postFactionLock(leagueId, amount)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun postFactionUnLock(context: Context?, leagueId: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(CommunityApiService::class.java)
                ?.postFactionUnLock(leagueId)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun sendRedPacket(context: Context?, type: String?, coinType: String?, amount: String?, quantity: Int, title: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(CommunityApiService::class.java)
                ?.sendRedPacket(type, coinType, amount, quantity, title)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun getRedPacketDetail(context: Context?, redPacketId: String?, callback: Callback<HttpRequestResultData<RedPacketDetail?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(CommunityApiService::class.java)
                ?.getRedPacketDetail(redPacketId)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun getRedPacketSummary(context: Context?, redPacketId: String?, callback: Callback<HttpRequestResultData<RedPacketPub?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(CommunityApiService::class.java)
                ?.getRedPacketSummary(redPacketId)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun openRedPacket(context: Context?, redPacketId: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(CommunityApiService::class.java)
                ?.openRedPacket(redPacketId)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, false, callback))
    }
}