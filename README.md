# RxBilling
Rx wrapper for Billing Library with connection managment

 [ ![Download](https://api.bintray.com/packages/betterme/rxbilling/com.betterme%3Arxbilling/images/download.svg) ](https://bintray.com/betterme/rxbilling/com.betterme%3Arxbilling/_latestVersion)

# How to use

## RxBilling and RxBillingFlow
RxBilling is a simple wrapper above [Google Billing library](https://developer.android.com/google/play/billing/billing_library.html)
Using RxBilling is preferable to RxBillingFlow in most cases (if you don't care about fine-grained events, and you don't know about this issue https://github.com/googlesamples/android-play-billing/issues/83)
RxBillingFlow is a wrapper above InAppBillingService that allows to launch billing flow and handle result of onActivityResult() callback


## Connection management

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
                   .setSku("you_id")
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

    private fun startFlowWithClient() {
           disposable.add(rxBilling.launchFlow(this, BillingFlowParams.newBuilder()
                   .setSku("you_id")
                   .setType(BillingClient.SkuType.SUBS)
                   .build())
                   .subscribe({
                       //flow started
                   }, {
                       //handle error
                   }))
        }

## Handle Billing result with RxBillingFlow

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        disposable.add(rxBillingFlow.handleActivityResult(resultCode, data)
                .subscribe({
                    //handle purchase
                }, {
                    //handle error
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

## Load owned subscriptions

    private fun loadSubscriptions() {
        disposable.add(rxBilling.getSubscriptions()
                .subscribe({
                    //handle purchases
                }, {
                   //handle error
                }))
    }

## Load products history

    private fun loadPurchasesHistory() {
        disposable.add(rxBilling.getPurchaseHistory()
                .subscribe({
                    //handle purchases
                }, {
                    //handle error
                }))
    }

## Load subscriptions history

    private fun loadPurchasesHistory() {
        disposable.add(rxBilling.getSubscriptionHistory()
                .subscribe({
                    //handle purchases
                }, {
                    //handle error
                }))
    }

## Load product sku details

    private fun loadPurchasesHistory() {
        disposable.add(rxBilling.getPurchaseSkuDetails(listOf("your_id1", "your_id2"))
                .subscribe({
                    //handle details
                }, {
                     //handle details
                }))
    }

## Load subscription sku details

    private fun loadDetails() {
        disposable.add(rxBilling.getSubscriptionSkuDetails(listOf("your_id1", "your_id2"))
                .subscribe({
                    //handle details
                }, {
                    //handle details
                }))
    }