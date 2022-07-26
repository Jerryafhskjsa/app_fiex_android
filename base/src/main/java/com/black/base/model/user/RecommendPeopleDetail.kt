package com.black.base.model.user

import android.content.Context
import com.black.base.R
import com.black.base.model.BaseAdapterItem

class RecommendPeopleDetail : BaseAdapterItem() {
    var createTime: Long? = null
    var name: String? = null
    var level: String? = null
    var username: String? = null
    var verified: Boolean? = null
    fun getVerifyDisplay(context: Context): String {
        return if (verified != null && verified!!) context.getString(R.string.has_real_name) else context.getString(R.string.not_real_name)
    }
}