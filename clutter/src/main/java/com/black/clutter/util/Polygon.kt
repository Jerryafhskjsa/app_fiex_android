package com.black.clutter.util

import android.graphics.Path
import android.graphics.PointF
import com.black.util.CommonUtil
import java.util.*

//多边形
class Polygon(private val centerX: Float, private val centerY: Float, private val radius: Double, private val edgeCount: Int) {
    private val center: PointF = PointF(centerX, centerY)
    private val vertexes: ArrayList<PointF>? = ArrayList()
    fun atPosition(centerX: Float, centerY: Float): Boolean {
        return this.centerX == centerX && this.centerY == centerY
    }

    //初始化图形，计算顶点位置
    fun initGraphics() {
        if (edgeCount < 3) {
            throw RuntimeException("edgeCount must not less than 3.")
        }
        if (radius < 1) {
            throw RuntimeException("radius to small.")
        }
        vertexes!!.clear()
        for (i in 0 until edgeCount) {
            vertexes.add(CommonUtil.getPointRotate(center, radius, i.toDouble() * 360 / edgeCount))
        }
    }

    val path: Path?
        get() {
            if (vertexes != null && vertexes.isNotEmpty()) {
                val path = Path()
                for (i in vertexes.indices) {
                    val point = vertexes[i]
                    if (i == 0) {
                        path.moveTo(point.x, point.y)
                    } else {
                        path.lineTo(point.x, point.y)
                    }
                }
                val firstPoint = vertexes[0]
                path.lineTo(firstPoint.x, firstPoint.y)
                return path
            }
            return null
        }

}