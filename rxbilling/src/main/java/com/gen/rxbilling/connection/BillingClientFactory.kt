package com.gen.rxbilling.connection

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.gen.rxbilling.exception.BillingException
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import timber.log.Timber

class BillingClientFactory(private val context: Context,
                           private val transformer:
                           FlowableTransformer<BillingClient, BillingClient>
                           = RepeatConnectionTransformer()) {

    fun createBillingFlowable(listener: PurchasesUpdatedListener): Flowable<BillingClient> {
        val flowable = Flowable.create<BillingClient>({
            val billingClient = BillingClient.newBuilder(context).setListener(listener).build()
            Timber.d("startConnection")
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {
                    Timber.d("onBillingServiceDisconnected")
                    if (!it.isCancelled) {
                        it.onComplete()
                    }
                }

                override fun onBillingSetupFinished(responseCode: Int) {
                    Timber.d("onBillingSetupFinished response $responseCode isReady ${billingClient.isReady}")
                    if (!it.isCancelled) {
                        if (responseCode == BillingClient.BillingResponse.OK) {
                            it.onNext(billingClient)
                        } else {
                            it.onError(BillingException.fromCode(responseCode))
                        }
                    } else {
                        if (billingClient.isReady) {
                            billingClient.endConnection()//release resources if there are no observers
                        }
                    }
                }
            })
            //finish connection when no subscribers
            it.setCancellable {
                Timber.d("endConnection")
                if (billingClient.isReady) {
                    billingClient.endConnection()
                }
            }
        }, BackpressureStrategy.LATEST)

        return flowable.compose(transformer)
    }
}