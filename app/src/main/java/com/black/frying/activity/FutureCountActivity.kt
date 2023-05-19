package com.black.frying.activity

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActivity
import com.black.base.model.payOrder
import com.black.base.util.RouterConstData
import com.black.router.annotation.Route
import com.black.wallet.R
import com.fbsex.exchange.databinding.FutureCountActivityBinding


@Route(value = [RouterConstData.FUTURE_COUNT_ACTIVITY])

class FutureCountActivity: BaseActivity(), View.OnClickListener{
    private var binding: FutureCountActivityBinding? = null
    private var order: payOrder? = null
    private var direction: String? = null
    private var bank: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, com.fbsex.exchange.R.layout.future_count_activity)

    }


    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String? {
        return "计算器"
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.bar_a -> {
            }
            R.id.btn_submit -> {

            }
        }
    }

}