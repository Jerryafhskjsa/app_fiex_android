package com.black.user.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActivity
import com.black.base.api.UserApiServiceHelper
import com.black.base.model.HttpRequestResultString
import com.black.base.model.user.UserInfo
import com.black.base.util.ConstData
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.user.R
import com.black.user.databinding.ActivitySafeBindBinding
import com.black.util.Callback
import com.black.util.CommonUtil



@Route(value = [RouterConstData.SAFE_BIND])
class SafeBindActivity: BaseActivity(), View.OnClickListener{

    private var code1 = ArrayList<EditText>()
    private var type = ConstData.AUTHENTICATE_TYPE_PHONE
    private var account:String? = null
    private var binding: ActivitySafeBindBinding? = null
    private var phoneAccount: String? = null
    private var mailAccount: String? =null
    private lateinit var editFirst:EditText
    private lateinit var editSecond:EditText
    private lateinit var editThird:EditText
    private lateinit var editFourth:EditText
    private lateinit var editFiveth:EditText
    private lateinit var editSixth:EditText
    private var sub1 = false
    private var sub2 = false
    private val mHandler = Handler()

    private var getPhoneCodeLocked = false
    private var getPhoneCodeLockedTime = 0
    private val getPhoneCodeLockTimer = object : Runnable {
        override fun run() {
            getPhoneCodeLockedTime--
            if (getPhoneCodeLockedTime <= 0) {
                getPhoneCodeLocked = false
                binding?.sent?.setText(R.string.get_check_code)
            } else {
                binding?.sent?.setText(getString(R.string.aler_get_code_locked, getPhoneCodeLockedTime.toString()))
                mHandler.postDelayed(this, ConstData.ONE_SECOND_MILLIS.toLong())
            }
        }

    }
    private var getMailCodeLocked = false
    private var getMailCodeLockedTime = 0
    private val getMailCodeLockTimer = object : Runnable {
        override fun run() {
            getMailCodeLockedTime--
            if (getMailCodeLockedTime <= 0) {
                getMailCodeLocked = false
                binding?.sent?.setText(R.string.get_check_code)
            } else {
                binding?.sent?.setText(getString(R.string.aler_get_code_locked, getMailCodeLockedTime.toString()))
                mHandler.postDelayed(this, ConstData.ONE_SECOND_MILLIS.toLong())
            }
        }

    }

    private val watcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable) {
            if (s.length != 0){
                focus()
            }else{
                safeBind()
            }

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = intent.getIntExtra(ConstData.TYPE, ConstData.AUTHENTICATE_TYPE_NONE)
        account = intent.getStringExtra(ConstData.ACCOUNT)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_safe_bind)
        editFirst = findViewById(R.id.edit_first)
        code1.add(editFirst)
        editSecond = findViewById(R.id.edit_second)
        code1.add(editSecond)
        editThird = findViewById(R.id.edit_third)
        code1.add(editThird)
        editFourth = findViewById(R.id.edit_fourth)
        code1.add(editFourth)
        editFiveth = findViewById(R.id.edit_fiveth)
        code1.add(editFiveth)
        editSixth = findViewById(R.id.edit_sixth)
        code1.add(editSixth)

        editFirst.setFocusable(true)
        binding?.googleCode?.setOnClickListener(this)
        binding?.mailCode?.setOnClickListener(this)
        binding?.phoneCode?.setOnClickListener(this)
        binding?.safeUnused?.setOnClickListener(this)
        binding?.sent?.setOnClickListener(this)
        binding?.code?.setOnClickListener(this)
        binding?.editFirst?.addTextChangedListener(watcher)
        binding?.editSecond?.addTextChangedListener(watcher)
        binding?.editThird?.addTextChangedListener(watcher)
        binding?.editFourth?.addTextChangedListener(watcher)
        binding?.editFiveth?.addTextChangedListener(watcher)
        binding?.editSixth?.addTextChangedListener(watcher)
        change()

    }
    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.safe_bind_check)
    }
    private fun change() {
        if (account != null) {
            when (type) {
                ConstData.AUTHENTICATE_TYPE_PHONE -> {
                    binding?.mailBarB?.visibility = View.GONE
                    binding?.phoneBarB?.visibility = View.GONE
                    binding?.googleBarB?.visibility = View.VISIBLE
                    binding?.mailCode?.visibility = View.GONE
                    binding?.phoneCode?.visibility = View.VISIBLE
                    binding?.googleCode?.visibility = View.VISIBLE
                    binding?.sent?.visibility = View.GONE
                    binding?.code?.visibility = View.VISIBLE
                }
                ConstData.AUTHENTICATE_TYPE_MAIL -> {
                    binding?.mailBarB?.visibility = View.GONE
                    binding?.phoneBarB?.visibility = View.GONE
                    binding?.googleBarB?.visibility = View.VISIBLE
                    binding?.view2?.visibility = View.GONE
                    binding?.mailCode?.visibility = View.VISIBLE
                    binding?.phoneCode?.visibility = View.GONE
                    binding?.googleCode?.visibility = View.VISIBLE
                    binding?.sent?.visibility = View.GONE
                    binding?.code?.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id){
                R.id.google_code -> {
                    changeLoinType(type)
                }
                R.id.mail_code -> {
                    changeLoinType(type)
                }
                R.id.phone_code ->{
                    changeLoinType(type)
                }
                R.id.safe_unused -> {
                    BlackRouter.getInstance().build(RouterConstData.SAFE_UNUSED).go(this)
                }
                R.id.sent -> {
                    if ( binding?.mailCode?.visibility == View.GONE){
                        phoneVerifyCode
                        sub1 = true
                    }else
                        mailVerifyCode
                        sub2 = true
                }

                R.id.code ->{
                    CommonUtil.pasteText(mContext, object : Callback<String>() {
                        override fun error(type: Int, error: Any) {}
                        override fun callback(returnData: String) {
                           editFirst.setText(returnData.substring(0,1))
                            editSecond.setText(returnData.substring(1,2))
                            editThird.setText(returnData.substring(2,3))
                            editFourth.setText(returnData.substring(3,4))
                            editFiveth.setText(returnData.substring(4,5))
                            editSixth.setText(returnData.substring(5,6))
                        }
                    })
                    safeBind()
                }

            }

        }
    }

    private fun changeLoinType(loginType:Int?){
        when(loginType){
            ConstData.AUTHENTICATE_TYPE_GOOGLE ->{
                binding?.mailBarB?.visibility = View.GONE
                binding?.phoneBarB?.visibility = View.GONE
                binding?.googleBarB?.visibility = View.VISIBLE
                binding?.sent?.visibility = View.GONE
                binding?.code?.visibility = View.VISIBLE

            }
            ConstData.AUTHENTICATE_TYPE_PHONE ->{
                binding?.mailBarB?.visibility = View.GONE
                binding?.phoneBarB?.visibility = View.VISIBLE
                binding?.googleBarB?.visibility = View.GONE
                binding?.sent?.visibility = View.VISIBLE
                binding?.code?.visibility = View.GONE
              phoneAccount = account
              mailAccount = null
            }
            ConstData.AUTHENTICATE_TYPE_MAIL ->{
                binding?.mailCode?.visibility = View.VISIBLE
                binding?.phoneCode?.visibility = View.GONE
                binding?.googleCode?.visibility = View.VISIBLE
                binding?.mailBarB?.visibility = View.VISIBLE
                binding?.phoneBarB?.visibility = View.GONE
                binding?.googleBarB?.visibility = View.GONE
                binding?.sent?.visibility = View.VISIBLE
                binding?.code?.visibility = View.GONE
                phoneAccount = null
                mailAccount = account

            }
        }
    }

    private fun focus(){
        var editText: EditText
        for (i in 0..code1.size){
            editText = code1.get(i)
            if (editText.getText().length < 1){
                editText.requestFocus()
                return
            }else
            {
                editText.setCursorVisible(false)
            }
        }
        val lastEditText = code1.get(code1.size-1)
        if (lastEditText.getText().length > 0){
            getResponse()
        }

    }
    fun getResponse(){
        Log.e("CodeView", "ok")
        val builder = StringBuilder()
        for (i in 0..code1.size)
            builder.append(code1.get(i).getText().toString())
    }

    fun setText(text: String){
        if (text.length == code1.size ) {
            val builder = StringBuilder(text)
            editFirst.setText(builder.substring(0,1))
            editSecond.setText(builder.substring(1,2))
            editThird.setText(builder.substring(2,3))
            editFourth.setText(builder.substring(3,4))
            editFiveth.setText(builder.substring(4,5))
            editThird.setText(builder.substring(5,6))

        }
        else{
            editFirst.setText("")
            editSecond.setText("")
            editThird.setText("")
            editFourth.setText("")
            editFiveth.setText("")
            editSixth.setText("")
            editFirst.requestFocus()
        }
    }
    private val phoneVerifyCode: Unit
        get() {
            UserApiServiceHelper.getVerifyCode(this, phoneAccount, null, object : NormalCallback<HttpRequestResultString?>() {
                override fun callback(returnData: HttpRequestResultString?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        FryingUtil.showToast(mContext, getString(R.string.alert_verify_code_success))
                        //锁定发送按钮
                        if (!getPhoneCodeLocked) {
                            getPhoneCodeLocked = true
                            getPhoneCodeLockedTime = ConstData.GET_CODE_LOCK_TIME
                            mHandler.post(getPhoneCodeLockTimer)
                        }
                    } else {
                        FryingUtil.showToast(mContext, getString(R.string.alert_verify_code_failed))
                    }
                }
            })
        }

    private val mailVerifyCode: Unit
        get(){
            UserApiServiceHelper.getVerifyCode(this, mailAccount, null, object : NormalCallback<HttpRequestResultString?>() {
                override fun callback(returnData: HttpRequestResultString?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        FryingUtil.showToast(mContext, getString(R.string.alert_verify_code_success))
                        if (!getMailCodeLocked) {
                            getMailCodeLocked = true
                            getMailCodeLockedTime = ConstData.GET_CODE_LOCK_TIME
                            mHandler.post(getMailCodeLockTimer)
                        }
                    } else {
                        FryingUtil.showToast(mContext, getString(R.string.alert_verify_code_failed))
                    }
                }
            })
        }
    private fun safeBind(){
        val editFirst = binding?.editFirst?.text.toString().trim{it <= ' '}
        val editSecond = binding?.editSecond?.text.toString().trim { it <= ' ' }
        val editThird = binding?.editThird?.text.toString().trim { it <= ' ' }
        val editFourth = binding?.editFourth?.text.toString().trim { it <= ' ' }
        val editFiveth = binding?.editFiveth?.text.toString().trim {  it <= ' ' }
        val editSixth = binding?.editSixth?.text.toString().trim { it <= ' ' }
        val phoneCode : String?
        val googleCode: String?
        val mailCode: String?
       if (sub1){
            phoneCode = editFirst + editSecond + editThird + editFourth + editFiveth + editSixth
            mailCode = null
            googleCode = null
       }
       else if (sub2){
                phoneCode = null
                mailCode = editFirst + editSecond + editThird + editFourth + editFiveth + editSixth
                googleCode = null
        }
        else {
             phoneCode = null
             mailCode = null
             googleCode = editFirst + editSecond + editThird + editFourth + editFiveth + editSixth
        }
        UserApiServiceHelper.bindSafe(mContext,phoneAccount, phoneCode,mailAccount, mailCode, googleCode, object : NormalCallback<HttpRequestResultString?>() {
            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    FryingUtil.showToast(mContext, getString(R.string.bind_success))
                    BlackRouter.getInstance().build(RouterConstData.LOGIN).go(mContext) { routeResult, _ ->
                        if (routeResult) {
                            finish()
                        }
                    }

                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }


}