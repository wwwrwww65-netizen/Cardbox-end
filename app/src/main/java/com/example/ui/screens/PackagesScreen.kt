package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.JoinStatus
import com.example.data.model.NetworkItem
import com.example.data.model.OrderTransaction
import com.example.data.model.VoucherPackage
import com.example.data.model.WalletTransaction
import com.example.data.model.WalletTxType
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackagesScreen(
    network: NetworkItem,
    packages: List<VoucherPackage>,
    selectedQuantities: Map<String, Int>,
    customerPhone: String,
    isPurchasing: Boolean,
    orders: List<OrderTransaction> = emptyList(),
    walletTransactions: List<WalletTransaction> = emptyList(),
    walletBalance: Double = 0.0,
    showUnjoinedTopUpDialog: Boolean = false,
    showCeilingExhaustedDialog: Boolean = false,
    showInsufficientWalletDialog: Boolean = false,
    onDismissUnjoinedDialog: () -> Unit = {},
    onDismissCeilingDialog: () -> Unit = {},
    onDismissInsufficientWalletDialog: () -> Unit = {},
    onQuantityChange: (String, Int) -> Unit,
    onClearAll: () -> Unit = {},
    onCustomerPhoneChange: (String) -> Unit,
    onContinuePurchase: (String) -> Unit,
    onRequestJoin: (NetworkItem) -> Unit,
    onNavigateToWallet: () -> Unit = {},
    onBack: () -> Unit
) {
    val selectedEntries = selectedQuantities.filterValues { it > 0 }
    val totalQuantity = selectedEntries.values.sum()

    // State for controlling the checkout modal bottom sheet
    var isCheckoutSheetOpen by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(totalQuantity) {
        if (totalQuantity == 0) {
            isCheckoutSheetOpen = false
        }
    }

    // Calculate total price based on POS price
    val totalPrice = selectedEntries.entries.sumOf { entry ->
        val pkg = packages.find { it.id == entry.key }
        (pkg?.posPrice ?: pkg?.price ?: 0.0) * entry.value
    }

    // Network stats metrics calculation
    val networkOrders = remember(orders, network.id, network.code, network.name) {
        orders.filter { 
            it.networkId == network.id || 
            (network.code.isNotBlank() && it.networkId.equals(network.code, ignoreCase = true)) ||
            (network.name.isNotBlank() && it.networkName.equals(network.name, ignoreCase = true))
        }
    }
    val totalSoldCards = networkOrders.sumOf { it.quantity }
    val totalSoldAmount = networkOrders.sumOf { it.totalAmount }

    // Identify which orders were paid via direct wallet debit (cash payment)
    val walletOrderIds = remember(walletTransactions) {
        walletTransactions.filter { 
            it.type == WalletTxType.VOUCHER_PURCHASE || 
            it.paymentMethod.contains("محفظة") || 
            it.title.contains("شراء كروت") 
        }.map { it.referenceNumber }.toSet()
    }

    // Available balance under the financial ceiling is directly from the server (Source of Truth)
    val effectiveAvailableBalance = network.currentBalance

    // Consumed debt to owner: (Financial Ceiling - Available Balance)
    val consumedDebt = if (network.financialCeiling > 0) {
        (network.financialCeiling - network.currentBalance).coerceIn(0.0, network.financialCeiling)
    } else {
        0.0
    }

    val totalProfit = remember(networkOrders, packages) {
        networkOrders.sumOf { order ->
            val pkg = packages.find { it.name == order.packageName || it.price == order.packagePrice }
            val retailPrice = if (pkg != null && pkg.price > 0) pkg.price else order.packagePrice
            val posCost = if (pkg != null && pkg.posPrice > 0) (pkg.posPrice * order.quantity) else order.totalAmount
            val profit = (retailPrice * order.quantity) - posCost
            profit.coerceAtLeast(0.0)
        }
    }

    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = network.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_btn")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "رجوع",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        when (network.status) {
                            JoinStatus.APPROVED -> {
                                Surface(
                                    color = PosEmeraldSuccess.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Outlined.CheckCircle,
                                            contentDescription = null,
                                            tint = PosEmeraldSuccess,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "معتمدة",
                                            fontSize = 11.sp,
                                            color = PosEmeraldSuccess,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            JoinStatus.PENDING -> {
                                Surface(
                                    color = PosAmberWarning.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Outlined.HourglassTop,
                                            contentDescription = null,
                                            tint = PosAmberWarning,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "قيد الانتظار",
                                            fontSize = 11.sp,
                                            color = PosAmberWarning,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            else -> {
                                Button(
                                    onClick = { onRequestJoin(network) },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier
                                        .height(34.dp)
                                        .testTag("btn_request_join_header")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Outlined.Storefront,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "طلب انضمام",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.shadow(elevation = 2.dp)
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = totalQuantity > 0,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 16.dp,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isCheckoutSheetOpen = true }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = Color.White
                                        ) {
                                            Text(
                                                text = "$totalQuantity",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ShoppingCart,
                                        contentDescription = "السلة",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "الإجمالي: ${totalPrice.toInt()} ريال",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "تم تحديد $totalQuantity ${if (totalQuantity == 1) "كرت" else "كروت"} • اضغط لإكمال الشراء",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { isCheckoutSheetOpen = true },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                            modifier = Modifier.testTag("btn_open_checkout")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "إتمام الشراء",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Outlined.ShoppingCartCheckout,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Stats cards overview at top or join request banner based on network status
            when (network.status) {
                JoinStatus.APPROVED -> {
                    NetworkStatsOverview(
                        availableBalance = effectiveAvailableBalance,
                        financialCeiling = network.financialCeiling,
                        soldCardsCount = totalSoldCards,
                        soldTotalAmount = totalSoldAmount,
                        consumedDebt = consumedDebt,
                        totalProfit = totalProfit
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                JoinStatus.PENDING -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PosAmberWarning.copy(alpha = 0.12f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PosAmberWarning.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.HourglassTop,
                                contentDescription = null,
                                tint = PosAmberWarning,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "طلب الانضمام قيد الانتظار",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "تم إرسال طلب انضمام نقطة بيعك إلى إدارة الشبكة وبانتظار الموافقة وتحديد السقف المالي لتفعيل البيع.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                JoinStatus.NOT_JOINED -> {
                    // Not joined notice card removed as requested
                }
                else -> {}
            }

            Text(
                text = "فئات كروت الإنترنت المتاحة للبيع الفوري",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            if (packages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 140.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(packages, key = { it.id }) { pkg ->
                        val qty = selectedQuantities[pkg.id] ?: 0
                        PackageGridCard(
                            packageItem = pkg,
                            quantity = qty,
                            onIncrement = {
                                onQuantityChange(pkg.id, qty + 1)
                                isCheckoutSheetOpen = true
                            },
                            onDecrement = {
                                val newQty = (qty - 1).coerceAtLeast(0)
                                onQuantityChange(pkg.id, newQty)
                            },
                            onCardClick = {
                                if (qty == 0) {
                                    onQuantityChange(pkg.id, 1)
                                }
                                isCheckoutSheetOpen = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog 1: Unjoined Network Top Up Alert
    if (showUnjoinedTopUpDialog) {
        AlertDialog(
            onDismissRequest = onDismissUnjoinedDialog,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.AccountBalanceWallet,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "يرجى تغذية رصيد حسابك لإكمال الشراء",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "يرجى تغذية رصيد حسابك لإكمال الشراء. قم بتغذية رصيد حسابك الآن عبر المحافظ المالية لتتمكن من إصدار الكروت فوراً.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDismissUnjoinedDialog()
                        onNavigateToWallet()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_dialog_topup_now")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تغذية الحساب الآن", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismissUnjoinedDialog()
                        onRequestJoin(network)
                    }
                ) {
                    Text("طلب انضمام للشبكة")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Dialog 2: Financial Ceiling Exhausted Alert
    if (showCeilingExhaustedDialog) {
        AlertDialog(
            onDismissRequest = onDismissCeilingDialog,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "انتهى السقف المالي المتاح",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "انتهى رصيد السقف المالي المتاح من صاحب الشبكة (${network.ownerName}). يرجى تسديد صاحب الشبكة المبلغ الذي عليك وتجديد رصيد حسابك.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "أو يمكنك الاستمرار فوراً وإكمال عملية الشراء عبر رصيد محفظة CardBox الخاص بك!",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDismissCeilingDialog()
                        onContinuePurchase("wallet")
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_dialog_buy_via_cardbox")
                ) {
                    Text("إكمال عملية الشراء عبر كارد بوكس", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        onDismissCeilingDialog()
                        onNavigateToWallet()
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("تغذية المحفظة")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Dialog 3: Insufficient Wallet Balance Alert
    if (showInsufficientWalletDialog) {
        AlertDialog(
            onDismissRequest = onDismissInsufficientWalletDialog,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "لا يوجد رصيد كافٍ في محفظتك",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = "لا يوجد رصيد كافٍ في محفظة كارد بوكس لإكمال العملية. قم بتغذية رصيد حسابك الآن عبر المحافظ المالية (جيب، كريمي، ون كاش، جوالي...).",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDismissInsufficientWalletDialog()
                        onNavigateToWallet()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_dialog_insufficient_topup")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تغذية الحساب الآن", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissInsufficientWalletDialog) {
                    Text("إلغاء")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Modal 4: Purchase Checkout Sheet (Appears when opened via bottom bar or checkout action)
    if (isCheckoutSheetOpen && totalQuantity > 0) {
        ModalBottomSheet(
            onDismissRequest = { isCheckoutSheetOpen = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
            dragHandle = null
        ) {
            CheckoutBottomSheet(
                packages = packages,
                selectedQuantities = selectedQuantities,
                totalQuantity = totalQuantity,
                totalPrice = totalPrice,
                availableBalance = network.currentBalance,
                financialCeiling = network.financialCeiling,
                walletBalance = walletBalance,
                customerPhone = customerPhone,
                isPurchasing = isPurchasing,
                onCustomerPhoneChange = onCustomerPhoneChange,
                onQuantityChange = onQuantityChange,
                onCloseSheet = { isCheckoutSheetOpen = false },
                onClearAll = {
                    onClearAll()
                    isCheckoutSheetOpen = false
                },
                onContinuePurchase = { method ->
                    isCheckoutSheetOpen = false
                    onContinuePurchase(method)
                }
            )
        }
    }
}

@Composable
fun PackageGridCard(
    packageItem: VoucherPackage,
    quantity: Int,
    isApproved: Boolean = true,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onCardClick: () -> Unit
) {
    val isSelected = quantity > 0
    val profit = (packageItem.price - packageItem.posPrice).coerceAtLeast(0.0)

    val displayName = when {
        packageItem.name.startsWith("فئة") -> packageItem.name
        packageItem.name.isNotBlank() && packageItem.name.all { it.isDigit() || it == '.' || it.isWhitespace() } -> "فئة ${packageItem.name}"
        packageItem.name.isNotBlank() -> "فئة ${packageItem.name}"
        packageItem.price > 0 -> "فئة ${packageItem.price.toInt()}"
        else -> "فئة الكرت"
    }

    // Clean and normalize package specs
    val cleanPkg = remember(packageItem) {
        var quota = packageItem.dataQuota
        var duration = packageItem.duration
        var validity = packageItem.validity

        if (duration.contains("سعة") || duration.contains("مدة")) {
            val qMatch = Regex("""(?:سعة|حجم)[\s:]*([0-9.]+\s*(?:ميجابايت|ميغابايت|ميجا|جيجابايت|غيغابايت|جيجا|كيلوبايت|MB|GB|KB))""").find(duration)
            if (qMatch != null) quota = qMatch.groupValues[1]

            val dMatch = Regex("""(?:مدة|الوقت)[\s:]*([0-9.]+\s*(?:ساعات|ساعة|دقائق|دقيقة|أيام|ايام|يوم|Hours?|Hour|Mins?|Days?))""").find(duration)
            if (dMatch != null) duration = dMatch.groupValues[1]
        }

        val valNum = validity.toIntOrNull()
        if (valNum != null) {
            validity = when (valNum) {
                1 -> "يوم واحد"
                2 -> "يومان"
                in 3..10 -> "$valNum أيام"
                else -> "$valNum يوم"
            }
        }

        val durMatch = Regex("""^(\d+)\s*(?:ساعة|ساعات)$""").find(duration.trim())
        if (durMatch != null) {
            val n = durMatch.groupValues[1].toIntOrNull() ?: 1
            duration = when (n) {
                1 -> "ساعة واحدة"
                2 -> "ساعتان"
                in 3..10 -> "$n ساعات"
                else -> "$n ساعة"
            }
        }

        packageItem.copy(
            dataQuota = if (quota.isBlank()) "حسب الفئة" else quota,
            duration = if (duration.isBlank()) "حسب الرصيد" else duration,
            validity = if (validity.isBlank()) "حسب الاستخدام" else validity
        )
    }

    val cardBackground = MaterialTheme.colorScheme.surface
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = cardBackground,
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            borderColor
        ),
        shadowElevation = if (isSelected) 6.dp else 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable { onCardClick() }
            .testTag("package_card_${packageItem.id}")
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = PosEmeraldSuccess,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "متوفر",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                if (packageItem.isPopular) {
                    Surface(
                        color = PosAmberWarning.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PosAmberWarning),
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Bolt,
                                contentDescription = null,
                                tint = PosAmberWarning,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "الأكثر طلباً",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PosAmberWarning
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Center Wifi Icon
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Wifi,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Package Title ("فئة 100")
            Text(
                text = displayName,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Price Pill
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "${packageItem.posPrice.toInt()} ${packageItem.currency}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Specs Table Box
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    // Row 1: Data Quota
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الحجم:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = cleanPkg.dataQuota,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 1.dp
                    )

                    // Row 2: Duration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "المدة:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = cleanPkg.duration,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 1.dp
                    )

                    // Row 3: Validity
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الصلاحية:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = cleanPkg.validity,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Action Bar - Active counter for seamless selection
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (quantity > 0) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .clickable(enabled = quantity > 0) { onDecrement() }
                            .testTag("btn_dec_${packageItem.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Remove,
                            contentDescription = "إنقاص",
                            tint = if (quantity > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "$quantity",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { onIncrement() }
                            .testTag("btn_inc_${packageItem.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "زيادة",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CheckoutBottomSheet(
    packages: List<VoucherPackage>,
    selectedQuantities: Map<String, Int>,
    totalQuantity: Int,
    totalPrice: Double,
    availableBalance: Double,
    financialCeiling: Double = 0.0,
    walletBalance: Double = 0.0,
    customerPhone: String,
    isPurchasing: Boolean,
    onCustomerPhoneChange: (String) -> Unit,
    onQuantityChange: (String, Int) -> Unit,
    onCloseSheet: () -> Unit,
    onClearAll: () -> Unit,
    onContinuePurchase: (String) -> Unit
) {
    val isCeilingSufficient = availableBalance >= totalPrice
    val isWalletSufficient = walletBalance >= totalPrice
    val isBalanceSufficient = isCeilingSufficient || isWalletSufficient || financialCeiling > 0
    val selectedEntries = selectedQuantities.filterValues { it > 0 }
    
    val autoPaymentMethod = if (isCeilingSufficient || availableBalance >= totalPrice) "network_credit" else "wallet"

    Surface(
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 16.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Drag handle pill and close icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.size(32.dp))

                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                )

                // Quick Close X Button - Closes the sheet WITHOUT clearing selections
                IconButton(
                    onClick = onCloseSheet,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                        .testTag("btn_close_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "إغلاق النافذة",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Summary Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingCart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "تأكيد طلب شراء الكروت",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "إجمالي المحدد: $totalQuantity ${if (totalQuantity == 1) "كرت" else "كروت"}",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Clear All Button
                Surface(
                    onClick = onClearAll,
                    shape = RoundedCornerShape(10.dp),
                    color = PosRedError.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PosRedError.copy(alpha = 0.25f)),
                    modifier = Modifier.testTag("btn_clear_selection")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = PosRedError,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "مسح الكل",
                            fontSize = 11.5.sp,
                            color = PosRedError,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Selected Items List with Stepper (+ / -) directly inside checkout!
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(selectedEntries.keys.toList(), key = { it }) { pkgId ->
                    val pkg = packages.find { it.id == pkgId }
                    val qty = selectedQuantities[pkgId] ?: 0
                    if (pkg != null && qty > 0) {
                        val pkgPrice = if (pkg.posPrice > 0) pkg.posPrice else pkg.price
                        val itemSubtotal = pkgPrice * qty
                        val pkgDisplayName = when {
                            pkg.name.startsWith("فئة") -> pkg.name
                            pkg.name.isNotBlank() && pkg.name.all { it.isDigit() || it == '.' || it.isWhitespace() } -> "فئة ${pkg.name}"
                            pkg.name.isNotBlank() -> "فئة ${pkg.name}"
                            pkg.price > 0 -> "فئة ${pkg.price.toInt()}"
                            else -> "فئة الكرت"
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = pkgDisplayName,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${pkgPrice.toInt()} ريال للكرت × $qty = ${itemSubtotal.toInt()} ريال",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                // Interactive Stepper in Checkout Sheet
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = { onQuantityChange(pkg.id, (qty - 1).coerceAtLeast(0)) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = if (qty == 1) Icons.Outlined.Delete else Icons.Outlined.Remove,
                                            contentDescription = "تقليل",
                                            modifier = Modifier.size(16.dp),
                                            tint = if (qty == 1) PosRedError else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = "$qty",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        modifier = Modifier.widthIn(min = 24.dp),
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    IconButton(
                                        onClick = { onQuantityChange(pkg.id, qty + 1) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Add,
                                            contentDescription = "زيادة",
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Total amount & Payment Source bar
            val isCeilingInsufficient = !isCeilingSufficient && financialCeiling > 0
            Surface(
                color = if (isCeilingInsufficient && isWalletSufficient) 
                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
                else 
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, 
                    if (!isCeilingSufficient && isWalletSufficient) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "المبلغ المطلوب سداده",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (isCeilingSufficient) "خصم من السقف المالي للشبكة (آجل)" 
                                       else if (isWalletSufficient) "خصم من رصيد المحفظة (نقد)" 
                                       else "رصيد المحفظة: ${walletBalance.toInt()} ريال",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCeilingSufficient) PosEmeraldSuccess 
                                       else if (isWalletSufficient) MaterialTheme.colorScheme.primary 
                                       else PosAmberWarning
                            )
                        }

                        Text(
                            text = "${totalPrice.toInt()} ريال",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (!isCeilingSufficient && isWalletSufficient) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PosAmberWarning.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = PosAmberWarning,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "تنبيه: نظراً لأن الرصيد المنصرف المتاح من الشبكة (${availableBalance.toInt()} ريال) لا يغطي الكرت، فسيتم الدفع من المحفظة.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Customer Phone Number (Optional) with clear helper
            OutlinedTextField(
                value = customerPhone,
                onValueChange = onCustomerPhoneChange,
                label = { Text("رقم هاتف العميل (اختياري)") },
                placeholder = { Text("مثال: 771234567") },
                supportingText = {
                    Text(
                        text = if (customerPhone.isNotBlank()) "⚡ سيقوم السيرفر بإرسال كود الكرت فوراً إلى هذا الرقم عبر SMS" else "اختياري - يتيح إرسال كود الكرت تلقائياً لهاتف العميل عبر SMS",
                        fontSize = 11.sp,
                        color = if (customerPhone.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_customer_phone"),
                shape = RoundedCornerShape(14.dp)
            )

            if (!isBalanceSufficient) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PosAmberWarning.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PosAmberWarning.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = PosAmberWarning,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "عذراً، رصيدك في سقف الشبكة والمحفظة غير كافٍ لإتمام العملية.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Confirmation Button
            Button(
                onClick = { onContinuePurchase(autoPaymentMethod) },
                enabled = isBalanceSufficient && !isPurchasing && totalQuantity > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("btn_continue_checkout"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                if (isPurchasing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.ShoppingCartCheckout, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تأكيد شراء الكرت (${totalPrice.toInt()} ريال)",
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NetworkStatsOverview(
    availableBalance: Double,
    financialCeiling: Double,
    soldCardsCount: Int,
    soldTotalAmount: Double,
    consumedDebt: Double,
    totalProfit: Double = 0.0,
    currency: String = "ريال"
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Row 1: 3 Cards (الرصيد المتاح، السقف المالي، المستحق للمالك)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Card 1: الرصيد المتاح
            StatMiniCard(
                title = "الرصيد المتاح",
                value = "${availableBalance.toInt()} $currency",
                subtitle = "جاهز للبيع",
                icon = Icons.Outlined.AccountBalanceWallet,
                iconTint = PosEmeraldSuccess,
                bgColor = PosEmeraldSuccess.copy(alpha = 0.12f),
                borderColor = PosEmeraldSuccess.copy(alpha = 0.3f),
                isCompact = true,
                modifier = Modifier.weight(1f)
            )

            // Card 2: السقف المالي / الرصيد الكلي
            StatMiniCard(
                title = "السقف المالي",
                value = "${financialCeiling.toInt()} $currency",
                subtitle = "الحد الائتماني",
                icon = Icons.Outlined.PieChart,
                iconTint = MaterialTheme.colorScheme.primary,
                bgColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                isCompact = true,
                modifier = Modifier.weight(1f)
            )

            // Card 3: المستحق للمالك (آجل)
            StatMiniCard(
                title = "المستحق للمالك",
                value = "${consumedDebt.toInt()} $currency",
                subtitle = "واجب السداد",
                icon = Icons.Outlined.ReceiptLong,
                iconTint = PosAmberWarning,
                bgColor = PosAmberWarning.copy(alpha = 0.12f),
                borderColor = PosAmberWarning.copy(alpha = 0.3f),
                isCompact = true,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: 2 Cards (الكروت المباعة، صافي أرباحك)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Card 4: المبيعات (كم بعت كروت)
            StatMiniCard(
                title = "الكروت المباعة",
                value = if (soldCardsCount > 0) "$soldCardsCount كرت" else "0 كرت",
                subtitle = if (soldTotalAmount > 0) "إجمالي المبيعات: ${soldTotalAmount.toInt()} $currency" else "لا توجد مبيعات بعد",
                icon = Icons.Outlined.Sell,
                iconTint = Color(0xFFA855F7),
                bgColor = Color(0xFFA855F7).copy(alpha = 0.12f),
                borderColor = Color(0xFFA855F7).copy(alpha = 0.3f),
                isCompact = false,
                modifier = Modifier.weight(1f)
            )

            // Card 5: صافي الأرباح المكتسبة
            StatMiniCard(
                title = "صافي أرباحك",
                value = "${totalProfit.toInt()} $currency",
                subtitle = if (totalProfit > 0) "مكاسبك من بيع الكروت" else "فارق الشراء والبيع",
                icon = Icons.Outlined.TrendingUp,
                iconTint = PosEmeraldSuccess,
                bgColor = PosEmeraldSuccess.copy(alpha = 0.15f),
                borderColor = PosEmeraldSuccess.copy(alpha = 0.4f),
                isCompact = false,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatMiniCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    bgColor: Color,
    borderColor: Color,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        shadowElevation = 2.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isCompact) 8.dp else 10.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = if (isCompact) 10.sp else 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Surface(
                    shape = CircleShape,
                    color = bgColor,
                    modifier = Modifier.size(if (isCompact) 20.dp else 22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(if (isCompact) 11.dp else 12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = value,
                fontSize = if (isCompact) 13.sp else 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subtitle,
                fontSize = 8.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
