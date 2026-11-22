package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.RewardStatus
import com.example.data.model.UserRole

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["playerUid"])
    ]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val email: String,
    val playerUid: String,
    val passwordHash: String,
    val passwordSalt: String,
    val role: UserRole = UserRole.USER,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "diamond_packages")
data class DiamondPackageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val diamonds: Int,
    val pricePkr: Int,
    val badge: String? = null,
    val isActive: Boolean = true,
    val sortOrder: Int = 0
)

@Entity(tableName = "payment_configs")
data class PaymentConfigEntity(
    @PrimaryKey val method: PaymentMethod,
    val accountHolderName: String,
    val accountNumber: String,
    val instructions: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "orders",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["status"])
    ]
)
data class OrderEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val playerUid: String,
    val diamonds: Int,
    val totalPricePkr: Int,
    val paymentMethod: PaymentMethod,
    val transactionId: String,
    val screenshotPath: String,
    val status: OrderStatus = OrderStatus.PENDING_PAYMENT,
    val adminNote: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "free_rewards",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["playerUid"])
    ]
)
data class FreeRewardEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val playerUid: String,
    val diamonds: Int,
    val reason: String,
    val issuedByAdminId: String,
    val issuedByAdminName: String,
    val status: RewardStatus = RewardStatus.PENDING_MANUAL_FULFILMENT,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "notifications",
    indices = [Index(value = ["userId"])]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String?,
    val title: String,
    val message: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "support_messages")
data class SupportMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String?,
    val name: String,
    val email: String,
    val playerUid: String,
    val subject: String,
    val message: String,
    val status: String = "Open",
    val createdAt: Long = System.currentTimeMillis()
)
