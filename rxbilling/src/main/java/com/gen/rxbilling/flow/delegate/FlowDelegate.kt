package com.gen.rxbilling.flow.delegate

import android.app.PendingIntent

interface FlowDelegate {

    fun startFlow(pendingIntent: PendingIntent, requestCode: Int)
}

