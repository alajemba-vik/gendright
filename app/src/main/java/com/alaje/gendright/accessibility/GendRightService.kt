package com.alaje.gendright.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.view.isVisible
import com.alaje.gendright.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


class GendRightService: AccessibilityService() {
    private var textFieldsCache: MutableMap<String, String> = mutableMapOf()

    private var job = Job()
    private var coroutineScope = CoroutineScope(Dispatchers.IO + job)

    private var floatingWidget: View? = null
    private lateinit var windowManager: WindowManager

    override fun onServiceConnected() {
        super.onServiceConnected()

        getSharedPreferences("gendright", MODE_PRIVATE)
            .edit()
            .putBoolean("serviceEnabled", true)
            .apply()

        //TODO("Launch the app and let the user know what they can do with GendRight")
        serviceInfo.apply {

            eventTypes = AccessibilityEvent.TYPE_VIEW_FOCUSED or AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or AccessibilityEvent.CONTENT_CHANGE_TYPE_TEXT

            feedbackType = AccessibilityServiceInfo.FEEDBACK_VISUAL

            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or AccessibilityServiceInfo.CAPABILITY_CAN_RETRIEVE_WINDOW_CONTENT

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                flags = flags or AccessibilityServiceInfo.FLAG_INPUT_METHOD_EDITOR or
                        AccessibilityServiceInfo.FLAG_REQUEST_ACCESSIBILITY_BUTTON
            }

            notificationTimeout = 300
        }

    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        //"Check if it's an input text change event and access Gemini API"
        event?.source?.apply {
            if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED || event.eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED) {

                val transformedTextArguments = Bundle()
                var transformedText = ""

                if (isEditable && isVisibleToUser && isFocused) {
                    if (floatingWidget?.isVisible != true && Settings.canDrawOverlays(this@GendRightService)){
                        displayFloatingWidget()
                    }

                    val text = text.toString()

                    if (text.isBlank() || !text.contains( " ")) return

                    coroutineScope.launch {
                        // Check if the text has been transformed before and use the cached data
                        val cachedData = textFieldsCache[text]
                        if (cachedData != null) {
                            // Set the cached data
                            transformedText = cachedData
                        } else {

                            //TODO("Make API call to Gemini API and assign new value to")


                            // Cache the transformed text
                            textFieldsCache[text] = transformedText
                        }

                        updateInput(transformedTextArguments, transformedText)
                    }
                }
            }

        }


    }

    private fun AccessibilityNodeInfo.updateInput(
        transformedTextArguments: Bundle,
        transformedText: String
    ) {
        transformedTextArguments.putCharSequence(
            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
            transformedText
        )

        performAction(
            AccessibilityNodeInfo.ACTION_SET_TEXT,
            transformedTextArguments
        )
    }

    override fun onInterrupt() {
        job.cancel()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        coroutineScope.cancel()
        getSharedPreferences("gendright", MODE_PRIVATE)
            .edit()
            .putBoolean("serviceEnabled", false)
            .apply()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {

        floatingWidget?.let {
            windowManager.removeView(it)
        }

        super.onDestroy()
    }

    fun displayFloatingWidget() {
        floatingWidget = LayoutInflater.from(this).inflate(R.layout.floating_widget, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            android.graphics.PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0;
        params.y = 100;

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(floatingWidget, params)

        floatingWidget?.rootView?.setOnClickListener {
            //TODO("Show the suggestions dialog")
        }

        floatingWidget?.rootView?.setOnTouchListener(
            object: View.OnTouchListener {
                var initialX = 0
                var initialY = 0
                var initialTouchX = 0f
                var initialTouchY = 0f
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    when (event?.action) {
                        MotionEvent.ACTION_DOWN -> {
                            initialX = params.x
                            initialY = params.y
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            return true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            params.x = initialX + (event.rawX - initialTouchX).toInt()
                            params.y = initialY + (event.rawY - initialTouchY).toInt()
                            windowManager.updateViewLayout(floatingWidget, params)
                            return true
                        }
                        MotionEvent.ACTION_UP -> {
                            v?.performClick()
                            return true
                        }
                        else -> return false
                    }
                }

            }
        )
    }

    companion object{
        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                enabledServices.contains(GendRightService::class.java.`package`?.name ?: "")
            } else {
                false
            }
        }
    }
}