package com.black.frying.fragment

import android.graphics.drawable.ColorDrawable
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import com.black.base.api.FutureApiServiceHelper
import com.black.base.fragment.BaseFragment
import com.black.base.model.ContractRecordTabBean
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.future.*
import com.black.base.model.socket.PairStatus
import com.black.base.util.*
import com.black.base.widget.AutoHeightViewPager
import com.black.frying.adapter.ContractPositionTabAdapter
import com.black.frying.viewmodel.ContractPositionViewModel
import com.black.util.Callback
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FragmentHomePageContractDetailBinding
import io.reactivex.Observer
import skin.support.content.res.SkinCompatResources
import kotlin.collections.ArrayList

/**
 * @author 合约持仓列表页
 */
class ContractPositionTabFragment : BaseFragment(),
    AdapterView.OnItemClickListener,
    View.OnClickListener,
    ContractPositionViewModel.OnContractPositionModelListener {
    private var type: ContractRecordTabBean? = null

    private var binding: FragmentHomePageContractDetailBinding? = null
    private var mViewPager: AutoHeightViewPager? = null

    private var viewModel: ContractPositionViewModel? = null
    private var adapter: ContractPositionTabAdapter? = null
    private var dataList: ArrayList<PositionBean?>? = ArrayList()

    //异步获取数据
    private var handlerThread: HandlerThread? = null
    private var socketHandler: Handler? = null

    private var pairObserver: Observer<ArrayList<PairStatus?>?>? = null
    private var gettingPairsData: Boolean? = false
    private var onTabModelListener: OnTabModelListener? = null

    /**
     * adapter height when viewpager in scrollview
     */
    fun setAutoHeightViewPager(viewPager: AutoHeightViewPager?) {
        mViewPager = viewPager
    }

    fun setOnTabModeListener(tabListener: OnTabModelListener) {
        onTabModelListener = tabListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (binding != null) {
            return binding?.root
        }
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_home_page_contract_detail,
            container,
            false
        )
        viewModel = ContractPositionViewModel(mContext!!, this)
        binding?.allDone?.setOnClickListener(this)
        binding?.entrustType?.visibility = View.GONE
        val drawable = ColorDrawable()
        drawable.color = SkinCompatResources.getColor(activity, R.color.L1)
        drawable.alpha = (0xff * 0.3).toInt()
        val emptyView = inflater.inflate(R.layout.list_view_empty, null)
        val group = binding?.listView?.parent as ViewGroup
        group.addView(emptyView)
        binding?.listView?.emptyView = emptyView

        adapter = ContractPositionTabAdapter(mContext!!, dataList)
        binding?.listView?.adapter = adapter
        binding?.listView?.onItemClickListener = this
        if (arguments != null) {
            val position = arguments!!.getInt(AutoHeightViewPager.POSITION)
            binding?.root?.let { mViewPager?.setViewPosition(it, position) }
        }
        return binding?.root
    }


    override fun onResume() {
        super.onResume()
        handlerThread = HandlerThread(ConstData.SOCKET_HANDLER, Process.THREAD_PRIORITY_BACKGROUND)
        handlerThread?.start()
        socketHandler = Handler(handlerThread?.looper)
        viewModel?.getMarketPrice(CookieUtil.getCurrentFutureUPair(context!!))
        viewModel?.getFundRate(CookieUtil.getCurrentFutureUPair(context!!))
        if (LoginUtil.isFutureLogin(context)) {
            viewModel?.getPositionAdlList(context)
            viewModel?.getLeverageBracketDetail()
            var all:Boolean? = SharedPreferenceUtils.getData(Constants.POSITION_ALLL_CHECKED,false) as Boolean
            viewModel?.getPositionData(all)
        }

    }

    override fun onStop() {
        super.onStop()
        if (pairObserver != null) {
            SocketDataContainer.removePairObservable(pairObserver)
            pairObserver == null
        }
        if (socketHandler != null) {
            socketHandler?.removeMessages(0)
            socketHandler = null
        }
        if (handlerThread != null) {
            handlerThread?.quit()
        }
    }

    override fun doResetSkinResources() {
        val drawable = ColorDrawable()
        drawable.color = SkinCompatResources.getColor(activity, R.color.L1_ALPHA30)
        binding?.listView?.divider = drawable
        binding?.listView?.dividerHeight = 1
//        adapter?.resetSkinResources()
//        adapter?.notifyDataSetChanged()
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        activity?.let {
        }
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.all_done -> {
                if (dataList?.size == 0) {
                    return
                }
                FutureApiServiceHelper.closeAll(
                    activity,
                    true,
                    object : Callback<HttpRequestResultBean<String>?>() {
                        override fun callback(returnData: HttpRequestResultBean<String>?) {
                            if (returnData != null) {
                                var all:Boolean? = SharedPreferenceUtils.getData(Constants.POSITION_ALLL_CHECKED,false) as Boolean
                                viewModel?.getPositionData(all)
                            }
                        }

                        override fun error(type: Int, error: Any?) {
                            FryingUtil.showToast(activity, error.toString())
                        }
                    })
            }
        }
    }

    override fun onGetPositionData(positionList: ArrayList<PositionBean?>?) {
        onTabModelListener?.onCount(positionList?.size)
        adapter?.data = positionList
        adapter?.notifyDataSetChanged()
    }

    override fun onFundingRate(fundRate: FundingRateBean?) {

    }

    override fun onLeverageDetail(leverageBracket: LeverageBracketBean?) {

    }

    override fun onMarketPrice(marketPrice: MarkPriceBean?) {

    }


    interface OnTabModelListener {
        fun onCount(count: Int?)
    }

    companion object {
        fun newInstance(type: ContractRecordTabBean?): ContractPositionTabFragment {
            val args = Bundle()
            val fragment = ContractPositionTabFragment()
            fragment.arguments = args
            fragment.type = type
            return fragment
        }
    }
}