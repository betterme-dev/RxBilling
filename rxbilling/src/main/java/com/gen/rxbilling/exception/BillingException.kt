package com.gen.rxbilling.exception

import com.android.billingclient.api.BillingClient

sealed class BillingException(open val code: Int) :
        Exception("Billing error, code $code") {

    companion object {
        fun fromCode(code: Int): BillingException {
            return when (code) {
                BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED -> FeatureNotSupportedException()
                BillingClient.BillingResponse.SERVICE_DISCONNECTED -> ServiceDisconnectedException()
                BillingClient.BillingResponse.USER_CANCELED -> UserCanceledException()
                BillingClient.BillingResponse.SERVICE_UNAVAILABLE -> ServiceUnavailableException()
                BillingClient.BillingResponse.BILLING_UNAVAILABLE -> BillingUnavailableException()
                BillingClient.BillingResponse.ITEM_UNAVAILABLE -> ItemUnavailableException()
                BillingClient.BillingResponse.DEVELOPER_ERROR -> DeveloperErrorException()
                BillingClient.BillingResponse.ERROR -> FatalException()
                BillingClient.BillingResponse.ITEM_ALREADY_OWNED -> AlreadyOwnedException()
                BillingClient.BillingResponse.ITEM_NOT_OWNED -> NotOwnedException()
                else -> UnknownException(code)
            }
        }
    }


    class FeatureNotSupportedException : BillingException(BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED)
    class ServiceDisconnectedException : BillingException(BillingClient.BillingResponse.SERVICE_DISCONNECTED)
    class UserCanceledException : BillingException(BillingClient.BillingResponse.USER_CANCELED)
    class ServiceUnavailableException : BillingException(BillingClient.BillingResponse.SERVICE_UNAVAILABLE)
    class BillingUnavailableException : BillingException(BillingClient.BillingResponse.BILLING_UNAVAILABLE)
    class ItemUnavailableException : BillingException(BillingClient.BillingResponse.ITEM_UNAVAILABLE)
    class DeveloperErrorException : BillingException(BillingClient.BillingResponse.DEVELOPER_ERROR)
    class FatalException : BillingException(BillingClient.BillingResponse.ERROR)
    class AlreadyOwnedException : BillingException(BillingClient.BillingResponse.ITEM_ALREADY_OWNED)
    class NotOwnedException : BillingException(BillingClient.BillingResponse.ITEM_NOT_OWNED)
    class UnknownException(code: Int) : BillingException(code)
}
