package br.com.frazo.janac.ui.util.permissions.base.strategy

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import br.com.frazo.janac.ui.util.permissions.base.requesters.PermissionRequester
import br.com.frazo.janac.ui.util.permissions.base.requesters.android.AndroidPermissionRequester

enum class AskingStrategy {
    KEEP_ASKING, ONLY_ASK_SYSTEM, STOP_ASKING_ON_USER_DENIAL
}

fun createUserDrivenAskingStrategy(
    type: AskingStrategy,
    permissionRequester: PermissionRequester<List<String>, String>,
    canStart: () -> Boolean = { true }
): UserDrivenAskingStrategy<Map<String, Boolean>> {
    return when (type) {
        AskingStrategy.STOP_ASKING_ON_USER_DENIAL -> StopOnUserDenialAskingStrategy(
            permissionRequester,
            canStart
        )
        AskingStrategy.KEEP_ASKING -> KeepAskingStrategy(permissionRequester, canStart)
        else ->
            StopOnUserDenialAskingStrategy(
                permissionRequester,
                canStart
            )
    }
}

@Composable
fun rememberUserDrivenAskingStrategy(
    type: AskingStrategy,
    permissionRequester: PermissionRequester<List<String>, String>,
    canStart: () -> Boolean
): UserDrivenAskingStrategy<Map<String, Boolean>> {

    return when (type) {
        AskingStrategy.STOP_ASKING_ON_USER_DENIAL -> {
            StopOnUserDenialAskingStrategy.rememberSavable(permissionRequester, canStart)
        }
        AskingStrategy.KEEP_ASKING -> {
            KeepAskingStrategy.rememberSavable(permissionRequester, canStart)
        }
        else -> {
            StopOnUserDenialAskingStrategy.rememberSavable(permissionRequester, canStart)
        }
    }
}

@Composable
fun rememberUserDrivenAskingStrategy(
    context: Context = LocalContext.current,
    type: AskingStrategy = AskingStrategy.STOP_ASKING_ON_USER_DENIAL,
    permissions: List<String>,
    canStart: () -> Boolean
): UserDrivenAskingStrategy<Map<String, Boolean>> {
    return rememberUserDrivenAskingStrategy(
        type = type,
        canStart = canStart,
        permissionRequester = AndroidPermissionRequester.rememberAndroidPermissionRequester(
            context = context,
            permissions = permissions
        )
    )
}

@JvmName("rememberUserDrivenAskingStrategy1")
@Composable
fun AskingStrategy.rememberUserDrivenAskingStrategy(
    permissionRequester: PermissionRequester<List<String>, String>,
    canStart: () -> Boolean
): UserDrivenAskingStrategy<Map<String, Boolean>> =
    rememberUserDrivenAskingStrategy(
        type = this,
        permissionRequester = permissionRequester,
        canStart = canStart
    )
