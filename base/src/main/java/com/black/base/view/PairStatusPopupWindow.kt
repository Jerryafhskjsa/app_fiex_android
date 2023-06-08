package com.black.base.view

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.PaintDrawable
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.black.base.R
import com.black.base.adapter.PairStatusAdapter
import com.black.base.model.PairStatusShowPopup
import com.black.base.model.QuotationSet
import com.black.base.model.SuccessObserver
import com.black.base.model.socket.PairStatus
import com.black.base.service.DearPairService.getDearPairList
import com.black.base.util.ConstData
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil.printError
import com.black.base.util.ImageLoader
import com.black.base.util.RouterConstData
import com.black.base.util.SocketDataContainer
import com.black.base.util.StatusBarUtil.fitPopupWindowOverStatusBar
import com.black.base.util.StatusBarUtil.getStatusBarHeight
import com.black.router.BlackRouter
import com.black.util.Callback
import com.black.util.CommonUtil
import com.google.android.material.tabs.TabLayout
import io.reactivex.Observer
import skin.support.content.res.SkinCompatResources
import java.util.*
import kotlin.collections.ArrayList

//弹出交易对状态列表
final class PairStatusPopupWindow(
    private val mActivity: Activity,
    type: Int,
    oldSets: List<QuotationSet?>?
) : View.OnClickListener, AdapterView.OnItemClickListener, PopupWindow.OnDismissListener {
    companion object {
        const val TYPE_TRANSACTION = 0 //现货交易
        const val TYPE_ENTRUST = 1 //委托
        const val TYPE_K_LINE_FULL = 2 //全屏K线
        const val TYPE_FUTURE_U = 3//u本位合约
        const val TYPE_FUTURE_COIN = 4//币本位合约
        const val TYPE_FUTURE_ALL = 5//全部合约币种
        private var pairStatusPopupWindow: PairStatusPopupWindow? = null
        fun getInstance(
            activity: Activity,
            type: Int,
            sets: List<QuotationSet?>?
        ): PairStatusPopupWindow {
            return PairStatusPopupWindow(activity, type, sets)
        }

        fun reset() {
            pairStatusPopupWindow = null
        }
    }

    private var imm: InputMethodManager
    private val myPairSet: String = mActivity.getString(R.string.pair_collect)
    private val imageLoader: ImageLoader = ImageLoader(mActivity)
    private val inputEdit: EditText
    private var searchKey: String? = null
    private val sets: MutableList<QuotationSet?> = oldSets?.let { ArrayList(it) } ?: ArrayList()
    private val pair: List<String>? = null
    private val popupWindow: PopupWindow
    protected var nullAmount: String
    private val type: Int
    private val setTab: TabLayout
    private val viewPager: ViewPager
    private var listViewMap: MutableMap<String, ListView> = HashMap()
    private val adapterMap: MutableMap<String, PairStatusAdapter> = HashMap()
    private val listViewDataMap: MutableMap<String, MutableList<PairStatusShowPopup?>> = HashMap()
    private val allPairStatusShowMap: MutableMap<String, PairStatusShowPopup> = HashMap()

    //    private final List<PairStatusShowPopup> allPairStatusShowList = new ArrayList<>();
    private val dearPairs = ArrayList<String?>()
    private val dearPairsShowMap: MutableMap<String, PairStatusShowPopup> = HashMap()
    private var defaultIndex = 1
    private var pairInitialled = false


    //异步获取数据
    private var handlerThread: HandlerThread? = null
    private var socketHandler: Handler? = null
    private var onPairStatusSelectListener: OnPairStatusSelectListener? = null
    private var pairObserver: Observer<ArrayList<PairStatus?>?>? = createPairObserver()
    private var futureTickerObserver: Observer<ArrayList<PairStatus?>?>? =
        createFutureTickerObserver()


    init {
        var myPairSetQuot = QuotationSet()
        myPairSetQuot.name = myPairSet
        myPairSetQuot.coinType = myPairSet
        sets.add(0, myPairSetQuot)
        this.type = type
        imm = mActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        nullAmount = mActivity.getString(R.string.number_default)
        val dm = mActivity.resources.displayMetrics
        val width =
            if (type == TYPE_K_LINE_FULL) (400 * dm.density).toInt() else (dm.widthPixels * 0.7333).toInt()
        val inflater = LayoutInflater.from(mActivity)
        val contentView = inflater.inflate(R.layout.view_pair_status_popup_window, null)
        if (type != TYPE_K_LINE_FULL) {
            contentView.findViewById<View>(R.id.header_layout)
                .setPadding(0, getStatusBarHeight(mActivity), 0, 0)
        }
        val height: Int = mActivity.resources.displayMetrics.heightPixels
        popupWindow = PopupWindow(contentView, width, height + getStatusBarHeight(mActivity) + getStatusBarHeight(mActivity) + getStatusBarHeight(mActivity))
        popupWindow.isClippingEnabled = false
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(PaintDrawable())
        popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        popupWindow.animationStyle = R.style.anim_pairs_status_popup_window
        popupWindow.setOnDismissListener(this)
        fitPopupWindowOverStatusBar(popupWindow, true)
        val titleView = contentView.findViewById<TextView>(R.id.title)
        val btnTotal = contentView.findViewById<TextView>(R.id.total)
        val dataType = type
        when (type) {
            TYPE_K_LINE_FULL -> {
                titleView.setText(R.string.k_line)
                btnTotal.visibility = View.GONE
            }
            TYPE_ENTRUST -> {
                titleView.setText(R.string.bill_manager)
                btnTotal.visibility = View.VISIBLE
                btnTotal.setOnClickListener(this)
            }
            TYPE_FUTURE_U, TYPE_FUTURE_COIN, TYPE_FUTURE_ALL -> {
                titleView.setText(R.string.futures)
                btnTotal.visibility = View.GONE
                btnTotal.setOnClickListener(this)
            }
            else -> {
                if (dataType == PairStatus.LEVER_DATA) {
                    titleView.text = "杠杆"
                } else {
                    titleView.setText(R.string.spot)
                }
                btnTotal.visibility = View.GONE
            }
        }
        setTab = contentView.findViewById(R.id.set_tab)
        setTab.setTabTextColors(
            SkinCompatResources.getColor(mActivity, R.color.C5),
            SkinCompatResources.getColor(mActivity, R.color.C1)
        )
        //setTab.setSelectedTabIndicatorHeight(3)
        setTab.tabMode = TabLayout.MODE_SCROLLABLE
        inputEdit = contentView.findViewById(R.id.input)
        inputEdit.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND || event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                search(v.text.toString())
                hideSoftKeyboard(v)
                return@OnEditorActionListener true
            }
            false
        })
        inputEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                search(s.toString())
            }

            override fun afterTextChanged(s: Editable) {}
        })
        viewPager = contentView.findViewById(R.id.view_pager)
        val setSize = sets.size
        listViewMap = HashMap(setSize)
        val viewPagerViews: MutableList<View> = ArrayList()
        for (i in 0 until setSize) {
            val set = sets[i]
            try {
                val view = inflater.inflate(R.layout.list_view_add_empty, null) as FrameLayout
                val listView = view.findViewById<ListView>(R.id.list_view)
                val adapter = PairStatusAdapter(mActivity, null, dataType)
                listView.adapter = adapter
                val layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.gravity = Gravity.TOP
                if (set?.name.equals(myPairSet, ignoreCase = true)) {
                    val emptyView = inflater.inflate(R.layout.list_view_empty_pair, null)
                    emptyView.findViewById<View>(R.id.btn_action).setOnClickListener(this)
                    val group = listView.parent as ViewGroup
                    group.addView(emptyView, layoutParams)
                    listView.emptyView = emptyView
                } else {
                    val emptyView = inflater.inflate(R.layout.list_view_empty_long, null)
                    val group = listView.parent as ViewGroup
                    group.addView(emptyView, layoutParams)
                    listView.emptyView = emptyView
                }
                val drawable = ColorDrawable()
                drawable.color = SkinCompatResources.getColor(mActivity, R.color.L1)
                drawable.alpha = (0.3 * 255).toInt()
                listView.divider = drawable
                listView.dividerHeight = 0
                listView.onItemClickListener = this
                var setName: String? = null
                setName = if (type == TYPE_TRANSACTION) {
                    set?.name as String
                } else if (type == TYPE_FUTURE_ALL || type == TYPE_FUTURE_U || type == TYPE_FUTURE_COIN) {
                    set?.coinType?.lowercase() as String
                } else {
                    set?.name as String
                }
                adapterMap[setName] = adapter
                listViewMap[setName] = listView
                listViewDataMap[setName] = ArrayList()
                viewPagerViews.add(view)
            } catch (throwable: Throwable) {
                printError(throwable)
            }
        }
        viewPager.adapter = ViewPagerAdapter(viewPagerViews)
        setTab.setupWithViewPager(viewPager, true)
        for (i in 0 until setTab.tabCount) {
            val set = sets[i]
            try {
                val tab = setTab.getTabAt(i)
                if (tab != null) {
                    tab.setCustomView(R.layout.view_pair_status_choose_tab)
                    if (tab.customView != null) {
                        val textView =
                            tab.customView?.findViewById<View>(android.R.id.text1) as TextView
                        textView.text = set?.name
                    }
                }
            } catch (throwable: Throwable) {
                printError(throwable)
            }
        }
        setTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            var textSize15 =
                mActivity.resources.getDimensionPixelSize(R.dimen.text_size_12).toFloat()
            var textSize14 =
                mActivity.resources.getDimensionPixelSize(R.dimen.text_size_12).toFloat()

            override fun onTabSelected(tab: TabLayout.Tab) {
                val view = tab.customView
                val textView =
                    if (view == null) null else view.findViewById<View>(android.R.id.text1) as TextView
                if (textView != null) {
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize15)
                    textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD), Typeface.BOLD)
                    textView.postInvalidate()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val view = tab.customView
                val textView =
                    if (view == null) null else view.findViewById<View>(android.R.id.text1) as TextView
                if (textView != null) {
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize14)
                    textView.setTypeface(
                        Typeface.defaultFromStyle(Typeface.NORMAL),
                        Typeface.NORMAL
                    )
                    textView.postInvalidate()
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        val currentPair = CookieUtil.getCurrentPair(mActivity)
        if (currentPair != null) {
            for (i in sets.indices) {
                if (currentPair.contains("_" + sets[i])) {
                    defaultIndex = i
                    break
                }
            }
        }
        viewPager.currentItem = defaultIndex
        setTab.getTabAt(defaultIndex)?.select()
    }

    private fun createPairObserver(): Observer<ArrayList<PairStatus?>?> {
        return object : SuccessObserver<ArrayList<PairStatus?>?>() {
            override fun onSuccess(value: ArrayList<PairStatus?>?) {
                value?.let { refreshPairsStatus(it) }
            }
        }
    }

    private fun createFutureTickerObserver(): Observer<ArrayList<PairStatus?>?> {
        return object : SuccessObserver<ArrayList<PairStatus?>?>() {
            override fun onSuccess(value: ArrayList<PairStatus?>?) {
                value?.let { refreshPairsStatus(it) }
            }
        }
    }


    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.btn_action) {
            BlackRouter.getInstance().build(RouterConstData.DEAR_PAIR_SEARCH)
                .go(mActivity) { routeResult, error -> popupWindow.dismiss() }
        } else if (i == R.id.total) {
            popupWindow.dismiss()
            if (onPairStatusSelectListener != null) {
                onPairStatusSelectListener?.onPairStatusSelected(null)
            }
        }
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        //交易对状态点击选择并回调
        val pairStatus = parent.adapter.getItem(position) as PairStatus
        popupWindow.dismiss()
        if (onPairStatusSelectListener != null) {
            onPairStatusSelectListener?.onPairStatusSelected(pairStatus)
        }
    }

    protected fun hideSoftKeyboard(view: View) {
        imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    fun show(onPairStatusSelectListener: OnPairStatusSelectListener?) {
        this.onPairStatusSelectListener = onPairStatusSelectListener
        popupWindow.showAtLocation(mActivity.window.decorView, Gravity.LEFT, 0, 0)
        val lp = mActivity.window.attributes
        lp.alpha = 0.6f
        mActivity.window.attributes = lp
        handlerThread = HandlerThread(ConstData.SOCKET_HANDLER, Process.THREAD_PRIORITY_BACKGROUND)
        handlerThread?.start()
        socketHandler = Handler(handlerThread?.looper)
        initAllPairStatus()
        getDearPairList(mActivity, socketHandler, object : Callback<ArrayList<String?>?>() {
            override fun error(type: Int, error: Any) {}
            override fun callback(returnData: ArrayList<String?>?) {
                if (returnData != null && returnData.isNotEmpty()) {
                    dearPairs.addAll(returnData)
                    initDearPairsShow()
                }
            }
        })
        if (type == TYPE_TRANSACTION) {
            if (pairObserver == null) {
                pairObserver = createPairObserver()
            }
        }
        SocketDataContainer.subscribePairObservable(pairObserver)
        if (type == TYPE_FUTURE_ALL) {
            if (futureTickerObserver == null) {
                futureTickerObserver = createFutureTickerObserver()
            }
            SocketDataContainer.subscribeFuturePairObservable(futureTickerObserver)
        }
    }

    private fun initDearPairsShow() {
        if (dearPairs.isNotEmpty()) {
            val myPairs = listViewDataMap[myPairSet]
            for (pair in dearPairs) {
                Log.d("uirywuieyui", pair.toString())
                pair?.let {
                    val pairStatusShowPopup = allPairStatusShowMap[pair]
                    if (pairStatusShowPopup != null) {
                        val pairShow = PairStatusShowPopup(mActivity, pairStatusShowPopup)
                        myPairs?.add(pairShow)
                        dearPairsShowMap[pair] = pairShow
                    }
                }
            }
            Log.d("uirywuieyui", "12 " + myPairs?.size.toString())
            val adapter = adapterMap[myPairSet]
            if (adapter != null) {
                adapter.data = listViewDataMap[myPairSet]
                Log.d("uirywuieyui", "11 " + listViewDataMap[myPairSet]?.size.toString())
                adapter.sortData(PairStatusShowPopup.COMPARATOR)
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onDismiss() {
        val lp = mActivity.window.attributes
        lp.alpha = 1f
        mActivity.window.attributes = lp
        if (socketHandler != null) {
            socketHandler?.removeMessages(0)
            socketHandler = null
        }
        if (handlerThread != null) {
            handlerThread?.quit()
            handlerThread = null
        }
        if (type == TYPE_TRANSACTION) {
            if (pairObserver != null) {
                SocketDataContainer.removePairObservable(pairObserver)
            }
        }

        if (type == TYPE_FUTURE_ALL) {
            if (futureTickerObserver != null) {
                SocketDataContainer.removeFuturePairObservable(futureTickerObserver)
            }
        }
    }

    private fun initAllPairStatus() {
        CommonUtil.postHandleTask(socketHandler) {
            val callback: Callback<ArrayList<PairStatus?>?> =
                object : Callback<ArrayList<PairStatus?>?>() {
                    override fun error(type: Int, error: Any) {
                        if (popupWindow.isShowing) {
                            initAllPairStatus()
                        }
                    }

                    override fun callback(returnData: ArrayList<PairStatus?>?) {
                        if (returnData == null) {
                            return
                        }
                        synchronized(allPairStatusShowMap) {
                            synchronized(listViewDataMap) {
                                for (setName in listViewDataMap.keys) {
                                    val tempList = listViewDataMap[setName]
                                    tempList?.clear()
                                }
                                allPairStatusShowMap.clear()
                                for (i in returnData.indices) {
                                    val pairStatusShow =
                                        PairStatusShowPopup(mActivity, returnData[i]!!)
                                    pairStatusShow.pair?.let {
                                        allPairStatusShowMap[it] = pairStatusShow
                                        val setName = pairStatusShow.setName
                                        if (!TextUtils.isEmpty(setName)) {
                                            var tempList = listViewDataMap[setName!!]
                                            if (tempList == null) {
                                                tempList = ArrayList()
                                                listViewDataMap[setName] = tempList
                                            }
                                            tempList.add(pairStatusShow)
                                        }
                                    }
                                }
                            }
                            for (setName in listViewDataMap.keys) {
                                val tempList: List<PairStatusShowPopup?>? = listViewDataMap[setName]
                                if (tempList != null) {
                                    Collections.sort(tempList, PairStatusShowPopup.COMPARATOR)
                                }
                            }
                            //所有显示的
                            mActivity.runOnUiThread {
                                //将分组对应到响应的listView
                                for (setName in listViewDataMap.keys) {
                                    if (setName.equals(myPairSet, ignoreCase = true)) {
                                        if (dearPairs.isNotEmpty()) {
                                            //initDearPairsShow()
                                        }
                                    } else {
                                        val adapter = adapterMap[setName]
                                        if (adapter != null) {
                                            adapter.data = listViewDataMap[setName]
                                            adapter.sortData(PairStatusShowPopup.COMPARATOR)
                                            adapter.notifyDataSetChanged()
                                        }
                                    }
                                }
                                //获取所有交易对数据信息，填充并显示
                                pairInitialled = true
                            }
                        }
                    }
                }
            when (type) {
                TYPE_FUTURE_U -> {
                    SocketDataContainer.getFuturesPairsWithSet(
                        mActivity,
                        ConstData.PairStatusType.FUTURE_U,
                        callback
                    )
                }
                TYPE_FUTURE_COIN -> {
                    SocketDataContainer.getFuturesPairsWithSet(
                        mActivity,
                        ConstData.PairStatusType.FUTURE_COIN,
                        callback
                    )
                }
                TYPE_FUTURE_ALL -> {
                    SocketDataContainer.getFuturesPairsWithSet(
                        mActivity,
                        ConstData.PairStatusType.FUTURE_ALL,
                        callback
                    )
                }
                TYPE_TRANSACTION -> SocketDataContainer.getAllPairStatus(mActivity, callback)
            }
        }
    }

    private fun search(searchKey: String?) {
        this.searchKey = searchKey
        synchronized(adapterMap) {
            synchronized(listViewDataMap) {
                for (i in sets.indices) {
                    var setName: String? = null
                    when (type) {
                        TYPE_FUTURE_ALL -> {
                            when (sets[i]?.name) {
                                mActivity.getString(R.string.usdt_base) -> {
                                    setName = mActivity.getString(R.string.usdt).lowercase()
                                }
                                mActivity.getString(R.string.coin_base) -> {
                                    setName = mActivity.getString(R.string.usd)
                                }
                            }
                        }
                        TYPE_TRANSACTION -> {
                            setName = sets[i]?.name
                        }
                    }
//                    val setName = sets[i]?.name
                    val adapter = adapterMap[setName]
                    if (adapter != null) {
                        val data: MutableList<PairStatusShowPopup?>? = listViewDataMap[setName]
                        if (searchKey == null || searchKey.trim { it <= ' ' }.isEmpty()) {
                            adapter.data = data
                        } else {
                            val showData: MutableList<PairStatusShowPopup?> = ArrayList()
                            if (data != null) {
                                for (j in data.indices) {
                                    val pair = data[j]
                                    if (pair?.pair != null && pair.pair!!.toUpperCase(Locale.getDefault())
                                            .contains(searchKey.toUpperCase(Locale.getDefault()))
                                    ) {
                                        showData.add(pair)
                                    }
                                }
                            }
                            adapter.data = showData
                        }
                        adapter.sortData(PairStatusShowPopup.COMPARATOR)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    //刷新所有交易对状态
    fun refreshPairsStatus(data: List<PairStatus?>?) {
        Log.d("iiiii", "refreshPairsStatus,data.size = " + data?.size)
        if (data == null || data.isEmpty()) {
            return
        }
        //更新对应交易对显示数据
        synchronized(allPairStatusShowMap) {
            synchronized(listViewDataMap) {
                var hasNewPair = false
                for (pairStatus in data) {
                    if (pairStatus == null) {
                        continue
                    }
                    var myPairs = listViewDataMap[myPairSet]
                    if (myPairs == null) {
                        myPairs = ArrayList()
                        listViewDataMap[myPairSet] = myPairs
                    }
                    pairStatus.pair?.let {
                        var pairStatusShowPopup = allPairStatusShowMap[it]
                        if (pairStatusShowPopup == null) {
                            //有新交易对上线，需要更新所有数据
                            pairStatusShowPopup = PairStatusShowPopup(mActivity, pairStatus)
                            allPairStatusShowMap[it] = pairStatusShowPopup
                            val setName = pairStatusShowPopup.setName
                            if (setName != null) {
                                var tempList = listViewDataMap[setName]
                                if (tempList == null) {
                                    tempList = ArrayList()
                                    listViewDataMap[setName] = tempList
                                }
                                tempList.add(pairStatusShowPopup)
                            }
                            if (pairStatusShowPopup.is_dear) {
                                val dearPairShow = PairStatusShowPopup(mActivity, pairStatus)
                                myPairs.add(dearPairShow)
                                dearPairsShowMap[it] = dearPairShow
                            }
                            hasNewPair = true
                        } else {
                            //已有交易对，直接更新界面
                            pairStatusShowPopup.updateData(pairStatus)
                            val dearPairShow = dearPairsShowMap[it]
                            dearPairShow?.updateData(pairStatus)
                        }
                    }
                }
                for (setName in listViewDataMap.keys) {
                    val tempList: List<PairStatusShowPopup?>? = listViewDataMap[setName]
                    if (tempList != null) {
                        Collections.sort(tempList, PairStatusShowPopup.COMPARATOR)
                    }
                }
                mActivity.runOnUiThread {
                    search(searchKey)
                }
            }
        }
    }

    private inner class ViewPagerAdapter internal constructor(var views: List<View>) :
        PagerAdapter() {
        override fun getCount(): Int {
            return views.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        //展示的view
        override fun instantiateItem(container: ViewGroup, position: Int): Any { //获得展示的view
            val view = views[position]
            //添加到容器
            container.addView(view)
            //返回显示的view
            return view
        }

        //销毁view
        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) { //从容器中移除view
            container.removeView(`object` as View)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return sets[position]?.name
        }

    }

    interface OnPairStatusSelectListener {
        fun onPairStatusSelected(pairStatus: PairStatus?)
    }
}
