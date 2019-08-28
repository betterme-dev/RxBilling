package com.gen.rxbilling.flow

data class ReplaceItemRequest(
        val oldId: String,
        val newId: String,
        val requestCode: Int,
        val developerPayload: String? = null
)