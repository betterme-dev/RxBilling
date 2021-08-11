package com.gen.rxbilling.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import com.gen.rxbilling3.client.RxBilling
import com.gen.rxbilling3.client.RxBillingImpl
import com.gen.rxbilling3.connection.BillingClientFactory
import com.gen.rxbilling3.lifecycle.BillingConnectionManager
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var rxBilling: RxBilling
    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rxBilling = RxBillingImpl(BillingClientFactory(applicationContext))
        lifecycle.addObserver(BillingConnectionManager(rxBilling))

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
                                .build())
                        .subscribe({
                            Timber.d("acknowledge success")
                        }, {
                            Timber.e(it)
                        }))
    }
}
