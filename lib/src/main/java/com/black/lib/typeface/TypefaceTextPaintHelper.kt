package com.black.lib.typeface

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextUtils
import android.view.Gravity
import com.black.lib.typeface.TypefaceManager.get
import java.util.*

class TypefaceTextPaintHelper(private val context: Context, private val paint: Paint, private val typefaceType: Int, text: String?) {
    private val text: String = text ?: ""
    private var list: List<Item>? = null
    var length = 0f
        private set
    var height = 0f
        private set

    //根据目标点绘制，默认左对齐
    fun draw(canvas: Canvas, x: Float, y: Float, gravity: Int) {
        var x = x
        var y = y
        if (height == 0f || length == 0f || list == null) {
            calculateSize()
        }
        //计算绘制开始位置
        if (gravity and Gravity.TOP == Gravity.TOP) {
            y += height
        } else if (gravity and Gravity.BOTTOM == Gravity.BOTTOM) {
            y = y
        } else if (gravity and Gravity.CENTER_VERTICAL == Gravity.CENTER_VERTICAL) {
            y += height / 2
        }
        if (gravity and Gravity.RIGHT == Gravity.RIGHT) {
            x -= length
        } else if (gravity and Gravity.LEFT == Gravity.LEFT) {
        } else if (gravity and Gravity.CENTER_HORIZONTAL == Gravity.CENTER_HORIZONTAL) {
            x -= length / 2
        }
        //绘制
        var offsetX = x
        canvas.save()
        for (i in list!!.indices) {
            val item = list!![i]
            paint.typeface = item.typeface
            canvas.drawText(item.text, offsetX, y - (height - item.height) / 2, paint)
            offsetX += item.length
        }
        canvas.restore()
    }

    fun calculateSize() {
        if (TextUtils.isEmpty(text)) {
            return
        }
        //先计算出使用字体的情况
        list = itemList
        //计算总长度和最大高度
        var length = 0f
        var maxHeight = 0f
        for (i in list!!.indices) {
            val item = list!![i]
            paint.typeface = item.typeface
            val itemLength = getTextLength(paint, item.text)
            val itemHeight = getTextHeight(paint)
            item.length = itemLength
            item.height = itemHeight
            length += itemLength
            maxHeight = Math.max(maxHeight, itemHeight)
        }
        this.length = length
        height = maxHeight
    }

    //        String regex = "([\\u4e00-\\u9fa5]+)";
//        Matcher matcher = Pattern.compile(regex).matcher(text);
//        int lastStart = 0;
//        int lastEnd = 0;
//        while (matcher.find()) {
//            int start = matcher.start();
//            int end = matcher.end();
//            if (start > 0 && lastEnd < start) {
//                list.add(new Item(text.substring(lastEnd, start), getOtherTypeface()));
//            }
//            list.add(new Item(text.substring(start, end), getChineseTypeface()));
//            lastStart = start;
//            lastEnd = end;
//        }
//        if (lastEnd < text.length()) {
//            list.add(new Item(text.substring(lastEnd, text.length()), getOtherTypeface()));
//        }
    private val itemList: List<Item>
        get() {
            val list: MutableList<Item> = ArrayList()
            list.add(Item(text, paint.typeface))
            //        String regex = "([\\u4e00-\\u9fa5]+)";
            //        Matcher matcher = Pattern.compile(regex).matcher(text);
            //        int lastStart = 0;
            //        int lastEnd = 0;
            //        while (matcher.find()) {
            //            int start = matcher.start();
            //            int end = matcher.end();
            //            if (start > 0 && lastEnd < start) {
            //                list.add(new Item(text.substring(lastEnd, start), getOtherTypeface()));
            //            }
            //            list.add(new Item(text.substring(start, end), getChineseTypeface()));
            //            lastStart = start;
            //            lastEnd = end;
            //        }
            //        if (lastEnd < text.length()) {
            //            list.add(new Item(text.substring(lastEnd, text.length()), getOtherTypeface()));
            //        }
            return list
        }

    fun getTextLength(paint: Paint, text: String?): Float {
        return if (TextUtils.isEmpty(text)) 0.toFloat() else paint.measureText(text)
    }

    fun getTextHeight(paint: Paint): Float { //        return paint.getFontMetrics().descent - paint.getFontMetrics().ascent;
        return Math.abs(paint.ascent()) - paint.descent()
    }

    private fun getShowTextCenterVerticalY(y: Float, paint: Paint): Float {
        return y + (Math.abs(paint.ascent()) - paint.descent()) / 2
    }

    private fun getChineseTypeface(): Typeface? {
        return if (Typeface.BOLD and typefaceType == Typeface.BOLD) {
            get(context, TypefaceManager.CHINESE_BOLD)
        } else {
            get(context, TypefaceManager.CHINESE_NORMAL)
        }
    }

    private fun getOtherTypeface(): Typeface? {
        return if (Typeface.BOLD and typefaceType == Typeface.BOLD) {
            get(context, TypefaceManager.OTHER_BOLD)
        } else {
            get(context, TypefaceManager.OTHER_NORMAL)
        }
    }

    internal inner class Item(var text: String, var typeface: Typeface?) {
        var length = 0f
        var height = 0f

    }
}
