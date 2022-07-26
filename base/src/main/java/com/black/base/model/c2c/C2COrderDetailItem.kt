package com.black.base.model.c2c

import com.black.base.model.BaseAdapterItem
import java.util.*

class C2COrderDetailItem : BaseAdapterItem() {
    //    orderId  订单id
//    type  0-用户咨询，1-商家回复
//    note  消息内容
//    createdTime 创建时间
//    isMerchant 是否商户 商户/用户  true/false
//    type和isMerchant共同来判断消息展示在左边还是右边
    var id: Long? = 0
    var orderId: String? = null
    internal var type = 0
    var note: String? = null
    var createTime: Long? = 0
    var merchant = false
    var merchantName: String? = null
    var userName: String? = null

    override fun getType(): Int {
        return if (merchant) {
            if (1 == type) C2C_ORDER_ITEM_M_RIGHT else C2C_ORDER_ITEM_C_LEFT
        } else {
            if (1 == type) C2C_ORDER_ITEM_M_LEFT else C2C_ORDER_ITEM_C_RIGHT
        }
    }

    override fun equals(obj: Any?): Boolean {
        return if (obj == null || obj !is C2COrderDetailItem) {
            false
        } else id == obj.id
    }

    companion object {
        @JvmStatic
        val comparator: Comparator<C2COrderDetailItem?> = Comparator { o1, o2 ->
            if (o1 == null || o2 == null || o1.createTime == null || o2.createTime == null) 0 else if (o1.createTime == o2.createTime) 0 else if (o1.createTime!! > o2.createTime!!) 1 else -1 //倒叙排列
        }
    }
}
