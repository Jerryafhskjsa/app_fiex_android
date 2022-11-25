package com.black.base.model.future

data class LeverageBracketResp(
    val leverageBrackets: List<LeverageBracket>,
    val symbol: String
)

data class LeverageBracket(
    val bracket: Int, //档位
    val maintMarginRate: String, //维持保证金率
    val maxLeverage: String, //最大杠杆倍数
    val maxNominalValue: String, //该层最大名义价值
    val maxStartMarginRate: String, //最大起始保证金率
    val minLeverage: String, //最小杠杆倍数
    val startMarginRate: String, //起始保证金率
    val symbol: String  //交易对
)