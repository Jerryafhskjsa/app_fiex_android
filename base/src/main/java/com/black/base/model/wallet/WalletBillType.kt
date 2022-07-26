package com.black.base.model.wallet

import android.content.Context
import com.black.base.R
import com.black.base.lib.filter.FilterEntity
import java.util.*

class WalletBillType {
    var id: Long? = null
    var billType: String? = null
    var zh: String? = null
    var en: String? = null
    var kr: String? = null
    var ja: String? = null
    var userVisible: Boolean? = null

    constructor() {}
    constructor(id: Long?, billType: String?, zh: String?, en: String?, kr: String?, ja: String?, userVisible: Boolean?) {
        this.id = id
        this.billType = billType
        this.zh = zh
        this.en = en
        this.kr = kr
        this.ja = ja
        this.userVisible = userVisible
    }

    companion object {
        const val KEY = "WalletBillType"
        lateinit var ALL: WalletBillType
        val typeNameMap = HashMap<String, String>()
        fun init(context: Context) {
            ALL = WalletBillType(null, null, context.getString(R.string.total), null, null, null, null)
        }

        fun getDefaultFilterEntity(context: Context?, list: List<WalletBillType?>?, selectItem: WalletBillType?): FilterEntity<WalletBillType> {
            val entity: FilterEntity<WalletBillType> = FilterEntity()
            entity.key = KEY
            val data = ArrayList<WalletBillType>()
            data.add(ALL)
            if (list != null) {
                for (walletBillType in list) {
                    if (walletBillType?.userVisible != null && walletBillType.userVisible!!) {
                        data.add(walletBillType)
                    }
                }
            }
            entity.data = data
            entity.selectItem = selectItem
            return entity
        }
    }

    override fun toString(): String {
        return zh!!
    }
}