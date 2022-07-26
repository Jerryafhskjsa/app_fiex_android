package com.black.im.config

import com.tencent.imsdk.TIMSdkConfig

class TUIKitConfigs private constructor() {
    companion object {
        private var sConfigs: TUIKitConfigs? = null
        /**
         * 获取TUIKit的全部配置
         *
         * @return
         */
        val configs: TUIKitConfigs?
            get() {
                if (sConfigs == null) {
                    sConfigs = TUIKitConfigs()
                }
                return sConfigs
            }
    }

    /**
     * 获取TUIKit的通用配置
     *
     * @return
     */
    var generalConfig: GeneralConfig? = null
        private set
    /**
     * 获取自定义表情包配置
     *
     * @return
     */
    var customFaceConfig: CustomFaceConfig? = null
        private set
    /**
     * 获取IMSDK的配置
     *
     * @return
     */
    var sdkConfig: TIMSdkConfig? = null
        private set

    /**
     * 设置TUIKit的通用配置
     *
     * @param generalConfig
     * @return
     */
    fun setGeneralConfig(generalConfig: GeneralConfig?): TUIKitConfigs {
        this.generalConfig = generalConfig
        return this
    }

    /**
     * 设置自定义表情包配置
     *
     * @param customFaceConfig
     * @return
     */
    fun setCustomFaceConfig(customFaceConfig: CustomFaceConfig?): TUIKitConfigs {
        this.customFaceConfig = customFaceConfig
        return this
    }

    /**
     * 设置IMSDK的配置
     *
     * @param timSdkConfig
     * @return
     */
    fun setSdkConfig(timSdkConfig: TIMSdkConfig?): TUIKitConfigs {
        sdkConfig = timSdkConfig
        return this
    }
}