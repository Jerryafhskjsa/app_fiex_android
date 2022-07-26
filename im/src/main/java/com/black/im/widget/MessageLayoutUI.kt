package com.black.im.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.model.MemberChatProfile
import com.black.base.util.ConstData
import com.black.im.action.PopMenuAction
import com.black.im.adapter.MessageListAdapter
import com.black.im.interfaces.IMessageLayout
import com.black.im.interfaces.IMessageProperties
import com.black.im.interfaces.IOnCustomMessageDrawListener
import com.black.im.util.CustomMessageUtil.init
import com.black.im.util.IMHelper.getUserUID
import com.black.im.util.ScreenUtil.getPxByDp
import java.util.*

abstract class MessageLayoutUI : RecyclerView, IMessageLayout {
    protected var mOnItemClickListener: MessageLayout.OnItemClickListener? = null
        get
    protected var mHandler: MessageLayout.OnLoadMoreHandler? = null
    protected var mEmptySpaceClickListener: MessageLayout.OnEmptySpaceClickListener? = null
    protected var mAdapter: MessageListAdapter? = null
        set
    protected var mPopActions: MutableList<PopMenuAction?>? = ArrayList()
    protected var mMorePopActions: MutableList<PopMenuAction?> = ArrayList()
    protected var mOnPopActionClickListener: MessageLayout.OnPopActionClickListener? = null
    private val properties: MessageLayoutUI.Properties = MessageLayoutUI.Properties()

    //    Properties.getInstance();
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        isLayoutFrozen = false
        setItemViewCacheSize(0)
        setHasFixedSize(true)
        isFocusableInTouchMode = false
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        layoutManager = linearLayoutManager
        init(properties)
    }

    open fun getProperties(): Properties? {
        return properties
    }

    override fun getAvatarRadius(): Int {
        return properties.getAvatarRadius()
    }

    override fun setAvatarRadius(radius: Int) {
        properties.setAvatarRadius(radius)
    }

    override fun getAvatarSize(): IntArray? {
        return properties.getAvatarSize()
    }

    override fun setAvatarSize(size: IntArray?) {
        properties.setAvatarSize(size)
    }

    override fun getAvatar(): Int {
        return properties.getAvatar()
    }

    override fun setAvatar(resId: Int) {
        properties.setAvatar(resId)
    }

    override fun getRightBubble(): Drawable? {
        return properties.getRightBubble()
    }

    override fun setRightBubble(drawable: Drawable?) {
        properties.setRightBubble(drawable)
    }

    override fun getLeftBubble(): Drawable? {
        return properties.getLeftBubble()
    }

    override fun setLeftBubble(drawable: Drawable?) {
        properties.setLeftBubble(drawable)
    }

    override fun getNameFontSize(): Int {
        return properties.getNameFontSize()
    }

    override fun setNameFontSize(size: Int) {
        properties.setNameFontSize(size)
    }

    override fun getNameFontColor(): Int {
        return properties.getNameFontColor()
    }

    override fun setNameFontColor(color: Int) {
        properties.setNameFontColor(color)
    }

    override fun getLeftNameVisibility(): Int {
        return properties.getLeftNameVisibility()
    }

    override fun setLeftNameVisibility(visibility: Int) {
        properties.setLeftNameVisibility(visibility)
    }

    open fun getLeftNameHard(): String? {
        return properties.getLeftNameHard()
    }

    open fun setLeftNameHard(leftNameHard: String?) {
        properties.setLeftNameHard(leftNameHard)
    }

    override fun getRightNameVisibility(): Int {
        return properties.getRightNameVisibility()
    }

    override fun setRightNameVisibility(visibility: Int) {
        properties.setRightNameVisibility(visibility)
    }

    open fun getRightNameHard(): String? {
        return properties.getRightNameHard()
    }

    open fun setRightNameHard(rightNameHard: String?) {
        properties.setRightNameHard(rightNameHard)
    }

    override fun getChatContextFontSize(): Int {
        return properties.getChatContextFontSize()
    }

    override fun setChatContextFontSize(size: Int) {
        properties.setChatContextFontSize(size)
    }

    override fun getRightChatContentFontColor(): Int {
        return properties.getRightChatContentFontColor()
    }

    override fun setRightChatContentFontColor(color: Int) {
        properties.setRightChatContentFontColor(color)
    }

    override fun getLeftChatContentFontColor(): Int {
        return properties.getLeftChatContentFontColor()
    }

    override fun setLeftChatContentFontColor(color: Int) {
        properties.setLeftChatContentFontColor(color)
    }

    override fun getTipsMessageBubble(): Drawable? {
        return properties.getTipsMessageBubble()
    }

    override fun setTipsMessageBubble(bubble: Drawable?) {
        properties.setTipsMessageBubble(bubble)
    }

    override fun getTipsMessageFontSize(): Int {
        return properties.getTipsMessageFontSize()
    }

    override fun setTipsMessageFontSize(size: Int) {
        properties.setTipsMessageFontSize(size)
    }

    override fun getTipsMessageFontColor(): Int {
        return properties.getTipsMessageFontColor()
    }

    override fun setTipsMessageFontColor(color: Int) {
        properties.setTipsMessageFontColor(color)
    }

    override fun getChatTimeBubble(): Drawable? {
        return properties.getChatTimeBubble()
    }

    override fun setChatTimeBubble(drawable: Drawable?) {
        properties.setChatTimeBubble(drawable)
    }

    override fun getChatTimeFontSize(): Int {
        return properties.getChatTimeFontSize()
    }

    override fun setChatTimeFontSize(size: Int) {
        properties.setChatTimeFontSize(size)
    }

    override fun getChatTimeFontColor(): Int {
        return properties.getChatTimeFontColor()
    }

    override fun setChatTimeFontColor(color: Int) {
        properties.setChatTimeFontColor(color)
    }

    open fun isUseLeftFirstLetter(): Boolean {
        return properties.isUseLeftFirstLetter()
    }

    open fun setUseLeftFirstLetter(useLeftFirstLetter: Boolean) {
        properties.setUseLeftFirstLetter(useLeftFirstLetter)
    }

    open fun isUseRightFirstLetter(): Boolean {
        return properties.isUseRightFirstLetter()
    }

    open fun setUseRightFirstLetter(useRightFirstLetter: Boolean) {
        properties.setUseRightFirstLetter(useRightFirstLetter)
    }

    open fun getLeftIconVisibility(): Int {
        return properties.getLeftIconVisibility()
    }

    open fun setLeftIconVisibility(leftIconVisibility: Int) {
        properties.setLeftIconVisibility(leftIconVisibility)
    }

    open fun getRightIconVisibility(): Int {
        return properties.getRightIconVisibility()
    }

    open fun setRightIconVisibility(rightIconVisibility: Int) {
        properties.setRightIconVisibility(rightIconVisibility)
    }

    open fun getLeftNameDefault(): String? {
        return properties.getLeftNameDefault()
    }

    open fun setLeftNameDefault(leftNameDefault: String?) {
        properties.setLeftNameDefault(leftNameDefault)
    }

    open fun getRightNameDefault(): String? {
        return properties.getRightNameDefault()
    }

    open fun setRightNameDefault(rightNameDefault: String?) {
        properties.setRightNameDefault(rightNameDefault)
    }

    open fun getLeftDefaultNameAvatar(): Bitmap? {
        return properties.getLeftDefaultNameAvatar()
    }

    open fun setLeftDefaultNameAvatar(leftDefaultNameAvatar: Bitmap?) {
        properties.setLeftDefaultNameAvatar(leftDefaultNameAvatar)
    }

    open fun getRightDefaultNameAvatar(): Bitmap? {
        return properties.getRightDefaultNameAvatar()
    }

    open fun setRightDefaultNameAvatar(rightDefaultNameAvatar: Bitmap?) {
        properties.setRightDefaultNameAvatar(rightDefaultNameAvatar)
    }

    open fun getProfileMap(): HashMap<String?, MemberChatProfile?>? {
        return properties.getProfileMap()
    }

    open fun setProfileMap(profileMap: HashMap<String?, MemberChatProfile?>?): Boolean {
        return properties.setProfileMap(profileMap)
    }

    override fun setOnCustomMessageDrawListener(listener: IOnCustomMessageDrawListener?) {
        mAdapter?.setOnCustomMessageDrawListener(listener)
    }

    override fun getOnItemClickListener(): MessageLayout.OnItemClickListener? {
        return mAdapter?.onItemClickListener
    }

    override fun setOnItemClickListener(listener: MessageLayout.OnItemClickListener?) {
        mOnItemClickListener = listener
        mAdapter?.onItemClickListener = listener
    }

    override fun setAdapter(adapter: MessageListAdapter?) {
        super.setAdapter(adapter)
        mAdapter = adapter
        postSetAdapter(adapter)
    }

    open fun refreshAdapter() {
        mAdapter?.notifyDataSetChanged()
    }

    protected abstract fun postSetAdapter(adapter: MessageListAdapter?)

    override fun getPopActions(): MutableList<PopMenuAction?>? {
        return mPopActions
    }

    override fun addPopAction(action: PopMenuAction?) {
        mMorePopActions.add(action)
    }

    class Properties internal constructor() : IMessageProperties {
        private var mAvatarId = 0
        private var mAvatarRadius = 0
        private var avatarSize: IntArray? = null
        private var mNameFontSize = 0
        private var mNameFontColor = 0
        private var mLeftNameVisibility = View.GONE
        private var leftNameHard //固定左边名字显示
                : String? = null
        private var leftNameDefault //默认左边名字显示
                : String? = null
        private var leftDefaultNameAvatar //默认左边头像默认
                : Bitmap? = null
        private var useLeftFirstLetter = false
        private var mRightNameVisibility = View.GONE
        private var useRightFirstLetter = false
        private var rightNameHard //固定右边名字显示
                : String? = null
        private var rightNameDefault //默认右边名字显示
                : String? = null
        private var rightDefaultNameAvatar //默认右边头像默认
                : Bitmap? = null
        private var mChatContextFontSize = 0
        private var mMyChatContentFontColor = 0
        private var mMyBubble: Drawable? = null
        private var mFriendChatContentFontColor = 0
        private var mFriendBubble: Drawable? = null
        private var mTipsMessageFontSize = 0
        private var mTipsMessageFontColor = 0
        private var mTipsMessageBubble: Drawable? = null
        private var mChatTimeFontSize = 0
        private var mChatTimeFontColor = 0
        private var mChatTimeBubble: Drawable? = null
        private var leftIconVisibility = View.GONE
        private var rightIconVisibility = View.GONE

        private var profileMap: HashMap<String?, MemberChatProfile?>? = null

        private fun Properties() {}

        companion object {
            private var sP: Properties? = MessageLayoutUI.Properties()
            fun getInstance(): Properties {
                if (sP == null) {
                    sP = MessageLayoutUI.Properties()
                }
                return sP!!
            }
        }

        override fun getAvatarRadius(): Int {
            return mAvatarRadius
        }

        override fun setAvatarRadius(radius: Int) {
            mAvatarRadius = getPxByDp(radius)
        }

        override fun getAvatarSize(): IntArray? {
            return avatarSize
        }

        override fun setAvatarSize(size: IntArray?) {
            if (size != null && size.size == 2) {
                avatarSize = IntArray(2)
                avatarSize!![0] = getPxByDp(size[0])
                avatarSize!![1] = getPxByDp(size[1])
            }
        }

        override fun getAvatar(): Int {
            return mAvatarId
        }

        override fun setAvatar(resId: Int) {
            mAvatarId = resId
        }

        override fun getRightBubble(): Drawable? {
            return mMyBubble
        }

        override fun setRightBubble(bubble: Drawable?) {
            mMyBubble = bubble
        }

        override fun getLeftBubble(): Drawable? {
            return mFriendBubble
        }

        override fun setLeftBubble(bubble: Drawable?) {
            mFriendBubble = bubble
        }

        override fun getNameFontSize(): Int {
            return mNameFontSize
        }

        override fun setNameFontSize(size: Int) {
            mNameFontSize = size
        }

        override fun getNameFontColor(): Int {
            return mNameFontColor
        }

        override fun setNameFontColor(color: Int) {
            mNameFontColor = color
        }

        override fun getLeftNameVisibility(): Int {
            return mLeftNameVisibility
        }

        override fun setLeftNameVisibility(visibility: Int) {
            mLeftNameVisibility = visibility
        }

        override fun getRightNameVisibility(): Int {
            return mRightNameVisibility
        }

        override fun setRightNameVisibility(visibility: Int) {
            mRightNameVisibility = visibility
        }

        override fun getChatContextFontSize(): Int {
            return mChatContextFontSize
        }

        override fun setChatContextFontSize(size: Int) {
            mChatContextFontSize = size
        }

        override fun getRightChatContentFontColor(): Int {
            return mMyChatContentFontColor
        }

        override fun setRightChatContentFontColor(color: Int) {
            mMyChatContentFontColor = color
        }

        override fun getLeftChatContentFontColor(): Int {
            return mFriendChatContentFontColor
        }

        override fun setLeftChatContentFontColor(color: Int) {
            mFriendChatContentFontColor = color
        }

        override fun getTipsMessageBubble(): Drawable? {
            return mTipsMessageBubble
        }

        override fun setTipsMessageBubble(bubble: Drawable?) {
            mTipsMessageBubble = bubble
        }

        override fun getTipsMessageFontSize(): Int {
            return mTipsMessageFontSize
        }

        override fun setTipsMessageFontSize(size: Int) {
            mTipsMessageFontSize = size
        }

        override fun getTipsMessageFontColor(): Int {
            return mTipsMessageFontColor
        }

        override fun setTipsMessageFontColor(color: Int) {
            mTipsMessageFontColor = color
        }

        override fun getChatTimeBubble(): Drawable? {
            return mChatTimeBubble
        }

        override fun setChatTimeBubble(bubble: Drawable?) {
            mChatTimeBubble = bubble
        }

        override fun getChatTimeFontSize(): Int {
            return mChatTimeFontSize
        }

        override fun setChatTimeFontSize(size: Int) {
            mChatTimeFontSize = size
        }

        override fun getChatTimeFontColor(): Int {
            return mChatTimeFontColor
        }

        override fun setChatTimeFontColor(color: Int) {
            mChatTimeFontColor = color
        }

        fun getLeftNameHard(): String? {
            return leftNameHard
        }

        fun setLeftNameHard(leftNameHard: String?) {
            this.leftNameHard = leftNameHard
        }

        fun getRightNameHard(): String? {
            return rightNameHard
        }

        fun setRightNameHard(rightNameHard: String?) {
            this.rightNameHard = rightNameHard
        }

        fun isUseLeftFirstLetter(): Boolean {
            return useLeftFirstLetter
        }

        fun setUseLeftFirstLetter(useLeftFirstLetter: Boolean) {
            this.useLeftFirstLetter = useLeftFirstLetter
        }

        fun isUseRightFirstLetter(): Boolean {
            return useRightFirstLetter
        }

        fun setUseRightFirstLetter(useRightFirstLetter: Boolean) {
            this.useRightFirstLetter = useRightFirstLetter
        }

        fun getLeftIconVisibility(): Int {
            return leftIconVisibility
        }

        fun setLeftIconVisibility(leftIconVisibility: Int) {
            this.leftIconVisibility = leftIconVisibility
        }

        fun getRightIconVisibility(): Int {
            return rightIconVisibility
        }

        fun setRightIconVisibility(rightIconVisibility: Int) {
            this.rightIconVisibility = rightIconVisibility
        }

        fun getLeftNameDefault(): String? {
            return leftNameDefault
        }

        fun setLeftNameDefault(leftNameDefault: String?) {
            this.leftNameDefault = leftNameDefault
        }

        fun getRightNameDefault(): String? {
            return rightNameDefault
        }

        fun setRightNameDefault(rightNameDefault: String?) {
            this.rightNameDefault = rightNameDefault
        }

        fun getLeftDefaultNameAvatar(): Bitmap? {
            return leftDefaultNameAvatar
        }

        fun setLeftDefaultNameAvatar(leftDefaultNameAvatar: Bitmap?) {
            this.leftDefaultNameAvatar = leftDefaultNameAvatar
        }

        fun getRightDefaultNameAvatar(): Bitmap? {
            return rightDefaultNameAvatar
        }

        fun setRightDefaultNameAvatar(rightDefaultNameAvatar: Bitmap?) {
            this.rightDefaultNameAvatar = rightDefaultNameAvatar
        }

        fun getProfileMap(): HashMap<String?, MemberChatProfile?>? {
            return profileMap
        }

        fun setProfileMap(profileMap: HashMap<String?, MemberChatProfile?>?): Boolean {
            if (this.profileMap == null && profileMap == null) {
                return false
            }
            if (this.profileMap == null) {
                this.profileMap = profileMap
                return true
            }
            return if (this.profileMap == profileMap) {
                false
            } else {
                this.profileMap = profileMap
                true
            }
        }

        fun getMemberChatProfile(userIdAbout: String?): MemberChatProfile? {
            if (profileMap == null || profileMap!!.isEmpty()) {
                return null
            }
            val id = getUserUID(userIdAbout!!) ?: return null
            var profile = profileMap!![id]
            if (profile == null) {
                profile = profileMap!![ConstData.FACTION_MEMBER_PROFILE_DEFAULT]
            }
            return profile
        }
    }
}