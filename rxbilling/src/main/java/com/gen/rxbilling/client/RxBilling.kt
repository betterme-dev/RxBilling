package com.gen.rxbilling.client

import android.app.Activity
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.FeatureType
import com.gen.rxbilling.connection.BillingClientFactory
import com.gen.rxbilling.exception.BillingException
import com.gen.rxbilling.lifecycle.Connectable
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

interface RxBilling : Connectable<BillingClient> {

    override fun connect(): Flowable<BillingClient>

    fun isFeatureSupported(@FeatureType feature: String): Single<Boolean>

    fun observeUpdates(): Flowable<PurchasesUpdate>

    fun getPurchases(@BillingClient.SkuType skuType: String): Single<List<Purchase>>

    fun getPurchaseHistory(@BillingClient.SkuType skuType: String): Single<List<PurchaseHistoryRecord>>

    fun getSkuDetails(params: SkuDetailsParams): Single<List<SkuDetails>>

    fun launchFlow(activity: Activity, params: BillingFlowParams): Completable

    fun consumeProduct(params: ConsumeParams): Completable

    fun acknowledge(params: AcknowledgePurchaseParams): Completable
}

class RxBillingImpl(
        billingFactory: BillingClientFactory
) : RxBilling {

    private val updateSubject = PublishSubject.create<PurchasesUpdate>()

    private val updatedListener = PurchasesUpdatedListener { result, purchases ->
        val event = when (val responseCode = result.responseCode) {
            BillingClient.BillingResponseCode.OK -> PurchasesUpdate.Success(responseCode, purchases.orEmpty())
            BillingClient.BillingResponseCode.USER_CANCELED -> PurchasesUpdate.Canceled(responseCode, purchases.orEmpty())
            else -> PurchasesUpdate.Failed(responseCode, purchases.orEmpty())
        }
        updateSubject.onNext(event)
    }

    private val connectionFlowable =
            Completable.complete()
                    .observeOn(AndroidSchedulers.mainThread()) // just to be sure billing client is called from main thread
                    .andThen(
                        billingFactory.createBillingFlowable(updatedListener)
                            .doOnError { Timber.e(it, "Failed to create billing connection flowable!") }
                    )

    override fun connect(): Flowable<BillingClient> {
        return connectionFlowable
    }

    override fun isFeatureSupported(@FeatureType feature: String): Single<Boolean> {
        return connectionFlowable.flatMapSingle {
            Single.defer {
                val result = it.isFeatureSupported(feature)
                Single.just(result.responseCode == BillingClient.BillingResponseCode.OK)
            }
        }.firstOrError()
    }

    override fun observeUpdates(): Flowable<PurchasesUpdate> {
        return connectionFlowable.flatMap {
            updateSubject.toFlowable(BackpressureStrategy.LATEST)
        }
    }

    override fun getPurchases(@BillingClient.SkuType skuType: String): Single<List<Purchase>> {
        return getBoughtItems(skuType)
    }

    override fun getPurchaseHistory(@BillingClient.SkuType skuType: String): Single<List<PurchaseHistoryRecord>> {
        return getHistory(skuType)
    }

    override fun getSkuDetails(params: SkuDetailsParams): Single<List<SkuDetails>> {
        return connectionFlowable
                .flatMapSingle { client ->
                    Single.create<List<SkuDetails>> {
                        client.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
                            if (it.isDisposed) return@querySkuDetailsAsync
                            val responseCode = billingResult.responseCode
                            if (isSuccess(responseCode)) {
                                it.onSuccess(skuDetailsList.orEmpty())
                            } else {
                                it.onError(BillingException.fromResult(billingResult))
                            }
                        }
                    }
                }.firstOrError()
    }

    override fun launchFlow(activity: Activity, params: BillingFlowParams): Completable {
        return connectionFlowable
                .flatMap {
                    val responseCode = it.launchBillingFlow(activity, params)
                    return@flatMap Flowable.just(responseCode)
                }
                .firstOrError()
                .flatMapCompletable {
                    return@flatMapCompletable if (isSuccess(it.responseCode)) {
                        Completable.complete()
                    } else {
                        Completable.error(BillingException.fromResult(it))
                    }
                }
    }

    override fun consumeProduct(params: ConsumeParams): Completable {
        return connectionFlowable
                .flatMapSingle { client ->
                    Single.create<Int> {
                        client.consumeAsync(params) { result, _ ->
                            if (it.isDisposed) return@consumeAsync
                            val responseCode = result.responseCode
                            if (isSuccess(responseCode)) {
                                it.onSuccess(responseCode)
                            } else {
                                it.onError(BillingException.fromResult(result))
                            }
                        }
                    }
                }
                .firstOrError()
                .ignoreElement()
    }

    override fun acknowledge(params: AcknowledgePurchaseParams): Completable {
        return connectionFlowable
                .flatMapSingle { client ->
                    Single.create<Int> {
                        client.acknowledgePurchase(params) { result ->
                            if (it.isDisposed) return@acknowledgePurchase
                            val responseCode = result.responseCode
                            if (isSuccess(responseCode)) {
                                it.onSuccess(responseCode)
                            } else {
                                it.onError(BillingException.fromResult(result))
                            }
                        }
                    }
                }
                .firstOrError()
                .ignoreElement()
    }

    private fun getBoughtItems(@BillingClient.SkuType type: String): Single<List<Purchase>> {
        return connectionFlowable
                .flatMapSingle {
                    val purchasesResult = it.queryPurchases(type)
                    return@flatMapSingle if (isSuccess(purchasesResult.responseCode)) {
                        Single.just(purchasesResult.purchasesList.orEmpty())
                    } else {
                        Single.error(BillingException.fromResult(purchasesResult.billingResult))
                    }
                }.firstOrError()
    }

    private fun getHistory(@BillingClient.SkuType type: String): Single<List<PurchaseHistoryRecord>> {
        return connectionFlowable
                .flatMapSingle { client ->
                    Single.create<List<PurchaseHistoryRecord>> {
                        client.queryPurchaseHistoryAsync(type) { billingResult: BillingResult, list: MutableList<PurchaseHistoryRecord>? ->
                            if (it.isDisposed) return@queryPurchaseHistoryAsync
                            val responseCode = billingResult.responseCode
                            if (isSuccess(responseCode)) {
                                it.onSuccess(list.orEmpty())
                            } else {
                                it.onError(BillingException.fromResult(billingResult))
                            }
                        }
                    }
                }.firstOrError()
    }

    private fun isSuccess(responseCode: Int): Boolean {
        return responseCode == BillingClient.BillingResponseCode.OK
    }
}
