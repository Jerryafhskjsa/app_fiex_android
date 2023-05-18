package com.black.frying.contract

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.black.base.api.FutureApiServiceHelper
import com.black.base.model.ContractRecordTabBean
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.future.Constants
import com.black.base.util.FryingUtil
import com.black.base.util.SharedPreferenceUtils
import com.black.frying.adapter.ContractPositionTabAdapter
import com.black.frying.contract.viewmodel.FuturesPositionTabVm
import com.black.util.Callback
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FragmentHomePageFutureDetailBinding

/**
 * @author 合约持仓列表页
 */
class FuturePositionTabFragment : FutureBaseFragment<FragmentHomePageFutureDetailBinding>() {
    private var type: ContractRecordTabBean? = null
    private val mAdapter: ContractPositionTabAdapter by lazy {
        ContractPositionTabAdapter(
            context!!,
            getDefaultViewModel<FuturesPositionTabVm>().positionListLD.value!!
        )
    }

    override fun startInit(contentViewBinding: FragmentHomePageFutureDetailBinding) {
        setupView()
        setupBindData()
    }

    private fun setupView() {
        val viewModel = getDefaultViewModel<FuturesPositionTabVm>()
        viewModel.init(this, globalVm)

        val checkALL = SharedPreferenceUtils.getData(Constants.PLAN_ALL_CHECKED, true) as Boolean
        viewModel.getPositionData(checkALL)

        contentViewBinding.apply {
            allDone.setOnClickListener {
                if (viewModel.positionListLD.value.isNullOrEmpty()) {
                    return@setOnClickListener
                }
                setupAllDoneClick(viewModel)
            }

            scbShowAll.isChecked = checkALL
            scbShowAll.setOnCheckedChangeListener { _, isChecked ->
                SharedPreferenceUtils.putData(Constants.PLAN_ALL_CHECKED, isChecked)
                viewModel.getPositionData(isChecked)
            }

            val temptyView = View.inflate(context, R.layout.list_view_empty, null)
            val group =listView.parent as ViewGroup
            group.addView(temptyView)
            listView.apply {
                emptyView = temptyView
                adapter = mAdapter
                onItemClickListener =
                    AdapterView.OnItemClickListener { parent, view, position, id -> }
            }
        }
    }

    private fun setupAllDoneClick(viewModel: FuturesPositionTabVm) {
        FutureApiServiceHelper.closeAll(
            activity,
            true,
            object : Callback<HttpRequestResultBean<String>?>() {
                override fun callback(returnData: HttpRequestResultBean<String>?) {
                    if (returnData != null) {
                        var all: Boolean? = SharedPreferenceUtils.getData(
                            Constants.POSITION_ALLL_CHECKED,
                            true
                        ) as Boolean
                        viewModel?.getPositionData(all)
                    }
                }

                override fun error(type: Int, error: Any?) {
                    FryingUtil.showToast(activity, error.toString())
                }
            })
    }


    private fun setupBindData() {
        val viewModel = getDefaultViewModel<FuturesPositionTabVm>()
        viewModel.positionListLD.observe(viewLifecycleOwner) {
            mAdapter.apply {
                data?.clear()
                data?.addAll(it)
                notifyDataSetChanged()
            }
        }
    }

    companion object {
        fun newInstance(type: ContractRecordTabBean?): FuturePositionTabFragment {
            val args = Bundle()
            val fragment = FuturePositionTabFragment()
            fragment.arguments = args
            fragment.type = type
            return fragment
        }
    }
}