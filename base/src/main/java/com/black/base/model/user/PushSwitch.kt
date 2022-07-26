package com.black.base.model.user

class PushSwitch {
    var orderSwitch //订单开关
            : Boolean? = null
    var investSwitch //充值开关
            : Boolean? = null
    var withdrawSwitch //提现开关
            : Boolean? = null

    companion object {
        fun copyPushSwitch(pushSwitch: PushSwitch?): PushSwitch? {
            if (pushSwitch == null) {
                return null
            }
            val result = PushSwitch()
            result.orderSwitch = pushSwitch.orderSwitch
            result.investSwitch = pushSwitch.investSwitch
            result.withdrawSwitch = pushSwitch.withdrawSwitch
            return result
        }
    }
}