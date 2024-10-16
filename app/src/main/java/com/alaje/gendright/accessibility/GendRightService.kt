package com.alaje.gendright.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


class GendRightService: AccessibilityService() {
    private var textFieldsCache: MutableMap<String, String> = mutableMapOf()

    private var job = Job()
    private var coroutineScope = CoroutineScope(Dispatchers.IO + job)


    override fun onServiceConnected() {
        super.onServiceConnected()

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
        return super.onUnbind(intent)

    }

    /*private fun getTextFieldNodeInfo(source: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var current = source
        while (true) {
            val parent = current.parent ?: return null
            if (TASK_LIST_VIEW_CLASS_NAME.equals(parent.className)) {
                return current
            }
            // NOTE: Recycle the infos.
            val oldCurrent = current
            current = parent
        }
    }*/

}