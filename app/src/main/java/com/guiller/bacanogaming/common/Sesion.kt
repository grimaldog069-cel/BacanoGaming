package com.guiller.bacanogaming.common

import android.app.Activity
import android.content.Intent

object Sesion {
    fun cerrar(activity: Activity) {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(intent)
    }
}