package com.black.base.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.black.base.model.socket.PairStatus
import com.black.base.model.user.UserInfo
import com.black.base.util.ConstData.CURRENT_PAIR_LEVER
import com.google.gson.Gson
import java.util.*
import kotlin.collections.ArrayList

object CookieUtil {
    fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
    }

    fun saveCookie(context: Context, key: String?, value: String?) {
        val prefs = getSharedPreferences(context)
        prefs.edit().putString(key, value).apply()
    }

    fun getCookie(context: Context, key: String?): String? {
        return getSharedPreferences(context).getString(key, null)
    }

    fun checkSetKey(context: Context, key: String?): Boolean {
        val prefs = getSharedPreferences(context)
        return prefs.contains(key)
    }

    /**
     * 删除用户信息
     *
     * @param context
     */
    fun deleteUserInfo(context: Context) {
        val prefs = getSharedPreferences(context)
        prefs.edit().remove(ConstData.USER_INFO).apply()
    }

    /**
     * 保存用户信息
     *
     * @param context
     * @param userInfo
     */
    fun saveUserInfo(context: Context, userInfo: UserInfo?) {
        val prefs = getSharedPreferences(context)
        prefs.edit().putString(ConstData.USER_INFO, if (userInfo == null) null else Gson().toJson(userInfo)).apply()
    }

    /**
     * 获取保存的用户信息
     *
     * @param context
     * @return
     */
    fun getUserInfo(context: Context): UserInfo? {
        val prefs = getSharedPreferences(context)
        val userInfoJsonString = prefs.getString(ConstData.USER_INFO, null)
        return try {
            Gson().fromJson(userInfoJsonString, UserInfo::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 保存用户名信息
     *
     * @param context
     * @param userName
     */
    fun saveUserName(context: Context, userName: String?) {
        val prefs = getSharedPreferences(context)
        prefs.edit().putString(ConstData.USER_NAME, userName).apply()
    }

    /**
     * 获取保存的用户信息
     *
     * @param context
     * @return
     */
    fun getUserName(context: Context): String? {
        return getSharedPreferences(context).getString(ConstData.USER_NAME, null)
    }

    /**
     * 保存用户信息
     *
     * @param context
     * @param userName
     */
    fun saveUserId(context: Context, userName: String?) {
        val prefs = getSharedPreferences(context)
        prefs.edit().putString(ConstData.USER_ID, userName).apply()
    }

    /**
     * 获取保存的用户信息
     *
     * @param context
     * @return
     */
    fun getUserId(context: Context): String? {
        return getSharedPreferences(context).getString(ConstData.USER_ID, null)
    }

    //移除登录token
    fun deleteToken(context: Context) {
        val editor = getSharedPreferences(context).edit()
        editor.remove(ConstData.TOKEN)
        editor.commit()
    }



    //保存登录token
    fun saveToken(context: Context, token: String?) {
        val editor = getSharedPreferences(context).edit()
        editor.putString(ConstData.TOKEN, token)
        editor.commit()
    }

    //获取登录token
    fun getToken(context: Context): String? {
        //        return "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxNTg4MTA2NzE5NyIsInVzZXJJZCI6IjQ4IiwiaWF0IjoxNTcxODE0OTgyLCJleHAiOjE1NzE4NzI1ODJ9.v68pROWQaVPyD2KH-SszD0ShgAp00nzMjJWhm0leAUI";
        return getSharedPreferences(context).getString(ConstData.TOKEN, null)
    }

    /**
     * 获取账号保护类型
     *
     * @param context
     * @return 0 没有保护，1 手势密码，2 指纹
     */
    fun getAccountProtectType(context: Context): Int {
        return getSharedPreferences(context).getInt(ConstData.ACCOUNT_PROTECT_TYPE, ConstData.ACCOUNT_PROTECT_NONE)
    }

    /**
     * 设置账号保护类型
     *
     * @param context
     * @param type    0 没有保护，1 手势密码，2 指纹
     * @return
     */
    fun setAccountProtectType(context: Context, type: Int) {
        getSharedPreferences(context).edit().putInt(ConstData.ACCOUNT_PROTECT_TYPE, type).apply()
    }

    /**
     * 获取账号保护跳过
     *
     * @param context
     * @return
     */
    fun getAccountProtectJump(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(ConstData.ACCOUNT_PROTECT_JUMP, false)
    }

    /**
     * 设置账号保护跳过
     *
     * @param context
     * @param isJump  是否跳过
     * @return
     */
    fun setAccountProtectJump(context: Context, isJump: Boolean) {
        getSharedPreferences(context).edit().putBoolean(ConstData.ACCOUNT_PROTECT_JUMP, isJump).apply()
    }

    /**
     * 获取手势密码
     *
     * @param context
     * @return
     */
    fun getGesturePassword(context: Context): String? {
        return getSharedPreferences(context).getString(ConstData.GESTURE_PASSWORD, null)
    }

    /**
     * 记录手势密码
     *
     * @param context
     * @param gesturePassword
     */
    fun setGesturePassword(context: Context, gesturePassword: String?) {
        getSharedPreferences(context).edit().putString(ConstData.GESTURE_PASSWORD, gesturePassword).apply()
    }

    /**
     * 获取当前交易对
     *
     * @param context
     * @return
     */
    fun getCurrentPair(context: Context): String? {
        return getSharedPreferences(context).getString(ConstData.CURRENT_PAIR, null)
    }

    /**
     * 保存当前交易对
     *
     * @param context
     * @param pair
     * @return
     */
    fun setCurrentPair(context: Context, pair: String?) {
        getSharedPreferences(context).edit().putString(ConstData.CURRENT_PAIR, pair).apply()
    }

    /**
     * 获取当前U本位交易对
     *
     * @param context
     * @return
     */
    fun getCurrentFutureUPair(context: Context): String? {
        return getSharedPreferences(context).getString(ConstData.CURRENT_FUTURE_U_PAIR, null)
    }

    /**
     * 保存当前U本位交易对
     *
     * @param context
     * @param pair
     * @return
     */
    fun setCurrentFutureUPair(context: Context, pair: String?) {
        getSharedPreferences(context).edit().putString(ConstData.CURRENT_FUTURE_U_PAIR, pair).apply()
    }

    /**
     * 保存当前u本位交易对信息
     *
     * @param context
     * @param userInfo
     */
    fun setCurrentFutureUPairObjrInfo(context: Context, pair: PairStatus?) {
        val prefs = getSharedPreferences(context)
        prefs.edit().putString(ConstData.CURRENT_FUTURE_U_PAIR_OBJ, if (pair == null) null else Gson().toJson(pair)).apply()
    }

    /**
     * 获取保存当前u本位交易对信息
     *
     * @param context
     * @return
     */
    fun getCurrentFutureUPairObjrInfo(context: Context): PairStatus? {
        val prefs = getSharedPreferences(context)
        val pairStatusJsonString = prefs.getString(ConstData.CURRENT_FUTURE_U_PAIR_OBJ, null)
        return try {
            Gson().fromJson(pairStatusJsonString, PairStatus::class.java)
        } catch (e: Exception) {
            null
        }
    }


    /**
     * 获取当前杠杆交易对
     *
     * @param context
     * @return
     */
    fun getCurrentPairLever(context: Context): String? {
        return getSharedPreferences(context).getString(CURRENT_PAIR_LEVER, null)
    }

    /**
     * 保存当前杠杆交易对
     *
     * @param context
     * @param pair
     * @return
     */
    fun setCurrentPairLever(context: Context, pair: String?) {
        getSharedPreferences(context).edit().putString(CURRENT_PAIR_LEVER, pair).apply()
    }

    fun setMainEyeStatus(context: Context, isChecked: Boolean) {
        getSharedPreferences(context).edit().putBoolean(ConstData.MAIN_EYE, isChecked).apply()
    }

    fun getMainEyeStatus(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(ConstData.MAIN_EYE, true)
    }

    fun setShowTradeWarning(context: Context, isShow: Boolean) {
        getSharedPreferences(context).edit().putBoolean(ConstData.SHOW_TRADE_WARNING, isShow).apply()
    }

    fun getShowTradeWarning(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(ConstData.SHOW_TRADE_WARNING, false)
    }

    fun setWalletCoinFilter(context: Context, isFilter: Boolean) {
        getSharedPreferences(context).edit().putBoolean(ConstData.WALLET_COIN_FILTER, isFilter).apply()
    }

    fun getWalletCoinFilter(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(ConstData.WALLET_COIN_FILTER, false)
    }

    fun setUpdateJumpVersion(context: Context, version: String?) {
        getSharedPreferences(context).edit().putString(ConstData.UPDATE_VERSION, version).apply()
    }

    fun getUpdateJumpVersion(context: Context): String? {
        return getSharedPreferences(context).getString(ConstData.UPDATE_VERSION, null)
    }

    fun hasSetNightMode(context: Context): Boolean {
        return checkSetKey(context, ConstData.NIGHT_MODE)
    }

    fun setNightMode(context: Context, isOpen: Boolean) {
        getSharedPreferences(context).edit().putBoolean(ConstData.NIGHT_MODE, isOpen).apply()
    }

    fun getNightMode(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(ConstData.NIGHT_MODE, false)
    }

    fun setC2CCellPrice(context: Context, cellPrice: Float) {
        getSharedPreferences(context).edit().putFloat(ConstData.CELL_PRICE, cellPrice).apply()
    }

    fun getC2CCellPrice(context: Context): Float {
        return getSharedPreferences(context).getFloat(ConstData.CELL_PRICE, 0f)
    }

    fun setC2CBuyPrice(context: Context, buyPrice: Float) {
        getSharedPreferences(context).edit().putFloat(ConstData.BUY_PRICE, buyPrice).apply()
    }

    fun getC2CBuyPrice(context: Context): Float {
        return getSharedPreferences(context).getFloat(ConstData.BUY_PRICE, 0f)
    }

    fun setHostIndex(context: Context, index: Int) {
        getSharedPreferences(context).edit().putInt(ConstData.HOST_INDEX, index).apply()
    }

    fun setServerHost(context: Context,data:ArrayList<String?>){
        var set = data.toSet()
        getSharedPreferences(context).edit().putStringSet(ConstData.HOST_DATA, set).apply()
    }

    fun getServerHost(context: Context):ArrayList<String?>?{
        var sets =  getSharedPreferences(context).getStringSet(ConstData.HOST_DATA,null)
        var data:ArrayList<String?>? = null
        if(sets != null){
             data = sets.toList() as ArrayList<String?>?
        }
        return data
    }

    fun getHostIndex(context: Context): Int {
        return getSharedPreferences(context).getInt(ConstData.HOST_INDEX, 0)
    }

    fun addPairSearchHistory(context: Context, pair: String?) {
        var history = getPairSearchHistory(context)
        history = history ?: ArrayList()
        if (!history.contains(pair)) {
            history.add(pair)
            getSharedPreferences(context).edit().putStringSet(ConstData.PAIR_SEARCH_HISTORY, HashSet(history)).apply()
        }
    }

    fun clearPairSearchHistory(context: Context) {
        getSharedPreferences(context).edit().remove(ConstData.PAIR_SEARCH_HISTORY).apply()
    }

    fun getPairSearchHistory(context: Context): ArrayList<String?>? {
        val set = getSharedPreferences(context).getStringSet(ConstData.PAIR_SEARCH_HISTORY, null)
        return set?.let { ArrayList(it) }
    }

    fun addCoinSearchHistory(context: Context, pair: String?) {
        var history: MutableList<String?> = getRealCoinSearchHistory(context)
        history = history ?: ArrayList()
        if (!history.contains(pair)) {
            history.add(pair)
            if (history.size > 3) {
                history = history.subList(history.size - 3, history.size)
            }
            getSharedPreferences(context).edit().putStringSet(ConstData.COIN_SEARCH_HISTORY, HashSet(history)).apply()
        }
    }

    fun clearCoinSearchHistory(context: Context) {
        getSharedPreferences(context).edit().remove(ConstData.COIN_SEARCH_HISTORY).apply()
    }

    fun getCoinSearchHistory(context: Context): ArrayList<String?> {
        val result = getRealCoinSearchHistory(context)
        result.add("USDT")
        return result
    }

    fun getRealCoinSearchHistory(context: Context): ArrayList<String?> {
        val set = getSharedPreferences(context).getStringSet(ConstData.COIN_SEARCH_HISTORY, null)
        return set?.let { ArrayList(it) } ?: ArrayList()
    }
}