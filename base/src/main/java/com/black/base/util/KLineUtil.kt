package com.black.base.util

import com.black.base.model.socket.KLineChartItem
import com.black.base.model.socket.KLineItem
import com.black.base.widget.AnalyticChart
import com.black.util.CommonUtil
import java.util.*
import kotlin.math.sqrt

object KLineUtil {
    fun sum(array: DoubleArray, start: Int, range: Int): Double? {
        if (start < 0 || range < 0 || array.size <= start || array.size < start + range) {
            return null
        }
        var sum = 0.0
        for (i in 0 until range) {
            sum += array[start + i]
        }
        return sum
    }

    fun min(vararg `is`: Double): Double {
        var result = 0.0
        val length = `is`.size
        if (length == 0) {
            result = 0.0
        } else if (length == 1) {
            result = `is`[0]
        } else {
            result = `is`[0]
            for (i in 1 until length) {
                if (`is`[i] == 0.0) {
                    continue
                }
                result = Math.min(result, `is`[i])
            }
        }
        return result
    }

    fun min(vararg `is`: Long): Long {
        var result: Long = 0
        val length = `is`.size
        if (length == 0) {
            result = 0
        } else if (length == 1) {
            result = `is`[0]
        } else {
            result = `is`[0]
            for (i in 1 until length) {
                if (`is`[i] == 0L) {
                    continue
                }
                result = Math.min(result, `is`[i])
            }
        }
        return result
    }

    fun max(vararg `is`: Double): Double {
        var result = 0.0
        val length = `is`.size
        if (length == 0) {
            result = 0.0
        } else if (length == 1) {
            result = `is`[0]
        } else {
            result = `is`[0]
            for (i in 1 until length) {
                if (`is`[i] == 0.0) {
                    continue
                }
                result = Math.max(result, `is`[i])
            }
        }
        return result
    }

    fun max(vararg `is`: Long): Long {
        var result: Long = 0
        val length = `is`.size
        if (length == 0) {
            result = 0
        } else if (length == 1) {
            result = `is`[0]
        } else {
            result = `is`[0]
            for (i in 1 until length) {
                if (`is`[i] == 0L) {
                    continue
                }
                result = Math.max(result, `is`[i])
            }
        }
        return result
    }

    fun updateNode(des: KLineChartItem?, newKLineChartItem: KLineChartItem?) {
        if (des != null && newKLineChartItem != null) {
            des.out = newKLineChartItem.out
            des.`in` = newKLineChartItem.`in`
            des.high = newKLineChartItem.high
            des.low = newKLineChartItem.low
            des.VOL = newKLineChartItem.VOL
        }
    }

    fun sum(array: Array<KLineChartItem?>, start: Int, range: Int): Double? {
        if (start < 0 || range < 0 || array.size <= start || array.size < start + range) {
            return null
        }
        var sum = 0.0
        for (i in 0 until range) {
            sum += (array[start + i]?.out ?: 0.0)
        }
        return sum
    }

    fun sumVol(array: Array<KLineChartItem?>, start: Int, range: Int): Double? {
        if (start < 0 || range < 0 || array.size <= start || array.size < start + range) {
            return null
        }
        var sum = 0.0
        for (i in 0 until range) {
            sum += (array[start + i]?.VOL ?: 0.0)
        }
        return sum
    }

    fun equalNodeData(KLineChartItem1: KLineChartItem?, KLineChartItem2: KLineChartItem?): Boolean {
        return if (KLineChartItem1 != null && KLineChartItem2 != null) {
            KLineChartItem1.time == KLineChartItem2.time && KLineChartItem1.VOL == KLineChartItem2.VOL && KLineChartItem1.high == KLineChartItem2.high && KLineChartItem1.low == KLineChartItem2.low && KLineChartItem1.`in` == KLineChartItem2.`in` && KLineChartItem1.out == KLineChartItem2.out
        } else KLineChartItem1 == null && KLineChartItem2 == null
    }

    /**
     * 计算ma
     */
    fun calculateMA(dataList: List<KLineChartItem?>?, start: Int, end: Int) {
        var start1 = start
        var end1 = end
        if (dataList == null) {
            return
        }
        val size = dataList.size
        start1 = Math.max(start1, 0)
        end1 = Math.min(size, end1)
        var ma5 = 0.0
        var ma10 = 0.0
        var ma20 = 0.0
        var ma30 = 0.0
        var ma60 = 0.0
        for (i in start1 until end1) {
            val point = dataList[i]
            val closePrice = point?.out ?: 0.0
            ma5 += closePrice
            ma10 += closePrice
            ma20 += closePrice
            ma30 += closePrice
            ma60 += closePrice
            if (i == 4) {
                point?.MA5 = ma5 / 5f
            } else if (i >= 5) {
                ma5 -= dataList[i - 5]?.out ?: 0.0
                point?.MA5 = ma5 / 5f
            } else {
                point?.MA5 = 0.0
            }
            if (i == 9) {
                point?.MA10 = ma10 / 10f
            } else if (i >= 10) {
                ma10 -= dataList[i - 10]?.out ?: 0.0
                point?.MA10 = ma10 / 10f
            } else {
                point?.MA10 = 0.0
            }
            if (i == 19) {
                point?.MA20 = ma20 / 20f
            } else if (i >= 20) {
                ma20 -= dataList[i - 20]?.out ?: 0.0
                point?.MA20 = ma20 / 20f
            } else {
                point?.MA20 = 0.0
            }
            if (i == 29) {
                point?.MA30 = ma30 / 30f
            } else if (i >= 30) {
                ma30 -= dataList[i - 30]?.out ?: 0.0
                point?.MA30 = ma30 / 30f
            } else {
                point?.MA30 = 0.0
            }
            if (i == 59) {
                point?.MA60 = ma60 / 60f
            } else if (i >= 60) {
                ma60 -= dataList[i - 60]?.out ?: 0.0
                point?.MA60 = ma60 / 60f
            } else {
                point?.MA60 = 0.0
            }
        }
    }

    fun calculateVolumeMA(entries: List<KLineChartItem?>?, start: Int, end: Int) {
        var realStart = start
        var realEnd = end
        if (entries == null) {
            return
        }
        val size = entries.size
        realStart = Math.max(realStart, 0)
        realEnd = Math.min(size, realEnd)
        var volumeMa5 = 0f
        var volumeMa10 = 0f
        for (i in realStart until realEnd) {
            val entry = entries[i]
            volumeMa5 += (entry?.VOL?.toFloat() ?: 0f)
            volumeMa10 += (entry?.VOL?.toFloat() ?: 0.0f)
            if (i == 4) {
                entry?.VOLMA5 = (volumeMa5 / 5f).toDouble()
            } else if (i > 4) {
                volumeMa5 -= (entries[i - 5]?.VOL?.toFloat() ?: 0.0f)
                entry?.VOLMA5 = (volumeMa5 / 5f).toDouble()
            } else {
                entry?.VOLMA5 = 0.0
            }
            if (i == 9) {
                entry?.VOLMA10 = (volumeMa10 / 10f).toDouble()
            } else if (i > 9) {
                volumeMa10 -= (entries[i - 10]?.VOL?.toFloat() ?: 0.0f)
                entry?.VOLMA10 = (volumeMa10 / 10f).toDouble()
            } else {
                entry?.VOLMA10 = 0.0
            }
        }
    }

    /**
     * 计算macd
     */
    fun calculateMACD(KLineChartItems: List<KLineChartItem?>?, start: Int, end: Int) {
        var realStart = start
        var realEnd = end
        if (KLineChartItems == null) {
            return
        }
        val size = KLineChartItems.size
        realStart = Math.max(realStart, 0)
        realEnd = Math.min(size, realEnd)
        var ema12 = 0.0
        var ema26 = 0.0
        var dif = 0.0
        var dea = 0.0
        var macd = 0.0
        for (i in realStart until realEnd) {
            val point = KLineChartItems[i]
            val closePrice = point?.out ?: 0.0
            if (i == 0) {
                ema12 = closePrice
                ema26 = closePrice
            } else { // EMA（12） = 前一日EMA（12） X 11/13 + 今日收盘价 X 2/13
                ema12 = ema12 * 11f / 13f + closePrice * 2f / 13f
                // EMA（26） = 前一日EMA（26） X 25/27 + 今日收盘价 X 2/27
                ema26 = ema26 * 25f / 27f + closePrice * 2f / 27f
            }
            // DIF = EMA（12） - EMA（26） 。
// 今日DEA = （前一日DEA X 8/10 + 今日DIF X 2/10）
// 用（DIF-DEA）*2即为MACD柱状图。
            dif = ema12 - ema26
            dea = dea * 8f / 10f + dif * 2f / 10f
            macd = (dif - dea) * 2f
            point?.DIF = dif
            point?.DEA = dea
            point?.MACD = macd
        }
    }

    /**
     * 计算kdj
     */
    fun calculateKDJ(dataList: List<KLineChartItem?>?, start: Int, end: Int) {
        var realStart = start
        var realEnd = end
        if (dataList == null) {
            return
        }
        val size = dataList.size
        realStart = Math.max(realStart, 0)
        realEnd = Math.min(size, realEnd)
        var k = 0.0
        var d = 0.0
        for (i in realStart until realEnd) {
            val point = dataList[i]
            val closePrice = point?.out ?: 0.0
            var startIndex = i - 13
            if (startIndex < 0) {
                startIndex = 0
            }
            var max14 = Float.MIN_VALUE.toDouble()
            var min14 = Float.MAX_VALUE.toDouble()
            for (index in startIndex..i) {
                max14 = Math.max(max14, dataList[index]?.high ?: 0.0)
                min14 = Math.min(min14, dataList[index]?.low ?: 0.0)
            }
            var rsv = 100f * (closePrice - min14) / (max14 - min14)
            if (rsv.isNaN()) {
                rsv = 0.0
            }
            if (i == 0) {
                k = 50.0
                d = 50.0
            } else {
                k = (rsv + 2f * k) / 3f
                d = (k + 2f * d) / 3f
            }
            if (i < 13) {
                point?.K = 0.0
                point?.D = 0.0
                point?.J = 0.0
            } else if (i == 13 || i == 14) {
                point?.K = k
                point?.D = 0.0
                point?.J = 0.0
            } else {
                point?.K = k
                point?.D = d
                point?.J = 3f * k - 2 * d
            }
        }
    }

    /**
     * 计算RSI
     */
    fun calculateRSI(dataList: List<KLineChartItem?>?, start: Int, end: Int) {
        var realStart = start
        var realEnd = end
        if (dataList == null) {
            return
        }
        val size = dataList.size
        realStart = Math.max(realStart, 0)
        realEnd = Math.min(size, realEnd)
        var rsi: Double
        var rsiABSEma = 0.0
        var rsiMaxEma = 0.0
        for (i in realStart until realEnd) {
            val point = dataList[i]
            val closePrice = point?.out ?: 0.0
            if (i == 0) {
                rsi = 0.0
                rsiABSEma = 0.0
                rsiMaxEma = 0.0
            } else {
                val Rmax = Math.max(0.0, closePrice - (dataList[i - 1]?.out ?: 0.0))
                val RAbs = Math.abs(closePrice - (dataList[i - 1]?.out ?: 0.0))
                rsiMaxEma = (Rmax + (14f - 1) * rsiMaxEma) / 14f
                rsiABSEma = (RAbs + (14f - 1) * rsiABSEma) / 14f
                rsi = rsiMaxEma / rsiABSEma * 100
            }
            if (i < 13) {
                rsi = 0.0
            }
            if (rsi.isNaN()) rsi = 0.0
            point?.RSI = rsi
        }
    }

    /**
     * 计算wr
     */
    fun calculateWR(dataList: List<KLineChartItem?>?, start: Int, end: Int) {
        var realStart = start
        var realEnd = end
        if (dataList == null) {
            return
        }
        val size = dataList.size
        realStart = Math.max(realStart, 0)
        realEnd = Math.min(size, realEnd)
        var r: Double
        for (i in realStart until realEnd) {
            val point = dataList[i]
            var startIndex = i - 14
            if (startIndex < 0) {
                startIndex = 0
            }
            var max14 = Double.MIN_VALUE
            var min14 = Double.MAX_VALUE
            for (index in startIndex..i) {
                max14 = Math.max(max14, dataList[index]?.high ?: 0.0)
                min14 = Math.min(min14, dataList[index]?.low ?: 0.0)
            }
            if (i < 13) {
                point?.WR = (-10).toDouble()
            } else {
                r = -100 * (max14 - (dataList[i]?.high ?: 0.0)) / (max14 - min14)
                if (r.isNaN()) {
                    point?.WR = 0.0
                } else {
                    point?.WR = r
                }
            }
        }
    }

    /**
     * 计算 BOLL 需要在计算ma之后进行
     */
    fun calculateBOLL(dataList: List<KLineChartItem?>?, start: Int, end: Int) {
        var realStart = start
        var realEnd = end
        if (dataList == null) {
            return
        }
        val size = dataList.size
        realStart = Math.max(realStart, 0)
        realEnd = Math.min(size, realEnd)
        for (i in realStart until realEnd) {
            val point = dataList[i]
            if (i < 19) {
                point?.BOLL = 0.0
                point?.UB = 0.0
                point?.LB = 0.0
            } else {
                val n = 20
                var md = 0.0
                for (j in i - n + 1..i) {
                    val c = dataList[j]?.out ?: 0.0
                    val m = point?.MA20 ?: 0.0
                    val value = c - m
                    md += value * value
                }
                md /= (n - 1)
                md = sqrt(md)
                point?.BOLL = point?.MA20 ?: 0.0
                point?.UB = (point?.BOLL ?: 0.0) + 2f * md
                point?.LB = (point?.BOLL ?: 0.0) - 2f * md
            }
        }
    }

    /**
     * 计算MA BOLL RSI KDJ MACD
     */
    fun calculate(dataList: Array<KLineChartItem?>) {
        calculate(Arrays.asList(*dataList))
    }

    /**
     * 计算MA BOLL RSI KDJ MACD
     */
    fun calculate(dataList: List<KLineChartItem?>?) {
        if (dataList == null) {
            return
        }
        val start = 0
        val end = dataList.size
        calculate(dataList, start, end)
    }

    /**
     * 计算MA BOLL RSI KDJ MACD
     */
    fun calculate(dataList: Array<KLineChartItem?>, start: Int, end: Int) {
        calculate(Arrays.asList(*dataList), start, end)
    }

    /**
     * 计算MA BOLL RSI KDJ MACD
     */
    fun calculate(dataList: List<KLineChartItem?>?, start: Int, end: Int) {
        var realStart = start
        var realEnd = end
        if (dataList == null) {
            return
        }
        val size = dataList.size
        realStart = Math.max(realStart, 0)
        realEnd = Math.min(size, realEnd)
        calculateMA(dataList, realStart, realEnd)
        calculateBOLL(dataList, realStart, realEnd)
        calculateMACD(dataList, realStart, realEnd)
        calculateRSI(dataList, realStart, realEnd)
        calculateKDJ(dataList, realStart, realEnd)
        calculateWR(dataList, realStart, realEnd)
        calculateVolumeMA(dataList, realStart, realEnd)
        for (i in realStart until realEnd) {
            val kLineChartItem = dataList[i]
            kLineChartItem?.resetMaxValues()
        }
    }

    fun getAllNode(kLineItems: ArrayList<KLineItem?>?, value: Long): Array<KLineChartItem?> {
        var items = kLineItems
        if (items == null) {
            items = ArrayList()
        }
        val size = items.size
        val kLineChartItems = ArrayList<KLineChartItem?>(size)
        var lastKLineChartItem: KLineChartItem? = null
        for (i in 0 until size) {
            val kLineItem = items[i] ?: continue
            val kLineChartItem = KLineChartItem(kLineItem.t!!, kLineItem.a, kLineItem.h, kLineItem.l, kLineItem.o, kLineItem.c)
            if (kLineChartItem.low == 0.0) {
                if (lastKLineChartItem == null) {
                    continue
                } else {
                    kLineChartItem.low = lastKLineChartItem.out
                    kLineChartItem.high = kLineChartItem.low
                    kLineChartItem.`in` = kLineChartItem.high
                    kLineChartItem.out = kLineChartItem.`in`
                }
            }
            if (lastKLineChartItem != null) { //判断是否有断层
                val count = ((kLineChartItem.time - lastKLineChartItem.time) / value).toInt()
                val time = lastKLineChartItem.time
                for (ii in 0 until count - 1) {
                    val insertKLineChartItem = KLineChartItem(time + value * (ii + 1), 0.0, lastKLineChartItem.high, lastKLineChartItem.low, lastKLineChartItem.`in`, lastKLineChartItem.out)
                    kLineChartItems.add(insertKLineChartItem)
                    insertKLineChartItem.isAddData = true
                }
            }
            kLineChartItems.add(kLineChartItem)
            lastKLineChartItem = kLineChartItem
        }
        val nodeSize = kLineChartItems.size
        return kLineChartItems.toTypedArray()
    }

    fun addKLIneItemsToFront(oldItems: Array<KLineChartItem?>, addedItems: Array<KLineChartItem?>?, value: Long): Array<KLineChartItem?>? {
        val oldFirstItem = CommonUtil.getItemFromArray(oldItems, 0)
        val oldLastItem = CommonUtil.getItemFromArray(oldItems, oldItems.size - 1)
        if (oldFirstItem == null || oldLastItem == null) {
            return addedItems
        }
        val addedLastItem = if (addedItems == null) null else CommonUtil.getItemFromArray(addedItems, addedItems.size - 1)
        val addedFirstItem = CommonUtil.getItemFromArray(addedItems, 0)
        if (addedLastItem == null || addedFirstItem == null || oldFirstItem.time <= addedFirstItem.time || oldLastItem.time <= addedFirstItem.time) {
            return oldItems
        }
        val addedSize = addedItems?.size ?: 0
        val oldSize = oldItems.size
        val count = ((oldFirstItem.time - addedLastItem.time) / value).toInt()
        return when {
            count == 1 -> { //完美衔接
                val result = arrayOfNulls<KLineChartItem?>(addedSize + oldSize)
                System.arraycopy(addedItems!!, 0, result, 0, addedSize)
                System.arraycopy(oldItems, 0, result, addedSize, oldSize)
                result
            }
            count < 1 -> { //存在重叠item
                val realAddSize = addedSize + count - 1
                val result = arrayOfNulls<KLineChartItem?>(realAddSize + oldSize)
                System.arraycopy(addedItems!!, 0, result, 0, realAddSize)
                System.arraycopy(oldItems, 0, result, realAddSize, oldSize)
                result
            }
            else -> { //存在间隙
                val result = arrayOfNulls<KLineChartItem?>(addedSize + oldSize + count - 1)
                val time = addedLastItem.time
                for (ii in 0 until count - 1) {
                    val insertKLineChartItem = KLineChartItem(time + value * (ii + 1), 0.0, addedLastItem.high, addedLastItem.low, addedLastItem.`in`, addedLastItem.out)
                    insertKLineChartItem.isAddData = true
                    result[addedSize + ii] = insertKLineChartItem
                }
                System.arraycopy(addedItems!!, 0, result, 0, addedSize)
                System.arraycopy(oldItems, 0, result, addedSize + count - 1, oldSize)
                result
            }
        }
    }
}