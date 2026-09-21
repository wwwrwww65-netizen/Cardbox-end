package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Locale

// Yemeni Governorates ordered as requested: Amanat Al-Asimah, Sana'a, then all others
val YEMENI_GOVERNORATES = listOf(
    "أمانة العاصمة",
    "صنعاء",
    "عدن",
    "تعز",
    "الحديدة",
    "إب",
    "حضرموت",
    "ذمار",
    "حجة",
    "صعدة",
    "عمران",
    "الضالع",
    "لحج",
    "أبين",
    "شبوة",
    "مأرب",
    "المهرة",
    "البيضاء",
    "الجوف",
    "ريمة",
    "سقطرى",
    "المحويت"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    savedPhone: String = "",
    savedPassword: String = "",
    initialRememberMe: Boolean = false,
    onToggleThemeMode: () -> Unit = {},
    onRegister: (String, String, String, String, String, Boolean, (Boolean, String, String) -> Unit) -> Unit,
    onVerifyOtp: (String, String, String, String, String, (Boolean, String) -> Unit) -> Unit = { _, _, _, _, _, cb -> cb(true, "") },
    onLogin: (String, String, Boolean, (Boolean, String) -> Unit) -> Unit,
    onRequestForgotPasswordOtp: (String, (Boolean, String, String) -> Unit) -> Unit = { _, cb -> cb(true, "", "") },
    onResetPassword: (String, String, String, (Boolean, String) -> Unit) -> Unit = { _, _, _, cb -> cb(true, "") }
) {
    val context = LocalContext.current
    var isRegisterMode by remember { mutableStateOf(false) }

    // --- Login Form fields (Saved credentials used only for Login) ---
    var loginPhone by remember { mutableStateOf(savedPhone) }
    var loginPassword by remember { mutableStateOf(savedPassword) }
    var isLoginPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(initialRememberMe) }

    // --- Registration Form fields (Clean and completely blank for new registration) ---
    var regOwnerName by remember { mutableStateOf("") }
    var regStoreName by remember { mutableStateOf("") }
    var regSelectedGovernorate by remember { mutableStateOf("أمانة العاصمة") }
    var regStreetAddress by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var isRegPasswordVisible by remember { mutableStateOf(false) }
    var isRegConfirmPasswordVisible by remember { mutableStateOf(false) }

    // Governorate Selector Modal
    var showGovernoratePicker by remember { mutableStateOf(false) }
    var governorateSearchQuery by remember { mutableStateOf("") }

    LaunchedEffect(savedPhone, savedPassword) {
        if (savedPhone.isNotBlank()) loginPhone = savedPhone
        if (savedPassword.isNotBlank()) loginPassword = savedPassword
    }

    // OTP Screen state (Full screen mode)
    var isOtpScreenVisible by remember { mutableStateOf(false) }

    // Forgot password states
    var showForgotPasswordModal by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    if (isOtpScreenVisible) {
        val fullLocation = "$regSelectedGovernorate - $regStreetAddress".trim()
        OtpVerificationScreen(
            phone = regPhone.trim(),
            storeName = regStoreName.trim(),
            location = fullLocation,
            password = regPassword,
            onVerifyOtp = { otp, cb ->
                onVerifyOtp(regPhone.trim(), regStoreName.trim(), fullLocation, regPassword, otp) { success, msg ->
                    cb(success, msg)
                    if (success) {
                        isOtpScreenVisible = false
                    }
                }
            },
            onResendOtp = { cb ->
                val loc = "$regSelectedGovernorate - $regStreetAddress".trim()
                onRegister(regOwnerName.trim(), regStoreName.trim(), regPhone.trim(), loc, regPassword, rememberMe) { success, msg, _ ->
                    cb(success, msg)
                }
            },
            onBack = {
                isOtpScreenVisible = false
            }
        )
        return
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = onToggleThemeMode) {
                    val icon = when (themeMode) {
                        ThemeMode.LIGHT -> Icons.Outlined.LightMode
                        ThemeMode.DARK -> Icons.Outlined.DarkMode
                        ThemeMode.SYSTEM -> Icons.Outlined.SettingsBrightness
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = "تبديل المظهر",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Logo Banner
                Surface(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    shadowElevation = 6.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = "Card Box POS Logo",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Card Box POS",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isRegisterMode) "قم بإدخال بياناتك ومتجرك للانضمام كنقطة بيع معتمدة في Card Box POS" else "نظام Card Box POS لبيع وتوزيع كروت الإنترنت والطباعة الحرارية",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                )

                // Tab Switcher Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Button(
                            onClick = {
                                isRegisterMode = false
                                errorMessage = null
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tab_login"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isRegisterMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (!isRegisterMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = null
                        ) {
                            Text("تسجيل الدخول", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                isRegisterMode = true
                                errorMessage = null
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tab_register"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRegisterMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (isRegisterMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = null
                        ) {
                            Text("إنشاء حساب جديد", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Main Form Card
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (isRegisterMode) {
                            // ----------------- REGISTRATION FORM FIELDS -----------------
                            // 1. Full Name (Owner Name) - الاسم الثلاثي لصاحب نقطة البيع
                            OutlinedTextField(
                                value = regOwnerName,
                                onValueChange = { regOwnerName = it },
                                label = { Text("اسم صاحب نقطة البيع (الاسم الثلاثي)") },
                                placeholder = { Text("مثال: أحمد محمد علي الحرازي") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_owner_name"),
                                shape = RoundedCornerShape(14.dp)
                            )

                            // 2. Store / POS Name - اسم البقالة / نقطة البيع
                            OutlinedTextField(
                                value = regStoreName,
                                onValueChange = { regStoreName = it },
                                label = { Text("اسم البقالة / نقطة البيع") },
                                placeholder = { Text("مثال: بقالة الأمانة / نقطة النور") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Storefront,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_store_name"),
                                shape = RoundedCornerShape(14.dp)
                            )

                            // 3. Governorate Selector (Interactive Popup) - حقل اختيار المحافظة
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showGovernoratePicker = true }
                            ) {
                                OutlinedTextField(
                                    value = if (regSelectedGovernorate.isNotBlank()) regSelectedGovernorate else "انقر لاختيار المحافظة",
                                    onValueChange = {},
                                    readOnly = true,
                                    enabled = false,
                                    label = { Text("المحافظة") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.LocationCity,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.KeyboardArrowDown,
                                            contentDescription = "فتح قائمة المحافظات",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        disabledLabelColor = MaterialTheme.colorScheme.primary,
                                        disabledLeadingIconColor = MaterialTheme.colorScheme.primary,
                                        disabledTrailingIconColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_governorate"),
                                    shape = RoundedCornerShape(14.dp)
                                )
                            }

                            // 4. Neighborhood / Street & Landmark - الحي / الشارع وأقرب معلم
                            OutlinedTextField(
                                value = regStreetAddress,
                                onValueChange = { regStreetAddress = it },
                                label = { Text("الحي / الشارع والمعلم") },
                                placeholder = { Text("اسم الحي أو الشارع وأقرب معلم (مثال: حي الروضة - بجانب جامع النور)") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.PinDrop,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_street_address"),
                                shape = RoundedCornerShape(14.dp)
                            )

                            // 5. Phone Number for Registration (Empty by default)
                            OutlinedTextField(
                                value = regPhone,
                                onValueChange = { regPhone = it },
                                label = { Text("رقم الجوال") },
                                placeholder = { Text("77XXXXXXX أو 73XXXXXXX") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Phone,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_reg_phone"),
                                shape = RoundedCornerShape(14.dp)
                            )

                            // 6. Password for Registration (Empty by default)
                            OutlinedTextField(
                                value = regPassword,
                                onValueChange = { regPassword = it },
                                label = { Text("كلمة المرور") },
                                placeholder = { Text("******") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { isRegPasswordVisible = !isRegPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isRegPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                            contentDescription = "عرض/إخفاء كلمة المرور"
                                        )
                                    }
                                },
                                visualTransformation = if (isRegPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_reg_password"),
                                shape = RoundedCornerShape(14.dp)
                            )

                            // 7. Confirm Password (Only in Register Mode) - تأكيد كلمة المرور
                            OutlinedTextField(
                                value = regConfirmPassword,
                                onValueChange = { regConfirmPassword = it },
                                label = { Text("تأكيد كلمة المرور") },
                                placeholder = { Text("أعد كتابة كلمة المرور") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.LockReset,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { isRegConfirmPasswordVisible = !isRegConfirmPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isRegConfirmPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                            contentDescription = "عرض/إخفاء"
                                        )
                                    }
                                },
                                visualTransformation = if (isRegConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_confirm_password"),
                                shape = RoundedCornerShape(14.dp)
                            )
                        } else {
                            // ----------------- LOGIN FORM FIELDS -----------------
                            // 1. Phone Number for Login (Uses saved credentials if any)
                            OutlinedTextField(
                                value = loginPhone,
                                onValueChange = { loginPhone = it },
                                label = { Text("رقم الجوال") },
                                placeholder = { Text("77XXXXXXX") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Phone,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_phone"),
                                shape = RoundedCornerShape(14.dp)
                            )

                            // 2. Password for Login (Uses saved credentials if any)
                            OutlinedTextField(
                                value = loginPassword,
                                onValueChange = { loginPassword = it },
                                label = { Text("كلمة المرور") },
                                placeholder = { Text("******") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { isLoginPasswordVisible = !isLoginPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isLoginPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                            contentDescription = "عرض/إخفاء كلمة المرور"
                                        )
                                    }
                                },
                                visualTransformation = if (isLoginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_password"),
                                shape = RoundedCornerShape(14.dp)
                            )

                            // Remember Me & Forgot Password in Login Mode
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { rememberMe = !rememberMe }
                                ) {
                                    Checkbox(
                                        checked = rememberMe,
                                        onCheckedChange = { rememberMe = it },
                                        modifier = Modifier.testTag("checkbox_remember_me")
                                    )
                                    Text(
                                        text = "تذكرني للدخول السريع",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                TextButton(
                                    onClick = { showForgotPasswordModal = true },
                                    modifier = Modifier.testTag("btn_forgot_password")
                                ) {
                                    Text(
                                        text = "نسيت كلمة المرور؟",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        if (errorMessage != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = PosRedError.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, PosRedError.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ErrorOutline,
                                        contentDescription = null,
                                        tint = PosRedError,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = errorMessage ?: "",
                                        color = PosRedError,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                errorMessage = null
                                if (isRegisterMode) {
                                    if (regOwnerName.isBlank()) {
                                        errorMessage = "يرجى إدخال اسم صاحب نقطة البيع (الاسم الثلاثي)"
                                        return@Button
                                    }
                                    if (regStoreName.isBlank()) {
                                        errorMessage = "يرجى إدخال اسم البقالة أو نقطة البيع"
                                        return@Button
                                    }
                                    if (regSelectedGovernorate.isBlank()) {
                                        errorMessage = "يرجى اختيار المحافظة"
                                        return@Button
                                    }
                                    if (regStreetAddress.isBlank()) {
                                        errorMessage = "يرجى إدخال اسم الحي أو الشارع وأقرب معلم"
                                        return@Button
                                    }
                                    if (regPhone.isBlank()) {
                                        errorMessage = "يرجى إدخال رقم الجوال"
                                        return@Button
                                    }
                                    if (regPassword.isBlank()) {
                                        errorMessage = "يرجى إدخال كلمة المرور"
                                        return@Button
                                    }
                                    if (regConfirmPassword.isBlank()) {
                                        errorMessage = "يرجى تأكيد كلمة المرور"
                                        return@Button
                                    }
                                    if (regPassword != regConfirmPassword) {
                                        errorMessage = "كلمتا المرور غير متطابقتين، يرجى التأكد وإعادة الكتابة"
                                        return@Button
                                    }

                                    val fullLocation = "$regSelectedGovernorate - $regStreetAddress".trim()
                                    isLoading = true
                                    onRegister(regOwnerName.trim(), regStoreName.trim(), regPhone.trim(), fullLocation, regPassword, rememberMe) { success, msg, _ ->
                                        isLoading = false
                                        if (success) {
                                            isOtpScreenVisible = true
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        } else {
                                            errorMessage = msg
                                        }
                                    }
                                } else {
                                    if (loginPhone.isBlank() || loginPassword.isBlank()) {
                                        errorMessage = "يرجى إدخال رقم الجوال وكلمة المرور"
                                        return@Button
                                    }
                                    isLoading = true
                                    onLogin(loginPhone.trim(), loginPassword, rememberMe) { success, msg ->
                                        isLoading = false
                                        if (!success) errorMessage = msg
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("submit_auth_btn"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (isRegisterMode) "إنشاء الحساب والتحقق برمز OTP" else "تسجيل الدخول",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal 1: Governorate Selection Popup (نافذة حديثة لاختيار المحافظة)
    if (showGovernoratePicker) {
        Dialog(
            onDismissRequest = {
                showGovernoratePicker = false
                governorateSearchQuery = ""
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 440.dp)
                    .clip(RoundedCornerShape(26.dp)),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 10.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header with Icon & Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.LocationCity,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "اختر المحافظة",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "حدد محافظة نقطة البيع",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = {
                                showGovernoratePicker = false
                                governorateSearchQuery = ""
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "إغلاق",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Search input
                    OutlinedTextField(
                        value = governorateSearchQuery,
                        onValueChange = { governorateSearchQuery = it },
                        placeholder = { Text("ابحث عن المحافظة...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            if (governorateSearchQuery.isNotBlank()) {
                                IconButton(onClick = { governorateSearchQuery = "" }) {
                                    Icon(Icons.Outlined.Close, contentDescription = "مسح البحث", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val filteredGovs = remember(governorateSearchQuery) {
                        if (governorateSearchQuery.isBlank()) {
                            YEMENI_GOVERNORATES
                        } else {
                            val q = governorateSearchQuery.trim()
                            YEMENI_GOVERNORATES.filter { it.contains(q, ignoreCase = true) }
                        }
                    }

                    if (filteredGovs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لا توجد محافظة مطابقة للبحث",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(filteredGovs, key = { it }) { gov ->
                                val isSelected = gov == regSelectedGovernorate
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            regSelectedGovernorate = gov
                                            showGovernoratePicker = false
                                            governorateSearchQuery = ""
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (gov == "أمانة العاصمة") Icons.Outlined.Star else Icons.Outlined.Place,
                                                contentDescription = null,
                                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = gov,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                fontSize = 13.5.sp
                                            )
                                        }

                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Outlined.CheckCircle,
                                                contentDescription = "تم الاختيار",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        } else if (gov == "أمانة العاصمة") {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                            ) {
                                                Text(
                                                    text = "العاصمة",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
        }
    }

    // Modal 2: Forgot Password & Reset Password Flow (تصميم احترافي متكامل من 3 خطوات منفصلة)
    if (showForgotPasswordModal) {
        var forgotStep by remember { mutableIntStateOf(1) } // 1: Request OTP, 2: Verify 6-digit OTP, 3: Set New Password
        var resetPhone by remember { mutableStateOf(loginPhone) }
        var resetOtp by remember { mutableStateOf("") }
        var newPass by remember { mutableStateOf("") }
        var confirmNewPass by remember { mutableStateOf("") }
        var isNewPassVisible by remember { mutableStateOf(false) }
        var isConfirmNewPassVisible by remember { mutableStateOf(false) }
        var modalError by remember { mutableStateOf<String?>(null) }
        var isSubmitting by remember { mutableStateOf(false) }
        var timerSeconds by remember { mutableIntStateOf(120) }

        val focusRequester = remember { FocusRequester() }
        val keyboardController = LocalSoftwareKeyboardController.current

        // Auto timer countdown in Step 2
        LaunchedEffect(forgotStep, timerSeconds) {
            if (forgotStep == 2 && timerSeconds > 0) {
                delay(1000L)
                timerSeconds -= 1
            }
        }

        // Auto focus OTP on entering Step 2
        LaunchedEffect(forgotStep) {
            if (forgotStep == 2) {
                delay(300L)
                try {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                } catch (_: Exception) {}
            }
        }

        // Pulsing animation for security badge
        val infiniteTransition = rememberInfiniteTransition(label = "forgot_pwd_pulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )

        Dialog(
            onDismissRequest = {
                if (!isSubmitting) {
                    showForgotPasswordModal = false
                }
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 460.dp)
                    .imePadding()
                    .clip(RoundedCornerShape(28.dp)),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 22.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Bar with Back Icon (if in Step 2 or 3) & Close Icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (forgotStep > 1) {
                            IconButton(
                                onClick = {
                                    if (!isSubmitting) {
                                        modalError = null
                                        forgotStep -= 1
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = "الرجوع للخطوة السابقة",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.LockReset,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Current Step Tag
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "الخطوة $forgotStep من 3",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        // Close button
                        IconButton(
                            onClick = {
                                if (!isSubmitting) {
                                    showForgotPasswordModal = false
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "إغلاق",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Glowing Shield / Lock Icon
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.02f)
                                        )
                                    )
                                )
                        )
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shadowElevation = 4.dp,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = when (forgotStep) {
                                        1 -> Icons.Outlined.LockReset
                                        2 -> Icons.Outlined.VerifiedUser
                                        else -> Icons.Outlined.Password
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Title
                    Text(
                        text = when (forgotStep) {
                            1 -> "استعادة كلمة المرور"
                            2 -> "التحقق من رمز OTP"
                            else -> "تعيين كلمة المرور الجديدة"
                        },
                        fontSize = 18.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Subtitle / Description
                    Text(
                        text = when (forgotStep) {
                            1 -> "أدخل رقم هاتفك المسجل وسيقوم السيرفر بإرسال رمز تحقق OTP إليك عبر رسالة SMS"
                            2 -> "أدخل رمز التحقق المكون من 6 أرقام المستلم في رسالة SMS على هاتفك"
                            else -> "قم بإنشاء كلمة مرور جديدة وقوية لحساب نقطة البيع وتأكيدها"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3-Step Progress Indicator Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Step 1 Pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (forgotStep == 1) MaterialTheme.colorScheme.primary else if (forgotStep > 1) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 5.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (forgotStep > 1) Icons.Outlined.CheckCircle else Icons.Outlined.PhoneAndroid,
                                    contentDescription = null,
                                    tint = if (forgotStep == 1) Color.White else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "1. الهاتف",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (forgotStep == 1) Color.White else MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Step 2 Pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (forgotStep == 2) MaterialTheme.colorScheme.primary else if (forgotStep > 2) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 5.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (forgotStep > 2) Icons.Outlined.CheckCircle else Icons.Outlined.Key,
                                    contentDescription = null,
                                    tint = if (forgotStep == 2) Color.White else if (forgotStep > 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "2. رمز OTP",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (forgotStep == 2) Color.White else if (forgotStep > 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Step 3 Pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (forgotStep == 3) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 5.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.LockReset,
                                    contentDescription = null,
                                    tint = if (forgotStep == 3) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "3. الكلمة",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (forgotStep == 3) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // =========================================================================
                    // ----------------- STEP 1: PHONE NUMBER & SEND SMS OTP -------------------
                    // =========================================================================
                    if (forgotStep == 1) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Phone input field with Yemen country prefix
                            OutlinedTextField(
                                value = resetPhone,
                                onValueChange = {
                                    val digits = it.filter { ch -> ch.isDigit() }
                                    if (digits.length <= 9) {
                                        resetPhone = digits
                                        modalError = null
                                    }
                                },
                                label = { Text("رقم هاتف الحساب") },
                                placeholder = { Text("77XXXXXXX") },
                                leadingIcon = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 12.dp, end = 6.dp)
                                    ) {
                                        Text(text = "🇾🇪", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "+967",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(1.dp)
                                                .height(18.dp)
                                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                        )
                                    }
                                },
                                trailingIcon = {
                                    if (resetPhone.isNotBlank()) {
                                        IconButton(onClick = { resetPhone = "" }) {
                                            Icon(
                                                imageVector = Icons.Outlined.Close,
                                                contentDescription = "مسح",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Phone,
                                    imeAction = ImeAction.Done
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    focusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_forgot_phone")
                            )

                            // Quick tip banner
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Sms,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "يقوم السيرفر بإرسال رمز تحقق صالح لمرة واحدة عبر رسالة SMS نصية للتأكد من هويتك.",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    } else if (forgotStep == 2) {
                        // =========================================================================
                        // ----------------- STEP 2: ENTER & VERIFY 6-DIGIT OTP --------------------
                        // =========================================================================
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Phone summary pill with edit option
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.PhoneAndroid,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "الرقم: $resetPhone",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    TextButton(
                                        onClick = {
                                            forgotStep = 1
                                            modalError = null
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("تعديل الرقم", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }

                            // 6-Digit PIN Title
                            Text(
                                text = "رمز التحقق OTP (أدخل الـ 6 أرقام المستلمة):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Hidden actual input field linked to focusRequester
                            BasicTextField(
                                value = resetOtp,
                                onValueChange = { input ->
                                    val filtered = input.filter { it.isDigit() }
                                    if (filtered.length <= 6) {
                                        resetOtp = filtered
                                        modalError = null
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                modifier = Modifier
                                    .size(1.dp)
                                    .focusRequester(focusRequester)
                                    .testTag("forgot_otp_hidden_input")
                            )

                            // 6 Aesthetic Digit Boxes
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        focusRequester.requestFocus()
                                        keyboardController?.show()
                                    },
                                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (index in 0 until 6) {
                                    val char = resetOtp.getOrNull(index)?.toString() ?: ""
                                    val isFocused = resetOtp.length == index || (index == 5 && resetOtp.length == 6)
                                    val isFilled = char.isNotEmpty()

                                    val borderColor = when {
                                        modalError != null -> MaterialTheme.colorScheme.error
                                        isFocused -> MaterialTheme.colorScheme.primary
                                        isFilled -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                    }

                                    val containerColor = when {
                                        modalError != null -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                                        isFocused -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                        isFilled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        else -> MaterialTheme.colorScheme.surface
                                    }

                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = containerColor),
                                        border = BorderStroke(if (isFocused) 2.dp else 1.dp, borderColor),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (char.isNotEmpty()) {
                                                Text(
                                                    text = char,
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            } else if (isFocused) {
                                                val cursorAlpha by infiniteTransition.animateFloat(
                                                    initialValue = 0.2f,
                                                    targetValue = 1f,
                                                    animationSpec = infiniteRepeatable(
                                                        animation = tween(500, easing = LinearEasing),
                                                        repeatMode = RepeatMode.Reverse
                                                    ),
                                                    label = "forgot_cursor_$index"
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .width(2.dp)
                                                        .height(20.dp)
                                                        .background(
                                                            MaterialTheme.colorScheme.primary.copy(alpha = cursorAlpha),
                                                            RoundedCornerShape(1.dp)
                                                        )
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .background(
                                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                                            CircleShape
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Resend Timer Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (timerSeconds > 0) {
                                    val minutes = timerSeconds / 60
                                    val seconds = timerSeconds % 60
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Timer,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "إعادة الإرسال بعد ${String.format(Locale.US, "%02d:%02d", minutes, seconds)}",
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                } else {
                                    TextButton(
                                        onClick = {
                                            modalError = null
                                            isSubmitting = true
                                            onRequestForgotPasswordOtp(resetPhone.trim()) { success, msg, _ ->
                                                isSubmitting = false
                                                if (success) {
                                                    timerSeconds = 120
                                                    Toast.makeText(context, msg.ifBlank { "تمت إعادة إرسال رمز التحقق عبر SMS بنجاح" }, Toast.LENGTH_SHORT).show()
                                                } else {
                                                    modalError = msg
                                                }
                                            }
                                        },
                                        enabled = !isSubmitting,
                                        contentPadding = PaddingValues(0.dp)
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

                                if (resetOtp.isNotEmpty()) {
                                    TextButton(
                                        onClick = { resetOtp = "" },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("مسح الرمز", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    } else {
                        // =========================================================================
                        // ----------------- STEP 3: SET NEW PASSWORD & CONFIRM -------------------
                        // =========================================================================
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Verified OTP summary badge
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = PosEmeraldSuccess.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, PosEmeraldSuccess.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CheckCircle,
                                        contentDescription = null,
                                        tint = PosEmeraldSuccess,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "تم إدخال رمز التحقق للرقم: $resetPhone",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "رمز OTP المدخل: $resetOtp",
                                            fontSize = 11.sp,
                                            color = PosEmeraldSuccess,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            // New Password Field
                            OutlinedTextField(
                                value = newPass,
                                onValueChange = {
                                    newPass = it
                                    modalError = null
                                },
                                label = { Text("كلمة المرور الجديدة") },
                                placeholder = { Text("6 أحرف أو أرقام على الأقل") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { isNewPassVisible = !isNewPassVisible }) {
                                        Icon(
                                            imageVector = if (isNewPassVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                            contentDescription = "إظهار/إخفاء"
                                        )
                                    }
                                },
                                visualTransformation = if (isNewPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    focusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_forgot_new_pass")
                            )

                            // Confirm New Password Field
                            OutlinedTextField(
                                value = confirmNewPass,
                                onValueChange = {
                                    confirmNewPass = it
                                    modalError = null
                                },
                                label = { Text("تأكيد كلمة المرور الجديدة") },
                                placeholder = { Text("أعد إدخال كلمة المرور") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.LockReset,
                                        contentDescription = null,
                                        tint = if (confirmNewPass.isNotEmpty() && confirmNewPass == newPass) PosEmeraldSuccess else MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { isConfirmNewPassVisible = !isConfirmNewPassVisible }) {
                                        Icon(
                                            imageVector = if (isConfirmNewPassVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                            contentDescription = "إظهار/إخفاء"
                                        )
                                    }
                                },
                                visualTransformation = if (isConfirmNewPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    focusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_forgot_confirm_pass")
                            )

                            // Password Match Helper Indicator
                            if (confirmNewPass.isNotEmpty()) {
                                val isMatched = confirmNewPass == newPass
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isMatched) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel,
                                        contentDescription = null,
                                        tint = if (isMatched) PosEmeraldSuccess else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (isMatched) "كلمتا المرور متطابقتان" else "كلمتا المرور غير متطابقتين",
                                        fontSize = 11.sp,
                                        color = if (isMatched) PosEmeraldSuccess else MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Error Message Animated Banner
                    AnimatedVisibility(
                        visible = modalError != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        modalError?.let { error ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
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
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = error,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary Action Button
                    Button(
                        onClick = {
                            modalError = null
                            when (forgotStep) {
                                1 -> {
                                    // STEP 1: Validate phone and request SMS OTP
                                    if (resetPhone.isBlank()) {
                                        modalError = "يرجى إدخال رقم هاتف الحساب"
                                        return@Button
                                    }
                                    if (resetPhone.trim().length < 9) {
                                        modalError = "يرجى إدخال رقم هاتف صالح مكون من 9 أرقام"
                                        return@Button
                                    }
                                    isSubmitting = true
                                    onRequestForgotPasswordOtp(resetPhone.trim()) { success, msg, _ ->
                                        isSubmitting = false
                                        if (success) {
                                            resetOtp = "" // Always blank, waiting for real SMS
                                            timerSeconds = 120
                                            forgotStep = 2
                                            Toast.makeText(context, msg.ifBlank { "تم إرسال رمز استعادة كلمة المرور عبر SMS بنجاح" }, Toast.LENGTH_LONG).show()
                                        } else {
                                            modalError = msg
                                        }
                                    }
                                }
                                2 -> {
                                    // STEP 2: Validate 6-digit OTP and move to Step 3 (Set new password)
                                    if (resetOtp.isBlank() || resetOtp.trim().length != 6) {
                                        modalError = "يرجى إدخال رمز التحقق OTP المكون من 6 أرقام كاملاً"
                                        return@Button
                                    }
                                    // Proceed to Step 3
                                    forgotStep = 3
                                }
                                3 -> {
                                    // STEP 3: Validate passwords and submit reset to server
                                    if (newPass.isBlank()) {
                                        modalError = "يرجى إدخال كلمة المرور الجديدة"
                                        return@Button
                                    }
                                    if (newPass.length < 6) {
                                        modalError = "كلمة المرور الجديدة يجب ألا تقل عن 6 أحرف أو أرقام"
                                        return@Button
                                    }
                                    if (confirmNewPass.isBlank()) {
                                        modalError = "يرجى تأكيد كلمة المرور الجديدة"
                                        return@Button
                                    }
                                    if (newPass != confirmNewPass) {
                                        modalError = "كلمتا المرور غير متطابقتين، يرجى التأكد"
                                        return@Button
                                    }

                                    isSubmitting = true
                                    onResetPassword(resetPhone.trim(), resetOtp.trim(), newPass) { success, msg ->
                                        isSubmitting = false
                                        if (success) {
                                            loginPhone = resetPhone.trim()
                                            loginPassword = newPass
                                            showForgotPasswordModal = false
                                            Toast.makeText(context, msg.ifBlank { "تم تغيير كلمة المرور بنجاح! يمكنك الدخول الآن" }, Toast.LENGTH_LONG).show()
                                        } else {
                                            modalError = msg
                                        }
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_forgot_submit")
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (forgotStep) {
                                    1 -> "جاري إرسال الرمز..."
                                    2 -> "جاري التحقق..."
                                    else -> "جاري حفظ كلمة المرور..."
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = when (forgotStep) {
                                        1 -> "إرسال رمز التحقق (OTP) عبر SMS"
                                        2 -> "التحقق من الرمز والمتابعة"
                                        else -> "حفظ كلمة المرور الجديدة والدخول"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = when (forgotStep) {
                                        3 -> Icons.Outlined.Check
                                        else -> Icons.AutoMirrored.Outlined.ArrowBack
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Secondary action button
                    if (forgotStep == 2) {
                        TextButton(
                            onClick = {
                                modalError = null
                                forgotStep = 1
                            },
                            enabled = !isSubmitting,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "تعديل رقم الهاتف أو العودة",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else if (forgotStep == 3) {
                        TextButton(
                            onClick = {
                                modalError = null
                                forgotStep = 2
                            },
                            enabled = !isSubmitting,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "تعديل رمز التحقق OTP",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        TextButton(
                            onClick = { showForgotPasswordModal = false },
                            enabled = !isSubmitting,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "إلغاء والعودة لتسجيل الدخول",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
