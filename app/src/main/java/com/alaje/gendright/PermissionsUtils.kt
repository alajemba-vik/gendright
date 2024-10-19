package com.alaje.gendright

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object PermissionsUtils {
    fun openPermissionToDrawOverOtherAppsSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )

        (context as? Activity)?.startActivity(intent)
    }

    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        (context as? Activity)?.startActivity(intent)
    }

    fun createDrawOverAppsPermissionJob(
        canDrawOverApps: () -> Boolean,
        context: Context,
        shouldEnable: Boolean
    ): Job {

        return  monitorPermissionsState(
            context,
            shouldEnable,
            condition = canDrawOverApps,
            onEnd = { }
        )
    }

    fun createAccessibilityPermissionJob(
        context: Context,
        hasAccessibilityPermission: () -> Boolean,
        shouldEnable: Boolean,
    ): Job {
        return monitorPermissionsState(
            context,
            shouldEnable,
            condition = hasAccessibilityPermission,
            onEnd = {}
        )
    }

    private fun monitorPermissionsState(
        context: Context,
        shouldEnable: Boolean,
        condition: () -> Boolean,
        onEnd: () -> Unit
    ): Job {
        return CoroutineScope(Dispatchers.Main).launch {

            if (!shouldEnable) {
                while (condition()) {
                    delay(300)
                }
            } else {
                while (!condition()) {
                    delay(300)
                }
            }

            onEnd()

            (context as? Activity)?.bringAppBackToTop()
        }
    }

    // Once permission is granted, bring the app to the foreground
    private fun Activity.bringAppBackToTop() {
        Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show()

        // Bring GendRight back to the foreground
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }

}
