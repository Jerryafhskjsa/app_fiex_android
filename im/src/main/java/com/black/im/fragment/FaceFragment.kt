package com.black.im.fragment

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.black.im.R
import com.black.im.databinding.FragmentFaceBinding
import com.black.im.manager.FaceManager
import com.black.im.manager.FaceManager.customFaceList
import com.black.im.manager.RecentEmojiManager
import com.black.im.model.face.Emoji
import com.black.im.model.face.FaceGroup
import com.black.im.model.face.FaceGroupIcon
import com.black.im.util.ScreenUtil.getPxByDp
import com.black.im.util.SoftKeyBoardUtil.getSoftKeyBoardHeight
import java.io.IOException

class FaceFragment : BaseInputFragment(), View.OnClickListener {
    companion object {
        fun Instance(): FaceFragment {
            val instance = FaceFragment()
            val bundle = Bundle()
            instance.arguments = bundle
            return instance
        }
    }

    private var binding: FragmentFaceBinding? = null

    var mCurrentSelected: FaceGroupIcon? = null
    var ViewPagerItems = ArrayList<View>()
    var emojiList: ArrayList<Emoji?>? = null
    var recentlyEmojiList: ArrayList<Emoji?>? = null
    var customFaces: ArrayList<FaceGroup>? = null
    private var mCurrentGroupIndex = 0
    private var columns = 7
    private var rows = 3
    private var vMargin = 0
    private var listener: OnEmojiClickListener? = null
    private var recentManager: RecentEmojiManager? = null

    fun setListener(listener: OnEmojiClickListener?) {
        this.listener = listener
    }

    override fun onAttach(activity: Activity) {
        if (activity is OnEmojiClickListener) {
            listener = activity
        }
        recentManager = RecentEmojiManager.make(activity)
        super.onAttach(activity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        emojiList = FaceManager.emojiList
        try {
            recentlyEmojiList = if (recentManager?.getCollection(RecentEmojiManager.PREFERENCE_NAME) != null) {
                recentManager?.getCollection(RecentEmojiManager.PREFERENCE_NAME) as ArrayList<Emoji?>?
            } else {
                ArrayList()
            }
        } catch (e: IOException) {
        } catch (e: ClassNotFoundException) {
        }
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_face, container, false)
        val params = binding?.root?.layoutParams
        params?.height = getSoftKeyBoardHeight()
        binding?.root?.layoutParams = params

        initViews()
        return binding?.root
    }

    private fun initViews() {
        initViewPager(emojiList, 7, 3)
        mCurrentSelected = binding?.faceFirstSet
        binding?.faceFirstSet?.isSelected = true
        binding?.faceFirstSet?.setOnClickListener(this)
        customFaces = customFaceList
        val width = getPxByDp(70)

        for (i in customFaces!!.indices) {
            val group = customFaces!![i]
            val faceBtn = FaceGroupIcon(activity)
            faceBtn.setFaceTabIcon(group.groupIcon)
            faceBtn.setOnClickListener { v ->
                if (mCurrentSelected != v) {
                    mCurrentGroupIndex = group.groupId
                    val faces = group.faces ?: ArrayList()
                    mCurrentSelected?.isSelected = false
                    initViewPager(faces, group.pageColumnCount, group.pageRowCount)
                    mCurrentSelected = v as FaceGroupIcon
                    mCurrentSelected?.isSelected = true
                }
            }
            val params = LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.MATCH_PARENT)
            binding?.faceViewGroup?.addView(faceBtn, params)
        }
    }

    private fun initViewPager(list: ArrayList<Emoji?>?, columns: Int, rows: Int) {
        this.columns = columns
        this.rows = rows
        if (list?.size ?: 0 > 0) {
            vMargin = (getSoftKeyBoardHeight() - (getPxByDp(40 + 20) + (list!![0]?.height
                    ?: 0) * rows)) / 4
        }
        intiIndicator(list)
        ViewPagerItems.clear()
        val pageCont = getPagerCount(list)
        for (i in 0 until pageCont) {
            ViewPagerItems.add(getViewPagerItem(i, list))
        }
        val mVpAdapter = FaceVPAdapter(ViewPagerItems)
        binding?.faceViewPager?.adapter = mVpAdapter
        binding?.faceViewPager?.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            var oldPosition = 0
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                binding?.faceIndicator?.playBy(oldPosition, position)
                oldPosition = position
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    private fun intiIndicator(list: ArrayList<Emoji?>?) {
        binding?.faceIndicator?.init(getPagerCount(list))
    }

    override fun onClick(v: View) {
        if (v.id == R.id.face_first_set) { /* if (binding?.faceIndicator?.getVisibility() == View.GONE) {
                binding?.faceIndicator?.setVisibility(View.VISIBLE);
            }*/
            if (mCurrentSelected != v) {
                mCurrentGroupIndex = 0
                mCurrentSelected?.isSelected = false
                mCurrentSelected = v as FaceGroupIcon
                initViewPager(emojiList, 7, 3)
                mCurrentSelected?.isSelected = true
            }
        }
    }

    /**
     * 根据表情数量以及GridView设置的行数和列数计算Pager数量
     *
     * @return
     */
    private fun getPagerCount(list: ArrayList<Emoji?>?): Int {
        val count = list?.size ?: 0
        var dit = 1
        if (mCurrentGroupIndex > 0) dit = 0
        return if (count % (columns * rows - dit) == 0) count / (columns * rows - dit) else count / (columns * rows - dit) + 1
    }

    private fun getViewPagerItem(position: Int, list: ArrayList<Emoji?>?): View {
        val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(R.layout.layout_face_grid, null) //表情布局
        val gridview = layout.findViewById<GridView>(R.id.chart_face_gv)
        /**
         * 注：因为每一页末尾都有一个删除图标，所以每一页的实际表情columns *　rows　－　1; 空出最后一个位置给删除图标
         */
        val subList: MutableList<Emoji?> = ArrayList()
        var dit = 1
        if (mCurrentGroupIndex > 0) dit = 0
        subList.addAll(list?.subList(position * (columns * rows - dit),
                if ((columns * rows - dit) * (position + 1) > list.size) list.size else (columns * rows - dit) * (position + 1))
                ?: ArrayList())
        /**
         * 末尾添加删除图标
         */
        if (mCurrentGroupIndex == 0 && subList.size < columns * rows - dit) {
            for (i in subList.size until columns * rows - dit) {
                subList.add(null)
            }
        }
        if (mCurrentGroupIndex == 0) {
            val deleteEmoji = Emoji()
            deleteEmoji.icon = BitmapFactory.decodeResource(resources, R.drawable.face_delete)
            subList.add(deleteEmoji)
        }
        val mGvAdapter = FaceGVAdapter(subList, activity)
        gridview.adapter = mGvAdapter
        gridview.numColumns = columns
        // 单击表情执行的操作
        gridview.onItemClickListener = AdapterView.OnItemClickListener { _, _, itemPosition, id ->
            if (mCurrentGroupIndex > 0) {
                listener?.onCustomFaceClick(mCurrentGroupIndex, subList[itemPosition])
            } else {
                if (itemPosition == columns * rows - 1) {
                    if (listener != null) {
                        listener?.onEmojiDelete()
                    }
                    return@OnItemClickListener
                }
                if (listener != null) {
                    listener?.onEmojiClick(subList[itemPosition])
                }
            }
            //insertToRecentList(subList.get(position));
        }
        return gridview
    }

    private fun insertToRecentList(emoji: Emoji?) {
        if (emoji != null) {
            if (true == recentlyEmojiList?.contains(emoji)) {
                //如果已经有该表情，就把该表情放到第一个位置
                val index = recentlyEmojiList?.indexOf(emoji)
                val emoji0 = recentlyEmojiList!![0]
                index?.let {
                    recentlyEmojiList!![index] = emoji0
                    recentlyEmojiList!![0] = emoji
                    return
                }
            }
            if (recentlyEmojiList?.size == rows * columns - 1) {
                //去掉最后一个
                recentlyEmojiList?.removeAt(rows * columns - 2)
            }
            recentlyEmojiList?.add(0, emoji)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            recentManager?.putCollection(RecentEmojiManager.PREFERENCE_NAME, recentlyEmojiList)
        } catch (e: IOException) {
        }
    }

    interface OnEmojiClickListener {
        fun onEmojiDelete()
        fun onEmojiClick(emoji: Emoji?)
        fun onCustomFaceClick(groupIndex: Int, emoji: Emoji?)
    }

    internal inner class FaceGVAdapter(private val list: List<Emoji?>, private val mContext: Context) : BaseAdapter() {
        override fun getCount(): Int {
            return list.size
        }

        override fun getItem(position: Int): Any? {
            return list[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            var convertView1 = convertView
            val holder: ViewHolder
            val emoji = list[position]
            if (convertView1 == null) {
                holder = ViewHolder()
                convertView1 = LayoutInflater.from(mContext).inflate(R.layout.item_face, null)
                holder.iv = convertView1.findViewById(R.id.face_image)
                val params = holder.iv?.layoutParams as FrameLayout.LayoutParams
                if (emoji != null) {
                    params.width = emoji.width
                    params.height = emoji.height
                }
                if (position / columns == 0) {
                    params.setMargins(0, vMargin, 0, 0)
                } else if (rows == 2) {
                    params.setMargins(0, vMargin, 0, 0)
                } else {
                    if (position / columns < rows - 1) {
                        params.setMargins(0, vMargin, 0, vMargin)
                    } else {
                        params.setMargins(0, 0, 0, vMargin)
                    }
                }
                holder.iv?.layoutParams = params
                convertView1.tag = holder
            } else {
                holder = convertView1.tag as ViewHolder
            }
            if (emoji != null) {
                holder.iv?.setImageBitmap(emoji.icon)
            }
            return convertView1
        }

        internal inner class ViewHolder {
            var iv: ImageView? = null
        }

    }

    internal inner class FaceVPAdapter(// 界面列表
            private val views: List<View>) : PagerAdapter() {

        override fun destroyItem(arg0: View, arg1: Int, arg2: Any) {
            (arg0 as ViewPager).removeView(arg2 as View)
        }

        override fun getCount(): Int {
            return views.size
        }

        // 初始化arg1位置的界面
        override fun instantiateItem(arg0: View, arg1: Int): Any {
            (arg0 as ViewPager).addView(views[arg1])
            return views[arg1]
        }

        // 判断是否由对象生成界
        override fun isViewFromObject(arg0: View, arg1: Any): Boolean {
            return arg0 === arg1
        }

    }
}