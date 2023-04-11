package com.black.frying.contract.viewmodel

import android.text.TextUtils
import android.util.Log
import android.util.Pair
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.black.base.model.SuccessObserver
import com.black.base.model.socket.TradeOrder
import com.black.base.model.socket.TradeOrderPairList
import com.black.base.util.CookieUtil
import com.black.base.util.SocketDataContainer
import com.black.base.util.SocketUtil
import com.black.frying.FryingApplication
import com.black.frying.viewmodel.ContractViewModel
import com.black.net.okhttp.OkWebSocketHelper
import com.black.util.CommonUtil
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.collections.ArrayList

class FuturesTransactionInfoDisplayViewModel : ViewModel() {

    val context = FryingApplication.instance()

    var okWebSocketHelper: OkWebSocketHelper? = null

    val onContractModelListener: OnContractModelListener?
        get() {
            TODO()
        }

    var lifecycleOwner: LifecycleOwner? = null

    var currentPair = CookieUtil.getCurrentFutureUPair(context)

    var depthObserver: Observer<Pair<String?, TradeOrderPairList?>>? = createDepthObserver()

    var askMax = 5
    var bidMax = 5


     fun onResume() {
       // super.onResume()
        if (depthObserver == null) {
            depthObserver = createDepthObserver()
        }
        SocketDataContainer.subscribeFutureDepthObservable(depthObserver)
    }

    private fun createDepthObserver(): Observer<Pair<String?, TradeOrderPairList?>> {
        return object : SuccessObserver<Pair<String?, TradeOrderPairList?>>() {
            override fun onSuccess(value: Pair<String?, TradeOrderPairList?>) {
                Log.d("iiiiii", "createDepthObserver,pair = " + value.first)
                Log.d(
                    "iiiiii",
                    "createDepthObserver,askOrderList->size = " + value.second?.askOrderList?.size
                )
                Log.d(
                    "iiiiii",
                    "createDepthObserver,bidOrderList->size = " + value.second?.bidOrderList?.size
                )
                if (TextUtils.equals(
                        currentPair,
                        value.first
                    ) && value.second != null && onContractModelListener != null
                ) {
                    sortTradeOrder(value.first, value.second)
                }
            }
        }
    }
    private fun sortTradeOrder(pair: String?, orderPairList: TradeOrderPairList?) {
        Observable.just(orderPairList)
            .flatMap(object : Function<TradeOrderPairList?, ObservableSource<Void>> {
                @Throws(Exception::class)
                override fun apply(orders: TradeOrderPairList): ObservableSource<Void> {
                    var tradeOrders = orders
//                        tradeOrders = tradeOrders ?: TradeOrderPairList()
                    tradeOrders.bidOrderList =
                        if (tradeOrders.bidOrderList == null) ArrayList() else tradeOrders.bidOrderList
                    tradeOrders.askOrderList =
                        if (tradeOrders.askOrderList == null) ArrayList() else tradeOrders.askOrderList
                    var bidTradeOrderList: List<TradeOrder?>? = tradeOrders.bidOrderList
                    if (bidTradeOrderList != null && bidTradeOrderList.isNotEmpty()) {
                        val bidTradeOrderListAfterFilter = ArrayList<TradeOrder>()
                        for (tradeOrder in bidTradeOrderList) {
                            //过滤出委托量>0的数据
                            if (tradeOrder != null && tradeOrder.exchangeAmount > 0 && TextUtils.equals(
                                    currentPair,
                                    tradeOrder.pair
                                )
                            ) {
                                bidTradeOrderListAfterFilter.add(tradeOrder)
                            }
                        }
                        //按照价格，精度和最大数量进行合并,
                        bidTradeOrderList = SocketUtil.mergeQuotationOrder(
                            bidTradeOrderListAfterFilter,
                            currentPair,
                            "BID",
                            15,
                            bidMax
                        )
                        bidTradeOrderList = bidTradeOrderList ?: ArrayList()
                        Collections.sort(bidTradeOrderList, TradeOrder.COMPARATOR_DOWN)
                    }
                    var askTradeOrderList: List<TradeOrder?>? = tradeOrders.askOrderList
                    if (askTradeOrderList != null && askTradeOrderList.isNotEmpty()) {
                        val askTradeOrderListAfterFilter = ArrayList<TradeOrder>()
                        for (tradeOrder in askTradeOrderList) {
                            if (tradeOrder != null && tradeOrder.exchangeAmount > 0 && TextUtils.equals(
                                    currentPair,
                                    tradeOrder.pair
                                )
                            ) {
                                askTradeOrderListAfterFilter.add(tradeOrder)
                            }
                        }
                        askTradeOrderList = SocketUtil.mergeQuotationOrder(
                            askTradeOrderListAfterFilter,
                            currentPair,
                            "ASK",
                            15,
                            askMax
                        )
                        askTradeOrderList = askTradeOrderList ?: ArrayList()
                        Collections.sort(askTradeOrderList, TradeOrder.COMPARATOR_UP)
                    }
                    val firstBidTrad = CommonUtil.getItemFromList(bidTradeOrderList, 0)
                    val firstBidTradPrice =
                        if (firstBidTrad == null) null else CommonUtil.parseBigDecimal(firstBidTrad.priceString)
                    val firstAskTrad = CommonUtil.getItemFromList(askTradeOrderList, 0)
                    val firstAskTradPrice =
                        if (firstAskTrad == null) null else CommonUtil.parseBigDecimal(firstAskTrad.priceString)
                    //如果买单价格大于等于卖单价格，说明数据异常，重新订阅socket挂单
                    val isError =
                        firstBidTradPrice != null && firstAskTradPrice != null && firstBidTradPrice >= firstAskTradPrice
                    if (isError) {
                        SocketUtil.sendSocketCommandBroadcast(
                            context,
                            SocketUtil.COMMAND_ORDER_RELOAD
                        )
                        return Observable.empty()
                    }
                    onContractModelListener?.onTradeOrder(
                        pair,
                        bidTradeOrderList,
                        askTradeOrderList
                    )
                    return Observable.empty()
                }

            })
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

     fun onStop() {
      //  super.onStop()
         if (depthObserver != null) {
             SocketDataContainer.removeFutureDepthObservable(depthObserver)
         }
    }

    interface OnContractModelListener {

        fun onTradeOrder(
            pair: String?,
            bidOrderList: List<TradeOrder?>?,
            askOrderList: List<TradeOrder?>?
        )

    }
}


