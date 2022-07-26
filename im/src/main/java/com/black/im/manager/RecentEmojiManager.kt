package com.black.im.manager

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import android.util.Base64
import java.io.*

//SharedPerferences工具

//SharedPerferences工具
class RecentEmojiManager private constructor(context: Context) {
    companion object {
        const val PREFERENCE_NAME = "recentFace" //"preference";
        fun make(context: Context): RecentEmojiManager {
            return RecentEmojiManager(context)
        }
    }

    private val mPreferences: SharedPreferences
    private val mEditor: SharedPreferences.Editor

    init {
        mPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        mEditor = mPreferences.edit()
    }

    fun getString(key: String?): String {
        return mPreferences.getString(key, "") ?: ""
    }

    fun putString(key: String?, value: String?): RecentEmojiManager {
        mEditor.putString(key, value).apply()
        return this
    }

    @Throws(IOException::class)
    fun putCollection(key: String?, collection: Collection<*>?): RecentEmojiManager { // 实例化一个ByteArrayOutputStream对象，用来装载压缩后的字节文件。
        val byteArrayOutputStream = ByteArrayOutputStream()
        // 然后将得到的字符数据装载到ObjectOutputStream
        val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
        // writeObject 方法负责写入特定类的对象的状态，以便相应的 readObject 方法可以还原它
        objectOutputStream.writeObject(collection)
        // 最后，用Base64.encode将字节文件转换成Base64编码保存在String中
        val collectionString = String(Base64.encode(byteArrayOutputStream.toByteArray(), Base64.DEFAULT))
        // 关闭objectOutputStream
        objectOutputStream.close()
        return putString(key, collectionString)
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    fun getCollection(key: String?): Collection<*>? {
        val collectionString = getString(key)
        if (TextUtils.isEmpty(collectionString) || TextUtils.isEmpty(collectionString.trim { it <= ' ' })) {
            return null
        }
        val mobileBytes = Base64.decode(collectionString.toByteArray(), Base64.DEFAULT)
        val byteArrayInputStream = ByteArrayInputStream(mobileBytes)
        val objectInputStream = ObjectInputStream(byteArrayInputStream)
        val collection = objectInputStream.readObject() as Collection<*>
        objectInputStream.close()
        return collection
    }
}