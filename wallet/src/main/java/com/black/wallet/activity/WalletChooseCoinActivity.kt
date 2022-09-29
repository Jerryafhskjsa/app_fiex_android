package com.black.wallet.activity

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActivity
import com.black.base.api.UserApiServiceHelper
import com.black.base.api.WalletApiServiceHelper
import com.black.base.databinding.ListViewEmptyLongBinding
import com.black.base.model.user.UserBalance
import com.black.base.model.user.UserBalanceWarpper
import com.black.base.model.wallet.CoinInfoType
import com.black.base.model.wallet.Wallet
import com.black.base.util.ConstData
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.view.SideBar
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.wallet.R
import com.black.wallet.adapter.WalletChooseCoinAdapter
import com.black.wallet.databinding.ActivityWalletChooseCoinBinding
import skin.support.content.res.SkinCompatResources
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList

@Route(value = [RouterConstData.WALLET_CHOOSE_COIN], beforePath = RouterConstData.LOGIN)
class WalletChooseCoinActivity : BaseActivity(), View.OnClickListener, AdapterView.OnItemClickListener {

    private var userSoptBanlace: ArrayList<UserBalance?>? = null
    private var walletList: ArrayList<Wallet?>? = null
    private var supportCoinInfoList:ArrayList<CoinInfoType?>? = null
    private var binding: ActivityWalletChooseCoinBinding? = null
    private var adapter: WalletChooseCoinAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_wallet_choose_coin)
        binding?.chooseCoinSearch?.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND || event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                search(v.text.toString())
                hideSoftKeyboard()
                return@OnEditorActionListener true
            }
            false
        })
        binding?.chooseCoinSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                search(s.toString())
            }

            override fun afterTextChanged(s: Editable) {}
        })
        binding?.listView?.onItemClickListener = this
        adapter = WalletChooseCoinAdapter(this, walletList)
        binding?.listView?.adapter = adapter

        val drawable = ColorDrawable()
        drawable.color = SkinCompatResources.getColor(mContext, R.color.L1_ALPHA60)
        binding?.listView?.divider = drawable
        binding?.listView?.dividerHeight = 0

        val emptyBinding: ListViewEmptyLongBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.list_view_empty_long, null, false)
        val group = binding?.listView?.parent as ViewGroup
        group.addView(emptyBinding.root)
        binding?.listView?.emptyView = emptyBinding.root
        getCoinlistConfig()
    }

    /**
     * 获取币种配置
     */
    private fun getCoinlistConfig(){
        WalletApiServiceHelper.getCoinInfoList(this, object :Callback<ArrayList<CoinInfoType?>?>(){
            override fun callback(returnData: ArrayList<CoinInfoType?>?) {
                supportCoinInfoList = returnData
                getUserBalance()
            }
            override fun error(type: Int, error: Any?) {
            }
        })
    }

    /**
     * 获取用户资产
     */
    private fun getUserBalance(){
        WalletApiServiceHelper.getUserBalanceReal(this,false,object :Callback<UserBalanceWarpper?>(){
            override fun callback(returnData: UserBalanceWarpper?) {
                if(returnData != null){
                    userSoptBanlace = returnData?.spotBalance
                    getWalletData()
                    if (walletList != null) {
                        Collections.sort(walletList, Wallet.COMPARATOR_CHOOSE_COIN)
                    }
                    showWalletList()
                }
            }
            override fun error(type: Int, error: Any?) {
            }
        },object :Callback<Any?>(){
            override fun error(type: Int, error: Any?) {
            }

            override fun callback(returnData: Any?) {
            }
        })
    }

    private fun getWalletData(){
        walletList?.clear()
        for (i in supportCoinInfoList!!.indices){
            var wallet = Wallet()
            wallet.coinAmount = BigDecimal.valueOf(0.00)
            for (j in userSoptBanlace!!.indices){
                if(supportCoinInfoList!![i]?.coinType.equals(userSoptBanlace!![j]?.coin)){
                    wallet.coinAmount = (userSoptBanlace!![j]?.availableBalance)?.toBigDecimal()
                }
            }
            wallet.coinType = supportCoinInfoList!![i]?.coinType
            wallet.coinIconUrl = supportCoinInfoList!![i]?.config?.get(0)?.coinConfigVO?.logosUrl
            wallet.coinTypeDes = supportCoinInfoList!![i]?.config?.get(0)?.coinConfigVO?.coinFullName
            walletList?.add(wallet)
        }
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String? {
        return getString(R.string.coin_choose)
    }

    override fun onClick(v: View) {
        when(v.id){
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        val wallet = adapter?.getItem(position)
        saveSearchHistory(wallet?.coinType)
        val resultData = Intent()
        resultData.putExtra(ConstData.WALLET, wallet)
        setResult(RESULT_OK, resultData)
        finish()
    }


    private fun showWalletList() {
        walletList = if (walletList == null) ArrayList() else walletList
        search(binding?.chooseCoinSearch?.text.toString())
    }

    private fun saveSearchHistory(searchKey: String?) {
        if ("USDT".equals(searchKey, ignoreCase = true)) {
            return
        }
        if (searchKey != null && searchKey.trim { it <= ' ' }.isNotEmpty()) {
            CookieUtil.addCoinSearchHistory(mContext, searchKey)
        }
    }

    private fun search(searchKey: String?) {
        var result: ArrayList<Wallet?>? = ArrayList()
        if (searchKey == null || searchKey.trim { it <= ' ' }.isEmpty()) {
            result = walletList
        } else {
            for (wallet in walletList!!) {
                if (wallet?.coinType != null && wallet.coinType!!.uppercase(Locale.getDefault()).trim { it <= ' ' }.contains(
                        searchKey.uppercase(Locale.getDefault())
                    )) {
                    result!!.add(wallet)
                }
            }
        }
        if (result != null) {
            Collections.sort(result, Wallet.COMPARATOR_CHOOSE_COIN)
        }
        adapter?.data = result
        adapter?.notifyDataSetChanged()
    }
}