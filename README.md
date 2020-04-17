# RxBilling
Rx wrapper for Billing Library with connection management

# Download

 [ ![Download](https://api.bintray.com/packages/betterme/rxbilling/com.betterme%3Arxbilling/images/download.svg) ](https://bintray.com/betterme/rxbilling/com.betterme%3Arxbilling/_latestVersion)

    implementation 'com.betterme:rxbilling:$latestVersion'
    implementation 'com.android.billingclient:billing:$billingClientVer'

# How to use

## RxBilling and RxBillingFlow
RxBilling is a simple wrapper above [Google Billing library](https://developer.android.com/google/play/billing/billing_library.html).
Using RxBilling is preferable to RxBillingFlow in most cases (if you don't care about fine-grained events, and you don't know about this issue https://github.com/googlesamples/android-play-billing/issues/83).

RxBillingFlow is a wrapper above InAppBillingService that allows to launch billing flow and handle result of onActivityResult() callback


## Connection management

### BillingConnectionManager

The entry point to Billing connection management is BillingConnectionManager, that connect and disconnect in onStart() / onStop() callbacks of your LifecycleOwner

Add next lines to your Activity,  Fragment or any other lifecycle owner

    class MainActivity : AppCompatActivity() {

        private lateinit var rxBilling: RxBilling
        private lateinit var rxBillingFlow: RxBillingFlow

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)
            rxBilling = RxBillingImpl(BillingClientFactory(applicationContext))
            rxBillingFlow = RxBillingFlow(applicationContext, BillingServiceFactory(this))
            lifecycle.addObserver(BillingConnectionManager(rxBilling))
            lifecycle.addObserver(BillingConnectionManager(rxBillingFlow))
        }
    }

### Retry / Repeat connection

The default implementation of retry transformation is RepeatConnectionTransformer().

You can provide your own transformer to BillingClientFactory and BillingServiceFactory

    val clientFactory = BillingClientFactory(this, FlowableTransformer { upstream ->
        upstream.retry(2)
    })

## Observe Billing updates

    override fun onStart() {
        super.onStart()
        disposable.add(
                rxBilling.observeUpdates()
                .subscribe({
                    //handle update here
                }, {
                    //handle error
                })
        )
    }

    override fun onStop() {
        disposable.clear()
        super.onStop()
    }

## Launch Billing flow with RxBilling

The result of this operation will be delivered to your updates observer

    private fun startFlowWithClient() {
           disposable.add(rxBilling.launchFlow(this, BillingFlowParams.newBuilder()
                   .setSkuDetails(SkuDetails) // see ## Load sku details
                   .setType(BillingClient.SkuType.SUBS)
                   .build())
                   .subscribe({
                       //flow started
                   }, {
                      //handle error
                   }))
        }

## Launch Billing flow with RxBillingFlow

The result of this operation will be delivered to onActivityResult() of your Activity or Fragment,
updates observer will not be triggered

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


## Handle Billing result with RxBillingFlow

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

## Load owned products

    private fun loadPurchases() {
         disposable.add(rxBilling.getPurchases()
                  .subscribe({
                      //handle purchases
                  }, {
                      //handle error
                  }))
    }

## Load owned purchases

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

## Load history

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

## Load sku details

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

## Consume  product

    private fun consume() {
        disposable.add(
                rxBilling.consumeProduct(
                        ConsumeParams.newBuilder()
                                .setPurchaseToken("token")
                                .build())
                        .subscribe()
        )
    }
    
## Acknowledge item
    
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
