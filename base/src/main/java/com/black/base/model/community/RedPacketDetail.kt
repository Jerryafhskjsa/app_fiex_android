package com.black.base.model.community

import java.util.*

class RedPacketDetail {
    var id: String? = null
    var type: String? = null
    var coinType: String? = null
    var userId: String? = null
    var userName: String? = null
    var avatar: String? = null
    var title: String? = null
    var amount: Double? = null
    var quantity //精度
            : Int? = null
    var remainder //
            : Double? = null
    var status // 0 初始值 1 已抢完 2 已过期
            : Int? = null
    var userRecord //自己领取记录
            : RedPacketGotRecord? = null
    var records //所有领取记录
            : ArrayList<RedPacketGotRecord?>? = null
}