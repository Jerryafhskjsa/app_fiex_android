package com.black.base.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.SystemClock
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import com.black.base.model.socket.*
import com.black.base.service.DearPairService
import com.black.base.sqlite.FryingSQLiteHelper
import com.black.base.util.FryingUtil.printError
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observer
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*

//交易信息管理
object SocketUtil {
    private const val TAG = "SocketUtil"
    private const val SAVE_PAIR_STATUS = 1
    private const val SAVE_ORDER_BID = 2
    private const val SAVE_ORDER_ASK = 3
    private const val SAVE_ORDER_DEAL = 4
    private const val SAVE_USER_MINING = 5
    private const val SAVE_USER_ORDER = 6
    private const val SAVE_USER_WALLET = 6
    const val ACTION_SOCKET_COMMAND = "com.bioko.exchange.socket.command"
    const val SOCKET_COMMAND = "socket_command"
    const val SOCKET_COMMAND_EXTRAS = "socket_command_extras"
    const val COMMAND_NOTHING = 0
    const val COMMAND_RECEIVE = 1 //接收数据
    const val COMMAND_PAUSE = 2 //暂停接收数据
    const val COMMAND_STOP = 3 //停止接收数据
    const val COMMAND_RESUME = 4 //恢复接收
    const val COMMAND_PAIR_CHANGED = 5 //交易对发生改变
    const val COMMAND_USER_LOGIN = 6 //用户登录
    const val COMMAND_USER_LOGOUT = 7 //用户注销
    const val COMMAND_KTAB_CHANGED = 8 //K线节点选择
    const val COMMAND_KTAB_CLOSE = 9 //K线关闭
    const val COMMAND_DEAL_OPEN = 10 //开始监听DEAL
    const val COMMAND_DEAL_CLOSE = 11 //停止监听DEAL
    const val COMMAND_ORDER_OPEN = 12 //开始监听挂单
    const val COMMAND_ORDER_CLOSE = 13 //停止监听挂单
    const val COMMAND_QUOTA_OPEN = 14 //开始监听行情
    const val COMMAND_QUOTA_CLOSE = 15 //停止监听行情
    const val COMMAND_FACTION_OPEN = 16 //开始监听笑傲江湖
    const val COMMAND_FACTION_CLOSE = 17 //停止监听笑傲江湖
    const val COMMAND_K_LOAD_MORE = 18 //K线加载更多
    const val COMMAND_KTAB_RESUME = 19 //恢复K线
    const val COMMAND_ORDER_RELOAD = 20 //挂单重新加载
    const val COMMAND_LEVER_DETAIL_START = 21 //监听杠杆详情
    const val COMMAND_LEVER_DETAIL_FINISH = 22 //停止监听杠杆详情
    /***fiex***/
    const val COMMAND_CURRENT_PAIR_QUOTA = 100//开始监听当前交易对委托行情
    /***fiex***/
    //上次保存数据时间记录
    private val lastSaveTimeMap = SparseArray<Long>()
    //时间间隔内，只保存一次数据
    private const val SAVE_DELAY_TIME: Long = 0
    private val gson = Gson()
    /**
     * 获取当前交易对
     *
     * @param context
     * @return
     */
    fun getCurrentPair(context: Context): String? {
        return CookieUtil.getCurrentPair(context)
    }

    fun setCurrentPair(context: Context, pair: String?) {
        CookieUtil.setCurrentPair(context, pair)
    }

    private fun createPairStatusContentValues(jsonObject: JSONObject?): ContentValues? {
        if (jsonObject == null) {
            return null
        }
        val values = ContentValues()
        values.put("currentPrice", jsonObject.optDouble("currentPrice"))
        values.put("firstPriceToday", jsonObject.optDouble("firstPriceToday"))
        values.put("lastPrice", jsonObject.optDouble("lastPrice"))
        values.put("maxPrice", jsonObject.optDouble("maxPrice"))
        values.put("minPrice", jsonObject.optDouble("minPrice"))
        values.put("pair", jsonObject.optString("pair"))
        values.put("priceChangeSinceToday", jsonObject.optDouble("priceChangeSinceToday"))
        values.put("statDate", jsonObject.optLong("statDate"))
        values.put("totalAmount", jsonObject.optDouble("totalAmount"))
        values.put("is_dear", jsonObject.optInt("is_dear", 0))
        return values
    }

    private fun createPairStatusOrderContentValues(pairStatus: PairStatus?): ContentValues? {
        if (pairStatus == null) {
            return null
        }
        val values = ContentValues()
        values.put("pair", pairStatus.pair)
        values.put("order_no", pairStatus.order_no)
        return values
    }

    //保存排序的交易对信息
    fun savePairStatusOrdered(context: Context?, orderedPairs: List<PairStatus?>?): Boolean {
        var result = false
        val helper = FryingSQLiteHelper.getInstance(context!!)
        val db = helper?.writableDatabase
        db?.beginTransaction()
        if (orderedPairs != null && orderedPairs.isNotEmpty()) {
            for (i in orderedPairs.indices) {
                val pairStatus = orderedPairs[i]
                if (pairStatus != null) {
                    val pair = pairStatus.pair
                    if (!TextUtils.isEmpty(pair)) {
                        val cursor = helper?.rawQuery("select * from pairs_stats where pair = ?", arrayOf(pair))
                        val values = createPairStatusOrderContentValues(pairStatus)
                        if ((cursor?.count ?: 0) == 0) {
                            helper?.insert("pairs_stats", null, values)
                        } else {
                            helper?.update("pairs_stats", values, "pair = ?", arrayOf(pair))
                        }
                        cursor?.close()
                    }
                }
            }
            result = true
        }
        db?.setTransactionSuccessful()
        db?.endTransaction()
        return result
    }

    fun savePairStatus(context: Context?, jsonArray: JSONArray?, deleteAll: Boolean): Boolean {
        var result = false
        val lastSaveTime = lastSaveTimeMap[SAVE_PAIR_STATUS]
        if (lastSaveTime == null || SystemClock.elapsedRealtime() - lastSaveTime > SAVE_DELAY_TIME) {
            val helper = FryingSQLiteHelper.getInstance(context!!)
            val db = helper?.writableDatabase
            db?.beginTransaction()
            if (deleteAll) {
                db?.delete("pairs_stats", null, null)
            }
            if (jsonArray != null && jsonArray.length() > 0) {
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.optJSONObject(i)
                    if (jsonObject != null) {
                        val pair = jsonObject.optString("pair")
                        val isDear = DearPairService.dearPairMap[pair]
                        try {
                            jsonObject.put("is_dear", if (isDear == null || !isDear) 0 else 1)
                        } catch (e: JSONException) {
                            printError(e)
                        }
                        if (!TextUtils.isEmpty(pair)) {
                            val cursor = helper?.rawQuery("select * from pairs_stats where pair = ?", arrayOf(pair))
                            val values = createPairStatusContentValues(jsonObject)
                            if ((cursor?.count ?: 0) == 0) {
                                helper?.insert("pairs_stats", null, values)
                            } else {
                                helper?.update("pairs_stats", values, "pair = ?", arrayOf(pair))
                            }
                            cursor?.close()
                        }
                    }
                }
                result = true
            }
            db?.setTransactionSuccessful()
            db?.endTransaction()
            lastSaveTimeMap.put(SAVE_PAIR_STATUS, SystemClock.elapsedRealtime())
        }
        return result
    }

    fun savePairStatus(context: Context?, jsonArray: ArrayList<PairStatusNew?>?, deleteAll: Boolean): Boolean {
        var result = false
        val lastSaveTime = lastSaveTimeMap[SAVE_PAIR_STATUS]
        if (lastSaveTime == null || SystemClock.elapsedRealtime() - lastSaveTime > SAVE_DELAY_TIME) {
            val helper = FryingSQLiteHelper.getInstance(context!!)
            val db = helper?.writableDatabase
            db?.beginTransaction()
            if (deleteAll) {
                db?.delete("pairs_stats", null, null)
            }
            if (jsonArray != null && jsonArray.size > 0) {
                for (i in jsonArray.indices) {
                    val pairStatusNew = jsonArray[i]
                    if (pairStatusNew != null) {
                        val pair = pairStatusNew.pair
                        if (!TextUtils.isEmpty(pair)) {
                            val cursor = helper?.rawQuery("select * from pairs_stats where pair = ?", arrayOf(pair))
                            val values = pairStatusNew.contentValues
                            if ((cursor?.count ?: 0) == 0) {
                                helper?.insert("pairs_stats", null, values)
                            } else {
                                helper?.update("pairs_stats", values, "pair = ?", arrayOf(pair))
                            }
                            cursor?.close()
                        }
                    }
                }
                result = true
            }
            db?.setTransactionSuccessful()
            db?.endTransaction()
            lastSaveTimeMap.put(SAVE_PAIR_STATUS, SystemClock.elapsedRealtime())
        }
        return result
    }

    fun getPairStatusListByKey(context: Context?, key: String): ArrayList<PairStatus>? {
        if (TextUtils.isEmpty(key)) {
            return null
        }
        val result = ArrayList<PairStatus>()
        val helper = FryingSQLiteHelper.getInstance(context!!)
        val db = helper?.readableDatabase
        db?.beginTransaction()
        val cursor = helper?.rawQuery("select * from pairs_stats where pair like ?", arrayOf("%$key%"))
        cursor?.let {
            while (cursor.moveToNext()) {
                val pairStatus = PairStatus()
                pairStatus.currentPrice = cursor.getDouble(cursor.getColumnIndex("currentPrice"))
                //            pairStatus.currentPrice = cursor?.getDouble(cursor?.getColumnIndex("currentPrice"));
                pairStatus.firstPriceToday = cursor.getDouble(cursor.getColumnIndex("firstPriceToday"))
                pairStatus.lastPrice = cursor.getDouble(cursor.getColumnIndex("lastPrice"))
                pairStatus.maxPrice = cursor.getDouble(cursor.getColumnIndex("maxPrice"))
                pairStatus.minPrice = cursor.getDouble(cursor.getColumnIndex("minPrice"))
                //            pairStatus.pair = cursor?.getString(cursor?.getColumnIndex("pair"));
                pairStatus.pair = cursor.getString(cursor.getColumnIndex("pair"))
                //            pairStatus.priceChangeSinceToday = cursor?.getDouble(cursor?.getColumnIndex("priceChangeSinceToday"));
                pairStatus.priceChangeSinceToday = cursor.getDouble(cursor.getColumnIndex("priceChangeSinceToday"))
                pairStatus.statDate = cursor.getLong(cursor.getColumnIndex("statDate"))
                //            pairStatus.totalAmount = cursor?.getDouble(cursor?.getColumnIndex("totalAmount"));
                pairStatus.totalAmount = cursor.getDouble(cursor.getColumnIndex("totalAmount"))
                pairStatus.order_no = cursor.getInt(cursor.getColumnIndex("order_no"))
                pairStatus.is_dear = cursor.getInt(cursor.getColumnIndex("is_dear")) == 1
                result.add(pairStatus)
            }
        }
        cursor?.close()
        db?.setTransactionSuccessful()
        db?.endTransaction()
        return result
    }

    fun getPairStatus(context: Context?, pair: String?): PairStatus? {
        if (TextUtils.isEmpty(pair)) {
            return null
        }
        val pairStatus = PairStatus()
        val helper = FryingSQLiteHelper.getInstance(context!!)
        val db = helper?.readableDatabase
        db?.beginTransaction()
        val cursor = helper?.rawQuery("select * from pairs_stats where pair = ?", arrayOf(pair))
        cursor?.let {
            if (cursor.count > 0) {
                cursor.moveToFirst()
                pairStatus.currentPrice = cursor.getDouble(cursor.getColumnIndex("currentPrice"))
                //            pairStatus.currentPrice = cursor?.getDouble(cursor?.getColumnIndex("currentPrice"));
                pairStatus.firstPriceToday = cursor.getDouble(cursor.getColumnIndex("firstPriceToday"))
                pairStatus.lastPrice = cursor.getDouble(cursor.getColumnIndex("lastPrice"))
                pairStatus.maxPrice = cursor.getDouble(cursor.getColumnIndex("maxPrice"))
                pairStatus.minPrice = cursor.getDouble(cursor.getColumnIndex("minPrice"))
                //            pairStatus.pair = cursor?.getString(cursor?.getColumnIndex("pair"));
                pairStatus.pair = cursor.getString(cursor.getColumnIndex("pair"))
                //            pairStatus.priceChangeSinceToday = cursor?.getDouble(cursor?.getColumnIndex("priceChangeSinceToday"));
                pairStatus.priceChangeSinceToday = cursor.getDouble(cursor.getColumnIndex("priceChangeSinceToday"))
                pairStatus.statDate = cursor.getLong(cursor.getColumnIndex("statDate"))
                //            pairStatus.totalAmount = cursor?.getDouble(cursor?.getColumnIndex("totalAmount"));
                pairStatus.totalAmount = cursor.getDouble(cursor.getColumnIndex("totalAmount"))
                pairStatus.order_no = cursor.getInt(cursor.getColumnIndex("order_no"))
            }
        }
        cursor?.close()
        db?.setTransactionSuccessful()
        db?.endTransaction()
        return pairStatus
    }

    //指定刷新的交易对
    fun getAllPairStatus(context: Context?, updatedPairs: ArrayList<String?>?): ArrayList<PairStatus> {
        val result = ArrayList<PairStatus>()
        val helper = FryingSQLiteHelper.getInstance(context!!)
        val db = helper?.readableDatabase
        db?.beginTransaction()
        val args: Array<String?> = updatedPairs?.toTypedArray() ?: arrayOf()
        val where = StringBuilder()
        if (updatedPairs != null && updatedPairs.isNotEmpty()) {
            where.append(" where pair in (")
            for (i in updatedPairs.indices) {
                if (i == 0) {
                    where.append("?")
                } else {
                    where.append(", ?")
                }
            }
            where.append(")")
        }
        val cursor = helper?.rawQuery("select * from pairs_stats $where order by order_no", args)
        cursor?.let {
            while (cursor.moveToNext()) {
                val pairStatus = PairStatus()
                pairStatus.currentPrice = cursor.getDouble(cursor.getColumnIndex("currentPrice"))
                //            pairStatus.currentPrice = cursor?.getDouble(cursor?.getColumnIndex("currentPrice"));
                pairStatus.firstPriceToday = cursor.getDouble(cursor.getColumnIndex("firstPriceToday"))
                pairStatus.lastPrice = cursor.getDouble(cursor.getColumnIndex("lastPrice"))
                pairStatus.maxPrice = cursor.getDouble(cursor.getColumnIndex("maxPrice"))
                pairStatus.minPrice = cursor.getDouble(cursor.getColumnIndex("minPrice"))
                //            pairStatus.pair = cursor?.getString(cursor?.getColumnIndex("pair"));
                pairStatus.pair = cursor.getString(cursor.getColumnIndex("pair"))
                //            pairStatus.priceChangeSinceToday = cursor?.getDouble(cursor?.getColumnIndex("priceChangeSinceToday"));
                pairStatus.priceChangeSinceToday = cursor.getDouble(cursor.getColumnIndex("priceChangeSinceToday"))
                pairStatus.statDate = cursor.getLong(cursor.getColumnIndex("statDate"))
                //            pairStatus.totalAmount = cursor?.getDouble(cursor?.getColumnIndex("totalAmount"));
                pairStatus.totalAmount = cursor.getDouble(cursor.getColumnIndex("totalAmount"))
                pairStatus.order_no = cursor.getInt(cursor.getColumnIndex("order_no"))
                pairStatus.is_dear = cursor.getInt(cursor.getColumnIndex("is_dear")) == 1
                result.add(pairStatus)
            }
        }
        cursor?.close()
        db?.setTransactionSuccessful()
        db?.endTransaction()
        return result
    }

    fun clearAllPairStatus(context: Context?) {
        val helper = FryingSQLiteHelper.getInstance(context!!)
        val db = helper?.writableDatabase
        db?.beginTransaction()
        db?.delete("pairs_stats", null, null)
        db?.setTransactionSuccessful()
        db?.endTransaction()
    }

    private fun createQuotationOrderContentValues(jsonObject: JSONObject?): ContentValues? {
        if (jsonObject == null) {
            return null
        }
        val values = ContentValues()
        values.put("anchorAmount", jsonObject.optDouble("anchorAmount"))
        values.put("direction", jsonObject.optString("direction"))
        values.put("exchangeAmount", jsonObject.optDouble("exchangeAmount"))
        values.put("formattedPrice", jsonObject.optString("formattedPrice"))
        values.put("orderType", jsonObject.optString("orderType"))
        values.put("pair", jsonObject.optString("pair"))
        values.put("price", jsonObject.optDouble("price"))
        return values
    }

    fun saveQuotationOrder(context: Context?, jsonArray: JSONArray?, direction: String?): Boolean {
        var tableName: String? = null
        tableName = when {
            "ASK".equals(direction, ignoreCase = true) -> {
                "quotation_order_ask"
            }
            "BID".equals(direction, ignoreCase = true) -> {
                "quotation_order_bid"
            }
            else -> {
                return false
            }
        }
        var result = false
        val type = if ("ASK".equals(direction, ignoreCase = true)) SAVE_ORDER_ASK else SAVE_ORDER_BID
        val lastSaveTime = lastSaveTimeMap[type]
        if (lastSaveTime == null || SystemClock.elapsedRealtime() - lastSaveTime > SAVE_DELAY_TIME) {
            val helper = FryingSQLiteHelper.getInstance(context!!)
            val db = helper?.writableDatabase
            db?.beginTransaction()
            db?.delete(tableName, null, null)
            if (jsonArray != null && jsonArray.length() > 0) {
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.optJSONObject(i)
                    if (jsonObject != null) {
                        val pair = jsonObject.optString("pair")
                        if (!TextUtils.isEmpty(pair)) {
                            val values = createQuotationOrderContentValues(jsonObject)
                            helper?.insert(tableName, null, values)
                        }
                    }
                }
                result = true
            }
            db?.setTransactionSuccessful()
            db?.endTransaction()
            lastSaveTimeMap.put(type, SystemClock.elapsedRealtime())
        }
        return result
    }

    //    public static List<TradeOrder> getAllQuotationOrder(Context context, String pair, String direction) {
//        if (TextUtils.isEmpty(pair)) {
//            return null;
//        }
//        String tableName = null;
//        if ("ASK".equalsIgnoreCase(direction)) {
//            tableName = "quotation_order_ask";
//        } else if ("BID".equalsIgnoreCase(direction)) {
//            tableName = "quotation_order_bid";
//        } else {
//            return null;
//        }
//        List<TradeOrder> result = new ArrayList<>();
//        FryingSQLiteHelper helper = FryingSQLiteHelper.getInstance(context);
//        SQLiteDatabase db = helper?.getReadableDatabase();
//        db?.beginTransaction();
//        Cursor cursor = helper?.rawQuery("select * from " + tableName + " where pair = ? ", new String[]{pair});
//        while (cursor?.moveToNext()) {
//            TradeOrder tradeOrder = new TradeOrder();
//            tradeOrder.anchorAmount = cursor?.getDouble(cursor?.getColumnIndex("anchorAmount"));
//            tradeOrder.direction = cursor?.getString(cursor?.getColumnIndex("direction"));
//            tradeOrder.exchangeAmount = cursor?.getDouble(cursor?.getColumnIndex("exchangeAmount"));
//            tradeOrder.formattedPrice = cursor?.getString(cursor?.getColumnIndex("formattedPrice"));
//            tradeOrder.orderType = cursor?.getString(cursor?.getColumnIndex("orderType"));
//            tradeOrder.pair = cursor?.getString(cursor?.getColumnIndex("pair"));
//            tradeOrder.price = cursor?.getDouble(cursor?.getColumnIndex("price"));
//            result.add(tradeOrder);
//        }
//        cursor?.close();
//        db?.setTransactionSuccessful();
//        db?.endTransaction();
//        return result;
//    }
    fun clearAllQuotationOrder(context: Context?) {
        val helper = FryingSQLiteHelper.getInstance(context!!)
        val db = helper?.writableDatabase
        db?.beginTransaction()
        db?.delete("quotation_order_ask", null, null)
        db?.delete("quotation_order_bid", null, null)
        db?.setTransactionSuccessful()
        db?.endTransaction()
    }

    private fun createQuotationDealContentValues(jsonObject: JSONObject?): ContentValues? {
        if (jsonObject == null) {
            return null
        }
        val values = ContentValues()
        values.put("createdTime", jsonObject.optDouble("createdTime"))
        values.put("dealAmount", jsonObject.optDouble("dealAmount"))
        values.put("formattedPrice", jsonObject.optString("formattedPrice"))
        values.put("pair", jsonObject.optString("pair"))
        values.put("price", jsonObject.optDouble("price"))
        values.put("tradeDealDirection", jsonObject.optString("tradeDealDirection"))
        return values
    }

    fun saveQuotationDeal(context: Context?, jsonArray: JSONArray?): Boolean {
        var result = false
        val lastSaveTime = lastSaveTimeMap[SAVE_ORDER_DEAL]
        if (lastSaveTime == null || SystemClock.elapsedRealtime() - lastSaveTime > SAVE_DELAY_TIME) {
            val helper = FryingSQLiteHelper.getInstance(context!!)
            val db = helper?.writableDatabase
            db?.beginTransaction()
            db?.delete("quotation_deal", null, null)
            if (jsonArray != null && jsonArray.length() > 0) {
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.optJSONObject(i)
                    if (jsonObject != null) {
                        val pair = jsonObject.optString("pair")
                        if (!TextUtils.isEmpty(pair)) {
                            val values = createQuotationDealContentValues(jsonObject)
                            helper?.insert("quotation_deal", null, values)
                        }
                    }
                }
                result = true
            }
            db?.setTransactionSuccessful()
            db?.endTransaction()
            lastSaveTimeMap.put(SAVE_ORDER_DEAL, SystemClock.elapsedRealtime())
        }
        return result
    }

    fun getAllQuotationDeal(context: Context?, pair: String?): List<TradeOrder>? {
        if (TextUtils.isEmpty(pair)) {
            return null
        }
        val result: MutableList<TradeOrder> = ArrayList()
        val helper = FryingSQLiteHelper.getInstance(context!!)
        val db = helper?.readableDatabase
        db?.beginTransaction()
        val cursor = helper?.rawQuery("select * from quotation_deal where pair = ? ", arrayOf(pair))
        cursor?.let {
            while (cursor.moveToNext()) {
                val tradeOrder = TradeOrder()
                tradeOrder.createdTime = cursor.getLong(cursor.getColumnIndex("createdTime"))
                tradeOrder.dealAmount = cursor.getDouble(cursor.getColumnIndex("dealAmount"))
                tradeOrder.formattedPrice = cursor.getString(cursor.getColumnIndex("formattedPrice"))
                tradeOrder.pair = cursor.getString(cursor.getColumnIndex("pair"))
                tradeOrder.price = cursor.getDouble(cursor.getColumnIndex("price"))
                tradeOrder.tradeDealDirection = cursor.getString(cursor.getColumnIndex("tradeDealDirection"))
                result.add(tradeOrder)
            }
        }
        cursor?.close()
        db?.setTransactionSuccessful()
        db?.endTransaction()
        return result
    }

    fun clearAllQuotationDeal(context: Context?) {
        val helper = FryingSQLiteHelper.getInstance(context!!)
        val db = helper?.writableDatabase
        db?.beginTransaction()
        db?.delete("quotation_deal", null, null)
        db?.setTransactionSuccessful()
        db?.endTransaction()
    }

    private fun createUserMiningValues(jsonObject: JSONObject?): ContentValues? {
        if (jsonObject == null) {
            return null
        }
        val values = ContentValues()
        values.put("platform", jsonObject.optDouble("totalDealAmt"))
        values.put("personal", jsonObject.optDouble("userDealAmt"))
        return values
    }

    fun saveUserMining(context: Context?, jsonObject: JSONObject?): Boolean {
        var result = false
        val lastSaveTime = lastSaveTimeMap[SAVE_USER_MINING]
        if (lastSaveTime == null || SystemClock.elapsedRealtime() - lastSaveTime > SAVE_DELAY_TIME) {
            val helper = FryingSQLiteHelper.getInstance(context!!)
            val db = helper?.writableDatabase
            db?.beginTransaction()
            db?.delete("user_mining", null, null)
            if (jsonObject != null) {
                val values = createUserMiningValues(jsonObject)
                helper?.insert("user_mining", null, values)
                result = true
            }
            db?.setTransactionSuccessful()
            db?.endTransaction()
            lastSaveTimeMap.put(SAVE_USER_MINING, SystemClock.elapsedRealtime())
        }
        return result
    }

    //    public static UserMining getUserMining(Context context) {
//        UserMining result = null;
//        FryingSQLiteHelper helper = FryingSQLiteHelper.getInstance(context);
//        SQLiteDatabase db = helper?.getReadableDatabase();
//        db?.beginTransaction();
//        Cursor cursor = helper?.rawQuery("select * from user_mining  LIMIT 1", new String[]{});
//        if (cursor?.getCount() > 0) {
//            cursor?.moveToFirst();
//            result = new UserMining();
//            result.platformTotal = cursor?.getDouble(cursor?.getColumnIndex("platform"));
//            result.userTotal = cursor?.getDouble(cursor?.getColumnIndex("personal"));
//        }
//        cursor?.close();
//        db?.setTransactionSuccessful();
//        db?.endTransaction();
//        return result;
//    }
    fun clearUserMining(context: Context?) {
        val helper = FryingSQLiteHelper.getInstance(context!!)
        val db = helper?.writableDatabase
        db?.beginTransaction()
        db?.delete("user_mining", null, null)
        db?.setTransactionSuccessful()
        db?.endTransaction()
    }

    private fun createUserOrderValues(jsonObject: JSONObject?): ContentValues? {
        if (jsonObject == null) {
            return null
        }
        val values = ContentValues()
        values.put("createdTime", jsonObject.optDouble("createdTime"))
        values.put("dealAmount", jsonObject.optDouble("dealAmount"))
        values.put("dealAvgPrice", jsonObject.optDouble("dealAvgPrice"))
        values.put("direction", jsonObject.optString("direction"))
        values.put("feeRate", jsonObject.optDouble("feeRate"))
        values.put("frozenAmountByOrder", jsonObject.optDouble("frozenAmountByOrder"))
        values.put("id", jsonObject.optString("id"))
        values.put("orderType", jsonObject.optString("orderType"))
        values.put("pair", jsonObject.optString("pair"))
        values.put("price", jsonObject.optDouble("price"))
        values.put("status", jsonObject.optInt("status"))
        values.put("totalAmount", jsonObject.optDouble("totalAmount"))
        return values
    }

    fun saveUserOrderList(context: Context?, jsonArray: JSONArray?): Boolean {
        var result = false
        val lastSaveTime = lastSaveTimeMap[SAVE_USER_ORDER]
        if (lastSaveTime == null || SystemClock.elapsedRealtime() - lastSaveTime > SAVE_DELAY_TIME) {
            val helper = FryingSQLiteHelper.getInstance(context!!)
            val db = helper?.writableDatabase
            db?.beginTransaction()
            db?.delete("user_order", null, null)
            if (jsonArray != null && jsonArray.length() > 0) {
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.optJSONObject(i)
                    if (jsonObject != null) {
                        val pair = jsonObject.optString("pair")
                        if (!TextUtils.isEmpty(pair)) {
                            val values = createUserOrderValues(jsonObject)
                            helper?.insert("user_order", null, values)
                        }
                    }
                }
                result = true
            }
            db?.setTransactionSuccessful()
            db?.endTransaction()
            lastSaveTimeMap.put(SAVE_USER_ORDER, SystemClock.elapsedRealtime())
        }
        return result
    }

    //    public static List<TradeOrder> getUserOrderList(Context context, String pair) {
//        if (TextUtils.isEmpty(pair)) {
//            return null;
//        }
//        if (TextUtils.isEmpty(pair)) {
//            return null;
//        }
//        List<TradeOrder> result = new ArrayList<>();
//        FryingSQLiteHelper helper = FryingSQLiteHelper.getInstance(context);
//        SQLiteDatabase db = helper?.getReadableDatabase();
//        db?.beginTransaction();
//        Cursor cursor = helper?.rawQuery("select * from user_order where pair = ? ", new String[]{pair});
//        while (cursor?.moveToNext()) {
//            TradeOrder tradeOrder = new TradeOrder();
//            tradeOrder.createdTime = cursor?.getLong(cursor?.getColumnIndex("createdTime"));
//            tradeOrder.dealAmount = cursor?.getDouble(cursor?.getColumnIndex("dealAmount"));
//            tradeOrder.dealAvgPrice = cursor?.getDouble(cursor?.getColumnIndex("dealAvgPrice"));
//            tradeOrder.direction = cursor?.getString(cursor?.getColumnIndex("direction"));
//            tradeOrder.feeRate = cursor?.getDouble(cursor?.getColumnIndex("feeRate"));
//            tradeOrder.frozenAmountByOrder = cursor?.getDouble(cursor?.getColumnIndex("frozenAmountByOrder"));
//            tradeOrder.id = cursor?.getString(cursor?.getColumnIndex("id"));
//            tradeOrder.orderType = cursor?.getString(cursor?.getColumnIndex("orderType"));
//            tradeOrder.pair = cursor?.getString(cursor?.getColumnIndex("pair"));
//            tradeOrder.price = cursor?.getDouble(cursor?.getColumnIndex("price"));
//            tradeOrder.status = cursor?.getInt(cursor?.getColumnIndex("status"));
//            tradeOrder.totalAmount = cursor?.getDouble(cursor?.getColumnIndex("totalAmount"));
//            result.add(tradeOrder);
//        }
//        cursor?.close();
//        db?.setTransactionSuccessful();
//        db?.endTransaction();
//        return result;
//    }
    fun clearUserOrderList(context: Context?) {
        val helper = FryingSQLiteHelper.getInstance(context!!)
        val db = helper?.writableDatabase
        db?.beginTransaction()
        db?.delete("user_order", null, null)
        db?.setTransactionSuccessful()
        db?.endTransaction()
    }

    private fun createUserWalletValues(jsonObject: JSONObject?): ContentValues? {
        if (jsonObject == null) {
            return null
        }
        val values = ContentValues()
        values.put("coinAmount", jsonObject.optDouble("coinAmount"))
        values.put("coinType", jsonObject.optString("coinType"))
        return values
    }

    //    private static ContentValues createUserWalletValues(Wallet wallet) {
//        if (wallet == null) {
//            return null;
//        }
//        ContentValues values = new ContentValues();
//        values.put("coinAmount", wallet.coinAmount);
//        values.put("coinType", wallet.coinType);
//        return values;
//    }
    fun saveUserWalletList(context: Context?, jsonArray: JSONArray?): Boolean {
        var result = false
        val lastSaveTime = lastSaveTimeMap[SAVE_USER_WALLET]
        if (lastSaveTime == null || SystemClock.elapsedRealtime() - lastSaveTime > SAVE_DELAY_TIME) {
            val helper = FryingSQLiteHelper.getInstance(context!!)
            val db = helper?.writableDatabase
            db?.beginTransaction()
            //db?.delete("user_wallet", null, null);
            if (jsonArray != null && jsonArray.length() > 0) {
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.optJSONObject(i)
                    if (jsonObject != null) {
                        val coinType = jsonObject.optString("coinType")
                        if (!TextUtils.isEmpty(coinType)) {
                            val cursor = helper?.rawQuery("select * from user_wallet where coinType = ?", arrayOf(coinType))
                            val values = createUserWalletValues(jsonObject)
                            if ((cursor?.count ?: 0) == 0) {
                                helper?.insert("user_wallet", null, values)
                            } else {
                                helper?.update("user_wallet", values, "coinType = ?", arrayOf(coinType))
                            }
                            cursor?.close()
                        }
                    }
                }
                result = true
            }
            db?.setTransactionSuccessful()
            db?.endTransaction()
            lastSaveTimeMap.put(SAVE_USER_WALLET, SystemClock.elapsedRealtime())
        }
        return result
    }

    fun clearUserWalletList(context: Context?) {
        val helper = FryingSQLiteHelper.getInstance(context!!)
        val db = helper?.writableDatabase
        db?.beginTransaction()
        db?.delete("user_wallet", null, null)
        db?.setTransactionSuccessful()
        db?.endTransaction()
    }

    fun clearAll(context: Context?) {
        val helper = FryingSQLiteHelper.getInstance(context!!)
        val db = helper?.writableDatabase
        db?.beginTransaction()
        db?.delete("pairs_stats", null, null)
        db?.delete("quotation_order_ask", null, null)
        db?.delete("quotation_order_bid", null, null)
        db?.delete("quotation_deal", null, null)
        db?.delete("user_mining", null, null)
        db?.delete("user_order", null, null)
        db?.delete("user_wallet", null, null)
        db?.setTransactionSuccessful()
        db?.endTransaction()
    }

    //按照价格进行合并委托，只要前N条数据
    fun mergeQuotationOrder(data: List<TradeOrder>?, pair: String?, direction: String?, precision: Int, maxSize: Int): List<TradeOrder>? {
        var precision = precision
        Log.d("888888", "precision = $precision")
        var maxSize = maxSize
        if (data == null || data.isEmpty() || TextUtils.isEmpty(pair) || TextUtils.isEmpty(direction)) {
            return null
        }
        Collections.sort(data, if ("ASK".equals(direction, ignoreCase = true)) TradeOrder.COMPARATOR_UP else TradeOrder.COMPARATOR_DOWN)
        if (maxSize <= 0) {
            maxSize = 5
        }
        val result: MutableList<TradeOrder> = ArrayList(maxSize)
        var lastOrder: TradeOrder? = null
        for (i in data.indices) {
            val tradeOrder = data[i]
            if (pair.equals(tradeOrder.pair, ignoreCase = true) && TextUtils.equals(direction, tradeOrder.orderType)) {
                val priceString = tradeOrder.priceString
                Log.d("888888", "priceString = $priceString")
                var price = CommonUtil.parseBigDecimal(priceString)
                Log.d("888888", "price = $price")
                price = price?.setScale(precision, if ("ASK".equals(direction, ignoreCase = true)) BigDecimal.ROUND_UP else BigDecimal.ROUND_DOWN)
                //                String formattedPrice = price == null ? null : price.setScale(precision, "ASK".equalsIgnoreCase(direction) ? BigDecimal.ROUND_UP : BigDecimal.ROUND_DOWN).toString();
                Log.d("888888", "price1 = $price")
                val formattedPrice = NumberUtil.formatNumberNoGroup(price, precision, precision)
                Log.d("888888", "formattedPrice = $formattedPrice")
                val lastFormattedPrice = lastOrder?.formattedPrice
                if (formattedPrice == null) {
                    continue
                }
                if (lastOrder == null || formattedPrice != lastFormattedPrice) {
                    if (result.size >= maxSize) {
                        break
                    }
                    lastOrder = TradeOrder()
                    lastOrder.price = tradeOrder.price
                    lastOrder.priceString = tradeOrder.priceString
                    lastOrder.direction = tradeOrder.direction
                    lastOrder.orderType = tradeOrder.orderType
                    lastOrder.anchorAmount = tradeOrder.anchorAmount
                    lastOrder.exchangeAmount = tradeOrder.exchangeAmount
                    lastOrder.pair = tradeOrder.pair
                    lastOrder.formattedPrice = formattedPrice
                    result.add(lastOrder)
                } else {
                    lastOrder.exchangeAmount += tradeOrder.exchangeAmount
                }
            }
        }
        return result
    }

    //按pair和价格合并 Ask Bid
    fun mergeQuotationDealNew(data: List<QuotationOrderNew?>?): Array<ArrayList<QuotationOrderNew>?>? {
        if (data == null || data.isEmpty()) {
            return null
        }
        val result: MutableMap<String, QuotationOrderNew> = HashMap()
        for (quotationDealNew in data) {
            val key = quotationDealNew?.key ?: continue
            var dealNew = result[key]
            if (dealNew == null) {
                dealNew = QuotationOrderNew()
                dealNew.pair = quotationDealNew.pair
                dealNew.p = quotationDealNew.p
                dealNew.d = quotationDealNew.d
                result[key] = dealNew
            }
            dealNew.a += quotationDealNew.a
            dealNew.v += quotationDealNew.v
        }
        val returnData: Array<ArrayList<QuotationOrderNew>?> = arrayOfNulls(2)
        val newList = ArrayList(result.values)
        val askList = ArrayList<QuotationOrderNew>()
        val bidList = ArrayList<QuotationOrderNew>()
        for (dealNew in newList) {
            if (dealNew.a >= 0.0001) {
                if ("ASK".equals(dealNew.d, ignoreCase = true)) {
                    askList.add(dealNew)
                } else if ("BID".equals(dealNew.d, ignoreCase = true)) {
                    bidList.add(dealNew)
                }
            }
        }
        returnData[0] = askList
        returnData[1] = bidList
        return returnData
    }

    fun getQuotationOrderNewOldData(context: Context?, direction: String?): ArrayList<QuotationOrderNew> {
        val helper = FryingSQLiteHelper.getInstance(context!!)
        val db = helper?.readableDatabase
        var tableName: String? = null
        if ("ASK".equals(direction, ignoreCase = true)) {
            tableName = "quotation_order_ask"
        } else if ("BID".equals(direction, ignoreCase = true)) {
            tableName = "quotation_order_bid"
        }
        db?.beginTransaction()
        val result = ArrayList<QuotationOrderNew>()
        val cursor = helper?.rawQuery("select * from $tableName", null)
        cursor?.let {
            while (cursor.moveToNext()) {
                val orderNew = QuotationOrderNew(cursor)
                result.add(orderNew)
            }
        }
        cursor?.close()
        db?.setTransactionSuccessful()
        db?.endTransaction()
        return result
    }

    fun updateQuotationOrderNewData(context: Context?, newData: List<QuotationOrderNew?>?, removeAll: Boolean): Boolean {
        if (newData == null || newData.isEmpty()) {
            return false
        }
        var result = false
        val helper = FryingSQLiteHelper.getInstance(context!!)
        val db = helper?.writableDatabase
        db?.beginTransaction()
        var askOldData: ArrayList<QuotationOrderNew>? = null
        var bidOldData: ArrayList<QuotationOrderNew>? = null
        if (removeAll) {
            db?.delete("quotation_order_ask", null, null)
            db?.delete("quotation_order_bid", null, null)
        } else {
            askOldData = getQuotationOrderNewOldData(context, "ASK")
            bidOldData = getQuotationOrderNewOldData(context, "BID")
        }
        val allData = ArrayList<QuotationOrderNew?>()
        if (askOldData != null && askOldData.isNotEmpty()) {
            allData.addAll(askOldData)
        }
        if (bidOldData != null && bidOldData.isNotEmpty()) {
            allData.addAll(bidOldData)
        }
        allData.addAll(newData)
        val mergeData = mergeQuotationDealNew(allData)
        val askNewData = mergeData!![0]
        val bidNewData = mergeData[1]
        db?.delete("quotation_order_ask", null, null)
        db?.delete("quotation_order_bid", null, null)
        //保存卖单数据
        if (askNewData != null && askNewData.isNotEmpty()) {
            for (orderNew in askNewData) {
                val values = orderNew.contentValues
                helper?.insert("quotation_order_ask", null, values)
            }
            result = true
        }
        //保存买单数据
        if (bidNewData != null && bidNewData.isNotEmpty()) {
            for (orderNew in bidNewData) {
                val values = orderNew.contentValues
                helper?.insert("quotation_order_bid", null, values)
            }
            result = true
        }
        db?.setTransactionSuccessful()
        db?.endTransaction()
        return result
    }

    fun updateQuotationDealNewData(context: Context?, newData: List<QuotationDealNew>?, removeAll: Boolean): Boolean {
        if (newData == null || newData.isEmpty()) {
            return false
        }
        var result = false
        val helper = FryingSQLiteHelper.getInstance(context!!)
        val db = helper?.writableDatabase
        db?.beginTransaction()
        if (removeAll) {
            db?.delete("quotation_deal", null, null)
        }
        for (dealNew in newData) {
            val values = dealNew.contentValues
            helper?.insert("quotation_deal", null, values)
        }
        result = true
        val cursor = helper?.rawQuery("select * from quotation_deal order by createdTime desc", null)
        cursor?.let {
            if (cursor.count > 100) {
                cursor.move(99)
                while (cursor.moveToNext()) {
                    helper.delete("quotation_order_ask", " _id = ? ", arrayOf(cursor.getInt(cursor.getColumnIndex("_id")).toString()))
                }
            }
        }
        db?.setTransactionSuccessful()
        db?.endTransaction()
        return result
    }

    private val kLineDataSet: MutableMap<String, ArrayList<KLineItem>> = HashMap()

    fun saveKLineDataAll(data: JSONObject) {
        val kLineId = data.optString("no")
        val listData = data.optJSONArray("list")
        val list: ArrayList<KLineItem> = if (listData == null) ArrayList() else gson.fromJson(listData.toString(),
                object : TypeToken<ArrayList<KLineItem>?>() {}.type) as ArrayList<KLineItem>
        if (kLineId != null && list != null) {
            kLineDataSet[kLineId] = list
        }
    }

    fun removeKLineData(kLineId: String?) {
        kLineDataSet.remove(kLineId)
    }

    fun getKLineAllData(kLineId: String?): ArrayList<KLineItem>? {
        return kLineDataSet[kLineId]
    }

    fun updateDearPairs(context: Context?, handler: Handler?, dearPairs: Map<String?, Boolean?>?, action: Int) {
        if (context == null || handler == null || dearPairs == null) {
            return
        }
        handler.post(Runnable {
            val helper = FryingSQLiteHelper.getInstance(context)
            val db = helper?.writableDatabase
            db?.beginTransaction()
            if (action == DearPairService.ACTION_UPDATE) {
                for (pair in dearPairs.keys) {
                    val values = ContentValues()
                    val isDear = dearPairs[pair]
                    values.put("is_dear", if (isDear == null || !isDear) 0 else 1)
                    helper?.update("pairs_stats", values, "pair = ?", arrayOf(pair))
                }
            } else if (action == DearPairService.ACTION_REPLACE) {
                val where = StringBuilder()
                val truePairs = ArrayList<String?>()
                for (pair in dearPairs.keys) {
                    val isDear = dearPairs[pair]
                    if (isDear != null && isDear) {
                        truePairs.add(pair)
                        if (where.isEmpty()) {
                            where.append("?")
                        } else {
                            where.append(", ?")
                        }
                    }
                }
                if (where.isNotEmpty()) {
                    val trueValues = ContentValues()
                    trueValues.put("is_dear", 1)
                    helper?.update("pairs_stats", trueValues, "pair in ($where)", truePairs.toTypedArray())
                    val falseValues = ContentValues()
                    falseValues.put("is_dear", 0)
                    helper?.update("pairs_stats", falseValues, "pair not in ($where)", truePairs.toTypedArray())
                } else {
                    val falseValues = ContentValues()
                    falseValues.put("is_dear", 0)
                    helper?.update("pairs_stats", falseValues, "", null)
                }
            }
            db?.setTransactionSuccessful()
            db?.endTransaction()
            notifyPairUpdate(context, ArrayList(dearPairs.keys))
        })
    }

    private fun notifyPairUpdate(context: Context, updatedPairs: ArrayList<String?>?) {
        val intent = Intent()
        intent.action = ConstData.ACTION_SOCKET_DATA_CHANGED
        intent.setPackage(context.packageName)
        intent.putExtra(ConstData.SOCKET_DATA_CHANGED_TYPE, ConstData.CURRENT_PAIR_STATUS_REFRESH)
        if (updatedPairs != null && updatedPairs.isNotEmpty()) {
            intent.putStringArrayListExtra(ConstData.UPDATE_PAIRS, updatedPairs)
        }
        context.sendBroadcast(intent)
    }

    fun notifyPairChanged(context: Context?) {
        sendSocketCommandBroadcast(context, COMMAND_PAIR_CHANGED)
    }

    //发送数据更新通知
//发送数据更新通知
    @JvmOverloads
    fun sendSocketCommandBroadcast(context: Context?, type: Int, bundle: Bundle? = null) {
        if (context == null) {
            return
        }
        //        Intent intent = new Intent();
//        intent.setAction(ACTION_SOCKET_COMMAND);
//        intent.setPackage(context.getPackageName());
//        intent.putExtra(SOCKET_COMMAND, type);
//        if (bundle != null && !bundle.isEmpty()) {
//            intent.putExtra(SocketUtil.SOCKET_COMMAND_EXTRAS, bundle);
//        }
//        context.sendBroadcast(intent);
        sendSocketCommandObservable(type, bundle)
    }

    private val commandObservers = ArrayList<Observer<Message?>>()
    //添加socket命令观察者
    fun subscribeCommandObservable(observer: Observer<Message?>?) {
        if (observer == null) {
            return
        }
        synchronized(commandObservers) {
            if (!commandObservers.contains(observer)) {
                commandObservers.add(observer)
            }
        }
    }

    //移除socket命令观察者
    fun removeCommandObservable(observer: Observer<Message?>?) {
        if (observer == null) {
            return
        }
        synchronized(commandObservers) { commandObservers.remove(observer) }
    }

    fun sendSocketCommandObservable(type: Int, bundle: Bundle?) {
        //Log.e("sendSocketCommandObservable", "type?:" + type)
        val message = Message()
        message.what = type
        message.obj = bundle
        synchronized(commandObservers) {
            for (observer in commandObservers) {
                observer.onNext(message)
            }
        }
//        Observable.just(message)
//                .subscribeOn(Schedulers.io())
//                .observeOn(Schedulers.io())
//                .subscribe(object : SuccessObserver<Message?>() {
//                    override fun onSuccess(value: Message?) {
//                        if (value == null) {
//                            return
//                        }
//                        synchronized(commandObservers) {
//                            for (observer in commandObservers) {
//                                observer.onNext(value)
//                            }
//                        }
//                    }
//                })
    }
}