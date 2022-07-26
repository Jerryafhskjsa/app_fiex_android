package com.black.im.config

import com.black.im.model.face.CustomFaceGroup
import java.util.*

/**
 * 自定义表情包的配置类
 */
class CustomFaceConfig {
    private var mFaceConfigs: MutableList<CustomFaceGroup>? = null
    /**
     * 增加自定义表情包
     *
     * @param group
     * @return
     */
    fun addFaceGroup(group: CustomFaceGroup): CustomFaceConfig {
        if (mFaceConfigs == null) {
            mFaceConfigs = ArrayList()
        }
        mFaceConfigs!!.add(group)
        return this
    }

    /**
     * 获取全部的自定义表情包
     *
     * @return
     */
    val faceGroups: List<CustomFaceGroup>?
        get() = mFaceConfigs
}