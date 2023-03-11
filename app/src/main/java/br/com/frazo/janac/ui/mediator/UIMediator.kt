package br.com.frazo.janac.ui.mediator

import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

interface UIMediator {

    fun inform(from: UIParticipant, event: UIEvent, to: List<UIParticipant>): MediationResult

    fun broadcast(from: UIParticipant, event: UIEvent): MediationResult

    fun <E: UIEvent>broadcastFlowOfEvent(eventClass: KClass<E>): Flow<Pair<UIParticipant,UIEvent>?>

    fun addParticipant(participant: UIParticipant)

    fun removeParticipant(participant: UIParticipant)

}

sealed class MediationResult{

    data class Sent(val event: UIEvent, val receivers: List<UIParticipant>): MediationResult()
    data class NotRegisteredReceiver(val event: UIEvent, val receivers: List<UIParticipant>): MediationResult()
    data class NotRegisteredSender(val event: UIEvent, val sender: UIParticipant): MediationResult()

}