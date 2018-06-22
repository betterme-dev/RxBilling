package com.gen.rxbilling.flow

import android.app.PendingIntent
import android.content.Intent
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.bluelinelabs.conductor.Controller

interface FlowDelegate {

    fun startFlow(pendingIntent: PendingIntent, requestCode: Int)
}

class ActivityFlowDelegate(private val activity: AppCompatActivity) : FlowDelegate {

    override fun startFlow(pendingIntent: PendingIntent, requestCode: Int) {
        activity.startIntentSenderForResult(
                pendingIntent.intentSender, requestCode, Intent(), 0, 0, 0)
    }
}

class FragmentFlowDelegate(private val fragment: Fragment) : FlowDelegate {

    override fun startFlow(pendingIntent: PendingIntent, requestCode: Int) {
        fragment.startIntentSenderForResult(
                pendingIntent.intentSender, requestCode, Intent(), 0, 0, 0, null)
    }
}

class ConductorFlowDelegate(private val controller: Controller) : FlowDelegate {

    override fun startFlow(pendingIntent: PendingIntent, requestCode: Int) {
        controller.startIntentSenderForResult(
                pendingIntent.intentSender, requestCode, Intent(), 0, 0, 0, null)
    }
}