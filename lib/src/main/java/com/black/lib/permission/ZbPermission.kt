package com.black.lib.permission

import android.annotation.TargetApi
import android.app.Activity
import androidx.fragment.app.Fragment
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*

class ZbPermission private constructor(private val `object`: Any) {
    companion object {
        private var zbPermissionCallback: ZbPermissionCallback? = null
        fun with(activity: Activity): ZbPermission {
            return ZbPermission(activity)
        }

        fun with(fragment: Fragment): ZbPermission {
            return ZbPermission(fragment)
        }

        fun needPermission(activity: Activity, requestCode: Int, permissions: Array<String>) {
            zbPermissionCallback = null
            requestPermissions(activity, requestCode, permissions)
        }

        fun needPermission(fragment: Fragment, requestCode: Int, permissions: Array<String>) {
            zbPermissionCallback = null
            requestPermissions(fragment, requestCode, permissions)
        }

        fun needPermission(activity: Activity, requestCode: Int, permissions: Array<String>, callback: ZbPermissionCallback?) {
            if (callback != null) {
                zbPermissionCallback = callback
            }
            requestPermissions(activity, requestCode, permissions)
        }

        fun needPermission(fragment: Fragment, requestCode: Int, permissions: Array<String>, callback: ZbPermissionCallback?) {
            if (callback != null) {
                zbPermissionCallback = callback
            }
            requestPermissions(fragment, requestCode, permissions)
        }

        fun needPermission(activity: Activity, requestCode: Int, permission: String) {
            zbPermissionCallback = null
            needPermission(activity, requestCode, arrayOf(permission))
        }

        fun needPermission(fragment: Fragment, requestCode: Int, permission: String) {
            zbPermissionCallback = null
            needPermission(fragment, requestCode, arrayOf(permission))
        }

        fun needPermission(activity: Activity, requestCode: Int, permission: String, callback: ZbPermissionCallback?) {
            if (callback != null) {
                zbPermissionCallback = callback
            }
            needPermission(activity, requestCode, arrayOf(permission))
        }

        fun needPermission(fragment: Fragment, requestCode: Int, permission: String, callback: ZbPermissionCallback?) {
            if (callback != null) {
                zbPermissionCallback = callback
            }
            needPermission(fragment, requestCode, arrayOf(permission))
        }

        @TargetApi(23)
        private fun requestPermissions(`object`: Any, requestCode: Int, permissions: Array<String>) {
            if (!ZbPermissionUtils.isOverMarshmallow) {
                if (zbPermissionCallback != null) {
                    zbPermissionCallback?.permissionSuccess(requestCode)
                } else {
                    doExecuteSuccess(`object`, requestCode)
                }
            } else {
                val deniedPermissions = ZbPermissionUtils.findDeniedPermissions(ZbPermissionUtils.getActivity(`object`), *permissions)
                if (deniedPermissions.size > 0) {
                    if (`object` is Activity) {
                        `object`.requestPermissions(deniedPermissions.toTypedArray() as Array<String>, requestCode)
                    } else {
                        require(`object` is Fragment) { `object`.javaClass.name + " is not supported" }
                        `object`.requestPermissions((deniedPermissions.toTypedArray() as Array<String>), requestCode)
                    }
                } else if (zbPermissionCallback != null) {
                    zbPermissionCallback?.permissionSuccess(requestCode)
                } else {
                    doExecuteSuccess(`object`, requestCode)
                }
            }
        }

        private fun doExecuteSuccess(activity: Any, requestCode: Int) {
            val executeMethod = ZbPermissionUtils.findMethodWithRequestCode(activity.javaClass, ZbPermissionSuccess::class.java, requestCode)
            Companion.executeMethod(activity, executeMethod)
        }

        private fun doExecuteFail(activity: Any, requestCode: Int) {
            val executeMethod = ZbPermissionUtils.findMethodWithRequestCode(activity.javaClass, ZbPermissionFail::class.java, requestCode)
            Companion.executeMethod(activity, executeMethod)
        }

        private fun executeMethod(activity: Any, executeMethod: Method?) {
            if (executeMethod != null) {
                try {
                    if (!executeMethod.isAccessible) {
                        executeMethod.isAccessible = true
                    }
                    executeMethod.invoke(activity, null)
                } catch (var3: IllegalAccessException) {
                } catch (var4: InvocationTargetException) {
                }
            }
        }

        fun onRequestPermissionsResult(activity: Activity, requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
            requestResult(activity, requestCode, permissions, grantResults)
        }

        fun onRequestPermissionsResult(fragment: Fragment, requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
            requestResult(fragment, requestCode, permissions, grantResults)
        }

        private fun requestResult(obj: Any, requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
            val deniedPermissions: MutableList<String?> = ArrayList()
            for (i in grantResults.indices) {
                if (grantResults[i] != 0) {
                    deniedPermissions.add(permissions[i])
                }
            }
            if (deniedPermissions.size > 0) {
                if (zbPermissionCallback != null) {
                    zbPermissionCallback?.permissionFail(requestCode)
                } else {
                    doExecuteFail(obj, requestCode)
                }
            } else if (zbPermissionCallback != null) {
                zbPermissionCallback?.permissionSuccess(requestCode)
            } else {
                doExecuteSuccess(obj, requestCode)
            }
        }
    }

    private lateinit var mPermissions: Array<String>
    private var mRequestCode = 0
    fun permissions(vararg permissions: String): ZbPermission {
        mPermissions = permissions as Array<String>
        return this
    }

    fun addRequestCode(requestCode: Int): ZbPermission {
        mRequestCode = requestCode
        return this
    }

    @TargetApi(23)
    fun request() {
        zbPermissionCallback = null
        requestPermissions(`object`, mRequestCode, mPermissions)
    }

    @TargetApi(23)
    fun request(callback: ZbPermissionCallback?) {
        if (callback != null) {
            zbPermissionCallback = callback
        }
        requestPermissions(`object`, mRequestCode, mPermissions)
    }

    interface ZbPermissionCallback {
        fun permissionSuccess(var1: Int)
        fun permissionFail(var1: Int)
    }
}