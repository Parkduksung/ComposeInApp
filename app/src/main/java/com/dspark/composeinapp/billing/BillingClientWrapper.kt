package com.dspark.composeinapp.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The [BillingClientWrapper] isolates the Google Play Billing's [BillingClient] methods needed
 * to have a simple implementation and emits responses to the data repository for processing.
 *
 */
class BillingClientWrapper(
    context: Context
) : PurchasesUpdatedListener, ProductDetailsResponseListener {

    // New Subscription ProductDetails
    private val _productWithProductDetails =
        MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val productWithProductDetails =
        _productWithProductDetails.asStateFlow()

    // Current Purchases
    private val _purchases =
        MutableStateFlow<List<Purchase>>(listOf())
    val purchases = _purchases.asStateFlow()

    // Tracks new purchases acknowledgement state.
    // Set to true when a purchase is acknowledged and false when not.
    private val _isNewPurchaseAcknowledged = MutableStateFlow(value = false)
    val isNewPurchaseAcknowledged = _isNewPurchaseAcknowledged.asStateFlow()

    // Initialize the BillingClient.
    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    // Establish a connection to Google Play.
    fun startBillingConnection(billingConnectionState: MutableLiveData<Boolean>) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                startBillingConnection(billingConnectionState)
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    Log.d("결과", "Billing response Ok")
                    queryPurchases()
                    queryProductDetails()
                    billingConnectionState.postValue(true)
                } else {
                    Log.d("결과", billingResult.debugMessage)
                }
            }
        })
    }

    // Query Google Play Billing for existing purchases.
    // New purchases will be provided to PurchasesUpdatedListener.onPurchasesUpdated().
    fun queryPurchases() {
        if (!billingClient.isReady) {
            Log.d("결과", "queryPurchases : Billing client is not ready")
        }
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { billingResult, purchaseList ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                if (!purchaseList.isNullOrEmpty()) {
                    _purchases.value = purchaseList
                } else {
                    _purchases.value = emptyList()
                }
            } else {
                Log.d("결과", billingResult.debugMessage)
            }
        }
    }

    // Query Google Play Billing for products available to sell and present them in the UI
    fun queryProductDetails() {
        val params = QueryProductDetailsParams.newBuilder()
        val productList = mutableListOf<QueryProductDetailsParams.Product>()
        for (product in LIST_OF_PRODUCTS) {

            productList.add(
                QueryProductDetailsParams.Product.newBuilder().setProductId(product)
                    .setProductType(BillingClient.ProductType.SUBS).build()
            )

            params.setProductList(productList).let { productDetailsParams ->
                Log.d("결과", "queryProductDetailsAsync")
                billingClient.queryProductDetailsAsync(productDetailsParams.build(), this)
            }

        }
    }

    // [ProductDetailsResponseListener] implementation
    // Listen to response back from [queryProductDetails] and emits the results
    // to [_productWithProductDetails].
    override fun onProductDetailsResponse(
        billingResult: BillingResult,
        productDetailsList: MutableList<ProductDetails>
    ) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage

        when (responseCode) {
            BillingResponseCode.OK -> {

                var newMap = emptyMap<String, ProductDetails>()
                if (productDetailsList.isNullOrEmpty()) {
                    Log.e(
                        TAG,
                        "onProductDetailsResponse: " +
                                "Found null or empty ProductDetails. " +
                                "Check to see if the Products you requested are correctly " +
                                "published in the Google Play Console."
                    )
                } else {
                    newMap = productDetailsList.associateBy { it.productId }
                }

                _productWithProductDetails.value = newMap
            }
            else -> {
                Log.d("결과", "onProductDetailsResponse : $debugMessage")
            }
        }
    }

    // Launch Purchase flow
    fun launchBillingFlow(activity: Activity, params: BillingFlowParams) {
        if (!billingClient.isReady) {
            Log.e(TAG, "launchBillingFlow: BillingClient is not ready")
        }
        billingClient.launchBillingFlow(activity, params)
    }

    // PurchasesUpdatedListener that helps handle new purchases returned from the API
    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: List<Purchase>?
    ) {
        if (billingResult.responseCode == BillingResponseCode.OK && !purchases.isNullOrEmpty()) {
            _purchases.value = purchases

            for (purchase in purchases) {
                acknowledgePurchases(purchase)
            }
        } else if (billingResult.responseCode == BillingResponseCode.USER_CANCELED) {
            Log.d("결과", "onPurchasesUpdated : User canceled the purchase")
        } else {
            //Handle any other error codes.
            Log.d("결과", billingResult.debugMessage)
        }
    }

    // Perform new subscription purchases' acknowledgement client side.
    private fun acknowledgePurchases(purchase: Purchase?) {
        purchase?.let {

            if (!it.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(it.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(
                    params
                ) { billingResult ->
                    if (billingResult.responseCode == BillingResponseCode.OK && it.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        _isNewPurchaseAcknowledged.value = true
                    }
                }
            }
        }
    }

    // End Billing connection.
    fun terminateBillingConnection() {
        Log.d(TAG, "terminateBillingConnection")
        billingClient.endConnection()
    }

    companion object {
        private const val TAG = "결과"

        // List of subscription product offerings
        private const val BASIC_SUB = "sub_3"

        private val LIST_OF_PRODUCTS = listOf(BASIC_SUB)
    }
}