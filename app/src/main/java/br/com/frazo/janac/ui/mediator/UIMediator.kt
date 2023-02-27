package br.com.frazo.janac.ui.mediator

interface UIMediator {

    fun inform(from: UIParticipant, event: UIEvent, to: List<UIParticipant>): MediationResult

    fun broadCast(from: UIParticipant, event: UIEvent): MediationResult

    fun addParticipant(participant: UIParticipant)

    fun removeParticipant(participant: UIParticipant)

}

sealed class MediationResult{

    data class Sent(val event: UIEvent, val receivers: List<UIParticipant>): MediationResult()
    data class NotRegisteredReceiver(val event: UIEvent, val receivers: List<UIParticipant>): MediationResult()
    data class NotRegisteredSender(val event: UIEvent, val sender: UIParticipant): MediationResult()

}