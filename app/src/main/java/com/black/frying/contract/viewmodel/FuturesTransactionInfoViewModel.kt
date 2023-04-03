package com.black.frying.contract.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.black.base.model.future.FundRateBean
import com.black.base.model.future.UserBalanceBean
import com.black.frying.contract.biz.okwebsocket.*
import com.black.frying.contract.viewmodel.dto.UserBalanceDto
import com.black.net.okhttp.OkWebSocketHelper
import com.black.net.okhttp.OkWebSocketHelper.IMessageLifeCycle

class FuturesTransactionInfoViewModel : ViewModel() {

    private val okWebSocketHelper: OkWebSocketHelper


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
    }

    private fun updateUserBalanceInfo() {
        userBalanceDto.postValue(UserBalanceDto.copyFrom(_userBalanceBean,_foundRateBean))
    }

}