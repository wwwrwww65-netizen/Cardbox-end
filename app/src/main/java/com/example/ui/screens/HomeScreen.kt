package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.JoinStatus
import com.example.data.model.NetworkItem
import com.example.data.model.PosUser
import com.example.data.model.PrinterDevice
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    user: PosUser?,
    joinedNetworks: List<NetworkItem>,
    printer: PrinterDevice?,
    walletBalance: Double = 0.0,
    unreadNotificationsCount: Int = 0,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onToggleThemeMode: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenSearch: () -> Unit,
    onOpenPrinterSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenWallet: () -> Unit = {},
    onSelectNetworkStore: (NetworkItem) -> Unit,
    onTogglePinNetwork: (String) -> Unit = {},
    onMoveNetworkOrder: (String, Boolean) -> Unit = { _, _ -> },
    onMoveNetworksBatch: (Set<String>, Boolean) -> Unit = { _, _ -> },
    onRemoveNetworksFromHome: (Set<String>) -> Unit = {},
    onPinNetworksBatch: (Set<String>, Boolean) -> Unit = { _, _ -> },
    onLogout: () -> Unit
) {

    var isEditMode by rememberSaveable { mutableStateOf(false) }
    val selectedNetworkIds = remember { mutableStateListOf<String>() }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = isEditMode) {
        isEditMode = false
        selectedNetworkIds.clear()
    }


    val pairedNetworks = remember(joinedNetworks) { joinedNetworks.chunked(2) }
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(12.dp)
                                ),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_app_logo),
                                contentDescription = "Card Box POS Logo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Card Box POS",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "تطبيق نقاط بيع وتوزيع الكروت المعتمد",
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Notification Bell Icon with status dot
                    IconButton(
                        onClick = onOpenNotifications,
                        modifier = Modifier.testTag("notifications_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationsCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            text = if (unreadNotificationsCount > 99) "+99" else unreadNotificationsCount.toString(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "الإشعارات",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }


                    // Theme Mode Toggle Icon
                    IconButton(
                        onClick = onToggleThemeMode,
                        modifier = Modifier.testTag("theme_toggle_btn")
                    ) {
                        val icon = when (themeMode) {
                            ThemeMode.LIGHT -> Icons.Outlined.LightMode
                            ThemeMode.DARK -> Icons.Outlined.DarkMode
                            ThemeMode.SYSTEM -> Icons.Outlined.SettingsBrightness
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = "النمط",
                            tint = MaterialTheme.colorScheme.primary
                        )
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = if (isEditMode) 220.dp else 140.dp)
            ) {
            // Main Wallet Card (Radical Glassmorphic 2026 Style)
            item {
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Unspecified),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
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
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Outlined.AccountBalanceWallet,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "رصيد المحفظة المتاح",
                                        fontSize = 14.5.sp,
                                        color = Color.White.copy(alpha = 0.95f),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Surface(
                                    color = if (printer?.isConnected == true) PosEmeraldSuccess else PosAmberWarning,
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable { onOpenPrinterSettings() }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Print,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = if (printer?.isConnected == true) "طابعة متصلة" else "طابعة غير متصلة",
                                            fontSize = 12.5.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = "${walletBalance.toInt()} ريال",
                                        fontSize = 30.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "رصيد محفظة CardBox للشراء المباشر",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.85f),
                                        maxLines = 1
                                    )
                                }

                                // Quick Top-Up Wallet Action directly inside the main card
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { onOpenWallet() }
                                        .testTag("btn_topup_wallet_main_card")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.AddCard,
                                            contentDescription = null,
                                            tint = PosIndigoPrimary,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "تغذية المحفظة",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PosIndigoPrimary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Wallet Status & Details Footer
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.VerifiedUser,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "المحفظة نشطة وجاهزة للعمليات",
                                            fontSize = 12.5.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = onOpenWallet,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("action_open_wallet_card")
                                ) {
                                    Text(
                                        text = "سجل المحفظة ›",
                                        fontSize = 13.5.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }


            // Quick Secondary Actions Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionButton(
                        title = "إعدادات الطابعة",
                        icon = Icons.Outlined.Print,
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                        iconColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_printer"),
                        onClick = onOpenPrinterSettings
                    )

                    QuickActionButton(
                        title = "سجل المبيعات",
                        icon = Icons.Outlined.ReceiptLong,
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                        iconColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_history"),
                        onClick = onOpenHistory
                    )
                }
            }

            // Section Header: Networks
            item {
                if (!isEditMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "شبكاتي (${joinedNetworks.size})",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "اضغط على أي بطاقة للدخول للمتجر والباقات",
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (joinedNetworks.isNotEmpty()) {
                                FilledTonalButton(
                                    onClick = {
                                        isEditMode = true
                                        selectedNetworkIds.clear()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.testTag("btn_edit_networks_mode")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "تحرير وترتيب الشبكات",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "تحرير",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            TextButton(onClick = onOpenSearch) {
                                Text(
                                    text = "+ إضافة شبكة",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (selectedNetworkIds.isEmpty()) "وضع التحرير والترتيب" else "تم تحديد (${selectedNetworkIds.size} من ${joinedNetworks.size})",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (selectedNetworkIds.isEmpty()) "حدد شبكات للحذف أو التحريك" else "اختر الإجراء من الشريط بالأسفل",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                TextButton(
                                    onClick = {
                                        if (selectedNetworkIds.size == joinedNetworks.size) {
                                            selectedNetworkIds.clear()
                                        } else {
                                            selectedNetworkIds.clear()
                                            selectedNetworkIds.addAll(joinedNetworks.map { it.id })
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (selectedNetworkIds.size == joinedNetworks.size) "إلغاء الكل" else "تحديد الكل",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Button(
                                    onClick = {
                                        isEditMode = false
                                        selectedNetworkIds.clear()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تم", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Networks List - 2 Columns Grid Layout
            if (joinedNetworks.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.WifiOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "لا توجد شبكات مضافة حالياً",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "بمجرد الانضمام أو التفاعل مع أي شبكة ستضاف أيقونتها وكل كروتها إلى هنا.",
                                fontSize = 13.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onOpenSearch,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("البحث عن شبكة الآن", fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                            }
                        }
                    }
                }
            } else {
                items(pairedNetworks, key = { pair -> pair.first().id }) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val net1 = pair[0]
                        val isSelected1 = selectedNetworkIds.contains(net1.id)
                        NetworkGridCardItem(
                            network = net1,
                            isEditMode = isEditMode,
                            isSelected = isSelected1,
                            onToggleSelect = {
                                if (isSelected1) selectedNetworkIds.remove(net1.id)
                                else selectedNetworkIds.add(net1.id)
                            },
                            onOpenStore = { onSelectNetworkStore(net1) },
                            onLongPressToEdit = {
                                isEditMode = true
                                if (!selectedNetworkIds.contains(net1.id)) selectedNetworkIds.add(net1.id)
                            },
                            onTogglePin = { onTogglePinNetwork(net1.id) },
                            onMoveUp = { onMoveNetworkOrder(net1.id, true) },
                            onMoveDown = { onMoveNetworkOrder(net1.id, false) },
                            modifier = Modifier.weight(1f)
                        )

                        if (pair.size > 1) {
                            val net2 = pair[1]
                            val isSelected2 = selectedNetworkIds.contains(net2.id)
                            NetworkGridCardItem(
                                network = net2,
                                isEditMode = isEditMode,
                                isSelected = isSelected2,
                                onToggleSelect = {
                                    if (isSelected2) selectedNetworkIds.remove(net2.id)
                                    else selectedNetworkIds.add(net2.id)
                                },
                                onOpenStore = { onSelectNetworkStore(net2) },
                                onLongPressToEdit = {
                                    isEditMode = true
                                    if (!selectedNetworkIds.contains(net2.id)) selectedNetworkIds.add(net2.id)
                                },
                                onTogglePin = { onTogglePinNetwork(net2.id) },
                                onMoveUp = { onMoveNetworkOrder(net2.id, true) },
                                onMoveDown = { onMoveNetworkOrder(net2.id, false) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Bottom Multi-Action Floating Bar in Edit Mode
        AnimatedVisibility(
            visible = isEditMode && joinedNetworks.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                shadowElevation = 16.dp,
                tonalElevation = 6.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedNetworkIds.isEmpty()) "حدد شبكة أو أكثر للتحكم" else "تم تحديد ${selectedNetworkIds.size} من أصل ${joinedNetworks.size}",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Reorder controls
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "تحريك:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                onClick = {
                                    if (selectedNetworkIds.size == 1) {
                                        onMoveNetworkOrder(selectedNetworkIds.first(), true)
                                    } else if (selectedNetworkIds.isNotEmpty()) {
                                        onMoveNetworksBatch(selectedNetworkIds.toSet(), true)
                                    }
                                },
                                enabled = selectedNetworkIds.isNotEmpty(),
                                shape = RoundedCornerShape(10.dp),
                                color = if (selectedNetworkIds.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.ArrowForwardIos,
                                        contentDescription = "تقديم",
                                        modifier = Modifier.size(12.dp),
                                        tint = if (selectedNetworkIds.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "تقديم",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedNetworkIds.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                                }
                            }

                            Surface(
                                onClick = {
                                    if (selectedNetworkIds.size == 1) {
                                        onMoveNetworkOrder(selectedNetworkIds.first(), false)
                                    } else if (selectedNetworkIds.isNotEmpty()) {
                                        onMoveNetworksBatch(selectedNetworkIds.toSet(), false)
                                    }
                                },
                                enabled = selectedNetworkIds.isNotEmpty(),
                                shape = RoundedCornerShape(10.dp),
                                color = if (selectedNetworkIds.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.ArrowBackIosNew,
                                        contentDescription = "تأخير",
                                        modifier = Modifier.size(12.dp),
                                        tint = if (selectedNetworkIds.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "تأخير",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedNetworkIds.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Pin / Unpin Button
                        val allPinned = selectedNetworkIds.isNotEmpty() && selectedNetworkIds.all { id -> joinedNetworks.find { it.id == id }?.isPinned == true }
                        Button(
                            onClick = {
                                if (selectedNetworkIds.isNotEmpty()) {
                                    onPinNetworksBatch(selectedNetworkIds.toSet(), !allPinned)
                                }
                            },
                            enabled = selectedNetworkIds.isNotEmpty(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(Icons.Outlined.PushPin, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (allPinned) "إلغاء التثبيت" else "تثبيت بالصدارة",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Delete / Remove from Home Button
                        Button(
                            onClick = { showDeleteConfirmDialog = true },
                            enabled = selectedNetworkIds.isNotEmpty(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PosRedError,
                                contentColor = Color.White,
                                disabledContainerColor = PosRedError.copy(alpha = 0.25f),
                                disabledContentColor = Color.White.copy(alpha = 0.5f)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(46.dp)
                                .testTag("btn_delete_selected_from_home")
                        ) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (selectedNetworkIds.isEmpty()) "حذف من الرئيسية" else "حذف من الرئيسية (${selectedNetworkIds.size})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

    if (showDeleteConfirmDialog) {
        val count = selectedNetworkIds.size
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.DeleteForever,
                    contentDescription = null,
                    tint = PosRedError,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "حذف من شبكاتي في الرئيسية",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "هل تريد بالتأكيد إزالة $count ${if (count == 1) "شبكة" else "شبكات"} محددة من قسم شبكاتي في الصفحة الرئيسية؟\n\nلن تفقد أي كروت أو مبيعات سابقة، ويمكنك الوصول للشبكة مجدداً في أي وقت عبر شاشة البحث.",
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onRemoveNetworksFromHome(selectedNetworkIds.toSet())
                        selectedNetworkIds.clear()
                        isEditMode = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PosRedError),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("confirm_remove_from_home_btn")
                ) {
                    Text("نعم، حذف من الرئيسية", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("إلغاء", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(22.dp)
        )
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    borderColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        shadowElevation = 2.dp,
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconColor.copy(alpha = 0.12f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NetworkGridCardItem(
    network: NetworkItem,
    isEditMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: () -> Unit = {},
    onOpenStore: () -> Unit,
    onLongPressToEdit: () -> Unit = {},
    onTogglePin: () -> Unit = {},
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isApproved = network.status == JoinStatus.APPROVED
    val isPinned = network.isPinned

    val cardBorderColor = when {
        isEditMode && isSelected -> MaterialTheme.colorScheme.primary
        isPinned -> Color(0xFFF59E0B)
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val cardElevation = when {
        isEditMode && isSelected -> 6.dp
        isPinned -> 4.dp
        else -> 2.dp
    }

    val cardContainerColor = when {
        isEditMode && isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)
        else -> MaterialTheme.colorScheme.surface
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = cardContainerColor,
        border = androidx.compose.foundation.BorderStroke(
            if (isEditMode && isSelected) 2.dp else if (isPinned) 1.5.dp else 1.dp,
            cardBorderColor
        ),
        shadowElevation = cardElevation,
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .combinedClickable(
                onClick = {
                    if (isEditMode) {
                        onToggleSelect()
                    } else {
                        onOpenStore()
                    }
                },
                onLongClick = {
                    if (!isEditMode) {
                        onLongPressToEdit()
                    }
                }
            )
            .testTag("net_grid_card_${network.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Action Row:
            // In Edit Mode: Checkbox on top with "محددة" / "تحديد", and if selected, mini move buttons
            // In Normal Mode: Pin badge (if pinned) or subtle clean layout. NO DELETE BUTTON, NO REORDER ARROWS!
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isEditMode) {
                    // Selection Checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onToggleSelect() }
                            .padding(2.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(7.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(
                                2.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier.size(24.dp)
                        ) {
                            if (isSelected) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "محدد",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSelected) "محددة" else "تحديد",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Mini Move buttons on top-end of the card if selected
                    if (isSelected) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                onClick = onMoveUp,
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.ArrowForwardIos,
                                        contentDescription = "تقديم",
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Surface(
                                onClick = onMoveDown,
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.ArrowBackIosNew,
                                        contentDescription = "تأخير",
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Normal Mode: Pin Badge if pinned, or clean space
                    if (isPinned) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFFFEF3C7),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                            modifier = Modifier.testTag("pin_badge_${network.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.PushPin,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "مثبتة بالصدارة",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(1.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Wifi Icon & Network Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
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

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = network.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "رمز: ${network.code}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            // Status Badge (Pending / Approved / Unjoined)
            Spacer(modifier = Modifier.height(10.dp))
            when (network.status) {
                JoinStatus.APPROVED -> {
                    Surface(
                        color = Color(0xFFD1FAE5),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF047857),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "معتمدة",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF047857)
                                )
                            }
                            if (network.financialCeiling > 0) {
                                Text(
                                    "${network.currentBalance.toInt()} / ${network.financialCeiling.toInt()} ${network.currency}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF047857)
                                )
                            } else if (network.currentBalance > 0) {
                                Text(
                                    "${network.currentBalance.toInt()} ${network.currency}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF047857)
                                )
                            }
                        }
                    }
                }
                JoinStatus.PENDING -> {
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.HourglassTop,
                                contentDescription = null,
                                tint = Color(0xFF92400E),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "قيد الانتظار",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                }
                else -> {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "دفع عبر المحفظة",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = network.code,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Action on Card:
            // In Normal Mode: "عرض متجر الكروت" Button
            // In Edit Mode: Clean Selection indicator button
            if (isEditMode) {
                Surface(
                    onClick = onToggleSelect,
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.Check else Icons.Outlined.CheckBoxOutlineBlank,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSelected) "تم التحديد" else "انقر للتحديد",
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                Button(
                    onClick = onOpenStore,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("enter_grid_btn_${network.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Storefront,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "عرض متجر الكروت",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                }
            }
        }
    }
}
