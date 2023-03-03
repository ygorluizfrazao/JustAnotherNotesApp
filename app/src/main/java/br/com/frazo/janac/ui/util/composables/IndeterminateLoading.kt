package br.com.frazo.janac.ui.util.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import br.com.frazo.janac.ui.theme.spacing

@Composable
fun IndeterminateLoading(
    modifier: Modifier = Modifier,
    loadingText: String
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        if (loadingText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(height = MaterialTheme.spacing.medium))
            Text(
                style = MaterialTheme.typography.bodyMedium,
                text = loadingText
            )
        }
    }
}