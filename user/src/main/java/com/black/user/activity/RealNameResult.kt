package com.black.user.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActivity
import com.black.base.model.user.RealNameAIEntity
import com.black.base.model.user.UserInfo
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.user.R
import com.black.user.databinding.ActivityRealNameResultBinding


@Route(value = [RouterConstData.REAL_NAME_RESULT])
class RealNameResult: BaseActivity(), View.OnClickListener {
    private var binding: ActivityRealNameResultBinding? = null
    private var userInfo: UserInfo? = null
    private var realNameAIEntity: RealNameAIEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_real_name_result)
        userInfo = CookieUtil.getUserInfo(this)
        binding?.weirenzheng?.setOnClickListener(this)
        if (TextUtils.equals(userInfo!!.idNoStatus, "1")) {
            binding?.tongguo?.visibility = View.VISIBLE
        } else if (TextUtils.equals(userInfo!!.idNoStatus, "2")) {
           binding?.shenhezhong?.visibility =View.VISIBLE
        }
        else if (TextUtils.equals(userInfo!!.idNoStatus, "0")) {
            binding?.weirenzheng?.visibility =View.VISIBLE
        }else {
            binding?.weitongguo?.visibility =View.VISIBLE
        }
    }
    override fun onResume() {
        super.onResume()
        userInfo = CookieUtil.getUserInfo(this)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.real_name)
    }

    override fun onClick(v: View) {
        val i = v.id
        when (i) {
            R.id.weirenzheng -> {
                BlackRouter.getInstance().build(RouterConstData.REAL_NAME_AUTHENTICATE_FIRST).go(mContext)
            }
        }
    }
}