package com.gen.rxbilling.flow.delegate

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent

class ActivityFlowDelegate(private val activity: Activity) : FlowDelegate {

    override fun startFlow(pendingIntent: PendingIntent, requestCode: Int) {
        activity.startIntentSenderForResult(
                pendingIntent.intentSender, requestCode, Intent(), 0, 0, 0)
    }
}