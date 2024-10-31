package com.alaje.gendright.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alaje.gendright.R
import com.alaje.gendright.di.AppContainer
import com.alaje.gendright.ui.components.ActionButton
import com.alaje.gendright.ui.components.GendrightLogo
import com.alaje.gendright.ui.theme.Grey50
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pageSize }
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_default)),
            ) {
                GendrightLogo()
            }
        },
        bottomBar = {
            ActionButton(
                onClick = {
                    AppContainer.instance?.localDataSource?.setUserHasOnboarded()
                    onGetStarted()
                },
                text = stringResource(id = R.string.get_started),
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.padding_default))
                    .padding(bottom = dimensionResource(id = R.dimen.padding_default))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = dimensionResource(id = R.dimen.padding_default))
        ) {

            PageIndicator(
                active = pagerState.currentPage,
                modifier = Modifier
                    .padding(vertical = dimensionResource(id = R.dimen.padding_default))
            )

            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
            ) {
                HorizontalPager(
                    state = pagerState
                ) { page ->
                    when (page) {
                        0 -> OnboardingContent(
                            title = stringResource(id = R.string.onboarding_title_1),
                            description = stringResource(id = R.string.onboarding_description_1),
                        )

                        1 -> OnboardingContent(
                            title = stringResource(id = R.string.onboarding_title_2),
                            description = stringResource(id = R.string.onboarding_description_2),
                        )

                        2 -> OnboardingContent(
                            title = stringResource(id = R.string.onboarding_title_3),
                            description = stringResource(id = R.string.onboarding_description_3),
                        )
                    }

                }

                val isPressedState = pagerState.interactionSource.collectIsPressedAsState()
                val coroutineScope = rememberCoroutineScope()

                LaunchedEffect(isPressedState) {
                    snapshotFlow { isPressedState.value }
                        .collectLatest { isPressed ->
                            if (!isPressed) {
                                while (true) {
                                    delay(5_000)
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(
                                            pagerState.currentPage.inc() % pagerState.pageCount
                                        )
                                    }
                                }
                            }
                        }
                }
            }

        }
    }
}

@Composable
private fun PageIndicator(
    active: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_min)),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        for (i in 0 until pageSize) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        if (i == active) MaterialTheme.colorScheme.primary else Grey50, CircleShape
                    ),
            )
        }
    }
}

@Composable
private fun OnboardingContent(
    title: String, description: String
) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge
            )
        }

        Text(
            text = description,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(bottom = 56.dp)
        )
    }
}

private const val pageSize = 3