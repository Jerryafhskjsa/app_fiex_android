package com.black.im.activity

import android.app.Activity
import android.os.Bundle
import com.black.im.R
import com.black.im.fragment.GroupInfoFragment

class GroupInfoActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.group_info_activity)
        val fragment = GroupInfoFragment()
        fragment.arguments = intent.extras
        fragmentManager.beginTransaction().replace(R.id.group_manager_base, fragment).commitAllowingStateLoss()
    }

    override fun finish() {
        super.finish()
        setResult(1001)
    }
}