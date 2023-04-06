package com.dspark.composeinapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.compose.rememberNavController
import com.dspark.composeinapp.Constants.PREPAID_BASIC_PLANS_TAG
import com.dspark.composeinapp.composable.LoadingScreen
import com.dspark.composeinapp.composable.SubscriptionNavigationComponent
import com.dspark.composeinapp.composable.UserProfile
import com.dspark.composeinapp.theme.ComposeInAppTheme
import com.dspark.composeinapp.ui.MainState
import com.dspark.composeinapp.ui.MainViewModel

class   MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            ComposeInAppTheme {
                MainNavHost(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun MainNavHost(viewModel: MainViewModel) {
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

        // Observe the ViewModel's destinationScreen object for changes in subscription status.
        val screen by viewModel.destinationScreen.observeAsState()

        // Load UI based on user's current subscription.
        when (screen) {
            // User has a Basic Prepaid subscription
            // the corresponding profile is loaded.
            MainViewModel.DestinationScreen.BASIC_PREPAID_PROFILE_SCREEN -> {
                UserProfile(
                    buttonModels =
                    emptyList(),
                    tag = PREPAID_BASIC_PLANS_TAG,
                    profileTextStringResource = null
                )
            }
            // User has a renewable basic subscription
            // the corresponding profile is loaded.
            MainViewModel.DestinationScreen.BASIC_RENEWABLE_PROFILE -> {
                UserProfile(
                    buttonModels =
                    emptyList(),
                    tag = null,
                    profileTextStringResource = R.string.basic_sub_message
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