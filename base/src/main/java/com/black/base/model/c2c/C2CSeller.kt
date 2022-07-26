package com.black.base.model.c2c

import com.black.base.model.BaseAdapterItem
import java.util.*

class C2CSeller : BaseAdapterItem() {
    var id //商户主键id
            : String? = null
    var name //商户名
            : String? = null
    var dealRate //比例
            : String? = null
    var amountLimit //限额
            : Double? = null
    var price //价格
            : Double? = null
    var coinType //币种
            : String? = null
    var c2cPaymentTypeEntities //支付方式
            : ArrayList<C2CPayment?>? = null

    override fun getType(): Int {
        return C2C_SELLER
    }

    val paymentTypes: ArrayList<String?>?
        get() {
            if (c2cPaymentTypeEntities == null) {
                return null
            }
            val list = ArrayList<String?>()
            for (c2CPayment in c2cPaymentTypeEntities!!) {
                if (c2CPayment?.type != null && !list.contains(c2CPayment.type)) {
                    list.add(c2CPayment.type)
                }
            }
            return list
        }
}
