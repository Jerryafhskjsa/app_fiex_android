package com.black.im.activity

import android.app.Activity
import android.os.Bundle
import com.black.im.R
import com.black.im.util.TUIKitConstants
import com.black.im.widget.FriendProfileLayout

class GroupApplyMemberActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.group_apply_member_activity)
        val layout: FriendProfileLayout = findViewById(R.id.friend_profile)
        layout.initData(intent.getSerializableExtra(TUIKitConstants.ProfileType.CONTENT))
    }
}