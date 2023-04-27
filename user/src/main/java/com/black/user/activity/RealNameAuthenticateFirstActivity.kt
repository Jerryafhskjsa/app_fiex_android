package com.black.user.activity


import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.widget.DatePicker
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActivity
import com.black.base.api.CommonApiServiceHelper
import com.black.base.model.CountryCode
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.NormalCallback
import com.black.base.model.user.UserInfo
import com.black.base.util.ConstData
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.view.CountryChooseWindow
import com.black.base.view.CountryChooseWindow.OnCountryChooseListener
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.user.R
import com.black.user.databinding.ActivityRealNameAuthenticateFirstBinding
import java.util.*

//实名认证
@Route(value = [RouterConstData.REAL_NAME_AUTHENTICATE_FIRST], beforePath = RouterConstData.LOGIN)
class RealNameAuthenticateFirstActivity : BaseActivity(), View.OnClickListener {
    private var userInfo: UserInfo? = null

    private var binding: ActivityRealNameAuthenticateFirstBinding? = null

    private val watcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            checkClickable()
        }

        override fun afterTextChanged(s: Editable) {}
    }
    private var thisCountry: CountryCode? = null
    private var chooseWindow: CountryChooseWindow? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userInfo = CookieUtil.getUserInfo(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_real_name_authenticate_first)

        binding?.country?.setOnClickListener(this)
        binding?.country?.addTextChangedListener(watcher)
        binding?.birth?.setOnClickListener(this)
        binding?.birth?.addTextChangedListener(watcher)
        binding?.name?.addTextChangedListener(watcher)
        binding?.identity?.addTextChangedListener(watcher)
        if (userInfo == null || TextUtils.isEmpty(userInfo!!.backReason)) {
            binding?.failedReason?.visibility = View.GONE
        } else {
            binding?.failedReason?.visibility = View.VISIBLE
            binding?.failedReason?.setText(String.format("%s%s", getString(R.string.failed_reason), userInfo!!.backReason.toString()))
        }
        binding?.btnSubmit?.setOnClickListener(this)
        chooseWindow = CountryChooseWindow(this, CountryChooseWindow.TYPE_02, thisCountry, object : OnCountryChooseListener {
            override fun onCountryChoose(chooseWindow: CountryChooseWindow, countryCode: CountryCode?) {
                chooseWindow.dismiss()
                thisCountry = countryCode
                binding?.country?.setText(countryCode?.en)
                binding?.country?.tag = countryCode
                checkClickable()
            }
        })
        initChooseWindowData()
        checkClickable()
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.real_name_first)
    }

    override fun onClick(v: View) {
        v.requestFocus()
        hideSoftKeyboard(v)
        val i = v.id
        if (i == R.id.country) {
            chooseCountryCode()
        }
        if (i == R.id.birth){
             birthChooseDialog()
        }
        else if (i == R.id.btn_submit) {
            submitRealNameAuthenticate()
        }
    }

    private fun checkClickable() {
        binding?.btnSubmit?.isEnabled = !(binding?.country?.tag == null
                || TextUtils.isEmpty(binding?.name?.text.toString().trim { it <= ' ' })
                || TextUtils.isEmpty(binding?.identity?.text.toString().trim { it <= ' ' })
                || TextUtils.isEmpty(binding?.birth?.text.toString().trim { it <= ' ' }))
    }

    private fun birthChooseDialog() {
        val calendar: Calendar = Calendar.getInstance()
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.birth_choose, null)
        var year = calendar.get(Calendar.YEAR)
        var month = calendar.get(Calendar.MONTH) + 1
        var day = calendar.get(Calendar.DAY_OF_MONTH)
        val dialog = Dialog(mContext, R.style.AlertDialog)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            //设置背景昏暗度
            params.dimAmount = 0.2f
            params.gravity = Gravity.BOTTOM
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            //设置dialog动画
            window.setWindowAnimations(R.style.anim_bottom_in_out)
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = resources.displayMetrics
        val layoutParams = ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(contentView, layoutParams)
        dialog.show()
        val datePickerDialog: DatePicker = dialog.findViewById<DatePicker>(R.id.data_picker)

            dialog.findViewById<View>(R.id.btn_confirm).setOnClickListener { v ->
                year = datePickerDialog.year
                month = datePickerDialog.month + 1
                day = datePickerDialog.dayOfMonth
                formatDate(year, month, day)
                dialog.dismiss()
            }

        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener {  v ->
            dialog.dismiss()
        }

    }

    private  fun  formatDate(year: Int, month: Int, day: Int): String {
        return binding?.birth?.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day))
            .toString()

    }


    private fun initChooseWindowData() {
        CommonApiServiceHelper.getCountryCodeList(this, false, object : NormalCallback<HttpRequestResultDataList<CountryCode?>?>(mContext!!) {
            override fun callback(returnData: HttpRequestResultDataList<CountryCode?>?) {
                if (returnData != null && returnData.code == 0 && returnData.data != null) {
                    chooseWindow?.setCountryList(returnData.data)
                }
            }
        })
    }

    private fun chooseCountryCode() {
        chooseWindow?.show(thisCountry)
    }

    //提交实名认证进入下一步
    private fun submitRealNameAuthenticate() {
        val name = binding?.name?.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(name)) {
            FryingUtil.showToast(mContext, getString(R.string.alert_input_real_name))
            return
        }
        val identity = binding?.identity?.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(identity)) {
            FryingUtil.showToast(mContext, getString(R.string.alert_input_identity_no))
            return
        }
        val countryCode = binding?.country?.tag as CountryCode?
        if (countryCode == null) {
            FryingUtil.showToast(mContext, getString(R.string.please_choose_country))
            return
        }
        val birthCode = binding?.birth?.text.toString().trim { it <= ' ' }
        if (birthCode == "请选择出生年月") {
            FryingUtil.showToast(mContext, getString(R.string.please_choose_birth))
            return
        }
        val bundle = Bundle()
        bundle.putString(ConstData.NAME, name)
        bundle.putString(ConstData.IDENTITY_NO, identity)
        bundle.putString(ConstData.COUNTRY, countryCode.id)
        bundle.putString(ConstData.BIRTH, birthCode)
        BlackRouter.getInstance().build(RouterConstData.REAL_NAME_AUTHENTICATE_SECOND).with(bundle).go(this)
    }


}



