package br.com.frazo.janac.ui.mediator

import kotlinx.coroutines.flow.*
import kotlin.reflect.KClass

class UIMediatorImpl(private var participants: List<UIParticipant> = emptyList()) : UIMediator {

    private val eventBroadcastMap =
        mutableMapOf<KClass<out UIEvent>, MutableStateFlow<Pair<UIParticipant, UIEvent>?>>()

    override fun inform(
        from: UIParticipant,
        event: UIEvent,
        to: List<UIParticipant>
    ): MediationResult {

        if (!participants.contains(from))
            return MediationResult.NotRegisteredSender(event, from)

        val receivers = participants.filter { to.contains(it) }
        receivers.forEach {
            it.receiveMessage(from, event)
        }

        if (receivers.size == to.size)
            return MediationResult.Sent(event, receivers)

        return MediationResult.NotRegisteredReceiver(event, to.filter { !receivers.contains(it) })
    }

    override fun broadcast(from: UIParticipant, event: UIEvent): MediationResult {

        if (!participants.contains(from))
            return MediationResult.NotRegisteredSender(event, from)

        participants.forEach {
            it.receiveMessage(from, event)
        }

        val flow = eventBroadcastMap.getOrPut(event::class) {
            MutableStateFlow(Pair(from, event))
        }

        flow.value = Pair(from, event)

        return MediationResult.Sent(event, participants)
    }

    override fun <E : UIEvent> broadcastFlowOfEvent(eventClass: KClass<E>): Flow<Pair<UIParticipant, UIEvent>?> {
        return eventBroadcastMap.getOrPut(eventClass) {
            MutableStateFlow(null)
        }
    }

    override fun addParticipant(participant: UIParticipant) {
        if (!participants.contains(participant))
            participants = participants + participant
    }

    override fun removeParticipant(participant: UIParticipant) {
        participants = participants - participant
    }

}