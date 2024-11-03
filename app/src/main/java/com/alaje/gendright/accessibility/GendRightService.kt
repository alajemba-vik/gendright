package com.alaje.gendright.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.accessibility.AccessibilityEvent
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
        val eventAccessibilityNodeInfo = event?.source ?: return
        val eventType = event.eventType

        if (eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED || eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
            if (eventAccessibilityNodeInfo.isEditable &&
                eventAccessibilityNodeInfo.isVisibleToUser &&
                eventAccessibilityNodeInfo.isFocused &&
                !eventAccessibilityNodeInfo.text.isNullOrBlank()
            ) {

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

                val inputText = eventAccessibilityNodeInfo.text.toString()
                    .removeSuffix("Compose Message") // some texts have this suffix

                if (inputText.trim().contains(" ") && !uiManager.didUserJustAcceptSuggestion) {

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
        }

        if (uiManager.suggestionsLayout?.isVisible == true && uiManager.shouldHideSuggestionsLayout(
                event
            )
        ) {
            uiManager.suggestionsLayout?.visibility = View.GONE
        }
    }

    override fun onInterrupt() {
        processTextJob.cancel()
    }

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