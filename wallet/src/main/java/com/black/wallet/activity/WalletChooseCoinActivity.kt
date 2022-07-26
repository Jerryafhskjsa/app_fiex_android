package com.black.wallet.activity

import android.app.Activity
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
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActivity
import com.black.base.api.WalletApiServiceHelper
import com.black.base.databinding.ListViewEmptyLongBinding
import com.black.base.model.wallet.Wallet
import com.black.base.util.ConstData
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.view.SideBar
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.wallet.R
import com.black.wallet.adapter.WalletChooseCoinAdapter
import com.black.wallet.databinding.ActivityWalletChooseCoinBinding
import skin.support.content.res.SkinCompatResources
import java.util.*

@Route(value = [RouterConstData.WALLET_CHOOSE_COIN], beforePath = RouterConstData.LOGIN)
class WalletChooseCoinActivity : BaseActivity(), View.OnClickListener, AdapterView.OnItemClickListener {
    private var supportCoinList: ArrayList<String>? = null

    private var binding: ActivityWalletChooseCoinBinding? = null

    private var walletList: ArrayList<Wallet?>? = null
    private var adapter: WalletChooseCoinAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportCoinList = intent.getStringArrayListExtra(ConstData.SUPPORT_COIN_LIST)
        walletList = intent.getParcelableArrayListExtra(ConstData.WALLET_LIST)
        walletList = getAfterFilterWalletList(walletList)
        if (walletList != null) {
            Collections.sort(walletList, Wallet.COMPARATOR_CHOOSE_COIN)
        }

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
        binding?.history01?.setOnClickListener(this)
        binding?.history02?.setOnClickListener(this)
        binding?.history03?.setOnClickListener(this)
        binding?.history04?.setOnClickListener(this)
        binding?.listView?.onItemClickListener = this
        adapter = WalletChooseCoinAdapter(this, walletList)
        binding?.listView?.adapter = adapter
        val drawable = ColorDrawable()
        drawable.color = SkinCompatResources.getColor(mContext, R.color.L1_ALPHA60)
        binding?.listView?.setDivider(drawable)
        binding?.listView?.dividerHeight = 1

        val emptyBinding: ListViewEmptyLongBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.list_view_empty_long, null, false)
        val group = binding?.listView?.parent as ViewGroup
        group.addView(emptyBinding.root)
        binding?.listView?.emptyView = emptyBinding.root

        binding?.sideBar?.setOnTouchLetterChangeListener(object : SideBar.OnTouchLetterChangeListener {
            override fun letterChange(s: String?) {
                val position = if (s == null) null else adapter?.getPositionForSection(s[0].toInt())
                if (position != null && position != -1) {
                    binding?.listView?.setSelection(position)
                }
            }

        })
        allWallet
        refreshSearchHistory()
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.history_01 || i == R.id.history_02 || i == R.id.history_03 || i == R.id.history_04) { //            search(((TextView) v).getText().toString());
            val coinType = (v as TextView).text.toString()
            var searchWallet: Wallet? = null
            if (walletList != null) {
                for (wallet in walletList!!) {
                    if (coinType.equals(wallet?.coinType, ignoreCase = true)) {
                        searchWallet = wallet
                        break
                    }
                }
            }
            if (searchWallet != null) {
                val resultData = Intent()
                resultData.putExtra(ConstData.WALLET, searchWallet)
                setResult(Activity.RESULT_OK, resultData)
                finish()
            } else {
                FryingUtil.showToast(this, getString(R.string.coin_search_failed))
            }
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        val wallet = adapter?.getItem(position)
        saveSearchHistory(wallet?.coinType)
        val resultData = Intent()
        resultData.putExtra(ConstData.WALLET, wallet)
        setResult(Activity.RESULT_OK, resultData)
        finish()
    }

    private val allWallet: Unit
        get() {
            WalletApiServiceHelper.getWalletList(mContext, true, object : Callback<ArrayList<Wallet?>?>() {
                override fun callback(result: ArrayList<Wallet?>?) {
                    walletList = result
                    walletList = getAfterFilterWalletList(walletList)
                    showWalletList()
                }

                override fun error(type: Int, message: Any) {
                    walletList = null
                    showWalletList()
                    FryingUtil.showToast(mContext, message.toString())
                }
            })
        }

    private fun showWalletList() {
        walletList = if (walletList == null) ArrayList() else walletList
        search(binding?.chooseCoinSearch?.text.toString())
    }

    private fun getAfterFilterWalletList(walletListSource: ArrayList<Wallet?>?): ArrayList<Wallet?>? {
        if (walletListSource == null) {
            return null
        }
        if (supportCoinList == null) {
            return walletListSource
        }
        val result = ArrayList<Wallet?>()
        for (i in walletListSource.indices) {
            val wallet = walletListSource[i]
            if (wallet != null && supportCoinList!!.contains(wallet.coinType)) {
                result.add(wallet)
            }
        }
        return result
    }

    private fun clearSearchHistory() {
        CookieUtil.clearCoinSearchHistory(mContext)
        refreshSearchHistory()
    }

    private fun refreshSearchHistory() {
        var history = CookieUtil.getCoinSearchHistory(mContext)
        history = history ?: ArrayList()
        if (supportCoinList != null) {
            val tmp = ArrayList<String?>()
            for (i in history.indices) {
                if (supportCoinList!!.contains(history[i])) {
                    tmp.add(history[i])
                }
            }
            history = tmp
        }
        history.reverse()
        refreshHistoryItem(binding?.history01, CommonUtil.getItemFromList(history, 0))
        refreshHistoryItem(binding?.history02, CommonUtil.getItemFromList(history, 1))
        refreshHistoryItem(binding?.history03, CommonUtil.getItemFromList(history, 2))
        refreshHistoryItem(binding?.history04, CommonUtil.getItemFromList(history, 3))
    }

    private fun refreshHistoryItem(textView: TextView?, text: String?) {
        if (text != null) {
            textView!!.text = text
            textView.visibility = View.VISIBLE
        } else {
            textView!!.text = ""
            textView.visibility = View.INVISIBLE
        }
    }

    private fun saveSearchHistory(searchKey: String?) {
        if ("USDT".equals(searchKey, ignoreCase = true)) {
            return
        }
        if (searchKey != null && searchKey.trim { it <= ' ' }.isNotEmpty()) {
            CookieUtil.addCoinSearchHistory(mContext, searchKey)
            refreshSearchHistory()
        }
    }

    private fun search(searchKey: String?) {
        var result: ArrayList<Wallet?>? = ArrayList()
        if (searchKey == null || searchKey.trim { it <= ' ' }.isEmpty()) {
            result = walletList
        } else {
            for (wallet in walletList!!) {
                if (wallet?.coinType != null && wallet.coinType!!.toUpperCase(Locale.getDefault()).trim { it <= ' ' }.contains(searchKey.toUpperCase(Locale.getDefault()))) {
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