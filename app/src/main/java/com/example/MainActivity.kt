package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.components.PosBottomBar
import com.example.ui.components.TopAlertHost
import com.example.ui.screens.*
import com.example.ui.theme.MikroTikPosTheme
import com.example.ui.viewmodel.PosViewModel

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val PERMISSIONS = "permissions"
    const val AUTH = "auth"
    const val HOME = "home"
    const val NETWORKS = "networks"
    const val SEARCH_NETWORK = "search_network"
    const val PACKAGES = "packages"
    const val WALLET = "wallet"
    const val WALLET_TOPUP_SELECTION = "wallet_topup_selection"
    const val WALLET_TOPUP_FORM = "wallet_topup_form/{walletId}"
    const val PRINTER_SETTINGS = "printer_settings"
    const val SALES_HISTORY = "sales_history"
    const val PROFILE = "profile"
    const val ACCOUNT_DETAILS = "account_details"
    const val NOTIFICATIONS = "notifications"
}



class MainActivity : ComponentActivity() {

    private val viewModel: PosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

            MikroTikPosTheme(themeMode = themeMode) {
                val navController = rememberNavController()
                val context = LocalContext.current
                val snackbarHostState = remember { SnackbarHostState() }

                // Automatically request POST_NOTIFICATIONS on Android 13+ (API 33+)
                val notifPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { /* Result handled by OS */ }

                LaunchedEffect(Unit) {
                    com.example.notification.AppNotificationManager.createNotificationChannels(context)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        if (androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.POST_NOTIFICATIONS
                            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) {
                            notifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                val user by viewModel.currentUser.collectAsStateWithLifecycle()
                val joinedNetworks by viewModel.sortedJoinedNetworks.collectAsStateWithLifecycle()
                val allNetworks by viewModel.allNetworks.collectAsStateWithLifecycle()
                val pinnedNetworkIds by viewModel.pinnedNetworkIds.collectAsStateWithLifecycle()
                val orderHistory by viewModel.orderHistory.collectAsStateWithLifecycle()
                val activePrinter by viewModel.activePrinter.collectAsStateWithLifecycle()

                val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
                val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
                val searchedNetwork by viewModel.searchedNetwork.collectAsStateWithLifecycle()

                val selectedNetwork by viewModel.selectedNetwork.collectAsStateWithLifecycle()
                val availablePackages by viewModel.availablePackages.collectAsStateWithLifecycle()
                val selectedQuantities by viewModel.selectedQuantities.collectAsStateWithLifecycle()
                val customerPhone by viewModel.customerPhone.collectAsStateWithLifecycle()
                val isPurchasing by viewModel.isPurchasing.collectAsStateWithLifecycle()

                val latestOrder by viewModel.latestOrder.collectAsStateWithLifecycle()
                val showReceiptModal by viewModel.showReceiptModal.collectAsStateWithLifecycle()
                val toastMsg by viewModel.toastMessage.collectAsStateWithLifecycle()
                val isPrinting by viewModel.isPrinting.collectAsStateWithLifecycle()

                val walletBalance by viewModel.walletBalance.collectAsStateWithLifecycle()
                val walletTransactions by viewModel.walletTransactions.collectAsStateWithLifecycle()
                val notifications by viewModel.notifications.collectAsStateWithLifecycle()
                val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()
                val showUnjoinedTopUpDialog by viewModel.showUnjoinedTopUpDialog.collectAsStateWithLifecycle()

                val showCeilingExhaustedDialog by viewModel.showCeilingExhaustedDialog.collectAsStateWithLifecycle()
                val showInsufficientWalletDialog by viewModel.showInsufficientWalletDialog.collectAsStateWithLifecycle()
                val isTopUpProcessing by viewModel.isTopUpProcessing.collectAsStateWithLifecycle()
                val currentAlert by viewModel.currentAlert.collectAsStateWithLifecycle()

                val startDestination = Routes.SPLASH

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val showBottomBar = remember(user, currentRoute) {
                    user?.isLoggedIn == true &&
                            currentRoute != Routes.AUTH &&
                            currentRoute != Routes.SPLASH &&
                            currentRoute != Routes.ONBOARDING &&
                            currentRoute != Routes.PERMISSIONS
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        bottomBar = {
                            if (showBottomBar) {
                                PosBottomBar(
                                    currentRoute = currentRoute,
                                    joinedNetworksCount = joinedNetworks.size,
                                    ordersCount = orderHistory.size,
                                    onNavigate = { targetRoute ->
                                        navController.navigate(targetRoute) {
                                            popUpTo(Routes.HOME) {
                                                inclusive = false
                                            }
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.background,
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = innerPadding.calculateBottomPadding())
                        ) {
                        NavHost(
                            navController = navController,
                            startDestination = startDestination
                        ) {
                            // SPLASH SCREEN
                            composable(Routes.SPLASH) {
                                SplashScreen(
                                    isFirstRun = viewModel.isFirstRun(),
                                    isLoggedIn = user?.isLoggedIn == true,
                                    onSplashFinished = { nextRoute ->
                                        navController.navigate(nextRoute) {
                                            popUpTo(Routes.SPLASH) { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // ONBOARDING SCREEN
                            composable(Routes.ONBOARDING) {
                                OnboardingScreen(
                                    onSkip = {
                                        navController.navigate(Routes.PERMISSIONS) {
                                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                                        }
                                    },
                                    onFinish = {
                                        navController.navigate(Routes.PERMISSIONS) {
                                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // PERMISSIONS SCREEN
                            composable(Routes.PERMISSIONS) {
                                PermissionsScreen(
                                    onPermissionsCompleted = {
                                        viewModel.setFirstRunCompleted()
                                        val targetRoute = if (user?.isLoggedIn == true) Routes.HOME else Routes.AUTH
                                        navController.navigate(targetRoute) {
                                            popUpTo(Routes.PERMISSIONS) { inclusive = true }
                                        }
                                    }
                                )
                            }
                            // AUTH
                            composable(Routes.AUTH) {
                                AuthScreen(
                                    themeMode = themeMode,
                                    savedPhone = viewModel.getSavedPhone(),
                                    savedPassword = viewModel.getSavedPassword(),
                                    initialRememberMe = viewModel.isRememberMeEnabled(),
                                    isPhonePendingOtp = { viewModel.isPhonePendingOtp(it) },
                                    getPendingOtpData = { viewModel.getPendingOtpData() },
                                    onToggleThemeMode = { viewModel.toggleThemeMode() },
                                    onRegister = { ownerName, storeName, phone, location, password, remember, callback ->
                                        viewModel.registerAccount(ownerName, storeName, phone, location, password, remember) { success, msg, testOtp ->
                                            callback(success, msg, testOtp)
                                        }
                                    },
                                    onVerifyOtp = { phone, storeName, location, password, otp, callback ->
                                        viewModel.verifyAccountOtp(phone, storeName, location, password, otp) { success, msg ->
                                            callback(success, msg)
                                            if (success) {
                                                navController.navigate(Routes.HOME) {
                                                    popUpTo(Routes.AUTH) { inclusive = true }
                                                }
                                            }
                                        }
                                    },
                                    onLogin = { phone, password, remember, callback ->
                                        viewModel.login(phone, password, remember) { success, msg ->
                                            callback(success, msg)
                                            if (success) {
                                                navController.navigate(Routes.HOME) {
                                                    popUpTo(Routes.AUTH) { inclusive = true }
                                                }
                                            }
                                        }
                                    },
                                    onRequestForgotPasswordOtp = { phone, callback ->
                                        viewModel.requestForgotPasswordOtp(phone, callback)
                                    },
                                    onResetPassword = { phone, otp, newPass, callback ->
                                        viewModel.resetPasswordWithOtp(phone, otp, newPass, callback)
                                    }
                                )
                            }

                            // HOME
                            composable(Routes.HOME) {
                                HomeScreen(
                                    user = user,
                                    joinedNetworks = joinedNetworks,
                                    printer = activePrinter,
                                    walletBalance = walletBalance,
                                    unreadNotificationsCount = unreadNotificationsCount,
                                    themeMode = themeMode,
                                    onToggleThemeMode = { viewModel.toggleThemeMode() },
                                    onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                                    onOpenSearch = { navController.navigate(Routes.SEARCH_NETWORK) },
                                    onOpenPrinterSettings = { navController.navigate(Routes.PRINTER_SETTINGS) },
                                    onOpenHistory = { navController.navigate(Routes.SALES_HISTORY) },
                                    onOpenWallet = { navController.navigate(Routes.WALLET) },
                                    onSelectNetworkStore = { net ->
                                        viewModel.selectNetworkForStore(net)
                                        navController.navigate(Routes.PACKAGES)
                                    },
                                    onTogglePinNetwork = { id -> viewModel.togglePinNetwork(id) },
                                    onMoveNetworkOrder = { id, moveUp -> viewModel.moveNetworkOrder(id, moveUp) },
                                    onLogout = {
                                        viewModel.logout()
                                        navController.navigate(Routes.AUTH) {
                                            popUpTo(Routes.HOME) { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // NOTIFICATIONS
                            composable(Routes.NOTIFICATIONS) {
                                NotificationsScreen(
                                    notifications = notifications,
                                    unreadCount = unreadNotificationsCount,
                                    onMarkRead = { id -> viewModel.markNotificationRead(id) },
                                    onMarkAllRead = { viewModel.markAllNotificationsRead() },
                                    onDeleteNotification = { id -> viewModel.deleteNotification(id) },
                                    onClearAll = { viewModel.clearAllNotifications() },
                                    onNavigateToWallet = { navController.navigate(Routes.WALLET) },
                                    onNavigateToNetworks = { navController.navigate(Routes.NETWORKS) },
                                    onBack = { navController.popBackStack() }
                                )
                            }


                            // WALLET
                            composable(Routes.WALLET) {
                                WalletScreen(
                                    balance = walletBalance,
                                    transactions = walletTransactions,
                                    isProcessing = isTopUpProcessing,
                                    onTopUpWallet = { amount, paymentMethod, refNum, callback ->
                                        viewModel.topUpWallet(amount, paymentMethod, refNum, callback)
                                    },
                                    onNavigateToTopUpSelection = {
                                        navController.navigate(Routes.WALLET_TOPUP_SELECTION)
                                    },
                                    onRefresh = { viewModel.syncWalletData() },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // WALLET TOPUP SELECTION PAGE
                            composable(Routes.WALLET_TOPUP_SELECTION) {
                                WalletTopUpSelectionScreen(
                                    currentBalance = walletBalance,
                                    onSelectWallet = { walletId ->
                                        navController.navigate("wallet_topup_form/$walletId")
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // WALLET TOPUP FORM PAGE
                            composable(
                                route = Routes.WALLET_TOPUP_FORM,
                                arguments = listOf(
                                    navArgument("walletId") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val walletId = backStackEntry.arguments?.getString("walletId") ?: "jaib"
                                WalletTopUpFormScreen(
                                    walletId = walletId,
                                    isProcessing = isTopUpProcessing,
                                    onConfirmDeposit = { amount, refNum, walletName, callback ->
                                        viewModel.topUpWallet(amount, walletName, refNum, callback)
                                    },
                                    onBack = { navController.popBackStack() },
                                    onDepositSuccess = {
                                        navController.popBackStack(Routes.WALLET, false)
                                    },
                                    onShowMessage = { viewModel.setToast(it) }
                                )
                            }

                            // NETWORKS (ALL NETWORKS)
                            composable(Routes.NETWORKS) {
                                NetworksScreen(
                                    networks = allNetworks,
                                    pinnedNetworkIds = pinnedNetworkIds,
                                    onSelectNetworkStore = { net ->
                                        viewModel.selectNetworkForStore(net)
                                        navController.navigate(Routes.PACKAGES)
                                    },
                                    onTogglePin = { id -> viewModel.togglePinNetwork(id) },
                                    onOpenSearch = { navController.navigate(Routes.SEARCH_NETWORK) }
                                )
                            }

                            // SEARCH NETWORK
                            composable(Routes.SEARCH_NETWORK) {
                                NetworkSearchScreen(
                                    searchQuery = searchQuery,
                                    isSearching = isSearching,
                                    allNetworks = allNetworks,
                                    searchedNetwork = searchedNetwork,
                                    pinnedNetworkIds = pinnedNetworkIds,
                                    onQueryChange = { viewModel.updateSearchQuery(it) },
                                    onSearch = { viewModel.searchNetwork() },
                                    onOpenNetworkStore = { net ->
                                        viewModel.selectNetworkForStore(net)
                                        navController.navigate(Routes.PACKAGES)
                                    },
                                    onTogglePin = { id -> viewModel.togglePinNetwork(id) },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // PACKAGES
                            composable(Routes.PACKAGES) {
                                val net = selectedNetwork
                                if (net != null) {
                                    PackagesScreen(
                                        network = net,
                                        packages = availablePackages,
                                        selectedQuantities = selectedQuantities,
                                        customerPhone = customerPhone,
                                        isPurchasing = isPurchasing,
                                        orders = orderHistory,
                                        walletTransactions = walletTransactions,
                                        walletBalance = walletBalance,
                                        showUnjoinedTopUpDialog = showUnjoinedTopUpDialog,
                                        showCeilingExhaustedDialog = showCeilingExhaustedDialog,
                                        showInsufficientWalletDialog = showInsufficientWalletDialog,
                                        onDismissUnjoinedDialog = { viewModel.dismissUnjoinedTopUpDialog() },
                                        onDismissCeilingDialog = { viewModel.dismissCeilingExhaustedDialog() },
                                        onDismissInsufficientWalletDialog = { viewModel.dismissInsufficientWalletDialog() },
                                        onQuantityChange = { pkgId, qty -> viewModel.setQuantity(pkgId, qty) },
                                        onClearAll = { viewModel.clearAllSelections() },
                                        onCustomerPhoneChange = { viewModel.setCustomerPhone(it) },
                                        onContinuePurchase = { method ->
                                            if (method == "wallet") {
                                                viewModel.processPurchaseViaWallet()
                                            } else {
                                                viewModel.processPurchase() 
                                            }
                                        },
                                        onRequestJoin = { targetNet -> viewModel.requestJoinNetwork(targetNet) },
                                        onNavigateToWallet = { navController.navigate(Routes.WALLET) },
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                            }


                            // PRINTER SETTINGS
                            composable(Routes.PRINTER_SETTINGS) {
                                PrinterSettingsScreen(
                                    currentPrinter = activePrinter,
                                    printerManager = viewModel.printerManager,
                                    onSavePrinter = { dev -> viewModel.saveSelectedPrinter(dev) },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // SALES HISTORY
                            composable(Routes.SALES_HISTORY) {
                                SalesHistoryScreen(
                                    orders = orderHistory,
                                    onReprintOrder = { order ->
                                        // Open receipt modal for reprint
                                        viewModel.printCurrentOrderReceipt()
                                    },
                                    onRefresh = { viewModel.syncAllData() },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // PROFILE / MY ACCOUNT
                            composable(Routes.PROFILE) {
                                ProfileScreen(
                                    user = user,
                                    printer = activePrinter,
                                    joinedNetworksCount = joinedNetworks.size,
                                    salesCount = orderHistory.size,
                                    walletBalance = walletBalance,
                                    themeMode = themeMode,
                                    onToggleThemeMode = { viewModel.toggleThemeMode() },
                                    onOpenPrinterSettings = { navController.navigate(Routes.PRINTER_SETTINGS) },
                                    onOpenSalesHistory = { navController.navigate(Routes.SALES_HISTORY) },
                                    onOpenWallet = { navController.navigate(Routes.WALLET) },
                                    onOpenAccountDetails = { navController.navigate(Routes.ACCOUNT_DETAILS) },
                                    onUpdateProfile = { storeName, location, callback ->
                                        viewModel.updateProfile(storeName, location, callback)
                                    },
                                    onChangePassword = { oldPass, newPass, callback ->
                                        viewModel.changePassword(oldPass, newPass, callback)
                                    },
                                    onDeleteAccount = { callback ->
                                        viewModel.deleteAccount { success, msg ->
                                            callback(success, msg)
                                            if (success) {
                                                navController.navigate(Routes.AUTH) {
                                                    popUpTo(Routes.HOME) { inclusive = true }
                                                }
                                            }
                                        }
                                    },
                                    onLogout = {
                                        viewModel.logout()
                                        navController.navigate(Routes.AUTH) {
                                            popUpTo(Routes.HOME) { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // ACCOUNT DETAILS (معلومات حسابي)
                            composable(Routes.ACCOUNT_DETAILS) {
                                AccountDetailsScreen(
                                    user = user,
                                    walletBalance = walletBalance,
                                    onUpdateProfile = { storeName, location, callback ->
                                        viewModel.updateProfile(storeName, location, callback)
                                    },
                                    onChangePassword = { oldPass, newPass, callback ->
                                        viewModel.changePassword(oldPass, newPass, callback)
                                    },
                                    onRequestForgotPasswordOtp = { phone, callback ->
                                        viewModel.requestForgotPasswordOtp(phone, callback)
                                    },
                                    onResetPasswordWithOtp = { phone, otp, newPass, callback ->
                                        viewModel.resetPasswordWithOtp(phone, otp, newPass, callback)
                                    },
                                    onDeleteAccount = { callback ->
                                        viewModel.deleteAccount { success, msg ->
                                            callback(success, msg)
                                            if (success) {
                                                navController.navigate(Routes.AUTH) {
                                                    popUpTo(Routes.HOME) { inclusive = true }
                                                }
                                            }
                                        }
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }

                        // Order Success / Receipt Modal Overlay
                        if (showReceiptModal && latestOrder != null) {
                            ReceiptDialog(
                                order = latestOrder!!,
                                isPrinting = isPrinting,
                                onPrintReceipt = { viewModel.printCurrentOrderReceipt() },
                                onDismiss = { viewModel.dismissReceiptModal() },
                                onShowMessage = { viewModel.setToast(it) }
                            )
                        }
                    }
                }

                // Top Floating In-App Alert
                TopAlertHost(
                    alert = currentAlert,
                    onDismiss = { viewModel.dismissAlert() }
                )
            }
            }
        }
    }
}
