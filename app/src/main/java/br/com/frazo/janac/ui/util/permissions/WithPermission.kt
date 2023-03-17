package br.com.frazo.janac.ui.util.permissions

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import br.com.frazo.janac.ui.util.goToAppSettings
import br.com.frazo.janac.ui.util.permissions.providers.AndroidPermissionProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Context.WithPermission(
    permissionProvider: AndroidPermissionProvider,
    modifier: Modifier = Modifier,
    rationaleRequestPermissionDialogParams: RequestPermissionDialogParams = RequestPermissionDialogParams(
        rationaleMessage = "${
            permissionProvider.name
        } Permission must be granted to use this feature.",
        messageStyle = LocalTextStyle.current,
        rationaleTitle = permissionProvider.name + " Permission.",
        titleStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold),
        grantPermissionButtonText = "Grant",
        denyPermissionButtonText = "Deny",
    ),
    ContentWhenGranted: @Composable (AndroidPermissionProvider) -> Unit,
    onPermissionDeniedByUser: (AndroidPermissionProvider) -> Unit
) {
    if (ContextCompat.checkSelfPermission(
            this,
            permissionProvider.provide()
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        ContentWhenGranted(permissionProvider)
    } else {

        var requestsMade by rememberSaveable {
            mutableStateOf(0)
        }

        var waitingResponse by rememberSaveable {
            mutableStateOf(false)
        }

        val permissionLauncher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
                requestsMade++
                waitingResponse = false
            }

        if (requestsMade == 0 && !waitingResponse) {
            SideEffect {
                waitingResponse = true
                permissionLauncher.launch(permissionProvider.provide())
            }
        }

        if (requestsMade > 0) {
            AlertDialog(
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                ),
                onDismissRequest = {}) {
                Surface {
                    Column(
                        modifier = modifier,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = rationaleRequestPermissionDialogParams.rationaleTitle,
                            style = rationaleRequestPermissionDialogParams.titleStyle
                        )
                        Divider()
                        Text(
                            text = rationaleRequestPermissionDialogParams.rationaleMessage,
                            style = rationaleRequestPermissionDialogParams.messageStyle
                        )
                        Divider()
                        Row(
                            modifier = Modifier
                                .wrapContentHeight()
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            FilledTonalButton(onClick = {
                                this@WithPermission.goToAppSettings()
                                requestsMade = 0
                            }) {
                                Text(text = rationaleRequestPermissionDialogParams.grantPermissionButtonText)
                            }
                            OutlinedButton(onClick = {
                                onPermissionDeniedByUser(permissionProvider)
                            }) {
                                Text(text = rationaleRequestPermissionDialogParams.denyPermissionButtonText)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun Context.WithPermission(
    permissionProvider: AndroidPermissionProvider,
    ContentWhenGranted: @Composable (AndroidPermissionProvider) -> Unit,
    ContentWhenDeniedBySystem: @Composable (AndroidPermissionProvider) -> Unit
) {
    if (ContextCompat.checkSelfPermission(
            this,
            permissionProvider.provide()
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        ContentWhenGranted(permissionProvider)
    } else {

        var requestsMade by rememberSaveable {
            mutableStateOf(0)
        }

        var waitingResponse by rememberSaveable {
            mutableStateOf(false)
        }

        val permissionLauncher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
                requestsMade++
                waitingResponse = false
            }

        if (requestsMade == 0 && !waitingResponse) {
            SideEffect {
                waitingResponse = true
                permissionLauncher.launch(permissionProvider.provide())
            }
        }

        if (requestsMade > 0) {
            ContentWhenDeniedBySystem(permissionProvider)
        }
    }
}