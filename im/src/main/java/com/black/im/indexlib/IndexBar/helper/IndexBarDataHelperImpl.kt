package com.black.im.indexlib.IndexBar.helper

import com.black.im.indexlib.IndexBar.bean.BaseIndexPinyinBean
import com.github.promeg.pinyinhelper.Pinyin
import java.util.*

/**
 * 介绍：IndexBar 的 数据相关帮助类 实现
 * 1 将汉语转成拼音(利用tinyPinyin)
 * 2 填充indexTag (取拼音首字母)
 * 3 排序源数据源
 * 4 根据排序后的源数据源->indexBar的数据源
 */
class IndexBarDataHelperImpl : IIndexBarDataHelper {
    /**
     * 如果需要，
     * 字符->拼音，
     *
     * @param data
     */
    override fun convert(data: MutableList<out BaseIndexPinyinBean?>?): IIndexBarDataHelper {
        if (!(null != data && data.isNotEmpty())) {
            return this
        }
        val size = data.size
        for (i in 0 until size) {
            val indexPinyinBean = data[i]
            val pySb = StringBuilder()
            //add by zhangxutong 2016 11 10 如果不是top 才转拼音，否则不用转了
            if (true == indexPinyinBean?.isNeedToPinyin) {
                val target = indexPinyinBean.getTarget() //取出需要被拼音化的字段
                //遍历target的每个char得到它的全拼音
                for (i1 in 0 until (target?.length ?: 0)) {
                    //利用TinyPinyin将char转成拼音
                    //查看源码，方法内 如果char为汉字，则返回大写拼音
                    //如果c不是汉字，则返回String.valueOf(c)
                    pySb.append(Pinyin.toPinyin(target!![i1]).toUpperCase())
                }
                indexPinyinBean.setBaseIndexPinyin(pySb.toString()) //设置城市名全拼音
            } else {
                //pySb.append(indexPinyinBean.getBaseIndexPinyin());
            }
        }
        return this
    }

    /**
     * 如果需要取出，则
     * 取出首字母->tag,或者特殊字母 "#".
     * 否则，用户已经实现设置好
     *
     * @param data
     */
    override fun fillInexTag(data: MutableList<out BaseIndexPinyinBean?>?): IIndexBarDataHelper {
        if (null == data || data.isEmpty()) {
            return this
        }
        val size = data.size
        for (i in 0 until size) {
            val indexPinyinBean = data[i]
            if (true == indexPinyinBean?.isNeedToPinyin) {
                //以下代码设置城市拼音首字母
                val tagString = indexPinyinBean.baseIndexPinyin?.substring(0, 1)
                if (true == tagString?.matches(Regex("[A-Z]"))) {
                    //如果是A-Z字母开头
                    indexPinyinBean.setBaseIndexTag(tagString)
                } else {
                    //特殊字母这里统一用#处理
                    indexPinyinBean.setBaseIndexTag("#")
                }
            }
        }
        return this
    }

    override fun sortSourceDatas(datas: MutableList<out BaseIndexPinyinBean?>?): IIndexBarDataHelper {
        if (null == datas || datas.isEmpty()) {
            return this
        }
        convert(datas)
        fillInexTag(datas)
        //对数据源进行排序
        Collections.sort(datas, Comparator<BaseIndexPinyinBean?> { lhs, rhs ->
            if (true != lhs?.isNeedToPinyin) {
                0
            } else if (true != rhs?.isNeedToPinyin) {
                0
            } else if (lhs.baseIndexTag == "#") {
                1
            } else if (rhs.baseIndexTag == "#") {
                -1
            } else {
                lhs.baseIndexPinyin?.compareTo(rhs.baseIndexPinyin!!) ?: 0
            }
        })
        return this
    }

    override fun getSortedIndexDatas(sourceDatas: MutableList<out BaseIndexPinyinBean?>?, datas: MutableList<String?>?): IIndexBarDataHelper {
        if (null == sourceDatas || sourceDatas.isEmpty()) {
            return this
        }
        //按数据源来 此时sourceDatas 已经有序
        val size = sourceDatas.size
        var baseIndexTag: String?
        for (i in 0 until size) {
            baseIndexTag = sourceDatas[i]?.baseIndexTag
            if (true != datas?.contains(baseIndexTag)) {
                //则判断是否已经将这个索引添加进去，若没有则添加
                datas?.add(baseIndexTag)
            }
        }
        return this
    }
}