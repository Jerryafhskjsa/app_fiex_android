package com.black.im.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.black.im.R
import com.black.im.databinding.GroupApplyManagerActivityBinding
import com.black.im.model.group.GroupApplyInfo
import com.black.im.model.group.GroupInfo
import com.black.im.util.TUIKitConstants

class GroupApplyManagerActivity : Activity() {
    private var mGroupInfo: GroupInfo? = null
    private var binding: GroupApplyManagerActivityBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.group_apply_manager_activity)
        if (intent.extras == null) {
            finish()
            return
        }
        mGroupInfo = intent.extras!!.getSerializable(TUIKitConstants.Group.GROUP_INFO) as GroupInfo
        binding?.groupApplyManagerLayout?.setDataSource(mGroupInfo)
    }

    public override fun onResume() {
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != TUIKitConstants.ActivityRequest.CODE_1) {
            return
        }
        if (resultCode != RESULT_OK) {
            return
        }
        val info = data?.getSerializableExtra(TUIKitConstants.Group.MEMBER_APPLY) as GroupApplyInfo?
                ?: return
        binding?.groupApplyManagerLayout?.updateItemData(info)
    }
}