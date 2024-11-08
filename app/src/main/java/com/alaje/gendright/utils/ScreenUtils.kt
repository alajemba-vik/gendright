package com.alaje.gendright.utils

import android.content.Context
import android.util.DisplayMetrics
import android.util.Size
import android.view.WindowManager
import kotlin.math.roundToInt

object ScreenUtils {
    private val getWindowManager = { context: Context ->
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    fun screenSize(context: Context): Size {
        val windowManager = getWindowManager(context)

        val displayMetrics = DisplayMetrics()
        val display = windowManager.defaultDisplay

        display?.getMetrics(displayMetrics)

        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels

        return Size(width, height)
    }

    fun pxToDp(context: Context, px: Float): Int {
        return (px * context.resources.displayMetrics.density).roundToInt()
    }
}