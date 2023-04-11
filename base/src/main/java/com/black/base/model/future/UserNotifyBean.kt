package com.black.base.model.future


//{
//
//    "symbol":"btc_usdt",
//    "positionType": "ISOLATED",
//    "positionSide": "LONG",
//    "positionSize":"123",  // 持仓数量
//
//    "notifyType": "WARN",  // 通知类型:  WARN：告警，即将被强平，PARTIAL：部分强平，LIQUIDATION：全部强平，ADL：自动减仓
//
//}
data class UserNotifyBean (
   val symbol :String,
   val positionType :String,
   val positionSide :String,
   val positionSize :String,  // 持仓数量
   val notifyType :String  // 通知类型:  WARN：告警，即将被强平，PARTIAL：部分强平，LIQUIDATION：全部强平，ADL：自动减仓
)