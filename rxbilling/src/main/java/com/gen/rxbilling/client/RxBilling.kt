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

interface RxBilling : Connectable<BillingClient> {

    override fun connect(): Flowable<BillingClient>

    fun isFeatureSupported(@FeatureType feature: String): Single<Boolean>

    fun observeUpdates(): Flowable<PurchasesUpdate>

    fun getPurchases(@BillingClient.ProductType productType: String): Single<List<Purchase>>

    fun getPurchaseHistory(@BillingClient.ProductType productType: String): Single<List<PurchaseHistoryRecord>>

    fun getProductDetails(params: QueryProductDetailsParams): Single<List<ProductDetails>>

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
                    .andThen(billingFactory.createBillingFlowable(updatedListener))

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

    override fun getPurchases(@BillingClient.ProductType productType: String): Single<List<Purchase>> {
        return getBoughtItems(productType)
    }

    override fun getPurchaseHistory(@BillingClient.ProductType productType: String): Single<List<PurchaseHistoryRecord>> {
        return getHistory(productType)
    }

    override fun getProductDetails(params: QueryProductDetailsParams): Single<List<ProductDetails>> {
        return connectionFlowable
                .flatMapSingle { client ->
                    Single.create<List<ProductDetails>> {
                        client.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                            if (it.isDisposed) return@queryProductDetailsAsync
                            val responseCode = billingResult.responseCode
                            if (isSuccess(responseCode)) {
                                it.onSuccess(productDetailsList)
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

    private fun getBoughtItems(@BillingClient.ProductType type: String): Single<List<Purchase>> {
        return connectionFlowable
            .flatMapSingle { client ->
                Single.create<List<Purchase>> {
                    client.queryPurchasesAsync(
                        QueryPurchasesParams.newBuilder().setProductType(type).build()
                    ) { billingResult, purchases ->
                        if (it.isDisposed) return@queryPurchasesAsync
                        if (isSuccess(billingResult.responseCode)) {
                            it.onSuccess(purchases)
                        } else {
                            it.onError(BillingException.fromResult(billingResult))
                        }
                    }
                }
            }
            .firstOrError()
    }

    private fun getHistory(type: String): Single<List<PurchaseHistoryRecord>> {
        return connectionFlowable
                .flatMapSingle { client ->
                    Single.create<List<PurchaseHistoryRecord>> {
                        client.queryPurchaseHistoryAsync(
                            QueryPurchaseHistoryParams.newBuilder().setProductType(type).build()
                        ) { billingResult: BillingResult, list: MutableList<PurchaseHistoryRecord>? ->
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
