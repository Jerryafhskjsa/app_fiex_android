package com.black.frying.service.socket

import android.content.Context
import android.os.Handler
import android.text.TextUtils
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.util.SocketDataContainer
import com.black.base.util.UrlConfig
import com.black.util.CommonUtil
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONException
import org.json.JSONObject

class FactionSocket(context: Context, handler: Handler) : FryingSocket(context, handler) {
    companion object {
        private const val TAG = "FactionSocket"
        const val FACTION_OWNER_CHANGE = "leagueOwnerChange"
        const val FACTION_UPDATE = "leagueUpdate"
        const val FACTION_MEMBER_UPDATE = "leagueMemberUpdate"
    }

    //笑傲江湖掌门信息变更
    private val onFactionOwnerChangeListener = Emitter.Listener { args ->
        for (`object` in args) {
            ////Log.e(TAG, "onFactionOwnerChangeListener ==============================================\n obj：" + object);
            val factionId = CommonUtil.parseLong(`object`)
            if (factionId != null) {
                SocketDataContainer.onFactionOwnerUpdate(factionId)
            }
        }
    }

    //笑傲江湖门派信息变更
    private val onFactionUpdateListener = Emitter.Listener { args ->
        for (`object` in args) {
            ////Log.e(TAG, "onFactionUpdateListener ==============================================\n obj：" + object);
            var data: JSONObject? = null
            if (`object` is String) {
                try {
                    data = JSONObject(`object`.toString())
                } catch (e: JSONException) {
                    FryingUtil.printError(e)
                }
            } else if (`object` is JSONObject) {
                data = `object`
            }
            if (data != null) {
                SocketDataContainer.onFactionUpdate(data)
            }
        }
    }

    //笑傲江湖门派成员变更
    private val onFactionMemberUpdateListener = Emitter.Listener { args ->
        for (`object` in args) {
            val factionId = CommonUtil.parseLong(`object`)
            if (factionId != null) {
                SocketDataContainer.onFactionMemberUpdate(factionId)
            }
        }
    }

    init {
        emitterListenerMap[FACTION_OWNER_CHANGE] = onFactionOwnerChangeListener
        emitterListenerMap[FACTION_UPDATE] = onFactionUpdateListener
        emitterListenerMap[FACTION_MEMBER_UPDATE] = onFactionMemberUpdateListener
    }

    override fun getTag(): String {
        return TAG
    }

    @Throws(Exception::class)
    override fun initSocket(): Socket {
        return IO.socket(UrlConfig.getSocketHost(context) + "/league", socketOptions)
    }

    //请求监听笑傲江湖连接
    fun startListenFactionConnect() {
        if (socket == null) {
            return
        }
        val token = CookieUtil.getToken(context)
        if (!TextUtils.isEmpty(token)) {
            try {
                val jsonObject = JSONObject()
                socket.emit("leagueConnect", jsonObject)
            } catch (e: Exception) {
                FryingUtil.printError(e)
            }
            for (event in emitterListenerMap.keys) {
                socket.on(event, emitterListenerMap[event])
            }
        }
    }

    //请求监听笑傲江湖断开
    fun startListenFactionDisconnect() {
        if (socket == null) {
            return
        }
        for (event in emitterListenerMap.keys) {
            socket.off(event, emitterListenerMap[event])
        }
    }
}