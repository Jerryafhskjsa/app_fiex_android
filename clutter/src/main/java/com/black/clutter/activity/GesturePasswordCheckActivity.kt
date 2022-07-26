package com.black.clutter.activity

import android.app.Activity
import android.content.Intent
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
import com.black.clutter.databinding.ActivityGestruePasswordCheckUnbackBinding
import com.black.clutter.util.GesturePasswordCirclePointChecked
import com.black.clutter.util.GesturePasswordCirclePointDefault
import com.black.lib.gesture.GesturePasswordView.GetPasswordCallBack
import com.black.lib.gesture.PointDisplay
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import skin.support.content.res.SkinCompatResources

//验证手势密码
@Route(RouterConstData.GESTURE_PASSWORD_CHECK)
class GesturePasswordCheckActivity : BaseActionBarActivity(), GetPasswordCallBack, View.OnClickListener {
    private var unBack = false
    private var nextAction: String? = null
    private var gesturePasswordFailedCount = 0
    private var c1 = 0
    private var c2 = 0

    private var binding: ActivityGestruePasswordCheckUnbackBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nextAction = intent.getStringExtra(ConstData.NEXT_ACTION)
        c1 = SkinCompatResources.getColor(this, R.color.C1)
        c2 = SkinCompatResources.getColor(this, R.color.C2)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_gestrue_password_check_unback)
        binding?.userName?.setText(CookieUtil.getUserName(mContext))
        binding?.message?.setText(R.string.gesture_show)
        val metrics = resources.displayMetrics
        val lineWeight = metrics.density * 2
        val innerPoint: PointDisplay = GesturePasswordCirclePointDefault(mContext, c2, metrics.density * 5)
        val outerPoint: PointDisplay = GesturePasswordCirclePointChecked(mContext, c1, c1, metrics.density * 5, metrics.density * 28, metrics.density * 2)
        binding?.gesturePassword?.setStyle(c1, lineWeight, innerPoint, outerPoint)
        binding?.gesturePassword?.setGetPasswordCallBack(this)
        gesturePasswordFailedCount = prefs.getInt(ConstData.GESTURE_PASSWORD_FAILED_COUNT, 0)
        binding?.forgetGesturePassword?.setOnClickListener(this)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getActionBarLayoutId(): Int {
        unBack = intent.getBooleanExtra(ConstData.CHECK_UN_BACK, false)
        return if (unBack) {
            0
        } else {
            R.layout.action_bar_left_back
        }
    }

    override fun initActionBarView(view: View) {
//        TextView headTitleView = view.findViewById(R.id.action_bar_title);
//        headTitleView.setText(R.string.gesture_password_check);
    }

    override fun routeCheck(uri: String, beforePath: String?, requestCode: Int, flags: Int, extras: Bundle?) {
        //不需要打开需要登录的目标
    }

    override fun getPassword(password: List<Any>?) {
        if (password == null || password.size < 4) {
            //绘制点数不够，提示
            FryingUtil.showToast(this, getString(R.string.gesture_at_least), FryingSingleToast.ERROR)
            //            topMessageWindow.setBackground(topMessageColorError);
//            topMessageWindow.show("请至少连接4个点");
        } else {
            val gesturePassword = FryingUtil.createGesturePassword(password)
            if (TextUtils.equals(gesturePassword, CookieUtil.getGesturePassword(mContext))) {
                //                binding?.message?.setText(R.string.gesture_check_success);
                //手势密码验证成功，完成登录
                //设置成功
                FryingUtil.showToast(this, getString(R.string.gesture_check_success), FryingSingleToast.NORMAL)
                //验证成功
                onGesturePasswordChecked()
            } else {
                gesturePasswordFailedCount++
                prefs.edit().putInt(ConstData.GESTURE_PASSWORD_FAILED_COUNT, gesturePasswordFailedCount).apply()
                if (gesturePasswordFailedCount >= ConstData.GESTURE_PASSWORD_MAX_COUNT) {
                    FryingUtil.showToast(this, getString(R.string.gesture_error_max), FryingSingleToast.ERROR)
                    //验证成功
                    clearAndLogin()
                } else {
                    FryingUtil.showToast(this, getString(R.string.gesture_error_count, ConstData.GESTURE_PASSWORD_MAX_COUNT - gesturePasswordFailedCount), FryingSingleToast.ERROR)
                    //                    topMessageWindow.setBackground(topMessageColorError);
//                    topMessageWindow.show(String.format("手势错误，还有%d次机会！", ConstData.GESTURE_PASSWORD_MAX_COUNT - gesturePasswordFailedCount));
                }
            }
        }
    }

    override fun onClear() {}
    override fun onPointChecked(position: Int, isChecked: Boolean, value: Any) {}
    //    private void refreshMessage(boolean isIllPassword) {
//        StringBuilder message = new StringBuilder();
////        if (isIllPassword) {
////            message.append(getString(R.string.gesture_6_point));
////        } else {
////            message.append("手势密码验证失败！");
////        }
//        if (gesturePasswordFailedCount <= 0) {
////            message.append(getString(R.string.gesture_paint));
//        } else if (gesturePasswordFailedCount >= ConstData.GESTURE_PASSWORD_MAX_COUNT) {
//            //手势密码失败次数超过上限，移除手势密码，并使用密码登录
//            FryingUtil.showToast(mContext, getString(R.string.gesture_error_too_more));
//            clearAndLogin();
//            return;
//        } else {
//            message.append(getString(R.string.gesture_check_failed));
//            message.append(getString(R.string.less_chance, ConstData.GESTURE_PASSWORD_MAX_COUNT - gesturePasswordFailedCount));
//        }
//        binding?.message?.setTextColor(SkinCompatResources.getInstance().getColor(this, R.color.C7));
//        binding?.message?.setText(message.toString());
//    }
    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.forget_gesture_password) {
            clearAndLogin()
        }
    }

    override fun onBackPressed() {
        if (unBack) {
        } else {
            cancelCheck()
        }
    }

    override fun onBackClick(view: View?) {
        cancelCheck()
    }

    private fun cancelCheck() {
        val intent = Intent()
        intent.putExtra(ConstData.GESTURE_PASSWORD_CHECK_RESULT, false)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun onGesturePasswordChecked() {
        //手势密码验证成功
        prefs.edit().putInt(ConstData.GESTURE_PASSWORD_FAILED_COUNT, 0).apply()
        if (!TextUtils.isEmpty(nextAction)) {
            BlackRouter.getInstance().build(nextAction)
                    .go(this) { routeResult, _ ->
                        if (routeResult) {
                            finish()
                        }
                    }
        } else {
            val intent = Intent()
            intent.putExtra(ConstData.GESTURE_PASSWORD_CHECK_RESULT, true)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun clearAndLogin() {
        if (unBack) {
            FryingUtil.clearAllUserInfo(this)
            //退回到主界面并要求登录
//        Intent startMain = new Intent(this, HomePageActivity.class);
//        startMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startMain.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        Bundle bundle = new Bundle();
//        bundle.putInt(ConstData.HOME_FRAGMENT_INDEX, 0);
//        startMain.putExtras(bundle);
//        startActivity(startMain, bundle);
            BlackRouter.getInstance().build(RouterConstData.HOME_PAGE)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .go(this) { routeResult, _ ->
                        if (routeResult) {
                            finish()
                        }
                    }
        } else {
            CookieUtil.setAccountProtectType(this, ConstData.ACCOUNT_PROTECT_NONE)
            CookieUtil.setGesturePassword(this, null)
            val intent = Intent()
            intent.putExtra(ConstData.GESTURE_PASSWORD_CHECK_RESULT, false)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}