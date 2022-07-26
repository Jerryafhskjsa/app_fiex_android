package com.black.base.model

import android.text.TextUtils

class MenuEntity(var code: String, var text: String) {
    override fun toString(): String {
        return text
    }

    override fun equals(other: Any?): Boolean {
        return if (other is MenuEntity) {
            TextUtils.equals(code, other.code)
        } else false
    }

}