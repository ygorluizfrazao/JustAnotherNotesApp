package br.com.frazo.janac.ui.util.permissions.materialv3.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun RequestPermissionDialog(
    modifier: Modifier = Modifier,
    shape: Shape = ShapeDefaults.ExtraLarge,
    params: RequestPermissionDialogParams,
    onGrantClicked: () -> Unit,
    onDenyClicked: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = false
        )
    ) {
        Surface(
            shape = shape
        ) {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = params.title,
                    style = params.titleStyle
                )
                Divider()
                Text(
                    text = params.message,
                    style = params.messageStyle
                )
                Divider()
                Row(
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilledTonalButton(onClick = onGrantClicked) {
                        Text(text = params.grantButtonText)
                    }
                    OutlinedButton(onClick = onDenyClicked) {
                        Text(text = params.denyButtonText)
                    }
                }
            }
        }
    }
}

data class RequestPermissionDialogParams(
    val message: String,
    val messageStyle: TextStyle,
    val title: String,
    val titleStyle: TextStyle,
    val grantButtonText: String,
    val denyButtonText: String
)