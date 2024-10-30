package com.alaje.gendright.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
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
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import com.alaje.gendright.R
import com.alaje.gendright.utils.BiasReader
import com.alaje.gendright.utils.ScreenUtils
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class GendRightService: AccessibilityService() {
    private val biasReader = BiasReader()
    private var job: Job = Job()

    val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }
    private var coroutineScope = CoroutineScope(Dispatchers.IO + coroutineExceptionHandler)

    private var floatingWidget: View? = null
    private var floatingWidgetAnimator: ObjectAnimator? = null
    private var suggestionsLayout: View? = null
    private var suggestionTextView1: TextView? = null
    private var suggestionTextView2: TextView? = null
    private var suggestionTextView3: TextView? = null

    private lateinit var windowManager: WindowManager

    private var lastEventAccessibilityNodeInfo: AccessibilityNodeInfo? = null

    override fun onServiceConnected() {
        super.onServiceConnected()

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

        coroutineScope.launch {
            biasReader.isProcessing.collect {
                withContext(Dispatchers.Main) {
                    if (it) {
                        floatingWidgetAnimator?.start()
                    } else {
                        floatingWidgetAnimator?.end()
                    }
                }
            }
        }


    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        //"Check if it's an input text change event and access Gemini API"
        event?.source?.apply {
            if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED || event.eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED) {

                if (isEditable && isVisibleToUser && isFocused) {
                    if (floatingWidget?.isVisible != true && Settings.canDrawOverlays(this@GendRightService)){
                        displayFloatingWidget()
                    }

                    val text = text.toString()

                    if (text.trim().contains(" ")) {

                        job.cancel()

                        job = coroutineScope.launch {
                            delay(1000)

                            biasReader.readText(text)

                            lastEventAccessibilityNodeInfo = this@apply
                        }
                    }
                }
            }

        }


    }

    override fun onInterrupt() {
        job.cancel()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        job.cancel()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {

        floatingWidget?.let {
            windowManager.removeView(it)
        }
        suggestionsLayout?.let {
            windowManager.removeView(it)
        }

        super.onDestroy()
    }

    private fun AccessibilityNodeInfo.updateInput(
        transformedText: String
    ) {
        val transformedTextArguments = Bundle()

        transformedTextArguments.putCharSequence(
            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
            transformedText
        )

        performAction(
            AccessibilityNodeInfo.ACTION_SET_TEXT,
            transformedTextArguments
        )
    }

    private fun displayFloatingWidget() {
        floatingWidget = LayoutInflater.from(this).inflate(R.layout.floating_widget, null)
        floatingWidgetAnimator = floatingWidget?.prepareFloatingWidgetAnimator()

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            android.graphics.PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = ScreenUtils.screenSize(this).height/2

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(floatingWidget, params)

        floatingWidget?.rootView?.setOnClickListener {
            if (suggestionsLayout?.isVisible != true) {
                displaySuggestionsWidget()
            } else {
                suggestionsLayout?.visibility = View.GONE
            }
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

    private fun displaySuggestionsWidget() {
        suggestionsLayout = LayoutInflater.from(this).inflate(R.layout.suggestions_layout, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            android.graphics.PixelFormat.TRANSLUCENT
        )

        floatingWidget?.rootView?.let {
            params.height = 500
        }

        params.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        params.x = 0

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(suggestionsLayout, params)

        suggestionsLayout?.apply {
            suggestionTextView1 = findViewById(R.id.suggestion_1)
            suggestionTextView2 = findViewById(R.id.suggestion_2)
            suggestionTextView3 = findViewById(R.id.suggestion_3)

            val apiResponse =
                biasReader.textFieldsCache[lastEventAccessibilityNodeInfo?.text.toString()]
            suggestionTextView1?.text = apiResponse?.suggestions?.firstOrNull() ?: ""
            suggestionTextView2?.text = apiResponse?.suggestions?.getOrNull(1) ?: ""
            suggestionTextView3?.text = apiResponse?.suggestions?.getOrNull(2) ?: ""

            findViewById<Button>(R.id.accept_suggestion_negative_button)?.setOnClickListener {
                suggestionsLayout?.visibility = View.GONE
            }

            findViewById<Button>(R.id.accept_suggestion_positive_button)?.setOnClickListener {
                lastEventAccessibilityNodeInfo?.updateInput(
                    suggestionTextView1?.text.toString()
                )
                suggestionsLayout?.visibility = View.GONE
            }
        }

    }
}

private fun View.prepareFloatingWidgetAnimator(): ObjectAnimator {
    val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.1f, 1f)
    val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.1f, 1f)
    val animator = ObjectAnimator.ofPropertyValuesHolder(this, scaleX, scaleY)
    animator.duration = 1000
    animator.repeatCount = ObjectAnimator.INFINITE
    return animator
}