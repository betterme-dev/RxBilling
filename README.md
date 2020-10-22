# RxBilling
Rx wrapper for Billing Library with connection management

# Download
 
 [![](https://jitpack.io/v/betterme-dev/RxBilling.svg)](https://jitpack.io/#betterme-dev/RxBilling)

    implementation 'com.betterme:rxbilling:$latestVersion'
    implementation 'com.android.billingclient:billing:$billingClientVer'

# How to use

## RxBilling
RxBilling is a simple wrapper above [Google Billing library](https://developer.android.com/google/play/billing/billing_library.html).


## Connection management

### BillingConnectionManager

The entry point to Billing connection management is BillingConnectionManager, that connect and disconnect in onStart() / onStop() callbacks of your LifecycleOwner

Add next lines to your Activity,  Fragment or any other lifecycle owner

    class MainActivity : AppCompatActivity() {

        private lateinit var rxBilling: RxBilling

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)
            rxBilling = RxBillingImpl(BillingClientFactory(applicationContext))
            lifecycle.addObserver(BillingConnectionManager(rxBilling))
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

## Launch Billing flow

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

## Load owned products

    private fun loadPurchases() {
         disposable.add(rxBilling.getPurchases(BillingClient.SkuType.INAPP)
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
