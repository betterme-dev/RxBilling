package com.gen.rxbilling3.connection

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchasesUpdatedListener
import com.gen.rxbilling3.exception.BillingException
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.FlowableTransformer
import timber.log.Timber

class BillingClientFactory(
        private val context: Context,
        private val transformer: FlowableTransformer<BillingClient, BillingClient>
        = RepeatConnectionTransformer()
) {

    fun createBillingFlowable(listener: PurchasesUpdatedListener): Flowable<BillingClient> {
        val flowable = Flowable.create<BillingClient>({
            val billingClient = BillingClient.newBuilder(context)
                    .enablePendingPurchases()
                    .setListener(listener)
                    .build()
            Timber.d("startConnection")
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {
                    Timber.d("onBillingServiceDisconnected")
                    if (!it.isCancelled) {
                        it.onComplete()
                    }
                }

                override fun onBillingSetupFinished(result: BillingResult) {
                    val responseCode = result.responseCode
                    Timber.d("onBillingSetupFinished response $responseCode isReady ${billingClient.isReady}")
                    if (!it.isCancelled) {
                        if (responseCode == BillingClient.BillingResponseCode.OK) {
                            it.onNext(billingClient)
                        } else {
                            it.onError(BillingException.fromResult(result))
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
