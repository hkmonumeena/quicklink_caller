package com.ruchitech.quicklinkcaller.helper

sealed class CallType(val digit: Digit) {
    object PostCall : CallType(Digit.ZERO)
    object IncomingCall : CallType(Digit.ONE)
    object OutgoingCall : CallType(Digit.TWO)
}