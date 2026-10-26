package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDao
import com.example.data.local.AppDatabase
import com.example.data.local.AuditLogEntity
import com.example.data.local.CartItemEntity
import com.example.data.local.CouponEntity
import com.example.data.local.OrderEntity
import com.example.data.local.OrderItemEntity
import com.example.data.local.ProductEntity
import com.example.data.local.SavedAddressEntity
import com.example.data.local.StoreSettingsEntity
import com.example.data.local.UserEntity
import com.example.data.model.CartItemWithProduct
import com.example.data.model.CheckoutResult
import com.example.data.model.CheckoutSummary
import com.example.data.model.OrderStatus
import com.example.data.model.ProductCategory
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class FruitXpressRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val dao: AppDao = db.appDao()

    // Active User / Role State for seamless role testing and switching
    private val _currentUser = MutableStateFlow(
        UserEntity(
            id = "cust_sachin",
            name = "Sachin Kumar",
            phone = "9876543210",
            email = "sachin@fruitxpress.in",
            role = "CUSTOMER"
        )
    )
    val currentUser: StateFlow<UserEntity> = _currentUser.asStateFlow()

    fun switchUserRole(role: UserRole) {
        when (role) {
            UserRole.CUSTOMER -> {
                _currentUser.value = UserEntity(
                    id = "cust_sachin",
                    name = "Sachin Kumar",
                    phone = "9876543210",
                    email = "sachin@fruitxpress.in",
                    role = "CUSTOMER"
                )
            }
            UserRole.ADMIN -> {
                _currentUser.value = UserEntity(
                    id = "admin_main",
                    name = "Fruit Xpress Admin",
                    phone = "9999900000",
                    email = "admin@fruitxpress.in",
                    role = "ADMIN"
                )
            }
            UserRole.RIDER -> {
                _currentUser.value = UserEntity(
                    id = "rider_ravi",
                    name = "Ravi Sharma (Partner)",
                    phone = "9811122233",
                    email = "rider.ravi@fruitxpress.in",
                    role = "RIDER"
                )
            }
        }
    }

    // Categories
    val categories: List<ProductCategory> = listOf(
        ProductCategory("all", "All Items", "सभी वस्तुएं", "cat_all"),
        ProductCategory("fruits", "Fresh Fruits", "ताज़ा फल", "cat_fruits"),
        ProductCategory("vegetables", "Daily Veggies", "सब्जियाँ", "cat_veggies"),
        ProductCategory("leafy", "Leafy Herbs", "साग & पत्तियां", "cat_leafy"),
        ProductCategory("seasonal", "Seasonal Picks", "मौसमी फल & सब्जियां", "cat_seasonal"),
        ProductCategory("combos", "Family Combos", "कॉम्बो बास्केट", "cat_combos"),
        ProductCategory("exotic", "Exotic Produce", "विदेशी फल & सब्जियां", "cat_exotic"),
        ProductCategory("dry_fruits", "Dry Fruits", "सूखे मेवे", "cat_dryfruits")
    )

    // Products
    fun getActiveProducts(): Flow<List<ProductEntity>> = dao.getActiveProducts()
    fun getAllProducts(): Flow<List<ProductEntity>> = dao.getAllProducts()
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>> {
        return if (category == "all") dao.getActiveProducts() else dao.getProductsByCategory(category)
    }
    suspend fun getProductById(id: String): ProductEntity? = dao.getProductById(id)

    suspend fun saveProduct(product: ProductEntity) {
        dao.insertProduct(product)
        dao.insertAuditLog(
            AuditLogEntity(
                performedBy = _currentUser.value.name,
                action = "PRODUCT_SAVED",
                details = "Saved product ${product.name} (Stock: ${product.stock}, Price: ₹${product.sellingPrice})"
            )
        )
    }

    suspend fun deleteProduct(id: String) {
        dao.deleteProduct(id)
        dao.insertAuditLog(
            AuditLogEntity(
                performedBy = _currentUser.value.name,
                action = "PRODUCT_DELETED",
                details = "Deleted product ID $id"
            )
        )
    }

    suspend fun updateStock(productId: String, newStock: Int) {
        dao.updateProductStock(productId, newStock)
    }

    // Cart
    fun observeCart(): Flow<List<CartItemWithProduct>> = dao.observeCart(_currentUser.value.id)

    suspend fun addToCart(productId: String, delta: Int = 1) {
        val uid = _currentUser.value.id
        val product = dao.getProductById(productId) ?: return
        if (product.stock <= 0) return

        val existing = dao.getCartItem(uid, productId)
        if (existing == null) {
            val toAdd = delta.coerceAtMost(product.stock)
            if (toAdd > 0) {
                dao.insertCartItem(CartItemEntity(userId = uid, productId = productId, quantity = toAdd))
            }
        } else {
            val newQty = (existing.quantity + delta).coerceAtMost(product.stock)
            if (newQty <= 0) {
                dao.removeCartItem(uid, productId)
            } else {
                dao.updateCartItem(existing.copy(quantity = newQty))
            }
        }
    }

    suspend fun decrementCartItem(productId: String) {
        val uid = _currentUser.value.id
        val existing = dao.getCartItem(uid, productId) ?: return
        if (existing.quantity <= 1) {
            dao.removeCartItem(uid, productId)
        } else {
            dao.updateCartItem(existing.copy(quantity = existing.quantity - 1))
        }
    }

    suspend fun removeFromCart(productId: String) {
        dao.removeCartItem(_currentUser.value.id, productId)
    }

    suspend fun clearCart() {
        dao.clearCart(_currentUser.value.id)
    }

    suspend fun calculateCheckoutSummary(couponCode: String?): CheckoutSummary {
        val cartItems = dao.getCartItemsSync(_currentUser.value.id)
        val settings = dao.getStoreSettingsSync() ?: StoreSettingsEntity()

        val subtotal = cartItems.sumOf { it.price * it.quantity }
        val isFreeDelivery = subtotal >= settings.freeDeliveryThreshold
        val deliveryFee = if (subtotal == 0.0 || isFreeDelivery) 0.0 else settings.deliveryFee

        var discount = 0.0
        var validatedCode: String? = null
        if (!couponCode.isNullOrBlank()) {
            val coupon = dao.getCouponByCode(couponCode.trim().uppercase())
            if (coupon != null && coupon.isActive && subtotal >= coupon.minOrderValue) {
                val percentageDiscount = (subtotal * coupon.discountPercentage) / 100.0
                val totalDiscount = percentageDiscount + coupon.discountAmount
                discount = totalDiscount.coerceAtMost(coupon.maxDiscount)
                validatedCode = coupon.code
            }
        }

        val total = (subtotal + deliveryFee - discount).coerceAtLeast(0.0)
        val minOrderMet = subtotal >= settings.minOrderValue
        val minOrderDeficit = (settings.minOrderValue - subtotal).coerceAtLeast(0.0)

        return CheckoutSummary(
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            discount = discount,
            couponCode = validatedCode,
            total = total,
            isFreeDelivery = isFreeDelivery,
            minOrderMet = minOrderMet,
            minOrderDeficit = minOrderDeficit
        )
    }

    // Checkout with Atomic Transaction & Duplicate Protection
    suspend fun placeOrder(
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        deliveryPinCode: String,
        deliverySlot: String,
        paymentMethod: String,
        couponCode: String?,
        clientSubmissionToken: String = UUID.randomUUID().toString()
    ): CheckoutResult {
        return try {
            val uid = _currentUser.value.id
            val idempotencyKey = "order_${uid}_${clientSubmissionToken.take(16)}"
            val createdOrder = dao.executeAtomicCheckout(
                userId = uid,
                idempotencyKey = idempotencyKey,
                customerName = customerName,
                customerPhone = customerPhone,
                deliveryAddress = deliveryAddress,
                deliveryPinCode = deliveryPinCode,
                deliverySlot = deliverySlot,
                paymentMethod = paymentMethod,
                couponCode = couponCode
            )
            CheckoutResult.Success(createdOrder.orderId)
        } catch (e: Exception) {
            CheckoutResult.Error(e.message ?: "Failed to process checkout transaction.")
        }
    }

    // Saved Addresses
    fun getSavedAddresses(): Flow<List<SavedAddressEntity>> = dao.getAddressesByUserId(_currentUser.value.id)

    suspend fun saveAddress(address: SavedAddressEntity) {
        if (address.isDefault) {
            dao.clearDefaultAddress(_currentUser.value.id)
        }
        dao.insertAddress(address.copy(userId = _currentUser.value.id))
    }

    suspend fun deleteAddress(addressId: String) {
        dao.deleteAddress(addressId)
    }

    // Orders Observation
    fun getCustomerOrders(): Flow<List<OrderEntity>> = dao.getOrdersByUserId(_currentUser.value.id)
    fun getAllOrders(): Flow<List<OrderEntity>> = dao.getAllOrders()
    fun getRiderOrders(riderId: String): Flow<List<OrderEntity>> = dao.getOrdersByRiderId(riderId)
    fun observeOrderById(orderId: String): Flow<OrderEntity?> = dao.observeOrderById(orderId)
    fun observeOrderItems(orderId: String): Flow<List<OrderItemEntity>> = dao.observeOrderItems(orderId)
    suspend fun getOrderItems(orderId: String): List<OrderItemEntity> = dao.getOrderItems(orderId)

    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        dao.updateOrderStatus(orderId, newStatus.name)
        dao.insertAuditLog(
            AuditLogEntity(
                performedBy = _currentUser.value.name,
                action = "ORDER_STATUS_UPDATED",
                details = "Order $orderId status changed to ${newStatus.label}"
            )
        )
    }

    suspend fun assignOrderToRider(orderId: String, riderId: String, riderName: String, riderPhone: String) {
        dao.assignOrderToRider(orderId, riderId, riderName, riderPhone)
        dao.updateOrderStatus(orderId, OrderStatus.ACCEPTED.name)
        dao.insertAuditLog(
            AuditLogEntity(
                performedBy = _currentUser.value.name,
                action = "ORDER_RIDER_ASSIGNED",
                details = "Order $orderId assigned to rider $riderName ($riderPhone)"
            )
        )
    }

    suspend fun riderAcceptAssignment(orderId: String, rider: UserEntity) {
        dao.assignOrderToRider(orderId, rider.id, rider.name, rider.phone)
        dao.updateOrderStatus(orderId, OrderStatus.ACCEPTED.name)
        dao.insertAuditLog(
            AuditLogEntity(
                performedBy = rider.name,
                action = "ORDER_ACCEPTED_BY_RIDER",
                details = "Order $orderId accepted by rider ${rider.name}"
            )
        )
    }

    suspend fun markOrderDelivered(orderId: String, isCodCollected: Boolean) {
        dao.updateOrderStatus(orderId, OrderStatus.DELIVERED.name)
        if (isCodCollected) {
            dao.markCodCollected(orderId)
        }
        dao.insertAuditLog(
            AuditLogEntity(
                performedBy = _currentUser.value.name,
                action = "ORDER_DELIVERED",
                details = "Order $orderId marked delivered. COD Collected: $isCodCollected"
            )
        )
    }

    suspend fun cancelOrder(orderId: String, reason: String) {
        dao.cancelOrder(orderId, reason)
        dao.insertAuditLog(
            AuditLogEntity(
                performedBy = _currentUser.value.name,
                action = "ORDER_CANCELLED",
                details = "Order $orderId cancelled: $reason"
            )
        )
    }

    // Store Settings & Delivery Slots
    fun observeStoreSettings(): Flow<StoreSettingsEntity?> = dao.observeStoreSettings()
    suspend fun updateStoreSettings(settings: StoreSettingsEntity) {
        dao.updateStoreSettings(settings)
    }
    fun getDeliverySlots(): Flow<List<com.example.data.local.DeliverySlotEntity>> = dao.getActiveDeliverySlots()
    fun getActiveCoupons(): Flow<List<CouponEntity>> = dao.getActiveCoupons()
    fun getAuditLogs(): Flow<List<AuditLogEntity>> = dao.getAuditLogs()

    // Riders List
    fun getRidersList(): Flow<List<UserEntity>> = dao.getUsersByRole("RIDER")
}
