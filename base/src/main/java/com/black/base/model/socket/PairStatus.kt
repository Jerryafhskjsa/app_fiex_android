package com.black.base.model.socket

import com.black.base.model.BaseAdapterItem
import com.black.base.model.clutter.HomeTickersKline
import com.black.util.CommonUtil
import com.black.util.Findable
import com.black.util.NumberUtil
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.pow

//交易对状态
open class PairStatus : BaseAdapterItem(), Findable {
    var TAG = PairStatus::class.java.simpleName

    /******futures******/
    var underlyingType: String? = null//标的类型，币本位(C_BASED)，u本位(U_BASED)
    var initLeverage: Int? = null//初始杠杆倍数
    var contractSize: String? = null//合约乘数（面值）
    var supportEntrustType: String? =
        null//支持计划委托类型("TAKE_PROFIT,STOP,TAKE_PROFIT_MARKET,STOP_MARKET,TRAILING_STOP_MARKET")
    var supportTimeInForce: String? = null//支持有效方式("GTC,FOK,IOC,GTX")
    fun getSupportTimeInForceTypeList(): ArrayList<String?>? {
        var timeInForceTypeList: ArrayList<String?>? = ArrayList()
        if (supportTimeInForce != null) {
            var typeArray = supportTimeInForce!!.split(",").toTypedArray()
            var temp = typeArray.toList()
            for (i in temp.indices) {
                timeInForceTypeList?.add(temp[i])
            }
        }
        return timeInForceTypeList
    }

    /******futures******/
    //k线数据
    var kLineData: HomeTickersKline? = null
    var supportingPrecisionList //支持深度
            : ArrayList<Deep>? = null

    //交易量
    var tradeVolume: Double? = null
        set(value) {
            field = value
            tradeVolueFormat = NumberUtil.formatNumberNoGroup(tradeVolume, 2, 5)

        }
    var tradeVolueFormat: String? = null

    //交易额(同tradeAmount，优化会删掉)
    var totalAmount = 0.0
        set(value) {
            field = value
            totalAmountFromat = NumberUtil.formatNumberNoGroup(value, 2, 5)
        }
    var totalAmountFromat: String? = null

    //交易额
    var tradeAmount: Double? = null
        set(value) {
            field = value
            tradeAmountFormat = NumberUtil.formatNumberNoGroup(tradeAmount, 2, 5)
        }
    var tradeAmountFormat: String? = null

    var priceChangeSinceToday: Double? = null //涨跌百分比
        set(value) {
            field = value
            priceChangeSinceTodayFormat = priceChangeSinceTodayDisplay
        }

    //支持订单下单类型
    var supportOrderType: String? = null


    var hot: Boolean? = null
    var setType: Int? = null

    var pair: String? = null
        set(value) {
            field = value
            if (value != null) {
                val arr = value.split("_").toTypedArray()
                if (arr.size > 1) {
                    name = arr[0]
                    setName = arr[1]
                }
            }
        }
    var currentPrice = 0.0
        set(value) {
            field = value
            currentPriceFormat = NumberUtil.formatNumberNoGroup(value, precision)
        }
    var currentPriceCNY: Double? = null
    var firstPriceToday = 0.0
    var lastPrice = 0.0
    var maxPrice = 0.0
        set(value) {
            field = value
            maxPriceFormat = NumberUtil.formatNumberNoGroup(value, precision)
        }
    var minPrice = 0.0
        set(value) {
            field = value
            minPriceFormat = NumberUtil.formatNumberNoGroup(value, precision)
        }

    var statDate: Long = 0
    var precision = 15 //价格精度
    var order_no = Int.MAX_VALUE //排序
    var is_dear = false //是否收藏
    var pairName: String? = null
    var amountPrecision: Int? = null //交易数量精度
    var baseCoinPrecision: Int? = null //标的币种精度
    var baseCoinDisplayPrecision: Int? = null //标的币种显示精度
    var pricePrecision: Int? = null //价格精度
    var quantityPrecision: Int? = null //数量精度
    var quoteCoinPrecision: Int? = null //报价币种精度
    var quoteCoinDisplayPrecision: Int? = null //报价币种显示精度


    var isHighRisk: Boolean? = null //是否是ST币种
    var feeRate: Double? = null
    var miningConfig: String? = null
    var leverConfEntity: PairLeverConfig? = null

    /*=====用于显示字段 =====*/
    var name: String? = null
        get() {
            if (pair != null) {
                val arr = pair!!.split("_").toTypedArray()
                if (arr.size > 1) {
                    field = arr[0]
                }
            }
            return field
        }

    fun getSupportOrderTypeList(): ArrayList<String?> {
        val typeList: ArrayList<String?> = ArrayList()
        if (supportOrderType != null) {
            val typeArray = supportOrderType!!.split(",").toTypedArray()
            val temp = typeArray.toList()
            for (i in temp.indices) {
                typeList.add(temp[i])
            }
        }
        return typeList
    }

    var setName: String? = null
        get() {
            if (pair != null) {
                val arr = pair!!.split("_").toTypedArray()
                if (arr.size > 1) {
                    field = arr[1]
                }
            }
            return field
        }

    var currentPriceFormat: String? = null
    var currentPriceCNYFormat: String? = null
    var maxPriceFormat: String? = null
    var minPriceFormat: String? = null

    var priceChangeSinceTodayFormat: String? = null

    /*=====用于显示字段 =====*/
    override fun getType(): Int {
        return PAIR_STATUS
    }

    fun setCurrentPriceCNY(currentPriceCNY: Double?, nullText: String?) {
        this.currentPriceCNY = currentPriceCNY
        currentPriceCNYFormat =
            if (currentPriceCNY == null) nullText else CommonUtil.formatMoneyCNY(currentPriceCNY)
    }

    fun setMaxSupportPrecisionList(
        pricePrecision: String?,
        depthPrecisionMerge: Int?
    ): ArrayList<Deep> {
        var depth = depthPrecisionMerge
        var pricePrecision = pricePrecision
        var deepList = ArrayList<Deep>()
        for (index in 0..(depth?.minus(1) ?: 0)) {
            var deep = Deep()
            var d = pricePrecision?.toInt()?.minus(index)
            if (d != null) {
                var p = if (d < 0) {
                    10.0.pow(abs(d).toDouble())
                } else if (d > 0) {
                    pricePrecision?.toInt()
                        ?.let { NumberUtil.formatNumberNoGroup(1 / 10.0.pow(d!!.toDouble()), it) }
                } else {
                    1.0
                }
                deep.precision = d
                deep.deep = p.toString()
                deepList.add(deep)
            }
        }
        return deepList
    }

    val priceChangeSinceTodayDisplay: String
        get() {
            val sign = if (priceChangeSinceToday != null && priceChangeSinceToday!! > 0) "+" else ""
            return sign + NumberUtil.formatNumberDynamicScaleNoGroup(
                priceChangeSinceToday?.times(
                    100
                ), 4, 0, 2
            ) + "%"
        }

    //判断是否是HOT
    val isHot: Boolean
        get() = hot == null || hot!!

    //判断是否下跌
    val isDown: Boolean
        get() = priceChangeSinceToday == null || priceChangeSinceToday!! <= 0

    //判断交易对数据是否一致
    fun equalsData(pairStatus: PairStatus?): Boolean {
        return if (pairStatus == null) {
            false
        } else pair != null && pair == pairStatus.pair && currentPrice == pairStatus.currentPrice && firstPriceToday == pairStatus.firstPriceToday && lastPrice == pairStatus.lastPrice && maxPrice == pairStatus.maxPrice && minPrice == pairStatus.minPrice && totalAmount == pairStatus.totalAmount && priceChangeSinceToday != null && priceChangeSinceToday == pairStatus.priceChangeSinceToday
    }

    val compareString: String
        get() {
            val result = StringBuilder()
            result.append("pair").append(pair)
            result.append("currentPrice").append(currentPrice)
            result.append("maxPrice").append(maxPrice)
            result.append("minPrice").append(minPrice)
            result.append("totalAmount").append(totalAmount)
            result.append("priceChangeSinceToday").append(priceChangeSinceToday)
            result.append("currentPriceCNY").append(currentPriceCNY)
            result.append("is_dear").append(is_dear)
            result.append("order_no").append(order_no)
            return result.toString()
        }

    val isLever: Boolean
        get() = (leverConfEntity != null && leverConfEntity!!.open != null && leverConfEntity!!.open!!
                && leverConfEntity!!.type != null && leverConfEntity!!.type!! and PairLeverConfig.ONE == PairLeverConfig.ONE)

    override fun getFindKey(): Any {
        return pair!!
    }

    companion object {
        const val NORMAL_DATA = 0x0100
        const val LEVER_DATA = 0x0200
        var COMPARATOR = Comparator<PairStatus?> { o1, o2 ->
            if (o1 == null || o2 == null) {
                0
            } else
                o1.order_no - o2.order_no
        }
        var COMPARATOR_SINCE_DOWN: Comparator<PairStatus?> = Comparator<PairStatus?> { o1, o2 ->
            if (o1 == null || o2 == null || o1.priceChangeSinceToday == null && o2.priceChangeSinceToday == null) {
                return@Comparator 0
            }
            if (o1.priceChangeSinceToday == null) {
                return@Comparator 1
            }
            if (o2.priceChangeSinceToday == null) {
                -1
            } else -(o2.priceChangeSinceToday!!).compareTo(o1.priceChangeSinceToday!!)
        }
        var COMPARATOR_SINCE_UP: Comparator<PairStatus?> = Comparator<PairStatus?> { o1, o2 ->
            if (o1 == null || o2 == null || o1.priceChangeSinceToday == null && o2.priceChangeSinceToday == null) {
                return@Comparator 0
            }
            if (o1.priceChangeSinceToday == null) {
                return@Comparator 1
            }
            if (o2.priceChangeSinceToday == null) {
                -1
            } else -(o1.priceChangeSinceToday!!).compareTo(o2.priceChangeSinceToday!!)
        }
        var COMPARATOR_QUOTATION: Comparator<PairStatus?> = Comparator<PairStatus?> { o1, o2 -> 0 }
        var COMPARATOR_VOLUME_24: Comparator<PairStatus?> = Comparator<PairStatus?> { o1, o2 ->
            if (o1 == null || o2 == null || o1.tradeAmount == null && o2.tradeAmount == null) {
                return@Comparator 0
            }
            if (o1.tradeVolume == null) {
                return@Comparator 1
            }
            if (o2.tradeVolume == null) {
                -1
            } else (o1.tradeVolume!!).compareTo(o2.tradeVolume!!)
        }
    }
}
