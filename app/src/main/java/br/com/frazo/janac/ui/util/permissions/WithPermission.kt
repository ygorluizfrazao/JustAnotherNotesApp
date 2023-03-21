package br.com.frazo.janac.ui.util.permissions

import android.content.Context
import androidx.activity.result.ActivityResultCaller
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import br.com.frazo.janac.ui.util.permissions.providers.android.AndroidPermissionProvider
import br.com.frazo.janac.ui.util.permissions.requesters.android.AndroidPermissionRequester
import br.com.frazo.janac.ui.util.permissions.requesters.android.AndroidPermissionRequester.Companion.rememberAndroidPermissionRequester
import br.com.frazo.janac.ui.util.permissions.strategy.*
import br.com.frazo.janac.ui.util.permissions.strategy.PermissionFlowStateEnum.*
import java.util.UUID

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun Context.WithPermission(
//    permissionProvider: AndroidPermissionProvider,
//    modifier: Modifier = Modifier,
//    rationaleRequestPermissionDialogParams: RequestPermissionDialogParams = RequestPermissionDialogParams(
//        rationaleMessage = "${
//            permissionProvider.name
//        } Permission must be granted to use this feature.",
//        messageStyle = LocalTextStyle.current,
//        rationaleTitle = permissionProvider.name + " Permission.",
//        titleStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold),
//        grantPermissionButtonText = "Grant",
//        denyPermissionButtonText = "Deny",
//    ),
//    contentWhenGranted: @Composable (AndroidPermissionProvider) -> Unit,
//    onPermissionDeniedByUser: (AndroidPermissionProvider) -> Unit
//) {
//    if (ContextCompat.checkSelfPermission(
//            this,
//            permissionProvider.provide()
//        ) == PackageManager.PERMISSION_GRANTED
//    ) {
//        contentWhenGranted(permissionProvider)
//    } else {
//
//        var requestsMade by rememberSaveable {
//            mutableStateOf(0)
//        }
//
//        var waitingResponse by rememberSaveable {
//            mutableStateOf(false)
//        }
//
//        val permissionLauncher =
//            rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
//                requestsMade++
//                waitingResponse = false
//            }
//
//        if (requestsMade == 0 && !waitingResponse) {
//            SideEffect {
//                waitingResponse = true
//                permissionLauncher.launch(permissionProvider.provide())
//            }
//        }
//
//        if (requestsMade > 0) {
//            AlertDialog(
//                properties = DialogProperties(
//                    dismissOnBackPress = false,
//                    dismissOnClickOutside = false
//                ),
//                onDismissRequest = {}) {
//                Surface {
//                    Column(
//                        modifier = modifier,
//                        verticalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        Text(
//                            text = rationaleRequestPermissionDialogParams.rationaleTitle,
//                            style = rationaleRequestPermissionDialogParams.titleStyle
//                        )
//                        Divider()
//                        Text(
//                            text = rationaleRequestPermissionDialogParams.rationaleMessage,
//                            style = rationaleRequestPermissionDialogParams.messageStyle
//                        )
//                        Divider()
//                        Row(
//                            modifier = Modifier
//                                .wrapContentHeight()
//                                .fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceEvenly
//                        ) {
//                            FilledTonalButton(onClick = {
//                                this@WithPermission.goToAppSettings()
//                                requestsMade = 0
//                            }) {
//                                Text(text = rationaleRequestPermissionDialogParams.grantPermissionButtonText)
//                            }
//                            OutlinedButton(onClick = {
//                                onPermissionDeniedByUser(permissionProvider)
//                            }) {
//                                Text(text = rationaleRequestPermissionDialogParams.denyPermissionButtonText)
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}


//@Composable
//fun (@Composable () -> Unit).withPermission(
//    permissionProvider: AndroidPermissionProvider,
//    canStartAsking: () -> Boolean,
//    permissionAskingStrategy: PermissionAskingStrategy = PermissionAskingStrategy.STOP_ASKING_ON_USER_DENIAL,
//    rationalePrompt: @Composable (permissionProvider: AndroidPermissionProvider, callMeWhen: RationaleCallback) -> Unit,
//    terminalState: @Composable (permissionProvider: AndroidPermissionProvider, isGranted: Boolean) -> Unit
//) {
//
//    var flowState =
//        PermissionFlowState(NOT_STARTED, this)
//
//    val context = LocalContext.current
//    val permissionGranted = ContextCompat.checkSelfPermission(
//        context,
//        permissionProvider.provide()
//    ) == PackageManager.PERMISSION_GRANTED
//
//    var userManuallyDenied by rememberSaveable {
//        mutableStateOf(false)
//    }
//
//    var requestsMade by rememberSaveable {
//        mutableStateOf(0)
//    }
//
//    //User manually block the permission after granting
//    if (flowState.state == TERMINAL_GRANTED && !permissionGranted) {
//        flowState = PermissionFlowState(NOT_STARTED, this)
//        requestsMade = 0
//    }
//
//    if (permissionGranted) {
//        flowState = PermissionFlowState(
//            TERMINAL_GRANTED
//        ) { terminalState(permissionProvider, true) }
//    } else {
//
//        when (permissionAskingStrategy) {
//
//            PermissionAskingStrategy.STOP_ASKING_ON_USER_DENIAL -> {
//                if (userManuallyDenied) {
//                    flowState = PermissionFlowState(
//                        TERMINAL_DENIED
//                    ) { terminalState(permissionProvider, false) }
//                }
//            }
//
//            PermissionAskingStrategy.ONLY_ASK_SYSTEM -> {
//                if (requestsMade > 0) {
//                    flowState = PermissionFlowState(
//                        TERMINAL_DENIED
//                    ) { terminalState(permissionProvider, false) }
//                }
//            }
//
//            else -> Unit
//        }
//
//    }
//
//    if (flowState.state == NOT_STARTED && canStartAsking()) {
//        flowState = PermissionFlowState(STARTED, this)
//    }
//
//    flowState.visibleComposable()
//
//    if (flowState.state == STARTED) {
//
//        var waitingResponse by rememberSaveable {
//            mutableStateOf(false)
//        }
//
//        val permissionLauncher =
//            rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
//                requestsMade++
//                waitingResponse = false
//            }
//
//        if (requestsMade == 0 && !waitingResponse) {
//            SideEffect {
//                waitingResponse = true
//                permissionLauncher.launch(permissionProvider.provide())
//            }
//        }
//
//        if (requestsMade > 0 && permissionAskingStrategy != PermissionAskingStrategy.ONLY_ASK_SYSTEM) {
//            rationalePrompt(permissionProvider, RationaleCallback(
//                requestedUserManualGrant = {
//                    requestsMade++
//                },
//                manuallyDeniedByUser = {
//                    if (permissionAskingStrategy == PermissionAskingStrategy.STOP_ASKING_ON_USER_DENIAL)
//                        userManuallyDenied = true
//                    if (permissionAskingStrategy == PermissionAskingStrategy.KEEP_ASKING) {
//                        flowState = PermissionFlowState(NOT_STARTED, this)
//                        requestsMade = 0
//                    }
//                }
//            ))
//        }
//    }
//}
//
//@JvmName("withPermissionExplicitArgument")
//@Composable
//fun withPermission(
//    beforeTerminalState: @Composable () -> Unit,
//    permissionProvider: AndroidPermissionProvider,
//    canStartAsking: () -> Boolean,
//    permissionAskingStrategy: PermissionAskingStrategy = PermissionAskingStrategy.STOP_ASKING_ON_USER_DENIAL,
//    rationalePrompt: @Composable (permissionProvider: AndroidPermissionProvider, callMeWhen: RationaleCallback) -> Unit,
//    terminalState: @Composable (permissionProvider: AndroidPermissionProvider, isGranted: Boolean) -> Unit
//) {
//    beforeTerminalState.withPermission(
//        permissionProvider = permissionProvider,
//        canStartAsking = canStartAsking,
//        permissionAskingStrategy = permissionAskingStrategy,
//        rationalePrompt = rationalePrompt,
//        terminalState = terminalState
//    )
//}

@Composable
fun (@Composable () -> Unit).withPermission(
    askingStrategy: AskingStrategy = AskingStrategy.STOP_ASKING_ON_USER_DENIAL,
    androidPermissionRequester: AndroidPermissionRequester,
    canStart: () -> Boolean = { true },
    rationalePrompt: @Composable (state: PermissionFlowStateEnum, permissionsStatus: Map<String, Boolean>, callMeWhen: RationaleCallback) -> Unit,
    terminalStateContent: @Composable (state: PermissionFlowStateEnum, permissionsStatus: Map<String, Boolean>) -> Unit
) {
    val androidPermissionAskingStrategy = remember {
        androidPermissionAskingStrategyFactory(
            type = askingStrategy,
            androidPermissionRequester = androidPermissionRequester,
            canStart = canStart,
            initialStateContent = this,
            rationalePrompt = rationalePrompt,
            terminalStateContent = terminalStateContent
        )
    }

    val key = remember {
        UUID.randomUUID().toString()
    }
    val flowState by androidPermissionAskingStrategy.flowState().collectAsState()

    flowState.data()

    LaunchedEffect(key1 = key) {
        androidPermissionAskingStrategy.resolveState()
    }
}

@Composable
fun ActivityResultCaller.withPermission(
    askingStrategy: AskingStrategy = AskingStrategy.STOP_ASKING_ON_USER_DENIAL,
    permissionProvider: AndroidPermissionProvider,
    canStart: () -> Boolean = { true },
    initialStateContent: @Composable () -> Unit,
    rationalePrompt: @Composable (state: PermissionFlowStateEnum, permissionsStatus: Map<String, Boolean>, callMeWhen: RationaleCallback) -> Unit,
    terminalStateContent: @Composable (state: PermissionFlowStateEnum, permissionsStatus: Map<String, Boolean>) -> Unit
) {
    val permissionRequester =
        rememberAndroidPermissionRequester(
            permissionProvider = permissionProvider
        )

    val androidPermissionAskingStrategy = remember {
        androidPermissionAskingStrategyFactory(
            type = askingStrategy,
            androidPermissionRequester = permissionRequester,
            canStart = canStart,
            initialStateContent = initialStateContent,
            rationalePrompt = rationalePrompt,
            terminalStateContent = terminalStateContent
        )
    }

    val flowState = androidPermissionAskingStrategy.flowState().collectAsState()

    flowState.value.data()
    androidPermissionAskingStrategy.resolveState()

    val lifecycleOwner = LocalLifecycleOwner.current
    
    DisposableEffect(key1 = lifecycleOwner, effect = {
        val observer = LifecycleEventObserver {_,event ->
            if(event==Lifecycle.Event.ON_RESUME){
                androidPermissionAskingStrategy.resolveState()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    })
}