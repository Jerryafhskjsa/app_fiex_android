package com.black.frying.contract.state

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.black.base.model.future.*
import com.black.base.model.socket.Deep
import com.black.base.model.socket.PairQuotation
import com.black.base.model.trade.TradeOrderDepth
import com.black.base.util.ConstData
import com.black.base.util.TimeUtil
import com.black.frying.contract.biz.model.FuturesRepository
import com.black.frying.contract.biz.okwebsocket.market.*
import com.black.frying.contract.viewmodel.model.FuturesCoinPair
import com.black.net.okhttp.OkWebSocketHelper
import com.black.net.okhttp.OkWebSocketHelper.IMessageLifeCycle
import com.black.util.NumberUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow

const val TAG = "FutureGlobalStateViewModel"

class FutureGlobalStateViewModel : ViewModel() {

    private val okWebSocketHelper: OkWebSocketHelper by lazy(mode = LazyThreadSafetyMode.PUBLICATION) {
        val okWebSocket = getMarketOkWebSocket()
        return@lazy OkWebSocketHelper(okWebSocket).apply {
            start()
            addAllMessageHandlers(this)
        }
    }
    private val futuresCoinPair: FuturesCoinPair? by lazy { FuturesCoinPair.load() }

    //杠杆倍数  逐仓
    var isolatedPositionBean: PositionBean? = null
    val isolatedPositionBeanLiveData = MutableLiveData<PositionBean>()

    //全仓
    var crossedPositionBean: PositionBean? = null
    val crossedPositionBeanLiveData = MutableLiveData<PositionBean>()


    var balanceBean: BalanceDetailBean? = null
    val balanceBeanLiveData = MutableLiveData<BalanceDetailBean>()

    var symbolList: List<SymbolBean>? = null
    var symbolBean: SymbolBean? = null

    //当前币种
    val symbolBeanLiveData = MutableLiveData<SymbolBean>()

    //价格百分比
    val pairQuotationLiveData = MutableLiveData<PairQuotation>()

    //指数价格
    val dealBeanLiveData = MutableLiveData<DealBean>()

    //全部深度数据
    val tradeOrderDepthLiveData = MutableLiveData<TradeOrderDepth>()

    //深度
    val deepBeanLiveData = MutableLiveData<DeepBean>()

    var supportingPrecisionList //支持深度
            : ArrayList<Deep>? = null

    //费率
    var fundRateBean: FundingRateBean? = null
    val fundRateBeanLiveData = MutableLiveData<FundRateBean?>()

    //价格百分比
    val indexPriceBeanLiveData = MutableLiveData<IndexPriceBean>()

    //k line
    val kLineBeanLiveData = MutableLiveData<KLineBean>()

    //标记价格
    val markPriceBeanLiveData = MutableLiveData<MarkPriceBean>()

    //订阅交易对的信息
    val tickerBeanLiveData = MutableLiveData<TickerBean>()


    //价格精度
    val pricePrecision = MutableLiveData<Int>(ConstData.DEFAULT_PRECISION)

    //数量精度
    val amountPrecision = MutableLiveData<Int>(ConstData.DEFAULT_AMOUNT_PRECISION)


    init {
        initCoinPair()
    }

    private fun initCoinPair() {
        viewModelScope.launch(Dispatchers.IO) {
            val response = FuturesRepository.getSymbolList() ?: return@launch
            if (response.isOk()) {
                val symbolLists = response.result
                if (!symbolLists.isNullOrEmpty()) {
                    symbolList = symbolLists
                    val source = futuresCoinPair?.source()
                    val tempSymbolBean = if (source == null) {
                        symbolLists.first()
                    } else {
                        symbolLists.first { bean -> source == bean.symbol }
                    }
                    symbolBeanLiveData.postValue(tempSymbolBean)
                    handlePrecisionData(tempSymbolBean)
                    sendSymbolCommand()
                    getCoinPositionList()
                    getCoinFundingRate()
                    getAccountDetail()
                    getDeepBeanInfo()
                }
            }
        }
    }

    private fun handlePrecisionData(bean: SymbolBean) {
        symbolBean = bean
        var maxPrecision = (bean.pricePrecision)
        if (maxPrecision == 0) maxPrecision = ConstData.DEFAULT_PRECISION
        pricePrecision.postValue(maxPrecision)

        var maxAPrecision = (bean.quantityPrecision)
        if (maxAPrecision == 0) maxAPrecision = ConstData.DEFAULT_AMOUNT_PRECISION
        amountPrecision.postValue(maxAPrecision)

        supportingPrecisionList =
            setMaxSupportPrecisionList(maxPrecision.toString(), bean.depthPrecisionMerge)
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

    private fun getDeepBeanInfo() {
        viewModelScope.launch {
            symbolBean?.let {
                val deepGraph = FuturesRepository.getDeepGraph(it.symbol)


//                deepBeanLiveData.postValue()
            }
        }
    }

    fun getAccountDetail() {
        viewModelScope.launch {
            symbolBean?.let { bean ->
                balanceBean = FuturesRepository.getBalanceDetailSuspend(bean.quoteCoin)
                // TODO: 计算盈亏 根据持仓列表算
                balanceBeanLiveData.postValue(balanceBean)
            }

        }
    }

    private fun getCoinFundingRate() {
        viewModelScope.launch {
            symbolBean?.let { symbolBean ->
                viewModelScope.launch {
                    fundRateBean = FuturesRepository.getFundingRate(symbolBean.symbol)
                    fundRateBean?.apply {
                        val time = nextCollectionTime
                        if (time <= 0) {
                            return@launch
                        }
                        val seconds = (time.minus(System.currentTimeMillis())).div(1000)
                        nextCollectionTime = seconds
                        fundRateBeanLiveData.postValue(
                            FundRateBean(
                                symbol,
                                fundingRate,
                                TimeUtil.formatSeconds(
                                    seconds
                                )
                            )
                        )
                        timerTask()

                    }
                }
            }
        }
    }


    private suspend fun timerTask() {
        viewModelScope.launch {
            fundRateBean?.let { bean ->
                if (bean.nextCollectionTime <= 0) {
                    return@launch
                } else {
                    delay(1000)
                    val time = bean.nextCollectionTime - 1
                    bean.nextCollectionTime = time
                    fundRateBeanLiveData.postValue(
                        FundRateBean(
                            bean.symbol,
                            bean.fundingRate,
                            TimeUtil.formatSeconds(time)
                        )
                    )

                    timerTask()
                }
            }
        }
    }

    private fun getCoinPositionList() {
        symbolBean?.let { symbol ->
            viewModelScope.launch {
                val positionList = FuturesRepository.getPositionList(symbol.symbol)
                positionList?.let { list ->
                    if (list.size != 2) {
                        return@launch
                    }
                    val first = list.first()
                    isolatedPositionBean = first
                    isolatedPositionBeanLiveData.postValue(first)
                    val last = list.last()
                    crossedPositionBean = last
                    crossedPositionBeanLiveData.postValue(last)
                }
            }
        }
    }

    fun adjustLeverage(positionSide: String, leverage: Int) {
        viewModelScope.launch {
            isolatedPositionBean?.let {
                it.leverage = leverage
                it.positionSide = positionSide
//                it.symbol = symbol
                val isOk = FuturesRepository.adjustLeverage(
                    it.symbol ?: "", it.positionSide ?: "", it.leverage ?: 0
                )
                isolatedPositionBeanLiveData.postValue(it)
            } ?: return@launch
        }
    }

    fun refreshCoinPair() {

    }

    private fun sendSymbolCommand() {
        symbolBean?.let {
            viewModelScope.launch {
                okWebSocketHelper.apply {
                    setIMessageLifeCycle(object : IMessageLifeCycle {
                        override fun onOpen() {
                            sendCommandSymbol(it.symbol)
                        }

                        override fun onMessage(msg: String?) {
                        }

                        override fun onClosing(code: Int, reason: String?) {
                        }

                        override fun onClosed(code: Int, reason: String?) {
                        }

                        override fun onError(throwable: Throwable?) {
                        }
                    })
                    sendCommandSymbol(it.symbol)
                }
            }
        }
    }

    private fun addAllMessageHandlers(okWebSocketHelper: OkWebSocketHelper) {
        okWebSocketHelper.addMessageHandler(object : SincePriceMessageHandler() {
            override fun consumeMessage(pairQuotation: PairQuotation) {
                pairQuotationLiveData.postValue(pairQuotation)
            }
        }).addMessageHandler(object : DealMessageHandler() {
            override fun consumeMessage(bean: DealBean) {
                dealBeanLiveData.postValue(bean)
            }
        }).addMessageHandler(object : DeepFullMessageHandler() {
            override fun consumeMessage(deepBean: TradeOrderDepth) {
                tradeOrderDepthLiveData.postValue(deepBean)
            }

        }).addMessageHandler(object : DeepMessageHandler() {
            override fun consumeMessage(deepBean: DeepBean) {
                deepBeanLiveData.postValue(deepBean)
            }
        }).addMessageHandler(object : FoundRateMessageHandler() {
            override fun consumeMessage(bean: FundRateBean) {
                fundRateBeanLiveData.postValue(bean)
            }

        }).addMessageHandler(object : IndexPriceMessageHandler() {

            override fun consumeMessage(bean: IndexPriceBean) {
                indexPriceBeanLiveData.postValue(bean)
            }
        }).addMessageHandler(object : KLineMessageHandler() {
            override fun consumeMessage(bean: KLineBean) {
                kLineBeanLiveData.postValue(bean)
            }
        }).addMessageHandler(object : MarkPriceMessageHandler() {

            override fun consumeMessage(bean: MarkPriceBean) {
                markPriceBeanLiveData.postValue(bean)
            }
        }).addMessageHandler(object : TickerMessageHandler() {
            override fun consumeMessage(bean: TickerBean) {
                tickerBeanLiveData.postValue(bean)
            }
        })
    }

    fun printThis() {
        Log.d(TAG, "printThis() called ${this.hashCode()}")
    }
}