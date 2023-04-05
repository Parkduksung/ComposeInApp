package com.dspark.composeinapp.composable

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dspark.composeinapp.Constants
import com.dspark.composeinapp.Constants.BASIC_BASE_PLANS_ROUTE
import com.dspark.composeinapp.Constants.MONTHLY_BASIC_PLANS_TAG
import com.dspark.composeinapp.Constants.MONTHLY_PREMIUM_PLANS_TAG
import com.dspark.composeinapp.Constants.PREMIUM_BASE_PLANS_ROUTE
import com.dspark.composeinapp.Constants.PREPAID_BASIC_PLANS_TAG
import com.dspark.composeinapp.Constants.PREPAID_PREMIUM_PLANS_TAG
import com.dspark.composeinapp.Constants.SUBSCRIPTION_ROUTE
import com.dspark.composeinapp.Constants.YEARLY_BASIC_PLANS_TAG
import com.dspark.composeinapp.Constants.YEARLY_PREMIUM_PLANS_TAG
import com.dspark.composeinapp.R
import com.dspark.composeinapp.ui.ButtonModel
import com.dspark.composeinapp.ui.MainState
import com.dspark.composeinapp.ui.MainViewModel


@Composable
fun SubscriptionNavigationComponent(
    productsForSale: MainState,
    navController: NavHostController,
    viewModel: MainViewModel
) {
    NavHost(
        navController = navController,
        startDestination = stringResource(id = R.string.subscription_composable_name)
    ) {
        composable(route = SUBSCRIPTION_ROUTE) {
            Subscription(
                navController = navController,
            )
        }
        composable(route = BASIC_BASE_PLANS_ROUTE) {
            BasicBasePlans(
                productsForSale = productsForSale,
                viewModel = viewModel
            )
        }
        composable(route = PREMIUM_BASE_PLANS_ROUTE) {
            PremiumBasePlans(
                productsForSale = productsForSale,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun Subscription(
    navController: NavHostController,
) {
    CenteredSurfaceColumn {
        val buttonModels = remember(navController) {
            listOf(
                ButtonModel(R.string.basic_sub_text) {
                    navController.navigate(route = BASIC_BASE_PLANS_ROUTE)
                },
                ButtonModel(R.string.premium_sub_text) {
                    navController.navigate(route = PREMIUM_BASE_PLANS_ROUTE)
                }
            )
        }
        ButtonGroup(buttonModels = buttonModels)
    }
}

@Composable
private fun BasicBasePlans(
    productsForSale: MainState,
    viewModel: MainViewModel,
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    CenteredSurfaceColumn {
        val buttonModels = remember(productsForSale, viewModel, activity) {
            listOf(
                ButtonModel(R.string.monthly_basic_sub_text) {
                    productsForSale.basicProductDetails?.let {
                        viewModel.buy(
                            productDetails = it,
                            currentPurchases = null,
                            tag = MONTHLY_BASIC_PLANS_TAG,
                            activity = activity
                        )
                    }
                },
            )
        }
        ButtonGroup(buttonModels = buttonModels)
    }
}

@Composable
private fun PremiumBasePlans(
    productsForSale: MainState,
    viewModel: MainViewModel,
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    CenteredSurfaceColumn {
        val buttonModels = remember(productsForSale, viewModel, activity) {
            listOf(
                ButtonModel(R.string.monthly_premium_sub_text) {
                    productsForSale.premiumProductDetails?.let {
                        viewModel.buy(
                            productDetails = it,
                            currentPurchases = null,
                            tag = MONTHLY_PREMIUM_PLANS_TAG,
                            activity = activity
                        )
                    }
                },
                ButtonModel(R.string.yearly_premium_sub_text) {
                    productsForSale.premiumProductDetails?.let {
                        viewModel.buy(
                            productDetails = it,
                            currentPurchases = null,
                            tag = YEARLY_PREMIUM_PLANS_TAG,
                            activity = activity
                        )
                    }
                },
                ButtonModel(R.string.prepaid_premium_sub_text) {
                    productsForSale.premiumProductDetails?.let {
                        viewModel.buy(
                            productDetails = it,
                            currentPurchases = null,
                            tag = PREPAID_PREMIUM_PLANS_TAG,
                            activity = activity
                        )
                    }
                }
            )
        }
        ButtonGroup(buttonModels = buttonModels)
    }
}

@Composable
fun UserProfile(
    buttonModels: List<ButtonModel>,
    tag: String?,
    profileTextStringResource: Int?
) {
    CenteredSurfaceColumn {
        if (tag.isNullOrEmpty()) {
            CenteredSurfaceColumn {
                profileTextStringResource?.let { stringResource(id = it) }
                    ?.let { ProfileText(text = it) }
                ButtonGroup(
                    buttonModels = buttonModels
                )
            }
        } else {
            CenteredSurfaceColumn {
                when (tag) {
                    PREPAID_BASIC_PLANS_TAG -> ProfileText(
                        text = stringResource(id = R.string.basic_prepaid_sub_message)
                    )
                    PREPAID_PREMIUM_PLANS_TAG -> ProfileText(
                        text = stringResource(id = R.string.premium_prepaid_sub_message)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                ButtonGroup(buttonModels = buttonModels)
            }
        }
    }
}

@Composable
private fun ButtonGroup(
    buttonModels: List<ButtonModel>,
) {
    CenteredSurfaceColumn {
        for (buttonModel in buttonModels) {
            Button(
                modifier = Modifier.size(
                    width = 240.dp,
                    height = 60.dp
                ),
                onClick = buttonModel.onClick
            ) {
                Text(text = stringResource(buttonModel.stringResource))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun LoadingScreen() {
    CenteredSurfaceColumn {
        Text(
            text = stringResource(id = R.string.loading_message),
            style = MaterialTheme.typography.h1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProfileText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.h3,
        textAlign = TextAlign.Center
    )
}

@Composable
fun CenteredSurfaceColumn(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            content()
        }
    }
}

/**
 * Find the closest Activity in a given Context.
 */
internal fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException(getString(R.string.context_finder_error))
}