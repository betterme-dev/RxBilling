package com.gen.rxbilling.flow.delegate

import android.app.PendingIntent
import android.content.Intent

class FragmentFlowDelegate(private val fragment: androidx.fragment.app.Fragment) : FlowDelegate {

    override fun startFlow(pendingIntent: PendingIntent, requestCode: Int) {
        fragment.startIntentSenderForResult(
                pendingIntent.intentSender, requestCode, Intent(), 0, 0, 0, null)
    }
}
