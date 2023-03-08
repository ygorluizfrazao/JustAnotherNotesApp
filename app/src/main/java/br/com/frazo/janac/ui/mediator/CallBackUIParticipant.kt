package br.com.frazo.janac.ui.mediator

class CallBackUIParticipant(private val onMessageReceived: (UIParticipant, UIEvent) -> Unit) :
    UIParticipant {

    override fun receiveMessage(from: UIParticipant, event: UIEvent) {
        onMessageReceived(from, event)
    }

}