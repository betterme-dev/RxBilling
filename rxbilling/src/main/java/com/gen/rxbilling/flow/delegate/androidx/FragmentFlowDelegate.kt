package com.gen.rxbilling.flow.delegate.androidx

import android.app.PendingIntent
import android.content.Intent
import com.gen.rxbilling.flow.delegate.FlowDelegate

class FragmentFlowDelegate(private val fragment: androidx.fragment.app.Fragment) : FlowDelegate {

    override fun startFlow(pendingIntent: PendingIntent, requestCode: Int) {
        fragment.startIntentSenderForResult(
                pendingIntent.intentSender, requestCode, Intent(), 0, 0, 0, null)
    }
}