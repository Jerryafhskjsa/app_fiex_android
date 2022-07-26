package com.black.im.util

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import java.util.*

object SharedPreferenceUtils {
    /**
     * 保存数据到SharedPreferences
     *
     * @param key   键
     * @param value 需要保存的数据
     * @return 保存结果
     */
    fun putData(sp: SharedPreferences, key: String?, value: Any): Boolean {
        val result: Boolean
        val editor = sp.edit()
        val type = value.javaClass.simpleName
        result = try {
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
            true
        } catch (e: Exception) {
            false
        }
        editor.apply()
        return result
    }

    /**
     * 获取SharedPreferences中保存的数据
     *
     * @param key          键
     * @param defaultValue 获取失败默认值
     * @return 从SharedPreferences读取的数据
     */
    fun getData(sp: SharedPreferences, key: String?, defaultValue: Any): Any? {
        val result: Any?
        val type = defaultValue.javaClass.simpleName
        result = try {
            when (type) {
                "Boolean" -> sp.getBoolean(key, (defaultValue as Boolean))
                "Long" -> sp.getLong(key, (defaultValue as Long))
                "Float" -> sp.getFloat(key, (defaultValue as Float))
                "String" -> sp.getString(key, defaultValue as String)
                "Integer" -> sp.getInt(key, (defaultValue as Int))
                else -> {
                    val gson = Gson()
                    val json = sp.getString(key, "")
                    if (json != "" && json.length > 0) {
                        gson.fromJson(json, defaultValue.javaClass)
                    } else {
                        defaultValue
                    }
                }
            }
        } catch (e: Exception) {
            null
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
        val result: Boolean
        val editor = sp.edit()
        val array = JsonArray()
        if (list.size <= 0) {
            editor.putString(key, array.toString())
            editor.apply()
            return false
        }
        val type = (list[0] as Any).javaClass.simpleName
        result = try {
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
            true
        } catch (e: Exception) {
            false
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
        if (json != "" && json.isNotEmpty()) {
            val gson = Gson()
            val array = JsonParser().parse(json).asJsonArray
            for (elem in array) {
                list.add(gson.fromJson(elem, cls))
            }
        }
        return list
    }
}