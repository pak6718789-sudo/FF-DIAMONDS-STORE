package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.RewardStatus
import com.example.data.model.UserRole

class StoreTypeConverters {
    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name

    @TypeConverter
    fun toUserRole(value: String): UserRole =
        enumValues<UserRole>().firstOrNull { it.name == value } ?: UserRole.USER

    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod): String = value.name

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod =
        enumValues<PaymentMethod>().firstOrNull { it.name == value } ?: PaymentMethod.JAZZCASH

    @TypeConverter
    fun fromOrderStatus(value: OrderStatus): String = value.name

    @TypeConverter
    fun toOrderStatus(value: String): OrderStatus =
        enumValues<OrderStatus>().firstOrNull { it.name == value } ?: OrderStatus.PENDING_PAYMENT

    @TypeConverter
    fun fromRewardStatus(value: RewardStatus): String = value.name

    @TypeConverter
    fun toRewardStatus(value: String): RewardStatus =
        enumValues<RewardStatus>().firstOrNull { it.name == value }
            ?: RewardStatus.PENDING_MANUAL_FULFILMENT
}

@Database(
    entities = [
        UserEntity::class,
        DiamondPackageEntity::class,
        PaymentConfigEntity::class,
        OrderEntity::class,
        FreeRewardEntity::class,
        NotificationEntity::class,
        SupportMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(StoreTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun storeDao(): StoreDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ff_diamond_store.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
