package com.devmeng.skinlib.skin

import android.app.Activity
import android.app.Application
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import com.devmeng.skinlib.skin.utils.Log
import com.devmeng.skinlib.skin.utils.SkinPreference
import com.devmeng.skinlib.skin.utils.SkinThemeUtils
import java.lang.ref.WeakReference

/**
 * Created by devmeng
 * Version : 1
 * Description :
 * 以切面编程的形式为每个 Activity 配置其相应的 LayoutFactory 从做换肤的准备
 * 并且对每一个 Factory 进行观察者注册
 *
 */
class SkinActivityLifecycle(private val activities: MutableList<String>) :
    Application.ActivityLifecycleCallbacks {

    private val factoryMap: HashMap<Activity, SkinLayoutFactory> = hashMapOf()

    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (singleApplySkin(activity)) {
            return
        }
        //加载皮肤包 字体
        var skinTypeface: Typeface? = null
        val layoutInflater = LayoutInflater.from(activity)
        if (IS_APPLICATION_TYPEFACE) {
            try {
                skinTypeface = SkinThemeUtils.getSkinTypeface(activity)
            } catch (e: Exception) {
                Log.e(e.stackTraceToString())
            }
        }
        val factory = SkinLayoutFactory(activity, skinTypeface)
        layoutInflater.factory2 = factory
        //注册观察者
        SkinManager.instance.addObserver(factory)
        factoryMap[activity] = factory
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
        if (singleApplySkin(activity)) {
            return
        }
        //防止重启状态栏还原
        SkinThemeUtils.updateStatusBarState(activity)

        SkinManager.instance.loadSkin(SkinPreference.instance.getSkinPath())
    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {
        if (singleApplySkin(activity)) {
            return
        }
        //取消观察者
        val factory = factoryMap.remove(activity)
        SkinManager.instance.deleteObserver(factory)
    }

    private fun singleApplySkin(activity: Activity): Boolean {
        if ((activities.size > 0).and(
                !activities.contains(
                    WeakReference(activity).get()?.componentName?.shortClassName?.substring(1)
                )
            )
        ) {
            return true
        }
        return false
    }
}