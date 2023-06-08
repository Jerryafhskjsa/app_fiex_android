package com.black.frying.activity

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.databinding.ListViewEmptyLongBinding
import com.black.base.model.SuccessObserver
import com.black.base.model.socket.PairStatus
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
import io.reactivex.Observable
import io.reactivex.Observer
import skin.support.app.SkinCompatDelegate
import java.util.*

@Route(value = [RouterConstData.DEAR_PAIR_SEARCH])
class DearPairSearchActivity : BaseActionBarActivity(), View.OnClickListener, OnSearchHandleListener {
    private var adapter: PairSearchAdapter? = null
    private var binding: ActivityDearPairSearchBinding? = null
    //异步获取数据
    private var handlerThread: HandlerThread? = null
    private var type: Int = 0
    private var socketHandler: Handler? = null
    private var pairObserver: Observer<ArrayList<PairStatus?>?>? = createPairObserver()
    private val dearPairs = ArrayList<String?>()
    private var pairSearch: ArrayList<PairStatus?>? = null
    private var futureTickerObserver: Observer<ArrayList<PairStatus?>?>? = null
    private var pairSearch1 = PairSearch()
    private var pairSearch2 = PairSearch()
    private var pairSearch3 = PairSearch()
    private var pairSearch4 = PairSearch()
    private var pairSearch5 = PairSearch()

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
                if(!s.isEmpty()){
                    binding?.history?.visibility = View.GONE
                    binding?.tab?.visibility = View.VISIBLE
                }
                searchPair(s.toString())
            }

            override fun afterTextChanged(s: Editable) {}
        })
        binding?.btnCancel?.setOnClickListener(this)
        binding?.one?.setOnClickListener(this)
        binding?.two?.setOnClickListener(this)
        binding?.three?.setOnClickListener(this)
        binding?.four?.setOnClickListener(this)
        binding?.five?.setOnClickListener(this)
        binding?.spot?.setOnClickListener(this)
        binding?.futures?.setOnClickListener(this)
        binding?.btnAction?.setOnClickListener(this)
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
    }

    override fun onResume() {
        super.onResume()
        pairSearchHistory
        searchPair("")
        if (pairObserver == null) {
            pairObserver = createPairObserver()
        }
        SocketDataContainer.subscribePairObservable(pairObserver)
        if (futureTickerObserver == null) {
            futureTickerObserver = createFutureTickerObserver()
        }
        SocketDataContainer.subscribeFuturePairObservable(futureTickerObserver)
        handlerThread = HandlerThread(ConstData.SOCKET_HANDLER, Process.THREAD_PRIORITY_BACKGROUND)
        handlerThread?.start()
        socketHandler = Handler(handlerThread?.looper)
    }

    override fun onStop() {
        super.onStop()
        if (pairObserver != null) {
            SocketDataContainer.removePairObservable(pairObserver)
        }
        if (futureTickerObserver != null) {
            SocketDataContainer.removeFuturePairObservable(futureTickerObserver)
            futureTickerObserver == null
        }
        if (socketHandler != null) {
            socketHandler?.removeMessages(0)
            socketHandler = null
        }
        if (handlerThread != null) {
            handlerThread?.quit()
            handlerThread = null
        }
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

            R.id.one ->{
                binding?.coinEdit?.setText(pairSearch1.pair!!.split("_")[0].toString())
                searchPair(pairSearch1.pair!!.split("_")[0].toString())
            }

            R.id.two ->{  binding?.coinEdit?.setText(pairSearch2.pair!!.split("_")[0].toString())
                searchPair(pairSearch1.pair!!.split("_")[0].toString())}

            R.id.three ->{  binding?.coinEdit?.setText(pairSearch3.pair!!.split("_")[0].toString())
                searchPair(pairSearch1.pair!!.split("_")[0].toString())}

            R.id.four ->{  binding?.coinEdit?.setText(pairSearch4.pair!!.split("_")[0].toString())
                searchPair(pairSearch1.pair!!.split("_")[0].toString())}

            R.id.five ->{  binding?.coinEdit?.setText(pairSearch5.pair!!.split("_")[0].toString())
                searchPair(pairSearch1.pair!!.split("_")[0].toString())}

            R.id.btn_action -> {
            CookieUtil.clearPairSearchHistory(this)
                binding?.history?.visibility = View.GONE
                binding?.bar?.visibility = View.VISIBLE
                binding?.listView?.visibility = View.VISIBLE
                binding?.tab?.visibility = View.VISIBLE
                val key = binding?.coinEdit?.text.toString()
                searchPair(key)
            }

            R.id.spot-> {
                type = 0
                binding?.spot?.isChecked = true
                binding?.futures?.isChecked = false
                binding?.spot?.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(
                        R.dimen.text_size_18).toFloat())
                binding?.futures?.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(
                        R.dimen.text_size_12).toFloat())
                val key = binding?.coinEdit?.text.toString()
                searchPair(key)

            }

            R.id.futures-> {
                type = 1
                binding?.spot?.isChecked = false
                binding?.futures?.isChecked = true
                binding?.spot?.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(
                        R.dimen.text_size_12).toFloat())
                binding?.futures?.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(
                        R.dimen.text_size_18).toFloat())
                val key = binding?.coinEdit?.text.toString()
                searchPair(key)
            }
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
        val bundle = Bundle()
        bundle.putString(ConstData.PAIR, pairSearch.pair)
        if (type == 0) bundle.putInt(ConstData.NAME,0) else bundle.putInt(ConstData.NAME,1)
        BlackRouter.getInstance().build(RouterConstData.QUOTATION_DETAIL).with(bundle).go(this)
    }

    private fun createPairObserver(): Observer<ArrayList<PairStatus?>?> {
        return object : SuccessObserver<ArrayList<PairStatus?>?>() {
            override fun onSuccess(value: ArrayList<PairStatus?>?) {
                if (value?.size!! > 0) {
                    pairSearch = value
                }
            }
        }
    }
    private fun createFutureTickerObserver(): Observer<ArrayList<PairStatus?>?> {
        return object : SuccessObserver<ArrayList<PairStatus?>?>() {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onSuccess(value: ArrayList<PairStatus?>?) {

            }
        }
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
        if (searchHistory.size == 0){
            binding?.history?.visibility = View.GONE
            binding?.tab?.visibility = View.VISIBLE
        }
        else{
            binding?.history?.visibility = View.VISIBLE
            binding?.tab?.visibility = View.GONE
        }
        val pairSearches = ArrayList<PairSearch?>()
        for (i in searchHistory.indices) {
            val pairSearch = PairSearch()
            pairSearch.pair = searchHistory[i]
            pairSearch.is_dear = dearPairs.contains(pairSearch.pair)
            pairSearches.add(pairSearch)
            if (i == searchHistory.size - 1){
                pairSearch1 = pairSearch
                binding?.one?.setText(pairSearch.pair)
                binding?.one?.visibility = View.VISIBLE
            }
            if (i == searchHistory.size - 2){
                pairSearch2 = pairSearch
                binding?.two?.setText(pairSearch.pair)
                binding?.two?.visibility = View.VISIBLE
            }
            if (i == searchHistory.size - 3){
                pairSearch3 = pairSearch
                binding?.three?.setText(pairSearch.pair)
                binding?.three?.visibility = View.VISIBLE
            }
            if (i == searchHistory.size - 4){
                pairSearch4 = pairSearch
                binding?.four?.setText(pairSearch.pair)
                binding?.four?.visibility = View.VISIBLE
            }
            if (i == searchHistory.size - 5){
                pairSearch5 = pairSearch
                binding?.five?.setText(pairSearch.pair)
                binding?.five?.visibility = View.VISIBLE
            }
        }
        //refreshSearchPairHistory(pairSearches)
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

    private fun searchPair(key: String?) {
        socketHandler?.post {
            when (type) {
                0 -> {
                    val pairStatuses = SocketDataContainer.getPairStatusListByKey(mContext, key)
                    //                ArrayList<PairStatus> pairStatuses = SocketUtil.getPairStatusListByKey(mContext, key);
                    if (pairStatuses != null && pairStatuses.isNotEmpty()) {
                        val pairSearches = ArrayList<PairSearch?>()
                        for (i in pairStatuses.indices) {
                            val pairStatus = pairStatuses[i]
                            pairStatus?.let {
                                var pairSearch = PairSearch()
                                pairSearch.pair = pairStatus.pair
                                pairSearch.currentPrice = pairStatus.currentPrice
                                pairSearch.priceChangeSinceToday = pairStatus.priceChangeSinceToday
                                pairSearch.currentPrice = pairStatus.currentPrice
                                pairSearch.is_dear = dearPairs.contains(pairSearch.pair)
                                pairSearches.add(pairSearch)
                            }
                        }
                        refreshSearchPairs(pairSearches)
                    } else {
                        refreshSearchPairs(null)
                    }
                }
                1 -> {
                    val pairStatuses = SocketDataContainer.getFPairStatusListByKey(mContext, key)
                    Log.d("oppiopoop", pairStatuses?.size.toString())
                    //                ArrayList<PairStatus> pairStatuses = SocketUtil.getPairStatusListByKey(mContext, key);
                    if (pairStatuses != null && pairStatuses.isNotEmpty()) {
                        val pairSearches = ArrayList<PairSearch?>()
                        for (i in pairStatuses.indices) {
                            val pairStatus = pairStatuses[i]
                            pairStatus?.let {
                                var pairSearch = PairSearch()
                                pairSearch.pair = pairStatus.pair
                                pairSearch.currentPrice = pairStatus.currentPrice
                                pairSearch.priceChangeSinceToday = pairStatus.priceChangeSinceToday
                                pairSearch.currentPrice = pairStatus.currentPrice
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
    }

}