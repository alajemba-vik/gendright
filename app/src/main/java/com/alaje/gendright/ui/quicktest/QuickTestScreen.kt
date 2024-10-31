package com.alaje.gendright.ui.quicktest

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.gestures.rememberDraggable2DState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.alaje.gendright.R
import com.alaje.gendright.data.models.DataResponse
import com.alaje.gendright.ui.components.ActionButton
import com.alaje.gendright.ui.theme.Pink300
import com.alaje.gendright.ui.theme.Yellow700
import com.alaje.gendright.utils.BiasReader
import com.alaje.gendright.utils.rememberSheetState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickTestScreen(
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberSheetState(
            true,
            initialValue = SheetValue.Hidden,
            skipHiddenState = false,
            confirmValueChange = {
                it == SheetValue.Expanded || it == SheetValue.Hidden
            }
        )
    )

    val biasReader = remember { BiasReader() }
    var job: Job = remember { Job() }

    val biasTextExample = stringResource(id = R.string.bias_text_example)
    var textFieldValueState by remember {
        mutableStateOf(
            TextFieldValue(
                text = biasTextExample,
                selection = TextRange(biasTextExample.length)
            )
        )
    }
    var hasUnseenSuggestions by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val inputFieldFocusRequester = remember { FocusRequester() }

    fun openBottomSheet() {
        coroutineScope.launch {
            scaffoldState.bottomSheetState.expand()
        }
    }

    fun closeBottomSheet() {
        coroutineScope.launch {
            scaffoldState.bottomSheetState.hide()
        }
    }

    BottomSheetScaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.navigate_back_icon_desc),
                        )
                    }
                },
            )
        },
        sheetContent = {
            SuggestionsBottomSheet(
                onAccept = {
                    textFieldValueState = TextFieldValue(
                        text = it,
                        selection = TextRange(it.length)
                    )
                    closeBottomSheet()
                },
                suggestions = (biasReader.response.value as? DataResponse.Success)?.data?.suggestions?.mapIndexed { index, s ->
                    Pair(index, s)
                }?.toMap() ?: emptyMap(),
                onClose = {
                    closeBottomSheet()
                }
            )
        },
        scaffoldState = scaffoldState,
        sheetDragHandle = null
    ) { paddingValues ->

        Box(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_default)),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = R.string.quick_test_title),
                    style = MaterialTheme.typography.displaySmall.copy(
                        brush = quickTestContentGradient()
                    )
                )

                Spacer(
                    modifier = Modifier.padding(
                        bottom = dimensionResource(id = R.dimen.padding_default)
                    )
                )

                OutlinedTextField(
                    value = textFieldValueState,
                    onValueChange = { textFieldValue ->
                        textFieldValueState = textFieldValue

                        job.cancel()
                        job = coroutineScope.launch {
                            biasReader.readText(textFieldValue.text)
                        }
                    },
                    modifier = Modifier
                        .focusRequester(inputFieldFocusRequester)
                        .height(200.dp)
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            brush = quickTestContentGradient(),
                            shape = OutlinedTextFieldDefaults.shape
                        ),
                    minLines = 10,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.quick_test_input_field_hint),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                )

                LaunchedEffect(Unit) {
                    inputFieldFocusRequester.requestFocus()
                }

                Spacer(
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                Text(
                    text = stringResource(id = R.string.quick_test_footer),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            //TODO (Display in-app FAB)
            if (false)
                GendRightFAB(
                    onClick = {
                        hasUnseenSuggestions = false

                        openBottomSheet()
                    },
                    hasUnseenSuggestion = hasUnseenSuggestions,
                    isLoading = isLoading,
                    enabled = biasReader.response.collectAsState().value.data != null
                )
        }
    }

    LaunchedEffect(biasReader) {
        biasReader.response.collect {
            hasUnseenSuggestions =
                it is DataResponse.Success && it.data?.suggestions?.isNotEmpty() == true
            isLoading = it is DataResponse.Loading
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun GendRightFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasUnseenSuggestion: Boolean,
    isLoading: Boolean,
    enabled: Boolean
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    val infiniteTransition = rememberInfiniteTransition(label = "animateFAB")

    var offset by remember { mutableStateOf(IntOffset(0, 0)) }
    val animatedOffset by animateIntOffsetAsState(targetValue = offset, label = "animateFABOffset")

    val sizeInPx = with(density) { fabSize.roundToPx() }

    val availableHeightInPx = remember(density) {
        with(density) {
            configuration.screenHeightDp.dp.roundToPx() - sizeInPx
        }
    }

    val availableWidthInPx = with(density) { configuration.screenWidthDp.dp.roundToPx() }

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gendrightFABAnimation"
    )

    val isKeyboardOpen = WindowInsets.isImeVisible
    val keyboardHeight = WindowInsets.ime.getBottom(density = density)

    // The keyboard's height gets updated as it opens up so the keyboard height when
    // isKeyboardOpen is true is not the final height of the keyboard,
    // so we keep track of the keyboard height to know when it's opening or closing
    var trackKeyboardHeightState by remember { mutableStateOf(0) }
    val trackKeyboardHeight by rememberUpdatedState(trackKeyboardHeightState)

    // Will be cleared once offset value stored is applied and updated to match the current
    // offset once dragged
    var tempBeforeModifiedOffsetForKeyboardState by remember { mutableStateOf<IntOffset?>(null) }
    val tempBeforeModifiedOffsetForKeyboard by rememberUpdatedState(
        tempBeforeModifiedOffsetForKeyboardState
    )

    Box(
        modifier
            .offset { animatedOffset }
            .draggable2D(
                state = rememberDraggable2DState {
                    var newOffsetX = (offset.x + it.x).roundToInt()
                    var newOffsetY = (offset.y + it.y).roundToInt()

                    newOffsetX = newOffsetX.coerceIn(0, availableWidthInPx - sizeInPx)
                    newOffsetY = newOffsetY.coerceIn(
                        0,
                        availableHeightInPx - with(density) {
                            fabBottomPadding.roundToPx()
                        }
                    )

                    offset = IntOffset(
                        newOffsetX,
                        newOffsetY
                    )

                    tempBeforeModifiedOffsetForKeyboardState = offset
                },
                onDragStopped = {
                    val distanceToLeft = offset.x
                    val distanceToRight = availableWidthInPx - sizeInPx - offset.x

                    offset = if (distanceToLeft < distanceToRight) {
                        IntOffset(0, offset.y)
                    } else {
                        IntOffset(availableWidthInPx - sizeInPx, offset.y)
                    }

                    tempBeforeModifiedOffsetForKeyboardState = offset
                }
            )
            .scale(if (isLoading) scale else 1f)
            .shadow(
                elevation = 1.dp,
                shape = CircleShape,
                ambientColor = MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.1f)
            )
            .background(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainer
            )
            .clickable(onClick = onClick, enabled = enabled)
            .size(fabSize)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.gendright_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .alpha(if (enabled) 1f else 0.5f)
                )
                if (hasUnseenSuggestion)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(8.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Pink300,
                                        Yellow700
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
            }
        }
    }


    LaunchedEffect(isKeyboardOpen, keyboardHeight) {

        val imeBoundsY = availableHeightInPx - keyboardHeight
        val elementPositionY = offset.y + sizeInPx

        val isCoveredByKeyboard = isKeyboardOpen && elementPositionY > imeBoundsY

        if (!isKeyboardOpen) {
            tempBeforeModifiedOffsetForKeyboard?.let {
                offset = it
                tempBeforeModifiedOffsetForKeyboardState = null
            }
        } else if (isCoveredByKeyboard) {
            val byHowMuch = elementPositionY - imeBoundsY

            if (keyboardHeight > trackKeyboardHeight) {
                // the keyboard is opening
                if (tempBeforeModifiedOffsetForKeyboardState == null) {
                    tempBeforeModifiedOffsetForKeyboardState = offset
                }

                offset = IntOffset(offset.x, offset.y - byHowMuch)
            }
        }

        trackKeyboardHeightState = keyboardHeight

    }

    LaunchedEffect(Unit) {
        offset = offset.copy(y = availableHeightInPx / 2)
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SuggestionsBottomSheet(
    onAccept: (String) -> Unit,
    onClose: () -> Unit,
    suggestions: Map<Int, String>
) {
    var selected by remember { mutableIntStateOf(0) }
    var expanded by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(
            dimensionResource(id = R.dimen.spacing_default),
        ),
        modifier = Modifier.padding(
            bottom = dimensionResource(id = R.dimen.padding_large),
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.Absolute.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(id = R.string.suggestions_bottomsheet_title),
                modifier = Modifier
                    .padding(top = dimensionResource(id = R.dimen.padding_large))
                    .padding(
                        horizontal = dimensionResource(id = R.dimen.padding_default),
                    ),
                style = MaterialTheme.typography.titleSmall
            )
            IconButton(onClick = onClose) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = stringResource(id = R.string.suggestions_bottomsheet_close_icon_desc)
                )
            }
        }

        for (suggestion in suggestions) {
            Text(
                text = suggestion.value,
                modifier = Modifier
                    .padding(
                        horizontal = dimensionResource(id = R.dimen.padding_default),
                    )
                    .apply {
                        if (selected == suggestion.key) {
                            border(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                ),
                                width = 1.dp,
                                shape = MaterialTheme.shapes.small
                            )
                        } else {
                            border(
                                color = MaterialTheme.colorScheme.outline,
                                width = 1.dp,
                                shape = MaterialTheme.shapes.small
                            )
                        }

                    }
                    .combinedClickable(
                        enabled = true,
                        onDoubleClick = {
                            expanded = !expanded
                        },
                        onClick = {
                            // collapse the previous element
                            if (selected != suggestion.key) {
                                expanded = false
                            }
                            selected = suggestion.key

                        }
                    )
                    .padding(
                        horizontal = dimensionResource(id = R.dimen.padding_min),
                        vertical = dimensionResource(id = R.dimen.padding_small),
                    ),
                maxLines = if (expanded) Int.MAX_VALUE else 2
            )
        }

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_min)))

        ActionButton(
            onClick = {
                onAccept(suggestions[selected] ?: "")
            },
            text = stringResource(id = R.string.suggestion_bottom_sheet_positive_button),
            modifier = Modifier.padding(
                horizontal = dimensionResource(id = R.dimen.padding_default),
            )
        )

    }
}

private val quickTestContentGradient = @Composable {
    Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary
        )
    )
}

private val fabSize = 60.dp
private val fabBottomPadding = 100.dp