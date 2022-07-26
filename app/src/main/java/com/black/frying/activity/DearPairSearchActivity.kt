package com.black.frying.activity

import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.databinding.ListViewEmptyLongBinding
import com.black.base.service.DearPairService
import com.black.base.util.*
import com.black.frying.adapter.PairSearchAdapter
import com.black.frying.adapter.PairSearchAdapter.OnSearchHandleListener
import com.black.frying.model.PairSearch
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.CallbackObject
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ActivityDearPairSearchBinding
import skin.support.app.SkinCompatDelegate
import java.util.*

@Route(value = [RouterConstData.DEAR_PAIR_SEARCH])
class DearPairSearchActivity : BaseActionBarActivity(), View.OnClickListener, OnSearchHandleListener {
    private var adapter: PairSearchAdapter? = null
    private var binding: ActivityDearPairSearchBinding? = null
    //异步获取数据
    private var handlerThread: HandlerThread? = null
    private var socketHandler: Handler? = null
    private val dearPairs = ArrayList<String?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dear_pair_search)
        binding?.coinEdit?.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND || event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                searchPair(v.text.toString())
                hideSoftKeyboard()
                return@OnEditorActionListener true
            }
            false
        })
        binding?.coinEdit?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                searchPair(binding?.coinEdit?.text.toString())
            }

            override fun afterTextChanged(s: Editable) {}
        })
        binding?.btnCancel?.setOnClickListener(this)
        adapter = PairSearchAdapter(this, null)
        adapter?.setOnSearchHandleListener(this)
        binding?.listView?.adapter = adapter
        val emptyViewBinding: ListViewEmptyLongBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.list_view_empty_long, null, false)
        val group = binding?.listView?.parent as ViewGroup?
        group?.addView(emptyViewBinding.root)
        binding?.listView?.emptyView = emptyViewBinding.root
        handlerThread = HandlerThread(ConstData.SOCKET_HANDLER, Process.THREAD_PRIORITY_BACKGROUND)
        handlerThread?.start()
        socketHandler = Handler(handlerThread?.looper)
        pairSearchHistory
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getActionBarLayoutId(): Int {
        return 0
    }

    override fun initActionBarView(view: View) {}

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_cancel -> finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (socketHandler != null) {
            socketHandler?.removeMessages(0)
            socketHandler = null
        }
        if (handlerThread != null) {
            handlerThread?.quit()
        }
    }

    override fun onDelete() {
        CookieUtil.clearPairSearchHistory(this)
        pairSearchHistory
    }

    override fun onCollect(pairSearch: PairSearch) {
        val callback: CallbackObject<Boolean> = object : CallbackObject<Boolean>() {
            override fun callback(returnData: Boolean) {
                if (returnData) {
                    val showMsg = if (pairSearch.is_dear) getString(R.string.pair_collect_cancel_ok) else getString(R.string.pair_collect_add_ok)
                    FryingUtil.showToast(mContext, showMsg)
                    pairSearch.is_dear = !pairSearch.is_dear
                    runOnUiThread { adapter?.notifyDataSetChanged() }
                }
            }
        }
        if (pairSearch.is_dear) {
            DearPairService.removeDearPair(this, socketHandler, pairSearch.pair, callback)
        } else {
            DearPairService.insertDearPair(this, socketHandler, pairSearch.pair, callback)
        }
    }

    override fun onPairClick(pairSearch: PairSearch) {
        CookieUtil.addPairSearchHistory(mContext, pairSearch.pair)
        CookieUtil.setCurrentPair(this, pairSearch.pair)
        sendSocketCommandChangedBroadcast(SocketUtil.COMMAND_PAIR_CHANGED)
        BlackRouter.getInstance().build(RouterConstData.QUOTATION_DETAIL).go(this)
    }

    private val pairSearchHistory: Unit
        get() {
            if (DearPairService.dearPairMap.isNotEmpty()) {
                dearPairs.clear()
                for (pair in DearPairService.dearPairMap.keys) {
                    val isDear = DearPairService.dearPairMap[pair]
                    if (isDear != null && isDear) {
                        dearPairs.add(pair)
                    }
                }
                refreshDearPairs()
            } else {
                DearPairService.getDearPairList(mContext, socketHandler, object : Callback<ArrayList<String?>?>() {
                    override fun error(type: Int, error: Any) {
                        dearPairs.clear()
                        refreshDearPairs()
                    }

                    override fun callback(returnData: ArrayList<String?>?) {
                        dearPairs.clear()
                        if (returnData != null && returnData.isNotEmpty()) {
                            dearPairs.addAll(returnData)
                        }
                        refreshDearPairs()
                    }
                })
            }
        }

    private fun refreshDearPairs() {
        var searchHistory = CookieUtil.getPairSearchHistory(mContext)
        searchHistory = searchHistory ?: ArrayList()
        val pairSearches = ArrayList<PairSearch?>()
        for (i in searchHistory.indices) {
            val pairSearch = PairSearch()
            pairSearch.pair = searchHistory[i]
            pairSearch.is_dear = dearPairs.contains(pairSearch.pair)
            pairSearches.add(pairSearch)
        }
        refreshSearchPairHistory(pairSearches)
    }

    private fun refreshSearchPairHistory(pairSearches: ArrayList<PairSearch?>) {
        if (pairSearches.isNotEmpty()) {
            val first = PairSearch()
            first.type = PairSearch.TITLE
            pairSearches.add(0, first)
            val last = PairSearch()
            last.type = PairSearch.DELETE
            pairSearches.add(last)
        }
        adapter?.data = pairSearches
        adapter?.notifyDataSetChanged()
    }

    private fun refreshSearchPairs(pairSearches: ArrayList<PairSearch?>?) {
        runOnUiThread {
            adapter?.data = pairSearches
            adapter?.notifyDataSetChanged()
        }
    }

    fun searchPair(key: String?) {
        socketHandler?.post {
            val pairStatuses = SocketDataContainer.getPairStatusListByKey(mContext, key)
            //                ArrayList<PairStatus> pairStatuses = SocketUtil.getPairStatusListByKey(mContext, key);
            if (pairStatuses != null && pairStatuses.isNotEmpty()) {
                val pairSearches = ArrayList<PairSearch?>()
                for (i in pairStatuses.indices) {
                    val pairStatus = pairStatuses[i]
                    pairStatus?.let {
                        val pairSearch = PairSearch()
                        pairSearch.pair = pairStatus.pair
                        pairSearch.is_dear = dearPairs.contains(pairSearch.pair)
                        pairSearches.add(pairSearch)
                    }
                }
                refreshSearchPairs(pairSearches)
            } else {
                refreshSearchPairs(null)
            }
        }
    }

}