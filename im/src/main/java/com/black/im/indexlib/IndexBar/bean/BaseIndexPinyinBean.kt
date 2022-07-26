package com.black.im.indexlib.IndexBar.bean

/**
 * 介绍：索引类的汉语拼音的接口
 */
abstract class BaseIndexPinyinBean : BaseIndexBean() {
    var baseIndexPinyin: String? = null //名字的拼音
        private set

    fun setBaseIndexPinyin(baseIndexPinyin: String?): BaseIndexPinyinBean {
        this.baseIndexPinyin = baseIndexPinyin
        return this
    }

    //是否需要被转化成拼音， 类似微信头部那种就不需要 美团的也不需要
    //微信的头部 不需要显示索引
    //美团的头部 索引自定义
    //默认应该是需要的
    open val isNeedToPinyin: Boolean
        get() = true

    //需要转化成拼音的目标字段
    abstract fun getTarget(): String?
}