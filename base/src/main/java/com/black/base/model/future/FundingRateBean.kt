package com.black.base.model.future

data class FundingRateBean(
    val fundingRate: String,
    val nextCollectionTime: Long,
    val symbol: String
)