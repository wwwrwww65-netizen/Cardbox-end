package com.example.data.local

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PosAccountDao {
    @Query("SELECT * FROM pos_account WHERE isLoggedIn = 1 LIMIT 1")
    fun getLoggedInAccount(): Flow<PosAccountEntity?>

    @Query("SELECT * FROM pos_account WHERE phone = :phone LIMIT 1")
    suspend fun getAccountByPhone(phone: String): PosAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: PosAccountEntity)

    @Query("UPDATE pos_account SET isLoggedIn = 0")
    suspend fun logoutAll()

    @Query("UPDATE pos_account SET isLoggedIn = 1 WHERE phone = :phone")
    suspend fun setLoggedIn(phone: String)

    @Query("UPDATE pos_account SET storeName = :storeName, location = :location WHERE phone = :phone")
    suspend fun updateAccountInfo(phone: String, storeName: String, location: String)

    @Query("UPDATE pos_account SET passwordHash = :newPasswordHash WHERE phone = :phone")
    suspend fun updatePassword(phone: String, newPasswordHash: String)

    @Query("DELETE FROM pos_account WHERE phone = :phone")
    suspend fun deleteAccountByPhone(phone: String)
}

@Dao
interface JoinedNetworkDao {
    @Query("SELECT * FROM joined_networks")
    fun getAllJoinedNetworks(): Flow<List<JoinedNetworkEntity>>

    @Query("SELECT * FROM joined_networks")
    suspend fun getAllNetworksList(): List<JoinedNetworkEntity>

    @Query("SELECT * FROM joined_networks WHERE id = :id LIMIT 1")
    suspend fun getNetworkById(id: String): JoinedNetworkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateNetwork(network: JoinedNetworkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(networks: List<JoinedNetworkEntity>)

    @Query("DELETE FROM joined_networks")
    suspend fun clearAll()

    @Query("UPDATE joined_networks SET currentBalance = :newBalance WHERE id = :networkId")
    suspend fun updateBalance(networkId: String, newBalance: Double)

    @Query("DELETE FROM joined_networks WHERE id = :id")
    suspend fun deleteNetworkById(id: String)

    @Query("DELETE FROM joined_networks WHERE id IN (:ids)")
    suspend fun deleteNetworksByIds(ids: List<String>)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<OrderTransactionEntity>>

    @Query("SELECT * FROM orders")
    suspend fun getOrdersListSync(): List<OrderTransactionEntity>

    @Query("SELECT * FROM orders WHERE networkId = :networkId ORDER BY timestamp DESC")
    fun getOrdersByNetwork(networkId: String): Flow<List<OrderTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderTransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(orders: List<OrderTransactionEntity>)

    @Query("UPDATE orders SET isPrinted = 1 WHERE id = :orderId")
    suspend fun markPrinted(orderId: String)

    @Query("DELETE FROM orders")
    suspend fun clearAll()
}

@Dao
interface PrinterDao {
    @Query("SELECT * FROM printer_settings WHERE id = 1 LIMIT 1")
    fun getPrinterSettings(): Flow<PrinterSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePrinterSettings(settings: PrinterSettingsEntity)
}

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet_account WHERE id = 1 LIMIT 1")
    fun getWalletAccount(): Flow<WalletAccountEntity?>

    @Query("SELECT * FROM wallet_account WHERE id = 1 LIMIT 1")
    suspend fun getWalletAccountSync(): WalletAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWalletAccount(account: WalletAccountEntity)

    @Query("DELETE FROM wallet_account")
    suspend fun clearWalletAccount()

    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC")
    fun getAllWalletTransactions(): Flow<List<WalletTransactionEntity>>

    @Query("SELECT * FROM wallet_transactions")
    suspend fun getWalletTransactionsListSync(): List<WalletTransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: WalletTransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<WalletTransactionEntity>)

    @Query("DELETE FROM wallet_transactions")
    suspend fun clearTransactions()
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<AppNotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<AppNotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: String)

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()
}

@Database(
    entities = [
        PosAccountEntity::class,
        JoinedNetworkEntity::class,
        OrderTransactionEntity::class,
        PrinterSettingsEntity::class,
        WalletAccountEntity::class,
        WalletTransactionEntity::class,
        AppNotificationEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun posAccountDao(): PosAccountDao
    abstract fun joinedNetworkDao(): JoinedNetworkDao
    abstract fun orderDao(): OrderDao
    abstract fun printerDao(): PrinterDao
    abstract fun walletDao(): WalletDao
    abstract fun notificationDao(): NotificationDao



    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mikrotik_pos_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
