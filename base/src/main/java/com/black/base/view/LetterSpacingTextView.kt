package com.black.base.view

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ScaleXSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class LetterSpacingTextView : AppCompatTextView {
    private var spacing = Spacing.NORMAL
    private var originalText: CharSequence = ""

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {}

    /**
     * 获取字间距
     *
     * @return
     */
    fun getSpacing(): Float {
        return spacing
    }

    /**
     * 设置间距
     *
     * @param spacing
     */
    fun setSpacing(spacing: Float) {
        this.spacing = spacing
        applySpacing()
    }

    override fun setText(text: CharSequence, type: BufferType) {
        originalText = text
        applySpacing()
    }

    override fun getText(): CharSequence {
        return originalText
    }

    /**
     * 添加应用空间
     */
    private fun applySpacing() {
        if (this == null || originalText == null) return
        val builder = StringBuilder()
        for (i in 0 until originalText.length) {
            builder.append(originalText[i])
            if (i + 1 < originalText.length) {
                // \u00A0 不间断空格
                // 追加空格
                builder.append("\u00A0")
            }
        }
        // TextView通常用来显示普通文本，但是有时候需要对其中某些文本进行样式、事件方面的设置。Android系统通过SpannableString类来对指定文本进行相关处理，具体有以下功能：
        // 1、BackgroundColorSpan 背景色
        // 2、ClickableSpan 文本可点击，有点击事件
        // 3、ForegroundColorSpan 文本颜色（前景色）
        // 4、MaskFilterSpan 修饰效果，如模糊(BlurMaskFilter)、浮雕(EmbossMaskFilter)
        // 5、MetricAffectingSpan 父类，一般不用
        // 6、RasterizerSpan 光栅效果
        // 7、StrikethroughSpan 删除线（中划线）
        // 8、SuggestionSpan 相当于占位符
        // 9、UnderlineSpan 下划线
        // 10、AbsoluteSizeSpan 绝对大小（文本字体）
        // 11、DynamicDrawableSpan 设置图片，基于文本基线或底部对齐。
        // 12、ImageSpan 图片
        // 13、RelativeSizeSpan 相对大小（文本字体）
        // 14、ReplacementSpan 父类，一般不用
        // 15、ScaleXSpan 基于x轴缩放
        // 16、StyleSpan 字体样式：粗体、斜体等
        // 17、SubscriptSpan 下标（数学公式会用到）
        // 18、SuperscriptSpan 上标（数学公式会用到）
        // 19、TextAppearanceSpan 文本外貌（包括字体、大小、样式和颜色）
        // 20、TypefaceSpan 文本字体
        // 21、URLSpan 文本超链接
        // 我们也是通过这个，去设置空格
        val finalText = SpannableString(builder.toString())
        if (builder.toString().length > 1) {
            // 如果当前TextView内容长度大于1，则进行空格添加
            var i = 1
            while (i < builder.toString().length) {
                // 小demo：100  1 0 0
                // 按照x轴等比例进行缩放 通过我们设置的字间距+1除以10进行等比缩放
                finalText.setSpan(ScaleXSpan((spacing + 1) / 10), i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                i += 2
            }
        }
        super.setText(finalText, BufferType.SPANNABLE)
    }

    object Spacing {
        const val NORMAL = 0f
    }
}