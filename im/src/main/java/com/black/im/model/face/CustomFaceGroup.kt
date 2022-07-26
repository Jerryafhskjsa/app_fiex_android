package com.black.im.model.face

import java.util.*

/**
 * 一个表情包
 */
class CustomFaceGroup {
    /**
     * 获取表情包所在的组ID
     *
     * @return
     */
    /**
     * 设置表情包所在的组ID
     *
     * @param faceGroupId
     */
    var faceGroupId = 0
    /**
     * 获取表情包的封面
     *
     * @return
     */
    /**
     * 设置表情包的封面
     *
     * @param faceIconPath
     */
    var faceIconPath: String? = null
    /**
     * 获取表情包的名称
     *
     * @return
     */
    /**
     * 设置表情包的名称
     *
     * @param faceIconName
     */
    var faceIconName: String? = null
    /**
     * 获取表情包每行显示数量
     *
     * @return
     */
    /**
     * 设置表情包每行的显示数量
     *
     * @param pageRowCount
     */
    var pageRowCount = 0
    /**
     * 获取表情包的列数量
     *
     * @return
     */
    /**
     * 设置表情包的列数量
     *
     * @param pageColumnCount
     */
    var pageColumnCount = 0
    /**
     * 获取表情包
     *
     * @return
     */
    val customFaceList = ArrayList<CustomFace>()

    /**
     * 增加一个表情
     *
     * @param face
     */
    fun addCustomFace(face: CustomFace) {
        customFaceList.add(face)
    }

}