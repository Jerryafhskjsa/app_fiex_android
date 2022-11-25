package com.black.frying.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import android.util.Log
import com.black.base.model.SuccessObserver
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.SocketDataContainer
import com.black.base.util.SocketUtil
import com.black.frying.service.socket.*
import com.black.net.websocket.WebSocketHandler
import com.google.gson.Gson
import io.reactivex.Observer

//SOCKET 长连接，获取数据，并保存到数据，供界面使用
class SocketService : Service() {
    companion object {
        private const val TAG = "SocketService"
    }

    private val gson = Gson()
    private var fiexSocketManager: FiexSocketManager? = null
    private var futureSocketManager: FutureSocketManager? = null

    //异步获取数据
    private var handlerThread: HandlerThread? = null
    private var handlerFutureThread: HandlerThread? = null
    private var socketServerHandler: Handler? = null
    private var socketFutureServerHandler: Handler? = null
    private val receiver = SocketCommandBroadcastReceiver()
    private val mHandler = Handler(Handler.Callback { msg ->
        when (msg.what) {
            //移除socket监听
            SocketUtil.COMMAND_REMOVE_SOCKET_LISTENER -> {
                var socketType: String? = null
                if (msg.obj is Bundle) {
                    socketType = (msg.obj as Bundle).getString(SocketUtil.WS_TYPE)
                }
//                fiexSocketManager?.removeListener(socketType)
            }
            //添加socket监听
            SocketUtil.COMMAND_ADD_SOCKET_LISTENER -> {
                var socketType: String? = null
                if (msg.obj is Bundle) {
                    socketType = (msg.obj as Bundle).getString(SocketUtil.WS_TYPE)
                }
//                fiexSocketManager?.addListener(socketType)
            }
            //交易对变更
            SocketUtil.COMMAND_PAIR_CHANGED -> {
                var pair: String? = null
                if (msg.obj is Bundle) {
                    pair = (msg.obj as Bundle).getString(ConstData.PAIR)
                }
                fiexSocketManager?.currentPair = mContext?.let { SocketUtil.getCurrentPair(it) }
                fiexSocketManager?.startListenPair(pair)
            }
            //k线时间段变更
            SocketUtil.COMMAND_KTAB_CHANGED -> {
                if (msg.obj is Bundle) {
                    fiexSocketManager?.kLineTimeStep = (msg.obj as Bundle).getString("timeStep")
                    fiexSocketManager?.kLineTimeStepSecond =
                        (msg.obj as Bundle).getLong("timeStepSecond")
                    fiexSocketManager?.kLineId = (msg.obj as Bundle).getString("kLineId")
                }
                fiexSocketManager?.startListenKLine()
            }
            SocketUtil.COMMAND_RECEIVE, SocketUtil.COMMAND_RESUME -> {
                fiexSocketManager?.startConnectAll()
            }
            SocketUtil.COMMAND_PAUSE, SocketUtil.COMMAND_STOP -> {
                fiexSocketManager?.stopConnectAll()
            }
            SocketUtil.COMMAND_USER_LOGIN -> uSocket?.startListenUserNewConnect()
            SocketUtil.COMMAND_USER_LOGOUT -> uSocket?.startListenUserDisconnect()
            SocketUtil.COMMAND_K_LOAD_MORE -> {
                var kLinePage = -1
                if (msg.obj is Bundle) {
                    kLinePage = (msg.obj as Bundle).getInt("kLinePage", -1)
                }
                qSocket?.loadMoreKLine(kLinePage)
            }
            SocketUtil.COMMAND_KTAB_CLOSE -> {
                qSocket?.kLineTimeStep = null
                qSocket?.kLineTimeStepSecond = 0
                //                    if (qSocket.kLineId != null) {
//                        SocketDataContainer.removeKLineData(qSocket.kLineId);
//                    }
                SocketDataContainer.clearKLineData()
                qSocket?.kLineId = null
                qSocket?.finishListenKLine()
            }
            SocketUtil.COMMAND_DEAL_OPEN -> {
                qSocket?.listenDeal = true
                qSocket?.startListenDeal()
            }
            SocketUtil.COMMAND_DEAL_CLOSE -> {
                qSocket?.listenDeal = false
                qSocket?.finishListenDeal()
            }
            SocketUtil.COMMAND_ORDER_OPEN -> qSocket?.startListenOrder()
            SocketUtil.COMMAND_ORDER_CLOSE -> qSocket?.finishListenOrder()
            SocketUtil.COMMAND_ORDER_RELOAD -> qSocket?.startListenQuotationOrderConnect()
            SocketUtil.COMMAND_QUOTA_OPEN -> qSocket?.startListenQuota()
            SocketUtil.COMMAND_QUOTA_CLOSE -> qSocket?.finishListenQuota()
            SocketUtil.COMMAND_FACTION_OPEN -> factionSocket?.startListenFactionConnect()
            SocketUtil.COMMAND_FACTION_CLOSE -> factionSocket?.startListenFactionDisconnect()
            SocketUtil.COMMAND_LEVER_DETAIL_START -> if (msg.obj is Bundle) {
                val pair = (msg.obj as Bundle).getString(ConstData.PAIR)
                if (pair != null) {
                    uSocket?.startListenLeverDetail(pair)
                }
            }
            SocketUtil.COMMAND_LEVER_DETAIL_FINISH -> uSocket?.finishListenLeverDetail()
            /***fiex***/
//            SocketUtil.COMMAND_CURRENT_PAIR_QUOTA -> fiexSocketManager?.startListenCurrentQuota(SocketUtil.getCurrentPair(mContext!!))
            /***fiex***/
        }
        false
    })
    private var mContext: Context? = null
    private var qSocket: QuotationSocket? = null
    private var uSocket: UserSocket? = null
    private var factionSocket: FactionSocket? = null //笑傲江湖监听
    private var pushSocket: PushSocket? = null  //平台配置推送监听
    private val currentPair: String? = null


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        mContext = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createErrorNotification()
        try {
            if (handlerThread == null) {
                handlerThread =
                    HandlerThread(ConstData.SOCKET_HANDLER, Process.THREAD_PRIORITY_BACKGROUND)
                handlerThread?.start()
            }
            if (handlerFutureThread == null) {
                handlerFutureThread =
                    HandlerThread(ConstData.SOCKET_HANDLER, Process.THREAD_PRIORITY_BACKGROUND)
                handlerFutureThread?.start()
            }
            if (socketServerHandler == null) {
                socketServerHandler = Handler(handlerThread?.looper)
            }
            if (socketFutureServerHandler == null) {
                socketFutureServerHandler = Handler(handlerFutureThread?.looper)
            }
            if (fiexSocketManager == null) {
                fiexSocketManager = FiexSocketManager(mContext!!, socketServerHandler!!)
//                futureSocketManager = FutureSocketManager(mContext!!, socketFutureServerHandler!!)
//                fiexSocketManager?.startConnect()
            }
            if (qSocket == null) {
//                qSocket = QuotationSocket(mContext!!, socketServerHandler!!)
//                qSocket?.start()
            }
            if (uSocket == null) {
//                uSocket = UserSocket(mContext!!, socketServerHandler!!)
//                uSocket?.start()
            }
            if (factionSocket == null) {
//                factionSocket = FactionSocket(mContext!!, socketServerHandler!!)
//                factionSocket?.start()
            }
            if (pushSocket == null) {
//                pushSocket = PushSocket(mContext!!, socketServerHandler!!)
//                pushSocket?.start()
            }

            if (observer == null) {
                observer = createCommandObserver()
            }
            SocketUtil.subscribeCommandObservable(observer)
        } catch (e: Exception) {
            FryingUtil.printError(e)
            //socket初始化失败，关闭service
            stopSelf()
        }
        //注册一个广播接收器，接收控制命令
        val filter = IntentFilter()
        filter.addAction(SocketUtil.ACTION_SOCKET_COMMAND)
        registerReceiver(receiver, filter)
        //        handlerThread = new HandlerThread("test_send", Process.THREAD_PRIORITY_BACKGROUND);
//        handlerThread.start();
//        socketHandler = new Handler(handlerThread.getLooper());
//testSend();
        //Log.e(TAG, "service:" + this + ",onStartCommand==============================================：qSocket" + qSocket);
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        //Log.e(TAG, "onDestroy==============================================：");
        if (socketServerHandler != null) {
            socketServerHandler?.removeMessages(0)
            socketServerHandler = null
        }
        if (handlerThread != null) {
            handlerThread?.quit()
            handlerThread = null
        }
        if (handlerFutureThread != null) {
            handlerFutureThread?.quit()
            handlerFutureThread = null
        }
        try {
            unregisterReceiver(receiver)
        } catch (ignored: Exception) {
        }
        if (qSocket != null) {
            qSocket?.stop()
        }
        if (uSocket != null) {
            uSocket?.stop()
        }
        if (factionSocket != null) {
            factionSocket?.stop()
        }
        if (pushSocket != null) {
            pushSocket?.stop()
        }
        if (observer != null) {
            SocketUtil.removeCommandObservable(observer)
            observer = null
        }
        stopForeground(true)
    }


    private fun createErrorNotification() {
        val notification: Notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "socketServiceNotify",
                "socketService",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            notification = Notification.Builder(this, "socketServiceNotify").build()
            startForeground(1, notification)
        } else {
            notification = Notification.Builder(this).build()
        }
    }

    private fun handleCommand(data: Bundle?) {
        if (data != null) {
            val command = data.getInt(SocketUtil.SOCKET_COMMAND, SocketUtil.COMMAND_NOTHING)
            val other = data[SocketUtil.SOCKET_COMMAND_EXTRAS]
            mHandler.obtainMessage(command, other).sendToTarget()
        }
    }

    private inner class SocketCommandBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            ////Log.e(TAG, "sendDataChangedBroadcast ==========================intent.getAction()：" + intent.getAction());
            if (SocketUtil.ACTION_SOCKET_COMMAND == intent?.action) {
                handleCommand(intent.extras)
            }
        }
    }

    private var observer: Observer<Message?>? = createCommandObserver()
    private fun createCommandObserver(): Observer<Message?> {
        return object : SuccessObserver<Message?>() {
            override fun onSuccess(value: Message?) {
                mHandler.sendMessage(value)
            }
        }
    }
}