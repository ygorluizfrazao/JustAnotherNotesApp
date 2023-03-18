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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import br.com.frazo.janac.ui.util.goToAppSettings
import br.com.frazo.janac.ui.util.permissions.providers.AndroidPermissionProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Context.WithPermission(
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
    contentWhenGranted: @Composable (AndroidPermissionProvider) -> Unit,
    onPermissionDeniedByUser: (AndroidPermissionProvider) -> Unit
) {
    if (ContextCompat.checkSelfPermission(
            this,
            permissionProvider.provide()
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        contentWhenGranted(permissionProvider)
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
fun (@Composable () -> Unit).withPermission(
    permissionProvider: AndroidPermissionProvider,
    canStartAsking: () -> Boolean,
    permissionAskingStrategy: PermissionAskingStrategy = PermissionAskingStrategy.STOP_ASKING_ON_USER_DENIAL,
    rationalePrompt: @Composable (permissionProvider: AndroidPermissionProvider, callMeWhen: RationaleCallback) -> Unit,
    terminalState: @Composable (permissionProvider: AndroidPermissionProvider, isGranted: Boolean) -> Unit
) {

    var flowState =
        PermissionFlowState(PermissionFlowStateEnum.NOT_STARTED, this)

    val context = LocalContext.current
    val permissionGranted = ContextCompat.checkSelfPermission(
        context,
        permissionProvider.provide()
    ) == PackageManager.PERMISSION_GRANTED

    var userManuallyDenied by rememberSaveable {
        mutableStateOf(false)
    }

    var requestsMade by rememberSaveable {
        mutableStateOf(0)
    }

    //User manually block the permission after granting
    if (flowState.state == PermissionFlowStateEnum.TERMINAL_GRANTED && !permissionGranted) {
        flowState = PermissionFlowState(PermissionFlowStateEnum.NOT_STARTED, this)
        requestsMade = 0
    }

    if (permissionGranted) {
        flowState = PermissionFlowState(
            PermissionFlowStateEnum.TERMINAL_GRANTED
        ) { terminalState(permissionProvider, true) }
    } else {

        when (permissionAskingStrategy) {

            PermissionAskingStrategy.STOP_ASKING_ON_USER_DENIAL -> {
                if (userManuallyDenied) {
                    flowState = PermissionFlowState(
                        PermissionFlowStateEnum.TERMINAL_DENIED
                    ) { terminalState(permissionProvider, false) }
                }
            }

            PermissionAskingStrategy.ONLY_ASK_SYSTEM -> {
                if (requestsMade > 0) {
                    flowState = PermissionFlowState(
                        PermissionFlowStateEnum.TERMINAL_DENIED
                    ) { terminalState(permissionProvider, false) }
                }
            }

            else -> Unit
        }

    }

    if (flowState.state == PermissionFlowStateEnum.NOT_STARTED && canStartAsking()) {
        flowState = PermissionFlowState(PermissionFlowStateEnum.STARTED, this)
    }

    flowState.visibleComposable()

    if (flowState.state == PermissionFlowStateEnum.STARTED) {

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

        if (requestsMade > 0 && permissionAskingStrategy != PermissionAskingStrategy.ONLY_ASK_SYSTEM) {
            rationalePrompt(permissionProvider, RationaleCallback(
                requestedUserManualGrant = {
                    requestsMade++
                },
                manuallyDeniedByUser = {
                    if (permissionAskingStrategy == PermissionAskingStrategy.STOP_ASKING_ON_USER_DENIAL)
                        userManuallyDenied = true
                    if (permissionAskingStrategy == PermissionAskingStrategy.KEEP_ASKING) {
                        flowState = PermissionFlowState(PermissionFlowStateEnum.NOT_STARTED, this)
                        requestsMade = 0
                    }
                }
            ))
        }
    }
}

@JvmName("withPermissionExplicitArgument")
@Composable
fun withPermission(
    beforeTerminalState: @Composable () -> Unit,
    permissionProvider: AndroidPermissionProvider,
    canStartAsking: () -> Boolean,
    permissionAskingStrategy: PermissionAskingStrategy = PermissionAskingStrategy.STOP_ASKING_ON_USER_DENIAL,
    rationalePrompt: @Composable (permissionProvider: AndroidPermissionProvider, callMeWhen: RationaleCallback) -> Unit,
    terminalState: @Composable (permissionProvider: AndroidPermissionProvider, isGranted: Boolean) -> Unit
) {
    beforeTerminalState.withPermission(
        permissionProvider = permissionProvider,
        canStartAsking = canStartAsking,
        permissionAskingStrategy = permissionAskingStrategy,
        rationalePrompt = rationalePrompt,
        terminalState = terminalState
    )
}

data class RationaleCallback(
    val requestedUserManualGrant: () -> Unit,
    val manuallyDeniedByUser: () -> Unit,
)

enum class PermissionAskingStrategy {
    KEEP_ASKING, ONLY_ASK_SYSTEM, STOP_ASKING_ON_USER_DENIAL
}

private enum class PermissionFlowStateEnum {
    NOT_STARTED, STARTED, TERMINAL_GRANTED, TERMINAL_DENIED
}

private data class PermissionFlowState(
    val state: PermissionFlowStateEnum,
    val visibleComposable: @Composable () -> Unit
)