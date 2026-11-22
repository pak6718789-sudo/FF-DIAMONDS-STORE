package com.example.data.repository

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
import com.example.security.PasswordHasher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class StoreRepository(
    private val dao: StoreDao,
    private val sessionManager: SessionStore
) {
    private val seedMutex = Mutex()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentUserFlow: Flow<UserEntity?> = flow {
        ensureSeeded()
        emitAll(
            sessionManager.currentUserIdFlow.flatMapLatest { userId ->
                if (userId == null) flowOf(null) else dao.observeUserById(userId)
            }
        )
    }

    val activePackagesFlow: Flow<List<DiamondPackageEntity>> = flow {
        ensureSeeded()
        emitAll(dao.observeActivePackages())
    }

    val allPackagesFlow: Flow<List<DiamondPackageEntity>> = flow {
        ensureSeeded()
        emitAll(dao.observeAllPackages())
    }

    val paymentConfigsFlow: Flow<List<PaymentConfigEntity>> = flow {
        ensureSeeded()
        emitAll(dao.observePaymentConfigs())
    }

    val adminCountFlow: Flow<Int> = dao.observeAdminCount()

    fun observeOrdersForUser(userId: String): Flow<List<OrderEntity>> =
        dao.observeOrdersForUser(userId)

    fun observeRewardsForUser(userId: String): Flow<List<FreeRewardEntity>> =
        dao.observeRewardsForUser(userId)

    fun observeNotificationsForUser(userId: String): Flow<List<NotificationEntity>> =
        dao.observeNotificationsForUser(userId)

    fun observeAllOrders(): Flow<List<OrderEntity>> = dao.observeAllOrders()

    fun observeAllUsers(): Flow<List<UserEntity>> = dao.observeAllUsers()

    fun searchUsers(query: String): Flow<List<UserEntity>> =
        if (query.isBlank()) dao.observeAllUsers() else dao.searchUsers(query.trim())

    fun observeAllRewards(): Flow<List<FreeRewardEntity>> = dao.observeAllRewards()

    fun observeAllSupportMessages(): Flow<List<SupportMessageEntity>> =
        dao.observeAllSupportMessages()

    suspend fun ensureSeeded() {
        seedMutex.withLock {
            if (dao.countPackages() == 0) {
                dao.insertPackages(defaultPackages())
            }
            if (dao.countPaymentConfigs() == 0) {
                dao.upsertPaymentConfig(
                    PaymentConfigEntity(
                        method = PaymentMethod.JAZZCASH,
                        accountHolderName = "FF Store Official (Demo)",
                        accountNumber = "0300-0000000",
                        instructions = "1. Open JazzCash app or dial *786#.\n2. Send exact PKR amount to the account above.\n3. Save your Transaction ID (TID) and receipt screenshot.\n4. Never share your MPIN or OTP with anyone."
                    )
                )
                dao.upsertPaymentConfig(
                    PaymentConfigEntity(
                        method = PaymentMethod.EASYPAISA,
                        accountHolderName = "FF Store Official (Demo)",
                        accountNumber = "0345-0000000",
                        instructions = "1. Open EasyPaisa app or dial *786#.\n2. Transfer exact PKR amount to the account above.\n3. Copy the Transaction ID (TID) and capture receipt screenshot.\n4. Never share your PIN or OTP."
                    )
                )
            }
        }
    }

    // --- Authentication ---

    suspend fun registerUser(
        fullName: String,
        email: String,
        playerUid: String,
        password: String
    ): Result<UserEntity> {
        ensureSeeded()
        val trimmedName = fullName.trim()
        val trimmedEmail = email.trim().lowercase()
        val trimmedUid = playerUid.trim()

        if (trimmedName.length < 2) {
            return Result.failure(IllegalArgumentException("Please enter your full name."))
        }
        if (!isValidEmail(trimmedEmail)) {
            return Result.failure(IllegalArgumentException("Enter a valid email address."))
        }
        if (trimmedEmail == DESIGNATED_ADMIN_EMAIL) {
            return Result.failure(IllegalStateException("This email is reserved for Admin access. Use the Admin Portal below."))
        }
        if (!isValidPlayerUid(trimmedUid)) {
            return Result.failure(IllegalArgumentException("Enter a valid numeric Free Fire Player UID (6 to 15 digits)."))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }
        if (dao.getUserByEmail(trimmedEmail) != null) {
            return Result.failure(IllegalStateException("An account with this email already exists."))
        }

        val hashed = PasswordHasher.hashPassword(password)
        val user = UserEntity(
            id = "USR-${UUID.randomUUID().toString().take(8).uppercase()}",
            fullName = trimmedName,
            email = trimmedEmail,
            playerUid = trimmedUid,
            passwordHash = hashed.hashBase64,
            passwordSalt = hashed.saltBase64,
            role = UserRole.USER // Never allow self-assigned ADMIN during user registration
        )
        dao.insertUser(user)
        sessionManager.setCurrentUserId(user.id)
        dao.insertNotification(
            NotificationEntity(
                userId = user.id,
                title = "Welcome to FF Diamond Store",
                message = "Your Player UID $trimmedUid is linked. Select any PKR diamond package to place an order."
            )
        )
        return Result.success(user)
    }

    suspend fun loginUser(email: String, password: String): Result<UserEntity> {
        ensureSeeded()
        val trimmedEmail = email.trim().lowercase()
        if (!isValidEmail(trimmedEmail)) {
            return Result.failure(IllegalArgumentException("Enter a valid email address."))
        }
        val user = dao.getUserByEmail(trimmedEmail)
            ?: return Result.failure(IllegalArgumentException("Invalid email or password."))

        if (!PasswordHasher.verifyPassword(password, user.passwordHash, user.passwordSalt)) {
            return Result.failure(IllegalArgumentException("Invalid email or password."))
        }

        sessionManager.setCurrentUserId(user.id)
        return Result.success(user)
    }

    suspend fun claimFirstAdmin(
        fullName: String,
        email: String,
        passcode: String
    ): Result<UserEntity> {
        ensureSeeded()
        if (dao.countUsersByRole(UserRole.ADMIN) > 0) {
            return Result.failure(IllegalStateException("An Admin account has already been claimed."))
        }
        val trimmedName = fullName.trim()
        val trimmedEmail = email.trim().lowercase()
        if (trimmedName.length < 2) {
            return Result.failure(IllegalArgumentException("Enter an Admin display name."))
        }
        if (!isValidEmail(trimmedEmail)) {
            return Result.failure(IllegalArgumentException("Enter a valid Admin email address."))
        }
        if (trimmedEmail != DESIGNATED_ADMIN_EMAIL) {
            return Result.failure(SecurityException("Only $DESIGNATED_ADMIN_EMAIL is authorized to claim Admin access."))
        }
        if (passcode.length < 6) {
            return Result.failure(IllegalArgumentException("Admin passcode must be at least 6 characters."))
        }
        if (dao.getUserByEmail(trimmedEmail) != null) {
            return Result.failure(IllegalStateException("Email is already registered as a standard user. Use a dedicated Admin email."))
        }

        val hashed = PasswordHasher.hashPassword(passcode)
        val admin = UserEntity(
            id = "ADM-${UUID.randomUUID().toString().take(8).uppercase()}",
            fullName = trimmedName,
            email = trimmedEmail,
            playerUid = "ADMIN",
            passwordHash = hashed.hashBase64,
            passwordSalt = hashed.saltBase64,
            role = UserRole.ADMIN
        )
        dao.insertUser(admin)
        sessionManager.setCurrentUserId(admin.id)
        return Result.success(admin)
    }

    suspend fun loginAdmin(email: String, passcode: String): Result<UserEntity> {
        ensureSeeded()
        val trimmedEmail = email.trim().lowercase()
        val user = dao.getUserByEmail(trimmedEmail)
            ?: return Result.failure(IllegalArgumentException("Invalid admin credentials."))
        if (user.role != UserRole.ADMIN) {
            return Result.failure(SecurityException("This account does not have Admin privileges."))
        }
        if (!PasswordHasher.verifyPassword(passcode, user.passwordHash, user.passwordSalt)) {
            return Result.failure(IllegalArgumentException("Invalid admin credentials."))
        }
        sessionManager.setCurrentUserId(user.id)
        return Result.success(user)
    }

    suspend fun logout() {
        sessionManager.clearSession()
    }

    suspend fun updatePlayerUid(userId: String, newPlayerUid: String): Result<Unit> {
        val trimmedUid = newPlayerUid.trim()
        if (!isValidPlayerUid(trimmedUid)) {
            return Result.failure(IllegalArgumentException("Enter a valid numeric Free Fire Player UID (6 to 15 digits)."))
        }
        dao.getUserById(userId)
            ?: return Result.failure(IllegalStateException("User not found."))
        dao.updateUserPlayerUid(userId, trimmedUid)
        return Result.success(Unit)
    }

    // --- User Checkout & Orders ---

    suspend fun placeOrder(
        userId: String,
        packageId: Int,
        playerUid: String,
        paymentMethod: PaymentMethod,
        transactionId: String,
        screenshotPath: String
    ): Result<OrderEntity> {
        ensureSeeded()
        val user = dao.getUserById(userId)
            ?: return Result.failure(IllegalStateException("Please sign in before placing an order."))

        val pkg = dao.getPackageById(packageId)
            ?: return Result.failure(IllegalArgumentException("Selected diamond package no longer exists."))

        val trimmedUid = playerUid.trim()
        if (!isValidPlayerUid(trimmedUid)) {
            return Result.failure(IllegalArgumentException("Enter a valid numeric Free Fire Player UID (6 to 15 digits)."))
        }
        val trimmedTid = transactionId.trim()
        if (trimmedTid.length < 4) {
            return Result.failure(IllegalArgumentException("Please enter a valid Transaction Reference ID."))
        }
        if (screenshotPath.isBlank()) {
            return Result.failure(IllegalArgumentException("Please upload your payment receipt screenshot."))
        }

        val now = System.currentTimeMillis()
        val order = OrderEntity(
            id = "ORD-${UUID.randomUUID().toString().take(8).uppercase()}",
            userId = user.id,
            userName = user.fullName,
            playerUid = trimmedUid,
            diamonds = pkg.diamonds,
            totalPricePkr = pkg.pricePkr,
            paymentMethod = paymentMethod,
            transactionId = trimmedTid,
            screenshotPath = screenshotPath,
            status = OrderStatus.PENDING_PAYMENT,
            adminNote = "",
            createdAt = now,
            updatedAt = now
        )
        dao.insertOrder(order)
        dao.insertNotification(
            NotificationEntity(
                userId = user.id,
                title = "Order Placed: ${order.id}",
                message = "${order.diamonds} Diamonds (Rs. ${order.totalPricePkr}) via ${paymentMethod.displayName} is Pending Payment verification."
            )
        )
        return Result.success(order)
    }

    suspend fun markNotificationRead(notificationId: Int) {
        dao.markNotificationRead(notificationId)
    }

    suspend fun submitSupportMessage(
        userId: String?,
        name: String,
        email: String,
        playerUid: String,
        subject: String,
        message: String
    ): Result<Unit> {
        if (name.trim().length < 2) {
            return Result.failure(IllegalArgumentException("Please enter your name."))
        }
        if (!isValidEmail(email.trim())) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (subject.trim().length < 3 || message.trim().length < 5) {
            return Result.failure(IllegalArgumentException("Please enter a subject and message."))
        }
        dao.insertSupportMessage(
            SupportMessageEntity(
                userId = userId,
                name = name.trim(),
                email = email.trim(),
                playerUid = playerUid.trim(),
                subject = subject.trim(),
                message = message.trim()
            )
        )
        return Result.success(Unit)
    }

    // --- Admin Actions (Strictly Role-Verified) ---

    private suspend fun requireAdmin(actingUserId: String): UserEntity {
        val actor = dao.getUserById(actingUserId)
            ?: throw SecurityException("Authentication required.")
        if (actor.role != UserRole.ADMIN) {
            throw SecurityException("Admin authorization required.")
        }
        return actor
    }

    suspend fun updateOrderStatus(
        actingUserId: String,
        orderId: String,
        newStatus: OrderStatus,
        adminNote: String = ""
    ): Result<Unit> {
        return try {
            requireAdmin(actingUserId)
            val order = dao.getOrderById(orderId)
                ?: return Result.failure(IllegalArgumentException("Order not found."))
            val now = System.currentTimeMillis()
            dao.updateOrderStatus(orderId, newStatus, adminNote.trim(), now)
            val noteSuffix = if (adminNote.isNotBlank()) " Note: ${adminNote.trim()}" else ""
            dao.insertNotification(
                NotificationEntity(
                    userId = order.userId,
                    title = "Order ${order.id}: ${newStatus.label}",
                    message = "Your ${order.diamonds} Diamonds order for UID ${order.playerUid} is now ${newStatus.label}.$noteSuffix"
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun savePackage(
        actingUserId: String,
        packageId: Int?,
        diamonds: Int,
        pricePkr: Int,
        badge: String?
    ): Result<Unit> {
        return try {
            requireAdmin(actingUserId)
            if (diamonds <= 0) {
                return Result.failure(IllegalArgumentException("Diamond quantity must be greater than 0."))
            }
            if (pricePkr <= 0) {
                return Result.failure(IllegalArgumentException("Price in PKR must be greater than 0."))
            }
            val cleanBadge = badge?.trim()?.takeIf { it.isNotEmpty() }
            if (packageId == null || packageId == 0) {
                dao.insertPackage(
                    DiamondPackageEntity(
                        diamonds = diamonds,
                        pricePkr = pricePkr,
                        badge = cleanBadge,
                        isActive = true,
                        sortOrder = diamonds
                    )
                )
            } else {
                val existing = dao.getPackageById(packageId)
                    ?: return Result.failure(IllegalArgumentException("Package not found."))
                dao.updatePackage(
                    existing.copy(
                        diamonds = diamonds,
                        pricePkr = pricePkr,
                        badge = cleanBadge,
                        sortOrder = diamonds
                    )
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePackage(actingUserId: String, packageId: Int): Result<Unit> {
        return try {
            requireAdmin(actingUserId)
            dao.deletePackageById(packageId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePaymentConfig(
        actingUserId: String,
        method: PaymentMethod,
        accountHolderName: String,
        accountNumber: String,
        instructions: String
    ): Result<Unit> {
        return try {
            requireAdmin(actingUserId)
            if (accountHolderName.trim().length < 2) {
                return Result.failure(IllegalArgumentException("Account holder name is required."))
            }
            if (accountNumber.trim().length < 7) {
                return Result.failure(IllegalArgumentException("Valid account number is required."))
            }
            if (instructions.trim().length < 5) {
                return Result.failure(IllegalArgumentException("Payment instructions are required."))
            }
            dao.upsertPaymentConfig(
                PaymentConfigEntity(
                    method = method,
                    accountHolderName = accountHolderName.trim(),
                    accountNumber = accountNumber.trim(),
                    instructions = instructions.trim(),
                    updatedAt = System.currentTimeMillis()
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendFreeDiamonds(
        actingUserId: String,
        targetUserId: String,
        diamonds: Int,
        reason: String
    ): Result<FreeRewardEntity> {
        return try {
            val admin = requireAdmin(actingUserId)
            val targetUser = dao.getUserById(targetUserId)
                ?: return Result.failure(IllegalArgumentException("Selected user not found."))
            if (diamonds <= 0 || diamonds > 100_000) {
                return Result.failure(IllegalArgumentException("Enter a valid diamond reward quantity (1 - 100,000)."))
            }
            val cleanReason = reason.trim()
            if (cleanReason.length < 3) {
                return Result.failure(IllegalArgumentException("Please enter a reward reason."))
            }
            val now = System.currentTimeMillis()
            val reward = FreeRewardEntity(
                id = "RWD-${UUID.randomUUID().toString().take(8).uppercase()}",
                userId = targetUser.id,
                userName = targetUser.fullName,
                playerUid = targetUser.playerUid,
                diamonds = diamonds,
                reason = cleanReason,
                issuedByAdminId = admin.id,
                issuedByAdminName = admin.fullName,
                status = RewardStatus.PENDING_MANUAL_FULFILMENT, // Never falsely mark delivered on creation
                createdAt = now,
                updatedAt = now
            )
            dao.insertFreeReward(reward)
            dao.insertNotification(
                NotificationEntity(
                    userId = targetUser.id,
                    title = "Free Diamond Reward: +$diamonds Diamonds",
                    message = "Admin ${admin.fullName} issued a reward for \"$cleanReason\" (UID ${targetUser.playerUid}). Status: Pending Manual Fulfilment."
                )
            )
            Result.success(reward)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markRewardFulfilled(actingUserId: String, rewardId: String): Result<Unit> {
        return try {
            requireAdmin(actingUserId)
            dao.updateRewardStatus(rewardId, RewardStatus.FULFILLED, System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendNotification(
        actingUserId: String,
        targetUserId: String?,
        title: String,
        message: String
    ): Result<Unit> {
        return try {
            requireAdmin(actingUserId)
            if (title.trim().length < 2 || message.trim().length < 3) {
                return Result.failure(IllegalArgumentException("Notification title and message are required."))
            }
            dao.insertNotification(
                NotificationEntity(
                    userId = targetUserId?.takeIf { it.isNotBlank() },
                    title = title.trim(),
                    message = message.trim()
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resolveSupportMessage(actingUserId: String, messageId: Int): Result<Unit> {
        return try {
            requireAdmin(actingUserId)
            dao.updateSupportMessageStatus(messageId, "Resolved")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        const val DESIGNATED_ADMIN_EMAIL = "is4790isp@gmail.com"

        fun isValidPlayerUid(uid: String): Boolean =
            uid.matches(Regex("^[0-9]{6,15}$"))

        fun isValidEmail(email: String): Boolean =
            email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))

        fun defaultPackages(): List<DiamondPackageEntity> = listOf(
            DiamondPackageEntity(diamonds = 25, pricePkr = 60, badge = "Starter", sortOrder = 1),
            DiamondPackageEntity(diamonds = 50, pricePkr = 110, badge = null, sortOrder = 2),
            DiamondPackageEntity(diamonds = 100, pricePkr = 220, badge = "Popular", sortOrder = 3),
            DiamondPackageEntity(diamonds = 210, pricePkr = 430, badge = null, sortOrder = 4),
            DiamondPackageEntity(diamonds = 310, pricePkr = 630, badge = "Hot", sortOrder = 5),
            DiamondPackageEntity(diamonds = 520, pricePkr = 1050, badge = "Best Value", sortOrder = 6),
            DiamondPackageEntity(diamonds = 1060, pricePkr = 2100, badge = null, sortOrder = 7),
            DiamondPackageEntity(diamonds = 2180, pricePkr = 4200, badge = "Pro Pack", sortOrder = 8),
            DiamondPackageEntity(diamonds = 5600, pricePkr = 10500, badge = "Mega Pack", sortOrder = 9)
        )
    }
}
