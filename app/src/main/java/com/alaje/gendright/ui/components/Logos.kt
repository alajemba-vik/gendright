package com.alaje.gendright.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alaje.gendright.R

@Composable
fun GendrightLogo() {
    Column {
        Spacer(modifier = Modifier.height(28.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_small)),
        ) {
            Image(
                painter = painterResource(id = R.drawable.gendright_logo),
                contentDescription = null,
                modifier = Modifier.size(32.dp, 40.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.inversePrimary)
            )

            Text(
                text = stringResource(id = R.string.app_name).lowercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }

}