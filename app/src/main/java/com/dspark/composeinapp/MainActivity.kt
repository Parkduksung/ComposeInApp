package com.dspark.composeinapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.android.billingclient.api.*
import com.dspark.composeinapp.Constants.MONTHLY_BASIC_PLANS_TAG
import com.dspark.composeinapp.Constants.MONTHLY_PREMIUM_PLANS_TAG
import com.dspark.composeinapp.Constants.PREPAID_BASIC_PLANS_TAG
import com.dspark.composeinapp.Constants.PREPAID_PREMIUM_PLANS_TAG
import com.dspark.composeinapp.Constants.YEARLY_BASIC_PLANS_TAG
import com.dspark.composeinapp.Constants.YEARLY_PREMIUM_PLANS_TAG
import com.dspark.composeinapp.composable.LoadingScreen
import com.dspark.composeinapp.composable.SubscriptionNavigationComponent
import com.dspark.composeinapp.composable.UserProfile
import com.dspark.composeinapp.theme.ComposeInAppTheme
import com.dspark.composeinapp.ui.ButtonModel
import com.dspark.composeinapp.ui.MainState
import com.dspark.composeinapp.ui.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            ComposeInAppTheme {
                MainNavHost(viewModel = viewModel, activity = this)
            }
        }
    }
}

@Composable
private fun MainNavHost(viewModel: MainViewModel, activity: MainActivity) {
    // State variable passed into Billing connection call and set to true when
    // connections is established.
    val isBillingConnected by viewModel.billingConnectionState.observeAsState()

    if (isBillingConnected == false) {
        // When false connection to the billing library is not established yet,
        // so a loading screen is rendered.
        LoadingScreen()
    } else {
        // When true connection to the billing library is established,
        // so the subscription composables are rendered.
        val navController = rememberNavController()

        // Collect available products to sale Flows from MainViewModel.
        val productsForSale by viewModel.productsForSaleFlows.collectAsState(
            initial = MainState()
        )

        // Collect current purchases Flows from MainViewModel.
        val currentPurchases by viewModel.currentPurchasesFlow.collectAsState(
            initial = listOf()
        )

        // Observe the ViewModel's destinationScreen object for changes in subscription status.
        val screen by viewModel.destinationScreen.observeAsState()

        // Load UI based on user's current subscription.
        when (screen) {
            // User has a Basic Prepaid subscription
            // the corresponding profile is loaded.
            MainViewModel.DestinationScreen.BASIC_PREPAID_PROFILE_SCREEN -> {
                UserProfile(
                    buttonModels =
                    listOf(
                        ButtonModel(R.string.topup_message) {
                            productsForSale.basicProductDetails?.let {
                                viewModel.buy(
                                    productDetails = it,
                                    currentPurchases = null,
                                    tag = PREPAID_BASIC_PLANS_TAG,
                                    activity = activity
                                )
                            }
                        },
                        ButtonModel(R.string.convert_to_basic_monthly_message) {
                            productsForSale.basicProductDetails?.let {
                                viewModel.buy(
                                    productDetails = it,
                                    currentPurchases = currentPurchases,
                                    tag = MONTHLY_BASIC_PLANS_TAG,
                                    activity = activity
                                )
                            }
                        },
                        ButtonModel(R.string.convert_to_basic_yearly_message) {
                            productsForSale.basicProductDetails?.let {
                                viewModel.buy(
                                    productDetails = it,
                                    currentPurchases = currentPurchases,
                                    tag = YEARLY_BASIC_PLANS_TAG,
                                    activity = activity
                                )
                            }
                        },
                    ),
                    tag = PREPAID_BASIC_PLANS_TAG,
                    profileTextStringResource = null
                )
            }
            // User has a renewable basic subscription
            // the corresponding profile is loaded.
            MainViewModel.DestinationScreen.BASIC_RENEWABLE_PROFILE -> {
                UserProfile(
                    buttonModels =
                    listOf(
                        ButtonModel(R.string.monthly_premium_upgrade_message) {
                            productsForSale.premiumProductDetails?.let {
                                viewModel.buy(
                                    productDetails = it,
                                    currentPurchases = currentPurchases,
                                    tag = MONTHLY_PREMIUM_PLANS_TAG,
                                    activity = activity
                                )
                            }
                        },
                        ButtonModel(R.string.yearly_premium_upgrade_message) {
                            productsForSale.premiumProductDetails?.let {
                                viewModel.buy(
                                    productDetails = it,
                                    currentPurchases = currentPurchases,
                                    tag = YEARLY_PREMIUM_PLANS_TAG,
                                    activity = activity
                                )
                            }
                        },
                        ButtonModel(R.string.prepaid_premium_upgrade_message) {
                            productsForSale.premiumProductDetails?.let {
                                viewModel.buy(
                                    productDetails = it,
                                    currentPurchases = currentPurchases,
                                    tag = PREPAID_PREMIUM_PLANS_TAG,
                                    activity = activity
                                )
                            }
                        }
                    ),
                    tag = null,
                    profileTextStringResource = R.string.basic_sub_message
                )
            }
            // User has a prepaid Premium subscription
            // the corresponding profile is loaded.
            MainViewModel.DestinationScreen.PREMIUM_PREPAID_PROFILE_SCREEN -> {
                UserProfile(
                    buttonModels =
                    listOf(
                        ButtonModel(R.string.topup_message) {
                            productsForSale.premiumProductDetails?.let {
                                viewModel.buy(
                                    productDetails = it,
                                    currentPurchases = null,
                                    tag = PREPAID_PREMIUM_PLANS_TAG,
                                    activity = activity
                                )
                            }
                        },
                        ButtonModel(R.string.convert_to_premium_monthly_message) {
                            productsForSale.premiumProductDetails?.let {
                                viewModel.buy(
                                    productDetails = it,
                                    currentPurchases = currentPurchases,
                                    tag = MONTHLY_PREMIUM_PLANS_TAG,
                                    activity = activity
                                )
                            }
                        },
                        ButtonModel(R.string.convert_to_premium_yearly_message) {
                            productsForSale.premiumProductDetails?.let {
                                viewModel.buy(
                                    productDetails = it,
                                    currentPurchases = currentPurchases,
                                    tag = YEARLY_PREMIUM_PLANS_TAG,
                                    activity = activity
                                )
                            }
                        },
                    ),
                    tag = PREPAID_PREMIUM_PLANS_TAG,
                    profileTextStringResource = null
                )
            }
            // User has a renewable Premium subscription
            // the corresponding profile is loaded.
            MainViewModel.DestinationScreen.PREMIUM_RENEWABLE_PROFILE -> {
                UserProfile(
                    listOf(
                        ButtonModel(R.string.monthly_basic_downgrade_message) {
                            productsForSale.basicProductDetails?.let {
                                viewModel.buy(
                                    productDetails = it,
                                    currentPurchases = currentPurchases,
                                    tag = MONTHLY_BASIC_PLANS_TAG,
                                    activity = activity
                                )
                            }
                        },
                        ButtonModel(R.string.yearly_basic_downgrade_message) {
                            productsForSale.basicProductDetails?.let {
                                viewModel.buy(
                                    productDetails = it,
                                    currentPurchases = currentPurchases,
                                    tag = YEARLY_BASIC_PLANS_TAG,
                                    activity = activity
                                )
                            }
                        },
                        ButtonModel(R.string.prepaid_basic_downgrade_message) {
                            productsForSale.basicProductDetails?.let {
                                viewModel.buy(
                                    productDetails = it,
                                    currentPurchases = currentPurchases,
                                    tag = PREPAID_BASIC_PLANS_TAG,
                                    activity = activity
                                )
                            }
                        }
                    ),
                    tag = null,
                    profileTextStringResource = R.string.premium_sub_message
                )
            }
            // User has no current subscription - the subscription composable
            // is loaded.
            MainViewModel.DestinationScreen.SUBSCRIPTIONS_OPTIONS_SCREEN -> {
                SubscriptionNavigationComponent(
                    productsForSale = productsForSale,
                    navController = navController,
                    viewModel = viewModel
                )
            }
            null -> {

            }
        }
    }
}