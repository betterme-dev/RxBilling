package com.gen.rxbilling.flow.delegate.support

import android.app.PendingIntent
import android.content.Intent
import android.support.v4.app.Fragment
import com.gen.rxbilling.flow.delegate.FlowDelegate

class FragmentFlowDelegate(private val fragment: Fragment) : FlowDelegate {

    override fun startFlow(pendingIntent: PendingIntent, requestCode: Int) {
        fragment.startIntentSenderForResult(
                pendingIntent.intentSender, requestCode, Intent(), 0, 0, 0, null)
    }
}