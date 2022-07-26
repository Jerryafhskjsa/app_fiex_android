package com.black.im.model.group

import com.tencent.imsdk.ext.group.TIMGroupPendencyItem
import java.io.Serializable

class GroupApplyInfo(val pendencyItem: TIMGroupPendencyItem) : Serializable {
    var status = 0

    companion object {
        const val APPLIED = 1
        const val REFUSED = -1
        const val UNHANDLED = 0
    }

}