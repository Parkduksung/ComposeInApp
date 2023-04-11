package com.dspark.composeinapp.repository

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.dspark.composeinapp.billing.BillingClientWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class SubscriptionDataRepository(billingClientWrapper: BillingClientWrapper) {

    val hasRenewableBasic: Flow<Boolean> = billingClientWrapper.purchases.map { purchases ->
        purchases.any { purchase ->
            purchase.products.contains(BASIC_SUB) && purchase.isAutoRenewing
        }
    }

    val hasPrepaidBasic: Flow<Boolean> = billingClientWrapper.purchases.map { purchases ->
        purchases.any { purchase ->
            purchase.products.contains(BASIC_SUB) && !purchase.isAutoRenewing
        }
    }

    // ProductDetails for the basic subscription.
    val basicProductDetails: Flow<ProductDetails> =
        billingClientWrapper.productWithProductDetails.filter {
            it.containsKey(
                BASIC_SUB
            )
        }.map { it[BASIC_SUB]!! }


//    // List of current purchases returned by the Google PLay Billing client library.
//    val purchases: Flow<List<Purchase>> = billingClientWrapper.purchases
//
//    // Set to true when a purchase is acknowledged.
//    val isNewPurchaseAcknowledged: Flow<Boolean> = billingClientWrapper.isNewPurchaseAcknowledged
//

    companion object {
        // List of subscription product offerings
        private const val BASIC_SUB = "sub_4"
    }

}