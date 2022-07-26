package com.black.clutter.activity

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.lib.FryingSingleToast
import com.black.base.util.ConstData
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.clutter.R
import com.black.clutter.databinding.ActivityGestruePasswordSettingBinding
import com.black.clutter.util.GesturePasswordCirclePointChecked
import com.black.clutter.util.GesturePasswordCirclePointDefault
import com.black.lib.gesture.GesturePasswordView.GetPasswordCallBack
import com.black.lib.gesture.PointDisplay
import com.black.router.annotation.Route
import skin.support.content.res.SkinCompatResources

//设置手势密码
@Route(value = [RouterConstData.GESTURE_PASSWORD_SETTING])
class GesturePasswordSettingActivity : BaseActionBarActivity(), GetPasswordCallBack, View.OnClickListener {
    private var unBack = false
    private var gesturePassword: String? = null
    private val status = 1
    private var clickSource: Bundle? = null
    private var c1 = 0
    private var c2 = 0

    private var binding: ActivityGestruePasswordSettingBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        clickSource = intent.extras
        c1 = SkinCompatResources.getColor(this, R.color.C1)
        c2 = SkinCompatResources.getColor(this, R.color.C2)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_gestrue_password_setting)
        val metrics = resources.displayMetrics
        val lineWeight = metrics.density * 2
        val innerPoint: PointDisplay = GesturePasswordCirclePointDefault(mContext, c2, metrics.density * 5)
        val outerPoint: PointDisplay = GesturePasswordCirclePointChecked(mContext, c1, c1, metrics.density * 5, metrics.density * 28, metrics.density * 2)
        binding?.gesturePassword?.setStyle(c1, lineWeight, innerPoint, outerPoint)
        binding?.gesturePassword?.setGetPasswordCallBack(this)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getActionBarLayoutId(): Int {
        unBack = intent.getBooleanExtra(ConstData.CHECK_UN_BACK, false)
        return R.layout.action_bar_left_back
    }

    override fun initActionBarView(view: View) {
//        TextView headTitleView = view.findViewById(R.id.action_bar_title);
//        String title = getIntent().getStringExtra(ConstData.TITLE);
//        headTitleView.setText(TextUtils.isEmpty(title) ? getString(R.string.gesture_setting) : title);
//        TextView cancelView = view.findViewById(R.id.action_bar_extras);
//        cancelView.setText(R.string.jump);
//        cancelView.setOnClickListener(this);
    }

    override fun onBackClick(view: View?) {
        gotoNext(Activity.RESULT_CANCELED)
    }

    override fun onBackPressed() {
        gotoNext(Activity.RESULT_CANCELED)
    }

    override fun getPassword(password: List<Any>?) {
        if (password == null || password.size < 4) {
            //绘制点数不够，提示
            FryingUtil.showToast(this, getString(R.string.gesture_at_least), FryingSingleToast.ERROR)
        } else {
            if (gesturePassword == null) {
                gesturePassword = FryingUtil.createGesturePassword(password)
                binding?.message!!.setText(R.string.gesture_again)
            } else {
                val newGesturePassword = FryingUtil.createGesturePassword(password)
                if (TextUtils.equals(gesturePassword, newGesturePassword)) {
                    //设置成功
                    FryingUtil.showToast(this, getString(R.string.gesture_set_success), FryingSingleToast.NORMAL)
                    //设置成功
                    CookieUtil.setGesturePassword(mContext, gesturePassword)
                    CookieUtil.setAccountProtectType(mContext, 1)
                    prefs.edit().putInt(ConstData.GESTURE_PASSWORD_FAILED_COUNT, ConstData.ACCOUNT_PROTECT_GESTURE).apply()
                    gotoNext(Activity.RESULT_OK)
                } else {
                    FryingUtil.showToast(this, getString(R.string.gesture_not_same), FryingSingleToast.ERROR)
                }
            }
        }
    }

    override fun onClear() {}
    override fun onPointChecked(position: Int, isChecked: Boolean, value: Any) {}
    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.action_bar_extras) {
            //跳过设置手势密码
            if (clickSource != null && clickSource!!.getInt(ConstData.OPEN_TYPE) == 1) {
                //                CookieUtil.setAccountProtectJump(this, true);
            }
            gotoNext(Activity.RESULT_CANCELED)
        }
    }

    private fun gotoNext(result: Int) {
        var forResult = false
        val bundle = Bundle()
        if (clickSource != null) {
            forResult = clickSource!!.getBoolean(ConstData.FOR_RESULT)
            bundle.putAll(clickSource)
        }
        if (forResult && !unBack) {
            setResult(result)
        } else {
            //            BlackRouter.getInstance().build(RouterConstData.HOME_PAGE)
//                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
//                    .with(bundle).go(this);
            setResult(result)
        }
        finish()
    }
}