package com.gen.rxbilling.sample

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingFlowParams.SubscriptionUpdateParams.ReplacementMode
import com.android.billingclient.api.QueryProductDetailsParams
import com.gen.rxbilling.client.RxBilling
import com.gen.rxbilling.client.RxBillingImpl
import com.gen.rxbilling.connection.BillingClientFactory
import com.gen.rxbilling.lifecycle.BillingConnectionManager
import com.gen.rxbilling.sample.databinding.ActivityMainBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var rxBilling: RxBilling
    private val disposable = CompositeDisposable()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        rxBilling = RxBillingImpl(BillingClientFactory(applicationContext))
        lifecycle.addObserver(BillingConnectionManager(rxBilling))

        with(binding) {
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
    }

    override fun onStart() {
        super.onStart()
        disposable.add(
            rxBilling.observeUpdates()
                .subscribe({
                    Timber.d("observeUpdates $it")
                    binding.tvClientFlow.text = it.toString()
                }, {
                    Timber.e(it)
                    binding.tvClientFlow.text = it.toString()
                })
        )
    }

    override fun onStop() {
        disposable.clear()
        super.onStop()
    }

    private fun startFlowWithClient() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)
                        .setProductId("your_id1")
                        .build(),
                ),
            )
            .build()
        disposable.add(
            rxBilling.getProductDetails(params)
                .flatMapCompletable { productDetailsList ->
                    val productDetails = productDetailsList[0]
                    val offerIndex = 0
                    val offerToken = productDetails.subscriptionOfferDetails
                        ?.get(offerIndex)?.offerToken ?: ""
                    rxBilling.launchFlow(
                        this, BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(
                                listOf(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                        .setProductDetails(productDetails)
                                        .setOfferToken(offerToken)
                                        .build()
                                )
                            )
                            .setSubscriptionUpdateParams(
                                BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                                    .setOldPurchaseToken("old_purchase_token")
                                    .setSubscriptionReplacementMode(ReplacementMode.CHARGE_PRORATED_PRICE)
                                    .build()
                            )
                            .build()
                    )
                }
                .subscribe({
                    Timber.d("startFlowWithClient")
                }, {
                    Timber.e(it)
                })
        )
    }

    private fun loadPurchases() {
        disposable.add(
            rxBilling.getPurchases(BillingClient.ProductType.SUBS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Timber.d("getPurchases $it")
                    binding.tvPurchases.text = it.toString()
                }, {
                    Timber.e(it)
                })
        )
    }

    private fun loadHistory() {
        disposable.add(
            rxBilling.getPurchaseHistory(BillingClient.ProductType.SUBS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Timber.d("getPurchaseHistory $it")
                    binding.tvHistory.text = it.toString()
                }, {
                    Timber.e(it)
                })
        )
    }

    private fun loadDetails() {
        disposable.add(
            rxBilling.getProductDetails(
                QueryProductDetailsParams.newBuilder()
                    .setProductList(
                        listOf(
                            QueryProductDetailsParams.Product.newBuilder()
                                .setProductType(BillingClient.ProductType.SUBS)
                                .setProductId("your_id1")
                                .build(),
                            QueryProductDetailsParams.Product.newBuilder()
                                .setProductType(BillingClient.ProductType.SUBS)
                                .setProductId("your_id2")
                                .build(),
                        ),
                    )
                    .build()
            )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Timber.d("loadDetails $it")
                    binding.tvDetails.text = it.toString()
                }, {
                    Timber.e(it)
                })
        )
    }

    private fun acknowledge() {
        disposable.add(
            rxBilling.acknowledge(
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken("token")
                    .build()
            )
                .subscribe({
                    Timber.d("acknowledge success")
                }, {
                    Timber.e(it)
                })
        )
    }
}
