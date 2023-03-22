package br.com.frazo.janac.ui.util.permissions.base.strategy

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import br.com.frazo.janac.ui.util.permissions.base.requesters.PermissionRequester

enum class AskingStrategy {
    KEEP_ASKING, ONLY_ASK_SYSTEM, STOP_ASKING_ON_USER_DENIAL
}

fun createUserDrivenAskingStrategy(
    type: AskingStrategy,
    androidPermissionRequester: PermissionRequester<List<String>, String>,
    canStart: () -> Boolean = { true }
): UserDrivenAskingStrategy<Map<String, Boolean>> {
    return when (type) {
        AskingStrategy.STOP_ASKING_ON_USER_DENIAL -> StopOnUserDenialAskingStrategy(
            androidPermissionRequester,
            canStart
        )
        else ->
            StopOnUserDenialAskingStrategy(
                androidPermissionRequester,
                canStart
            )
    }
}

@Composable
fun rememberUserDrivenAskingStrategy(
    type: AskingStrategy,
    androidPermissionRequester: PermissionRequester<List<String>, String>,
    canStart: () -> Boolean
): UserDrivenAskingStrategy<Map<String, Boolean>> {
    return rememberSaveable(type, androidPermissionRequester, canStart) {
        createUserDrivenAskingStrategy(type, androidPermissionRequester, canStart)
    }
}

@JvmName("rememberUserDrivenAskingStrategy1")
@Composable
fun AskingStrategy.rememberUserDrivenAskingStrategy(
    androidPermissionRequester: PermissionRequester<List<String>, String>,
    canStart: () -> Boolean
): UserDrivenAskingStrategy<Map<String, Boolean>> =
    rememberUserDrivenAskingStrategy(
        type = this,
        androidPermissionRequester = androidPermissionRequester,
        canStart = canStart
    )
