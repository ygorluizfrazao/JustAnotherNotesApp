package br.com.frazo.janac.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import br.com.frazo.janac.ui.IconResource
import br.com.frazo.janac.ui.theme.spacing

@Composable
fun NoItemsContent(
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier
        .padding(bottom = MaterialTheme.spacing.small),
    text: String,
    icon: IconResource = IconResource.fromImageVector(Icons.Filled.Cancel),
    additionalContent: (@Composable (ColumnScope)->Unit)?=null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            modifier = iconModifier,
            painter = icon.asPainterResource(),
            contentDescription = icon.contentDescription
        )
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
        additionalContent?.invoke(this)
    }
}