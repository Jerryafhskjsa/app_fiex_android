package com.black.base.model.c2c

import java.util.*

class C2CDetail : C2COrder() {
    var totalPrice: Double? = null
    var expireTime: Long? = null
    var note: String? = null
    var contact: String? = null
    var merchantUserId: String? = null
    var selectPayment: C2CPayment? = null
    var userPayment //用户支付列表
            : ArrayList<C2CPayment?>? = null
    var storePayment //商户支付列表
            : ArrayList<C2CPayment?>? = null
}
