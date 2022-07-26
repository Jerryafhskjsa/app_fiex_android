package com.black.base.sqlite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import com.black.base.util.FryingUtil.printError
import java.util.*

class FryingSQLiteHelper private constructor(context: Context, name: String, factory: CursorFactory?, version: Int) : SQLiteOpenHelper(context, name, factory, version) {
    private constructor(context: Context, name: String = dataBaseName, version: Int = VERSION) : this(context, name, null, version) {}

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_PAIRS_STATS)
        db.execSQL(CREATE_QUOTATION_ORDER_ASK)
        db.execSQL(CREATE_QUOTATION_ORDER_BID)
        db.execSQL(CREATE_QUOTATION_DEAL)
        db.execSQL(CREATE_USER_MINING)
        db.execSQL(CREATE_USER_ORDER)
        db.execSQL(CREATE_USER_WALLET)
        db.execSQL(CREATE_DEAR_PAIR)
        db.execSQL(CREATE_USER)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS pairs_stats")
        db.execSQL("DROP TABLE IF EXISTS quotation_order_ask")
        db.execSQL("DROP TABLE IF EXISTS quotation_order_bid")
        db.execSQL("DROP TABLE IF EXISTS quotation_deal")
        db.execSQL("DROP TABLE IF EXISTS user_mining")
        db.execSQL("DROP TABLE IF EXISTS user_order")
        db.execSQL("DROP TABLE IF EXISTS user_wallet")
        db.execSQL("DROP TABLE IF EXISTS dear_pair")
        db.execSQL("DROP TABLE IF EXISTS user")
        onCreate(db)
    }

    fun insert(table: String?, nullColumnHack: String?, values: ContentValues?): Long {
        val writableDatabase = writableDatabase
        return writableDatabase.insert(table, nullColumnHack, values)
    }

    fun update(table: String?, values: ContentValues?, whereClause: String?, whereArgs: Array<String?>?): Int {
        val writableDatabase = writableDatabase
        return writableDatabase.update(table, values, whereClause, whereArgs)
    }

    fun delete(table: String?, whereClause: String?, whereArgs: Array<String?>?): Int {
        val writableDatabase = writableDatabase
        return writableDatabase.delete(table, whereClause, whereArgs)
    }

    fun rawQuery(sql: String?, selectionArgs: Array<String?>?): Cursor {
        val readableDatabase = readableDatabase
        return readableDatabase.rawQuery(sql, selectionArgs)
    }

    fun selectSQLite(tableName: String, whereClause: String?, whereArgs: Array<String?>?): List<Map<String?, String>> {
        val readableDatabase = readableDatabase
        val result: MutableList<Map<String?, String>> = ArrayList()
        try {
            val cursor = readableDatabase.rawQuery("select * from " + tableName + if (whereClause == null) "" else " where $whereClause",
                    if (whereClause == null || whereArgs == null) null else whereArgs)
            var columns: Array<String?>? = null
            if (columns == null) {
                val columnCount = cursor.columnCount
                columns = arrayOfNulls(columnCount)
                for (i in 0 until columnCount) {
                    columns[i] = cursor.getColumnName(i)
                }
            }
            while (cursor.moveToNext()) {
                val map: MutableMap<String?, String> = HashMap()
                for (i in columns.indices) {
                    map[columns[i]] = cursor.getString(cursor.getColumnIndex(columns[i]))
                }
                result.add(map)
            }
            cursor.close()
        } catch (e: Exception) {
            printError(e)
        }
        return result
    }

    fun selectSQLite(tableNames: Array<String>, whereClause: String?, whereArgs: Array<String?>?): List<Map<String?, Any>> {
        val readableDatabase = readableDatabase
        val result: MutableList<Map<String?, Any>> = ArrayList()
        try {
            var selectSql = "select * from "
            for (i in tableNames.indices) {
                selectSql += if (i == 0) {
                    " " + tableNames[i] + " "
                } else {
                    ", " + tableNames[i] + " "
                }
            }
            selectSql += if (whereClause == null) "" else " where $whereClause"
            val cursor = readableDatabase.rawQuery(selectSql, if (whereClause == null || whereArgs == null) null else whereArgs)
            val columnCount = cursor.columnCount
            val columns = arrayOfNulls<String>(columnCount)
            for (i in 0 until columnCount) {
                columns[i] = cursor.getColumnName(i)
            }
            while (cursor.moveToNext()) {
                val map: MutableMap<String?, Any> = HashMap()
                for (i in columns.indices) {
                    map[columns[i]] = cursor.getString(cursor.getColumnIndex(columns[i]))
                }
                result.add(map)
            }
            cursor.close()
        } catch (e: Exception) {
            printError(e)
        }
        return result
    }

    companion object {
        private const val dataBaseName = "com_bioko_exchange.db"
        private const val VERSION = 10
        private var helper: FryingSQLiteHelper? = null
        @Synchronized
        fun getInstance(context: Context): FryingSQLiteHelper? {
            if (helper == null) {
                helper = FryingSQLiteHelper(context)
            }
            return helper
        }

        //交易对状态
        private const val CREATE_PAIRS_STATS = "create table pairs_stats (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "currentPrice double , " +
                "firstPriceToday double , " +
                "lastPrice double , " +
                "maxPrice double , " +
                "minPrice double , " +
                "pair VARCHAR , " +
                "priceChangeSinceToday double , " +
                "statDate long , " +
                "totalAmount double," +
                "order_no INTEGER," +
                "is_dear INTEGER " +
                ")"
        //行情订单 卖出
        private const val CREATE_QUOTATION_ORDER_ASK = "create table quotation_order_ask (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "anchorAmount double , " +
                "direction VARCHAR , " +
                "exchangeAmount double , " +
                "formattedPrice VARCHAR , " +
                "orderType VARCHAR , " +
                "pair VARCHAR , " +
                "price VARCHAR, " +
                "volume double " +
                ")"
        //行情订单 买入
        private const val CREATE_QUOTATION_ORDER_BID = "create table quotation_order_bid (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "anchorAmount double , " +
                "direction VARCHAR , " +
                "exchangeAmount double , " +
                "formattedPrice VARCHAR , " +
                "orderType VARCHAR , " +
                "pair VARCHAR , " +
                "price VARCHAR, " +
                "volume double " +
                ")"
        //行情订单 成交
        private const val CREATE_QUOTATION_DEAL = "create table quotation_deal (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "createdTime long , " +
                "dealAmount double , " +
                "formattedPrice VARCHAR , " +
                "pair VARCHAR , " +
                "price double," +
                "tradeDealDirection  VARCHAR" +
                ")"
        //交易总额数据
        private const val CREATE_USER_MINING = "create table user_mining (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "platform double , " +
                "personal double " +
                ")"
        //的当前委托数据
        private const val CREATE_USER_ORDER = "create table user_order (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "createdTime long , " +
                "dealAvgPrice double ," +
                "dealAmount double ," +
                "direction VARCHAR ," +
                "feeRate double ," +
                "frozenAmountByOrder double ," +
                "orderType VARCHAR ," +
                "id VARCHAR ," +
                "pair ETH_USDT ," +
                "price double ," +
                "status int ," +
                "totalAmount double " +
                ")"
        //当前钱包数据
        private const val CREATE_USER_WALLET = "create table user_wallet (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "coinAmount decimal(30,15)," +
                "coinType VARCHAR" +
                ")"
        //自选交易对数据
        private const val CREATE_DEAR_PAIR = "create table dear_pair (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "pair VARCHAR" +
                ")"
        //自选交易对数据
        private const val CREATE_USER = "create table user (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "county_code VARCHAR," +
                "user_name VARCHAR," +
                "uid VARCHAR," +
                "password VARCHAR," +
                "token VARCHAR," +
                "login_date LONG, " +
                "is_current_user INTEGER" +
                ")"
    }
}