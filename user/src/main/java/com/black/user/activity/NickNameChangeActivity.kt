package com.black.user.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.UserApiServiceHelper
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.model.user.UserInfo
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.im.util.IMHelper
import com.black.net.HttpRequestResult
import com.black.router.annotation.Route
import com.black.user.R
import com.black.user.databinding.ActivityNickNameChangeBinding
import com.black.util.Callback

@Route(value = [RouterConstData.NICK_NAME_CHANGE], beforePath = RouterConstData.LOGIN)
class NickNameChangeActivity : BaseActionBarActivity(), View.OnClickListener {
    private var userInfo: UserInfo? = null

    private var binding: ActivityNickNameChangeBinding? = null

    private val watcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            checkClickable()
        }

        override fun afterTextChanged(s: Editable) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userInfo = CookieUtil.getUserInfo(this)
        if (userInfo == null) {
            finish()
            return
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_nick_name_change)

        binding?.nickName?.addTextChangedListener(watcher)
        binding?.btnModify?.setOnClickListener(this)
        binding?.nickName?.setText(userInfo!!.nickname)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return "修改昵称"
    }

    override fun onClick(v: View) {
        if (v.id == R.id.btn_modify) {
            val nickName = binding?.nickName?.text.toString()
            if (TextUtils.isEmpty(nickName)) {
                FryingUtil.showToast(this, "请输入昵称")
            } else {
                val userIdHeader = IMHelper.getUserIdHeader(mContext)
                IMHelper.imLogin(mContext, userIdHeader + userInfo!!.id, object : Callback<Boolean?>() {
                    override fun callback(returnData: Boolean?) {
                        if (returnData != null) {
                            UserApiServiceHelper.modifyUserInfo(mContext, null, nickName, object : NormalCallback<HttpRequestResultString?>(mContext!!) {
                                override fun callback(modifyResult: HttpRequestResultString?) {
                                    if (modifyResult != null && modifyResult.code == HttpRequestResult.SUCCESS) {
                                        getUserInfo(object : NormalCallback<UserInfo?>(mContext!!) {
                                            override fun error(type: Int, error: Any?) {}
                                            override fun callback(returnData: UserInfo?) {
                                                finish()
                                            }
                                        })
                                    } else {
                                        FryingUtil.showToast(mContext, modifyResult?.msg)
                                    }
                                }
                            })
                            IMHelper.updateNickName(nickName, object : Callback<Boolean?>() {
                                override fun error(type: Int, error: Any) {
                                    FryingUtil.showToast(mContext, "昵称同步失败")
                                }

                                override fun callback(returnData: Boolean?) {
                                    FryingUtil.showToast(mContext, "昵称同步成功")
                                }
                            })

                        } else{
                            FryingUtil.showToast(mContext, "初始化失败，稍后重试！")
                        }
                    }

                    override fun error(type: Int, error: Any) {}
                })
            }
        }
    }

    private fun checkClickable() {
        binding?.btnModify?.isEnabled = !TextUtils.isEmpty(binding?.nickName?.text.toString().trim { it <= ' ' })
    }
}