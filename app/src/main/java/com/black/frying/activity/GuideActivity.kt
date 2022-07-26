package com.black.frying.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.black.base.activity.BaseActivity
import com.black.base.util.ConstData
import com.black.base.util.CookieUtil
import com.black.base.util.RouterConstData
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.CommonUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ActivityGuideBinding
import java.util.*

@Route(value = [RouterConstData.GUID])
class GuideActivity : BaseActivity() {
    private var binding: ActivityGuideBinding? = null
    private val viewList: MutableList<View> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_guide)
        binding?.guideViewPager?.adapter = GuidePagerAdapter()
        binding?.guideViewPager?.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                refreshIndicatorLayout(position)
            }
        })
        initViewList()
    }

    private fun refreshIndicatorLayout(selectedIndex: Int) {
        val radioButton = binding?.guideIndicator?.getChildAt(selectedIndex) as RadioButton?
        if (radioButton != null) {
            radioButton.isChecked = true
        }
    }

    private fun initViewList() {
        viewList.clear()
        binding?.guideIndicator?.removeAllViews()
        val inflater = layoutInflater
        viewList.add(inflater.inflate(R.layout.activity_guide_01, null))
        viewList.add(inflater.inflate(R.layout.activity_guide_02, null))
        val view3 = inflater.inflate(R.layout.activity_guide_03, null)
        viewList.add(view3)
        val view4 = inflater.inflate(R.layout.activity_guide_04, null)
        view4.setOnClickListener { gotoMainWorkActivity() }
        viewList.add(view4)
        for (i in viewList.indices) {
            binding?.guideIndicator?.addView(inflater.inflate(R.layout.view_guide_slider_dot, null))
        }
        binding?.guideViewPager?.adapter?.notifyDataSetChanged()
        binding?.guideViewPager?.currentItem = 0
        refreshIndicatorLayout(0)
    }

    override fun gotoMainWorkActivity() {
        var routerPath = ""
        routerPath = RouterConstData.HOME_PAGE
        if (!TextUtils.isEmpty(CookieUtil.getToken(mContext))) { //快捷登录必须有token
            val protectType = CookieUtil.getAccountProtectType(mContext)
            if (protectType == ConstData.ACCOUNT_PROTECT_GESTURE) { //验证手势面是否存在
                if (!TextUtils.isEmpty(CookieUtil.getGesturePassword(mContext))) {
                    routerPath = RouterConstData.GESTURE_PASSWORD_CHECK
                }
            } else if (protectType == ConstData.ACCOUNT_PROTECT_FINGER) {
                val fingerPrintStatus = CommonUtil.getFingerPrintStatus(this)
                if (fingerPrintStatus != 1) {
                    CookieUtil.setAccountProtectType(this, ConstData.ACCOUNT_PROTECT_NONE)
                } else {
                    routerPath = RouterConstData.FINGER_PRINT_CHECK
                }
            }
        }
        val bundle = Bundle()
        bundle.putBoolean(ConstData.CHECK_UN_BACK, true)
        bundle.putString(ConstData.NEXT_ACTION, RouterConstData.HOME_PAGE)
        BlackRouter.getInstance().build(routerPath).with(bundle).go(this) { routeResult, _ ->
            if (routeResult) {
                finish()
            }
        }
        //        Intent openMainActivity = new Intent(mContext, HomePageActivity.class);
//        startActivity(openMainActivity);
//        Class c = null;
//        if (!TextUtils.isEmpty(CookieUtil.getToken(mContext))) {
//            //快捷登录必须有token
//            int protectType = CookieUtil.getAccountProtectType(mContext);
//            if (protectType == 1) {
//                //验证手势面是否存在
//                if (!TextUtils.isEmpty(CookieUtil.getGesturePassword(mContext))) {
//                    routerPath = RouterConstData.HOME_PAGE;
//                    c = GesturePasswordCheckActivity.class;
//                }
//            }
//        }
//        if(c != null){
//            Intent passwordIntent = new Intent(mContext, c);
//            startActivity(passwordIntent);
//        }
//        finish();
//        overridePendingTransition(0, 0);
    }

    internal inner class GuidePagerAdapter : PagerAdapter() {
        override fun getCount(): Int {
            return viewList.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = viewList[position]
            // 设置Item的点击监听器
//            view.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                }
//            });
            container.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(viewList[position])
        }
    }
}