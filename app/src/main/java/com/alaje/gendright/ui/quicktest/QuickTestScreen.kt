package com.alaje.gendright.ui.quicktest

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.alaje.gendright.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickTestScreen(
    onBack: () -> Unit
) {
    val biasTextExample = stringResource(id = R.string.bias_text_example)
    var textFieldValueState by remember {
        mutableStateOf(
            TextFieldValue(
                text = biasTextExample,
                selection = TextRange(biasTextExample.length)
            )
        )
    }
    val inputFieldFocusRequester = remember { FocusRequester() }

    Scaffold(
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
        }
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
                    minLines = 10
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
        }
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