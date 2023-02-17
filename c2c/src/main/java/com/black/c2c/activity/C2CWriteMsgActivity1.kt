package com.black.c2c.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.CommonApiServiceHelper
import com.black.base.model.CountryCode
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.NormalCallback
import com.black.base.util.ConstData
import com.black.base.util.RouterConstData
import com.black.base.view.CountryChooseWindow
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cIdCardsTwoBinding
import com.black.c2c.databinding.ActivitySellerChooseBinding
import com.black.c2c.databinding.ViewFirstC2cBinding
import com.black.c2c.databinding.ViewSecondC2cBinding
import com.black.router.BlackRouter
import com.black.router.annotation.Route

@Route(value = [RouterConstData.C2C_MSG1])
class C2CWriteMsgActivity1: BaseActionBarActivity(), View.OnClickListener{
    private var binding: ActivityC2cIdCardsTwoBinding? = null
    private var thisCountry: CountryCode? = null
    private var chooseWindow: CountryChooseWindow? = null
    private var chooseWindow2: CountryChooseWindow? = null
    private val watcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            checkClickable()
        }

        override fun afterTextChanged(s: Editable) {}
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_id_cards_two)
        binding?.btnSubmit?.setOnClickListener(this)
        binding?.countryCode1?.setOnClickListener(this)
        binding?.countryCode2?.setOnClickListener(this)
        binding?.num1?.addTextChangedListener(watcher)
        binding?.num2?.addTextChangedListener(watcher)
        binding?.num3?.addTextChangedListener(watcher)
        binding?.num4?.addTextChangedListener(watcher)
        binding?.num5?.addTextChangedListener(watcher)
        binding?.num6?.addTextChangedListener(watcher)
        binding?.num7?.addTextChangedListener(watcher)
        binding?.num8?.addTextChangedListener(watcher)
        binding?.num9?.addTextChangedListener(watcher)
        if (thisCountry == null) {
            thisCountry = CountryCode()
            thisCountry?.code = "86"
        }
        chooseWindow = CountryChooseWindow(this, thisCountry, object :
            CountryChooseWindow.OnCountryChooseListener {
            override fun onCountryChoose(chooseWindow: CountryChooseWindow, countryCode: CountryCode?) {
                chooseWindow.dismiss()
                thisCountry = countryCode
                binding?.countryCode1?.tag = thisCountry?.code
                binding?.countryCode1?.setText("+" + thisCountry?.code)
            }
        })
        chooseWindow2 = CountryChooseWindow(this, thisCountry, object :
            CountryChooseWindow.OnCountryChooseListener {
            override fun onCountryChoose(chooseWindow: CountryChooseWindow, countryCode: CountryCode?) {
                chooseWindow.dismiss()
                thisCountry = countryCode
                binding?.countryCode2?.tag = thisCountry?.code
                binding?.countryCode2?.setText("+" + thisCountry?.code)
            }
        })
        initChooseWindowData()
        checkClickable()
    }

    override fun getTitleText(): String? {
        return  "填写申请人资料"
    }
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btn_submit){
            val bundle = Bundle()
            val address = binding?.num9?.text?.trim { it <= ' ' }.toString()
            val email = binding?.num5?.text?.trim { it <= ' ' }.toString()
            val emergencyName = binding?.num6?.text?.trim { it <= ' ' }.toString()
            val emergencyTel = binding?.num7?.text?.trim { it <= ' ' }.toString()
            val name = binding?.num1?.text?.trim { it <= ' ' }.toString()
            val nickName = binding?.num2?.text?.trim { it <= ' ' }.toString()
            val relation = binding?.num8?.text?.trim { it <= ' ' }.toString()
            val tel = binding?.num3?.text?.trim { it <= ' ' }.toString()
            bundle.putString("address", address)
            bundle.putString("email", email)
            bundle.putString("emergencyName", emergencyName)
            bundle.putString("emergencyTel", emergencyTel)
            bundle.putString("name" ,name)
            bundle.putString("nickName" ,nickName)
            bundle.putString("relation" ,relation)
            bundle.putString("tel" ,tel)
            BlackRouter.getInstance().build(RouterConstData.C2C_MSG2).with(bundle).go(this)
        }
        if (id == R.id.country_code_1){
            code1()
        }
        if (id == R.id.country_code_2){
            code2()
        }
    }
    private fun initChooseWindowData() {
        CommonApiServiceHelper.getCountryCodeList(this, false, object : NormalCallback<HttpRequestResultDataList<CountryCode?>?>(mContext!!) {
            override fun callback(returnData: HttpRequestResultDataList<CountryCode?>?) {
                if (returnData != null && returnData.code == 0 && returnData.data != null) {
                    chooseWindow?.setCountryList(returnData.data)
                    chooseWindow2?.setCountryList(returnData.data)
                }
            }
        })
    }
    private fun checkClickable() {
        binding?.btnSubmit?.isEnabled = !(TextUtils.isEmpty(binding?.num1?.text.toString().trim { it <= ' ' })
                || TextUtils.isEmpty(binding?.num2?.text.toString().trim { it <= ' ' })
                || TextUtils.isEmpty(binding?.num3?.text.toString().trim { it <= ' ' })
                || TextUtils.isEmpty(binding?.num4?.text.toString().trim { it <= ' ' })
                || TextUtils.isEmpty(binding?.num5?.text.toString().trim { it <= ' ' })
                || TextUtils.isEmpty(binding?.num6?.text.toString().trim { it <= ' ' })
                || TextUtils.isEmpty(binding?.num7?.text.toString().trim { it <= ' ' })
                || TextUtils.isEmpty(binding?.num8?.text.toString().trim { it <= ' ' })
                || TextUtils.isEmpty(binding?.num9?.text.toString().trim { it <= ' ' }))
    }
    private fun code1() {
        chooseWindow?.show(thisCountry)
    }
    private fun code2() {
        chooseWindow2?.show(thisCountry)
    }
}