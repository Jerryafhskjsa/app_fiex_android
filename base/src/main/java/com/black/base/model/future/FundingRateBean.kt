package com.black.base.model.future

data class FundingRateBean(
    val fundingRate: String,
    var nextCollectionTime: Long,
    val symbol: String
)