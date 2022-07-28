package com.black.base.util

import android.content.ContentValues
import android.content.Context
import com.black.base.model.user.User
import com.black.base.sqlite.FryingSQLiteHelper
import com.black.util.Encryptor
import java.util.*

object DataBaseUtil {
    fun saveUser(context: Context?, user: User?) {
        if (context == null || user == null) {
            return
        }
        val helper = FryingSQLiteHelper.getInstance(context)
        val db = helper!!.writableDatabase
        db.beginTransaction()
        user.userName = Encryptor.encrypt(user.userName, ConstData.FRYING_PASSWORD)
        user.password = Encryptor.encrypt(user.password, ConstData.FRYING_PASSWORD)
        user.token = Encryptor.encrypt(user.token, ConstData.FRYING_PASSWORD)
        user.loginDate = System.currentTimeMillis()
        //查询是否已经记录了该用户
        val cursor = helper.rawQuery("select * from user where user_name = ?", arrayOf(user.userName))
        val values = user.contentValues
        if (cursor.count == 0) {
            helper.insert("user", null, values)
        } else {
            helper.update("user", values, "user_name = ?", arrayOf(user.userName))
        }
        cursor.close()
        db.setTransactionSuccessful()
        db.endTransaction()
    }

    fun saveUser(context: Context?, telCountryCode: String?, userName: String?, password: String?, token: String?) {
        if (context == null) {
            return
        }
        var name = userName
        var password1 = password
        var token1 = token
        val helper = FryingSQLiteHelper.getInstance(context)
        val db = helper!!.writableDatabase
        db.beginTransaction()
        name = Encryptor.encrypt(name, ConstData.FRYING_PASSWORD)
        password1 = Encryptor.encrypt(password1, ConstData.FRYING_PASSWORD)
        token1 = Encryptor.encrypt(token1, ConstData.FRYING_PASSWORD)
        val user = User()
        user.telCountryCode = telCountryCode
        user.userName = name
        user.password = password1
        user.token = token1
        user.loginDate = System.currentTimeMillis()
        //查询是否已经记录了该用户
        val cursor = helper.rawQuery("select * from user where user_name = ?", arrayOf(name))
        val values = user.contentValues
        if (cursor.count == 0) {
            helper.insert("user", null, values)
        } else {
            helper.update("user", values, "user_name = ?", arrayOf(name))
        }
        cursor.close()
        db.setTransactionSuccessful()
        db.endTransaction()
    }

    fun removeUser(context: Context?, user: User?) {
        if (context == null || user == null) {
            return
        }
        val helper = FryingSQLiteHelper.getInstance(context)
        val db = helper!!.writableDatabase
        db.beginTransaction()
        if (user.id != null) {
            helper.delete("user", "_id = ?", arrayOf(user.id.toString()))
        } else if (user.userName != null) {
            val userName = Encryptor.encrypt(user.userName, ConstData.FRYING_PASSWORD)
            helper.delete("user", "user_name = ?", arrayOf(userName))
        }
        db.setTransactionSuccessful()
        db.endTransaction()
    }

    fun getSavedUsers(context: Context?): MutableList<User?>? {
        if (context == null) {
            return null
        }
        val helper = FryingSQLiteHelper.getInstance(context)
        val db = helper!!.readableDatabase
        db.beginTransaction()
        val list: MutableList<User?> = ArrayList()
        val cursor = helper.rawQuery("select * from user order by login_date desc", null)
        while (cursor.moveToNext()) {
            val user = User(cursor)
            user.userName = Encryptor.decrypt(user.userName, ConstData.FRYING_PASSWORD)
            user.password = Encryptor.decrypt(user.password, ConstData.FRYING_PASSWORD)
            user.token = Encryptor.decrypt(user.token, ConstData.FRYING_PASSWORD)
            user.ucToken = Encryptor.decrypt(user.ucToken, ConstData.FRYING_PASSWORD)
            list.add(user)
        }
        cursor.close()
        db.setTransactionSuccessful()
        db.endTransaction()
        return list
    }

    fun refreshCurrentUser(context: Context?, user: User?) {
        if (context == null || user == null) {
            return
        }
        val helper = FryingSQLiteHelper.getInstance(context)
        val db = helper!!.writableDatabase
        db.beginTransaction()
        val userName = Encryptor.encrypt(user.userName, ConstData.FRYING_PASSWORD)
        val clearValues = ContentValues()
        clearValues.put("is_current_user", 0)
        helper.update("user", clearValues, " is_current_user = ? and user_name <> ?", arrayOf("1", userName))
        val setValues = ContentValues()
        setValues.put("is_current_user", 1)
        helper.update("user", setValues, " user_name = ?", arrayOf(userName))
        db.setTransactionSuccessful()
        db.endTransaction()
    }

    fun clear(context: Context?) {
        if (context == null) {
            return
        }
        val helper = FryingSQLiteHelper.getInstance(context)
        val db = helper!!.writableDatabase
        db.beginTransaction()
        db.delete("user", null, null)
        db.setTransactionSuccessful()
        db.endTransaction()
    }
}