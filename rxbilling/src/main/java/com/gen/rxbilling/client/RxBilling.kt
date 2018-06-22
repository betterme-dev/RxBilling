package com.gen.rxbilling.client

import android.app.Activity
import com.android.billingclient.api.*
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

    fun observeUpdates(): Flowable<PurchasesUpdate>

    fun getPurchases(): Single<List<Purchase>>

    fun getSubscriptions(): Single<List<Purchase>>

    fun getPurchaseHistory(): Single<List<Purchase>>

    fun getSubscriptionHistory(): Single<List<Purchase>>

    fun getPurchaseSkuDetails(ids: List<String>): Single<List<SkuDetails>>

    fun getSubscriptionSkuDetails(ids: List<String>): Single<List<SkuDetails>>

    fun launchFlow(activity: Activity, params: BillingFlowParams): Completable

    fun consumeProduct(purchaseToken: String): Completable
}

class RxBillingImpl(billingFactory: BillingClientFactory)
    : RxBilling {

    private val updateSubject = PublishSubject.create<PurchasesUpdate>()

    private val updatedListener = PurchasesUpdatedListener { responseCode, purchases ->
        Timber.d("$responseCode", "$purchases")
        val event = when (responseCode) {
            BillingClient.BillingResponse.OK -> PurchasesUpdate.Success(responseCode, purchases.orEmpty())
            BillingClient.BillingResponse.USER_CANCELED -> PurchasesUpdate.Canceled(responseCode, purchases.orEmpty())
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

    override fun observeUpdates(): Flowable<PurchasesUpdate> {
        return connectionFlowable.flatMap {
            updateSubject.toFlowable(BackpressureStrategy.LATEST)
        }
    }

    override fun getPurchases(): Single<List<Purchase>> {
        return getBoughtItems(BillingClient.SkuType.INAPP)
    }

    override fun getSubscriptions(): Single<List<Purchase>> {
        return getBoughtItems(BillingClient.SkuType.SUBS)
    }

    override fun getPurchaseHistory(): Single<List<Purchase>> {
        return getHistory(BillingClient.SkuType.INAPP)
    }

    override fun getSubscriptionHistory(): Single<List<Purchase>> {
        return getHistory(BillingClient.SkuType.SUBS)
    }

    override fun getPurchaseSkuDetails(ids: List<String>): Single<List<SkuDetails>> {
        return getSkuDetails(ids, BillingClient.SkuType.INAPP)
    }


    override fun getSubscriptionSkuDetails(ids: List<String>): Single<List<SkuDetails>> {
        return getSkuDetails(ids, BillingClient.SkuType.SUBS)
    }

    override fun launchFlow(activity: Activity, params: BillingFlowParams): Completable {
        return connectionFlowable
                .flatMap {
                    val responseCode = it.launchBillingFlow(activity, params)
                    return@flatMap Flowable.just(responseCode)
                }
                .firstOrError()
                .flatMapCompletable {
                    return@flatMapCompletable if (isSuccess(it)) {
                        Completable.complete()
                    } else {
                        Completable.error(BillingException.fromCode(it))
                    }
                }
    }

    override fun consumeProduct(purchaseToken: String): Completable {
        return connectionFlowable
                .flatMap { client ->
                    Flowable.create<Int>({
                        client.consumeAsync(purchaseToken) { responseCode, _ ->
                            if (it.isCancelled) return@consumeAsync
                            if (isSuccess(responseCode)) {
                                it.onNext(responseCode)
                                it.onComplete()
                            } else {
                                it.onError(BillingException.fromCode(responseCode))
                            }
                        }
                    }, BackpressureStrategy.LATEST)
                }
                .firstOrError()
                .toCompletable()
    }

    private fun getBoughtItems(type: String): Single<List<Purchase>> {
        return connectionFlowable
                .flatMap {
                    val purchasesResult = it.queryPurchases(type)
                    return@flatMap if (isSuccess(purchasesResult.responseCode)) {
                        Flowable.just(purchasesResult.purchasesList.orEmpty())
                    } else {
                        Flowable.error<List<Purchase>>(BillingException.fromCode(purchasesResult.responseCode))
                    }
                }.firstOrError()
    }

    private fun getHistory(type: String): Single<List<Purchase>> {
        return connectionFlowable
                .flatMap { client ->
                    Flowable.create<List<Purchase>>({
                        client.queryPurchaseHistoryAsync(type, { responseCode: Int, mutableList: MutableList<Purchase>? ->
                            if (it.isCancelled) return@queryPurchaseHistoryAsync
                            if (isSuccess(responseCode)) {
                                it.onNext(mutableList.orEmpty())
                                it.onComplete()
                            } else {
                                it.onError(BillingException.fromCode(responseCode))
                            }
                        })
                    }, BackpressureStrategy.LATEST)
                }.firstOrError()
    }

    private fun getSkuDetails(ids: List<String>, type: String): Single<List<SkuDetails>> {
        val params = SkuDetailsParams.newBuilder().setSkusList(ids).setType(type).build()
        return connectionFlowable
                .flatMap { client ->
                    Flowable.create<List<SkuDetails>>({
                        client.querySkuDetailsAsync(params, { responseCode: Int, mutableList: MutableList<SkuDetails>? ->
                            if (it.isCancelled) return@querySkuDetailsAsync
                            if (isSuccess(responseCode)) {
                                it.onNext(mutableList.orEmpty())
                                it.onComplete()
                            } else {
                                it.onError(BillingException.fromCode(responseCode))
                            }
                        })
                    }, BackpressureStrategy.LATEST)
                }.firstOrError()
    }

    private fun isSuccess(responseCode: Int): Boolean {
        return responseCode == BillingClient.BillingResponse.OK
    }
}