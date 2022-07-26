package com.black.lib.stickylistheaders

import java.util.*

/**
 * simple two way hashmap
 * @author lsjwzh
 */
internal class DualHashMap<TKey, TValue> {
    var mKeyToValue = HashMap<TKey, TValue>()
    var mValueToKey = HashMap<TValue, TKey>()
    fun put(t1: TKey, t2: TValue) {
        remove(t1)
        removeByValue(t2)
        mKeyToValue[t1] = t2
        mValueToKey[t2] = t1
    }

    fun getKey(value: TValue): TKey? {
        return mValueToKey[value]
    }

    operator fun get(key: TKey): TValue? {
        return mKeyToValue[key]
    }

    fun remove(key: TKey) {
        if (get(key) != null) {
            mValueToKey.remove(get(key))
        }
        mKeyToValue.remove(key)
    }

    fun removeByValue(value: TValue) {
        if (getKey(value) != null) {
            mKeyToValue.remove(getKey(value))
        }
        mValueToKey.remove(value)
    }
}