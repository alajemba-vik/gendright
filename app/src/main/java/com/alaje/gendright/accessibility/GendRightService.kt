package com.alaje.gendright.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class GendRightService: AccessibilityService() {
    private var textFieldsCache: MutableMap<String, String> = mutableMapOf()

    private var job = Job()
    private var coroutineScope = CoroutineScope(Dispatchers.IO + job)


    override fun onServiceConnected() {
        super.onServiceConnected()

        TODO("Launch the app and let the user know what they can do with GendRight")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        //"Check if it's an input text change event and access Gemini API"
        event?.source?.apply {
            if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {

                coroutineScope.cancel()
                coroutineScope.launch {

                    delay(1000)
                    val transformedTextArguments = Bundle()
                    var transformedText = "Safe text";

                    if (isEditable && isVisibleToUser && isFocused) {
                        val text = text.toString()

                        if (text.isBlank()) return@launch

                        // Check if the text has been transformed before and use the cached data
                        val cachedData = textFieldsCache[text]
                        if (cachedData != null) {
                            // Set the cached data
                            transformedText = cachedData
                        } else {
                            //TODO("Make API call to Gemini API")

                            // Cache the transformed text
                            textFieldsCache[text] = transformedText
                        }

                        transformedTextArguments.putCharSequence(
                            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                            transformedText
                        )

                        withContext(Dispatchers.Main) {
                            performAction(
                                AccessibilityNodeInfo.ACTION_SET_TEXT,
                                transformedTextArguments
                            )
                        }
                    }
                }
            }

        }


    }

    override fun onInterrupt() {
        TODO("Stop the API call")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
        TODO("Clean up")
    }
}