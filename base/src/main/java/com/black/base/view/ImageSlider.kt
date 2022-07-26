package com.black.base.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.black.base.R
import java.util.*

//图片轮播器
class ImageSlider : FrameLayout {
    private var mCcontext: Context? = null
    private var contentView: View? = null
    private var imageViewPager: ViewPager? = null
    private var indicatorLayout: RadioGroup? = null
    private var viewList: MutableList<View> = ArrayList()
    // 声明监听器
    private var onItemClickListener: OnItemClickListener? = null

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
        mCcontext = context
        // 初始化View
        initView()
        // 初始化Animator
        initAnimator()
    }

    /**
     * 初始化View
     */
    private fun initView() {
        contentView = LayoutInflater.from(context).inflate(R.layout.view_image_slider, this, true)
        imageViewPager = findViewById(R.id.image_view_pager)
        imageViewPager?.adapter = ImagePagerAdapter()
        imageViewPager?.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                refreshIndicatorLayout(position)
            }
        })
        indicatorLayout = findViewById(R.id.indicator)
    }

    //初始化动画
    private fun initAnimator() {}

    private fun refreshIndicatorLayout(selectedIndex: Int) {
        val radioButton = indicatorLayout?.getChildAt(selectedIndex) as RadioButton?
        if (radioButton != null) {
            radioButton.isChecked = true
        }
        //        for(int i = 0; i < indicatorLayout.getChildCount(); i++){
//            RadioButton radioButton = (RadioButton) indicatorLayout.getChildAt(i);
//            if(i == selectedIndex){
//                radioButton.setChecked(true);
//            }
//            else{
////                radioButton.setChecked(false);
//            }
//        }
    }

    //    // 设置小圆点的大小
//    public void setDotSize(int dotSize) {
//        this.dotSize = dotSize;
//    }
//
//    // 设置小圆点的间距
//    public void setDotSpace(int dotSpace) {
//        this.dotSpace = dotSpace;
//    }
//
//    // 设置图片轮播间隔时间
//    public void setDelay(int delay) {
//        this.delay = delay;
//    }
//
//    // 添加图片
//    public void addImageUrl(String imageUrl) {
//        ImageTitleBean imageTitleBean = new ImageTitleBean();
//        imageTitleBean.setImageUrl(imageUrl);
//        imageTitleBeanList.add(imageTitleBean);
//    }
//
//    // 添加图片和标题
//    public void addImageTitle(String imageUrl, String title) {
//        ImageTitleBean imageTitleBean = new ImageTitleBean();
//        imageTitleBean.setImageUrl(imageUrl);
//        imageTitleBean.setTitle(title);
//        imageTitleBeanList.add(imageTitleBean);
//    }
//
//    // 添加图片和标题的JavaBean
//    public void addImageTitleBean(ImageTitleBean imageTitleBean) {
//        imageTitleBeanList.add(imageTitleBean);
//    }
//
//    // 设置图片和标题的JavaBean数据列表
//    public void setImageTitleBeanList(List<ImageTitleBean> imageTitleBeanList) {
//        this.imageTitleBeanList = imageTitleBeanList;
//    }
//
//    // 设置完后最终提交
//    public void commit() {
//        if (imageTitleBeanList != null) {
//            count = imageTitleBeanList.size();
//            // 设置ViewPager
//            setViewPager(imageTitleBeanList);
//            // 设置指示器
//            setIndicator();
//            // 开始播放
//            starPlay();
//        } else {
//        }
//    }
//
//    /**
//     * 设置指示器
//     */
//    private void setIndicator() {
//        isLarge = new SparseBooleanArray();
//        // 记得创建前先清空数据，否则会受遗留数据的影响。
//        llDot.removeAllViews();
//        for (int i = 0; i < count; i++) {
//            View view = new View(context);
//            view.setBackgroundResource(R.drawable.dot_unselected);
//            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dotSize, dotSize);
//            layoutParams.leftMargin = dotSpace / 2;
//            layoutParams.rightMargin = dotSpace / 2;
//            layoutParams.topMargin = dotSpace / 2;
//            layoutParams.bottomMargin = dotSpace / 2;
//            llDot.addView(view, layoutParams);
//            isLarge.put(i, false);
//        }
//        llDot.getChildAt(0).setBackgroundResource(R.drawable.dot_selected);
//        animatorToLarge.setTarget(llDot.getChildAt(0));
//        animatorToLarge.start();
//        isLarge.put(0, true);
//    }
//
//    /**
//     * 开始自动播放图片
//     */
//    private void starPlay() {
//        // 如果少于2张就不用自动播放了
//        if (count < 2) {
//            isAutoPlay = false;
//        } else {
//            isAutoPlay = true;
//            handler = new Handler();
//            handler.postDelayed(task, delay);
//        }
//    }
//
//    private Runnable task = new Runnable() {
//        @Override
//        public void run() {
//            if (isAutoPlay) {
//                // 位置循环
//                currentItem = currentItem % (count + 1) + 1;
//                // 正常每隔3秒播放一张图片
//                vpImageTitle.setCurrentItem(currentItem);
//                handler.postDelayed(task, delay);
//            } else {
//                // 如果处于拖拽状态停止自动播放，会每隔5秒检查一次是否可以正常自动播放。
//                handler.postDelayed(task, 5000);
//            }
//        }
//    };
// 创建监听器接口
    interface OnItemClickListener {
        fun onItemClick(imageBean: ImageBean?, position: Int)
    }

    // 提供设置监听器的公共方法
    fun setOnItemClickListener(listener: OnItemClickListener?) {
        onItemClickListener = listener
    }

    fun setImageResources(imageList: List<ImageBean?>?) {
        if (imageList == null || imageList.isEmpty()) {
            viewList = ArrayList()
            indicatorLayout?.removeAllViews()
        } else {
            val size = imageList.size
            viewList = ArrayList(size)
            indicatorLayout?.removeAllViews()
            for (i in 0 until size) {
                val imageBean = imageList[i]
                if (imageBean?.image != null) {
                    val view = imageBean.getShowView(context)
                    viewList.add(view)
                    indicatorLayout?.addView(createIndicatorChild())
                }
            }
        }
        imageViewPager?.adapter?.notifyDataSetChanged()
        imageViewPager?.currentItem = 0
        refreshIndicatorLayout(0)
        invalidate()
    }

    private fun createIndicatorChild(): View {
        return LayoutInflater.from(context).inflate(R.layout.view_image_slider_dot, null)
    }

    class ImageBean(internal val image: Drawable?, private val tag: Any) {
        internal fun getShowView(context: Context): View {
            val showView = LayoutInflater.from(context).inflate(R.layout.view_image_slider_show, null) as ImageView
            showView.setImageDrawable(image)
            showView.tag = tag
            return showView
        }

    }

    internal inner class ImagePagerAdapter : PagerAdapter() {
        override fun getCount(): Int {
            return viewList.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = viewList[position]
            // 设置Item的点击监听器
            view.setOnClickListener { v ->
                // 注意：位置是position-1
                if (onItemClickListener != null) {
                    onItemClickListener?.onItemClick(v.tag as ImageBean, position - 1)
                }
            }
            container.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(viewList[position])
        }
    }
}