package com.gen.rxbilling3.exception

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult

sealed class BillingException(result: BillingResult) :
        Exception("Billing error, code ${result.responseCode}\n${result.debugMessage}") {

    companion object {
        fun fromResult(result: BillingResult): BillingException {
            return when (result.responseCode) {
                BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> FeatureNotSupportedException(result)
                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> ServiceDisconnectedException(result)
                BillingClient.BillingResponseCode.USER_CANCELED -> UserCanceledException(result)
                BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> ServiceUnavailableException(result)
                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> BillingUnavailableException(result)
                BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> ItemUnavailableException(result)
                BillingClient.BillingResponseCode.DEVELOPER_ERROR -> DeveloperErrorException(result)
                BillingClient.BillingResponseCode.ERROR -> FatalException(result)
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> AlreadyOwnedException(result)
                BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> NotOwnedException(result)
                else -> UnknownException(result)
            }
        }
    }

    class FeatureNotSupportedException(result: BillingResult) : BillingException(result)
    class ServiceDisconnectedException(result: BillingResult) : BillingException(result)
    class UserCanceledException(result: BillingResult) : BillingException(result)
    class ServiceUnavailableException(result: BillingResult) : BillingException(result)
    class BillingUnavailableException(result: BillingResult) : BillingException(result)
    class ItemUnavailableException(result: BillingResult) : BillingException(result)
    class DeveloperErrorException(result: BillingResult) : BillingException(result)
    class FatalException(result: BillingResult) : BillingException(result)
    class AlreadyOwnedException(result: BillingResult) : BillingException(result)
    class NotOwnedException(result: BillingResult) : BillingException(result)
    class UnknownException(result: BillingResult) : BillingException((result))
}
