package com.example.data.model

enum class JoinStatus {
    NOT_JOINED,
    PENDING,
    APPROVED,
    REJECTED
}

data class PosUser(
    val storeName: String,
    val phone: String,
    val location: String,
    val isLoggedIn: Boolean = false
)

data class NetworkItem(
    val id: String,
    val code: String,
    val name: String,
    val ownerName: String,
    val financialCeiling: Double,
    val currentBalance: Double,
    val currency: String = "ريال",
    val status: JoinStatus = JoinStatus.NOT_JOINED,
    val location: String = "المنطقة المركزية",
    val packagesCount: Int = 6,
    val description: String = "شبكة إنترنت عالية السرعة تغطي الأحياء المجاورة",
    val isPinned: Boolean = false
)

data class VoucherPackage(
    val id: String,
    val networkId: String,
    val name: String,
    val price: Double,
    val posPrice: Double = price,
    val currency: String = "ر.ي",
    val duration: String = "3 ساعات",
    val dataQuota: String = "400 MB",
    val validity: String = "4 أيام",
    val colorHex: String = "#0F4C81",
    val isAvailable: Boolean = true,
    val isPopular: Boolean = false
)

data class OrderTransaction(
    val id: String,
    val networkId: String,
    val networkName: String,
    val packageName: String,
    val packagePrice: Double,
    val costPrice: Double = 0.0,
    val quantity: Int,
    val totalAmount: Double,
    val totalCost: Double = 0.0,
    val customerPhone: String?,
    val voucherPin: String,
    val timestamp: Long,
    val posStoreName: String,
    val isPrinted: Boolean = false,
    val duration: String? = null,
    val dataQuota: String? = null,
    val validity: String? = null,
    val paymentSource: String = "NETWORK_CREDIT" // "NETWORK_CREDIT" (سقف الشبكة) or "WALLET" (محفظة CardBox)
) {
    val effectiveCostPrice: Double
        get() = if (costPrice > 0.0) costPrice else if (packagePrice > 0.0) packagePrice * 0.9 else 0.0

    val effectiveTotalCost: Double
        get() = if (totalCost > 0.0) totalCost else effectiveCostPrice * quantity

    val profit: Double
        get() = (totalAmount - effectiveTotalCost).coerceAtLeast(0.0)
}

data class PrinterDevice(
    val name: String,
    val address: String,
    val isConnected: Boolean = false,
    val isSimulationMode: Boolean = true
)

enum class WalletTxType {
    DEPOSIT,          // تغذية / إيداع
    VOUCHER_PURCHASE, // شراء كرت عبر المحفظة
    NETWORK_SETTLEMENT, // سداد مالك الشبكة
    TRANSFER          // تحويل
}

enum class WalletTxStatus {
    COMPLETED, // ناجحة / مكتملة
    PENDING,   // قيد التحقق
    REJECTED   // مرفوضة
}

data class WalletTransaction(
    val id: String,
    val title: String,
    val type: WalletTxType,
    val amount: Double,
    val sellingPrice: Double? = null,
    val profit: Double? = null,
    val currency: String = "ريال",
    val referenceNumber: String,
    val paymentMethod: String,
    val status: WalletTxStatus,
    val timestamp: Long,
    val networkName: String? = null
) {
    val effectiveCostPrice: Double
        get() = amount

    val effectiveSellingPrice: Double
        get() = sellingPrice ?: if (type == WalletTxType.VOUCHER_PURCHASE) (amount / 0.9) else 0.0

    val profitOrCalculated: Double
        get() = profit ?: (effectiveSellingPrice - effectiveCostPrice).coerceAtLeast(0.0)
}

data class EWalletOption(
    val id: String,
    val name: String,
    val arabicName: String,
    val accountNumber: String,
    val colorHex: String,
    val subtitle: String,
    val instructions: String,
    val accountHolderName: String = "بصمة العصر الحديث",
    val accountLabel: String = "رقم نقطة البيع"
)

enum class NotificationType {
    NETWORK_JOIN_APPROVED,   // قبول الانضمام في شبكة
    NETWORK_CREDIT_GRANTED,  // شحن رصيد من شبكة
    NETWORK_LOW_BALANCE,     // تنبيه قرب نفاذ رصيد شبكة
    WALLET_TOPUP_SUCCESS,    // شحن المحفظة
    WALLET_LOW_BALANCE,      // تنبيه رصيد المحفظة
    SYSTEM_ANNOUNCEMENT      // تنبيهات النظام والعروض
}

enum class NotificationCategory(val title: String) {
    ALL("الكل"),
    UNREAD("غير مقروءة"),
    NETWORKS("الشبكات"),
    WALLET("المحفظة"),
    SYSTEM("النظام")
}

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: Long,
    val isRead: Boolean = false,
    val relatedEntityId: String? = null,
    val amount: Double? = null
)


