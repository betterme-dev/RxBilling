package com.gen.rxbilling.exception

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult

sealed class BillingException(
    responseCode: Int,
    debugMessage: String? = null,
) : Exception("Billing error, code $responseCode\n$debugMessage") {

    companion object {
        fun fromResult(result: BillingResult): BillingException {
            return fromResponseCode(result.responseCode, result.debugMessage)
        }

        fun fromResponseCode(responseCode: Int, message: String? = null): BillingException {
            return when (responseCode) {
                BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED ->
                    FeatureNotSupportedException(responseCode, message)

                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED ->
                    ServiceDisconnectedException(responseCode, message)

                BillingClient.BillingResponseCode.USER_CANCELED ->
                    UserCanceledException(
                        responseCode,
                        message,
                    )

                BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE ->
                    ServiceUnavailableException(
                        responseCode,
                        message,
                    )

                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE ->
                    BillingUnavailableException(responseCode, message)

                BillingClient.BillingResponseCode.ITEM_UNAVAILABLE ->
                    ItemUnavailableException(
                        responseCode,
                        message,
                    )

                BillingClient.BillingResponseCode.DEVELOPER_ERROR ->
                    DeveloperErrorException(
                        responseCode,
                        message,
                    )

                BillingClient.BillingResponseCode.ERROR ->
                    FatalException(responseCode, message)

                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED ->
                    AlreadyOwnedException(
                        responseCode,
                        message,
                    )

                BillingClient.BillingResponseCode.ITEM_NOT_OWNED ->
                    NotOwnedException(
                        responseCode,
                        message,
                    )

                BillingClient.BillingResponseCode.NETWORK_ERROR ->
                    NetworkException(
                        responseCode,
                        message,
                    )

                else -> UnknownException(responseCode, message)
            }
        }
    }

    class FeatureNotSupportedException(
        responseCode: Int,
        message: String? = null,
    ) : BillingException(responseCode, message)

    class ServiceDisconnectedException(
        responseCode: Int,
        message: String? = null,
    ) : BillingException(responseCode, message)

    class UserCanceledException(
        responseCode: Int,
        message: String? = null,
    ) : BillingException(responseCode, message)

    class ServiceUnavailableException(
        responseCode: Int,
        message: String? = null,
    ) : BillingException(responseCode, message)

    class BillingUnavailableException(
        responseCode: Int,
        message: String? = null,
    ) : BillingException(responseCode, message)

    class ItemUnavailableException(
        responseCode: Int,
        message: String? = null,
    ) : BillingException(responseCode, message)

    class DeveloperErrorException(
        responseCode: Int,
        message: String? = null,
    ) : BillingException(responseCode, message)

    class FatalException(
        responseCode: Int,
        message: String? = null,
    ) : BillingException(responseCode, message)

    class AlreadyOwnedException(
        responseCode: Int,
        message: String? = null,
    ) : BillingException(responseCode, message)

    class NotOwnedException(
        responseCode: Int,
        message: String? = null,
    ) : BillingException(responseCode, message)

    class NetworkException(
        responseCode: Int,
        message: String? = null,
    ) : BillingException(responseCode, message)

    class UnknownException(
        responseCode: Int,
        message: String? = null,
    ) : BillingException(responseCode, message)
}
