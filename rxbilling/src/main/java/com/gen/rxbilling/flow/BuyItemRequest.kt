package com.gen.rxbilling.flow

import com.android.billingclient.api.BillingClient

data class BuyItemRequest(
        @BillingClient.SkuType val type: String,
        val id: String,
        val requestCode: Int,
        val developerPayload: String? = null
)