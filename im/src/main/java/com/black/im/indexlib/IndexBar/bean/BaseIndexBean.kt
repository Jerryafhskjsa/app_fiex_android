package com.black.im.indexlib.IndexBar.bean

import com.black.im.indexlib.suspension.ISuspensionInterface
import java.io.Serializable

/**
 * 介绍：索引类的标志位的实体基类
 */
abstract class BaseIndexBean : ISuspensionInterface, Serializable {
    var baseIndexTag: String? = null //所属的分类（名字的汉语拼音首字母）
        private set

    fun setBaseIndexTag(baseIndexTag: String?): BaseIndexBean {
        this.baseIndexTag = baseIndexTag
        return this
    }

    override fun getSuspensionTag(): String? {
        return baseIndexTag
    }

    override fun isShowSuspension(): Boolean {
        return true
    }
}
