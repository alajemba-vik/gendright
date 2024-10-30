package com.alaje.gendright.ui.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alaje.gendright.R
import com.alaje.gendright.ui.components.ActionButton
import com.alaje.gendright.ui.components.GendrightLogo
import com.alaje.gendright.ui.theme.Grey50

@Composable
fun OnboardingScreen() {
    val pagerState = rememberPagerState(pageCount = { pageSize })

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_default)),
            ) {
                GendrightLogo()
            }
        },
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
                modifier = Modifier.weight(1f)
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
            }

            ActionButton(
                onClick = {

                },
                text = stringResource(id = R.string.get_started)
            )
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