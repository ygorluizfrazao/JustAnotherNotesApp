package br.com.frazo.janac.ui.mediator

interface UIParticipant {

    fun receiveMessage(from: UIParticipant, event: UIEvent)

}