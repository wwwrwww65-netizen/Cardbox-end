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
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EWalletOption
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletTopUpSelectionScreen(
    currentBalance: Double,
    wallets: List<EWalletOption> = DEFAULT_E_WALLETS,
    onSelectWallet: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "طرق تغذية المحفظة",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_to_wallet_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "رجوع",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.shadow(2.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 140.dp)
        ) {
            // Balance Summary Header Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PosTealSecondary
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
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
                                Column {
                                    Text(
                                        text = "رصيد المحفظة الحالي",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${currentBalance.toInt()} ريال يمني",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.22f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Bolt,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "تغذية فورية",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.2f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )

                        Text(
                            text = "اختر المحفظة الإلكترونية المناسبة لك لإتمام عملية التغذية المباشرة بأمان وبأسرع وقت.",
                            fontSize = 11.5.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            // Section Title
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "المحافظ الإلكترونية المتاحة (${wallets.size}):",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Wallet Items
            items(wallets, key = { it.id }) { wallet ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.3.dp,
                        MaterialTheme.colorScheme.outlineVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    onClick = { onSelectWallet(wallet.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("topup_method_${wallet.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.AccountBalance,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = wallet.arabicName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = wallet.subtitle,
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletTopUpFormScreen(
    walletId: String,
    wallets: List<EWalletOption> = DEFAULT_E_WALLETS,
    isProcessing: Boolean,
    onConfirmDeposit: (amount: Double, referenceNumber: String, walletName: String, callback: (Boolean, String) -> Unit) -> Unit,
    onBack: () -> Unit,
    onDepositSuccess: () -> Unit,
    onShowMessage: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val eWallet = remember(walletId, wallets) {
        wallets.find { it.id == walletId } ?: wallets.first()
    }

    var amountText by remember { mutableStateOf("") }
    var refNumberText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isInstructionsExpanded by remember { mutableStateOf(false) }
    val instructionsArrowRotation by animateFloatAsState(
        targetValue = if (isInstructionsExpanded) 180f else 0f,
        label = "instructions_arrow_rot"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "تغذية عبر ${eWallet.arabicName}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_from_topup_form")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "رجوع",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.shadow(2.dp)
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
            // Stepper Visual Header
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant
                    ),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StepItem(number = "1", title = "دفع مشتريات", isActive = true)
                        HorizontalDivider(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 6.dp),
                            thickness = 2.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                        StepItem(number = "2", title = "تأكيد الاسم", isActive = true)
                        HorizontalDivider(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 6.dp),
                            thickness = 2.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                        StepItem(number = "3", title = "رقم المرجع", isActive = true)
                    }
                }
            }

            // Card 1: POS Account Number Display with Fast Copy
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${eWallet.accountLabel} المعتمد:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PosTealSecondary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = eWallet.arabicName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PosTealSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Display Account Number + Copy Button
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = eWallet.accountLabel,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = eWallet.accountNumber,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 1.5.sp
                                    )
                                }

                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText(eWallet.accountLabel, eWallet.accountNumber)
                                        clipboard.setPrimaryClip(clip)
                                        if (onShowMessage != null) {
                                            onShowMessage("تم نسخ ${eWallet.accountLabel} (${eWallet.accountNumber}) بنجاح")
                                        } else {
                                            Toast.makeText(context, "تم نسخ ${eWallet.accountLabel} بنجاح", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                    modifier = Modifier.testTag("copy_topup_acc_num_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ContentCopy,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("نسخ الرقم", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Account name badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PosEmeraldSuccess.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, PosEmeraldSuccess.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Verified,
                                    contentDescription = null,
                                    tint = PosEmeraldSuccess,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = "اسم الحساب المعتمد للنقطة:",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = eWallet.accountHolderName,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Card 2: Guided Step-by-Step Instructions (Expandable Accordion)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isInstructionsExpanded) PosTealSecondary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isInstructionsExpanded) PosTealSecondary.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { isInstructionsExpanded = !isInstructionsExpanded }
                        .animateContentSize()
                        .testTag("toggle_instructions_accordion")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Accordion Header Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = PosTealSecondary.copy(alpha = 0.14f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.MenuBook,
                                            contentDescription = null,
                                            tint = PosTealSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "طريقة وخطوات الإيداع",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = PosTealSecondary.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "5 خطوات",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PosTealSecondary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (isInstructionsExpanded) "اضغط لإخفاء الخطوات" else "انقر هنا لعرض الشرح التوضيحي بالتفصيل",
                                        fontSize = 11.5.sp,
                                        color = if (isInstructionsExpanded) PosTealSecondary else MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Rotating Chevron Icon
                            Surface(
                                shape = CircleShape,
                                color = if (isInstructionsExpanded) PosTealSecondary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.KeyboardArrowDown,
                                        contentDescription = if (isInstructionsExpanded) "إخفاء الخطوات" else "عرض الخطوات",
                                        tint = if (isInstructionsExpanded) PosTealSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .rotate(instructionsArrowRotation)
                                    )
                                }
                            }
                        }

                        // Collapsed Content (Animated In/Out)
                        AnimatedVisibility(
                            visible = isInstructionsExpanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier.padding(top = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = PosTealSecondary.copy(alpha = 0.2f)
                                )

                                if (eWallet.id == "jaib") {
                                    InstructionBulletStep(
                                        stepNumber = "1",
                                        title = "الدخول لتطبيق محفظة جيب",
                                        description = "قم بالدخول إلى تطبيق محفظة جيب على هاتفك وتسجيل الدخول إلى حسابك."
                                    )

                                    InstructionBulletStep(
                                        stepNumber = "2",
                                        title = "اختيار «دفع مشتريات»",
                                        description = "من القائمة والخدمات الرئيسية في تطبيق جيب، اختر خيار «دفع مشتريات»."
                                    )

                                    InstructionBulletStep(
                                        stepNumber = "3",
                                        title = "كتابة رقم نقطة البيع والمبلغ",
                                        description = "اكتب رقم نقطة البيع حسابنا في جيب: (${eWallet.accountNumber}) ثم اكتب المبلغ المراد إيداعه أو الشراء به."
                                    )

                                    InstructionBulletStep(
                                        stepNumber = "4",
                                        title = "استمرار والتأكد من الاسم ثم تنفيذ",
                                        description = "اضغط على «استمرار»، وتأكد تماماً بأن اسم حساب النقطة يظهر باسم: «${eWallet.accountHolderName}»، ثم اضغط على «تنفيذ»."
                                    )

                                    InstructionBulletStep(
                                        stepNumber = "5",
                                        title = "نسخ رقم مرجع العملية والمتابعة هنا",
                                        description = "انسخ «رقم مرجع العملية» من إشعار أو تفاصيل العملية في تطبيق جيب، وعُد إلى هنا وضع المبلغ ورقم مرجع العملية واضغط على متابعة."
                                    )
                                } else if (eWallet.id == "jawali") {
                                    InstructionBulletStep(
                                        stepNumber = "1",
                                        title = "الدخول لتطبيق محفظة جوالي",
                                        description = "قم بالدخول إلى تطبيق محفظة جوالي على هاتفك وتسجيل الدخول إلى حسابك."
                                    )

                                    InstructionBulletStep(
                                        stepNumber = "2",
                                        title = "اختيار «دفع مشتريات / تاجر»",
                                        description = "من القائمة والخدمات الرئيسية في تطبيق جوالي، اختر خيار «دفع مشتريات» (أو دفع لتاجر)."
                                    )

                                    InstructionBulletStep(
                                        stepNumber = "3",
                                        title = "كتابة رقم نقطة البيع والمبلغ",
                                        description = "اكتب رقم نقطة البيع حسابنا في جوالي: (${eWallet.accountNumber}) ثم اكتب المبلغ المراد إيداعه أو الشراء به."
                                    )

                                    InstructionBulletStep(
                                        stepNumber = "4",
                                        title = "استمرار والتأكد من الاسم ثم تنفيذ",
                                        description = "اضغط على «استمرار»، وتأكد تماماً بأن اسم حساب النقطة يظهر باسم: «${eWallet.accountHolderName}»، ثم اضغط على «تنفيذ»."
                                    )

                                    InstructionBulletStep(
                                        stepNumber = "5",
                                        title = "نسخ رقم مرجع العملية والمتابعة هنا",
                                        description = "انسخ «رقم مرجع العملية» من إشعار أو تفاصيل العملية في تطبيق جوالي، وعُد إلى هنا وضع المبلغ ورقم مرجع العملية واضغط على متابعة."
                                    )
                                } else {
                                    InstructionBulletStep(
                                        stepNumber = "1",
                                        title = "الدخول لتطبيق ${eWallet.name}",
                                        description = "قم بالدخول إلى تطبيق ${eWallet.arabicName} على هاتفك."
                                    )

                                    InstructionBulletStep(
                                        stepNumber = "2",
                                        title = "اختيار خدمة التحويل المالي",
                                        description = "اختر خدمة التحويل المالي أو الإيداع لحساب."
                                    )

                                    InstructionBulletStep(
                                        stepNumber = "3",
                                        title = "إدخال ${eWallet.accountLabel} والمبلغ",
                                        description = "أدخل ${eWallet.accountLabel}: (${eWallet.accountNumber}) واكتب المبلغ المطلوب."
                                    )

                                    InstructionBulletStep(
                                        stepNumber = "4",
                                        title = "التأكد من اسم الحساب والتنفيذ",
                                        description = "تأكد من أن اسم المستفيد يظهر باسم: «${eWallet.accountHolderName}» ثم نفذ العملية."
                                    )

                                    InstructionBulletStep(
                                        stepNumber = "5",
                                        title = "نسخ رقم السند/المرجع والمتابعة هنا",
                                        description = "انسخ رقم مرجع العملية أو السند وضعه في الحقل أدناه مع المبلغ واضغط على متابعة."
                                    )
                                }

                                // Important Verification Alert Box
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = PosAmberWarning.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, PosAmberWarning.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.WarningAmber,
                                            contentDescription = null,
                                            tint = PosAmberWarning,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = "تنبيه هام أثناء العملية:",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "تأكد من ظهور اسم حساب النقطة باسم «${eWallet.accountHolderName}» قبل الضغط على تنفيذ في المحفظة.",
                                                fontSize = 11.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Medium,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }

                                // Collapse Helper Button at bottom
                                OutlinedButton(
                                    onClick = { isInstructionsExpanded = false },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, PosTealSecondary.copy(alpha = 0.4f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = PosTealSecondary
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.KeyboardArrowUp,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "إغلاق خطوات الشرح",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Card 3: Deposit Form Fields
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "بيانات تأكيد التغذية:",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Amount Field
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { 
                                amountText = it 
                                errorMessage = null
                            },
                            label = { Text("مبلغ الإيداع (بالريال اليمني)") },
                            placeholder = { Text("أدخل المبلغ الذي قمت بإيداعه") },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.PriceChange,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = "ريال",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("topup_page_input_amount")
                        )

                        // Reference Number Field
                        OutlinedTextField(
                            value = refNumberText,
                            onValueChange = { 
                                refNumberText = it 
                                errorMessage = null
                            },
                            label = { Text("الرقم المرجعي / رقم العملية") },
                            placeholder = { Text("أدخل رقم السند أو التحويل للتحقق") },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Tag,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clipData = clipboard.primaryClip
                                        if (clipData != null && clipData.itemCount > 0) {
                                            val pasteText = clipData.getItemAt(0).text?.toString() ?: ""
                                            if (pasteText.isNotBlank()) {
                                                refNumberText = pasteText.trim()
                                                errorMessage = null
                                                Toast.makeText(context, "تم لصق الرقم المرجعي", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ContentPaste,
                                        contentDescription = "لصق الرقم المرجعي",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("topup_page_input_ref_num")
                        )

                        // Error Banner when verification fails or data doesn't match
                        AnimatedVisibility(
                            visible = !errorMessage.isNullOrBlank(),
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("topup_error_card")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ErrorOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = "خطأ في التحقق من الإيداع:",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Text(
                                            text = errorMessage ?: "",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            fontWeight = FontWeight.SemiBold,
                                            lineHeight = 17.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Submit Button
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
                                    errorMessage = "يرجى إدخال مبلغ إيداع صحيح (أكبر من 0)"
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
                                    errorMessage = "يرجى إدخال الرقم المرجعي أو رقم السند للتحقق"
                                    return@Button
                                }
                                errorMessage = null
                                onConfirmDeposit(amt, cleanRef, eWallet.name) { success, msg ->
                                    if (success) {
                                        errorMessage = null
                                        onDepositSuccess()
                                    } else {
                                        val errorDetail = msg.ifBlank { "المبلغ المدخل أو الرقم المرجعي غير مطابق لبيانات الحوالة بالسيرفر." }
                                        errorMessage = errorDetail
                                    }
                                }
                            },
                            enabled = !isProcessing && amountText.isNotBlank() && refNumberText.isNotBlank(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_confirm_page_topup")
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Verified,
                                        contentDescription = null,
                                        modifier = Modifier.size(19.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "تأكيد واستكمال تغذية المحفظة",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepItem(number: String, title: String, isActive: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
            modifier = Modifier.size(22.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun InstructionBulletStep(
    stepNumber: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = PosTealSecondary,
            modifier = Modifier.size(22.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stepNumber,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}
