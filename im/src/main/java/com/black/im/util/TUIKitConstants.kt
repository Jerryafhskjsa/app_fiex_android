package com.black.im.util

import android.os.Environment
import com.black.im.util.TUIKit.appContext
import com.black.im.util.TUIKit.configs

object TUIKitConstants {
    const val CAMERA_IMAGE_PATH = "camera_image_path"
    const val IMAGE_WIDTH = "image_width"
    const val IMAGE_HEIGHT = "image_height"
    const val VIDEO_TIME = "video_time"
    const val CAMERA_VIDEO_PATH = "camera_video_path"
    const val IMAGE_DATA = "image_data"
    const val SELF_MESSAGE = "self_message"
    const val CAMERA_TYPE = "camera_type"
    var SD_CARD_PATH = Environment.getExternalStorageDirectory().absolutePath
    var APP_DIR = if (configs.generalConfig!!.appCacheDir != null) configs.generalConfig!!.appCacheDir else SD_CARD_PATH + "/" + appContext.packageName
    var RECORD_DIR = "$APP_DIR/record/"
    var RECORD_DOWNLOAD_DIR = "$APP_DIR/record/download/"
    var VIDEO_DOWNLOAD_DIR = "$APP_DIR/video/download/"
    var IMAGE_BASE_DIR = "$APP_DIR/image/"
    var IMAGE_DOWNLOAD_DIR = IMAGE_BASE_DIR + "download/"
    var MEDIA_DIR = "$APP_DIR/media"
    var FILE_DOWNLOAD_DIR = "$APP_DIR/file/download/"
    var CRASH_LOG_DIR = "$APP_DIR/crash/"
    var UI_PARAMS = "ilive_ui_params"
    var SOFT_KEY_BOARD_HEIGHT = "soft_key_board_height"

    object ActivityRequest {
        const val CODE_1 = 1
    }

    object Group {
        const val MODIFY_GROUP_NAME = 0X01
        const val MODIFY_GROUP_NOTICE = 0X02
        const val MODIFY_GROUP_JOIN_TYPE = 0X03
        const val MODIFY_MEMBER_NAME = 0X11
        const val GROUP_ID = "group_id"
        const val GROUP_INFO = "groupInfo"
        const val MEMBER_APPLY = "apply"
    }

    object Selection {
        const val CONTENT = "content"
        const val TYPE = "type"
        const val TITLE = "title"
        const val INIT_CONTENT = "init_content"
        const val DEFAULT_SELECT_ITEM_INDEX = "default_select_item_index"
        const val LIST = "list"
        const val LIMIT = "limit"
        const val TYPE_TEXT = 1
        const val TYPE_LIST = 2
    }

    object ProfileType {
        const val CONTENT = "content"
        const val FROM = "from"
        const val FROM_CHAT = 1
        const val FROM_NEW_FRIEND = 2
        const val FROM_CONTACT = 3
        const val FROM_GROUP_APPLY = 4
    }

    object GroupType {
        const val TYPE = "type"
        const val GROUP = "isGroup"
        const val PRIVATE = 0
        const val PUBLIC = 1
        const val CHAT_ROOM = 2
        const val TYPE_PRIVATE = "Private"
        const val TYPE_PUBLIC = "Public"
        const val TYPE_CHAT_ROOM = "ChatRoom"
    }
}