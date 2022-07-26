package com.black.lib.stickylistheaders

import java.util.*

/**
 * a hash map can maintain an one-to-many relationship which the value only belongs to one “one” part
 * and the map also support getKey by value quickly
 *
 * @author lsjwzh
 */

class DistinctMultiHashMap<TKey, TItemValue> {
    private var mIDMapper: IDMapper<TKey, TItemValue>? = null

    interface IDMapper<TKey, TItemValue> {
        fun keyToKeyId(key: TKey?): Any
        fun keyIdToKey(keyId: Any?): TKey
        fun valueToValueId(value: TItemValue): Any
        fun valueIdToValue(valueId: Any?): TItemValue
    }

    var mKeyToValuesMap = LinkedHashMap<Any, MutableList<TItemValue>?>()
    var mValueToKeyIndexer = LinkedHashMap<Any, TKey>()

    constructor() : this(object : IDMapper<TKey, TItemValue> {
        override fun keyToKeyId(key: TKey?): Any {
            return key as Any
        }

        override fun keyIdToKey(keyId: Any?): TKey {
            return keyId as TKey
        }

        override fun valueToValueId(value: TItemValue): Any {
            return value as Any
        }

        override fun valueIdToValue(valueId: Any?): TItemValue {
            return valueId as TItemValue
        }
    })

    constructor(idMapper: IDMapper<TKey, TItemValue>?) {
        this.mIDMapper = idMapper
    }

    operator fun get(key: TKey): List<TItemValue>? { //todo immutable
        return mKeyToValuesMap[mIDMapper?.keyToKeyId(key)]
    }

    fun getKey(value: TItemValue): TKey? {
        return mValueToKeyIndexer[mIDMapper?.valueToValueId(value)]
    }

    fun add(key: TKey, value: TItemValue) {
        val keyId = mIDMapper?.keyToKeyId(key)
        if (keyId != null && mKeyToValuesMap[keyId] == null) {
            mKeyToValuesMap[keyId] = ArrayList()
        }
        //remove old relationship
        val keyForValue = getKey(value)
        if (keyForValue != null) {
            mKeyToValuesMap[mIDMapper?.keyToKeyId(keyForValue)]?.remove(value)
        }
        mIDMapper?.valueToValueId(value)?.let {
            mValueToKeyIndexer[it] = key
        }
        if (!containsValue(mKeyToValuesMap[mIDMapper?.keyToKeyId(key)], value)) {
            mKeyToValuesMap[mIDMapper?.keyToKeyId(key)]?.add(value)
        }
    }

    fun removeKey(key: TKey) {
        if (mKeyToValuesMap[mIDMapper?.keyToKeyId(key)] != null) {
            for (value in mKeyToValuesMap[mIDMapper?.keyToKeyId(key)]!!) {
                mValueToKeyIndexer.remove(mIDMapper?.valueToValueId(value))
            }
            mKeyToValuesMap.remove(mIDMapper?.keyToKeyId(key))
        }
    }

    fun removeValue(value: TItemValue) {
        if (getKey(value) != null) {
            val itemValues = mKeyToValuesMap[mIDMapper?.keyToKeyId(getKey(value))]
            itemValues?.remove(value)
        }
        mValueToKeyIndexer.remove(mIDMapper?.valueToValueId(value))
    }

    fun clear() {
        mValueToKeyIndexer.clear()
        mKeyToValuesMap.clear()
    }

    fun clearValues() {
        for ((_, value) in entrySet()) {
            if (value != null) {
                value.clear()
            }
        }
        mValueToKeyIndexer.clear()
    }

    fun entrySet(): Set<Map.Entry<Any, MutableList<TItemValue>?>> {
        return mKeyToValuesMap.entries
    }

    fun reverseEntrySet(): Set<Map.Entry<Any, TKey>?>? {
        return mValueToKeyIndexer.entries
    }

    fun size(): Int {
        return mKeyToValuesMap.size
    }

    fun valuesSize(): Int {
        return mValueToKeyIndexer.size
    }

    protected fun containsValue(list: List<TItemValue>?, value: TItemValue): Boolean {
        for (itemValue in list!!) {
            if (mIDMapper?.valueToValueId(itemValue) == mIDMapper?.valueToValueId(value)) {
                return true
            }
        }
        return false
    }

    /**
     * @param position
     * @return
     */
    fun getValueByPosition(position: Int): TItemValue? {
        val vauleIdArray = mValueToKeyIndexer.keys.toTypedArray()
        if (position > vauleIdArray.size) {
            throw IndexOutOfBoundsException()
        }
        val valueId = vauleIdArray[position]
        return mIDMapper?.valueIdToValue(valueId)
    }
}