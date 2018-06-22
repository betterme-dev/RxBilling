package com.gen.rxbilling.client

import com.android.billingclient.api.Purchase


sealed class PurchasesUpdate(open val code: Int,
                             open val purchases: List<Purchase>) {

    data class Success(override val code: Int,
                       override val purchases: List<Purchase>) : PurchasesUpdate(code, purchases)

    data class Canceled(override val code: Int,
                        override val purchases: List<Purchase>) : PurchasesUpdate(code, purchases)

    data class Failed(override val code: Int,
                      override val purchases: List<Purchase>) : PurchasesUpdate(code, purchases)
}