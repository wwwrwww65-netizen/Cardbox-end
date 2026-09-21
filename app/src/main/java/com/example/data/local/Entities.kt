package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.JoinStatus

@Entity(tableName = "pos_account")
data class PosAccountEntity(
    @PrimaryKey val phone: String,
    val storeName: String,
    val location: String,
    val passwordHash: String,
    val isLoggedIn: Boolean
)

@Entity(tableName = "joined_networks")
data class JoinedNetworkEntity(
    @PrimaryKey val id: String,
    val code: String,
    val name: String,
    val ownerName: String,
    val financialCeiling: Double,
    val currentBalance: Double,
    val currency: String,
    val status: String, // JoinStatus name
    val location: String,
    val packagesCount: Int
)

@Entity(tableName = "orders")
data class OrderTransactionEntity(
    @PrimaryKey val id: String,
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
    val isPrinted: Boolean,
    val duration: String? = null,
    val dataQuota: String? = null,
    val validity: String? = null,
    val paymentSource: String = "NETWORK_CREDIT"
)

@Entity(tableName = "printer_settings")
data class PrinterSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val printerName: String,
    val macAddress: String,
    val isConnected: Boolean,
    val isSimulationMode: Boolean
)

@Entity(tableName = "wallet_account")
data class WalletAccountEntity(
    @PrimaryKey val id: Int = 1,
    val balance: Double
)

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val type: String, // WalletTxType name
    val amount: Double,
    val currency: String,
    val referenceNumber: String,
    val paymentMethod: String,
    val status: String, // WalletTxStatus name
    val timestamp: Long,
    val networkName: String?
)

@Entity(tableName = "notifications")
data class AppNotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val type: String, // NotificationType name
    val timestamp: Long,
    val isRead: Boolean,
    val relatedEntityId: String? = null,
    val amount: Double? = null
)


