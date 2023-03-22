package br.com.frazo.janac.ui.util.permissions.base

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.result.ActivityResultCaller
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import br.com.frazo.janac.ui.util.permissions.base.providers.android.AndroidPermissionProvider
import br.com.frazo.janac.ui.util.permissions.base.requesters.android.AndroidPermissionRequester.Companion.rememberAndroidPermissionRequester
import br.com.frazo.janac.ui.util.permissions.base.strategy.AskingStrategy
import br.com.frazo.janac.ui.util.permissions.base.strategy.PermissionFlowStateEnum
import br.com.frazo.janac.ui.util.permissions.base.strategy.RationaleCallback
import br.com.frazo.janac.ui.util.permissions.base.strategy.androidPermissionAskingStrategyFactory

@Composable
fun ActivityResultCaller.WithPermission(
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
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                androidPermissionAskingStrategy.resolveState()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    })
}

@Composable
fun Context.WithPermission(
    askingStrategy: AskingStrategy = AskingStrategy.STOP_ASKING_ON_USER_DENIAL,
    permissionProvider: AndroidPermissionProvider,
    canStart: () -> Boolean = { true },
    initialStateContent: @Composable () -> Unit,
    rationalePrompt: @Composable (state: PermissionFlowStateEnum, permissionsStatus: Map<String, Boolean>, callMeWhen: RationaleCallback) -> Unit,
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
    canStart: () -> Boolean = { true },
    initialStateContent: @Composable () -> Unit,
    rationalePrompt: @Composable (state: PermissionFlowStateEnum, permissionsStatus: Map<String, Boolean>, callMeWhen: RationaleCallback) -> Unit,
    terminalStateContent: @Composable (state: PermissionFlowStateEnum, permissionsStatus: Map<String, Boolean>) -> Unit
) {
    context.findActivityResultCaller().WithPermission(
        askingStrategy,
        permissionProvider,
        canStart,
        initialStateContent,
        rationalePrompt,
        terminalStateContent
    )
}

fun Context.findActivityResultCaller(): ActivityResultCaller {
    var context = this
    while (context is ContextWrapper) {
        if (context is ActivityResultCaller) return context
        context = context.baseContext
    }
    throw IllegalStateException("no activity")
}