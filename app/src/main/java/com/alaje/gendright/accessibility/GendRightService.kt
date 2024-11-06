package com.alaje.gendright.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import androidx.core.view.isVisible
import com.alaje.gendright.R
import com.alaje.gendright.di.AppContainer
import com.alaje.gendright.utils.BiasReader
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class GendRightService : AccessibilityService() {
    private val localDataSource = AppContainer.instance?.localDataSource

    private val biasReader = BiasReader()
    private var processTextJob: Job = Job()
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }
    private var coroutineScope = CoroutineScope(Dispatchers.IO + coroutineExceptionHandler)

    private var uiManager = GendRightServiceUIManager(
        this,
        biasReader,
        coroutineScope
    )

    private var isYetToSeeKeyboard = false

    override fun onServiceConnected() {
        super.onServiceConnected()

        setTheme(R.style.Theme_GendRight)

        configureServiceInfo()

        uiManager.listenForPerAPIResponse()
    }

    private fun configureServiceInfo() {
        serviceInfo.apply {

            eventTypes = AccessibilityEvent.TYPE_VIEW_FOCUSED or
                    AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or
                    AccessibilityEvent.CONTENT_CHANGE_TYPE_TEXT or
                    AccessibilityEvent.TYPE_VIEW_CLICKED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED

            feedbackType = AccessibilityServiceInfo.FEEDBACK_VISUAL

            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                    AccessibilityServiceInfo.CAPABILITY_CAN_RETRIEVE_WINDOW_CONTENT or
                    AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                flags = flags or AccessibilityServiceInfo.FLAG_INPUT_METHOD_EDITOR or
                        AccessibilityServiceInfo.FLAG_REQUEST_ACCESSIBILITY_BUTTON
            }

            notificationTimeout = accessibilityEventsTimeout
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.d("GendRightService", "onAccessibilityEvent: $event")
        Log.d("GendRightService", "nodeInfo: ${event?.source}")
        val eventAccessibilityNodeInfo = event?.source ?: return
        val eventType = event.eventType

        uiManager.apply {
            if (suggestionsLayout?.isVisible == true && hasClickedOutside(event)) {
                suggestionsLayout?.visibility = View.GONE
            }

            if (
                (eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED ||
                        eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) &&
                eventAccessibilityNodeInfo.isInputField()
            ) {
                isYetToSeeKeyboard = true
                if (packageName == event.packageName) {
                    // Only display walkthrough on Gendright app
                    val hasSeenWalkthrough =
                        localDataSource?.checkUserSeenWalkthroughOnQuickTest() == true

                    if (!hasSeenWalkthrough) {
                        localDataSource?.setUserSeenWalkthroughOnQuickTest()
                        uiManager.displayOnboardingHighlightUI()
                    }
                }

                if (uiManager.floatingWidgetLayout?.isVisible != true && Settings.canDrawOverlays(
                        this@GendRightService
                    )
                ) {
                    uiManager.displayFloatingWidget()
                }

                val inputText = eventAccessibilityNodeInfo.nodeInfoText

                if (inputText.trim().contains(" ") && !uiManager.didUserJustAcceptSuggestion) {

                    Log.d("GendRightService", "Canceling job for new text: $inputText")
                    processTextJob.cancel()

                    uiManager.lastEventAccessibilityNodeInfo = eventAccessibilityNodeInfo

                    processTextJob = coroutineScope.launch {
                        biasReader.readText(inputText)
                    }
                }

                if (uiManager.didUserJustAcceptSuggestion) {
                    uiManager.didUserJustAcceptSuggestion = false
                }
            }

            val hasInputMethod = windows.firstOrNull {
                it.type == AccessibilityWindowInfo.TYPE_INPUT_METHOD
            } != null

            if (hasInputMethod) {
                isYetToSeeKeyboard = false
                if (uiManager.floatingWidgetLayout?.isVisible != true) {
                    uiManager.displayFloatingWidget()
                }
            }

            if (!isYetToSeeKeyboard && !hasInputMethod) {
                uiManager.hideFloatingWidget()
                Log.d("GendRightService", "No input method found")
            }
        }

    }

    override fun onInterrupt() {}

    override fun onUnbind(intent: Intent?): Boolean {
        processTextJob.cancel()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        uiManager.onDestroy()
        super.onDestroy()
    }
}

private const val accessibilityEventsTimeout = 300L