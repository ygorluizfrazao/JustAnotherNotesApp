package br.com.frazo.janac.ui.util.permissions.base.strategy

import android.content.Context
import android.view.View
import androidx.compose.material3.DismissState
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import br.com.frazo.janac.ui.util.permissions.base.requesters.PermissionRequester
import br.com.frazo.janac.ui.util.permissions.base.requesters.android.AndroidPermissionRequester

/**
 * Possible Permission Asking Strategies.
 *
 * Used with [rememberUserDrivenAskingStrategy] to create the appropriate [UserDrivenAskingStrategy]
 * instance for the given need.
 */
enum class AskingStrategy {
    /**
     * This strategy consists in keep asking for permission while exists denied/not granted permissions.
     *
     * The internal state machine can assume the following states
     * [PermissionFlowStateEnum.NOT_STARTED],
     * [PermissionFlowStateEnum.STARTED],
     * [PermissionFlowStateEnum.DENIED_BY_SYSTEM],
     * [PermissionFlowStateEnum.APP_PROMPT] and
     * [PermissionFlowStateEnum.TERMINAL_GRANTED].
     *
     * @see KeepAskingStrategy
     */
    KEEP_ASKING,

    /**
     * This strategy consists in only asking for permission to the system, no additional custom
     * prompt is expected to be showed to the the user. In summary, if exists denied/not granted
     * it will ask the system at least twice. After that, if still exists denied/not granted
     * permissions, it will assume the [PermissionFlowStateEnum.TERMINAL_DENIED] state.
     *
     *
     * The internal state machine can assume the following states
     * [PermissionFlowStateEnum.NOT_STARTED],
     * [PermissionFlowStateEnum.STARTED],
     * [PermissionFlowStateEnum.TERMINAL_GRANTED].
     * [PermissionFlowStateEnum.TERMINAL_DENIED]
     *
     * @see OnlyAskSystem
     */
    ONLY_ASK_SYSTEM,

    /**
     * This strategy consists in keep asking for permission while exists denied/not granted
     * permissions and the user has not denied the permissions manually inside the instance
     * scope.
     *
     *
     * The internal state machine can assume the following states
     * [PermissionFlowStateEnum.NOT_STARTED],
     * [PermissionFlowStateEnum.STARTED],
     * [PermissionFlowStateEnum.DENIED_BY_SYSTEM]
     * [PermissionFlowStateEnum.APP_PROMPT]
     * [PermissionFlowStateEnum.TERMINAL_GRANTED].
     * [PermissionFlowStateEnum.TERMINAL_DENIED]
     *
     * @see StopOnUserDenialAskingStrategy
     */
    STOP_ASKING_ON_USER_DENIAL
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
        AskingStrategy.ONLY_ASK_SYSTEM -> OnlyAskSystem(permissionRequester, canStart)
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
        AskingStrategy.ONLY_ASK_SYSTEM -> {
            OnlyAskSystem.rememberSavable(permissionRequester, canStart)
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
