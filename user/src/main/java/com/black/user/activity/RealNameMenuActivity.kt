package com.black.user.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActivity
import com.black.base.api.CommonApiServiceHelper
import com.black.base.api.UserApiServiceHelper
import com.black.base.model.CountryCode
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.HttpRequestResultString
import com.black.base.model.user.RealNameAIEntity
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.user.R
import com.black.user.databinding.ActivityRealNameMenuBinding
import com.black.util.Callback
import com.black.util.CallbackObject
import java.io.File
import java.util.*

@Route(value = [RouterConstData.REAL_NAME_MENU], beforePath = RouterConstData.LOGIN)
class RealNameMenuActivity : BaseActivity(), View.OnClickListener {
    private var binding: ActivityRealNameMenuBinding? = null
    private var realNameAIEntity: RealNameAIEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_real_name_menu)
        binding?.realNameAi?.setOnClickListener(this)
        binding?.realNamePerson?.setOnClickListener(this)
        initRPC()
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.real_name)
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.real_name_ai) {
//            FryingUtil.showToast(this, getString(R.string.please_waiting));
//            if (CommonUtil.isApkInDebug(this)) {
//            }
            openRealNameAI()
        } else if (i == R.id.real_name_person) {
            BlackRouter.getInstance().build(RouterConstData.REAL_NAME_AUTHENTICATE_FIRST).withRequestCode(ConstData.REAL_NAME_AUTHENTICATE).go(mContext)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ConstData.REAL_NAME_AUTHENTICATE) {
                finish()
            }
        }
    }

    //初始化创蓝接口
    private fun initRPC() {
//        RPCSDKManager.getInstance().setModifiedIdcardMsg(false, false, false)
//        val list: MutableList<LivenessTypeEnum> = ArrayList()
//        list.add(LivenessTypeEnum.Eye)
//        RPCSDKManager.getInstance().setLivenessTypeEnum(list)
//        RPCSDKManager.getInstance().setRPCLister(object : IRPCLister {
//            override fun onFail(code: String, failMsg: String) { //Log.e("UUU", "onFail : code=" + code + "，failMsg=" + failMsg);
////        if (code.equals("1007") || code.equals("1008")) {
////            Intent it = new Intent(activity, VerificationActivity.class);
////            it.putExtra("score", "0");
////            activity.startActivity(it);
////        }
//            }
//
//            override fun onVerifaction(code: String, score: String) {
//                realNameAIEntity?.score = score
//                //Log.e("UUU", "onVerifaction : code=" + code + "，score=" + score);
//                startVerification()
//            }
//
//            override fun onIdentityCardAuth(code: String, msg: String) { //Log.e("UUU", "onIdentityCardAuth : code=" + code + "，msg=" + msg);
////        try {
////            JSONObject object = new JSONObject(msg);
////            JSONObject data = object.getJSONObject("data");
////            String result = data.optString("result");
////            if (result.equals("01")) {
////                //返回结果 01-认证一致(收费) 02-认证不一致(收费) 03-认证不确定（不收费） 04-认证失败(不收费)
////                startVerification();
////            } else {
////                Intent it = new Intent(activity, VerificationActivity.class);
////                it.putExtra("score", "0");
////                activity.startActivity(it);
////            }
////                : code=1000picVerifiedMap={Eye=/storage/emulated/0/ocr/1567920999337.jpg,
////                rpc_sdk_idcard_front=/storage/emulated/0/ocr/1567921039997.jpg,
////                rpc_sdk_idcard_back=/storage/emulated/0/ocr/1567921057046.jpg,
////                bestImage0=/storage/emulated/0/ocr/1567920999371.jpg}
////                {"chargeStatus":1,"message":"成功","data":
////                {"remark":"","faceMatchData":{"tradeNo":"19090813382577053",
////                "score":"93.40253448","remark":"成功","code":"0"}},"code":"200000"}
////        } catch (JSONException e) {
////            e.printError();
////        }
//            }
//
//            override fun onVerifiedPic(code: String, picVerifiedMap: Map<String, String>) { //Log.e("UUU", "onVerifiedPic : code=" + code + "picVerifiedMap=" + picVerifiedMap);
//                if (picVerifiedMap.containsKey("rpc_sdk_idcard_front")) {
//                    realNameAIEntity?.picPathList?.add(picVerifiedMap["rpc_sdk_idcard_front"])
//                }
//                if (picVerifiedMap.containsKey("rpc_sdk_idcard_back")) {
//                    realNameAIEntity?.picPathList?.add(picVerifiedMap["rpc_sdk_idcard_back"])
//                }
//                if (picVerifiedMap.containsKey("bestImage0")) {
//                    realNameAIEntity?.picPathList?.add(picVerifiedMap["bestImage0"])
//                }
//            }
//
//            override fun onIdcardMsg(code: String, IdcardFront: String, IdcardBack: String) {
//                //ocr返回的身份证信息
//                //Log.e("UUU", "ocr识别结果 : IdcardFront=" + IdcardFront + "IdcardBack=" + IdcardBack);
//                //ocr识别结果 : IdcardFront={"chargeStatus":1,"message":"成功","data":
//                // {"tradeNo":"19090814145295017","code":"0","riskType":"normal","address":"成都市武侯区武侯祠大街264号附1号","birth":"19870421","payeeName":"何闵","cardNum":"51162219870421281X","sex":"男","nation":"汉"},"code":"200000"}
//                // IdcardBack={"chargeStatus":1,"message":"成功","data":{"tradeNo":"19090814145295027","code":"0","issuingDate":"20160524","issuingAuthority":"成都市公安局武侯区分局","expiryDate":"20360524"},"code":"200000"}
//                //正面获取姓名和身份证号码，背面获取 tradeNo
//                val result = gson.fromJson<RealNameAIResult<RealNameAIFront?>>(IdcardFront, object : TypeToken<RealNameAIResult<RealNameAIFront?>?>() {}.type)
//                if (result?.data != null) {
//                    realNameAIEntity?.name = result.data?.name
//                    realNameAIEntity?.idNo = result.data?.cardNum
//                }
//            }
//
//            override fun onIdcardModifiedMsg(code: String, IdcardFront: String, IdcardBack: String) {
//                //修改后的身份证信息
//                //Log.e("UUU", "修改后结果 : IdcardFront=" + IdcardFront + "IdcardBack=" + IdcardBack);
//                val result = gson.fromJson<RealNameAIResult<RealNameAIFront?>>(IdcardFront, object : TypeToken<RealNameAIResult<RealNameAIFront?>?>() {}.type)
//                if (result?.data != null) {
//                    realNameAIEntity?.name = result.data?.name
//                    realNameAIEntity?.idNo = result.data?.cardNum
//                }
//            }
//        })
    }

    var upLoadCount = 0
    private fun startVerification() { // 根据识别结果进行后续业务
//        RPCSDKManager.getInstance().finishActivity()
//        getCountyId(object : CallbackObject<String?>() {
//            override fun callback(countyId: String?) {
//                val pathList: List<String>? = realNameAIEntity?.picPathList
//                val needUploadCount = pathList?.size ?: 0
//                upLoadCount = 0
//                val imgUrlList: MutableList<String?> = ArrayList(3)
//                upLoadPhoto(pathList, object : Callback<String?>() {
//                    override fun callback(result: String?) {
//                        upLoadCount++
//                        imgUrlList.add(result)
//                        if (upLoadCount >= needUploadCount) {
//                            val imageStrings = StringBuilder()
//                            for (imgUrl in imgUrlList) {
//                                if (imgUrl == null) {
//                                    continue
//                                }
//                                if (imageStrings.isEmpty()) {
//                                    imageStrings.append(imgUrl)
//                                } else {
//                                    imageStrings.append(",").append(imgUrl)
//                                }
//                            }
//                            UserApiServiceHelper.bindIdentityAI(mContext, realNameAIEntity?.name, realNameAIEntity?.idNo, countyId, imageStrings.toString(), realNameAIEntity!!.score, object : NormalCallback<HttpRequestResultString?>() {
//                                override fun callback(returnData: HttpRequestResultString?) {
//                                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
//                                        FryingUtil.showToast(mContext, getString(R.string.submit_success))
//                                        //提交成功后回到个人中心
//                                        BlackRouter.getInstance().build(RouterConstData.PERSON_INFO_CENTER)
//                                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                                                .go(mContext) { routeResult, _ ->
//                                                    if (routeResult) {
//                                                        finish()
//                                                    }
//                                                }
//                                    } else {
//                                        FryingUtil.showToast(mContext, returnData?.msg)
//                                    }
//                                }
//                            })
//                        }
//                    }
//
//                    override fun error(type: Int, error: Any) {}
//                })
//            }
//        })
    }

    private fun openRealNameAI() {
//        RPCSDKManager.getInstance().getTiker(object : TikerCallBack {
//            override fun onSuccess(s: String) {
//                requestPermission(Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE, Permission.READ_PHONE_STATE)
//            }
//
//            override fun onFail(code: String, failMsg: String) { //Log.e("UUU", "getTiker onFail : code=" + code + ", failMsg=" + failMsg);
//            }
//        })
    }

    private fun upLoadPhoto(pathList: List<String>?, callBack: Callback<String?>) {
        if (pathList == null) {
            return
        }
        for (path in pathList) {
            //先上传图片，根据返回的路径，进行验证
            val fileParams: MutableMap<String, File> = HashMap()
            fileParams["file"] = File(path)
            UserApiServiceHelper.upload(mContext, "file", File(path), object : NormalCallback<HttpRequestResultString?>() {
                override fun callback(returnData: HttpRequestResultString?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        //上传成功
                        callBack.callback(returnData.data)
                    } else {
                        FryingUtil.showToast(mContext, returnData?.msg)
                    }
                }
            })
        }
    }

    private fun requestPermission(vararg permissions: String) {
//        AndPermission.with(this)
//                .permission(*permissions)
//                .onGranted {
//                    realNameAIEntity = RealNameAIEntity()
//                    RPCSDKManager.getInstance().startAuthentication(this@RealNameMenuActivity)
//                }
//                .onDenied {
//                    //Log.e("UUU", "onDenied : permissions=" + permissions);
//                }
//                .start()
    }

    private fun getCountyId(callback: CallbackObject<String?>) {
        CommonApiServiceHelper.getCountryCodeList(this, true, object : Callback<HttpRequestResultDataList<CountryCode?>?>() {
            override fun error(type: Int, error: Any) {
                callback.callback(null)
            }

            override fun callback(returnData: HttpRequestResultDataList<CountryCode?>?) {
                if (returnData != null && returnData.code == 0 && returnData.data != null) {
                    for (countryCode in returnData.data!!) {
                        if (countryCode?.code != null && countryCode.code!!.endsWith("86")) {
                            callback.callback(countryCode.id)
                            return
                        }
                    }
                }
                callback.callback(null)
            }
        })
    }
}