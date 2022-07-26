package com.black.lib.permission

interface PermissionHelper {
    fun requestCameraPermissions(callback: Runnable?)
    fun requestStoragePermissions(callback: Runnable?)
    fun requestCallPermissions(callback: Runnable?)
    fun requestMicrophonePermissions(callback: Runnable?)
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray)
}
