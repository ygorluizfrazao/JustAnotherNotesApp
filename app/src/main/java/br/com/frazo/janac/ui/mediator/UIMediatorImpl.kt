package br.com.frazo.janac.ui.mediator

class UIMediatorImpl(participants: List<UIParticipant> = emptyList()) : UIMediator {

    private val participants = mutableListOf<UIParticipant>().apply {
        addAll(participants)
    }

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

    override fun broadCast(from: UIParticipant, event: UIEvent): MediationResult {

        if (!participants.contains(from))
            return MediationResult.NotRegisteredSender(event, from)

        participants.forEach {
            it.receiveMessage(from, event)
        }

        return MediationResult.Sent(event, participants)
    }

    override fun addParticipant(participant: UIParticipant) {
        if(!participants.contains(participant))
            participants.add(participant)
    }

    override fun removeParticipant(participant: UIParticipant) {
        participants.remove(participant)
    }

}