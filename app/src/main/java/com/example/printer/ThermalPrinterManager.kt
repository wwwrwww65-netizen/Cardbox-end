package com.example.printer

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.example.data.model.OrderTransaction
import com.example.data.model.PrinterDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ThermalPrinterManager(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    fun isBluetoothAvailable(): Boolean {
        return try {
            bluetoothAdapter != null && bluetoothAdapter.isEnabled
        } catch (e: Exception) {
            false
        }
    }

    fun getBondedDevices(): List<PrinterDevice> {
        val list = mutableListOf<PrinterDevice>()
        // Add a default simulation device for fast emulator testing
        list.add(
            PrinterDevice(
                name = "طابعة حرارية افتراضية (Simulation Mode)",
                address = "00:11:22:33:44:55",
                isConnected = true,
                isSimulationMode = true
            )
        )
        try {
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
                val paired = bluetoothAdapter.bondedDevices
                paired?.forEach { device ->
                    list.add(
                        PrinterDevice(
                            name = device.name ?: "طابعة بلوتوث",
                            address = device.address,
                            isConnected = false,
                            isSimulationMode = false
                        )
                    )
                }
            }
        } catch (e: SecurityException) {
            // Permission missing or handled at runtime
        }
        return list
    }

    fun generateReceiptText(order: OrderTransaction): String {
        val cleanPin = order.voucherPin.replace("-", "").trim()
        val quotaStr = if (!order.dataQuota.isNullOrEmpty()) "الرصيد: ${order.dataQuota}\n" else ""
        val validityStr = if (!order.validity.isNullOrEmpty()) "الصلاحية: ${order.validity}\n" else if (!order.duration.isNullOrEmpty()) "المدة: ${order.duration}\n" else ""
        val storeStr = if (order.posStoreName.isNotBlank()) "نقطة: ${order.posStoreName}\n" else ""
        return """
        ${order.networkName}
        ${storeStr}فئة: ${order.packageName}
        ${quotaStr}${validityStr}--------------------------------
        رمز الكرت: $cleanPin
        --------------------------------
        شكراً لتعاملكم معنا
        CardBox POS
        
        """.trimIndent()
    }

    private fun String?.isNull_or_Empty(): Boolean = this == null || this.trim().isEmpty()

    suspend fun printOrder(order: OrderTransaction, device: PrinterDevice): PrintResult = withContext(Dispatchers.IO) {
        if (device.isSimulationMode) {
            // Simulated print delay
            kotlinx.coroutines.delay(1000)
            return@withContext PrintResult(
                success = true,
                message = "تمت عملية الطباعة بنجاح عبر الطابعة الافتراضية 58mm"
            )
        }

        if (!isBluetoothAvailable()) {
            return@withContext PrintResult(
                success = false,
                message = "البلوتوث غير مفعل! يرجى تفعيل البلوتوث والربط مع الطابعة الحرارية."
            )
        }

        try {
            val bluetoothDevice: BluetoothDevice = bluetoothAdapter?.getRemoteDevice(device.address)
                ?: return@withContext PrintResult(false, "لم يتم العثور على عنوان الطابعة البلوتوث المحددة.")

            val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard SPP UUID
            var socket: BluetoothSocket? = null
            try {
                socket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid)
                socket.connect()

                val outputStream: OutputStream = socket.outputStream
                val receiptContent = generateReceiptText(order)
                
                // ESC/POS Reset & Arabic encoding initialization
                outputStream.write(byteArrayOf(0x1B, 0x40)) // ESC @ initialize
                outputStream.write(receiptContent.toByteArray(charset("Cp864"))) // Arabic ESC/POS codepage
                outputStream.write(byteArrayOf(0x1D, 0x56, 0x42, 0x00)) // Paper Cut command
                outputStream.flush()
                socket.close()

                PrintResult(true, "تمت الطباعة بنجاح على الطابعة ${device.name}")
            } catch (e: Exception) {
                socket?.close()
                PrintResult(
                    false,
                    "تعذر الاتصال بالطابعة الحرارية (${e.localizedMessage ?: "تأكد من تشغيل الطابعة والبلوتوث"})"
                )
            }
        } catch (e: SecurityException) {
            PrintResult(false, "يرجى منح أذونات البلوتوث للاتصال بالطابعة.")
        }
    }
}

data class PrintResult(
    val success: Boolean,
    val message: String
)
