package com.black.base.util

import android.content.Context
import android.content.SharedPreferences
import com.black.base.BaseApplication
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import java.lang.Exception
import java.util.*

object SharedPreferenceUtils {
    private val TAG: String = BaseApplication.instance.packageName
    private val sp: SharedPreferences =
        BaseApplication.instance.getSharedPreferences(TAG, Context.MODE_PRIVATE)

    /**
     * 保存数据到SharedPreferences
     *
     * @param key   键
     * @param value 需要保存的数据
     * @return 保存结果
     */
    fun putData(key: String?, value: Any): Boolean {
        var result: Boolean
        val editor = sp.edit()
        val type = value.javaClass.simpleName
        try {
            when (type) {
                "Boolean" -> editor.putBoolean(key, (value as Boolean))
                "Long" -> editor.putLong(key, (value as Long))
                "Float" -> editor.putFloat(key, (value as Float))
                "String" -> editor.putString(key, value as String)
                "Integer" -> editor.putInt(key, (value as Int))
                else -> {
                    val gson = Gson()
                    val json = gson.toJson(value)
                    editor.putString(key, json)
                }
            }
            result = true
        } catch (e: Exception) {
            result = false
            e.printStackTrace()
        }
        editor.apply()
        return result
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sp.getBoolean(key, defaultValue)
    }

    fun putBoolean(key: String, defaultValue: Boolean) {
        sp.edit().putBoolean(key, defaultValue).apply()
    }

    /**
     * 获取SharedPreferences中保存的数据
     *
     * @param key          键
     * @param defaultValue 获取失败默认值
     * @return 从SharedPreferences读取的数据
     */
    fun getData(key: String?, defaultValue: Any): Any? {
        var result: Any?
        val type = defaultValue.javaClass.simpleName
        try {
            result = when (type) {
                "Boolean" -> sp.getBoolean(key, (defaultValue as Boolean))
                "Long" -> sp.getLong(key, (defaultValue as Long))
                "Float" -> sp.getFloat(key, (defaultValue as Float))
                "String" -> sp.getString(key, defaultValue as String)
                "Integer" -> sp.getInt(key, (defaultValue as Int))
                else -> {
                    val gson = Gson()
                    val json = sp.getString(key, "")
                    if (json != "" && json.isNotEmpty()) {
                        gson.fromJson(json, defaultValue.javaClass)
                    } else {
                        defaultValue
                    }
                }
            }
        } catch (e: Exception) {
            result = null
            e.printStackTrace()
        }
        return result
    }

    /**
     * 用于保存集合
     *
     * @param key  key
     * @param list 集合数据
     * @return 保存结果
     */
    fun <T> putListData(sp: SharedPreferences, key: String?, list: LinkedList<T>): Boolean {
        var result: Boolean
        val editor = sp.edit()
        val array = JsonArray()
        if (list.size <= 0) {
            editor.putString(key, array.toString())
            editor.apply()
            return false
        }
        val type: String = list[0]!!::class.java.getSimpleName()
        try {
            when (type) {
                "Boolean" -> {
                    var i = 0
                    while (i < list.size) {
                        array.add(list[i] as Boolean)
                        i++
                    }
                }
                "Long" -> {
                    var i = 0
                    while (i < list.size) {
                        array.add(list[i] as Long)
                        i++
                    }
                }
                "Float" -> {
                    var i = 0
                    while (i < list.size) {
                        array.add(list[i] as Float)
                        i++
                    }
                }
                "String" -> {
                    var i = 0
                    while (i < list.size) {
                        array.add(list[i] as String)
                        i++
                    }
                }
                "Integer" -> {
                    var i = 0
                    while (i < list.size) {
                        array.add(list[i] as Int)
                        i++
                    }
                }
                else -> {
                    val gson = Gson()
                    var i = 0
                    while (i < list.size) {
                        val obj = gson.toJsonTree(list[i])
                        array.add(obj)
                        i++
                    }
                }
            }
            editor.putString(key, array.toString())
            result = true
        } catch (e: Exception) {
            result = false
            e.printStackTrace()
        }
        editor.apply()
        return result
    }

    /**
     * 获取保存的List
     *
     * @param key key
     * @return 对应的Lis集合
     */
    fun <T> getListData(sp: SharedPreferences, key: String?, cls: Class<T>?): LinkedList<T> {
        val list = LinkedList<T>()
        val json = sp.getString(key, "")
        if (json != "" && json.length > 0) {
            val gson = Gson()
            val array = JsonParser().parse(json).asJsonArray
            for (elem in array) {
                list.add(gson.fromJson(elem, cls))
            }
        }
        return list
    }
}