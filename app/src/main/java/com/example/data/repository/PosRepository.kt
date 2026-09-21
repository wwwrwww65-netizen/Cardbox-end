package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import com.example.data.remote.ApiResponse
import com.example.data.remote.MikroTikApiService
import com.example.data.remote.PurchaseResult
import com.example.data.remote.RetrofitClient
import com.example.notification.AppNotificationManager
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext



class PosRepository(
    private val db: AppDatabase,
    private val apiService: MikroTikApiService,
    private val context: Context? = null
) {
    private val walletPrefs by lazy {
        context?.getSharedPreferences("pos_wallet_purchases_store", Context.MODE_PRIVATE)
    }

    fun recordWalletPurchase(orderId: String?, pin: String?) {
        try {
            walletPrefs?.let { prefs ->
                val currentPins = prefs.getStringSet("wallet_pins", emptySet())?.toMutableSet() ?: mutableSetOf()
                val currentIds = prefs.getStringSet("wallet_ids", emptySet())?.toMutableSet() ?: mutableSetOf()

                if (!pin.isNullOrBlank() && pin != "------") {
                    val parts = pin.split("|")
                    for (part in parts) {
                        val trimmed = part.trim()
                        if (trimmed.isNotBlank() && trimmed != "------") {
                            val cleanPin = trimmed.replace("-", "").replace(" ", "")
                            currentPins.add(trimmed)
                            currentPins.add(cleanPin)
                        }
                    }
                }
                if (!orderId.isNullOrBlank()) {
                    currentIds.add(orderId.trim())
                }

                prefs.edit()
                    .putStringSet("wallet_pins", currentPins)
                    .putStringSet("wallet_ids", currentIds)
                    .apply()
            }
        } catch (e: Exception) {
            // Non-blocking
        }
    }

    fun isKnownWalletPurchase(orderId: String?, pin: String?): Boolean {
        try {
            val prefs = walletPrefs ?: return false
            val savedPins = prefs.getStringSet("wallet_pins", emptySet()) ?: emptySet()
            val savedIds = prefs.getStringSet("wallet_ids", emptySet()) ?: emptySet()

            if (!orderId.isNullOrBlank() && savedIds.contains(orderId.trim())) return true
            if (!pin.isNullOrBlank() && pin != "------") {
                val parts = pin.split("|")
                for (part in parts) {
                    val trimmed = part.trim()
                    val cleanPin = trimmed.replace("-", "").replace(" ", "")
                    if (savedPins.contains(trimmed) || (cleanPin.isNotBlank() && savedPins.contains(cleanPin))) {
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            // Non-blocking
        }
        return false
    }

    val loggedInUser: Flow<PosUser?> = db.posAccountDao().getLoggedInAccount().map { entity ->
        entity?.let {
            PosUser(
                storeName = it.storeName,
                phone = it.phone,
                location = it.location,
                isLoggedIn = it.isLoggedIn
            )
        }
    }

    val joinedNetworks: Flow<List<NetworkItem>> = db.joinedNetworkDao().getAllJoinedNetworks().map { list ->
        list.map {
            NetworkItem(
                id = it.id,
                code = it.code,
                name = it.name,
                ownerName = it.ownerName,
                financialCeiling = it.financialCeiling,
                currentBalance = it.currentBalance,
                currency = it.currency,
                status = try { JoinStatus.valueOf(it.status) } catch (e: Exception) { JoinStatus.NOT_JOINED },
                location = it.location,
                packagesCount = it.packagesCount
            )
        }
    }

    val orderHistory: Flow<List<OrderTransaction>> = combine(
        db.orderDao().getAllOrders(),
        db.walletDao().getAllWalletTransactions()
    ) { list, walletTxs ->
        val walletDebitTxs = walletTxs.filter {
            it.type == WalletTxType.VOUCHER_PURCHASE.name ||
            it.title.contains("شراء", ignoreCase = true) ||
            it.title.contains("كرت", ignoreCase = true)
        }
        val walletOrderIds = walletDebitTxs.mapNotNull { it.referenceNumber.ifBlank { null } }.toSet()

        // Deduplicate orders in case of transient local/server overlap
        val seenPins = mutableSetOf<String>()
        val seenIds = mutableSetOf<String>()
        val dedupedList = mutableListOf<OrderTransactionEntity>()

        list.forEach { order ->
            val hasPin = order.voucherPin.isNotBlank() && order.voucherPin != "------"
            val alreadySeenPin = hasPin && seenPins.contains(order.voucherPin)
            val alreadySeenId = seenIds.contains(order.id)

            if (!alreadySeenId && !alreadySeenPin) {
                seenIds.add(order.id)
                if (hasPin) seenPins.add(order.voucherPin)
                dedupedList.add(order)
            }
        }

        dedupedList.map {
            val matchesWalletRef = walletOrderIds.contains(it.id)
            val isKnownInPrefs = isKnownWalletPurchase(it.id, it.voucherPin)
            val matchesWalletDetail = walletDebitTxs.any { tx ->
                val amountMatches = Math.abs(tx.amount - it.totalAmount) < 0.5 || Math.abs(tx.amount - it.totalCost) < 0.5
                val timeMatches = Math.abs(tx.timestamp - it.timestamp) < 180000
                val cleanPin = it.voucherPin.replace("-", "").replace(" ", "").trim()
                val pinMatches = cleanPin.isNotBlank() && cleanPin != "------" && (
                    tx.title.contains(it.voucherPin) || tx.referenceNumber.contains(it.voucherPin) ||
                    tx.title.replace("-", "").replace(" ", "").contains(cleanPin) ||
                    tx.referenceNumber.replace("-", "").replace(" ", "").contains(cleanPin)
                )
                val netMatches = (tx.networkName != null && tx.networkName.equals(it.networkName, ignoreCase = true)) || tx.title.contains(it.networkName)
                (amountMatches && timeMatches) || pinMatches || (amountMatches && netMatches && Math.abs(tx.timestamp - it.timestamp) < 300000)
            }

            val isWallet = it.paymentSource.equals("WALLET", ignoreCase = true) || isKnownInPrefs || matchesWalletRef || matchesWalletDetail

            if (isWallet) {
                recordWalletPurchase(it.id, it.voucherPin)
            }

            OrderTransaction(
                id = it.id,
                networkId = it.networkId,
                networkName = it.networkName,
                packageName = it.packageName,
                packagePrice = it.packagePrice,
                costPrice = if (it.costPrice > 0.0) it.costPrice else it.packagePrice * 0.9,
                quantity = it.quantity,
                totalAmount = it.totalAmount,
                totalCost = if (it.totalCost > 0.0) it.totalCost else (if (it.costPrice > 0.0) it.costPrice else it.packagePrice * 0.9) * it.quantity,
                customerPhone = it.customerPhone,
                voucherPin = it.voucherPin,
                timestamp = it.timestamp,
                posStoreName = it.posStoreName,
                isPrinted = it.isPrinted,
                duration = it.duration,
                dataQuota = it.dataQuota,
                validity = it.validity,
                paymentSource = if (isWallet) "WALLET" else "NETWORK_CREDIT"
            )
        }
    }

    val printerSettings: Flow<PrinterDevice?> = db.printerDao().getPrinterSettings().map { entity ->
        if (entity != null) {
            PrinterDevice(
                name = entity.printerName,
                address = entity.macAddress,
                isConnected = entity.isConnected,
                isSimulationMode = entity.isSimulationMode
            )
        } else {
            PrinterDevice(
                name = "لم يتم ربط طابعة",
                address = "",
                isConnected = false,
                isSimulationMode = false
            )
        }
    }

    val walletBalance: Flow<Double> = db.walletDao().getWalletAccount().map { entity ->
        entity?.balance ?: 0.0
    }

    val walletTransactions: Flow<List<WalletTransaction>> = combine(
        db.walletDao().getAllWalletTransactions(),
        db.orderDao().getAllOrders()
    ) { txList, ordersList ->
        val result = mutableListOf<WalletTransaction>()
        val seenKeys = mutableSetOf<String>()

        // 1. Add all direct wallet deposit/debit transactions from backend / local
        txList.forEach { tx ->
            val hasValidRef = tx.referenceNumber.isNotBlank() && tx.referenceNumber != "-"
            
            val isDuplicate = when {
                seenKeys.contains(tx.id) -> true
                hasValidRef && (seenKeys.contains(tx.referenceNumber) || seenKeys.contains("REF_${tx.referenceNumber}")) -> true
                else -> false
            }

            if (!isDuplicate) {
                seenKeys.add(tx.id)
                if (hasValidRef) {
                    seenKeys.add(tx.referenceNumber)
                    seenKeys.add("REF_${tx.referenceNumber}")
                    seenKeys.add("VOUCHER_PURCHASE_${tx.referenceNumber}")
                    seenKeys.add("tx-cb-${tx.referenceNumber}")
                }

                result.add(
                    WalletTransaction(
                        id = tx.id,
                        title = tx.title,
                        type = try { WalletTxType.valueOf(tx.type) } catch (e: Exception) { WalletTxType.DEPOSIT },
                        amount = tx.amount,
                        currency = tx.currency,
                        referenceNumber = tx.referenceNumber,
                        paymentMethod = tx.paymentMethod,
                        status = try { WalletTxStatus.valueOf(tx.status) } catch (e: Exception) { WalletTxStatus.COMPLETED },
                        timestamp = tx.timestamp,
                        networkName = tx.networkName
                    )
                )
            }
        }

        // 2. Add all orders that were purchased via WALLET if not already recorded in walletDao
        val walletOrders = ordersList.filter { it.paymentSource.equals("WALLET", ignoreCase = true) }
        
        walletOrders.forEach { order ->
            val orderKey = "VOUCHER_PURCHASE_${order.id}"
            val txCbKey = "tx-cb-${order.id}"
            val pinKey = if (order.voucherPin.isNotBlank() && order.voucherPin != "------") "VOUCHER_PURCHASE_${order.voucherPin}" else ""
            
            val isAlreadyRecorded = seenKeys.contains(order.id) ||
                    seenKeys.contains(orderKey) ||
                    seenKeys.contains(txCbKey) ||
                    seenKeys.contains("REF_${order.id}") ||
                    (pinKey.isNotBlank() && seenKeys.contains(pinKey))

            if (!isAlreadyRecorded) {
                val costAmt = if (order.totalCost > 0.0) order.totalCost else if (order.costPrice > 0.0) order.costPrice * order.quantity else order.totalAmount * 0.9
                val profitAmt = (order.totalAmount - costAmt).coerceAtLeast(0.0)
                
                seenKeys.add(order.id)
                seenKeys.add(orderKey)
                seenKeys.add(txCbKey)
                seenKeys.add("REF_${order.id}")
                if (pinKey.isNotBlank()) seenKeys.add(pinKey)

                result.add(
                    WalletTransaction(
                        id = txCbKey,
                        title = "شراء ${order.quantity} كرت (${order.packageName}) - ${order.networkName}",
                        type = WalletTxType.VOUCHER_PURCHASE,
                        amount = costAmt,
                        sellingPrice = order.totalAmount,
                        profit = profitAmt,
                        currency = "ريال",
                        referenceNumber = order.id,
                        paymentMethod = "محفظة كارد بوكس (CardBox Wallet)",
                        status = WalletTxStatus.COMPLETED,
                        timestamp = order.timestamp,
                        networkName = order.networkName
                    )
                )
            }
        }

        result.sortedByDescending { it.timestamp }
    }

    suspend fun initDefaultDataIfNeeded() = withContext(Dispatchers.IO) {
        val wallet = db.walletDao().getWalletAccount()
        if (wallet == null) {
            db.walletDao().saveWalletAccount(WalletAccountEntity(id = 1, balance = 0.0))
        }
    }

    suspend fun fetchPublicNetworks(): ApiResponse<List<NetworkItem>> {
        return apiService.fetchRealNetworks()
    }

    suspend fun syncUserProfile(): ApiResponse<PosUser> = withContext(Dispatchers.IO) {
        val res = apiService.getProfileApi()
        if (res.success && res.data != null) {
            val profile = res.data
            val phone = profile.phone?.trim() ?: ""
            val storeName = profile.shopName?.trim()?.ifBlank { profile.name?.trim() } ?: ""
            val location = profile.address?.trim() ?: "المركز الرئيسي"
            if (phone.isNotBlank()) {
                val current = db.posAccountDao().getAccountByPhone(phone)
                val pass = current?.passwordHash ?: ""
                val updatedAccount = PosAccountEntity(
                    phone = phone,
                    storeName = storeName.ifBlank { "متجر $phone" },
                    location = location.ifBlank { "المركز الرئيسي" },
                    passwordHash = pass,
                    isLoggedIn = true
                )
                db.posAccountDao().insertAccount(updatedAccount)
                ApiResponse(true, "تم تحديث الملف الشخصي", PosUser(updatedAccount.storeName, updatedAccount.phone, updatedAccount.location, true))
            } else {
                ApiResponse(false, "لم يتم العثور على رقم الهاتف في الملف الشخصي")
            }
        } else {
            ApiResponse(false, res.message)
        }
    }

    suspend fun syncSalesHistory(): ApiResponse<List<OrderTransactionEntity>> = withContext(Dispatchers.IO) {
        val res = apiService.fetchSalesHistoryApi()
        if (res.success && res.data != null && res.data.isNotEmpty()) {
            val localOrders = db.orderDao().getOrdersListSync()
            val localById = localOrders.associateBy { it.id }
            val localByPin = localOrders.filter { it.voucherPin.isNotBlank() && it.voucherPin != "------" }
                .associateBy { it.voucherPin }

            val walletTxs = db.walletDao().getWalletTransactionsListSync()
            val walletPurchasesOrderIds = walletTxs.filter {
                it.type == WalletTxType.VOUCHER_PURCHASE.name ||
                it.paymentMethod.contains("محفظة") ||
                it.paymentMethod.contains("CardBox")
            }.map { it.referenceNumber }.toSet()

            val serverOrders = res.data
            // Sort by timestamp ascending (earliest to latest)
            val sortedAsc = serverOrders.sortedWith(
                compareBy<com.example.data.local.OrderTransactionEntity> { it.timestamp }
                    .thenBy { it.id.toLongOrNull() ?: 0L }
            )
            
            val classifiedOrders = sortedAsc.map { serverOrder ->
                val localOrder = localById[serverOrder.id]
                    ?: if (serverOrder.voucherPin.isNotBlank() && serverOrder.voucherPin != "------") localByPin[serverOrder.voucherPin] else null

                val localPaymentSource = localOrder?.paymentSource

                val costToDebit = if (serverOrder.totalCost > 0.0) serverOrder.totalCost 
                                  else if (serverOrder.costPrice > 0.0) serverOrder.costPrice * serverOrder.quantity 
                                  else serverOrder.totalAmount * 0.9

                // ✅ PRIORITY 1: Local record is the absolute source of truth.
                val finalPaymentSource = if (!localPaymentSource.isNullOrBlank()) {
                    localPaymentSource
                } else {
                    // If no local record exists (e.g. app data was cleared), use heuristics
                    val serverIsExplicitlyWallet = serverOrder.paymentSource.equals("WALLET", ignoreCase = true)
                    val isKnownInPrefs = isKnownWalletPurchase(serverOrder.id, serverOrder.voucherPin)

                    val isWalletHeuristic = serverIsExplicitlyWallet ||
                        isKnownInPrefs ||
                        walletPurchasesOrderIds.contains(serverOrder.id) ||
                        walletTxs.any { tx ->
                            val cleanServerPin = serverOrder.voucherPin.replace("-", "").replace(" ", "").trim()
                            val hasServerPin = cleanServerPin.isNotBlank() && cleanServerPin != "------"
                            val pinMatch = hasServerPin && (
                                tx.title.contains(serverOrder.voucherPin) ||
                                tx.referenceNumber.contains(serverOrder.voucherPin) ||
                                tx.title.replace("-", "").replace(" ", "").contains(cleanServerPin) ||
                                tx.referenceNumber.replace("-", "").replace(" ", "").contains(cleanServerPin)
                            )
                            (tx.type == WalletTxType.VOUCHER_PURCHASE.name || tx.paymentMethod.contains("محفظة") || tx.paymentMethod.contains("CardBox")) && (
                                tx.referenceNumber == serverOrder.id || pinMatch
                            )
                        }

                    if (isWalletHeuristic) "WALLET" else "NETWORK_CREDIT"
                }
                
                val isWallet = finalPaymentSource.equals("WALLET", ignoreCase = true)

                if (isWallet) {
                    recordWalletPurchase(serverOrder.id, serverOrder.voucherPin)
                    if (localOrder != null) {
                        recordWalletPurchase(localOrder.id, localOrder.voucherPin)
                    }
                }

                // Preserve local printed state and metadata
                val isPrinted = localOrder?.isPrinted ?: serverOrder.isPrinted
                val customerPhone = if (!serverOrder.customerPhone.isNullOrBlank()) serverOrder.customerPhone else localOrder?.customerPhone
                val posStoreName = if (serverOrder.posStoreName.isNotBlank() && serverOrder.posStoreName != "نقطة البيع") serverOrder.posStoreName else (localOrder?.posStoreName ?: serverOrder.posStoreName)

                // Only create/update wallet transactions if this server order isn't just a tiny piece of a local grouped order
                val isPartOfLocalGroup = localOrders.any { l -> l.quantity > 1 && l.voucherPin.contains("|") && l.voucherPin.contains(serverOrder.voucherPin) && serverOrder.voucherPin.isNotBlank() }

                if (finalPaymentSource == "WALLET" && !isPartOfLocalGroup) {
                    val existingTx = walletTxs.find {
                        it.referenceNumber == serverOrder.id ||
                        (localOrder != null && it.referenceNumber == localOrder.id) ||
                        (serverOrder.voucherPin.isNotBlank() && serverOrder.voucherPin != "------" && (it.title.contains(serverOrder.voucherPin) || it.referenceNumber.contains(serverOrder.voucherPin)))
                    }
                    if (existingTx == null) {
                        val txId = "tx-cb-sync-${serverOrder.id}"
                        db.walletDao().insertTransaction(
                            WalletTransactionEntity(
                                id = txId,
                                title = "شراء ${serverOrder.quantity} كرت (${serverOrder.packageName}) - كود: ${serverOrder.voucherPin} - ${serverOrder.networkName}",
                                type = WalletTxType.VOUCHER_PURCHASE.name,
                                amount = if (serverOrder.totalCost > 0) serverOrder.totalCost else costToDebit,
                                currency = "ريال",
                                referenceNumber = serverOrder.id,
                                paymentMethod = "محفظة كارد بوكس (CardBox Wallet)",
                                status = WalletTxStatus.COMPLETED.name,
                                timestamp = serverOrder.timestamp,
                                networkName = serverOrder.networkName
                            )
                        )
                    } else if (existingTx.referenceNumber != serverOrder.id && existingTx.id != "tx-cb-sync-${serverOrder.id}") {
                        // Safely link the existing transaction but don't overwrite its primary properties unnecessarily
                        db.walletDao().insertTransaction(existingTx.copy(referenceNumber = serverOrder.id))
                    }
                }

                serverOrder.copy(
                    paymentSource = finalPaymentSource,
                    isPrinted = isPrinted,
                    customerPhone = customerPhone,
                    posStoreName = posStoreName
                )
            }

            // ✅ DEDUPLICATION LOGIC:
            // If the local database has a perfectly grouped "PIN1 | PIN2" order, we keep it and DROP the split server orders!
            val localMultiPins = localOrders.filter { it.quantity > 1 && it.voucherPin.contains("|") }.map { it.voucherPin }
            val filteredServerOrders = classifiedOrders.filterNot { serverOrder ->
                serverOrder.voucherPin.isNotBlank() && serverOrder.voucherPin != "------" && localMultiPins.any { it.contains(serverOrder.voucherPin) }
            }

            val serverPins = filteredServerOrders.mapNotNull { it.voucherPin.takeIf { p -> p.isNotBlank() && p != "------" } }.toSet()
            val serverIds = filteredServerOrders.map { it.id }.toSet()
            
            val pendingLocalOrders = localOrders.filter { local ->
                val hasExactServerMatch = serverIds.contains(local.id) || (local.voucherPin.isNotBlank() && local.voucherPin != "------" && serverPins.contains(local.voucherPin))
                !hasExactServerMatch
            }

            db.orderDao().clearAll()
            db.orderDao().insertAll(filteredServerOrders + pendingLocalOrders)
            
            val allOrders = filteredServerOrders + pendingLocalOrders
            val distinctNetworkIds = allOrders.map { it.networkId }.filter { it.isNotBlank() }.distinct()
            for (netId in distinctNetworkIds) {
                val alreadyExists = db.joinedNetworkDao().getNetworkById(netId)
                if (alreadyExists == null) {
                    // Find most recent order for this network to get its name
                    val sample = allOrders.filter { it.networkId == netId }.maxByOrNull { it.timestamp }
                    if (sample != null) {
                        db.joinedNetworkDao().insertOrUpdateNetwork(
                            com.example.data.local.JoinedNetworkEntity(
                                id = netId,
                                code = netId,
                                name = sample.networkName,
                                ownerName = "",
                                financialCeiling = 0.0,
                                currentBalance = 0.0,
                                currency = "ريال",
                                status = "APPROVED",
                                location = "",
                                packagesCount = 0
                            )
                        )
                    }
                }
            }

            ApiResponse(true, "تم جلب وتحديث سجل المبيعات بنجاح", classifiedOrders + pendingLocalOrders)
        } else {
            res
        }
    }

    suspend fun syncWalletBalance(): ApiResponse<Double> = withContext(Dispatchers.IO) {
        try {
            val walletRes = apiService.fetchWalletDataApi()
            if (walletRes.success && walletRes.data != null) {
                val (balance, serverTxs) = walletRes.data
                db.walletDao().saveWalletAccount(WalletAccountEntity(id = 1, balance = balance))

                if (serverTxs.isNotEmpty()) {
                    db.walletDao().insertAll(serverTxs)
                }

                ApiResponse(true, "تم مزامنة الرصيد وسجل المحفظة بنجاح", data = balance)
            } else {
                ApiResponse(false, walletRes.message.ifBlank { "تعذر مزامنة بيانات المحفظة" })
            }
        } catch (e: Exception) {
            ApiResponse(false, "فشل الاتصال بالسيرفر لمزامنة الرصيد: ${e.localizedMessage}")
        }
    }

    suspend fun syncAllDataFromServer(): ApiResponse<Boolean> = withContext(Dispatchers.IO) {
        if (RetrofitClient.authToken.isNullOrBlank()) {
            return@withContext ApiResponse(false, "غير مسجل الدخول")
        }
        try {
            syncUserProfile()
            fetchMyNetworks()
            fetchPublicNetworks()
            syncWalletBalance()
            syncSalesHistory()
            ApiResponse(true, "تمت مزامنة جميع البيانات بنجاح من السيرفر")
        } catch (e: Exception) {
            ApiResponse(false, "حدث خطأ أثناء المزامنة: ${e.localizedMessage}")
        }
    }

    suspend fun fetchMyNetworks(): ApiResponse<List<NetworkItem>> = withContext(Dispatchers.IO) {
        val res = apiService.fetchMyNetworks()
        if (res.success) {
            db.joinedNetworkDao().clearAll()
            val networks = res.data ?: emptyList()
            if (networks.isNotEmpty()) {
                val entities = networks.map { net ->
                    JoinedNetworkEntity(
                        id = net.id,
                        code = net.code,
                        name = net.name,
                        ownerName = net.ownerName,
                        financialCeiling = net.financialCeiling,
                        currentBalance = net.currentBalance,
                        currency = net.currency,
                        status = net.status.name,
                        location = net.location,
                        packagesCount = net.packagesCount
                    )
                }
                db.joinedNetworkDao().insertAll(entities)
            }
        }
        res
    }

    suspend fun registerAccount(
        ownerName: String,
        storeName: String,
        phone: String,
        location: String,
        password: String
    ): ApiResponse<String> = withContext(Dispatchers.IO) {
        if (ownerName.isBlank() || storeName.isBlank() || phone.isBlank() || password.isBlank()) {
            return@withContext ApiResponse(false, "يرجى تعبئة جميع الحقول المطلوبة بشكل صحيح.")
        }

        val apiRes = apiService.registerApi(
            name = ownerName.trim(),
            shopName = storeName.trim(),
            phone = phone.trim(),
            pass = password,
            address = location.ifBlank { "العنوان الرئيسي" }
        )

        if (!apiRes.success) {
            return@withContext ApiResponse(false, apiRes.message)
        }

        val testOtpCode = apiRes.data ?: ""
        ApiResponse(
            success = true,
            message = apiRes.message.ifBlank { "تم تسجيل الحساب في السيرفر بنجاح، يرجى إدخال رمز التحقق OTP (6 أرقام)" },
            data = testOtpCode
        )
    }

    suspend fun registerAccount(storeName: String, phone: String, location: String, password: String): ApiResponse<String> =
        registerAccount(
            ownerName = storeName,
            storeName = storeName,
            phone = phone,
            location = location,
            password = password
        )

    suspend fun verifyAccountOtp(phone: String, storeName: String, location: String, pass: String, otpCode: String): ApiResponse<PosUser> = withContext(Dispatchers.IO) {
        if (otpCode.isBlank() || otpCode.length != 6) {
            return@withContext ApiResponse(false, "يرجى إدخال كود التحقق OTP المكون من 6 أرقام بشكل صحيح.")
        }

        val apiRes = apiService.verifyOtpApi(phone, otpCode)
        if (!apiRes.success) {
            return@withContext ApiResponse(false, apiRes.message)
        }

        val token = apiRes.data ?: ""
        if (token.isNotBlank()) {
            context?.let { ctx -> RetrofitClient.saveToken(ctx, token) }
        }

        // تفريغ كافة بيانات الجلسة السابقة لضمان عدم تداخل بيانات الحسابات
        clearUserSessionData()

        val newAccount = PosAccountEntity(
            phone = phone,
            storeName = storeName,
            location = location,
            passwordHash = pass,
            isLoggedIn = true
        )
        db.posAccountDao().insertAccount(newAccount)

        // جلب ومزامنة كل البيانات الخاصة بالحساب الجديد من السيرفر مباشرة
        syncAllDataFromServer()

        val updatedAccount = db.posAccountDao().getAccountByPhone(phone) ?: newAccount

        ApiResponse(
            success = true,
            message = "تم تفعيل الحساب وتسجيل الدخول بنجاح!",
            data = PosUser(updatedAccount.storeName, updatedAccount.phone, updatedAccount.location, true)
        )
    }

    suspend fun login(phone: String, password: String): ApiResponse<PosUser> = withContext(Dispatchers.IO) {
        val apiRes = apiService.loginApi(phone, password)
        if (!apiRes.success) {
            return@withContext ApiResponse(false, apiRes.message)
        }

        // تفريغ كافة بيانات الحساب السابق بالكامل لضمان عزلة البيانات بين الحسابات
        clearUserSessionData()

        val loginToken = apiRes.data ?: ""
        if (loginToken.isNotBlank()) {
            context?.let { ctx -> RetrofitClient.saveToken(ctx, loginToken) }
        }

        var account = db.posAccountDao().getAccountByPhone(phone)
        if (account == null) {
            val newAccount = PosAccountEntity(
                phone = phone,
                storeName = "متجر $phone",
                location = "المركز الرئيسي",
                passwordHash = password,
                isLoggedIn = true
            )
            db.posAccountDao().insertAccount(newAccount)
            account = newAccount
        }

        db.posAccountDao().setLoggedIn(phone)

        // جلب ومزامنة كل البيانات الخاصة بالحساب الجديد من السيرفر فوراً
        syncAllDataFromServer()

        val updatedAccount = db.posAccountDao().getAccountByPhone(phone) ?: account

        ApiResponse(
            success = true,
            message = "تم تسجيل الدخول بنجاح مع سيرفر POS!",
            data = PosUser(updatedAccount.storeName, updatedAccount.phone, updatedAccount.location, true)
        )
    }

    suspend fun clearUserSessionData() = withContext(Dispatchers.IO) {
        try {
            db.posAccountDao().logoutAll()
            db.joinedNetworkDao().clearAll()
            db.orderDao().clearAll()
            db.walletDao().clearWalletAccount()
            db.walletDao().clearTransactions()
            db.notificationDao().clearAllNotifications()
            context?.let { ctx -> RetrofitClient.clearToken(ctx) }
        } catch (e: Exception) {
            // Ignore if already clean
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        clearUserSessionData()
    }

    suspend fun updateProfileInfo(phone: String, newStoreName: String, newLocation: String): ApiResponse<PosUser> = withContext(Dispatchers.IO) {
        if (newStoreName.isBlank()) {
            return@withContext ApiResponse(false, "يرجى كتابة اسم متجر صحيح.")
        }
        
        // Fetch current user from local DB to get name if any, or default to store name
        val account = db.posAccountDao().getAccountByPhone(phone)
        val nameToSend = account?.storeName ?: newStoreName

        // Hit the network endpoint to keep server in sync
        val apiRes = apiService.updateProfileApi(
            name = nameToSend,
            shopName = newStoreName,
            phone = phone,
            address = newLocation
        )
        if (!apiRes.success) {
            return@withContext ApiResponse(false, apiRes.message)
        }

        db.posAccountDao().updateAccountInfo(phone, newStoreName, newLocation)
        ApiResponse(
            success = true,
            message = "تم تحديث بيانات الحساب والمتجر بنجاح!",
            data = PosUser(newStoreName, phone, newLocation, true)
        )
    }

    suspend fun changePassword(phone: String, oldPass: String, newPass: String): ApiResponse<Boolean> = withContext(Dispatchers.IO) {
        val account = db.posAccountDao().getAccountByPhone(phone)
            ?: return@withContext ApiResponse(false, "الحساب غير موجود.")
        if (account.passwordHash != oldPass) {
            return@withContext ApiResponse(false, "كلمة المرور الحالية غير صحيحة.")
        }
        if (newPass.length < 6) {
            return@withContext ApiResponse(false, "كلمة المرور الجديدة يجب ألا تقل عن 6 أحرف.")
        }
        
        val apiRes = apiService.changePasswordApi(oldPass, newPass)
        if (!apiRes.success) {
            return@withContext ApiResponse(false, apiRes.message)
        }

        db.posAccountDao().updatePassword(phone, newPass)
        ApiResponse(true, "تم تغيير كلمة المرور بنجاح!")
    }

    suspend fun requestForgotPasswordOtp(phone: String): ApiResponse<String> = withContext(Dispatchers.IO) {
        if (phone.isBlank()) {
            return@withContext ApiResponse(false, "يرجى إدخال رقم الجوال")
        }
        apiService.forgotPasswordApi(phone.trim())
    }

    suspend fun resetPasswordWithOtp(phone: String, otpCode: String, newPass: String): ApiResponse<Boolean> = withContext(Dispatchers.IO) {
        if (phone.isBlank()) {
            return@withContext ApiResponse(false, "يرجى إدخال رقم الجوال.")
        }
        if (otpCode.isBlank() || otpCode.length != 6) {
            return@withContext ApiResponse(false, "كود التحقق غير صحيح، يجب أن يتكون من 6 أرقام.")
        }
        if (newPass.length < 6) {
            return@withContext ApiResponse(false, "كلمة المرور الجديدة يجب ألا تقل عن 6 أحرف.")
        }
        
        val apiRes = apiService.resetPasswordApi(phone.trim(), otpCode.trim(), newPass)
        if (!apiRes.success) {
            return@withContext ApiResponse(false, apiRes.message)
        }

        val account = db.posAccountDao().getAccountByPhone(phone.trim())
        if (account != null) {
            db.posAccountDao().updatePassword(phone.trim(), newPass)
        }
        ApiResponse(true, apiRes.message.ifBlank { "تم إعادة تعيين كلمة المرور بنجاح" })
    }

    suspend fun deleteAccount(phone: String): ApiResponse<Boolean> = withContext(Dispatchers.IO) {
        db.posAccountDao().deleteAccountByPhone(phone)
        db.posAccountDao().logoutAll()
        db.joinedNetworkDao().clearAll()
        context?.let { ctx -> RetrofitClient.clearToken(ctx) }
        ApiResponse(true, "تم حذف حساب نقطة البيع وجميع البيانات بنجاح.")
    }

    suspend fun trackNetworkInDb(network: NetworkItem) = withContext(Dispatchers.IO) {
        val entity = JoinedNetworkEntity(
            id = network.id,
            code = network.code,
            name = network.name,
            ownerName = network.ownerName,
            financialCeiling = network.financialCeiling,
            currentBalance = network.currentBalance,
            currency = network.currency,
            status = network.status.name,
            location = network.location,
            packagesCount = network.packagesCount
        )
        db.joinedNetworkDao().insertOrUpdateNetwork(entity)
    }

    suspend fun refreshMyNetworksFromApi(): ApiResponse<List<NetworkItem>> = withContext(Dispatchers.IO) {
        val res = apiService.fetchMyNetworks()
        if (res.success && res.data != null) {
            val entities = res.data.map { net ->
                JoinedNetworkEntity(
                    id = net.id,
                    code = net.code,
                    name = net.name,
                    ownerName = net.ownerName,
                    financialCeiling = net.financialCeiling,
                    currentBalance = net.currentBalance,
                    currency = net.currency,
                    status = net.status.name,
                    location = net.location,
                    packagesCount = net.packagesCount
                )
            }
            if (entities.isNotEmpty()) {
                db.joinedNetworkDao().clearAll()
                db.joinedNetworkDao().insertAll(entities)
            }
        }
        res
    }

    suspend fun searchNetwork(code: String): ApiResponse<NetworkItem> = withContext(Dispatchers.IO) {
        val rawQuery = code.trim()
        if (rawQuery.isBlank()) {
            return@withContext ApiResponse(false, "يرجى كتابة نص للبحث", data = null)
        }

        // Query server directly to get fresh network metadata from server
        val serverRes = apiService.searchNetworkByCode(rawQuery)
        if (serverRes.success && serverRes.data != null) {
            return@withContext serverRes
        }

        fun norm(s: String): String {
            return s.lowercase()
                .replace(Regex("[أإآٱ]"), "ا")
                .replace(Regex("[ة]"), "ه")
                .replace(Regex("[ى]"), "ي")
                .replace(Regex("[ؤ]"), "و")
                .replace(Regex("[ئ]"), "ي")
                .replace(Regex("[\\u064B-\\u065F\\u0670]"), "")
                .replace(Regex("[\\s\\-_.]"), "")
        }

        val qNorm = norm(rawQuery)
        val localJoined = db.joinedNetworkDao().getAllNetworksList()
        val matchLocal = localJoined.find { net ->
            val nCode = norm(net.code)
            val nName = norm(net.name)
            val nOwner = norm(net.ownerName)
            val nLoc = norm(net.location)

            nCode.contains(qNorm) ||
            nName.contains(qNorm) ||
            nOwner.contains(qNorm) ||
            nLoc.contains(qNorm) ||
            net.code.equals(rawQuery, ignoreCase = true) ||
            net.id == rawQuery
        }
        if (matchLocal != null) {
            val net = NetworkItem(
                id = matchLocal.id,
                code = matchLocal.code,
                name = matchLocal.name,
                ownerName = matchLocal.ownerName,
                financialCeiling = matchLocal.financialCeiling,
                currentBalance = matchLocal.currentBalance,
                currency = matchLocal.currency,
                status = try { JoinStatus.valueOf(matchLocal.status) } catch (e: Exception) { JoinStatus.APPROVED },
                location = matchLocal.location,
                packagesCount = matchLocal.packagesCount,
                description = "الموقع: ${matchLocal.location}"
            )
            return@withContext ApiResponse(true, "تم العثور على تفاصيل الشبكة بنجاح", data = net)
        }

        serverRes
    }

    suspend fun requestJoinNetwork(network: NetworkItem, storeName: String, storePhone: String): ApiResponse<NetworkItem> = withContext(Dispatchers.IO) {
        val res = apiService.requestJoinNetwork(network.id)
        if (res.data != null) {
            val net = res.data
            val entity = JoinedNetworkEntity(
                id = network.id,
                code = network.code,
                name = network.name,
                ownerName = network.ownerName,
                financialCeiling = if (net.financialCeiling > 0) net.financialCeiling else network.financialCeiling,
                currentBalance = if (net.currentBalance > 0) net.currentBalance else network.currentBalance,
                currency = network.currency,
                status = net.status.name,
                location = network.location,
                packagesCount = network.packagesCount
            )
            db.joinedNetworkDao().insertOrUpdateNetwork(entity)
        }
        res
    }

    suspend fun getVoucherPackages(networkId: String): ApiResponse<List<VoucherPackage>> {
        return apiService.getVoucherPackages(networkId)
    }

    suspend fun purchaseVouchers(
        network: NetworkItem,
        packageItem: VoucherPackage,
        quantity: Int,
        customerPhone: String?,
        storeName: String
    ): ApiResponse<OrderTransaction> = withContext(Dispatchers.IO) {

        val res = apiService.purchaseVouchers(
            networkId = network.id,
            packageItem = packageItem,
            quantity = quantity,
            currentCeilingBalance = network.currentBalance,
            customerPhone = customerPhone
        )

        if (!res.success || res.data == null) {
            return@withContext ApiResponse(false, res.message)
        }

        val result: PurchaseResult = res.data
        // Update Room local balance for this network
        db.joinedNetworkDao().updateBalance(network.id, result.newBalance)

        val costPrice = packageItem.posPrice
        val totalCost = costPrice * quantity
        val totalSelling = packageItem.price * quantity

        // Save Order transaction record
        val order = OrderTransaction(
            id = result.orderId,
            networkId = network.id,
            networkName = network.name,
            packageName = packageItem.name,
            packagePrice = packageItem.price,
            costPrice = costPrice,
            quantity = quantity,
            totalAmount = totalSelling,
            totalCost = totalCost,
            customerPhone = customerPhone,
            voucherPin = result.voucherPin,
            timestamp = result.timestamp,
            posStoreName = storeName,
            isPrinted = false,
            duration = packageItem.duration,
            dataQuota = packageItem.dataQuota,
            validity = packageItem.validity,
            paymentSource = "NETWORK_CREDIT"
        )

        db.orderDao().insertOrder(
            OrderTransactionEntity(
                id = order.id,
                networkId = order.networkId,
                networkName = order.networkName,
                packageName = order.packageName,
                packagePrice = order.packagePrice,
                costPrice = order.costPrice,
                quantity = order.quantity,
                totalAmount = order.totalAmount,
                totalCost = order.totalCost,
                customerPhone = order.customerPhone,
                voucherPin = order.voucherPin,
                timestamp = order.timestamp,
                posStoreName = order.posStoreName,
                isPrinted = order.isPrinted,
                duration = order.duration,
                dataQuota = order.dataQuota,
                validity = order.validity,
                paymentSource = "NETWORK_CREDIT"
            )
        )

        // Trigger background sync with server to ensure all balances and networks are 100% up-to-date
        try {
            fetchMyNetworks()
            syncSalesHistory()
        } catch (e: Exception) {
            // Non-blocking sync
        }

        ApiResponse(
            success = true,
            message = "تمت عملية الشراء بنجاح وترحيل الرصيد من السقف المالي المتاح",
            data = order
        )
    }

    suspend fun savePrinterSettings(device: PrinterDevice) = withContext(Dispatchers.IO) {
        db.printerDao().savePrinterSettings(
            PrinterSettingsEntity(
                id = 1,
                printerName = device.name,
                macAddress = device.address,
                isConnected = device.isConnected,
                isSimulationMode = device.isSimulationMode
            )
        )
    }

    suspend fun markOrderPrinted(orderId: String) = withContext(Dispatchers.IO) {
        db.orderDao().markPrinted(orderId)
    }

    suspend fun topUpWalletBalance(
        amount: Double,
        paymentMethodName: String,
        referenceNumber: String
    ): ApiResponse<Double> = withContext(Dispatchers.IO) {
        if (amount <= 0) {
            return@withContext ApiResponse(false, "يرجى إدخال مبلغ إيداع صحيح.")
        }
        if (referenceNumber.isBlank()) {
            return@withContext ApiResponse(false, "يرجى إدخال الرقم المرجعي أو رقم العملية بشكل صحيح للتحقق.")
        }

        // Call the server API for wallet recharge
        val apiRes = apiService.topUpWalletApi(amount, paymentMethodName, referenceNumber)
        if (!apiRes.success) {
            return@withContext ApiResponse(false, apiRes.message)
        }

        // Sync real balance and transactions from server
        val syncRes = syncWalletBalance()
        val updatedBalance = syncRes.data ?: 0.0

        // Only insert a local record if the server hasn't already returned it in recentTransactions
        val existingTxs = db.walletDao().getWalletTransactionsListSync()
        val alreadyRecorded = existingTxs.any {
            it.referenceNumber == referenceNumber ||
            (it.type == WalletTxType.DEPOSIT.name && Math.abs(it.amount - amount) < 0.01 && Math.abs(it.timestamp - System.currentTimeMillis()) < 120000)
        }

        if (!alreadyRecorded) {
            val txId = "tx-${System.currentTimeMillis()}"
            val tx = WalletTransactionEntity(
                id = txId,
                title = "تغذية حساب عبر $paymentMethodName",
                type = WalletTxType.DEPOSIT.name,
                amount = amount,
                currency = "ريال",
                referenceNumber = referenceNumber,
                paymentMethod = paymentMethodName,
                status = WalletTxStatus.COMPLETED.name,
                timestamp = System.currentTimeMillis(),
                networkName = null
            )
            db.walletDao().insertTransaction(tx)
        }

        ApiResponse(
            success = true,
            message = apiRes.message.ifBlank { "تم التغذية بنجاح! رصيدك الجديد: ${updatedBalance.toInt()} ريال." },
            data = updatedBalance
        )
    }

    suspend fun purchaseVouchersWithWallet(
        network: NetworkItem,
        packageItem: VoucherPackage,
        quantity: Int,
        customerPhone: String?,
        storeName: String,
        currentWalletBalance: Double
    ): ApiResponse<OrderTransaction> = withContext(Dispatchers.IO) {
        val totalCost = packageItem.posPrice * quantity
        if (currentWalletBalance < totalCost) {
            return@withContext ApiResponse(
                success = false,
                message = "لا يوجد رصيد كافٍ في محفظة كارد بوكس. رصيدك الحالي: ${currentWalletBalance.toInt()} ريال، المطلوب: ${totalCost.toInt()} ريال. يرجى تغذية رصيد حسابك."
            )
        }

        // We must call the server to do the actual purchase.
        // It will deduct from the wallet or network credit on the server side.
        val res = apiService.purchaseVouchers(
            networkId = network.id,
            packageItem = packageItem,
            quantity = quantity,
            currentCeilingBalance = currentWalletBalance, // Use wallet balance here
            customerPhone = customerPhone,
            paymentMethod = "wallet"
        )

        if (!res.success || res.data == null) {
            return@withContext ApiResponse(false, res.message)
        }

        val result: PurchaseResult = res.data
        // Update local wallet balance 
        val newWalletBalance = (currentWalletBalance - totalCost).coerceAtLeast(0.0)
        db.walletDao().saveWalletAccount(WalletAccountEntity(id = 1, balance = newWalletBalance))

        val costPrice = packageItem.posPrice
        val totalSelling = packageItem.price * quantity

        val order = OrderTransaction(
            id = result.orderId,
            networkId = network.id,
            networkName = network.name,
            packageName = packageItem.name,
            packagePrice = packageItem.price,
            costPrice = costPrice,
            quantity = quantity,
            totalAmount = totalSelling,
            totalCost = totalCost,
            customerPhone = customerPhone,
            voucherPin = result.voucherPin,
            timestamp = result.timestamp,
            posStoreName = storeName,
            isPrinted = false,
            duration = packageItem.duration,
            dataQuota = packageItem.dataQuota,
            validity = packageItem.validity,
            paymentSource = "WALLET"
        )

        db.orderDao().insertOrder(
            OrderTransactionEntity(
                id = order.id,
                networkId = order.networkId,
                networkName = order.networkName,
                packageName = order.packageName,
                packagePrice = order.packagePrice,
                costPrice = order.costPrice,
                quantity = order.quantity,
                totalAmount = order.totalAmount,
                totalCost = order.totalCost,
                customerPhone = order.customerPhone,
                voucherPin = order.voucherPin,
                timestamp = order.timestamp,
                posStoreName = order.posStoreName,
                isPrinted = order.isPrinted,
                duration = order.duration,
                dataQuota = order.dataQuota,
                validity = order.validity,
                paymentSource = "WALLET"
            )
        )

        // Record Wallet Transaction record for this card purchase
        val txId = "tx-cb-${System.currentTimeMillis()}"
        db.walletDao().insertTransaction(
            WalletTransactionEntity(
                id = txId,
                title = "شراء ${quantity} كرت (${packageItem.name}) - ${network.name}",
                type = WalletTxType.VOUCHER_PURCHASE.name,
                amount = totalCost,
                currency = "ريال",
                referenceNumber = order.id,
                paymentMethod = "محفظة كارد بوكس (CardBox Wallet)",
                status = WalletTxStatus.COMPLETED.name,
                timestamp = result.timestamp,
                networkName = network.name
            )
        )

        // Ensure the network appears in My Networks locally (even if not formally joined)
        val existingNet = db.joinedNetworkDao().getNetworkById(network.id)
        if (existingNet == null) {
            db.joinedNetworkDao().insertOrUpdateNetwork(
                com.example.data.local.JoinedNetworkEntity(
                    id = network.id,
                    code = network.code,
                    name = network.name,
                    ownerName = network.ownerName,
                    financialCeiling = network.financialCeiling,
                    currentBalance = network.currentBalance,
                    currency = network.currency,
                    status = network.status.name,
                    location = network.location,
                    packagesCount = network.packagesCount
                )
            )
        }

        // Trigger background sync with server to ensure wallet, sales, and networks are 100% up-to-date
        try {
            syncWalletBalance()
            fetchMyNetworks()
            syncSalesHistory()
        } catch (e: Exception) {
            // Non-blocking sync
        }

        ApiResponse(
            success = true,
            message = "تم خصم الإجمالي (${totalCost.toInt()} ريال) من محفظة كارد بوكس وشراء الكرت بنجاح!",
            data = order
        )
    }

    // --- Notifications logic ---
    val notifications: Flow<List<AppNotification>> = db.notificationDao().getAllNotifications().map { list ->
        list.map {
            AppNotification(
                id = it.id,
                title = it.title,
                message = it.message,
                type = try { NotificationType.valueOf(it.type) } catch (e: Exception) { NotificationType.SYSTEM_ANNOUNCEMENT },
                timestamp = it.timestamp,
                isRead = it.isRead,
                relatedEntityId = it.relatedEntityId,
                amount = it.amount
            )
        }
    }

    val unreadNotificationsCount: Flow<Int> = db.notificationDao().getUnreadCount()

    suspend fun addNotification(
        title: String,
        message: String,
        type: NotificationType,
        relatedEntityId: String? = null,
        amount: Double? = null,
        context: Context? = null
    ) = withContext(Dispatchers.IO) {
        val id = "notif-${System.currentTimeMillis()}-${(1000..9999).random()}"
        val entity = AppNotificationEntity(
            id = id,
            title = title,
            message = message,
            type = type.name,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            relatedEntityId = relatedEntityId,
            amount = amount
        )
        db.notificationDao().insertNotification(entity)

        context?.let { ctx ->
            AppNotificationManager.showSystemNotification(
                context = ctx,
                notificationId = (id.hashCode() and 0x7FFFFFFF),
                title = title,
                message = message,
                type = type
            )
        }
    }

    suspend fun markNotificationRead(id: String) = withContext(Dispatchers.IO) {
        db.notificationDao().markAsRead(id)
    }

    suspend fun markAllNotificationsRead() = withContext(Dispatchers.IO) {
        db.notificationDao().markAllAsRead()
    }

    suspend fun deleteNotification(id: String) = withContext(Dispatchers.IO) {
        db.notificationDao().deleteNotification(id)
    }

    suspend fun clearAllNotifications() = withContext(Dispatchers.IO) {
        db.notificationDao().clearAllNotifications()
    }

    suspend fun seedDefaultNotificationsIfEmpty(context: Context? = null) = withContext(Dispatchers.IO) {
        // No fake notifications seeded
    }
}



