package org.eclipse.paho.android.androidmqtt

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.view.MotionEvent
import android.view.View

internal infix fun View.onClick(function: () -> Unit) {
    setOnClickListener { function() }
}

/**
 * 添加点击缩放效果
 */
@SuppressLint("ClickableViewAccessibility")
fun View.addClickScale(scale: Float = 0.9f, duration: Long = 150) {
    this.setOnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                this.animate().scaleX(scale).scaleY(scale).setDuration(duration).start()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                this.animate().scaleX(1f).scaleY(1f).setDuration(duration).start()
            }
        }
        // 点击事件处理，交给View自身
        this.onTouchEvent(event)
    }
}

/**
 * 判断某个服务是否正在运行的方法
 *
 * @param mContext
 * @param serviceName 是包名+服务的类名
 * @return true代表正在运行，false代表服务没有正在运行
 */
fun isServiceWork(mContext: Context, serviceName: String): Boolean {
    var isWork = false
    val myAM = mContext
        .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val myList: List<ActivityManager.RunningServiceInfo> = myAM.getRunningServices(40)
    if (myList.isEmpty()) {
        return false
    }
    for (i in myList.indices) {
        val mName: String = myList[i].service.className
        if (mName == serviceName) {
            isWork = true
            break
        }
    }
    return isWork
}