package com.black.frying.activity

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import com.black.base.activity.BaseActivity
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.frying.service.FutureService
import com.black.lib.refresh.QRefreshLayout
import com.fbsex.exchange.R

class TestActivity() : BaseActivity(), QRefreshLayout.OnRefreshListener, QRefreshLayout.OnLoadListener, QRefreshLayout.OnLoadMoreCheckListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        refreshLayout = findViewById<QRefreshLayout>(R.id.refresh_layout)
        refreshLayout?.setRefreshHolder(RefreshHolderFrying(this))
        refreshLayout?.setOnRefreshListener(this)
        refreshLayout?.setOnLoadListener(this)
        refreshLayout?.setOnLoadMoreCheckListener(this)
        button=findViewById<Button>(R.id.btn_test);
        button?.setOnClickListener(View.OnClickListener {
            FutureService.getDepthOrder(this,"btc_usdt");
        })
        FutureService.initSymbol(this);
        FutureService.initMarkPrice(this);
    }
    var button:Button?=null
    var refreshLayout: QRefreshLayout? = null
    val handler = Handler()
    override fun onRefresh() {
        handler.postDelayed({
            refreshLayout?.setRefreshing(false)
        }, 300)
    }

    override fun onLoad() {
        handler.postDelayed({
            refreshLayout?.setLoading(false)
        }, 300)
    }

    override fun onLoadMoreCheck(): Boolean {
        return true
    }
}