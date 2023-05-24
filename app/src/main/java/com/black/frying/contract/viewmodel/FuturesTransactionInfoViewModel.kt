package com.black.frying.contract.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.black.base.api.FutureApiService
import com.black.base.api.FutureApiServiceHelper
import com.black.base.api.FutureSuspendApiService
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.future.ADLBean
import com.black.base.model.future.BalanceDetailBean
import com.black.base.model.future.FundRateBean
import com.black.base.model.future.UserBalanceBean
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.RxJavaHelper
import com.black.base.util.UrlConfig
import com.black.frying.FryingApplication
import com.black.frying.contract.biz.okwebsocket.market.FoundRateMessageHandler
import com.black.frying.contract.biz.okwebsocket.market.getUserOkWebSocket
import com.black.frying.contract.biz.okwebsocket.market.sendCommandUserListenKey
import com.black.frying.contract.biz.okwebsocket.user.UserWalletMessageHandler
import com.black.frying.contract.utils.getBuyLeverageMultiple
import com.black.frying.contract.utils.getSellLeverageMultiple
import com.black.frying.contract.viewmodel.dto.UserBalanceDto
import com.black.frying.service.FutureService
import com.black.net.okhttp.OkWebSocketHelper
import com.black.net.okhttp.OkWebSocketHelper.IMessageLifeCycle
import com.black.util.Callback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FuturesTransactionInfoViewModel : ViewModel() {

    private val okWebSocketHelper: OkWebSocketHelper


    // 开仓 杠杆倍数
    val buyLeverageMultiple = MutableLiveData<Int>()
    // 平仓 杠杆倍数
    val sellLeverageMultiple = MutableLiveData<Int>()





    val userBalanceDto = MutableLiveData<UserBalanceDto>()
    private var _userBalanceBean: UserBalanceBean? = null
    private var _foundRateBean: FundRateBean? = null

    init {
        val okWebSocket = getUserOkWebSocket()
        okWebSocketHelper = OkWebSocketHelper(okWebSocket)
        okWebSocketHelper.start()
        okWebSocketHelper.setIMessageLifeCycle(object :IMessageLifeCycle{
            override fun onOpen() {
                okWebSocketHelper.sendCommandUserListenKey()
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
        okWebSocketHelper.addMessageHandler(object : UserWalletMessageHandler() {
            override fun consumeMessage(userBalanceBean: UserBalanceBean) {
                _userBalanceBean = userBalanceBean
                updateUserBalanceInfo()
            }
        })
            .addMessageHandler(object: FoundRateMessageHandler(){
                override fun consumeMessage(bean: FundRateBean) {
                    _foundRateBean = bean
                    updateUserBalanceInfo()
                }

            })

        loadLeverageMultiple()
    }

    private fun loadLeverageMultiple() {
        viewModelScope.launch(Dispatchers.IO) {
            val buy = getBuyLeverageMultiple()
            val sell = getSellLeverageMultiple()
            buyLeverageMultiple.postValue(buy)
            sellLeverageMultiple.postValue(sell)
        }
    }
    fun setBuyLeverageMultiple(times :Int){
        buyLeverageMultiple.value = times
        viewModelScope.launch(Dispatchers.IO){
            setBuyLeverageMultiple(times)
        }
    }
    fun setSellLeverageMultiple(times :Int){
        sellLeverageMultiple.value = times
        viewModelScope.launch(Dispatchers.IO){
            setSellLeverageMultiple(times)
        }
    }

    private fun updateUserBalanceInfo() {
        userBalanceDto.postValue(UserBalanceDto.copyFrom(_userBalanceBean,_foundRateBean))
    }

    fun testApi() {
        val context = FryingApplication.instance()
        FutureApiServiceHelper.getBalanceDetail(context, "usdt", FutureService.underlyingType, false,
            object : Callback<HttpRequestResultBean<BalanceDetailBean?>?>() {
                override fun error(type: Int, error: Any?) {
                    Log.d(TAG, "error() called with: type = $type, error = $error")
                }

                override fun callback(returnData: HttpRequestResultBean<BalanceDetailBean?>?) {
                    Log.d(TAG, "callback() called with: returnData = $returnData")
                }
            })

    }
    fun testAPiSuspend(){
        viewModelScope.launch {
            val context = FryingApplication.instance()
            try {
                val detailSuspend = ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
                    .getService(FutureSuspendApiService::class.java)
                    ?.getBalanceDetailSuspend("usdt", FutureService.underlyingType)
                Log.d(TAG, "testAPiSuspend() called detailSuspend = ${detailSuspend}")
            }catch (e:Exception){
                e.printStackTrace()
            }

        }

    }

}