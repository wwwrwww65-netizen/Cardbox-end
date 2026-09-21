package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.OrderTransaction
import com.example.data.model.PrinterDevice
import com.example.printer.ThermalPrinterManager
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrinterSettingsScreen(
    currentPrinter: PrinterDevice?,
    printerManager: ThermalPrinterManager,
    onSavePrinter: (PrinterDevice) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var devicesList by remember { mutableStateOf<List<PrinterDevice>>(emptyList()) }
    var selectedDevice by remember { mutableStateOf(currentPrinter) }
    var isTestingPrint by remember { mutableStateOf(false) }
    var testResultMsg by remember { mutableStateOf<String?>(null) }

    // Check printer bluetooth & location permissions
    fun checkPrinterPermissionsGranted(): Boolean {
        val btConnect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else true

        val loc = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        return btConnect && loc
    }

    var hasPrinterPermissions by remember { mutableStateOf(checkPrinterPermissionsGranted()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { map ->
        val granted = checkPrinterPermissionsGranted()
        hasPrinterPermissions = granted
        if (granted) {
            Toast.makeText(context, "تم منح أذونات البلوتوث والموقع للبحث عن الطابعات", Toast.LENGTH_SHORT).show()
            devicesList = printerManager.getBondedDevices()
        } else {
            Toast.makeText(context, "لم يتم منح كامل الأذونات المطلوبة للطابعة", Toast.LENGTH_SHORT).show()
        }
    }

    // Custom WiFi/IP Printer Dialog State
    var showWifiPrinterDialog by remember { mutableStateOf(false) }
    var wifiIpAddress by remember { mutableStateOf("192.168.1.200") }
    var wifiPort by remember { mutableStateOf("9100") }

    LaunchedEffect(Unit) {
        devicesList = printerManager.getBondedDevices()
        if (selectedDevice == null && devicesList.isNotEmpty()) {
            selectedDevice = devicesList.first()
        }
    }

    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "إعدادات الطابعة الحرارية",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 140.dp)
        ) {
            // Explanation Card for Printer Permissions
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "ما هي الأذونات المطلوبة للطابعة الحرارية؟",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "• طابعات البلوتوث: يتطلب نظام أندرويد إذني (البلوتوث Bluetooth Scan/Connect) و(الموقع Fine Location) لاكتشاف الأجهزة والاقتران الحراري بها.\n• طابعات الـ Wi-Fi / LAN الشبكية: تعمل عبر شبكة المحل (IP Address: 9100) وتتصل مباشرة بدون أذونات البلوتوث.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (hasPrinterPermissions) {
                                Surface(
                                    color = PosEmeraldSuccess.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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
                                            text = "أذونات البلوتوث والموقع ممنوحة",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PosEmeraldSuccess
                                        )
                                    }
                                }
                            } else {
                                Button(
                                    onClick = {
                                        val reqs = mutableListOf<String>()
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            reqs.add(Manifest.permission.BLUETOOTH_CONNECT)
                                            reqs.add(Manifest.permission.BLUETOOTH_SCAN)
                                        }
                                        reqs.add(Manifest.permission.ACCESS_FINE_LOCATION)
                                        reqs.add(Manifest.permission.ACCESS_COARSE_LOCATION)
                                        permissionLauncher.launch(reqs.toTypedArray())
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.BluetoothSearching,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("طلب أذونات البلوتوث والموقع الآن", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Active Printer Status Card
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.Print,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "الطابعة المحددة حالياً",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = selectedDevice?.name ?: "لم يتم تحديد طابعة",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Surface(
                                color = if (selectedDevice?.isConnected == true) PosEmeraldSuccess.copy(alpha = 0.15f) else PosAmberWarning.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = when {
                                        selectedDevice?.address?.contains(".") == true -> "Wi-Fi LAN"
                                        selectedDevice?.isSimulationMode == true -> "افتراضي"
                                        else -> "بلوتوث"
                                    },
                                    color = if (selectedDevice?.isConnected == true) PosEmeraldSuccess else PosAmberWarning,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Test Print Button
                        Button(
                            onClick = {
                                val dev = selectedDevice ?: return@Button
                                isTestingPrint = true
                                testResultMsg = null
                                coroutineScope.launch {
                                    val dummyOrder = OrderTransaction(
                                        id = "TEST-9988",
                                        networkId = "net-101",
                                        networkName = "شبكة التجربة اللاسلكية",
                                        packageName = "كرت تجربة 100",
                                        packagePrice = 100.0,
                                        quantity = 1,
                                        totalAmount = 100.0,
                                        customerPhone = "770000000",
                                        voucherPin = "123456789012",
                                        timestamp = System.currentTimeMillis(),
                                        posStoreName = "نقطة بيع التجربة",
                                        dataQuota = "500 MB",
                                        validity = "3 أيام",
                                        duration = "24 ساعة"
                                    )
                                    val res = printerManager.printOrder(dummyOrder, dev)
                                    isTestingPrint = false
                                    testResultMsg = if (res.success) "تمت عملية الطباعة التجريبية واختبار الربط بنجاح" else "تعذر الطباعة: ${res.message}"
                                }
                            },
                            enabled = !isTestingPrint && selectedDevice != null,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_test_print")
                        ) {
                            if (isTestingPrint) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Print,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("طباعة فاتورة تجريبية لاختبار الربط")
                            }
                        }

                        testResultMsg?.let { msg ->
                            Text(
                                text = msg,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (msg.contains("بنجاح")) PosEmeraldSuccess else PosRedError
                            )
                        }
                    }
                }
            }

            // Connection Type Selection Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "أجهزة البلوتوث والـ Wi-Fi المتاحة",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row {
                        OutlinedButton(
                            onClick = { showWifiPrinterDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(imageVector = Icons.Outlined.Wifi, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("طابعة شبكة IP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = { devicesList = printerManager.getBondedDevices() }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = "تحديث",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            items(devicesList, key = { it.address }) { dev ->
                val isSelected = selectedDevice?.address == dev.address

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedDevice = dev
                            onSavePrinter(dev)
                        }
                        .testTag("printer_item_${dev.address}")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = if (dev.address.contains(".")) Icons.Outlined.Wifi else Icons.Outlined.Bluetooth,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Column {
                                Text(
                                    text = dev.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = dev.address,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = "محدد",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog to add custom Wi-Fi / IP Printer
    if (showWifiPrinterDialog) {
        AlertDialog(
            onDismissRequest = { showWifiPrinterDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Outlined.Wifi, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إضافة طابعة حرارية عبر شبكة الـ Wi-Fi", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "أدخل عنوان IP الخاص بطابعة الفواتير الحرارية في شبكتك المحلية (منفذ الطباعة الافتراضي 9100):",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = wifiIpAddress,
                        onValueChange = { wifiIpAddress = it },
                        label = { Text("عنوان الـ IP (مثال: 192.168.1.200)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = wifiPort,
                        onValueChange = { wifiPort = it },
                        label = { Text("المنفذ (Port)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val wifiPrinter = PrinterDevice(
                            name = "طابعة Wi-Fi ($wifiIpAddress)",
                            address = "$wifiIpAddress:$wifiPort",
                            isConnected = true,
                            isSimulationMode = false
                        )
                        selectedDevice = wifiPrinter
                        onSavePrinter(wifiPrinter)
                        showWifiPrinterDialog = false
                        Toast.makeText(context, "تم حفظ طابعة الـ Wi-Fi بنجاح", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("حفظ واقتران")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWifiPrinterDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
