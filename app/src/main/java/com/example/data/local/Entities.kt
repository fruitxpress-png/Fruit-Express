package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val role: String, // "CUSTOMER", "ADMIN", "RIDER"
    val avatarUrl: String? = null
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val hindiName: String,
    val category: String, // "fruits", "vegetables", "leafy", "seasonal", "combos", "exotic", "dry_fruits"
    val unit: String, // e.g. "1 kg", "500 g", "1 bunch"
    val purchaseCost: Double,
    val sellingPrice: Double,
    val mrp: Double,
    val stock: Int,
    val isActive: Boolean = true,
    val isFeatured: Boolean = false,
    val isSeasonal: Boolean = false,
    val description: String = ""
)

@Entity(
    tableName = "saved_addresses",
    indices = [Index(value = ["userId"])]
)
data class SavedAddressEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val label: String, // "Home", "Work", "Other"
    val houseFlatNo: String,
    val buildingStreet: String,
    val locality: String,
    val sector: String,
    val city: String,
    val state: String,
    val pinCode: String,
    val landmark: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isDefault: Boolean = false
)

@Entity(
    tableName = "cart_items",
    indices = [Index(value = ["userId", "productId"], unique = true)]
)
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val productId: String,
    val quantity: Int
)

@Entity(
    tableName = "orders",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["idempotencyKey"], unique = true),
        Index(value = ["assignedRiderId"])
    ]
)
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val userId: String,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val deliveryPinCode: String,
    val deliverySlot: String,
    val subtotal: Double,
    val deliveryFee: Double,
    val discount: Double,
    val couponCode: String? = null,
    val total: Double,
    val paymentMethod: String, // "Cash on Delivery (COD)", "UPI Payment"
    val paymentStatus: String, // "PENDING", "PAID"
    val status: String, // "PLACED", "ACCEPTED", "PACKED", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED"
    val placedAt: Long = System.currentTimeMillis(),
    val assignedRiderId: String? = null,
    val assignedRiderName: String? = null,
    val assignedRiderPhone: String? = null,
    val codCollected: Boolean = false,
    val codCollectedAt: Long? = null,
    val cancellationReason: String? = null,
    val idempotencyKey: String
)

@Entity(
    tableName = "order_items",
    indices = [Index(value = ["orderId"])]
)
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: String,
    val productId: String,
    val productName: String,
    val hindiName: String,
    val unit: String,
    val unitPrice: Double,
    val quantity: Int,
    val totalPrice: Double
)

@Entity(tableName = "coupons")
data class CouponEntity(
    @PrimaryKey val code: String,
    val description: String,
    val discountPercentage: Double,
    val discountAmount: Double,
    val minOrderValue: Double,
    val maxDiscount: Double,
    val expiryDate: String,
    val isActive: Boolean = true
)

@Entity(tableName = "store_settings")
data class StoreSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val storeName: String = "Fruit Xpress",
    val contactPhone: String = "+91 98765 43210",
    val contactEmail: String = "support@fruitxpress.in",
    val operatingCity: String = "Faridabad, Haryana",
    val servicedPinCodes: String = "121001, 121002, 121003, 121004, 121005, 121006, 121007, 121008, 121009",
    val freeDeliveryThreshold: Double = 299.0,
    val deliveryFee: Double = 30.0,
    val minOrderValue: Double = 99.0,
    val isStoreOpen: Boolean = true,
    val upiId: String = "fruitxpress@okaxis"
)

@Entity(tableName = "delivery_slots")
data class DeliverySlotEntity(
    @PrimaryKey val id: String,
    val slotTitle: String,
    val timeWindow: String,
    val isActive: Boolean = true
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val performedBy: String,
    val action: String,
    val details: String
)
