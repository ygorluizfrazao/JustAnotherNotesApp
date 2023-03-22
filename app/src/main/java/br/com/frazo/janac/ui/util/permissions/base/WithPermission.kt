package br.com.frazo.janac.ui.util.permissions.base

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.result.ActivityResultCaller
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import br.com.frazo.janac.ui.util.permissions.base.providers.android.AndroidPermissionProvider
import br.com.frazo.janac.ui.util.permissions.base.requesters.android.AndroidPermissionRequester.Companion.rememberAndroidPermissionRequester
import br.com.frazo.janac.ui.util.permissions.base.strategy.*

@Composable
fun WithPermission(
    userDrivenAskingStrategy: UserDrivenAskingStrategy<Map<String, Boolean>>,
    initialStateContent: @Composable () -> Unit,
    rationalePrompt: @Composable (state: PermissionFlowStateEnum, permissionsStatus: Map<String, Boolean>, callMeWhen: AppPermissionRequestCallbackHolder) -> Unit,
    terminalStateContent: @Composable (state: PermissionFlowStateEnum, permissionsStatus: Map<String, Boolean>) -> Unit
) {

    val flowState = userDrivenAskingStrategy.flowState().collectAsState()

    val rationaleComposable: @Composable () -> Unit = remember {
        {
            initialStateContent()
            rationalePrompt(
                PermissionFlowStateEnum.DENIED_BY_SYSTEM,
                flowState.value.data,
                AppPermissionRequestCallbackHolder(
                    requestedUserManualGrant = {
                        userDrivenAskingStrategy.onRequestedUserManualGrant()
                    },
                    manuallyDeniedByUser = {
                        userDrivenAskingStrategy.onUserDenied()
                    })
            )
        }
    }

    userDrivenAskingStrategy.resolveState()

    when (flowState.value.state) {
        PermissionFlowStateEnum.NOT_STARTED, PermissionFlowStateEnum.STARTED, PermissionFlowStateEnum.DENIED_BY_SYSTEM -> initialStateContent()
        PermissionFlowStateEnum.APP_PROMPT -> rationaleComposable()
        PermissionFlowStateEnum.TERMINAL_GRANTED, PermissionFlowStateEnum.TERMINAL_DENIED -> terminalStateContent(
            flowState.value.state,
            flowState.value.data
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(key1 = lifecycleOwner, effect = {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                userDrivenAskingStrategy.resolveState()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    })
}

@Composable
fun ActivityResultCaller.WithPermission(
    askingStrategy: AskingStrategy = AskingStrategy.STOP_ASKING_ON_USER_DENIAL,
    permissionProvider: AndroidPermissionProvider,
    canStart: ()->Boolean = {true},
    initialStateContent: @Composable () -> Unit,
    rationalePrompt: @Composable (state: PermissionFlowStateEnum, permissionsStatus: Map<String, Boolean>, callMeWhen: AppPermissionRequestCallbackHolder) -> Unit,
    terminalStateContent: @Composable (state: PermissionFlowStateEnum, permissionsStatus: Map<String, Boolean>) -> Unit
) {
    val permissionRequester =
        rememberAndroidPermissionRequester(
            permissionProvider = permissionProvider
        )

    val userDrivenAskingStrategy =
        askingStrategy.rememberUserDrivenAskingStrategy(
            androidPermissionRequester = permissionRequester,
            canStart = canStart
        )

   WithPermission(
        userDrivenAskingStrategy = userDrivenAskingStrategy,
        initialStateContent = initialStateContent,
        rationalePrompt = rationalePrompt,
        terminalStateContent = terminalStateContent
    )
}

@Composable
fun Context.WithPermission(
    askingStrategy: AskingStrategy = AskingStrategy.STOP_ASKING_ON_USER_DENIAL,
    permissionProvider: AndroidPermissionProvider,
    canStart: ()->Boolean = {true},
    initialStateContent: @Composable () -> Unit,
    rationalePrompt: @Composable (state: PermissionFlowStateEnum, permissionsStatus: Map<String, Boolean>, callMeWhen: AppPermissionRequestCallbackHolder) -> Unit,
    terminalStateContent: @Composable (state: PermissionFlowStateEnum, permissionsStatus: Map<String, Boolean>) -> Unit
) {

    findActivityResultCaller().WithPermission(
        askingStrategy,
        permissionProvider,
        canStart,
        initialStateContent,
        rationalePrompt,
        terminalStateContent
    )
}


@JvmName("withPermission1")
@Composable
fun WithPermission(
    context: Context = LocalContext.current,
    askingStrategy: AskingStrategy = AskingStrategy.STOP_ASKING_ON_USER_DENIAL,
    permissionProvider: AndroidPermissionProvider,
    canStart: ()->Boolean = {true},
    initialStateContent: @Composable () -> Unit,
    rationalePrompt: @Composable (state: PermissionFlowStateEnum, permissionsStatus: Map<String, Boolean>, callMeWhen: AppPermissionRequestCallbackHolder) -> Unit,
    terminalStateContent: @Composable (state: PermissionFlowStateEnum, permissionsStatus: Map<String, Boolean>) -> Unit
) {

    context.findActivityResultCaller().WithPermission(
        askingStrategy,
        permissionProvider,
        canStart = canStart,
        initialStateContent,
        rationalePrompt,
        terminalStateContent
    )
}

data class AppPermissionRequestCallbackHolder(
    val requestedUserManualGrant: () -> Unit,
    val manuallyDeniedByUser: () -> Unit,
)

fun Context.findActivityResultCaller(): ActivityResultCaller {
    var context = this
    while (context is ContextWrapper) {
        if (context is ActivityResultCaller) return context
        context = context.baseContext
    }
    throw IllegalStateException("no activity")
}