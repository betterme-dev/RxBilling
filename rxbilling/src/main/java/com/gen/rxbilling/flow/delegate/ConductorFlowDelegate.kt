package com.gen.rxbilling.flow.delegate

import android.app.PendingIntent
import android.content.Intent
import com.bluelinelabs.conductor.Controller

class ConductorFlowDelegate(private val controller: Controller) : FlowDelegate {

    override fun startFlow(pendingIntent: PendingIntent, requestCode: Int) {
        controller.startIntentSenderForResult(
                pendingIntent.intentSender, requestCode, Intent(), 0, 0, 0, null)
    }
}