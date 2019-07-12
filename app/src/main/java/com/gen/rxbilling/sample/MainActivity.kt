package com.gen.rxbilling.sample

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import com.gen.rxbilling.client.RxBilling
import com.gen.rxbilling.client.RxBillingImpl
import com.gen.rxbilling.connection.BillingClientFactory
import com.gen.rxbilling.connection.BillingServiceFactory
import com.gen.rxbilling.flow.delegate.ActivityFlowDelegate
import com.gen.rxbilling.flow.BuyItemRequest
import com.gen.rxbilling.flow.RxBillingFlow
import com.gen.rxbilling.lifecycle.BillingConnectionManager
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var rxBilling: RxBilling
    private lateinit var rxBillingFlow: RxBillingFlow
    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rxBilling = RxBillingImpl(BillingClientFactory(applicationContext))
        rxBillingFlow = RxBillingFlow(applicationContext, BillingServiceFactory(this))
        lifecycle.addObserver(BillingConnectionManager(rxBilling))
        lifecycle.addObserver(BillingConnectionManager(rxBillingFlow))
        btnLaunchServiceFlow.setOnClickListener {
            startFlowWithService()
        }
        btnLaunchClientFlow.setOnClickListener {
            startFlowWithClient()
        }
        btnLoadPurchases.setOnClickListener {
            loadPurchases()
        }
        btnLoadHistory.setOnClickListener {
            loadHistory()
        }
        btnLoadDetails.setOnClickListener {
            loadDetails()
        }
        btnAcknowledge.setOnClickListener {
            acknowledge()
        }
    }

    override fun onStart() {
        super.onStart()
        disposable.add(
                rxBilling.observeUpdates()
                        .subscribe({
                            Timber.d("observeUpdates $it")
                            tvClientFlow.text = it.toString()
                        }, {
                            Timber.e(it)
                            tvClientFlow.text = it.toString()
                        }))
    }

    override fun onStop() {
        disposable.clear()
        super.onStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        disposable.add(
                rxBillingFlow.handleActivityResult(data)
                        .subscribe({
                            Timber.d("onActivityResult $it")
                            tvServiceFlow.text = it.toString()
                        }, {
                            Timber.e(it)
                            tvServiceFlow.text = it.toString()
                        }))
    }

    private fun startFlowWithService() {
        disposable.add(
                rxBillingFlow.buyItem(
                        BuyItemRequest(BillingClient.SkuType.SUBS, "your_id", 101),
                        ActivityFlowDelegate(this)
                )
                        .subscribe({
                            Timber.d("flow started")
                        }, {
                            Timber.e(it)
                        }))
    }

    private fun startFlowWithClient() {
        disposable.add(
                rxBilling.launchFlow(this, BillingFlowParams.newBuilder()
                        .setSkuDetails(SkuDetails("{}"))
                        .build())
                        .subscribe({
                            Timber.d("startFlowWithClient")
                        }, {
                            Timber.e(it)
                        }))
    }

    private fun loadPurchases() {
        disposable.add(
                rxBilling.getPurchases(BillingClient.SkuType.SUBS)
                        .subscribe({
                            Timber.d("getPurchases $it")
                            tvPurchases.text = it.toString()
                        }, {
                            Timber.e(it)
                        }))
    }

    private fun loadHistory() {
        disposable.add(
                rxBilling.getPurchaseHistory(BillingClient.SkuType.SUBS)
                        .subscribe({
                            Timber.d("getPurchaseHistory $it")
                            tvHistory.text = it.toString()
                        }, {
                            Timber.e(it)
                        }))
    }

    private fun loadDetails() {
        disposable.add(
                rxBilling.getSkuDetails(
                        SkuDetailsParams.newBuilder()
                                .setSkusList(listOf("your_id1", "your_id2"))
                                .setType(BillingClient.SkuType.SUBS)
                                .build())
                        .subscribe({
                            Timber.d("loadDetails $it")
                            tvDetails.text = it.toString()
                        }, {
                            Timber.e(it)
                        }))
    }

    private fun acknowledge() {
        disposable.add(
                rxBilling.acknowledge(
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken("token")
                                .setDeveloperPayload("payload")
                                .build())
                        .subscribe({
                            Timber.d("acknowledge success")
                        }, {
                            Timber.e(it)
                        }))
    }
}
