package com.ruchitech.quicklinkcaller.persistence

/* loaded from: classes.dex */
object McsConstants {
    var ACTION_ACK = "org.microg.gms.gcm.mcs.ACK"
    var ACTION_CONNECT = "org.microg.gms.gcm.mcs.CONNECT"
    var ACTION_HEARTBEAT = "org.microg.gms.gcm.mcs.HEARTBEAT"
    var ACTION_RECONNECT = "org.microg.gms.gcm.mcs.RECONNECT"
    var ACTION_SEND = "org.microg.gms.gcm.mcs.SEND"
    var EXTRA_REASON = "org.microg.gms.gcm.mcs.REASON"
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