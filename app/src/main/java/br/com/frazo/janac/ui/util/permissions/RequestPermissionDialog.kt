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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import br.com.frazo.janac.ui.util.goToAppSettings
import br.com.frazo.janac.ui.util.permissions.providers.android.AndroidPermissionProvider


//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun Context.RequestPermissionDialog(
//    modifier: Modifier = Modifier,
//    permissionProvider: AndroidPermissionProvider,
//    params: RequestPermissionDialogParams = RequestPermissionDialogParams(
//        rationaleMessage = "${
//            permissionProvider.name
//        } Permission must be granted to use this feature.",
//        messageStyle = LocalTextStyle.current,
//        rationaleTitle = permissionProvider.name + " Permission.",
//        titleStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold),
//        grantPermissionButtonText = "Grant",
//        denyPermissionButtonText = "Deny",
//    ),
//    onPermissionDeniedBySystem: () -> Unit = {},
//    onPermissionManuallyRequested: () -> Unit = {},
//    onPermissionDeniedByUser: () -> Unit,
//    onPermissionGranted: () -> Unit
//) {
//
//    if (ContextCompat.checkSelfPermission(
//            this,
//            permissionProvider.name
//        ) == PackageManager.PERMISSION_GRANTED
//    ) {
//        onPermissionGranted()
//        return
//    }
//
//    var requestsMade by rememberSaveable {
//        mutableStateOf(0)
//    }
//
//    var waitingResponse by rememberSaveable {
//        mutableStateOf(false)
//    }
//
//    val permissionLauncher =
//        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { permissionGranted ->
//            requestsMade++
//            waitingResponse = false
//            if (permissionGranted) {
//                onPermissionGranted()
//            } else
//                onPermissionDeniedBySystem()
//        }
//
//    if (requestsMade == 0 && !waitingResponse) {
//        SideEffect {
//            waitingResponse = true
//            permissionLauncher.launch(permissionProvider.provide())
//        }
//    }
//
//    if (requestsMade > 0) {
//        AlertDialog(
//            properties = DialogProperties(
//                dismissOnBackPress = false,
//                dismissOnClickOutside = false
//            ),
//            onDismissRequest = {}) {
//            Surface {
//                Column(
//                    modifier = modifier,
//                    verticalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    Text(
//                        text = params.rationaleTitle,
//                        style = params.titleStyle
//                    )
//                    Divider()
//                    Text(
//                        text = params.rationaleMessage,
//                        style = params.messageStyle
//                    )
//                    Divider()
//                    Row(
//                        modifier = Modifier
//                            .wrapContentHeight()
//                            .fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceEvenly
//                    ) {
//                        FilledTonalButton(onClick = {
//                            this@RequestPermissionDialog.goToAppSettings()
//                            requestsMade = 0
//                            onPermissionManuallyRequested()
//                        }) {
//                            Text(text = params.grantPermissionButtonText)
//                        }
//                        OutlinedButton(onClick = {
//                            onPermissionDeniedByUser()
//                        }) {
//                            Text(text = params.denyPermissionButtonText)
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//data class RequestPermissionDialogParams(
//    val rationaleMessage: String,
//    val messageStyle: TextStyle,
//    val rationaleTitle: String,
//    val titleStyle: TextStyle,
//    val grantPermissionButtonText: String,
//    val denyPermissionButtonText: String
//)