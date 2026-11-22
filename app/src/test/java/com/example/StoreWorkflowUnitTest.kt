package com.example

import com.example.data.local.DiamondPackageEntity
import com.example.data.local.FreeRewardEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.OrderEntity
import com.example.data.local.PaymentConfigEntity
import com.example.data.local.StoreDao
import com.example.data.local.SupportMessageEntity
import com.example.data.local.UserEntity
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.RewardStatus
import com.example.data.model.UserRole
import com.example.data.repository.SessionStore
import com.example.data.repository.StoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class InMemorySessionStore : SessionStore {
    private val _uid = MutableStateFlow<String?>(null)
    override val currentUserIdFlow: Flow<String?> = _uid
    override suspend fun setCurrentUserId(userId: String) {
        _uid.value = userId
    }
    override suspend fun clearSession() {
        _uid.value = null
    }
}

private class InMemoryStoreDao : StoreDao {
    private val users = MutableStateFlow<List<UserEntity>>(emptyList())
    private val packages = MutableStateFlow<List<DiamondPackageEntity>>(emptyList())
    private val paymentConfigs = MutableStateFlow<List<PaymentConfigEntity>>(emptyList())
    private val orders = MutableStateFlow<List<OrderEntity>>(emptyList())
    private val rewards = MutableStateFlow<List<FreeRewardEntity>>(emptyList())
    private val notifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    private val supportMessages = MutableStateFlow<List<SupportMessageEntity>>(emptyList())
    private var nextPkgId = 1
    private var nextNotifId = 1
    private var nextSupportId = 1

    override fun observeUserById(id: String): Flow<UserEntity?> =
        users.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun getUserById(id: String): UserEntity? =
        users.value.firstOrNull { it.id == id }

    override suspend fun getUserByEmail(email: String): UserEntity? =
        users.value.firstOrNull { it.email.equals(email, ignoreCase = true) }

    override suspend fun countUsersByRole(role: UserRole): Int =
        users.value.count { it.role == role }

    override fun observeAdminCount(role: UserRole): Flow<Int> =
        users.map { list -> list.count { it.role == role } }

    override fun observeAllUsers(): Flow<List<UserEntity>> = users

    override fun searchUsers(query: String): Flow<List<UserEntity>> =
        users.map { list ->
            list.filter {
                it.playerUid.contains(query, ignoreCase = true) ||
                    it.fullName.contains(query, ignoreCase = true) ||
                    it.email.contains(query, ignoreCase = true)
            }
        }

    override suspend fun insertUser(user: UserEntity) {
        users.value = users.value + user
    }

    override suspend fun updateUserPlayerUid(userId: String, playerUid: String) {
        users.value = users.value.map { if (it.id == userId) it.copy(playerUid = playerUid) else it }
    }

    override fun observeActivePackages(): Flow<List<DiamondPackageEntity>> =
        packages.map { list -> list.filter { it.isActive }.sortedBy { it.diamonds } }

    override fun observeAllPackages(): Flow<List<DiamondPackageEntity>> =
        packages.map { list -> list.sortedBy { it.diamonds } }

    override suspend fun getPackageById(packageId: Int): DiamondPackageEntity? =
        packages.value.firstOrNull { it.id == packageId }

    override suspend fun countPackages(): Int = packages.value.size

    override suspend fun insertPackage(pkg: DiamondPackageEntity) {
        val assigned = if (pkg.id == 0) pkg.copy(id = nextPkgId++) else pkg
        packages.value = packages.value.filterNot { it.id == assigned.id } + assigned
    }

    override suspend fun insertPackages(packages: List<DiamondPackageEntity>) {
        packages.forEach { insertPackage(it) }
    }

    override suspend fun updatePackage(pkg: DiamondPackageEntity) {
        packages.value = packages.value.map { if (it.id == pkg.id) pkg else it }
    }

    override suspend fun deletePackageById(packageId: Int) {
        packages.value = packages.value.filterNot { it.id == packageId }
    }

    override fun observePaymentConfigs(): Flow<List<PaymentConfigEntity>> = paymentConfigs

    override suspend fun getPaymentConfig(method: PaymentMethod): PaymentConfigEntity? =
        paymentConfigs.value.firstOrNull { it.method == method }

    override suspend fun countPaymentConfigs(): Int = paymentConfigs.value.size

    override suspend fun upsertPaymentConfig(config: PaymentConfigEntity) {
        paymentConfigs.value = paymentConfigs.value.filterNot { it.method == config.method } + config
    }

    override fun observeOrdersForUser(userId: String): Flow<List<OrderEntity>> =
        orders.map { list -> list.filter { it.userId == userId } }

    override fun observeAllOrders(): Flow<List<OrderEntity>> = orders

    override suspend fun getOrderById(orderId: String): OrderEntity? =
        orders.value.firstOrNull { it.id == orderId }

    override suspend fun insertOrder(order: OrderEntity) {
        orders.value = listOf(order) + orders.value
    }

    override suspend fun updateOrderStatus(orderId: String, status: OrderStatus, adminNote: String, updatedAt: Long) {
        orders.value = orders.value.map {
            if (it.id == orderId) it.copy(status = status, adminNote = adminNote, updatedAt = updatedAt) else it
        }
    }

    override fun observeRewardsForUser(userId: String): Flow<List<FreeRewardEntity>> =
        rewards.map { list -> list.filter { it.userId == userId } }

    override fun observeAllRewards(): Flow<List<FreeRewardEntity>> = rewards

    override suspend fun insertFreeReward(reward: FreeRewardEntity) {
        rewards.value = listOf(reward) + rewards.value
    }

    override suspend fun updateRewardStatus(rewardId: String, status: RewardStatus, updatedAt: Long) {
        rewards.value = rewards.value.map {
            if (it.id == rewardId) it.copy(status = status, updatedAt = updatedAt) else it
        }
    }

    override fun observeNotificationsForUser(userId: String): Flow<List<NotificationEntity>> =
        notifications.map { list -> list.filter { it.userId == userId || it.userId == null } }

    override fun observeAllNotifications(): Flow<List<NotificationEntity>> = notifications

    override suspend fun insertNotification(notification: NotificationEntity) {
        val assigned = notification.copy(id = nextNotifId++)
        notifications.value = listOf(assigned) + notifications.value
    }

    override suspend fun markNotificationRead(notificationId: Int) {
        notifications.value = notifications.value.map {
            if (it.id == notificationId) it.copy(isRead = true) else it
        }
    }

    override fun observeAllSupportMessages(): Flow<List<SupportMessageEntity>> = supportMessages

    override suspend fun insertSupportMessage(message: SupportMessageEntity) {
        supportMessages.value = listOf(message.copy(id = nextSupportId++)) + supportMessages.value
    }

    override suspend fun updateSupportMessageStatus(messageId: Int, status: String) {
        supportMessages.value = supportMessages.value.map {
            if (it.id == messageId) it.copy(status = status) else it
        }
    }
}

class StoreWorkflowUnitTest {

    private lateinit var dao: InMemoryStoreDao
    private lateinit var session: InMemorySessionStore
    private lateinit var repository: StoreRepository

    @Before
    fun setUp() {
        dao = InMemoryStoreDao()
        session = InMemorySessionStore()
        repository = StoreRepository(dao, session)
    }

    @Test
    fun `default 9 packages and payment configs seed cleanly`() = runTest {
        repository.ensureSeeded()
        val packages = repository.activePackagesFlow.first()
        assertEquals(9, packages.size)
        assertEquals(25, packages.first().diamonds)
        assertEquals(60, packages.first().pricePkr)
        assertEquals(5600, packages.last().diamonds)
        assertEquals(10500, packages.last().pricePkr)

        val configs = repository.paymentConfigsFlow.first()
        assertEquals(2, configs.size)
        assertTrue(configs.any { it.method == PaymentMethod.JAZZCASH })
        assertTrue(configs.any { it.method == PaymentMethod.EASYPAISA })
    }

    @Test
    fun `end to end - register, login, checkout, admin approval, free reward, and package updates`() = runTest {
        repository.ensureSeeded()

        // 1. Register & Login
        val regResult = repository.registerUser(
            fullName = "Zain Ali",
            email = "zain@example.com",
            playerUid = "1234567890",
            password = "password123"
        )
        assertTrue(regResult.isSuccess)
        val user = regResult.getOrThrow()
        assertEquals(UserRole.USER, user.role)

        repository.logout()
        assertTrue(repository.loginUser("zain@example.com", "wrongpass").isFailure)
        assertTrue(repository.loginUser("zain@example.com", "password123").isSuccess)

        // 2. Checkout Order Placement
        val pkg100 = repository.activePackagesFlow.first().first { it.diamonds == 100 }
        val orderResult = repository.placeOrder(
            userId = user.id,
            packageId = pkg100.id,
            playerUid = "1234567890",
            paymentMethod = PaymentMethod.JAZZCASH,
            transactionId = "TID99887766",
            screenshotPath = "/data/user/0/com.example/files/screenshots/receipt_1.jpg"
        )
        assertTrue(orderResult.isSuccess)
        val order = orderResult.getOrThrow()
        assertEquals(OrderStatus.PENDING_PAYMENT, order.status)
        assertEquals(220, order.totalPricePkr)

        // 3. Regular user cannot approve or issue free diamonds
        assertTrue(repository.updateOrderStatus(user.id, order.id, OrderStatus.PAYMENT_APPROVED).isFailure)
        assertTrue(repository.sendFreeDiamonds(user.id, user.id, 100, "Hack").isFailure)

        // 4. First-run Admin Claim (restricted to designated email) & Admin Login
        val reservedRes = repository.registerUser("Fake", StoreRepository.DESIGNATED_ADMIN_EMAIL, "123456", "pass123")
        assertTrue(reservedRes.isFailure)
        val wrongAdminRes = repository.claimFirstAdmin("Wrong", "other@example.com", "adminpass99")
        assertTrue(wrongAdminRes.isFailure)
        val adminRes = repository.claimFirstAdmin("Store Owner", StoreRepository.DESIGNATED_ADMIN_EMAIL, "adminpass99")
        assertTrue(adminRes.isSuccess)
        val admin = adminRes.getOrThrow()
        assertEquals(UserRole.ADMIN, admin.role)
        assertEquals("is4790isp@gmail.com", admin.email)
        val secondClaimRes = repository.claimFirstAdmin("Second", StoreRepository.DESIGNATED_ADMIN_EMAIL, "123456")
        assertTrue(secondClaimRes.isFailure)
        val adminLoginRes = repository.loginAdmin("is4790isp@gmail.com", "adminpass99")
        assertTrue(adminLoginRes.isSuccess)

        // 5. Admin Order Status Updates
        assertTrue(repository.updateOrderStatus(admin.id, order.id, OrderStatus.PAYMENT_UNDER_REVIEW, "Checking").isSuccess)
        assertTrue(repository.updateOrderStatus(admin.id, order.id, OrderStatus.PAYMENT_APPROVED, "Verified").isSuccess)
        assertTrue(repository.updateOrderStatus(admin.id, order.id, OrderStatus.PROCESSING, "Topping up").isSuccess)
        assertTrue(repository.updateOrderStatus(admin.id, order.id, OrderStatus.DELIVERED, "Sent").isSuccess)
        assertEquals(OrderStatus.DELIVERED, repository.observeOrdersForUser(user.id).first().first().status)

        // 6. Admin Give Free Diamonds (pending manual fulfilment until fulfilled)
        val reward = repository.sendFreeDiamonds(
            actingUserId = admin.id,
            targetUserId = user.id,
            diamonds = 210,
            reason = "Tournament Winner"
        ).getOrThrow()
        assertEquals(RewardStatus.PENDING_MANUAL_FULFILMENT, reward.status)
        assertEquals("Store Owner", reward.issuedByAdminName)

        assertTrue(repository.markRewardFulfilled(admin.id, reward.id).isSuccess)
        assertEquals(RewardStatus.FULFILLED, repository.observeRewardsForUser(user.id).first().first().status)

        // 7. Admin Package Editing, Adding, Deleting & Payment Config Update
        assertTrue(repository.savePackage(admin.id, pkg100.id, 100, 240, "Updated").isSuccess)
        assertEquals(240, repository.activePackagesFlow.first().first { it.id == pkg100.id }.pricePkr)

        assertTrue(repository.savePackage(admin.id, null, 75, 165, "New").isSuccess)
        assertEquals(10, repository.activePackagesFlow.first().size)

        assertTrue(
            repository.updatePaymentConfig(
                admin.id,
                PaymentMethod.EASYPAISA,
                "Updated Holder",
                "0345-9999999",
                "Send exact PKR"
            ).isSuccess
        )
        assertEquals(
            "0345-9999999",
            repository.paymentConfigsFlow.first().first { it.method == PaymentMethod.EASYPAISA }.accountNumber
        )
    }
}
