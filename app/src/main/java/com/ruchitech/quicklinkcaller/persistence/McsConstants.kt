package com.ruchitech.quicklinkcaller.persistence


object McsConstants {
    var ACTION_ACK = "com.ruchitech.quicklinkcaller.persistence.ACK"
    var ACTION_CONNECT = "com.ruchitech.quicklinkcaller.persistence.CONNECT"
    var ACTION_HEARTBEAT = "com.ruchitech.quicklinkcaller.persistence.HEARTBEAT"
    var ACTION_RECONNECT = "com.ruchitech.quicklinkcaller.persistence.RECONNECT"
    var ACTION_SEND = "com.ruchitech.quicklinkcaller.persistence.SEND"
    var EXTRA_REASON = "com.ruchitech.quicklinkcaller.persistence.REASON"
    const val SERVICE_STARTED = 40
    const val HEARTBEAT_INITIATED = 41
    const val PERIODIC_5_S = 42
    const val INITIATING_MANUAL_WORK = 44
    const val ONE_MINUTE: Long = 60000
    const val FIVE_SEC_DELAY: Long = 5000
    const val ONE_MINUTE_FIFTEEN_SECONDS = 75000L
    const val TWENTY_SECONDS = 20000L
    const val ZERO = 0
    const val CALL_STATE_RINGING = 99
    const val CALL_STATE_OFFHOOK = 100

}