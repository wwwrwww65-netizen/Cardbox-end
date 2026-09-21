package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.remote.MikroTikApiService
import com.example.data.repository.PosRepository
import com.example.printer.ThermalPrinterManager
import com.example.ui.components.AlertType
import com.example.ui.components.InAppAlert
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PosViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val apiService = MikroTikApiService()
    val repository = PosRepository(db, apiService, application)
    val printerManager = ThermalPrinterManager(application)

    private val prefs = application.getSharedPreferences("cardbox_pos_prefs", android.content.Context.MODE_PRIVATE)

    fun isFirstRun(): Boolean = prefs.getBoolean("is_first_run", true)

    fun setFirstRunCompleted() {
        prefs.edit().putBoolean("is_first_run", false).apply()
    }

    private val _themeMode = MutableStateFlow(
        runCatching {
            com.example.ui.theme.ThemeMode.valueOf(prefs.getString("theme_mode", com.example.ui.theme.ThemeMode.SYSTEM.name) ?: com.example.ui.theme.ThemeMode.SYSTEM.name)
        }.getOrDefault(com.example.ui.theme.ThemeMode.SYSTEM)
    )
    val themeMode: StateFlow<com.example.ui.theme.ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: com.example.ui.theme.ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun toggleThemeMode() {
        val nextMode = when (_themeMode.value) {
            com.example.ui.theme.ThemeMode.LIGHT -> com.example.ui.theme.ThemeMode.DARK
            com.example.ui.theme.ThemeMode.DARK -> com.example.ui.theme.ThemeMode.SYSTEM
            com.example.ui.theme.ThemeMode.SYSTEM -> com.example.ui.theme.ThemeMode.LIGHT
        }
        setThemeMode(nextMode)
    }

    val currentUser = repository.loggedInUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val joinedNetworks = repository.joinedNetworks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _publicNetworks = MutableStateFlow<List<NetworkItem>>(emptyList())
    val publicNetworks: StateFlow<List<NetworkItem>> = _publicNetworks.asStateFlow()

    fun fetchPublicNetworks() {
        viewModelScope.launch {
            val res = repository.fetchPublicNetworks()
            if (res.success && res.data != null) {
                _publicNetworks.value = res.data
            }
        }
    }

    fun fetchMyNetworks() {
        viewModelScope.launch {
            repository.fetchMyNetworks()
        }
    }

    private val _pinnedNetworkIds = MutableStateFlow<Set<String>>(emptySet())
    val pinnedNetworkIds: StateFlow<Set<String>> = _pinnedNetworkIds.asStateFlow()

    private val _networkCustomOrder = MutableStateFlow<List<String>>(emptyList())
    val networkCustomOrder: StateFlow<List<String>> = _networkCustomOrder.asStateFlow()

    val sortedJoinedNetworks: StateFlow<List<NetworkItem>> = combine(
        repository.joinedNetworks,
        _pinnedNetworkIds,
        _networkCustomOrder
    ) { joinedList, pinnedSet, customOrder ->
        val joinedApproved = joinedList.filter { it.status == JoinStatus.APPROVED }

        // Deduplicate strictly by ID and Code to ensure absolutely no duplicates
        val uniqueMap = linkedMapOf<String, NetworkItem>()
        joinedApproved.forEach { net ->
            val existing = uniqueMap.values.find { 
                it.id == net.id || (it.code.isNotBlank() && it.code.equals(net.code, ignoreCase = true))
            }
            if (existing == null) {
                uniqueMap[net.id] = net
            }
        }

        uniqueMap.values.map { net ->
            net.copy(isPinned = pinnedSet.contains(net.id))
        }.sortedWith(
            compareByDescending<NetworkItem> { it.isPinned }
                .thenBy { net ->
                    val idx = customOrder.indexOf(net.id)
                    if (idx != -1) idx else Int.MAX_VALUE
                }
                .thenBy { it.name }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allNetworks: StateFlow<List<NetworkItem>> = combine(
        _publicNetworks,
        repository.joinedNetworks,
        _pinnedNetworkIds
    ) { pubList, joinedList, pinnedSet ->
        val joinedMapById = joinedList.associateBy { it.id }
        val joinedMapByCode = joinedList.filter { it.code.isNotBlank() }.associateBy { it.code.trim().uppercase() }
        val allMap = linkedMapOf<String, NetworkItem>()

        pubList.forEach { net ->
            val joined = joinedMapById[net.id] ?: joinedMapByCode[net.code.trim().uppercase()]
            val itemToAdd = if (joined != null) {
                joined.copy(isPinned = pinnedSet.contains(net.id))
            } else {
                net.copy(isPinned = pinnedSet.contains(net.id))
            }
            val existing = allMap.values.find {
                it.id == itemToAdd.id || (it.code.isNotBlank() && it.code.equals(itemToAdd.code, ignoreCase = true))
            }
            if (existing == null) {
                allMap[itemToAdd.id] = itemToAdd
            }
        }

        joinedList.forEach { joined ->
            val existing = allMap.values.find {
                it.id == joined.id || (it.code.isNotBlank() && it.code.equals(joined.code, ignoreCase = true))
            }
            if (existing == null) {
                allMap[joined.id] = joined.copy(isPinned = pinnedSet.contains(joined.id))
            }
        }

        allMap.values.sortedWith(
            compareByDescending<NetworkItem> { it.isPinned }
                .thenBy { net ->
                    when (net.status) {
                        JoinStatus.APPROVED -> 0
                        JoinStatus.PENDING -> 1
                        else -> 2
                    }
                }
                .thenBy { it.name }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun togglePinNetwork(networkId: String) {
        viewModelScope.launch {
            val current = _pinnedNetworkIds.value.toMutableSet()
            if (current.contains(networkId)) {
                current.remove(networkId)
                setToast("تم إلغاء تثبيت الشبكة")
            } else {
                current.add(networkId)
                val searched = _searchedNetwork.value
                if (searched != null && searched.id == networkId) {
                    repository.trackNetworkInDb(searched)
                }
                setToast("تم تثبيت الشبكة في الأعلى")
            }
            _pinnedNetworkIds.value = current
        }
    }

    fun moveNetworkOrder(networkId: String, moveUp: Boolean) {
        val currentList = sortedJoinedNetworks.value.map { it.id }.toMutableList()
        val index = currentList.indexOf(networkId)
        if (index != -1) {
            val targetIndex = if (moveUp) index - 1 else index + 1
            if (targetIndex in 0 until currentList.size) {
                val item = currentList.removeAt(index)
                currentList.add(targetIndex, item)
                _networkCustomOrder.value = currentList
                setToast("تم تعديل ترتيب الشبكة")
            }
        }
    }

    val orderHistory = repository.orderHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val activePrinter = repository.printerSettings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val walletBalance = repository.walletBalance.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val walletTransactions = repository.walletTransactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Dialog state management for unjoined network purchase & ceiling exhausted flows
    private val _showUnjoinedTopUpDialog = MutableStateFlow(false)
    val showUnjoinedTopUpDialog: StateFlow<Boolean> = _showUnjoinedTopUpDialog.asStateFlow()

    private val _showCeilingExhaustedDialog = MutableStateFlow(false)
    val showCeilingExhaustedDialog: StateFlow<Boolean> = _showCeilingExhaustedDialog.asStateFlow()

    private val _showInsufficientWalletDialog = MutableStateFlow(false)
    val showInsufficientWalletDialog: StateFlow<Boolean> = _showInsufficientWalletDialog.asStateFlow()

    private val _isTopUpProcessing = MutableStateFlow(false)
    val isTopUpProcessing: StateFlow<Boolean> = _isTopUpProcessing.asStateFlow()

    fun dismissUnjoinedTopUpDialog() {
        _showUnjoinedTopUpDialog.value = false
    }

    fun dismissCeilingExhaustedDialog() {
        _showCeilingExhaustedDialog.value = false
    }

    fun dismissInsufficientWalletDialog() {
        _showInsufficientWalletDialog.value = false
    }


    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchedNetwork = MutableStateFlow<NetworkItem?>(null)
    val searchedNetwork: StateFlow<NetworkItem?> = _searchedNetwork.asStateFlow()

    private val _selectedNetworkId = MutableStateFlow<String?>(null)
    private val _fallbackSelectedNetwork = MutableStateFlow<NetworkItem?>(null)

    val selectedNetwork: StateFlow<NetworkItem?> = combine(
        allNetworks,
        sortedJoinedNetworks,
        _selectedNetworkId,
        _fallbackSelectedNetwork
    ) { allNets, joinedNets, id, fallback ->
        if (id == null) return@combine null
        // 1. Look in joined networks by ID or fallback's code
        val joined = joinedNets.find { 
            it.id == id || (fallback != null && it.code.isNotBlank() && it.code.equals(fallback.code, ignoreCase = true))
        }
        if (joined != null) return@combine joined

        // 2. Look in all networks
        val found = allNets.find { it.id == id }
        if (found != null) return@combine found

        fallback
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _availablePackages = MutableStateFlow<List<VoucherPackage>>(emptyList())
    val availablePackages: StateFlow<List<VoucherPackage>> = _availablePackages.asStateFlow()

    // Map of packageId to quantity selected
    private val _selectedQuantities = MutableStateFlow<Map<String, Int>>(emptyMap())
    val selectedQuantities: StateFlow<Map<String, Int>> = _selectedQuantities.asStateFlow()

    private val _customerPhone = MutableStateFlow("")
    val customerPhone: StateFlow<String> = _customerPhone.asStateFlow()

    private val _isPurchasing = MutableStateFlow(false)
    val isPurchasing: StateFlow<Boolean> = _isPurchasing.asStateFlow()

    private val _latestOrder = MutableStateFlow<OrderTransaction?>(null)
    val latestOrder: StateFlow<OrderTransaction?> = _latestOrder.asStateFlow()

    private val _showReceiptModal = MutableStateFlow(false)
    val showReceiptModal: StateFlow<Boolean> = _showReceiptModal.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _currentAlert = MutableStateFlow<InAppAlert?>(null)
    val currentAlert: StateFlow<InAppAlert?> = _currentAlert.asStateFlow()

    fun showAlert(message: String, type: AlertType = AlertType.INFO, title: String? = null, durationMs: Long = 3500L) {
        if (message.isBlank()) return
        _currentAlert.value = InAppAlert(
            message = message,
            type = type,
            title = title,
            durationMs = durationMs
        )
    }

    fun dismissAlert() {
        _currentAlert.value = null
    }

    private val _isPrinting = MutableStateFlow(false)
    val isPrinting: StateFlow<Boolean> = _isPrinting.asStateFlow()

    init {
        com.example.data.remote.RetrofitClient.initToken(application)
        viewModelScope.launch {
            repository.initDefaultDataIfNeeded()
            fetchPublicNetworks()
            // جلب ومزامنة كافة البيانات تلقائياً عند فتح التطبيق إذا كان التوكن موجوداً
            if (!com.example.data.remote.RetrofitClient.authToken.isNullOrBlank()) {
                repository.syncAllDataFromServer()
                fetchPublicNetworks()
            }
            repository.seedDefaultNotificationsIfEmpty(getApplication())
        }
    }

    fun syncAllData() {
        viewModelScope.launch {
            repository.syncAllDataFromServer()
            fetchPublicNetworks()
        }
    }

    val notifications = repository.notifications.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val unreadNotificationsCount = repository.unreadNotificationsCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
            setToast("تم تحديد جميع التنبيهات كمقروءة")
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
            setToast("تم مسح جميع التنبيهات")
        }
    }

    fun sendNotification(title: String, message: String, type: NotificationType, amount: Double? = null) {
        viewModelScope.launch {
            repository.addNotification(
                title = title,
                message = message,
                type = type,
                amount = amount,
                context = getApplication()
            )
        }
    }


    fun setToast(message: String?) {
        _toastMessage.value = message
        if (!message.isNullOrBlank()) {
            val lower = message.lowercase()
            val isNegative = lower.contains("لم يتم") ||
                    lower.contains("لم نعثر") ||
                    lower.contains("غير مطابق") ||
                    lower.contains("غير متطابق") ||
                    lower.contains("غير صحيح") ||
                    lower.contains("غير موجود") ||
                    lower.contains("لا يوجد") ||
                    lower.contains("لا توجد") ||
                    lower.contains("تعذر") ||
                    lower.contains("خطأ") ||
                    lower.contains("فشل") ||
                    lower.contains("غير كافٍ") ||
                    lower.contains("غير كافي") ||
                    lower.contains("نفذ") ||
                    lower.contains("مرفوض") ||
                    lower.contains("invalid") ||
                    lower.contains("mismatch") ||
                    lower.contains("failed") ||
                    lower.contains("error") ||
                    lower.contains("not found")

            val type = when {
                isNegative -> AlertType.ERROR
                lower.contains("يرجى") || lower.contains("تنبيه") || lower.contains("تحذير") || lower.contains("سقف") -> AlertType.WARNING
                lower.contains("نجاح") || lower.contains("بنجاح") || lower.contains("تمت إضافة") || lower.contains("تم حفظ") ||
                        lower.contains("تم تسجيل") || lower.contains("تم تحديث") || lower.contains("تم شحن") || lower.contains("تم خصم") -> AlertType.SUCCESS
                else -> AlertType.INFO
            }
            showAlert(message = message, type = type)
        }
    }

    fun clearToast() {
        _toastMessage.value = null
        _currentAlert.value = null
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun searchNetwork() {
        val q = _searchQuery.value.trim()
        if (q.isEmpty()) {
            setToast("يرجى إدخال كود الشبكة للبحث (مثال: NET-101)")
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            val res = repository.searchNetwork(q)
            _isSearching.value = false
            if (res.success && res.data != null) {
                _searchedNetwork.value = res.data
                setToast(res.message)
            } else {
                _searchedNetwork.value = null
                setToast(res.message)
            }
        }
    }

    fun requestJoinNetwork(network: NetworkItem) {
        val user = currentUser.value
        val storeName = user?.storeName ?: ""
        val storePhone = user?.phone ?: ""

        viewModelScope.launch {
            val res = repository.requestJoinNetwork(network, storeName, storePhone)
            val msg = res.message.orEmpty()

            val isPendingMsg = msg.contains("معلق") || msg.contains("انتظار") || msg.contains("pending", ignoreCase = true)
            val isApprovedMsg = msg.contains("منضم") || msg.contains("نشط") || msg.contains("active", ignoreCase = true) || msg.contains("approved", ignoreCase = true)

            val updatedStatus = when {
                res.success -> JoinStatus.PENDING
                isApprovedMsg -> JoinStatus.APPROVED
                isPendingMsg -> JoinStatus.PENDING
                res.data != null -> res.data.status
                else -> null
            }

            if (updatedStatus != null) {
                val updatedNetwork = (res.data ?: network).copy(
                    id = network.id,
                    name = network.name,
                    code = network.code,
                    status = updatedStatus
                )
                repository.trackNetworkInDb(updatedNetwork)
                _searchedNetwork.value = updatedNetwork
                _fallbackSelectedNetwork.value = updatedNetwork

                if (updatedStatus == JoinStatus.APPROVED) {
                    repository.addNotification(
                        title = "تم قبول انضمامك في شبكة ${network.name}",
                        message = "تهانينا! تم قبول انضمام نقطة بيعك بنجاح في شبكة ${network.name}. يمكنك الآن بيع واستخراج جميع باقات الشبكة مباشرة.",
                        type = NotificationType.NETWORK_JOIN_APPROVED,
                        relatedEntityId = network.id,
                        context = getApplication()
                    )
                } else if (updatedStatus == JoinStatus.PENDING) {
                    repository.addNotification(
                        title = "طلب الانضمام قيد الانتظار (${network.name})",
                        message = "تم إرسال طلب انضمام نقطة بيعك إلى إدارة شبكة ${network.name} بنجاح، وهو قيد الانتظار لموافقة الإدارة.",
                        type = NotificationType.NETWORK_JOIN_APPROVED,
                        relatedEntityId = network.id,
                        context = getApplication()
                    )
                }
            }

            fetchMyNetworks()
            if (!res.message.isNullOrBlank()) {
                setToast(res.message)
            } else if (res.success) {
                setToast("تم إرسال طلب الانضمام بنجاح")
            }
        }
    }

    fun selectNetworkForStore(network: NetworkItem) {
        _selectedNetworkId.value = network.id
        val joinedNet = sortedJoinedNetworks.value.find { 
            it.id == network.id || (it.code.isNotBlank() && it.code.equals(network.code, ignoreCase = true))
        }
        _fallbackSelectedNetwork.value = joinedNet ?: network
        _selectedQuantities.value = emptyMap()
        _customerPhone.value = ""
        fetchPackagesForNetwork(network.id)
        fetchMyNetworks()
    }

    fun fetchPackagesForNetwork(networkId: String) {
        viewModelScope.launch {
            val res = repository.getVoucherPackages(networkId)
            if (res.success && res.data != null) {
                _availablePackages.value = res.data
            } else {
                _availablePackages.value = emptyList()
            }
        }
    }

    fun setQuantity(packageId: String, qty: Int) {
        // Enforce only ONE package type selection at a time
        val map = mutableMapOf<String, Int>()
        if (qty > 0) {
            map[packageId] = qty
        }
        _selectedQuantities.value = map
    }

    fun incrementQuantity(packageId: String) {
        val current = _selectedQuantities.value[packageId] ?: 0
        setQuantity(packageId, current + 1)
    }

    fun decrementQuantity(packageId: String) {
        val current = _selectedQuantities.value[packageId] ?: 0
        setQuantity(packageId, current - 1)
    }

    fun setCustomerPhone(phone: String) {
        _customerPhone.value = phone
    }

    fun clearAllSelections() {
        _selectedQuantities.value = emptyMap()
        _customerPhone.value = ""
    }

    fun processPurchase() {
        val net = selectedNetwork.value
        if (net == null) {
            setToast("يرجى تحديد الشبكة أولاً")
            return
        }

        val entries = _selectedQuantities.value.filterValues { it > 0 }
        if (entries.isEmpty()) {
            setToast("يرجى تحديد كمية كرت واحد على الأقل للمتابعة")
            return
        }

        val packageId = entries.keys.first()
        val quantity = entries.values.first()
        val pkg = _availablePackages.value.find { it.id == packageId }
        if (pkg == null) {
            setToast("الباقة غير متاحة حالياً")
            return
        }

        val totalCost = pkg.posPrice * quantity
        val currentWallet = walletBalance.value

        val storeName = currentUser.value?.storeName ?: "نقطة بيع معتمدة"

        viewModelScope.launch {
            _isPurchasing.value = true
            val res = repository.purchaseVouchers(
                network = net,
                packageItem = pkg,
                quantity = quantity,
                customerPhone = _customerPhone.value.ifBlank { null },
                storeName = storeName
            )
            _isPurchasing.value = false

            if (res.success && res.data != null) {
                _latestOrder.value = res.data
                _showReceiptModal.value = true
                
                // Update selected network balance locally from the latest fetch inside repository.purchaseVouchers
                // But for now, just fallback refresh:
                fetchMyNetworks()
                
                _selectedQuantities.value = emptyMap()
                setToast(res.message)
            } else {
                setToast(res.message)
            }
        }
    }

    fun processPurchaseViaWallet() {
        _showCeilingExhaustedDialog.value = false
        _showUnjoinedTopUpDialog.value = false

        val net = selectedNetwork.value ?: return
        val entries = _selectedQuantities.value.filterValues { it > 0 }
        if (entries.isEmpty()) return

        val packageId = entries.keys.first()
        val quantity = entries.values.first()
        val pkg = _availablePackages.value.find { it.id == packageId } ?: return
        val totalCost = pkg.posPrice * quantity
        val currentWallet = walletBalance.value

        if (currentWallet < totalCost) {
            _showInsufficientWalletDialog.value = true
            return
        }

        val storeName = currentUser.value?.storeName ?: "نقطة بيع معتمدة"

        viewModelScope.launch {
            _isPurchasing.value = true
            val res = repository.purchaseVouchersWithWallet(
                network = net,
                packageItem = pkg,
                quantity = quantity,
                customerPhone = _customerPhone.value.ifBlank { null },
                storeName = storeName,
                currentWalletBalance = currentWallet
            )
            _isPurchasing.value = false

            if (res.success && res.data != null) {
                _latestOrder.value = res.data
                _showReceiptModal.value = true
                _selectedQuantities.value = emptyMap()
                _fallbackSelectedNetwork.value = net
                setToast(res.message)
                fetchMyNetworks()
                repository.syncWalletBalance()
            } else {
                setToast(res.message)
            }
        }
    }

    fun topUpWallet(amount: Double, paymentMethodName: String, referenceNumber: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isTopUpProcessing.value = true
            val res = repository.topUpWalletBalance(amount, paymentMethodName, referenceNumber)
            _isTopUpProcessing.value = false
            if (res.success) {
                showAlert(
                    message = res.message,
                    type = AlertType.SUCCESS,
                    title = "تمت التغذية بنجاح"
                )
                repository.addNotification(
                    title = "شحن المحفظة بنجاح",
                    message = "تمت إضافة مبلغ ${amount.toInt()} ريال لحساب محفظتك بنجاح عبر $paymentMethodName (المرجع: $referenceNumber).",
                    type = NotificationType.WALLET_TOPUP_SUCCESS,
                    amount = amount,
                    context = getApplication()
                )
            } else {
                showAlert(
                    message = res.message,
                    type = AlertType.ERROR,
                    title = "فشل التحقق من الإيداع"
                )
            }
            onResult(res.success, res.message)
        }
    }



    fun syncWalletData() {
        viewModelScope.launch {
            repository.syncWalletBalance()
            repository.syncSalesHistory()
            fetchMyNetworks()
            setToast("تم تحديث بيانات المحفظة بنجاح")
        }
    }

    fun dismissReceiptModal() {
        _showReceiptModal.value = false
    }

    fun printCurrentOrderReceipt() {
        val order = _latestOrder.value
        val printer = activePrinter.value
        if (order == null || printer == null) {
            setToast("لا يوجد كرت جاهز للطباعة")
            return
        }

        viewModelScope.launch {
            _isPrinting.value = true
            val printRes = printerManager.printOrder(order, printer)
            _isPrinting.value = false
            if (printRes.success) {
                repository.markOrderPrinted(order.id)
                _latestOrder.value = order.copy(isPrinted = true)
                setToast(printRes.message)
            } else {
                setToast(printRes.message)
            }
        }
    }

    fun saveSelectedPrinter(device: PrinterDevice) {
        viewModelScope.launch {
            repository.savePrinterSettings(device)
            setToast("تم حفظ إعدادات الطابعة (${device.name}) بنجاح")
        }
    }

    fun getSavedPhone(): String = prefs.getString("saved_phone", "") ?: ""
    fun getSavedPassword(): String = prefs.getString("saved_password", "") ?: ""
    fun isRememberMeEnabled(): Boolean = prefs.getBoolean("remember_me", false)

    fun saveAuthCredentials(phone: String, pass: String, remember: Boolean) {
        if (remember) {
            prefs.edit()
                .putString("saved_phone", phone)
                .putString("saved_password", pass)
                .putBoolean("remember_me", true)
                .apply()
        } else {
            prefs.edit()
                .remove("saved_phone")
                .remove("saved_password")
                .putBoolean("remember_me", false)
                .apply()
        }
    }

    fun setPendingOtpVerification(phone: String, storeName: String, location: String, pass: String) {
        prefs.edit()
            .putString("pending_otp_phone", phone.trim())
            .putString("pending_otp_store_name", storeName.trim())
            .putString("pending_otp_location", location.trim())
            .putString("pending_otp_password", pass)
            .apply()
    }

    fun isPhonePendingOtp(phone: String): Boolean {
        val pending = prefs.getString("pending_otp_phone", "") ?: ""
        return pending.isNotBlank() && pending == phone.trim()
    }

    fun getPendingOtpData(): Map<String, String> {
        return mapOf(
            "phone" to (prefs.getString("pending_otp_phone", "") ?: ""),
            "storeName" to (prefs.getString("pending_otp_store_name", "") ?: ""),
            "location" to (prefs.getString("pending_otp_location", "") ?: ""),
            "password" to (prefs.getString("pending_otp_password", "") ?: "")
        )
    }

    fun clearPendingOtpVerification() {
        prefs.edit()
            .remove("pending_otp_phone")
            .remove("pending_otp_store_name")
            .remove("pending_otp_location")
            .remove("pending_otp_password")
            .apply()
    }

    fun registerAccount(
        ownerName: String,
        storeName: String,
        phone: String,
        location: String,
        password: String,
        rememberMe: Boolean = false,
        onResult: (Boolean, String, String) -> Unit
    ) {
        saveAuthCredentials(phone, password, rememberMe)
        viewModelScope.launch {
            val res = repository.registerAccount(ownerName, storeName, phone, location, password)
            val testOtp = res.data ?: ""
            if (res.success) {
                setPendingOtpVerification(phone, storeName, location, password)
            }
            onResult(res.success, res.message, testOtp)
            setToast(res.message)
        }
    }

    fun registerAccount(storeName: String, phone: String, location: String, password: String, rememberMe: Boolean = false, onResult: (Boolean, String, String) -> Unit) {
        registerAccount(
            ownerName = storeName,
            storeName = storeName,
            phone = phone,
            location = location,
            password = password,
            rememberMe = rememberMe,
            onResult = onResult
        )
    }

    fun verifyAccountOtp(phone: String, storeName: String, location: String, pass: String, otpCode: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.verifyAccountOtp(phone, storeName, location, pass, otpCode)
            if (res.success && res.data != null) {
                clearPendingOtpVerification()
                setToast(res.message)
                onResult(true, res.message)
                // جلب ومزامنة كل البيانات فوراً بعد تفعيل الحساب
                repository.syncAllDataFromServer()
                fetchPublicNetworks()
            } else {
                setToast(res.message)
                onResult(false, res.message)
            }
        }
    }

    fun login(phone: String, password: String, rememberMe: Boolean = false, onResult: (Boolean, String) -> Unit) {
        saveAuthCredentials(phone, password, rememberMe)
        viewModelScope.launch {
            val res = repository.login(phone, password)
            onResult(res.success, res.message)
            if (res.success) {
                setToast(res.message)
                // جلب ومزامنة كل البيانات فوراً بعد تسجيل الدخول
                repository.syncAllDataFromServer()
                fetchPublicNetworks()
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _pinnedNetworkIds.value = emptySet()
            _networkCustomOrder.value = emptyList()
            _searchedNetwork.value = null
            _publicNetworks.value = emptyList()
            _latestOrder.value = null
            if (!isRememberMeEnabled()) {
                saveAuthCredentials("", "", false)
            }
            setToast("تم تسجيل الخروج بنجاح")
        }
    }

    fun updateProfile(newStoreName: String, newLocation: String, onResult: (Boolean, String) -> Unit) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.updateProfileInfo(user.phone, newStoreName, newLocation)
            if (res.success) {
                setToast(res.message)
            }
            onResult(res.success, res.message)
        }
    }

    fun changePassword(oldPass: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.changePassword(user.phone, oldPass, newPass)
            if (res.success) {
                setToast(res.message)
            }
            onResult(res.success, res.message)
        }
    }

    fun requestForgotPasswordOtp(phone: String, onResult: (Boolean, String, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.requestForgotPasswordOtp(phone)
            val testOtp = res.data ?: ""
            if (res.success) {
                setToast(res.message)
            }
            onResult(res.success, res.message, testOtp)
        }
    }

    fun resetPasswordWithOtp(phone: String, otpCode: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.resetPasswordWithOtp(phone, otpCode, newPass)
            if (res.success) {
                setToast(res.message)
            }
            onResult(res.success, res.message)
        }
    }

    fun deleteAccount(onResult: (Boolean, String) -> Unit) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.deleteAccount(user.phone)
            if (res.success) {
                setToast(res.message)
            }
            onResult(res.success, res.message)
        }
    }
}
