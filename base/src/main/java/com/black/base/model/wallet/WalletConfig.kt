package com.black.base.model.wallet

import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.reflect.TypeToken
import java.util.*

class WalletConfig : HashMap<String, JsonArray?>() {
    var userCoinAccountVO: ArrayList<Wallet?>? = null
    var userCoinAccountLeverVO: ArrayList<WalletLever?>? = null

    override fun put(key: String, value: JsonArray?): JsonArray? {
        if (value != null) {
            if (TextUtils.equals(KEY_WALLET, key)) {
                userCoinAccountVO = Gson().fromJson(value, object : TypeToken<ArrayList<Wallet?>?>() {}.type)
            } else if (TextUtils.equals(KEY_WALLET_LEVER, key)) {
                userCoinAccountLeverVO = Gson().fromJson(value, object : TypeToken<ArrayList<WalletLever?>?>() {}.type)
            }
        }
        return super.put(key, value)
    }

    companion object {
        const val KEY_WALLET = "userCoinAccounts"
        const val KEY_WALLET_LEVER = "userCoinAccountsLever"
    }
}