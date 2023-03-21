package br.com.frazo.janac.ui.util.permissions.strategy

import androidx.compose.runtime.Composable
import br.com.frazo.janac.ui.util.permissions.requesters.android.AndroidPermissionRequester
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PermissionAskingStrategy<D> {
    fun resolveState()
    fun flowState(): StateFlow<PermissionFlowState<D>>
}

abstract class AndroidPermissionAskingStrategy(
    protected val androidPermissionRequester: AndroidPermissionRequester,
    protected val canStart: () -> Boolean = { true },
    protected val initialStateContent: @Composable () -> Unit,
    protected val rationalePrompt: @Composable (state: PermissionFlowStateEnum, permissionsStatus: Map<String, Boolean>, callMeWhen: RationaleCallback) -> Unit,
    protected val terminalStateContent: @Composable (state: PermissionFlowStateEnum, permissionsStatus: Map<String, Boolean>) -> Unit
) :
    PermissionAskingStrategy<@Composable () -> Unit>

data class RationaleCallback(
    val requestedUserManualGrant: () -> Unit,
    val manuallyDeniedByUser: () -> Unit,
)

enum class AskingStrategy {
    KEEP_ASKING, ONLY_ASK_SYSTEM, STOP_ASKING_ON_USER_DENIAL
}

fun androidPermissionAskingStrategyFactory(
    type: AskingStrategy,
    androidPermissionRequester: AndroidPermissionRequester,
    canStart: () -> Boolean = { true },
    initialStateContent: @Composable () -> Unit,
    rationalePrompt: @Composable (state: PermissionFlowStateEnum, permissionsStatus: Map<String, Boolean>, callMeWhen: RationaleCallback) -> Unit,
    terminalStateContent: @Composable (state: PermissionFlowStateEnum, permissionsStatus: Map<String, Boolean>) -> Unit
): AndroidPermissionAskingStrategy {
    return when (type) {
        AskingStrategy.STOP_ASKING_ON_USER_DENIAL -> StopOnUserDenial(
            androidPermissionRequester,
            canStart,
            initialStateContent,
            rationalePrompt,
            terminalStateContent
        )
        else ->
            StopOnUserDenial(
                androidPermissionRequester,
                canStart,
                initialStateContent,
                rationalePrompt,
                terminalStateContent
            )
    }
}

class StopOnUserDenial(
    androidPermissionRequester: AndroidPermissionRequester,
    canStart: () -> Boolean = { true },
    initialStateContent: @Composable () -> Unit,
    rationalePrompt: @Composable (state: PermissionFlowStateEnum, permissionsStatus: Map<String, Boolean>, callMeWhen: RationaleCallback) -> Unit,
    terminalStateContent: @Composable (state: PermissionFlowStateEnum, permissionsStatus: Map<String, Boolean>) -> Unit
) :
    AndroidPermissionAskingStrategy(
        androidPermissionRequester,
        canStart,
        initialStateContent,
        rationalePrompt,
        terminalStateContent
    ) {

    private val _flowState =
        MutableStateFlow(
            PermissionFlowState(
                PermissionFlowStateEnum.NOT_STARTED,
                initialStateContent
            )
        )
    private val flowState = _flowState.asStateFlow()
    private var lastPermissionMap = mapOf<String, Boolean>()


    private var userManuallyDenied = false
    private var requestsMade = 0
    private var waitingSystemResponse = false
    private var waitingUserResponse = false

    override fun flowState(): StateFlow<PermissionFlowState<() -> Unit>> = flowState

    override fun resolveState() {

        lastPermissionMap = androidPermissionRequester.permissionsStatus()

        if (lastPermissionMap.filter { (_,isGranted) ->
                !isGranted
            }.isEmpty())
        {
            _flowState.value =
                PermissionFlowState(
                    PermissionFlowStateEnum.TERMINAL_GRANTED,
                    prepareTerminalComposable(
                        PermissionFlowStateEnum.TERMINAL_GRANTED,
                        lastPermissionMap
                    )
                )
        } else {
            when (flowState.value.state) {
                PermissionFlowStateEnum.NOT_STARTED -> {
                    if (canStart()) {
                        requestsMade = 0
                        userManuallyDenied = false
                        _flowState.value =
                            PermissionFlowState(
                                PermissionFlowStateEnum.STARTED,
                                initialStateContent
                            )
                    }
                }

                PermissionFlowStateEnum.STARTED -> {
                    if (requestsMade == 0) {
                        if (!waitingSystemResponse) {
                            waitingSystemResponse = true
                            androidPermissionRequester.ask { permissionsMap ->
                                waitingSystemResponse = false
                                requestsMade++
                                lastPermissionMap = permissionsMap
                                val notGranted = permissionsMap.filter { (_, isGranted) ->
                                    !isGranted
                                }
                                if (notGranted.isEmpty()) {
                                    _flowState.value =
                                        PermissionFlowState(
                                            PermissionFlowStateEnum.TERMINAL_GRANTED,
                                            prepareTerminalComposable(
                                                PermissionFlowStateEnum.TERMINAL_GRANTED,
                                                permissionsMap
                                            )
                                        )
                                } else {
                                    _flowState.value =
                                        flowState.value.copy(state = PermissionFlowStateEnum.DENIED_BY_SYSTEM)
                                }
                            }
                        }
                    }
                }

                PermissionFlowStateEnum.DENIED_BY_SYSTEM -> {
                    if (!userManuallyDenied && !waitingUserResponse && !waitingSystemResponse) {
                        waitingUserResponse = true
                        val rationaleComposable: @Composable () -> Unit = {
                            initialStateContent()
                            rationalePrompt(
                                PermissionFlowStateEnum.DENIED_BY_SYSTEM,
                                lastPermissionMap,
                                RationaleCallback(
                                    requestedUserManualGrant = {
                                        waitingUserResponse = false
                                        _flowState.value = PermissionFlowState(
                                            PermissionFlowStateEnum.NOT_STARTED,
                                            initialStateContent
                                        )
                                    },
                                    manuallyDeniedByUser = {
                                        waitingUserResponse = false
                                        userManuallyDenied = true
                                        _flowState.value =
                                            PermissionFlowState(
                                                PermissionFlowStateEnum.TERMINAL_DENIED,
                                                prepareTerminalComposable(
                                                    PermissionFlowStateEnum.TERMINAL_DENIED,
                                                    lastPermissionMap
                                                )
                                            )
                                    })
                            )
                        }
                        _flowState.value =
                            PermissionFlowState(
                                PermissionFlowStateEnum.DENIED_BY_SYSTEM,
                                rationaleComposable
                            )
                    }
                }

                PermissionFlowStateEnum.TERMINAL_GRANTED -> {
                    waitingSystemResponse = true
                    androidPermissionRequester.ask {
                        lastPermissionMap = it
                        waitingSystemResponse = false
                        val notGranted = it.filter { (_, isGranted) ->
                            !isGranted
                        }
                        if (notGranted.isNotEmpty()) {
                            _flowState.value =
                                PermissionFlowState(
                                    PermissionFlowStateEnum.NOT_STARTED,
                                    initialStateContent
                                )
                        }
                    }
                }

                PermissionFlowStateEnum.TERMINAL_DENIED -> {
                    waitingSystemResponse = true
                    androidPermissionRequester.ask {
                        waitingSystemResponse = false
                        requestsMade++
                        val notGranted = it.filter { (_, isGranted) ->
                            !isGranted
                        }
                        if (notGranted.isEmpty()) {
                            _flowState.value =
                                PermissionFlowState(
                                    PermissionFlowStateEnum.TERMINAL_GRANTED,
                                    prepareTerminalComposable(
                                        PermissionFlowStateEnum.TERMINAL_GRANTED,
                                        it
                                    )
                                )
                        }
                    }
                }
            }
        }

    }

    private fun prepareTerminalComposable(
        state: PermissionFlowStateEnum,
        permissionsStatus: Map<String, Boolean>
    ): @Composable () -> Unit {

        val composable: @Composable () -> Unit = {
            terminalStateContent(
                state,
                permissionsStatus
            )
        }
        return composable

    }
}