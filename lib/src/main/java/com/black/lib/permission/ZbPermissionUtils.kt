package com.black.lib.permission

import android.annotation.TargetApi
import android.app.Activity
import android.os.Build
import androidx.fragment.app.Fragment
import java.lang.reflect.Method
import java.util.*

object ZbPermissionUtils {
    val isOverMarshmallow: Boolean
        get() = Build.VERSION.SDK_INT >= 23

    @TargetApi(23)
    fun findDeniedPermissions(activity: Activity?, vararg permission: String): List<String?> {
        val denyPermissions: MutableList<String?> = ArrayList()
        val var3: Array<String> = permission as Array<String>
        val var4 = permission.size
        for (var5 in 0 until var4) {
            val value = var3[var5]
            if (activity?.checkSelfPermission(value) ?: 0 != 0) {
                denyPermissions.add(value)
            }
        }
        return denyPermissions
    }

    fun findAnnotationMethods(clazz: Class<*>, clazz1: Class<out Annotation?>?): List<Method?> {
        val methods: MutableList<Method?> = ArrayList()
        val var3 = clazz.declaredMethods
        val var4 = var3.size
        for (var5 in 0 until var4) {
            val method = var3[var5]
            if (method.isAnnotationPresent(clazz1)) {
                methods.add(method)
            }
        }
        return methods
    }

    fun <A : Annotation?> findMethodPermissionFailWithRequestCode(clazz: Class<*>, permissionFailClass: Class<A>?, requestCode: Int): Method? {
        val var3 = clazz.declaredMethods
        val var4 = var3.size
        for (var5 in 0 until var4) {
            val method = var3[var5]
            if (method.isAnnotationPresent(permissionFailClass) && requestCode == (method.getAnnotation(ZbPermissionFail::class.java) as ZbPermissionFail).requestCode) {
                return method
            }
        }
        return null
    }

    private fun isEqualRequestCodeFromAnnotation(m: Method, clazz: Class<*>, requestCode: Int): Boolean {
        return when (clazz) {
            ZbPermissionFail::class.java -> {
                requestCode == (m.getAnnotation(ZbPermissionFail::class.java) as ZbPermissionFail).requestCode
            }
            ZbPermissionSuccess::class.java -> {
                requestCode == (m.getAnnotation(ZbPermissionSuccess::class.java) as ZbPermissionSuccess).requestCode
            }
            else -> {
                false
            }
        }
    }

    fun <A : Annotation?> findMethodWithRequestCode(clazz: Class<*>, annotation: Class<A>, requestCode: Int): Method? {
        val var3 = clazz.declaredMethods
        val var4 = var3.size
        for (var5 in 0 until var4) {
            val method = var3[var5]
            if (method.isAnnotationPresent(annotation) && isEqualRequestCodeFromAnnotation(method, annotation, requestCode)) {
                return method
            }
        }
        return null
    }

    fun <A : Annotation?> findMethodPermissionSuccessWithRequestCode(clazz: Class<*>, permissionFailClass: Class<A>?, requestCode: Int): Method? {
        val var3 = clazz.declaredMethods
        val var4 = var3.size
        for (var5 in 0 until var4) {
            val method = var3[var5]
            if (method.isAnnotationPresent(permissionFailClass) && requestCode == (method.getAnnotation(ZbPermissionSuccess::class.java) as ZbPermissionSuccess).requestCode) {
                return method
            }
        }
        return null
    }

    fun getActivity(`object`: Any?): Activity? {
        return if (`object` is Fragment) {
            `object`.activity
        } else {
            if (`object` is Activity) `object` else null
        }
    }
}