package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.RewardStatus
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {
    // --- Users ---
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun observeUserById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users WHERE role = :role")
    suspend fun countUsersByRole(role: UserRole = UserRole.ADMIN): Int

    @Query("SELECT COUNT(*) FROM users WHERE role = :role")
    fun observeAdminCount(role: UserRole = UserRole.ADMIN): Flow<Int>

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun observeAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE playerUid LIKE '%' || :query || '%' OR LOWER(fullName) LIKE '%' || LOWER(:query) || '%' OR LOWER(email) LIKE '%' || LOWER(:query) || '%' ORDER BY createdAt DESC")
    fun searchUsers(query: String): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity)

    @Query("UPDATE users SET playerUid = :playerUid WHERE id = :userId")
    suspend fun updateUserPlayerUid(userId: String, playerUid: String)

    // --- Diamond Packages ---
    @Query("SELECT * FROM diamond_packages WHERE isActive = 1 ORDER BY diamonds ASC")
    fun observeActivePackages(): Flow<List<DiamondPackageEntity>>

    @Query("SELECT * FROM diamond_packages ORDER BY diamonds ASC")
    fun observeAllPackages(): Flow<List<DiamondPackageEntity>>

    @Query("SELECT * FROM diamond_packages WHERE id = :packageId LIMIT 1")
    suspend fun getPackageById(packageId: Int): DiamondPackageEntity?

    @Query("SELECT COUNT(*) FROM diamond_packages")
    suspend fun countPackages(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackage(pkg: DiamondPackageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackages(packages: List<DiamondPackageEntity>)

    @Update
    suspend fun updatePackage(pkg: DiamondPackageEntity)

    @Query("DELETE FROM diamond_packages WHERE id = :packageId")
    suspend fun deletePackageById(packageId: Int)

    // --- Payment Configs ---
    @Query("SELECT * FROM payment_configs ORDER BY method ASC")
    fun observePaymentConfigs(): Flow<List<PaymentConfigEntity>>

    @Query("SELECT * FROM payment_configs WHERE method = :method LIMIT 1")
    suspend fun getPaymentConfig(method: PaymentMethod): PaymentConfigEntity?

    @Query("SELECT COUNT(*) FROM payment_configs")
    suspend fun countPaymentConfigs(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPaymentConfig(config: PaymentConfigEntity)

    // --- Orders ---
    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeOrdersForUser(userId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun observeAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: String): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertOrder(order: OrderEntity)

    @Query("UPDATE orders SET status = :status, adminNote = :adminNote, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus, adminNote: String, updatedAt: Long)

    // --- Free Rewards ---
    @Query("SELECT * FROM free_rewards WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeRewardsForUser(userId: String): Flow<List<FreeRewardEntity>>

    @Query("SELECT * FROM free_rewards ORDER BY createdAt DESC")
    fun observeAllRewards(): Flow<List<FreeRewardEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertFreeReward(reward: FreeRewardEntity)

    @Query("UPDATE free_rewards SET status = :status, updatedAt = :updatedAt WHERE id = :rewardId")
    suspend fun updateRewardStatus(rewardId: String, status: RewardStatus, updatedAt: Long)

    // --- Notifications ---
    @Query("SELECT * FROM notifications WHERE userId = :userId OR userId IS NULL ORDER BY createdAt DESC")
    fun observeNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun observeAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markNotificationRead(notificationId: Int)

    // --- Support Messages ---
    @Query("SELECT * FROM support_messages ORDER BY createdAt DESC")
    fun observeAllSupportMessages(): Flow<List<SupportMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupportMessage(message: SupportMessageEntity)

    @Query("UPDATE support_messages SET status = :status WHERE id = :messageId")
    suspend fun updateSupportMessageStatus(messageId: Int, status: String)
}
