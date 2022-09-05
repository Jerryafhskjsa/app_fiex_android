package com.black.clutter.activity

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.BaseApplication
import com.black.base.activity.BaseActivity
import com.black.base.api.CommonApiService
import com.black.base.manager.ApiManager
import com.black.base.model.clutter.NoticeHome
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.RouterConstData
import com.black.base.util.RxJavaHelper
import com.black.clutter.BR
import com.black.clutter.R
import com.black.clutter.adapter.NotificationAdapter
import com.black.clutter.databinding.ActivityNotificationListBinding
import com.black.router.annotation.Route
import com.black.util.Callback
import io.reactivex.Observable

@Route(value = [RouterConstData.NOTIFICATION_LIST])
class NotificationListActivity : BaseActivity(), View.OnClickListener {
    private var application: BaseApplication? = null
    private var binding: ActivityNotificationListBinding? = null
    private var adapter:NotificationAdapter? = null
    private var dataList:ArrayList<NoticeHome.NoticeHomeItem?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        application = getApplication() as BaseApplication
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notification_list)
//        binding?.chinese?.setOnClickListener(this)
//        binding?.chinese?.tag = application!!.getChinese()
//        binding?.english?.setOnClickListener(this)
//        binding?.english?.tag = application!!.getEnglish()
    }

    //获取公告信息
    fun getNoticeInfo() {
        ApiManager.build(mContext!!)
            .getService(CommonApiService::class.java)
//                ?.getNoticeHome(FryingUtil.getLanguageKey(context!!), 6, 1)
            ?.getNoticeHome("zh-tw", 6, 1)
            ?.flatMap { noticeHomeResult ->
                if (noticeHomeResult.articles != null && noticeHomeResult.articles!!.isNotEmpty()) {
                    dataList = noticeHomeResult.articles
                }
                Observable.just(noticeHomeResult)
            }
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(mContext, false, object : Callback<NoticeHome?>() {
            override fun callback(returnData: NoticeHome?) =
                if (returnData?.articles != null && returnData.articles!!.isNotEmpty()) {
                    val noticeList = returnData.articles
                    val layoutManager = LinearLayoutManager(mContext)
                    layoutManager.orientation = RecyclerView.VERTICAL
                    layoutManager.isSmoothScrollbarEnabled = true
                    binding?.recyclerView?.layoutManager = layoutManager
                    adapter = NotificationAdapter(mContext,BR.notificationListModel ,noticeList)
                    binding?.recyclerView?.adapter = adapter
                    binding?.recyclerView?.isNestedScrollingEnabled = false
                    binding?.recyclerView?.setEmptyView(binding?.emptyView?.root)
                    //解决数据加载不完的问题
                    binding?.recyclerView?.isNestedScrollingEnabled = false
                    binding?.recyclerView?.setHasFixedSize(true)
                    //解决数据加载完成后, 没有停留在顶部的问题
                    binding?.recyclerView?.isFocusable = false
                } else {
                }
            override fun error(type: Int, error: Any?) {
            }
        }))
    }

    override fun onResume() {
        super.onResume()
        getNoticeInfo()
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.notification)
    }

    override fun onClick(v: View) {
        val i = v.id
    }
}