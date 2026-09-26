package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EWalletOption
import com.example.data.model.WalletTransaction
import com.example.data.model.WalletTxStatus
import com.example.data.model.WalletTxType
import com.example.ui.theme.PosEmeraldSuccess
import com.example.ui.theme.PosIndigoPrimary
import com.example.ui.theme.PosIndigoPrimaryLight
import com.example.ui.theme.PosRedError
import com.example.ui.theme.PosTealSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Pre-configured Yemeni E-Wallets
val DEFAULT_E_WALLETS = listOf(
    EWalletOption(
        id = "jaib",
        name = "Jaib",
        arabicName = "محفظة جيب (Jaib)",
        accountNumber = "542865",
        colorHex = "#0F766E",
        subtitle = "دفع مشتريات لنقطة (542865) - بصمة العصر الحديث",
        instructions = "1. ادخل تطبيق جيب -> 2. اختر «دفع مشتريات» -> 3. اكتب رقم النقطة (542865) والمبلغ -> 4. اضغط استمرار وتأكد من الاسم (بصمة العصر الحديث) ثم تنفيذ -> 5. انسخ رقم المرجع وأدخله هنا للمتابعة.",
        accountHolderName = "بصمة العصر الحديث",
        accountLabel = "رقم نقطة البيع"
    ),
    EWalletOption(
        id = "jawali",
        name = "Jawali",
        arabicName = "محفظة جوالي (Jawali)",
        accountNumber = "891770",
        colorHex = "#0284C7",
        subtitle = "دفع مشتريات لنقطة (891770) - بصمة العصر الحديث",
        instructions = "1. ادخل تطبيق جوالي -> 2. اختر «دفع مشتريات / تاجر» -> 3. اكتب رقم النقطة (891770) والمبلغ -> 4. اضغط استمرار وتأكد من الاسم (بصمة العصر الحديث) ثم تنفيذ -> 5. انسخ رقم المرجع وأدخله هنا للمتابعة.",
        accountHolderName = "بصمة العصر الحديث",
        accountLabel = "رقم نقطة البيع"
    ),
    EWalletOption(
        id = "kuraimi",
        name = "Kuraimi",
        arabicName = "بنك الكريمي (حاسب / ام كريمي)",
        accountNumber = "777310606",
        colorHex = "#1E3A8A",
        subtitle = "تحويل مباشر للرقم: 777310606",
        instructions = "يرجى التحويل للرقم التالي 777310606 باسم هاشم محمد حمود الجايفي ثم أدخل الرقم المرجعي المباشر للتحقق",
        accountHolderName = "هاشم محمد حمود الجايفي",
        accountLabel = "رقم التحويل"
    ),
    EWalletOption(
        id = "onecash",
        name = "OneCash",
        arabicName = "محفظة ون كاش (OneCash)",
        accountNumber = "777310606",
        colorHex = "#B91C1C",
        subtitle = "تحويل مباشر للرقم: 777310606",
        instructions = "يرجى التحويل للرقم التالي 777310606 باسم هاشم محمد حمود الجايفي ثم أدخل رقم السند",
        accountHolderName = "هاشم محمد حمود الجايفي",
        accountLabel = "رقم التحويل"
    ),
    EWalletOption(
        id = "floosak",
        name = "Floosak",
        arabicName = "محفظة فلوسك (Floosak)",
        accountNumber = "777310606",
        colorHex = "#C2410C",
        subtitle = "تحويل مباشر للرقم: 777310606",
        instructions = "يرجى التحويل للرقم التالي 777310606 باسم هاشم محمد حمود الجايفي للتحقق الفوري",
        accountHolderName = "هاشم محمد حمود الجايفي",
        accountLabel = "رقم التحويل"
    ),
    EWalletOption(
        id = "saba_cash",
        name = "SabaCash",
        arabicName = "محفظة سبأ كاش / كاش",
        accountNumber = "777310606",
        colorHex = "#4338CA",
        subtitle = "تحويل مباشر للرقم: 777310606",
        instructions = "يرجى التحويل للرقم التالي 777310606 باسم هاشم محمد حمود الجايفي وأدخل رقم إشعار التحويل للتحقق",
        accountHolderName = "هاشم محمد حمود الجايفي",
        accountLabel = "رقم التحويل"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    balance: Double,
    transactions: List<WalletTransaction>,
    isProcessing: Boolean,
    onTopUpWallet: (amount: Double, paymentMethod: String, referenceNumber: String, callback: (Boolean, String) -> Unit) -> Unit,
    onNavigateToTopUpSelection: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredTransactions = remember(transactions, selectedFilter) {
        when (selectedFilter) {
            "DEPOSIT" -> transactions.filter { it.type == WalletTxType.DEPOSIT }
            "PURCHASE" -> transactions.filter { it.type == WalletTxType.VOUCHER_PURCHASE }
            else -> transactions
        }
    }

    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "محفظة CardBox",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("wallet_back_btn")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "رجوع",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.testTag("wallet_refresh_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "تحديث العمليات والرصيد",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(
                        onClick = onNavigateToTopUpSelection,
                        modifier = Modifier.testTag("header_topup_btn")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.AddCard,
                                    contentDescription = "تغذية الرصيد",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
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
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 140.dp)
        ) {
            // Main Balance Card
            item {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Unspecified),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        PosIndigoPrimary,
                                        PosIndigoPrimaryLight,
                                        PosTealSecondary
                                    )
                                )
                            )
                            .padding(22.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.White.copy(alpha = 0.2f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Outlined.AccountBalanceWallet,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "رصيد محفظة CardBox",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }

                                Surface(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = "جاهز للشراء",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "${balance.toInt()} ريال",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Text(
                                text = "الرصيد المتاح حالياً لشراء كروت الإنترنت من المحفظة مباشرة",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Action buttons row inside balance card
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = onNavigateToTopUpSelection,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = PosIndigoPrimary
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("btn_wallet_topup_now")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.AddCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "تغذية رصيد المحفظة",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Reports / Transactions Filter Header
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Analytics,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تقارير سجل العمليات",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "إجمالي: ${filteredTransactions.size} عملية",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Filter chips row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = selectedFilter == "ALL",
                            onClick = { selectedFilter = "ALL" },
                            label = { Text("الكل", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("filter_all")
                        )
                        FilterChip(
                            selected = selectedFilter == "DEPOSIT",
                            onClick = { selectedFilter = "DEPOSIT" },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.SouthWest, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("إيداعات", fontWeight = FontWeight.Bold)
                                }
                            },
                            modifier = Modifier.testTag("filter_deposit")
                        )
                        FilterChip(
                            selected = selectedFilter == "PURCHASE",
                            onClick = { selectedFilter = "PURCHASE" },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.ShoppingCart, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("مشتريات كروت", fontWeight = FontWeight.Bold)
                                }
                            },
                            modifier = Modifier.testTag("filter_purchase")
                        )
                    }
                }
            }

            // Transactions List
            if (filteredTransactions.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.3.dp,
                            MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(42.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "لا توجد عمليات مسجلة حالياً",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "عند تغذية الرصيد أو الشراء ستظهر جميع العمليات والتقارير هنا",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredTransactions, key = { it.id }) { tx ->
                    WalletTransactionCardItem(tx = tx)
                }
            }
        }
    }
}

@Composable
fun WalletTransactionCardItem(tx: WalletTransaction) {
    val isDeposit = tx.type == WalletTxType.DEPOSIT
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar")) }
    val formattedDate = remember(tx.timestamp) { dateFormat.format(Date(tx.timestamp)) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.3.dp,
            MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isDeposit) PosEmeraldSuccess.copy(alpha = 0.12f) else PosRedError.copy(alpha = 0.12f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isDeposit) Icons.Outlined.SouthWest else Icons.Outlined.NorthEast,
                                contentDescription = null,
                                tint = if (isDeposit) PosEmeraldSuccess else PosRedError,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = tx.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "مرجعي: ${tx.referenceNumber}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "• $formattedDate",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${if (isDeposit) "+" else "-"}${tx.amount.toInt()} ${tx.currency}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDeposit) PosEmeraldSuccess else PosRedError
                        )
                        if (!isDeposit) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "تكلفة",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        color = PosEmeraldSuccess.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isDeposit) "إيداع مكتمل" else "خصم تكلفة",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PosEmeraldSuccess,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // If it is a voucher purchase with selling price & profit details, show the bottom profit banner
            if (!isDeposit && tx.effectiveSellingPrice > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Sell,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "سعر البيع: ${tx.effectiveSellingPrice.toInt()} ريال",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(سعر التكلفة: ${tx.effectiveCostPrice.toInt()} ريال)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            color = PosEmeraldSuccess.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.TrendingUp,
                                    contentDescription = null,
                                    tint = PosEmeraldSuccess,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "ربح: +${tx.profitOrCalculated.toInt()} ريال",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PosEmeraldSuccess
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EWalletsSelectionDialog(
    wallets: List<EWalletOption>,
    onSelectWallet: (EWalletOption) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountBalanceWallet,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "اختر المحفظة المالية للتغذية",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "اختر محفظتك المفضلة للإيداع في حساب كارد بوكس وستحصل على رقم حساب نقطة المبيعات للتحويل الفوري:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                wallets.forEach { wallet ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        onClick = { onSelectWallet(wallet) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ewallet_option_${wallet.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.AccountBalance,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = wallet.arabicName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = wallet.subtitle,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Outlined.ArrowBackIosNew,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletDepositPopupDialog(
    eWallet: EWalletOption,
    isProcessing: Boolean,
    onConfirmDeposit: (amount: Double, referenceNumber: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var amountText by remember { mutableStateOf("") }
    var refNumberText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isStepsExpanded by remember { mutableStateOf(false) }
    val stepsArrowRotation by animateFloatAsState(
        targetValue = if (isStepsExpanded) 180f else 0f,
        label = "dialog_steps_arrow"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Payments,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "تغذية الحساب عبر ${eWallet.arabicName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // CardBox POS Account Number Info Box
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${eWallet.accountLabel} المعتمد في ${eWallet.arabicName}:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = eWallet.accountNumber,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Copy Account Number Button
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText(eWallet.accountLabel, eWallet.accountNumber)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "تم نسخ ${eWallet.accountLabel} بنجاح", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("btn_copy_account_number")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("نسخ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Account name verification tag
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Verified,
                                    contentDescription = null,
                                    tint = PosEmeraldSuccess,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "اسم الحساب المعتمد للنقطة: ${eWallet.accountHolderName}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Expandable step-by-step guidance
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { isStepsExpanded = !isStepsExpanded }
                                .animateContentSize()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.HelpOutline,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = if (isStepsExpanded) "إخفاء طريقة الإيداع والشراء" else "انقر لعرض خطوات العملية والشراء",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Outlined.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .rotate(stepsArrowRotation)
                                    )
                                }

                                AnimatedVisibility(
                                    visible = isStepsExpanded,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(top = 6.dp)
                                    ) {
                                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                                        if (eWallet.id in listOf("jaib", "jawali")) {
                                            val walletName = if (eWallet.id == "jaib") "جيب" else "جوالي"
                                            val serviceName = if (eWallet.id == "jaib") "«دفع مشتريات»" else "«دفع مشتريات / تاجر»"
                                            Text(
                                                text = "1. افتح تطبيق $walletName واختر $serviceName.\n2. اكتب رقم النقطة (${eWallet.accountNumber}) والمبلغ.\n3. اضغط «استمرار» وتأكد أن اسم النقطة هو «${eWallet.accountHolderName}» ثم اضغط «تنفيذ».\n4. انسخ رقم مرجع العملية وضعه أدناه للمتابعة.",
                                                fontSize = 10.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 15.sp
                                            )
                                        } else {
                                            Text(
                                                text = "1. افتح تطبيق ${eWallet.name} واختر التحويل.\n2. أدخل ${eWallet.accountLabel} (${eWallet.accountNumber}) والمبلغ.\n3. تأكد أن الاسم (${eWallet.accountHolderName}) ثم نفذ العملية.\n4. انسخ رقم المرجع وضعه أدناه للمتابعة.",
                                                fontSize = 10.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Amount Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { 
                        amountText = it 
                        errorMessage = null
                    },
                    label = { Text("كم المبلغ (بالريال)") },
                    placeholder = { Text("مثال: 10000") },
                    leadingIcon = { Icon(Icons.Outlined.PriceChange, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_deposit_amount")
                )

                // Reference Number Field
                OutlinedTextField(
                    value = refNumberText,
                    onValueChange = { 
                        refNumberText = it 
                        errorMessage = null
                    },
                    label = { Text("الرقم المرجعي / رقم العملية") },
                    placeholder = { Text("أدخل رقم السند أو العملية للتحقق") },
                    leadingIcon = { Icon(Icons.Outlined.Tag, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_deposit_ref_num")
                )

                // Error Banner when verification fails or inputs are invalid
                AnimatedVisibility(
                    visible = !errorMessage.isNullOrBlank(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .testTag("quick_deposit_error_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = errorMessage ?: "",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanAmtStr = amountText
                        .replace("٠", "0")
                        .replace("١", "1")
                        .replace("٢", "2")
                        .replace("٣", "3")
                        .replace("٤", "4")
                        .replace("٥", "5")
                        .replace("٦", "6")
                        .replace("٧", "7")
                        .replace("٨", "8")
                        .replace("٩", "9")
                        .replace(",", "")
                        .replace("،", "")
                        .replace(" ", "")
                        .replace("ريال", "")
                        .trim()
                    val amt = cleanAmtStr.toDoubleOrNull() ?: 0.0
                    if (amt <= 0) {
                        errorMessage = "يرجى إدخال مبلغ إيداع صحيح"
                        return@Button
                    }
                    val cleanRef = refNumberText
                        .replace("٠", "0")
                        .replace("١", "1")
                        .replace("٢", "2")
                        .replace("٣", "3")
                        .replace("٤", "4")
                        .replace("٥", "5")
                        .replace("٦", "6")
                        .replace("٧", "7")
                        .replace("٨", "8")
                        .replace("٩", "9")
                        .trim()
                    if (cleanRef.isBlank()) {
                        errorMessage = "يرجى إدخال الرقم المرجعي للعملية للتحقق"
                        return@Button
                    }
                    errorMessage = null
                    onConfirmDeposit(amt, cleanRef)
                },
                enabled = !isProcessing && amountText.isNotBlank() && refNumberText.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_verify_and_deposit")
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("التحقق وتغذية الحساب", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
