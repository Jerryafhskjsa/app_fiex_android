package com.black.user.activity

import android.content.Intent
import android.os.Bundle
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


@Route(value = [RouterConstData.SAFE_BIND], beforePath = RouterConstData.LOGIN)
class SafeBindActivity: BaseActivity(), View.OnClickListener{
    private var userInfo: UserInfo? = null
    private var code = ArrayList<EditText>()
    private var binding: ActivitySafeBindBinding? = null
    private lateinit var editFirst:EditText
    private lateinit var editSecond:EditText
    private lateinit var editThird:EditText
    private lateinit var editFourth:EditText
    private lateinit var editFiveth:EditText
    private lateinit var editSixth:EditText



    private val watcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
       override fun afterTextChanged(s: Editable) {
            if (s.length != 0){
                focus()
            }else{
                safebind()
            }

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userInfo = CookieUtil.getUserInfo(mContext)
        if (userInfo == null) {
            finish()
            return
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_safe_bind)
        editFirst = findViewById(R.id.edit_first)
        code.add(editFirst)
        editSecond = findViewById(R.id.edit_second)
        code.add(editSecond)
        editThird = findViewById(R.id.edit_third)
        code.add(editThird)
        editFourth = findViewById(R.id.edit_fourth)
        code.add(editFourth)
        editFiveth = findViewById(R.id.edit_fiveth)
        code.add(editFiveth)
        editSixth = findViewById(R.id.edit_sixth)
        code.add(editSixth)

        editFirst.setFocusable(true)
        binding?.googleCode?.setOnClickListener(this)
        binding?.mailCode?.setOnClickListener(this)
        binding?.safeUnused?.setOnClickListener(this)
        binding?.code?.setOnClickListener(this)
        binding?.editFirst?.addTextChangedListener(watcher)
        binding?.editSecond?.addTextChangedListener(watcher)
        binding?.editThird?.addTextChangedListener(watcher)
        binding?.editFourth?.addTextChangedListener(watcher)
        binding?.editFiveth?.addTextChangedListener(watcher)
        binding?.editSixth?.addTextChangedListener(watcher)
        if (TextUtils.equals("1", userInfo!!.googleSecurityStatus)) {
            binding?.googleCode?.visibility = View.VISIBLE
        } else {
            binding?.googleCode?.visibility = View.GONE
        }
    }
    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.safe_mail_check)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id){
                R.id.google_code -> {
                    googleVerifyCode
                }
                R.id.mail_code -> {
                    mailVerifyCode

                }
                R.id.safe_unused -> {
                    BlackRouter.getInstance().build(RouterConstData.SAFE_UNUSED).go(this)
                }
                R.id.code -> {
                    CommonUtil.pasteText(mContext, object : Callback<String?>() {
                        override fun error(type: Int, error: Any) {}
                        override fun callback(returnData: String?) {
                            binding?.googleCode?.setText(returnData ?: "")
                        }
                    })
                    safebind()
                }

            }
        }
    }

    private fun focus(){
      var editText: EditText
     for (i in 0..code.size){
       editText = code.get(i)
         if (editText.getText().length < 1){
             editText.requestFocus()
             return
         }else
         {
             editText.setCursorVisible(false)
         }
     }
         val lastEditText = code.get(code.size-1)
         if (lastEditText.getText().length > 0){
             getResponse()
         }

    }
     fun getResponse(){
        Log.e("CodeView", "ok")
        val builder = StringBuilder()
        for (i in 0..code.size)
            builder.append(code.get(i).getText().toString())
    }

    fun setText(text: String){
        if (text.length == code.size ) {
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
    private val googleVerifyCode: Unit
    get() {
        UserApiServiceHelper.getVerifyCode(
            this,
            userInfo!!.google,
            null,
            object : NormalCallback<HttpRequestResultString?>() {
                override fun callback(returnData: HttpRequestResultString?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        FryingUtil.showToast(
                            mContext,
                            getString(R.string.alert_verify_code_success)
                        )
                    } else {
                        FryingUtil.showToast(mContext, getString(R.string.alert_verify_code_failed))
                    }
                }
            })
    }

    private val mailVerifyCode: Unit
    get(){
        UserApiServiceHelper.getVerifyCode(this, userInfo!!.email, null, object : NormalCallback<HttpRequestResultString?>() {
            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    FryingUtil.showToast(mContext, getString(R.string.alert_verify_code_success))
                } else {
                    FryingUtil.showToast(mContext, getString(R.string.alert_verify_code_failed))
                }
            }
        })
    }
    private fun safebind(){
        val googleCode = binding?.code?.text.toString().trim { it <= ' ' }
        val mailCode = binding?.code?.text.toString().trim { it <= ' ' }
        UserApiServiceHelper.bindSafe(mContext, null, null,null,null, mailCode, googleCode, object : NormalCallback<HttpRequestResultString?>() {
            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    FryingUtil.showToast(mContext, getString(R.string.bind_success))
                    onBindSuccess()
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    private fun onBindSuccess() {
        getUserInfo(object : Callback<UserInfo?>() {
            override fun callback(result: UserInfo?) {
                if (result != null) {
                    //回到安全中心界面
                    BlackRouter.getInstance().build(RouterConstData.SAFE_CENTER)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .go(mContext)
                }
            }

            override fun error(type: Int, error: Any) {
                //回到安全中心界面
                BlackRouter.getInstance().build(RouterConstData.SAFE_CENTER)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .go(mContext)
            }
        })
    }
}