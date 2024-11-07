package com.alaje.gendright.accessibility

import android.accessibilityservice.AccessibilityService.WINDOW_SERVICE
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.FOCUS_INPUT
import androidx.core.view.isVisible
import com.alaje.gendright.R
import com.alaje.gendright.data.models.DataResponse
import com.alaje.gendright.utils.BiasReader
import com.alaje.gendright.utils.ScreenUtils
import kotlinx.coroutines.*

class GendRightServiceUIManager(
    private val gendRightService: GendRightService,
    private val biasReader: BiasReader,
    private val coroutineScope: CoroutineScope
) {
    private val layoutInflater by lazy { LayoutInflater.from(gendRightService) }
    private var onboardingOverlay: View? = null
    private var onboardingHighlightLayout: View? = null

    var floatingWidgetLayout: View? = null
    private var floatingWidgetAnimator: ObjectAnimator? = null
    private var isUserMovingFAB = false

    private var importantMessageTextView: TextView? = null
    private var displayImportantMessageJob: Job? = null

    var suggestionsLayout: View? = null
    private var suggestionTextView1: TextView? = null
    private var suggestionTextView2: TextView? = null
    private var suggestionTextView3: TextView? = null
    var didUserJustAcceptSuggestion = false

    val windowManager: WindowManager by lazy {
        gendRightService.getSystemService(WINDOW_SERVICE) as WindowManager
    }
    private val resources by lazy { gendRightService.resources }

    var lastEventAccessibilityNodeInfo: AccessibilityNodeInfo? = null

    fun displayFloatingWidget() {
        if (floatingWidgetLayout == null) {
            floatingWidgetLayout = layoutInflater.inflate(R.layout.floating_widget, null)
            floatingWidgetAnimator = floatingWidgetLayout?.prepareFloatingWidgetAnimator()

            val sizeInPx = gendRightService.resources.getDimensionPixelSize(
                R.dimen.floating_widget_parent_size
            )
            val params = createParams()

            params.gravity = fabGravity
            params.x = fabInitialX
            params.y = fabInitialY(gendRightService)

            windowManager.addView(floatingWidgetLayout, params)

            floatingWidgetLayout?.rootView?.setOnClickListener {
                if (isUserMovingFAB) {
                    isUserMovingFAB = false
                    return@setOnClickListener
                }

                val inputText = lastEventAccessibilityNodeInfo?.nodeInfoText ?: ""
                val hasSuggestions = biasReader.textFieldsCache[inputText]
                    ?.suggestions?.isNotEmpty() ?: false

                if (suggestionsLayout?.isVisible != true && hasSuggestions && lastEventAccessibilityNodeInfo?.isFocused == true) {
                    displaySuggestionsWidget()
                    toggleUnreadIndicator(false)
                } else {

                    if (isAPIErrorIndicatorVisible()) {
                        displayImportantMessageForShortTime()
                    }
                    suggestionsLayout?.visibility = View.GONE
                }
            }

            floatingWidgetLayout?.rootView?.setOnTouchListener(
                object : View.OnTouchListener {
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
                                windowManager.updateViewLayout(floatingWidgetLayout, params)
                                isUserMovingFAB = true
                                return true
                            }

                            MotionEvent.ACTION_UP -> {
                                val screenSize = ScreenUtils.screenSize(gendRightService)
                                val widthOfFAB = floatingWidgetLayout?.width ?: 0
                                val distanceToLeft = params.x
                                val distanceToRight = screenSize.width - widthOfFAB - params.x
                                params.x = if (distanceToLeft < distanceToRight) {
                                    0
                                } else {
                                    screenSize.width - widthOfFAB
                                }
                                windowManager.updateViewLayout(floatingWidgetLayout, params)

                                v?.performClick()
                                return true
                            }

                            else -> return false
                        }
                    }
                }
            )

        } else {
            floatingWidgetLayout?.visibility = View.VISIBLE
        }
    }

    private fun displayImportantMessageForShortTime() {
        val result = biasReader.response.value

        if (result is DataResponse.APIError) {
            if (importantMessageTextView == null) {
                importantMessageTextView = floatingWidgetLayout?.findViewById(R.id.important_message_textview)
            }

            importantMessageTextView?.apply {
                text = result.message.ifBlank {
                    context.getString(R.string.api_error_default_message)
                }
                setOnClickListener {
                    importantMessageTextView?.visibility = View.GONE
                }

                visibility = View.VISIBLE

            }

            displayImportantMessageJob?.cancel()
            displayImportantMessageJob = coroutineScope.launch {
                delay(4000L)
                withContext(Dispatchers.Main){
                    importantMessageTextView?.visibility = View.GONE
                    toggleAPIErrorIndicator(false)
                }
            }
        }


    }

    fun displayOnboardingHighlightUI() {
        displayOnboardingOverlay()

        onboardingHighlightLayout = layoutInflater
            .inflate(R.layout.onboarding_floating_widget_highlight, null)

        val params = createParams()
        params.x = fabInitialX

        val indicatorSize = resources.getDimensionPixelSize(
            R.dimen.floating_widget_highlight_indicator_size
        )
        val fabY = fabInitialY(gendRightService)
        val fabHeight = resources.getDimensionPixelSize(R.dimen.floating_widget_parent_size)
        params.y = fabY + (fabHeight / 2) - (indicatorSize / 2)

        params.gravity = fabGravity

        onboardingHighlightLayout?.translationX = -resources.getDimensionPixelSize(
            R.dimen.floating_widget_highlight_indicator_translationX
        ).toFloat()

        windowManager.addView(onboardingHighlightLayout, params)

        coroutineScope.launch {
            delay(5000L)
            removeWalkthroughUI()
        }
    }

    private fun displayOnboardingOverlay() {
        onboardingOverlay = layoutInflater
            .inflate(R.layout.onboarding_highlight_overlay, null)

        val params = createParams(
            width = WindowManager.LayoutParams.MATCH_PARENT,
            height = WindowManager.LayoutParams.MATCH_PARENT
        )

        onboardingOverlay?.setOnClickListener {
            removeWalkthroughUI()
        }

        windowManager.addView(onboardingOverlay, params)
    }

    private fun displaySuggestionsWidget() {
        if (gendRightService.findFocus(FOCUS_INPUT) == null) {
            return
        }
        if (suggestionsLayout == null) {
            suggestionsLayout = layoutInflater.inflate(R.layout.suggestions_layout, null)

            suggestionsLayout?.setupSuggestionsView()

            val params = createParams(
                width = WindowManager.LayoutParams.MATCH_PARENT,
            )

            params.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            params.x = 0

            windowManager.addView(suggestionsLayout, params)

        } else {
            suggestionsLayout?.setupSuggestionsView()

            suggestionsLayout?.visibility = View.VISIBLE
        }
    }

    private fun removeWalkthroughUI() {
        windowManager.removeView(onboardingHighlightLayout)
        windowManager.removeView(onboardingOverlay)
    }

    private fun toggleUnreadIndicator(show: Boolean) {
        floatingWidgetLayout?.findViewById<View>(R.id.unread_suggestion_indicator)
            ?.visibility = if (!show) View.GONE else View.VISIBLE
    }

    private fun toggleNoInternetIndicator(show: Boolean) {
        floatingWidgetLayout?.findViewById<View>(R.id.no_internet_connection_indicator)
            ?.visibility = if (!show) View.GONE else View.VISIBLE
    }

    private fun toggleAPIErrorIndicator(show: Boolean) {
        floatingWidgetLayout?.findViewById<View>(R.id.api_error_indicator)
            ?.visibility = if (!show) View.GONE else View.VISIBLE
    }

    private fun isAPIErrorIndicatorVisible(): Boolean {
        return floatingWidgetLayout?.findViewById<View>(R.id.api_error_indicator)?.isVisible == true
    }

    fun hasClickedOutside(event: AccessibilityEvent?): Boolean {
        if (lastEventAccessibilityNodeInfo == null) {
            return true
        }

        if (event?.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED && event.source?.packageName != gendRightService.packageName) {
            return true
        }

        return false
    }

    private fun View.setupSuggestionsView() {

        suggestionTextView1 = findViewById(R.id.suggestion_1)
        suggestionTextView2 = findViewById(R.id.suggestion_2)
        suggestionTextView3 = findViewById(R.id.suggestion_3)

        val apiResponse =
            biasReader.textFieldsCache[lastEventAccessibilityNodeInfo?.nodeInfoText.toString()]
        suggestionTextView1?.text = apiResponse?.suggestions?.firstOrNull() ?: ""
        suggestionTextView2?.text = apiResponse?.suggestions?.getOrNull(1) ?: ""
        suggestionTextView3?.text = apiResponse?.suggestions?.getOrNull(2) ?: ""

        fun getDrawableResource(resId: Int): Drawable? {
            return AppCompatResources.getDrawable(context, resId)
        }

        val activeCard = getDrawableResource(R.drawable.suggestion_card_active)
        val inactiveCard = getDrawableResource(R.drawable.suggestion_card_inactive)

        suggestionTextView1?.background = activeCard
        suggestionTextView2?.background = inactiveCard
        suggestionTextView3?.background = inactiveCard

        suggestionTextView1?.setOnClickListener {
            suggestionTextView1?.background = activeCard
            suggestionTextView2?.background = inactiveCard
            suggestionTextView3?.background = inactiveCard
        }
        suggestionTextView2?.setOnClickListener {
            suggestionTextView2?.background = activeCard
            suggestionTextView1?.background = inactiveCard
            suggestionTextView3?.background = inactiveCard
        }
        suggestionTextView3?.setOnClickListener {
            suggestionTextView3?.background = activeCard
            suggestionTextView1?.background = inactiveCard
            suggestionTextView2?.background = inactiveCard
        }

        findViewById<ImageButton>(R.id.close_suggestions_bottomsheet)?.setOnClickListener {
            suggestionsLayout?.visibility = View.GONE
            if (lastEventAccessibilityNodeInfo?.isFocused == true && floatingWidgetLayout?.isVisible == false){
                displayFloatingWidget()
            }
        }

        findViewById<Button>(R.id.accept_suggestion_positive_button)?.setOnClickListener {
            didUserJustAcceptSuggestion = true
            val selectedTextView = when {
                suggestionTextView3?.background == activeCard -> suggestionTextView3
                suggestionTextView2?.background == activeCard -> suggestionTextView2
                else -> suggestionTextView1
            }

            lastEventAccessibilityNodeInfo?.updateInput(
                selectedTextView?.text.toString()
            )

            suggestionsLayout?.visibility = View.GONE
        }

    }

    fun hideFloatingWidget() {
        floatingWidgetAnimator?.cancel()
        floatingWidgetLayout?.visibility = View.GONE
    }

    private fun createParams(
        width: Int = WindowManager.LayoutParams.WRAP_CONTENT,
        height: Int = WindowManager.LayoutParams.WRAP_CONTENT
    ) = WindowManager.LayoutParams(
        width,
        height,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    )

    fun listenForPerAPIResponse() {
        coroutineScope.launch(Dispatchers.Main) {
            biasReader.response.collect {
                if (it is DataResponse.Loading) {

                    clearExtraUIState()

                    floatingWidgetAnimator?.start()

                } else {
                    floatingWidgetAnimator?.end()

                    if (it is DataResponse.Success && !it.data?.suggestions.isNullOrEmpty()) {
                        toggleAPIErrorIndicator(false)
                        toggleNoInternetIndicator(false)
                        toggleUnreadIndicator(true)
                    } else {
                        if (it is DataResponse.NetworkError) {
                            toggleNoInternetIndicator(true)
                            delay(5000L)
                            toggleNoInternetIndicator(false)
                        } else if (it is DataResponse.APIError) {
                            toggleAPIErrorIndicator(true)
                        }

                    }

                }
            }
        }
    }

    private fun clearExtraUIState() {
        toggleUnreadIndicator(false)
        toggleAPIErrorIndicator(false)
        toggleNoInternetIndicator(false)

        importantMessageTextView?.visibility = View.GONE
    }

    fun onDestroy() {
        floatingWidgetLayout?.let {
            windowManager.removeView(it)
        }
        suggestionsLayout?.let {
            windowManager.removeView(it)
        }
    }
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

fun AccessibilityNodeInfo.isInputField() =
    isEditable && isVisibleToUser && isFocused && nodeInfoText.isNotBlank()

private fun View.prepareFloatingWidgetAnimator(): ObjectAnimator {
    val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.1f, 1f)
    val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.1f, 1f)
    val animator = ObjectAnimator.ofPropertyValuesHolder(this, scaleX, scaleY)
    animator.duration = 1000
    animator.repeatCount = ObjectAnimator.INFINITE
    return animator
}

val AccessibilityNodeInfo.nodeInfoText: String
    get() {
        val contentDescription = contentDescription?.toString() ?: ""
        return text?.toString()?.removeSuffix(contentDescription) ?: ""
    }

private const val fabInitialX = 0
private val fabInitialY = { context: Context -> ScreenUtils.screenSize(context).height / 2 }
private const val fabGravity: Int = Gravity.TOP or Gravity.START