package com.black.user.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.BaseApplication
import com.black.base.activity.BaseActivity
import com.black.base.util.ConstData
import com.black.base.util.RouterConstData
import com.black.router.annotation.Route
import com.black.user.R
import com.black.user.databinding.ActivityForgetPasswordNewPwdBinding

@Route(value = [RouterConstData.FORGET_PASSWORD_NEW_PWD])
class ForgetPasswordNewPwdActivity : BaseActivity(), View.OnClickListener {
    private var binding: ActivityForgetPasswordNewPwdBinding? = null
    private var type:Int? = ConstData.AUTHENTICATE_TYPE_PHONE
    private var account:String? = null
    private var countryCode:String? = null
    private var verifyCode:String? = null
    private var googlgCode:String? = null

    private val watcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            checkClickable()
        }

        override fun afterTextChanged(s: Editable) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = intent.getIntExtra(ConstData.TYPE, ConstData.AUTHENTICATE_TYPE_NONE)
        account = intent.getStringExtra(ConstData.ACCOUNT)
        countryCode = intent.getStringExtra(ConstData.COUNTRY_CODE)
        verifyCode = intent.getStringExtra(ConstData.VERIFY_CODE)
        googlgCode = intent.getStringExtra(ConstData.GOOGLE_CODE)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_forget_password_new_pwd)
        binding?.btnConfirm?.setOnClickListener(this)
        binding?.newPsw?.addTextChangedListener(watcher)
        initView()
    }


    private fun initView(){
        when(type){
            ConstData.AUTHENTICATE_TYPE_PHONE ->{
                binding?.loginType?.text = getString(R.string.phone_number)
            }
            ConstData.AUTHENTICATE_TYPE_MAIL ->{
                binding?.loginType?.text = getString(R.string.email)
                binding?.countryCode?.visibility = View.GONE
            }
        }
        binding?.tvAccount?.text = account
    }

    private fun checkClickable() {
        binding!!.btnConfirm.isEnabled = !(TextUtils.isEmpty(binding!!.newPsw.text.toString().trim { it <= ' ' }))
    }


    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.new_psw)
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.btn_confirm ->{

            }
        }
    }

}