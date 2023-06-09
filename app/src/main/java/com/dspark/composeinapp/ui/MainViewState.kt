package com.dspark.composeinapp.ui

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase

data class MainState(
    val hasRenewableBasic: Boolean? = false,
    val hasPrepaidBasic: Boolean? = false,
    val basicProductDetails: ProductDetails? = null,
    val purchases: List<Purchase>? = null,
)