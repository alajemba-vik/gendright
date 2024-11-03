package com.alaje.gendright.ui.settings

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alaje.gendright.R
import com.alaje.gendright.di.AppContainer
import com.alaje.gendright.ui.components.ActionButton
import com.alaje.gendright.ui.components.GendrightLogo
import com.alaje.gendright.utils.PermissionUtils
import com.alaje.gendright.utils.PermissionUtils.canDrawOverApps
import com.alaje.gendright.utils.PermissionUtils.isAccessibilityServiceEnabled
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onQuickTest: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var permissionCheckJob: Job? = remember { null }
    var isFloatingWidgetEnabled by remember {
        mutableStateOf(canDrawOverApps(context))
    }
    var isAccessibilityFeatureEnabled by remember {
        mutableStateOf(isAccessibilityServiceEnabled(context))
    }

    var showWalkthroughOverlay by remember {
        val hasSeenWalkthrough =
            AppContainer.instance?.localDataSource?.checkUserHasSeenWalkthroughUIOnSettings() == true
        if (!hasSeenWalkthrough) {
            AppContainer.instance?.localDataSource?.setUserHasSeenWalkthroughUIOnSettings()
        }
        mutableStateOf(true)
    }

    val autoSelectMostRelevant by viewModel.autoSelectMostRelevantState.collectAsState()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_default)),
            ) {
                GendrightLogo()
            }
        },
        bottomBar = {
            ActionButton(
                enabled = canDrawOverApps(context) && isAccessibilityServiceEnabled(context),
                onClick = onQuickTest,
                text = stringResource(id = R.string.quick_test),
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.padding_default))
                    .padding(bottom = dimensionResource(id = R.dimen.padding_default))
            )
        }
    ) { paddingValues ->
        Box {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_default))
            ) {
                Text(
                    text = stringResource(id = R.string.settings),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(vertical = 32.dp)
                )

                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(30.dp),
                ) {
                    SettingsItem(
                        title = stringResource(R.string.settings_allow_to_display_over_apps_title),
                        isSelected = isFloatingWidgetEnabled,
                        onCheckChange = { shouldEnable ->
                            // Ensure any existing coroutine is cancelled before starting a new one
                            permissionCheckJob?.cancel()

                            permissionCheckJob = PermissionUtils.createDrawOverAppsPermissionJob(
                                {
                                    canDrawOverApps(context)
                                },
                                context,
                                shouldEnable
                            )

                            PermissionUtils.openPermissionToDrawOverOtherAppsSettings(context)
                        },
                        description = stringResource(R.string.settings_allow_to_display_over_apps_desc),
                    )

                    SettingsItem(
                        title = stringResource(R.string.settings_accessibility_feature_title),
                        description = stringResource(R.string.settings_accessibility_feature_desc),
                        isSelected = isAccessibilityFeatureEnabled,
                        onCheckChange = { shouldEnable ->
                            // Ensure any existing coroutine is cancelled before starting a new one
                            permissionCheckJob?.cancel()

                            permissionCheckJob = PermissionUtils.createAccessibilityPermissionJob(
                                context,
                                hasAccessibilityPermission = { isAccessibilityServiceEnabled(context) },
                                shouldEnable,
                            )

                            PermissionUtils.openAccessibilitySettings(context)

                        },
                    )

                    SettingsItem(
                        title = stringResource(R.string.settings_auto_select_most_relevant_title),
                        isSelected = autoSelectMostRelevant,
                        onCheckChange = { shouldEnable ->
                            viewModel.updateAutoSelectSettingsState(
                                autoSelectMostRelevant = shouldEnable
                            )
                        },
                        showDivider = false
                    )
                }
            }

            if (showWalkthroughOverlay && (!canDrawOverApps(context) || isAccessibilityServiceEnabled(
                    context
                ))
            ) {
                WalkthroughOverlay()
            }
        }

    }

    LifecycleResumeEffect(key1 = lifecycleOwner) {
        permissionCheckJob?.cancel()
        isFloatingWidgetEnabled = canDrawOverApps(context)
        isAccessibilityFeatureEnabled = isAccessibilityServiceEnabled(context)

        onPauseOrDispose {}
    }

    if (showWalkthroughOverlay) {
        LaunchedEffect(Unit) {
            delay(3000)
            showWalkthroughOverlay = false
        }
    }
}

@Composable
private fun WalkthroughOverlay() {
    val highlightIndicatorColor = MaterialTheme.colorScheme.surfaceBright
    val highlightSize = dimensionResource(id = R.dimen.floating_widget_highlight_indicator_size)

    val infiniteTransition = rememberInfiniteTransition(label = "animateFAB")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "highlightInfiniteAnimation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.overlay_color))
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .scale(scale)
                .background(highlightIndicatorColor, CircleShape)
                .size(highlightSize)
                .graphicsLayer {
                    translationY = -45.dp.toPx()
                }
        )
    }
}
// create shape where the botton looks like a hole


@Composable
fun SettingsItem(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    isSelected: Boolean,
    onCheckChange: (Boolean) -> Unit,
    showDivider: Boolean = true
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(
            dimensionResource(id = R.dimen.spacing_default)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(
                dimensionResource(id = R.dimen.spacing_medium)
            )
        ) {
            Text(
                text = title,
                modifier = modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall
                    .copy(
                        fontWeight = FontWeight.SemiBold
                    )
            )
            Switch(
                checked = isSelected,
                onCheckedChange = onCheckChange
            )

        }
        if (!description.isNullOrBlank())
            SettingsItemDescription(description)

        if (showDivider) HorizontalDivider(
            modifier = Modifier.padding(
                top = dimensionResource(id = R.dimen.spacing_default)
            )
        )
    }
}

@Composable
private fun SettingsItemDescription(
    description: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = description,
        style = MaterialTheme.typography.bodySmall
            .copy(
                fontSize = MaterialTheme.typography.labelSmall.fontSize
            ),
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.tertiaryContainer
                    )
                ),
                shape = MaterialTheme.shapes.small
            )
            .padding(
                vertical = dimensionResource(id = R.dimen.padding_small),
                horizontal = dimensionResource(id = R.dimen.horizontal_card_padding_default)
            )
    )
}
