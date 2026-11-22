package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.AppDatabase
import com.example.data.repository.ScreenshotStorage
import com.example.data.repository.SessionManager
import com.example.data.repository.StoreRepository
import com.example.ui.StoreViewModel
import com.example.ui.UiMessage
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.ContactUsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.PackagesScreen
import com.example.ui.screens.PaymentInstructionsScreen
import com.example.ui.theme.FFDiamondStoreTheme
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.StoreBackground
import com.example.ui.theme.StoreSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

object StoreRoutes {
    const val HOME = "home"
    const val PACKAGES = "packages"
    const val ORDERS = "orders"
    const val DASHBOARD = "dashboard"
    const val CHECKOUT = "checkout"
    const val PAYMENT_INSTRUCTIONS = "payment_instructions"
    const val CONTACT_US = "contact_us"
    const val ADMIN = "admin"
}

private data class TopLevelTab(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val topLevelTabs = listOf(
    TopLevelTab(StoreRoutes.HOME, R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home),
    TopLevelTab(StoreRoutes.PACKAGES, R.string.nav_packages, Icons.Filled.Diamond, Icons.Outlined.Diamond),
    TopLevelTab(StoreRoutes.ORDERS, R.string.nav_orders, Icons.AutoMirrored.Filled.ReceiptLong, Icons.AutoMirrored.Outlined.ReceiptLong),
    TopLevelTab(StoreRoutes.DASHBOARD, R.string.nav_dashboard, Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FFDiamondStoreTheme {
                FFDiamondStoreApp()
            }
        }
    }
}

@Composable
fun FFDiamondStoreApp() {
    val context = LocalContext.current.applicationContext
    val viewModel: StoreViewModel = viewModel(
        factory = remember {
            val db = AppDatabase.getInstance(context)
            val session = SessionManager(context)
            val repo = StoreRepository(db.storeDao(), session)
            val storage = ScreenshotStorage(context)
            StoreViewModel.Factory(repo, storage)
        }
    )

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { msg ->
            val text = when (msg) {
                is UiMessage.Success -> msg.text
                is UiMessage.Error -> msg.text
            }
            snackbarHostState.showSnackbar(text)
        }
    }

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val adminExists by viewModel.adminExists.collectAsStateWithLifecycle()
    val activePackages by viewModel.activePackages.collectAsStateWithLifecycle()
    val allPackages by viewModel.allPackages.collectAsStateWithLifecycle()
    val paymentConfigs by viewModel.paymentConfigs.collectAsStateWithLifecycle()
    val userOrders by viewModel.userOrders.collectAsStateWithLifecycle()
    val userRewards by viewModel.userRewards.collectAsStateWithLifecycle()
    val userNotifications by viewModel.userNotifications.collectAsStateWithLifecycle()
    val selectedPackage by viewModel.selectedPackage.collectAsStateWithLifecycle()
    val quickUid by viewModel.quickPlayerUid.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()

    val adminMetrics by viewModel.adminMetrics.collectAsStateWithLifecycle()
    val adminAllOrders by viewModel.adminAllOrders.collectAsStateWithLifecycle()
    val adminUsers by viewModel.adminUsers.collectAsStateWithLifecycle()
    val adminUserSearch by viewModel.userSearchQuery.collectAsStateWithLifecycle()
    val adminAllRewards by viewModel.adminAllRewards.collectAsStateWithLifecycle()
    val adminSupportMessages by viewModel.adminSupportMessages.collectAsStateWithLifecycle()

    val isTopLevelRoute = topLevelTabs.any { tab ->
        currentDestination?.hierarchy?.any { it.route == tab.route } == true
    }

    fun navigateToTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 840.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = StoreBackground,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = StoreSurface,
                        contentColor = TextPrimary,
                        actionColor = NeonCyan
                    )
                }
            },
            bottomBar = {
                if (isTopLevelRoute && !useNavigationRail) {
                    NavigationBar(containerColor = StoreSurface) {
                        topLevelTabs.forEach { tab ->
                            val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTopLevel(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = stringResource(tab.labelRes)
                                    )
                                },
                                label = { Text(stringResource(tab.labelRes)) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NeonCyan,
                                    selectedTextColor = NeonCyan,
                                    indicatorColor = NeonBlue.copy(alpha = 0.2f),
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isTopLevelRoute && useNavigationRail) {
                    NavigationRail(containerColor = StoreSurface) {
                        topLevelTabs.forEach { tab ->
                            val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTopLevel(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = stringResource(tab.labelRes)
                                    )
                                },
                                label = { Text(stringResource(tab.labelRes)) }
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = StoreRoutes.HOME,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(StoreRoutes.HOME) {
                        HomeScreen(
                            currentUser = currentUser,
                            packages = activePackages,
                            quickUid = quickUid,
                            onQuickUidChange = viewModel::setQuickPlayerUid,
                            onBrowsePackages = { navigateToTopLevel(StoreRoutes.PACKAGES) },
                            onSelectPackage = { pkg ->
                                viewModel.selectPackage(pkg)
                                navController.navigate(StoreRoutes.CHECKOUT)
                            },
                            onOpenPaymentInstructions = { navController.navigate(StoreRoutes.PAYMENT_INSTRUCTIONS) },
                            onOpenContactUs = { navController.navigate(StoreRoutes.CONTACT_US) },
                            onOpenAccount = { navigateToTopLevel(StoreRoutes.DASHBOARD) },
                            onOpenAdmin = { navController.navigate(StoreRoutes.ADMIN) }
                        )
                    }

                    composable(StoreRoutes.PACKAGES) {
                        PackagesScreen(
                            packages = activePackages,
                            onSelectPackage = { pkg ->
                                viewModel.selectPackage(pkg)
                                navController.navigate(StoreRoutes.CHECKOUT)
                            }
                        )
                    }

                    composable(StoreRoutes.ORDERS) {
                        OrdersScreen(
                            currentUser = currentUser,
                            orders = userOrders,
                            onBrowsePackages = { navigateToTopLevel(StoreRoutes.PACKAGES) },
                            onOpenLogin = { navigateToTopLevel(StoreRoutes.DASHBOARD) }
                        )
                    }

                    composable(StoreRoutes.DASHBOARD) {
                        DashboardScreen(
                            currentUser = currentUser,
                            adminExists = adminExists,
                            orders = userOrders,
                            rewards = userRewards,
                            notifications = userNotifications,
                            isBusy = isBusy,
                            onLogin = viewModel::login,
                            onRegister = viewModel::register,
                            onClaimFirstAdmin = viewModel::claimFirstAdmin,
                            onLoginAdmin = viewModel::loginAdmin,
                            onLogout = viewModel::logout,
                            onUpdatePlayerUid = viewModel::updatePlayerUid,
                            onMarkNotificationRead = viewModel::markNotificationRead,
                            onOpenPaymentInstructions = { navController.navigate(StoreRoutes.PAYMENT_INSTRUCTIONS) },
                            onOpenContactUs = { navController.navigate(StoreRoutes.CONTACT_US) },
                            onOpenAdminPanel = { navController.navigate(StoreRoutes.ADMIN) }
                        )
                    }

                    composable(StoreRoutes.CHECKOUT) {
                        CheckoutScreen(
                            selectedPackage = selectedPackage,
                            currentUser = currentUser,
                            quickUid = quickUid,
                            paymentConfigs = paymentConfigs,
                            isBusy = isBusy,
                            onBack = { navController.popBackStack() },
                            onRequireLogin = {
                                navController.popBackStack()
                                navigateToTopLevel(StoreRoutes.DASHBOARD)
                            },
                            onPlaceOrder = { pkgId, uid, method, tid, uri ->
                                viewModel.placeOrder(pkgId, uid, method, tid, uri) {
                                    navController.popBackStack()
                                    navigateToTopLevel(StoreRoutes.ORDERS)
                                }
                            }
                        )
                    }

                    composable(StoreRoutes.PAYMENT_INSTRUCTIONS) {
                        PaymentInstructionsScreen(
                            paymentConfigs = paymentConfigs,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(StoreRoutes.CONTACT_US) {
                        ContactUsScreen(
                            currentUser = currentUser,
                            isBusy = isBusy,
                            onBack = { navController.popBackStack() },
                            onSubmitMessage = viewModel::submitSupportMessage
                        )
                    }

                    composable(StoreRoutes.ADMIN) {
                        AdminDashboardScreen(
                            currentUser = currentUser,
                            metrics = adminMetrics,
                            orders = adminAllOrders,
                            users = adminUsers,
                            userSearchQuery = adminUserSearch,
                            packages = allPackages,
                            paymentConfigs = paymentConfigs,
                            rewards = adminAllRewards,
                            supportMessages = adminSupportMessages,
                            onBack = { navController.popBackStack() },
                            onUpdateUserSearch = viewModel::updateAdminUserSearch,
                            onUpdateOrderStatus = viewModel::updateOrderStatus,
                            onSavePackage = viewModel::savePackage,
                            onDeletePackage = viewModel::deletePackage,
                            onUpdatePaymentConfig = viewModel::updatePaymentConfig,
                            onSendFreeDiamonds = viewModel::sendFreeDiamonds,
                            onMarkRewardFulfilled = viewModel::markRewardFulfilled,
                            onSendNotification = viewModel::sendNotification,
                            onResolveSupportMessage = viewModel::resolveSupportMessage
                        )
                    }
                }
            }
        }
    }
}
