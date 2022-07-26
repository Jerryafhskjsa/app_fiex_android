package com.black.im.indexlib.IndexBar.helper

import com.black.im.indexlib.IndexBar.bean.BaseIndexPinyinBean

/**
 * 介绍：IndexBar 的 数据相关帮助类
 * 1 将汉语转成拼音
 * 2 填充indexTag
 * 3 排序源数据源
 * 4 根据排序后的源数据源->indexBar的数据源
 */
interface IIndexBarDataHelper {
    //汉语-》拼音
    fun convert(data: MutableList<out BaseIndexPinyinBean?>?): IIndexBarDataHelper

    //拼音->tag
    fun fillInexTag(data: MutableList<out BaseIndexPinyinBean?>?): IIndexBarDataHelper

    //对源数据进行排序（RecyclerView）
    fun sortSourceDatas(datas: MutableList<out BaseIndexPinyinBean?>?): IIndexBarDataHelper

    //对IndexBar的数据源进行排序(右侧栏),在 sortSourceDatas 方法后调用
    fun getSortedIndexDatas(sourceDatas: MutableList<out BaseIndexPinyinBean?>?, datas: MutableList<String?>?): IIndexBarDataHelper
}