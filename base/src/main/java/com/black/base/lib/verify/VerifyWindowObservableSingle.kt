package com.black.base.lib.verify

import android.app.Activity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.black.base.R
import com.black.base.databinding.ViewVerifySingleBinding
import com.black.base.lib.verify.VerifyType.Companion.GOOGLE
import com.black.base.lib.verify.VerifyType.Companion.MAIL
import com.black.base.lib.verify.VerifyType.Companion.MONEY_PASSWORD
import com.black.base.lib.verify.VerifyType.Companion.NONE
import com.black.base.lib.verify.VerifyType.Companion.PASSWORD
import com.black.base.lib.verify.VerifyType.Companion.PHONE
import com.black.util.CommonUtil
import com.google.android.material.tabs.TabLayout
import skin.support.content.res.SkinCompatResources
import java.util.*

class VerifyWindowObservableSingle
    (activity: Activity, type: Int, target: Target, alwaysNoToken: Boolean) : VerifyWindowObservable(activity, type, target, alwaysNoToken), TabLayout.BaseOnTabSelectedListener<TabLayout.Tab> {
    private var singleBinding: ViewVerifySingleBinding? = null
    private var showViews: ArrayList<View>? = ArrayList()
    private var googleIndex = -1
    private var phoneIndex = -1

    private var workSpace: LinearLayout? = null

    init {
        singleBinding = DataBindingUtil.inflate(inflater!!, R.layout.view_verify_single, null, false)
        singleBinding?.message?.visibility = View.GONE
        singleBinding?.tabLayout?.setTabTextColors(SkinCompatResources.getColor(activity, R.color.C5), SkinCompatResources.getColor(activity, R.color.C1))
        singleBinding?.tabLayout?.setSelectedTabIndicatorHeight(0)
        singleBinding?.tabLayout?.tabMode = TabLayout.MODE_FIXED
        this.showViews = ArrayList()
        this.googleIndex = -1
        this.phoneIndex = -1
        if (type and GOOGLE == GOOGLE) {
            val tab: TabLayout.Tab? = singleBinding?.tabLayout?.newTab()?.setText(R.string.verify_google)
            tab?.setCustomView(R.layout.view_verify_single_tab)
            val textView = tab?.customView?.findViewById<View>(android.R.id.text1) as TextView
            textView.setText(R.string.verify_google)
            tab.tag = GOOGLE
            singleBinding?.tabLayout?.addTab(tab)
            val googleLayout: View? = getGoogleLayout()?.root
            googleLayout!!.visibility = View.GONE
            singleBinding?.contentLayout?.addView(googleLayout)
            this.showViews?.add(googleLayout)
            this.googleIndex = 0
        }
        if (type and PHONE == PHONE) {
            this.phoneIndex = googleIndex + 1
            val tab: TabLayout.Tab? = singleBinding?.tabLayout?.newTab()?.setText(R.string.verify_phone)
            tab?.setCustomView(R.layout.view_verify_single_tab)
            val textView = tab?.customView?.findViewById<View>(android.R.id.text1) as TextView
            textView.setText(R.string.verify_phone)
            tab.tag = PHONE
            singleBinding?.tabLayout?.addTab(tab)
            val phoneLayout: View? = getPhoneLayout()?.root
            phoneLayout?.visibility = View.GONE
            singleBinding?.contentLayout?.addView(phoneLayout)
            this.showViews?.add(phoneLayout!!)
        }
        if (type and MAIL == MAIL) {
            val tab: TabLayout.Tab? = singleBinding?.tabLayout?.newTab()?.setText(R.string.verify_mail)
            tab?.setCustomView(R.layout.view_verify_single_tab)
            val textView = tab?.customView?.findViewById<View>(android.R.id.text1) as TextView
            textView.setText(R.string.verify_mail)
            tab.tag = MAIL
            singleBinding?.tabLayout?.addTab(tab)
            val mailLayout: View? = getMailLayout()?.root
            mailLayout!!.visibility = View.GONE
            singleBinding?.contentLayout?.addView(mailLayout)
            this.showViews?.add(mailLayout)
        }
        if (type and MONEY_PASSWORD == MONEY_PASSWORD) {
            val tab: TabLayout.Tab? = singleBinding?.tabLayout?.newTab()?.setText(R.string.money_password)
            tab?.setCustomView(R.layout.view_verify_single_tab)
            val textView = tab?.customView?.findViewById<View>(android.R.id.text1) as TextView
            textView.setText(R.string.money_password)
            tab.tag = MONEY_PASSWORD
            singleBinding?.tabLayout?.addTab(tab)
            val moneyPasswordLayout: View? = getMoneyPasswordLayout()?.root
            moneyPasswordLayout?.visibility = View.GONE
            singleBinding?.contentLayout?.addView(moneyPasswordLayout)
            this.showViews?.add(moneyPasswordLayout!!)
        }
        if (type and PASSWORD == PASSWORD) {
            val tab: TabLayout.Tab? = singleBinding?.tabLayout?.newTab()?.setText(R.string.verify_password)
            tab?.setCustomView(R.layout.view_verify_single_tab)
            val textView = tab?.customView?.findViewById<View>(android.R.id.text1) as TextView
            textView.setText(R.string.verify_password)
            tab.tag = PASSWORD
            singleBinding?.tabLayout?.addTab(tab)
            val passwordLayout: View? = getPasswordLayout()?.root
            passwordLayout?.visibility = View.GONE
            singleBinding?.contentLayout?.addView(passwordLayout)
            this.showViews?.add(passwordLayout!!)
        }
        singleBinding?.tabLayout?.addOnTabSelectedListener(this)
        binding?.title?.visibility = View.GONE
        workSpace?.removeAllViews()
        workSpace?.addView(singleBinding?.root)
        if (singleBinding?.tabLayout?.tabCount!! > 0) {
            singleBinding?.tabLayout?.getTabAt(0)?.select()
            this.showViews!![0].visibility = View.VISIBLE
            when {
                this.googleIndex != -1 -> {
                    singleBinding?.message?.setText(R.string.google_code_lost)
                    singleBinding?.message?.visibility = View.VISIBLE
                }
                this.phoneIndex != -1 -> {
                    singleBinding?.message?.setText(R.string.phone_number_error)
                    singleBinding?.message?.visibility = View.VISIBLE
                }
                else -> {
                    singleBinding?.message?.setText("")
                    singleBinding?.message?.visibility = View.GONE
                }
            }
        }
    }

    override fun placeWorkSpace(workSpaceLayout: LinearLayout?) {
        workSpace = workSpaceLayout
    }

    override fun getResult(): Target {
        val target = Target()
        when (getReturnType()) {
            GOOGLE -> target.googleCode = getGoogleCode()
            PHONE -> {
                target.phoneCode = getPhoneCode()
                target.phoneCaptcha = phoneCaptcha
            }
            MAIL -> {
                target.mailCode = getMailCode()
                target.mailCaptcha = mailCaptcha
            }
            PASSWORD -> target.password = getPassword()
            MONEY_PASSWORD -> target.moneyPassword = getMoneyPassword()
        }
        return target
    }

    override fun getReturnType(): Int {
        val index: Int = if (singleBinding?.tabLayout?.selectedTabPosition == null) -1 else singleBinding?.tabLayout?.selectedTabPosition!!
        val tab: TabLayout.Tab? = if (index == -1) null else singleBinding?.tabLayout?.getTabAt(index)
        val tabTag = if (tab == null) null else CommonUtil.parseInt(tab.tag)
        return tabTag ?: NONE
    }

    override fun onTabReselected(p0: TabLayout.Tab?) {
    }

    override fun onTabUnselected(p0: TabLayout.Tab?) {
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        val index: Int = tab?.position!!
        for (i in this.showViews!!.indices) {
            if (index == i) {
                this.showViews!![i].visibility = View.VISIBLE
            } else {
                this.showViews!![i].visibility = View.GONE
            }
        }
        when {
            this.googleIndex == index -> {
                singleBinding?.message?.setText(R.string.google_code_lost)
                singleBinding?.message?.visibility = View.VISIBLE
            }
            this.phoneIndex == index -> {
                singleBinding?.message?.setText(R.string.phone_number_error)
                singleBinding?.message?.visibility = View.VISIBLE
            }
            else -> {
                singleBinding?.message?.setText("")
                singleBinding?.message?.visibility = View.GONE
            }
        }
    }
}