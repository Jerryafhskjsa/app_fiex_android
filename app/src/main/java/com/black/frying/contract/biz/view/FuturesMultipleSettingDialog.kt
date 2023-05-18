package com.black.frying.contract.biz.view

import android.content.Context
import android.view.LayoutInflater
import com.fbsex.exchange.databinding.FuturesDialogLayoutMultipleSettingBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class FuturesMultipleSettingDialog(ctx: Context) : BottomSheetDialog(ctx) {
    val binding =
        FuturesDialogLayoutMultipleSettingBinding.inflate(LayoutInflater.from(context))

    init {
        setContentView(binding.root)
    }


}