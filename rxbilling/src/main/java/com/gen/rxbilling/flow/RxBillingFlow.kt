package com.gen.rxbilling.flow

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.util.BillingHelper
import com.android.billingclient.util.BillingHelper.RESPONSE_BUY_INTENT_KEY
import com.android.vending.billing.IInAppBillingService
import com.gen.rxbilling.connection.BillingServiceFactory
import com.gen.rxbilling.exception.BillingException
import com.gen.rxbilling.flow.delegate.FlowDelegate
import com.gen.rxbilling.lifecycle.Connectable
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import timber.log.Timber

class RxBillingFlow(
        private val context: Context,
        factory: BillingServiceFactory) : Connectable<IInAppBillingService> {

    private val connectionFlowable = factory
            .createConnection()

    override fun connect(): Flowable<IInAppBillingService> {
        return connectionFlowable
    }

    fun buyItem(request: BuyItemRequest, delegate: FlowDelegate): Completable {
        return connectionFlowable
                .flatMap {
                    val buyIntentBundle = it.getBuyIntent(
                            /* apiVersion */ 3,
                            context.packageName,
                            request.id,
                            request.type,
                            /* developerPayload */ null)
                    val responseCode = BillingHelper.getResponseCodeFromBundle(buyIntentBundle, null)
                    val responseMessage = BillingHelper.getDebugMessageFromBundle(buyIntentBundle, null)
                    val billingResult = BillingResult.newBuilder()
                            .setResponseCode(responseCode)
                            .setDebugMessage(responseMessage)
                            .build()
                    if (responseCode == BillingClient.BillingResponseCode.OK) {
                        val pendingIntent: PendingIntent = buyIntentBundle.getParcelable(RESPONSE_BUY_INTENT_KEY)!!
                        delegate.startFlow(pendingIntent, request.requestCode)
                        return@flatMap Flowable.just(responseCode)
                    } else {
                        return@flatMap Flowable.error<Int>(BillingException.fromResult(billingResult))
                    }
                }
                .firstOrError()
                .toCompletable()
    }

    fun replaceItem(request: ReplaceItemRequest, delegate: FlowDelegate): Completable {
        return connectionFlowable
                .flatMap {
                    val buyIntentBundle = it.getBuyIntentToReplaceSkus(
                            /* apiVersion */ 5,
                            context.packageName,
                            listOf(request.oldId),
                            request.newId,
                            BillingClient.SkuType.SUBS,
                            /* developerPayload */ null

                    )
                    val responseCode = BillingHelper.getResponseCodeFromBundle(buyIntentBundle, null)
                    val responseMessage = BillingHelper.getDebugMessageFromBundle(buyIntentBundle, null)
                    val billingResult = BillingResult.newBuilder()
                            .setResponseCode(responseCode)
                            .setDebugMessage(responseMessage)
                            .build()
                    if (responseCode == BillingClient.BillingResponseCode.OK) {
                        val pendingIntent: PendingIntent = buyIntentBundle.getParcelable(RESPONSE_BUY_INTENT_KEY)!!
                        delegate.startFlow(pendingIntent, request.requestCode)
                        return@flatMap Flowable.just(responseCode)
                    } else {
                        return@flatMap Flowable.error<Int>(BillingException.fromResult(billingResult))
                    }
                }
                .firstOrError()
                .toCompletable()
    }

    fun handleActivityResult(activityResultCode: Int, data: Intent?): Single<Purchase> {
        return Single.create<Purchase> {
            if (it.isDisposed) return@create
            Timber.d("onActivityResult %s", data?.extras)
            val billingResult = BillingHelper.getBillingResultFromIntent(data, null)
            val responseCode = billingResult.responseCode
            when (responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    val purchases = BillingHelper.extractPurchases(data?.extras)
                    it.onSuccess(purchases[0])
                }
                else -> it.onError(BillingException.fromResult(billingResult))
            }
        }
    }
}
