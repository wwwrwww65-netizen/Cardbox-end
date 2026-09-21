package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.model.PosUser
import com.example.ui.theme.*
import kotlinx.coroutines.delay

enum class PendingAccountAction {
    UPDATE_PROFILE,
    CHANGE_PASSWORD,
    DELETE_ACCOUNT,
    RESET_PASSWORD_DIRECT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailsScreen(
    user: PosUser?,
    walletBalance: Double = 0.0,
    onUpdateProfile: (String, String, (Boolean, String) -> Unit) -> Unit,
    onChangePassword: (String, String, (Boolean, String) -> Unit) -> Unit,
    onRequestForgotPasswordOtp: (String, (Boolean, String, String) -> Unit) -> Unit = { _, _ -> },
    onResetPasswordWithOtp: (String, String, String, (Boolean, String) -> Unit) -> Unit = { _, _, _, _ -> },
    onDeleteAccount: ((Boolean, String) -> Unit) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Form states
    var storeName by remember(user) { mutableStateOf(user?.storeName ?: "") }
    var location by remember(user) { mutableStateOf(user?.location ?: "") }
    val phone = user?.phone ?: ""

    // Password states
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isOldPassVisible by remember { mutableStateOf(false) }
    var isNewPassVisible by remember { mutableStateOf(false) }
    var isConfirmPassVisible by remember { mutableStateOf(false) }

    // OTP Modal states
    var pendingAction by remember { mutableStateOf<PendingAccountAction?>(null) }
    var showOtpModal by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }
    var isOtpVerifying by remember { mutableStateOf(false) }
    var isOtpSending by remember { mutableStateOf(false) }
    var otpErrorMsg by remember { mutableStateOf<String?>(null) }
    var timerSeconds by remember { mutableIntStateOf(120) }

    // Delete confirmation dialog
    var showDeleteWarningDialog by remember { mutableStateOf(false) }

    // Countdown Timer for OTP
    LaunchedEffect(showOtpModal, timerSeconds) {
        if (showOtpModal && timerSeconds > 0) {
            delay(1000L)
            timerSeconds -= 1
        }
    }

    // Function to trigger OTP request to Server
    fun triggerOtpRequest(action: PendingAccountAction) {
        pendingAction = action
        otpCode = ""
        otpErrorMsg = null
        isOtpSending = true

        onRequestForgotPasswordOtp(phone) { success, msg, _ ->
            isOtpSending = false
            if (success) {
                timerSeconds = 120
                showOtpModal = true
                Toast.makeText(context, msg.ifBlank { "تم إرسال رمز التحقق OTP عبر رسالة SMS إلى هاتفك $phone" }, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, msg.ifBlank { "تعذر إرسال رمز التحقق، يرجى المحاولة لاحقاً" }, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "معلومات حسابي ونقطة البيع",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
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
                .imePadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 140.dp)
        ) {
            // Card 1: Account Header Summary
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Surface(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .border(
                                        2.dp,
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary
                                            )
                                        ),
                                        CircleShape
                                    ),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_app_logo),
                                    contentDescription = "Store Avatar",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(PosEmeraldSuccess)
                                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = storeName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "مالك نقطة البيع: العميل المسجل",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Phone,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = phone,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Surface(
                                color = PosEmeraldSuccess.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CheckCircle,
                                        contentDescription = null,
                                        tint = PosEmeraldSuccess,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "حساب موثق ومفعل",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PosEmeraldSuccess
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Card 2: Editable Store Details
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Storefront,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تعديل بيانات المحل والمتجر",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        OutlinedTextField(
                            value = storeName,
                            onValueChange = { storeName = it },
                            label = { Text("اسم نقطة البيع / المتجر") },
                            leadingIcon = { Icon(Icons.Outlined.Store, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("عنوان المتجر / المدينة") },
                            leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = {},
                            enabled = false,
                            label = { Text("رقم الجوال (موثق بالنظام)") },
                            leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null) },
                            trailingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (storeName.isBlank() || location.isBlank()) {
                                    Toast.makeText(context, "يرجى ملء جميع الحقول المطلوب تعديلها", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                onUpdateProfile(storeName, location) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("حفظ وتحديث معلومات الحساب", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Card 3: Change Password Section with Full OTP Security
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.LockReset,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "تغيير وتعيين كلمة المرور",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Security Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Shield,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "محمي بـ OTP",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }

                        // Current Password
                        OutlinedTextField(
                            value = oldPassword,
                            onValueChange = { oldPassword = it },
                            label = { Text("كلمة المرور الحالية") },
                            placeholder = { Text("أدخل كلمة المرور الحالية") },
                            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { isOldPassVisible = !isOldPassVisible }) {
                                    Icon(
                                        imageVector = if (isOldPassVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (isOldPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // New Password
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("كلمة المرور الجديدة") },
                            placeholder = { Text("6 أحرف أو أرقام على الأقل") },
                            leadingIcon = { Icon(Icons.Outlined.Key, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { isNewPassVisible = !isNewPassVisible }) {
                                    Icon(
                                        imageVector = if (isNewPassVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (isNewPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Confirm New Password
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("تأكيد كلمة المرور الجديدة") },
                            placeholder = { Text("أعد كتابة كلمة المرور الجديدة") },
                            leadingIcon = { Icon(Icons.Outlined.Key, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { isConfirmPassVisible = !isConfirmPassVisible }) {
                                    Icon(
                                        imageVector = if (isConfirmPassVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (isConfirmPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Match Indicator
                        if (confirmPassword.isNotEmpty()) {
                            val isMatched = newPassword == confirmPassword
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isMatched) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel,
                                    contentDescription = null,
                                    tint = if (isMatched) PosEmeraldSuccess else PosRedError,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (isMatched) "كلمتا المرور متطابقتان ✓" else "كلمتا المرور غير متطابقتين",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMatched) PosEmeraldSuccess else PosRedError
                                )
                            }
                        }

                        // Button 1: Update Password via OTP SMS Verification
                        Button(
                            onClick = {
                                if (oldPassword.isBlank() || newPassword.isBlank()) {
                                    Toast.makeText(context, "يرجى كتابة كلمة المرور الحالية والجديدة", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (newPassword.length < 6) {
                                    Toast.makeText(context, "كلمة المرور الجديدة يجب ألا تقل عن 6 أحرف أو أرقام", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (newPassword != confirmPassword) {
                                    Toast.makeText(context, "كلمة المرور الجديدة غير متطابقة مع التأكيد", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                // Request SMS OTP from Server
                                triggerOtpRequest(PendingAccountAction.CHANGE_PASSWORD)
                            },
                            enabled = !isOtpSending,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isOtpSending) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("جاري طلب رمز التحقق SMS...", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Outlined.Sms, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("تحديث كلمة المرور بحماية رمز SMS (OTP)", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Button 2: Forgot Password Shortcut (Reset via SMS directly without knowing old password)
                        TextButton(
                            onClick = {
                                triggerOtpRequest(PendingAccountAction.RESET_PASSWORD_DIRECT)
                            },
                            enabled = !isOtpSending,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LockOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "نسيت كلمة المرور الحالية؟ إعادة تعيينها عبر SMS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Card 4: Danger Zone - Delete Account Button
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = PosRedError.copy(alpha = 0.1f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.Warning,
                                        contentDescription = null,
                                        tint = PosRedError,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "منطقة حذف الحساب النهائي",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PosRedError
                            )
                        }

                        Text(
                            text = "حذف حساب نقطة البيع نهائياً سيؤدي لإلغاء الارتباط بجميع شبكات المايكروتك ومحو سجل السندات بصفة دائمة.",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )

                        OutlinedButton(
                            onClick = { showDeleteWarningDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = PosRedError
                            ),
                            border = BorderStroke(1.dp, PosRedError.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_delete_account_in_details")
                        ) {
                            Icon(Icons.Outlined.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("حذف حساب نقطة البيع نهائياً", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // --- DIALOGS & OTP MODAL ---

    // Delete Warning Confirmation Dialog
    if (showDeleteWarningDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteWarningDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.ReportProblem, contentDescription = null, tint = PosRedError)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("هل أنت متأكد من حذف الحساب؟", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Text(
                    text = "سيصلك رمز تأكيد OTP عبر رسالة SMS إلى هاتفك $phone لإتمام عملية الحذف النهائية.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteWarningDialog = false
                        triggerOtpRequest(PendingAccountAction.DELETE_ACCOUNT)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PosRedError)
                ) {
                    Text("نعم، أرسل رمز SMS للحذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteWarningDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Professional Modern 6-Digit OTP Security Modal
    if (showOtpModal && pendingAction != null) {
        val focusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current

        LaunchedEffect(Unit) {
            delay(250L)
            try { focusRequester.requestFocus() } catch (e: Exception) {}
        }

        Dialog(
            onDismissRequest = {
                if (!isOtpVerifying) showOtpModal = false
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 460.dp)
                    .imePadding()
                    .clip(RoundedCornerShape(26.dp)),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Shield,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "تأكيد أمني SMS",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = { if (!isOtpVerifying) showOtpModal = false },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "إغلاق",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Title
                    Text(
                        text = "التحقق من رمز OTP",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Subtitle with Phone
                    Text(
                        text = "أدخل رمز التحقق (6 أرقام) المستلم في رسالة SMS على الرقم $phone لتأكيد العملية:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hidden actual BasicTextField for PIN entry
                    BasicTextField(
                        value = otpCode,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() }
                            if (filtered.length <= 6) {
                                otpCode = filtered
                                otpErrorMsg = null
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier
                            .size(1.dp)
                            .focusRequester(focusRequester)
                    )

                    // 6 Interactive PIN Digit Boxes
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { focusRequester.requestFocus() },
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until 6) {
                            val digit = otpCode.getOrNull(i)?.toString() ?: ""
                            val isFocused = otpCode.length == i || (otpCode.length == 6 && i == 5)

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = when {
                                    digit.isNotEmpty() -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                                    isFocused -> MaterialTheme.colorScheme.surfaceVariant
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                },
                                border = BorderStroke(
                                    width = if (isFocused) 2.dp else 1.dp,
                                    color = when {
                                        isFocused -> MaterialTheme.colorScheme.primary
                                        digit.isNotEmpty() -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        else -> MaterialTheme.colorScheme.outlineVariant
                                    }
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    if (digit.isNotEmpty()) {
                                        Text(
                                            text = digit,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            textAlign = TextAlign.Center
                                        )
                                    } else if (isFocused) {
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(20.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.primary,
                                                    RoundedCornerShape(1.dp)
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Error Message Display
                    otpErrorMsg?.let { error ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PosRedError.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, PosRedError.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = PosRedError, modifier = Modifier.size(16.dp))
                                Text(text = error, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = PosRedError)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Resend SMS Counter / Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (timerSeconds > 0) {
                            Icon(
                                imageVector = Icons.Outlined.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "إعادة إرسال SMS بعد: ${timerSeconds / 60}:${(timerSeconds % 60).toString().padStart(2, '0')}",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            TextButton(
                                onClick = {
                                    otpErrorMsg = null
                                    triggerOtpRequest(pendingAction ?: PendingAccountAction.CHANGE_PASSWORD)
                                },
                                enabled = !isOtpSending && !isOtpVerifying,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Refresh,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "لم يصلك؟ إعادة إرسال SMS",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary Confirmation Action Button
                    Button(
                        onClick = {
                            if (otpCode.length != 6) {
                                otpErrorMsg = "يرجى كتابة رمز OTP المكون من 6 أرقام كاملاً"
                                return@Button
                            }
                            isOtpVerifying = true
                            otpErrorMsg = null

                            when (pendingAction) {
                                PendingAccountAction.CHANGE_PASSWORD -> {
                                    // Verify OTP and reset password on server
                                    onResetPasswordWithOtp(phone, otpCode.trim(), newPassword) { success, msg ->
                                        isOtpVerifying = false
                                        if (success) {
                                            showOtpModal = false
                                            oldPassword = ""
                                            newPassword = ""
                                            confirmPassword = ""
                                            Toast.makeText(context, msg.ifBlank { "تم تغيير كلمة المرور بنجاح!" }, Toast.LENGTH_LONG).show()
                                        } else {
                                            otpErrorMsg = msg
                                        }
                                    }
                                }
                                PendingAccountAction.RESET_PASSWORD_DIRECT -> {
                                    // Direct reset without old password
                                    if (newPassword.isBlank() || newPassword.length < 6) {
                                        isOtpVerifying = false
                                        otpErrorMsg = "يرجى إدخال كلمة مرور جديدة لا تقل عن 6 أحرف في الحقل أعلاه"
                                        return@Button
                                    }
                                    onResetPasswordWithOtp(phone, otpCode.trim(), newPassword) { success, msg ->
                                        isOtpVerifying = false
                                        if (success) {
                                            showOtpModal = false
                                            oldPassword = ""
                                            newPassword = ""
                                            confirmPassword = ""
                                            Toast.makeText(context, msg.ifBlank { "تمت استعادة وتعيين كلمة المرور بنجاح!" }, Toast.LENGTH_LONG).show()
                                        } else {
                                            otpErrorMsg = msg
                                        }
                                    }
                                }
                                PendingAccountAction.UPDATE_PROFILE -> {
                                    onUpdateProfile(storeName, location) { success, msg ->
                                        isOtpVerifying = false
                                        if (success) {
                                            showOtpModal = false
                                            Toast.makeText(context, "تم تحديث بيانات الحساب بنجاح", Toast.LENGTH_SHORT).show()
                                        } else {
                                            otpErrorMsg = msg
                                        }
                                    }
                                }
                                PendingAccountAction.DELETE_ACCOUNT -> {
                                    onDeleteAccount { success, msg ->
                                        isOtpVerifying = false
                                        if (success) {
                                            showOtpModal = false
                                            Toast.makeText(context, "تم حذف الحساب بنجاح", Toast.LENGTH_SHORT).show()
                                        } else {
                                            otpErrorMsg = msg
                                        }
                                    }
                                }
                                null -> {
                                    isOtpVerifying = false
                                }
                            }
                        },
                        enabled = !isOtpVerifying,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (pendingAction == PendingAccountAction.DELETE_ACCOUNT) PosRedError else MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        if (isOtpVerifying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("جاري التحقق والتنفيذ...", fontWeight = FontWeight.Bold)
                        } else {
                            Text("تأكيد العملية الآن", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { showOtpModal = false },
                        enabled = !isOtpVerifying,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إلغاء والعودة", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
