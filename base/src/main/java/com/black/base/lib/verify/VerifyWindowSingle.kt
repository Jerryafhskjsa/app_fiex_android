package com.black.base.lib.verify

import android.app.Activity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.black.base.R
import com.black.util.CommonUtil
import com.google.android.material.tabs.TabLayout
import skin.support.content.res.SkinCompatResources
import java.util.*

internal open class VerifyWindowSingle : VerifyWindow, TabLayout.OnTabSelectedListener {
    private var tabLayout: TabLayout? = null
    private var contentLayout: LinearLayout? = null
    private var messageView: TextView? = null

    constructor(activity: Activity?, type: Int, alwaysNoToken: Boolean, target: Target?, onReturnListener: OnReturnListener?) : super(activity!!, type, alwaysNoToken, target!!, onReturnListener) {}
    constructor(activity: Activity?, type: Int, checkType: Int, alwaysNoToken: Boolean, target: Target?, onReturnListener: OnReturnListener?) : super(activity!!, type, checkType, alwaysNoToken, target!!, onReturnListener) {}

    override fun placeWorkSpace(workSpaceLayout: LinearLayout?) {
        val innerView = inflater.inflate(R.layout.view_verify_single, null)
        contentLayout = innerView.findViewById(R.id.content_layout)
        messageView = innerView.findViewById(R.id.message)
        messageView?.visibility = View.GONE
        tabLayout = innerView.findViewById(R.id.tab_layout)
        tabLayout?.setTabTextColors(SkinCompatResources.getColor(activity, R.color.C5), SkinCompatResources.getColor(activity, R.color.C1))
        tabLayout?.setSelectedTabIndicatorHeight(0)
        tabLayout?.tabMode = TabLayout.MODE_FIXED
        showViews = ArrayList()
        googleIndex = -1
        phoneIndex = -1
        if (type and VerifyType.GOOGLE == VerifyType.GOOGLE) {
            val tab = tabLayout?.newTab()?.setText(R.string.verify_google)
            tab?.setCustomView(R.layout.view_verify_single_tab)
            val textView = tab?.customView?.findViewById<View>(android.R.id.text1) as TextView?
            textView?.setText(R.string.verify_google)
            tab?.tag = VerifyType.GOOGLE
            tabLayout?.addTab(tab!!)
            val googleLayout = getGoogleLayout()
            googleLayout?.visibility = View.GONE
            contentLayout?.addView(googleLayout)
            showViews.add(googleLayout)
            googleIndex = 0
        }
        if (type and VerifyType.PHONE == VerifyType.PHONE) {
            phoneIndex = googleIndex + 1
            val tab = tabLayout?.newTab()?.setText(R.string.verify_phone)
            tab?.setCustomView(R.layout.view_verify_single_tab)
            val textView = tab?.customView?.findViewById<View>(android.R.id.text1) as TextView?
            textView?.setText(R.string.verify_phone)
            tab?.tag = VerifyType.PHONE
            tabLayout?.addTab(tab!!)
            val phoneLayout = getPhoneLayout()
            phoneLayout?.visibility = View.GONE
            contentLayout?.addView(phoneLayout)
            showViews.add(phoneLayout)
        }
        if (type and VerifyType.MAIL == VerifyType.MAIL) {
            val tab = tabLayout?.newTab()?.setText(R.string.verify_mail)
            tab?.setCustomView(R.layout.view_verify_single_tab)
            val textView = tab?.customView?.findViewById<View>(android.R.id.text1) as TextView?
            textView?.setText(R.string.verify_mail)
            tab?.tag = VerifyType.MAIL
            tabLayout?.addTab(tab!!)
            val mailLayout = getMailLayout()
            mailLayout?.visibility = View.GONE
            contentLayout?.addView(mailLayout)
            showViews.add(mailLayout)
        }
        if (type and VerifyType.MONEY_PASSWORD == VerifyType.MONEY_PASSWORD) {
            val tab = tabLayout?.newTab()?.setText(R.string.money_password)
            tab?.setCustomView(R.layout.view_verify_single_tab)
            val textView = tab?.customView?.findViewById<View>(android.R.id.text1) as TextView?
            textView?.setText(R.string.money_password)
            tab?.tag = VerifyType.MONEY_PASSWORD
            tabLayout?.addTab(tab!!)
            val passwordLayout = getMoneyPasswordLayout()
            passwordLayout?.visibility = View.GONE
            contentLayout?.addView(passwordLayout)
            showViews.add(passwordLayout)
        }
        if (type and VerifyType.PASSWORD == VerifyType.PASSWORD) {
            val tab = tabLayout?.newTab()?.setText(R.string.verify_password)
            tab?.setCustomView(R.layout.view_verify_single_tab)
            val textView = tab?.customView?.findViewById<View>(android.R.id.text1) as TextView?
            textView?.setText(R.string.verify_password)
            tab?.tag = VerifyType.PASSWORD
            tabLayout?.addTab(tab!!)
            val passwordLayout = getPasswordLayout()
            passwordLayout?.visibility = View.GONE
            contentLayout?.addView(passwordLayout)
            showViews.add(passwordLayout)
        }
        tabLayout?.addOnTabSelectedListener(this)
        titleView.visibility = View.GONE
        workSpaceLayout?.removeAllViews()
        workSpaceLayout?.addView(innerView)
        if ((tabLayout?.tabCount ?: 0) > 0) {
            tabLayout?.getTabAt(0)?.select()
            showViews[0]?.visibility = View.VISIBLE
            when {
                googleIndex != -1 -> {
                    messageView?.setText(R.string.google_code_lost)
                    messageView?.visibility = View.VISIBLE
                }
                phoneIndex != -1 -> {
                    messageView?.setText(R.string.phone_number_error)
                    messageView?.visibility = View.VISIBLE
                }
                else -> {
                    messageView?.text = ""
                    messageView?.visibility = View.GONE
                }
            }
        }
    }

    override val result: Target
        get() {
            val target = Target()
            when (returnType) {
                VerifyType.GOOGLE -> target.googleCode = googleCode
                VerifyType.PHONE -> {
                    target.phoneCode = phoneCode
                    target.phoneCaptcha = phoneCaptcha
                }
                VerifyType.MAIL -> {
                    target.mailCode = mailCode
                    target.mailCaptcha = mailCaptcha
                }
                VerifyType.PASSWORD -> target.password = password
                VerifyType.MONEY_PASSWORD -> target.moneyPassword = moneyPassword
            }
            return target
        }

    override val returnType: Int
        get() {
            val tab = tabLayout?.getTabAt(tabLayout?.selectedTabPosition ?: -1)
            val tabTag = if (tab == null) null else CommonUtil.parseInt(tab.tag)
            return tabTag ?: VerifyType.NONE
        }

    override fun onTabSelected(tab: TabLayout.Tab) {
        val index = tab.position
        for (i in showViews.indices) {
            if (index == i) {
                showViews[i]?.visibility = View.VISIBLE
            } else {
                showViews[i]?.visibility = View.GONE
            }
        }
        when {
            googleIndex == index -> {
                messageView?.setText(R.string.google_code_lost)
                messageView?.visibility = View.VISIBLE
            }
            phoneIndex == index -> {
                messageView?.setText(R.string.phone_number_error)
                messageView?.visibility = View.VISIBLE
            }
            else -> {
                messageView?.text = ""
                messageView?.visibility = View.GONE
            }
        }
        //        if (index == 0) {
//            messageView.setText("谷歌验证码遗失，请联系客服");
//            messageView.setVisibility(View.VISIBLE);
//        } else if (index == 1) {
//            messageView.setText("手机号码无法接收验证码，请联系客服");
//            messageView.setVisibility(View.VISIBLE);
//        } else {
//            messageView.setText("");
//            messageView.setVisibility(View.GONE);
//        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {}
    override fun onTabReselected(tab: TabLayout.Tab) {}

    companion object {
        private var showViews: MutableList<View?> = ArrayList()
        private var googleIndex = -1
        private var phoneIndex = -1
    }
}
