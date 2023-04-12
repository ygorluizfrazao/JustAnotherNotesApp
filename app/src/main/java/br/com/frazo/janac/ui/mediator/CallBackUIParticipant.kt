package br.com.frazo.janac.ui.mediator

class CallBackUIParticipant(private val onMessageReceived: (sender: UIParticipant, event: UIEvent) -> Unit) :
    UIParticipant {

    override fun receiveMessage(from: UIParticipant, event: UIEvent) {
        onMessageReceived(from, event)
    }

}