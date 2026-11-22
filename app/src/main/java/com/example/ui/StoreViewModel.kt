package com.example.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.DiamondPackageEntity
import com.example.data.local.FreeRewardEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.OrderEntity
import com.example.data.local.PaymentConfigEntity
import com.example.data.local.SupportMessageEntity
import com.example.data.local.UserEntity
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.UserRole
import com.example.data.repository.ScreenshotStorage
import com.example.data.repository.StoreRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiMessage {
    data class Success(val text: String) : UiMessage
    data class Error(val text: String) : UiMessage
}

data class AdminMetrics(
    val totalUsers: Int = 0,
    val totalOrders: Int = 0,
    val pendingOrders: Int = 0,
    val completedOrders: Int = 0,
    val totalSalesPkr: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class StoreViewModel(
    private val repository: StoreRepository,
    private val screenshotStorage: ScreenshotStorage
) : ViewModel() {

    val currentUser: StateFlow<UserEntity?> = repository.currentUserFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val adminExists: StateFlow<Boolean> = repository.adminCountFlow
        .map { it > 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val activePackages: StateFlow<List<DiamondPackageEntity>> = repository.activePackagesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPackages: StateFlow<List<DiamondPackageEntity>> = repository.allPackagesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val paymentConfigs: StateFlow<List<PaymentConfigEntity>> = repository.paymentConfigsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userOrders: StateFlow<List<OrderEntity>> = currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList()) else repository.observeOrdersForUser(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userRewards: StateFlow<List<FreeRewardEntity>> = currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList()) else repository.observeRewardsForUser(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userNotifications: StateFlow<List<NotificationEntity>> = currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList()) else repository.observeNotificationsForUser(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Admin Streams (only active when currentUser is ADMIN) ---
    val adminAllOrders: StateFlow<List<OrderEntity>> = currentUser
        .flatMapLatest { user ->
            if (user?.role == UserRole.ADMIN) repository.observeAllOrders() else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _userSearchQuery = MutableStateFlow("")
    val userSearchQuery: StateFlow<String> = _userSearchQuery.asStateFlow()

    val adminUsers: StateFlow<List<UserEntity>> = currentUser
        .flatMapLatest { user ->
            if (user?.role == UserRole.ADMIN) {
                _userSearchQuery.flatMapLatest { query -> repository.searchUsers(query) }
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminAllUsersRaw: StateFlow<List<UserEntity>> = currentUser
        .flatMapLatest { user ->
            if (user?.role == UserRole.ADMIN) repository.observeAllUsers() else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminAllRewards: StateFlow<List<FreeRewardEntity>> = currentUser
        .flatMapLatest { user ->
            if (user?.role == UserRole.ADMIN) repository.observeAllRewards() else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminSupportMessages: StateFlow<List<SupportMessageEntity>> = currentUser
        .flatMapLatest { user ->
            if (user?.role == UserRole.ADMIN) repository.observeAllSupportMessages() else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminMetrics: StateFlow<AdminMetrics> = kotlinx.coroutines.flow.combine(
        adminAllUsersRaw,
        adminAllOrders
    ) { users, orders ->
        val standardUsers = users.count { it.role == UserRole.USER }
        val pendingCount = orders.count { it.status.isPending }
        val completedCount = orders.count { it.status == OrderStatus.DELIVERED }
        val sales = orders.filter {
            it.status == OrderStatus.DELIVERED ||
                it.status == OrderStatus.PAYMENT_APPROVED ||
                it.status == OrderStatus.PROCESSING
        }.sumOf { it.totalPricePkr }
        AdminMetrics(
            totalUsers = standardUsers,
            totalOrders = orders.size,
            pendingOrders = pendingCount,
            completedOrders = completedCount,
            totalSalesPkr = sales
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminMetrics())

    private val _selectedPackage = MutableStateFlow<DiamondPackageEntity?>(null)
    val selectedPackage: StateFlow<DiamondPackageEntity?> = _selectedPackage.asStateFlow()

    private val _quickPlayerUid = MutableStateFlow("")
    val quickPlayerUid: StateFlow<String> = _quickPlayerUid.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _messages = MutableSharedFlow<UiMessage>()
    val messages = _messages.asSharedFlow()

    fun setQuickPlayerUid(uid: String) {
        _quickPlayerUid.value = uid
    }

    fun selectPackage(pkg: DiamondPackageEntity) {
        _selectedPackage.value = pkg
    }

    fun updateAdminUserSearch(query: String) {
        _userSearchQuery.value = query
    }

    fun register(fullName: String, email: String, playerUid: String, password: String) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.registerUser(fullName, email, playerUid, password)
            _isBusy.value = false
            res.onSuccess {
                _messages.emit(UiMessage.Success("Welcome, ${it.fullName}!"))
            }.onFailure {
                _messages.emit(UiMessage.Error(it.message ?: "Registration failed."))
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.loginUser(email, password)
            _isBusy.value = false
            res.onSuccess {
                _messages.emit(UiMessage.Success("Signed in as ${it.fullName}"))
            }.onFailure {
                _messages.emit(UiMessage.Error(it.message ?: "Login failed."))
            }
        }
    }

    fun claimFirstAdmin(fullName: String, email: String, passcode: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.claimFirstAdmin(fullName, email, passcode)
            _isBusy.value = false
            res.onSuccess {
                _messages.emit(UiMessage.Success("Admin role claimed. Welcome, ${it.fullName}!"))
                onSuccess()
            }.onFailure {
                _messages.emit(UiMessage.Error(it.message ?: "Could not claim Admin access."))
            }
        }
    }

    fun loginAdmin(email: String, passcode: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.loginAdmin(email, passcode)
            _isBusy.value = false
            res.onSuccess {
                _messages.emit(UiMessage.Success("Admin Portal unlocked."))
                onSuccess()
            }.onFailure {
                _messages.emit(UiMessage.Error(it.message ?: "Admin login failed."))
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _messages.emit(UiMessage.Success("Signed out."))
        }
    }

    fun updatePlayerUid(newUid: String) {
        val userId = currentUser.value?.id ?: return
        viewModelScope.launch {
            val res = repository.updatePlayerUid(userId, newUid)
            res.onSuccess {
                _messages.emit(UiMessage.Success("Player UID updated to $newUid"))
            }.onFailure {
                _messages.emit(UiMessage.Error(it.message ?: "Could not update Player UID."))
            }
        }
    }

    fun placeOrder(
        packageId: Int,
        playerUid: String,
        paymentMethod: PaymentMethod,
        transactionId: String,
        screenshotUri: Uri?,
        onOrderCreated: (OrderEntity) -> Unit
    ) {
        val userId = currentUser.value?.id
        if (userId == null) {
            viewModelScope.launch { _messages.emit(UiMessage.Error("Please sign in to place an order.")) }
            return
        }
        if (screenshotUri == null) {
            viewModelScope.launch { _messages.emit(UiMessage.Error("Please upload your payment receipt screenshot.")) }
            return
        }
        viewModelScope.launch {
            _isBusy.value = true
            val savedPath = screenshotStorage.saveReceiptFromUri(screenshotUri)
            if (savedPath.isFailure) {
                _isBusy.value = false
                _messages.emit(UiMessage.Error(savedPath.exceptionOrNull()?.message ?: "Failed to save screenshot."))
                return@launch
            }
            val res = repository.placeOrder(
                userId = userId,
                packageId = packageId,
                playerUid = playerUid,
                paymentMethod = paymentMethod,
                transactionId = transactionId,
                screenshotPath = savedPath.getOrThrow()
            )
            _isBusy.value = false
            res.onSuccess { order ->
                _messages.emit(UiMessage.Success("Order ${order.id} placed (Pending Payment)."))
                onOrderCreated(order)
            }.onFailure {
                _messages.emit(UiMessage.Error(it.message ?: "Failed to place order."))
            }
        }
    }

    fun markNotificationRead(id: Int) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun submitSupportMessage(
        name: String,
        email: String,
        playerUid: String,
        subject: String,
        message: String,
        onSubmitted: () -> Unit
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.submitSupportMessage(
                userId = currentUser.value?.id,
                name = name,
                email = email,
                playerUid = playerUid,
                subject = subject,
                message = message
            )
            _isBusy.value = false
            res.onSuccess {
                _messages.emit(UiMessage.Success("Support inquiry submitted. Our team will respond soon."))
                onSubmitted()
            }.onFailure {
                _messages.emit(UiMessage.Error(it.message ?: "Failed to send message."))
            }
        }
    }

    // --- Admin Actions ---

    fun updateOrderStatus(orderId: String, status: OrderStatus, adminNote: String = "") {
        val adminId = currentUser.value?.id ?: return
        viewModelScope.launch {
            val res = repository.updateOrderStatus(adminId, orderId, status, adminNote)
            res.onSuccess {
                _messages.emit(UiMessage.Success("Order $orderId updated to ${status.label}"))
            }.onFailure {
                _messages.emit(UiMessage.Error(it.message ?: "Failed to update order."))
            }
        }
    }

    fun savePackage(packageId: Int?, diamonds: Int, pricePkr: Int, badge: String?) {
        val adminId = currentUser.value?.id ?: return
        viewModelScope.launch {
            val res = repository.savePackage(adminId, packageId, diamonds, pricePkr, badge)
            res.onSuccess {
                _messages.emit(UiMessage.Success("Package ($diamonds Diamonds — Rs. $pricePkr) saved."))
            }.onFailure {
                _messages.emit(UiMessage.Error(it.message ?: "Failed to save package."))
            }
        }
    }

    fun deletePackage(packageId: Int) {
        val adminId = currentUser.value?.id ?: return
        viewModelScope.launch {
            val res = repository.deletePackage(adminId, packageId)
            res.onSuccess {
                _messages.emit(UiMessage.Success("Package deleted."))
            }.onFailure {
                _messages.emit(UiMessage.Error(it.message ?: "Failed to delete package."))
            }
        }
    }

    fun updatePaymentConfig(
        method: PaymentMethod,
        holderName: String,
        accountNumber: String,
        instructions: String
    ) {
        val adminId = currentUser.value?.id ?: return
        viewModelScope.launch {
            val res = repository.updatePaymentConfig(adminId, method, holderName, accountNumber, instructions)
            res.onSuccess {
                _messages.emit(UiMessage.Success("${method.displayName} account details updated."))
            }.onFailure {
                _messages.emit(UiMessage.Error(it.message ?: "Failed to update payment config."))
            }
        }
    }

    fun sendFreeDiamonds(targetUserId: String, diamonds: Int, reason: String, onSent: () -> Unit) {
        val adminId = currentUser.value?.id ?: return
        viewModelScope.launch {
            val res = repository.sendFreeDiamonds(adminId, targetUserId, diamonds, reason)
            res.onSuccess { reward ->
                _messages.emit(
                    UiMessage.Success(
                        "Reward ${reward.id} ($diamonds Diamonds) recorded as Pending Manual Fulfilment."
                    )
                )
                onSent()
            }.onFailure {
                _messages.emit(UiMessage.Error(it.message ?: "Failed to send free diamond reward."))
            }
        }
    }

    fun markRewardFulfilled(rewardId: String) {
        val adminId = currentUser.value?.id ?: return
        viewModelScope.launch {
            val res = repository.markRewardFulfilled(adminId, rewardId)
            res.onSuccess {
                _messages.emit(UiMessage.Success("Reward $rewardId marked as Fulfilled."))
            }.onFailure {
                _messages.emit(UiMessage.Error(it.message ?: "Failed to update reward."))
            }
        }
    }

    fun sendNotification(targetUserId: String?, title: String, message: String, onSent: () -> Unit) {
        val adminId = currentUser.value?.id ?: return
        viewModelScope.launch {
            val res = repository.sendNotification(adminId, targetUserId, title, message)
            res.onSuccess {
                _messages.emit(UiMessage.Success("Notification sent."))
                onSent()
            }.onFailure {
                _messages.emit(UiMessage.Error(it.message ?: "Failed to send notification."))
            }
        }
    }

    fun resolveSupportMessage(messageId: Int) {
        val adminId = currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.resolveSupportMessage(adminId, messageId)
        }
    }

    class Factory(
        private val repository: StoreRepository,
        private val screenshotStorage: ScreenshotStorage
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StoreViewModel(repository, screenshotStorage) as T
        }
    }
}
