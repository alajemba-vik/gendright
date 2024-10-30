package com.alaje.gendright.utils

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.alaje.gendright.accessibility.GendRightService

object PermissionUtils {
    fun canDrawOverApps(context: Context) = Settings.canDrawOverlays(context)

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        return try {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                enabledServices.contains(GendRightService::class.java.`package`?.name ?: "")
            } else {
                false
            }
        } catch (e: Exception) {
            return false
        }
    }
}