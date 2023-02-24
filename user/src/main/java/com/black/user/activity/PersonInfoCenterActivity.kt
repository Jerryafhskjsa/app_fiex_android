package com.black.user.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActivity
import com.black.base.api.UserApiServiceHelper
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.model.user.UserInfo
import com.black.base.util.*
import com.black.base.view.ImageSelectorHelper
import com.black.base.view.ImageSelectorHelper.OnImageGetListener
import com.black.im.util.IMHelper
import com.black.lib.permission.PermissionHelper
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.user.R
import com.black.user.databinding.ActivityPersonInfoCenterBinding
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.Size
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import skin.support.content.res.SkinCompatResources
import java.io.File

@Route(value = [RouterConstData.PERSON_INFO_CENTER], beforePath = RouterConstData.LOGIN)
class PersonInfoCenterActivity : BaseActivity(), View.OnClickListener, OnImageGetListener {
    private companion object {
        private var C1 = 0
        private var C2 = 0
        private var C3 = 0
        private var C5 = 0
        private var T1 = 0
        private var T2 = 0
        private var T5 = 0
    }

    private var userInfo: UserInfo? = null
    private var binding: ActivityPersonInfoCenterBinding? = null
    private var imageSelectorHelper: ImageSelectorHelper? = null
    private var imageLoader: ImageLoader? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        C1 = SkinCompatResources.getColor(mContext, R.color.C1)
        C2 = SkinCompatResources.getColor(mContext, R.color.C2)
        C3 = SkinCompatResources.getColor(mContext, R.color.C3)
        C5 = SkinCompatResources.getColor(mContext, R.color.C5)
        T1 = SkinCompatResources.getColor(mContext, R.color.T1)
        T2 = SkinCompatResources.getColor(mContext, R.color.T2)
        T5 = SkinCompatResources.getColor(mContext, R.color.T5)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_person_info_center)
        binding?.iconAvatar?.setOnClickListener(this)
        binding?.nickNameLayout?.setOnClickListener(this)
        binding?.state?.setOnClickListener(this)
        imageSelectorHelper = ImageSelectorHelper(this)
        imageSelectorHelper!!.addOnImageGetListener(this)
        imageLoader = ImageLoader(this)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.personal_center)
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.icon_avatar) {
            val userIdHeader = IMHelper.getUserIdHeader(mContext)
            IMHelper.imLogin(mContext, userIdHeader + userInfo!!.id, object : Callback<Boolean?>() {
                override fun callback(returnData: Boolean?) {
                    if (returnData != null) {
                        imageSelectorHelper!!.showSelectPhoto(mContext as Activity, mContext as PermissionHelper, Size(480, 480))
                    } else {
                        FryingUtil.showToast(mContext, "初始化失败，稍后重试！")
                    }
                }

                override fun error(type: Int, error: Any) { FryingUtil.showToast(mContext, "头像同步失败")}
            })
        } else if (i == R.id.nick_name_layout) {
            BlackRouter.getInstance().build(RouterConstData.NICK_NAME_CHANGE).go(mContext)
        } else if (i == R.id.state) {
            if (userInfo == null) {
                BlackRouter.getInstance().build(RouterConstData.LOGIN).go(mContext)
            } else {
                if (TextUtils.equals(userInfo!!.idNoStatus, "1")) {
//                    requestCameraPermissions(Runnable {
//                        requestStoragePermissions(Runnable {
//                            BlackRouter.getInstance().build(RouterConstData.REAL_NAME_MENU).go(mContext);
//                        })
//                    })
                } else if (TextUtils.equals(userInfo!!.idNoStatus, "2")) {
                    FryingUtil.showToast(mContext, getString(R.string.checking_waiting))
                } else {
                    requestCameraPermissions(Runnable {
                        requestStoragePermissions(Runnable {
                            BlackRouter.getInstance().build(RouterConstData.REAL_NAME_MENU).go(mContext)
                        })
                    })
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        userInfo = CookieUtil.getUserInfo(this)
        if(userInfo != null){
            refreshView()
        }else{
            getUserInfo(object : NormalCallback<UserInfo?>(mContext!!) {
                override fun error(type: Int, error: Any?) {
                    super.error(type, error)
                    finish()
                }
                override fun callback(returnData: UserInfo?) {
                    userInfo = returnData
                    refreshView()
                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imageSelectorHelper!!.onActivityResult(this, requestCode, resultCode, data)
    }

    override fun onImageGet(path: String?) {
        UserApiServiceHelper.uploadPublic(mContext, "file", File(path), object : NormalCallback<HttpRequestResultString?>(mContext!!) {
            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) { //上传成功
                    var avatarUrl:String? = returnData.data
                    if(avatarUrl != null){
                        if (!avatarUrl.startsWith("http")){
                            avatarUrl = UrlConfig.getHost(mContext) + avatarUrl
                        }
                    }
                    UserApiServiceHelper.modifyUserInfo(mContext, avatarUrl, null, object : NormalCallback<HttpRequestResultString?>(mContext!!) {
                        override fun callback(modifyResult: HttpRequestResultString?) {
                            if (modifyResult != null && modifyResult.code == HttpRequestResult.SUCCESS) {
//                                val bitmap = ImageSelectorHelper.getImageAndSave(path, currentImagePath)
//                                binding?.iconAvatar?.setImageBitmap(bitmap)
                                getUserInfo(object : NormalCallback<UserInfo?>(mContext!!) {
                                    override fun callback(returnData: UserInfo?) {
                                        userInfo = returnData
                                        FryingUtil.showToast(mContext, getString(R.string.image_success))
                                        refreshView()
                                    }
                                })
                            } else {
                                FryingUtil.showToast(mContext, modifyResult?.msg)
                            }
                        }
                    })
//                    IMHelper.updateAvatar(avatarUrl, object : Callback<Boolean?>() {
//                        override fun error(type: Int, error: Any) {
//                            FryingUtil.showToast(mContext, "头像同步失败")
//                        }
//
//                        override fun callback(returnData: Boolean?) {
//                            FryingUtil.showToast(mContext, "头像同步成功")
//                        }
//                    })
                } else {
                    FryingUtil.showToast(mContext, returnData?.msg)
                }
            }
        })
    }

    private val currentImagePath: String
        get() = CommonUtil.getCatchFilePath(this) + File.separator + ConstData.TEMP_IMG_NAME_01

    private fun refreshView() {
        if (userInfo == null) {
            return
        }
        binding?.nickName?.text = if (userInfo == null || userInfo!!.nickname == null) "" else userInfo!!.nickname
        if(userInfo?.headPortrait != null){
            binding?.iconAvatar?.let {
                Glide.with(mContext)
                    .load(Uri.parse(userInfo?.headPortrait!!))
                    .apply(RequestOptions.bitmapTransform(CircleCrop()).error(R.drawable.icon_avatar))
                    .into(it)
            }
        }
        if (TextUtils.equals(userInfo!!.idNoStatus, "1")) {
            //已认证
            binding?.stateText?.setText(R.string.person_checked)
            binding?.stateText?.setTextColor(T1)
            CommonUtil.setTextViewCompoundDrawable(binding?.stateText, null, 2)
            binding?.name?.text = userInfo!!.realName
            binding?.name?.setTextColor(T1)
            binding?.account?.text = userInfo!!.displayName
            binding?.uid?.text = if (userInfo!!.id == null) "" else userInfo!!.id
            val language = LanguageUtil.getLanguageSetting(this)
            val isChinese = language == null || language.languageCode != 4
            val countryName = if (isChinese) if (userInfo!!.countryZh == null) getString(R.string.unknown) else userInfo!!.countryZh else if (userInfo!!.countryEn == null) getString(R.string.unknown) else userInfo!!.countryEn
            binding?.country?.text = countryName
            binding?.country?.setTextColor(T1)
            binding?.identity?.text = userInfo!!.idNo
            binding?.identity?.setTextColor(T1)
        } else {
            CommonUtil.setTextViewCompoundDrawable(binding?.stateText, SkinCompatResources.getDrawable(this, R.drawable.icon_left), 2)
            when {
                TextUtils.equals(userInfo!!.idNoStatus, "2") -> {
                    binding?.stateText?.setText(R.string.in_audit)
                }
                TextUtils.equals(userInfo!!.idNoStatus, "3") -> {
                    binding?.stateText?.setText(R.string.audit_failed)
                }
                else -> {
                    binding?.stateText?.setText(R.string.person_check_imd)
                }
            }
            binding?.stateText?.setTextColor(T5)
            binding?.name?.setText(R.string.person_unchecked)
            binding?.name?.setTextColor(T2)
            binding?.account?.text = userInfo!!.displayName
            binding?.uid?.text = if (userInfo!!.id == null) "" else userInfo!!.id
            binding?.country?.setText(R.string.person_unchecked)
            binding?.country?.setTextColor(T2)
            binding?.identity?.setText(R.string.person_unchecked)
            binding?.identity?.setTextColor(T2)
        }
    }
}