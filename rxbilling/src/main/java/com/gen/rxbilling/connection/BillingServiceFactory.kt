package com.gen.rxbilling.connection

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.android.vending.billing.IInAppBillingService
import com.gen.rxbilling.exception.BillingException
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import timber.log.Timber

class BillingServiceFactory(private val context: Context,
                            private val transformer:
                            FlowableTransformer<IInAppBillingService, IInAppBillingService>
                            = RepeatConnectionTransformer()) {

    companion object {
        private const val BIND_ACTION = "com.android.vending.billing.InAppBillingService.BIND"
        private const val BILLING_PACKAGE = "com.android.vending"

    }

    fun createConnection(): Flowable<IInAppBillingService> {
        val flowable = Flowable.create<IInAppBillingService>({ emitter ->
            var bound = false
            Timber.d("createConnection")
            val serviceIntent = Intent(BIND_ACTION)
            serviceIntent.`package` = BILLING_PACKAGE
            val serviceConnection = object : ServiceConnection {
                override fun onServiceDisconnected(p0: ComponentName?) {
                    if (!emitter.isCancelled) {
                        emitter.onComplete()
                    }
                }

                override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
                    if (!emitter.isCancelled) {
                        bound = true
                        val service = IInAppBillingService.Stub.asInterface(p1)
                        emitter.onNext(service!!)
                        Timber.d("onServiceConnected \n$p1 \n$service")
                    } else {
                        Timber.d("service connected but not needed")
                        context.unbindService(this)
                    }
                }
            }
            val bindService = context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            if (!bindService && !emitter.isCancelled) {
                emitter.onError(BillingException.BillingUnavailableException())
                return@create
            }
            emitter.setCancellable {
                Timber.d("endConnection")
                if (bound) {
                    context.unbindService(serviceConnection)
                }
            }
        }, BackpressureStrategy.LATEST)
        return flowable.compose(transformer)
    }
}