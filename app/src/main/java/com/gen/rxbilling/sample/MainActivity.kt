package com.gen.rxbilling.sample

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.SkuDetails
import com.gen.rxbilling.client.RxBilling
import com.gen.rxbilling.client.RxBillingImpl
import com.gen.rxbilling.connection.BillingClientFactory
import com.gen.rxbilling.connection.BillingServiceFactory
import com.gen.rxbilling.flow.delegate.ActivityFlowDelegate
import com.gen.rxbilling.flow.BuyItemRequest
import com.gen.rxbilling.flow.RxBillingFlow
import com.gen.rxbilling.lifecycle.arch.BillingConnectionManager
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
        disposable.add(rxBilling.observeUpdates()
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
        disposable.add(rxBillingFlow.handleActivityResult(resultCode, data)
                .subscribe({
                    Timber.d("onActivityResult $it")
                    tvServiceFlow.text = it.toString()
                }, {
                    Timber.e(it)
                    tvServiceFlow.text = it.toString()
                }))
    }

    private fun startFlowWithService() {
        disposable.add(rxBillingFlow.buyItem(BuyItemRequest(BillingClient.SkuType.SUBS, "your_id", 101),
                ActivityFlowDelegate(this))
                .subscribe({
                    Timber.d("flow started")
                }, {
                    Timber.e(it)
                }))
    }

    private fun startFlowWithClient() {
        disposable.add(rxBilling.launchFlow(this, BillingFlowParams.newBuilder()
                .setSkuDetails(SkuDetails("{}"))
                .build())
                .subscribe({
                    Timber.d("startFlowWithClient")
                }, {
                    Timber.e(it)
                }))
    }

    private fun loadPurchases() {
        disposable.add(rxBilling.getPurchases()
                .subscribe({
                    Timber.d("getPurchases $it")
                    tvPurchases.text = it.toString()
                }, {
                    Timber.e(it)
                }))
    }

    private fun loadHistory() {
        disposable.add(rxBilling.getPurchaseHistory()
                .subscribe({
                    Timber.d("getPurchaseHistory $it")
                    tvHistory.text = it.toString()
                }, {
                    Timber.e(it)
                }))
    }

    private fun loadDetails() {
        disposable.add(rxBilling.getPurchaseSkuDetails(listOf("your_id1", "your_id2"))
                .subscribe({
                    Timber.d("loadDetails $it")
                    tvDetails.text = it.toString()
                }, {
                    Timber.e(it)
                }))
    }

    private fun acknowledge() {
        disposable.add(rxBilling.acknowledge("token")
                .subscribe({
                    Timber.d("acknowledge success")
                }, {
                    Timber.e(it)
                }))
    }
}
