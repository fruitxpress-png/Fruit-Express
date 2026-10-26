package com.example.data.model

enum class UserRole {
    CUSTOMER,
    ADMIN,
    RIDER
}

enum class OrderStatus(val label: String, val stepIndex: Int) {
    PLACED("Order Placed", 0),
    ACCEPTED("Order Accepted", 1),
    PACKED("Packed & Ready", 2),
    OUT_FOR_DELIVERY("Out for Delivery", 3),
    DELIVERED("Delivered", 4),
    CANCELLED("Cancelled", -1)
}

enum class PaymentMethod(val label: String) {
    COD("Cash on Delivery (COD)"),
    UPI("UPI Payment")
}

data class ProductCategory(
    val id: String,
    val name: String,
    val hindiName: String,
    val iconName: String
)

data class CartItemWithProduct(
    val cartItemId: Long,
    val productId: String,
    val name: String,
    val hindiName: String,
    val unit: String,
    val price: Double,
    val mrp: Double,
    val quantity: Int,
    val stock: Int
)

data class OrderWithItems(
    val order: com.example.data.local.OrderEntity,
    val items: List<com.example.data.local.OrderItemEntity>
)

data class CheckoutSummary(
    val subtotal: Double,
    val deliveryFee: Double,
    val discount: Double,
    val couponCode: String?,
    val total: Double,
    val isFreeDelivery: Boolean,
    val minOrderMet: Boolean,
    val minOrderDeficit: Double
)

sealed interface CheckoutResult {
    data class Success(val orderId: String) : CheckoutResult
    data class Error(val message: String) : CheckoutResult
}
