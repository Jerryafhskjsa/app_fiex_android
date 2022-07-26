package com.tencent.qcloud.tim.uikit2.modules.chat.base;

import com.tencent.qcloud.tim.uikit2.base.BaseFragment;
import com.tencent.qcloud.tim.uikit2.modules.chat.interfaces.IChatLayout;

public class BaseInputFragment extends BaseFragment {

    private IChatLayout mChatLayout;

    public IChatLayout getChatLayout() {
        return mChatLayout;
    }

    public BaseInputFragment setChatLayout(IChatLayout layout) {
        mChatLayout = layout;
        return this;
    }
}
