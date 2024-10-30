package com.alaje.gendright.ui.quicktest

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alaje.gendright.R
import com.alaje.gendright.ui.components.ActionButton
import com.alaje.gendright.ui.theme.Pink300
import com.alaje.gendright.ui.theme.Yellow700
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickTestScreen(
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()

    var text by remember { mutableStateOf("I do not like women") }

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

        },
        scaffoldState = scaffoldState
    ) { paddingValues ->

        Box {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
                    .padding(paddingValues)
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
                    value = text,
                    onValueChange = {
                        text = it
                    },
                    modifier = Modifier
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

                Spacer(
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                Text(
                    text = stringResource(id = R.string.quick_test_footer),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            GendRightFAB(
                modifier = Modifier.align(Alignment.CenterStart),
                onClick = {
                    coroutineScope.launch {
                        scaffoldState.bottomSheetState.show()
                    }
                }
            )
        }

    }
}

@Composable
private fun GendRightFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasUnseenSuggestion: Boolean
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Box {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Pink300,
                                Yellow700
                            )
                        ),
                    )
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SuggestionsBottomSheet(
    onAccept: (String) -> Unit,
    suggestions: Map<Int, String>,
    onClose: () -> Unit
) {
    var selected by remember { mutableIntStateOf(0) }
    var expanded by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(
            dimensionResource(id = R.dimen.spacing_default),
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.Absolute.SpaceBetween
        ) {
            Text(
                text = stringResource(id = R.string.suggestions_bottomsheet_title),
                modifier = Modifier
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