package com.black.wallet.activity

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.util.*
import com.black.base.view.ChooseCoinControllerWindow
import com.black.base.view.ChooseWalletControllerWindow
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.wallet.R
import com.black.wallet.adapter.LeverPairAdapter
import com.black.wallet.databinding.ActivityLeverPairChooseBinding
import io.reactivex.Observable

/**
 * 资产划转页面 对应WalletTransferActivity
 */
@Route(value = [RouterConstData.LEVER_PAIR_CHOOSE])
class LeverPairChooseActivity : BaseActionBarActivity(), View.OnClickListener{
    private var binding: ActivityLeverPairChooseBinding? = null
    private var adapter: LeverPairAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_lever_pair_choose)
        binding?.relFrom?.setOnClickListener(this)
        binding?.relTo?.setOnClickListener(this)
        binding?.relChoose?.setOnClickListener(this)
        var actionBarRecord: ImageButton? = binding?.root?.findViewById(R.id.img_action_bar_right)
        actionBarRecord?.visibility = View.VISIBLE
        actionBarRecord?.setOnClickListener(this)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }


    override fun getTitleText(): String {
        return getString(R.string.asset_transfer)
    }

    private fun getLeverPair() {
        Observable.just(SocketDataContainer.getAllLeverPair(this) ?: ArrayList())
                .compose(RxJavaHelper.observeOnMainThread())
                .subscribe {
                    adapter?.data = it
                    adapter?.notifyDataSetChanged()
                }
                .run { }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.rel_from -> {
                showWalletChooseDialog()
            }
            R.id.rel_to ->{

            }
            R.id.rel_choose ->{
                showCoinChooseDialog()
            }
            R.id.img_action_bar_right ->{
                val bundle = Bundle()
                var pair = "BTC"
                bundle.putString(ConstData.PAIR, pair)
                BlackRouter.getInstance().build(RouterConstData.WALLET_TRANSFER_RECORD).with(bundle).go(this)
            }
        }
    }

    private fun showWalletChooseDialog(){
        var fromWallet = "现货账户"
        var toWallet = "合约账户"
        var wallList = ArrayList<Wallet?>()
        var wallet1 = Wallet()
        wallet1.name = "现货账户"
        var wallet2 = Wallet()
        wallet2.name = "合约账户"
        var wallet3 = Wallet()
        wallet3.name = "理财账户"
        var wallet4 = Wallet()
        wallet4.name = "钱包账户"
        wallList.add(wallet1)
        wallList.add(wallet2)
        wallList.add(wallet3)
        wallList.add(wallet4)
        ChooseWalletControllerWindow(mContext as Activity, getString(R.string.select_wallet), wallet1,
            wallList,
            object : ChooseWalletControllerWindow.OnReturnListener<Wallet?> {
                override fun onReturn(window: ChooseWalletControllerWindow<Wallet?>, item: Wallet?) {

                }

            }).show()
    }

    private fun showCoinChooseDialog(){
        var fromWallet = "现货账户"
        var toWallet = "合约账户"
        var wallList = ArrayList<Wallet?>()
        var wallet1 = Wallet()
        wallet1.name = "现货账户"
        var wallet2 = Wallet()
        wallet2.name = "合约账户"
        var wallet3 = Wallet()
        wallet3.name = "理财账户"
        var wallet4 = Wallet()
        wallet4.name = "钱包账户"
        wallList.add(wallet1)
        wallList.add(wallet2)
        wallList.add(wallet3)
        wallList.add(wallet4)
        ChooseCoinControllerWindow(mContext as Activity, getString(R.string.select_wallet), wallet1,
            wallList,
            object : ChooseCoinControllerWindow.OnReturnListener<Wallet?> {
                override fun onReturn(window: ChooseCoinControllerWindow<Wallet?>, item: Wallet?) {

                }

            }).show()
    }

    inner class Wallet{
        var name:String? = null
        var selected:Boolean? = null
    }
}