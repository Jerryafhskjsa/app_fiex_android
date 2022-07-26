package com.black.lib.camera

import android.os.IBinder
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * This class is used to activate the weak light on some camera phones (not flash)
 * in order to illuminate surfaces for scanning. There is no official way to do this,
 * but, classes which allow access to this function still exist on some devices.
 * This therefore proceeds through a great deal of reflection.
 *
 *
 * See [
 * http://almondmendoza.com/2009/01/05/changing-the-screen-brightness-programatically/](http://almondmendoza.com/2009/01/05/changing-the-screen-brightness-programatically/) and
 * [
 * http://code.google.com/p/droidled/source/browse/trunk/src/com/droidled/demo/DroidLED.java](http://code.google.com/p/droidled/source/browse/trunk/src/com/droidled/demo/DroidLED.java).
 * Thanks to Ryan Alford for pointing out the availability of this class.
 */
internal object FlashlightManager {
    private val TAG = FlashlightManager::class.java.simpleName
    private var iHardwareService: Any? = null
    private var setFlashEnabledMethod: Method? = null

    init {
        iHardwareService = hardwareService
        setFlashEnabledMethod = getSetFlashEnabledMethod(iHardwareService)
        if (iHardwareService == null) {
        } else {
        }
    }

    /**
     * �����������ƿ���
     */
//FIXME
    fun enableFlashlight() {
        setFlashlight(false)
    }

    fun disableFlashlight() {
        setFlashlight(false)
    }

    private val hardwareService: Any?
        get() {
            val serviceManagerClass = maybeForName("android.os.ServiceManager") ?: return null
            val getServiceMethod = maybeGetMethod(serviceManagerClass, "getService", String::class.java)
                    ?: return null
            val hardwareService = invoke(getServiceMethod, null, "hardware") ?: return null
            val iHardwareServiceStubClass = maybeForName("android.os.IHardwareService\$Stub")
                    ?: return null
            val asInterfaceMethod = maybeGetMethod(iHardwareServiceStubClass, "asInterface", IBinder::class.java)
                    ?: return null
            return invoke(asInterfaceMethod, null, hardwareService)
        }

    private fun getSetFlashEnabledMethod(iHardwareService: Any?): Method? {
        if (iHardwareService == null) {
            return null
        }
        val proxyClass: Class<*> = iHardwareService.javaClass
        return maybeGetMethod(proxyClass, "setFlashlightEnabled", Boolean::class.javaPrimitiveType as Class<*>)
    }

    private fun maybeForName(name: String): Class<*>? {
        return try {
            Class.forName(name)
        } catch (cnfe: ClassNotFoundException) { // OK
            null
        } catch (re: RuntimeException) {
            null
        }
    }

    private fun maybeGetMethod(clazz: Class<*>, name: String, vararg argClasses: Class<*>): Method? {
        return try {
            clazz.getMethod(name, *argClasses)
        } catch (nsme: NoSuchMethodException) { // OK
            null
        } catch (re: RuntimeException) {
            null
        }
    }

    private operator fun invoke(method: Method?, instance: Any?, vararg args: Any): Any? {
        return try {
            method!!.invoke(instance, *args)
        } catch (e: IllegalAccessException) {
            null
        } catch (e: InvocationTargetException) {
            null
        } catch (re: RuntimeException) {
            null
        }
    }

    private fun setFlashlight(active: Boolean) {
        if (iHardwareService != null) {
            invoke(setFlashEnabledMethod, iHardwareService, active)
        }
    }
}