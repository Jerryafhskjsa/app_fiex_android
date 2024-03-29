package com.tencent.qcloud.tim.uikit2.modules.group.member;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.tencent.qcloud.tim.uikit2.R;
import com.tencent.qcloud.tim.uikit2.base.BaseFragment;
import com.tencent.qcloud.tim.uikit2.modules.group.info.GroupInfo;
import com.tencent.qcloud.tim.uikit2.utils.TUIKitConstants;


public class GroupMemberInviteFragment extends BaseFragment {

    private GroupMemberInviteLayout mInviteLayout;
    private View mBaseView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.group_fragment_invite_members, container, false);
        mInviteLayout = mBaseView.findViewById(R.id.group_member_invite_layout);
        mInviteLayout.setParentLayout(this);
        init();
        return mBaseView;
    }

    private void init() {
        mInviteLayout.setDataSource((GroupInfo) getArguments().getSerializable(TUIKitConstants.Group.GROUP_INFO));
        mInviteLayout.getTitleBar().setOnLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backward();
            }
        });
    }
}
