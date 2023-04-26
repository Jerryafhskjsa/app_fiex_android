package com.black.frying.contract

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.black.frying.contract.state.FutureGlobalStateViewModel
import java.lang.reflect.ParameterizedType

/**
 * base类:
 *      1、持有globalViewModel成员变量，和activity生命周期绑定
 *      2、声明时传入布局viewBinding即可确定初始化布局
 *      3、子类只需要实现startInit初始化布局和绑定数据
 */
abstract class FutureBaseFragment<T : ViewBinding> : Fragment() {
    protected val globalVm: FutureGlobalStateViewModel by lazy {
        getDefaultViewModel<FutureGlobalStateViewModel>()
    }

    protected var defaultVmMap: MutableMap<Class<*>, ViewModel> = HashMap()

    protected val contentViewBinding: T by lazy {
        val type = javaClass.genericSuperclass as ParameterizedType
        val cls = type.actualTypeArguments[0] as Class<*>
        val inflate = cls.getDeclaredMethod("inflate", LayoutInflater::class.java)
        return@lazy inflate.invoke(null, layoutInflater) as T
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return contentViewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startInit(contentViewBinding)
    }

    protected inline fun <reified VM : ViewModel> getDefaultViewModel(vararg parameters: Any = emptyArray()): VM {
        val clazz = VM::class.java
        var selectVm = defaultVmMap[clazz]
        if (selectVm != null) {
            return selectVm as VM
        }
        val empty = parameters.isEmpty()
        if (empty) {
            return ViewModelProvider(this).get(clazz).apply {
                defaultVmMap[clazz] = this
            }
        }
        return ViewModelProvider(this,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(clazz)) {
                        var parameterTypes: Array<Class<Any>> =
                            parameters.map { it.javaClass }.toTypedArray()
                        return clazz.getConstructor(*parameterTypes).newInstance(*parameters) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            })[clazz].apply {
            defaultVmMap[clazz] = this
        }
    }

    abstract fun startInit(contentViewBinding: T)

}