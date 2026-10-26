package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.CartItemWithProduct
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- Users ---
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE role = :role")
    fun getUsersByRole(role: String): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    // --- Products ---
    @Query("SELECT * FROM products ORDER BY isFeatured DESC, name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY isFeatured DESC, name ASC")
    fun getActiveProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE category = :category AND isActive = 1")
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET stock = :newStock WHERE id = :productId")
    suspend fun updateProductStock(productId: String, newStock: Int)

    @Query("UPDATE products SET isActive = :isActive WHERE id = :productId")
    suspend fun updateProductActiveStatus(productId: String, isActive: Boolean)

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProduct(productId: String)

    // --- Saved Addresses ---
    @Query("SELECT * FROM saved_addresses WHERE userId = :userId ORDER BY isDefault DESC, label ASC")
    fun getAddressesByUserId(userId: String): Flow<List<SavedAddressEntity>>

    @Query("SELECT * FROM saved_addresses WHERE id = :addressId LIMIT 1")
    suspend fun getAddressById(addressId: String): SavedAddressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: SavedAddressEntity)

    @Update
    suspend fun updateAddress(address: SavedAddressEntity)

    @Query("DELETE FROM saved_addresses WHERE id = :addressId")
    suspend fun deleteAddress(addressId: String)

    @Query("UPDATE saved_addresses SET isDefault = 0 WHERE userId = :userId")
    suspend fun clearDefaultAddress(userId: String)

    // --- Cart ---
    @Query("""
        SELECT c.id as cartItemId, p.id as productId, p.name, p.hindiName, p.unit, 
               p.sellingPrice as price, p.mrp, c.quantity, p.stock
        FROM cart_items c
        INNER JOIN products p ON c.productId = p.id
        WHERE c.userId = :userId
    """)
    fun observeCart(userId: String): Flow<List<CartItemWithProduct>>

    @Query("""
        SELECT c.id as cartItemId, p.id as productId, p.name, p.hindiName, p.unit, 
               p.sellingPrice as price, p.mrp, c.quantity, p.stock
        FROM cart_items c
        INNER JOIN products p ON c.productId = p.id
        WHERE c.userId = :userId
    """)
    suspend fun getCartItemsSync(userId: String): List<CartItemWithProduct>

    @Query("SELECT * FROM cart_items WHERE userId = :userId AND productId = :productId LIMIT 1")
    suspend fun getCartItem(userId: String, productId: String): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItemEntity)

    @Update
    suspend fun updateCartItem(cartItem: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE userId = :userId AND productId = :productId")
    suspend fun removeCartItem(userId: String, productId: String)

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clearCart(userId: String)

    // --- Orders ---
    @Query("SELECT * FROM orders ORDER BY placedAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY placedAt DESC")
    fun getOrdersByUserId(userId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE assignedRiderId = :riderId ORDER BY placedAt DESC")
    fun getOrdersByRiderId(riderId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE orderId = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE orderId = :orderId LIMIT 1")
    fun observeOrderById(orderId: String): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE idempotencyKey = :idempotencyKey LIMIT 1")
    suspend fun getOrderByDeliveryIdempotency(idempotencyKey: String): OrderEntity?

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getOrderItems(orderId: String): List<OrderItemEntity>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun observeOrderItems(orderId: String): Flow<List<OrderItemEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE orders SET status = :status WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String)

    @Query("""
        UPDATE orders 
        SET assignedRiderId = :riderId, assignedRiderName = :riderName, assignedRiderPhone = :riderPhone 
        WHERE orderId = :orderId
    """)
    suspend fun assignOrderToRider(orderId: String, riderId: String, riderName: String, riderPhone: String)

    @Query("UPDATE orders SET codCollected = 1, codCollectedAt = :collectedAt, paymentStatus = 'PAID' WHERE orderId = :orderId")
    suspend fun markCodCollected(orderId: String, collectedAt: Long = System.currentTimeMillis())

    @Query("UPDATE orders SET status = 'CANCELLED', cancellationReason = :reason WHERE orderId = :orderId")
    suspend fun cancelOrder(orderId: String, reason: String)

    // --- Coupons ---
    @Query("SELECT * FROM coupons WHERE isActive = 1")
    fun getActiveCoupons(): Flow<List<CouponEntity>>

    @Query("SELECT * FROM coupons WHERE code = :code AND isActive = 1 LIMIT 1")
    suspend fun getCouponByCode(code: String): CouponEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupons(coupons: List<CouponEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupon(coupon: CouponEntity)

    @Update
    suspend fun updateCoupon(coupon: CouponEntity)

    @Delete
    suspend fun deleteCoupon(coupon: CouponEntity)

    // --- Store Settings ---
    @Query("SELECT * FROM store_settings WHERE id = 1 LIMIT 1")
    fun observeStoreSettings(): Flow<StoreSettingsEntity?>

    @Query("SELECT * FROM store_settings WHERE id = 1 LIMIT 1")
    suspend fun getStoreSettingsSync(): StoreSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateStoreSettings(settings: StoreSettingsEntity)

    // --- Delivery Slots ---
    @Query("SELECT * FROM delivery_slots WHERE isActive = 1")
    fun getActiveDeliverySlots(): Flow<List<DeliverySlotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeliverySlots(slots: List<DeliverySlotEntity>)

    // --- Audit Logs ---
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 50")
    fun getAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    // --- Atomic Checkout Transaction ---
    @Transaction
    suspend fun executeAtomicCheckout(
        userId: String,
        idempotencyKey: String,
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        deliveryPinCode: String,
        deliverySlot: String,
        paymentMethod: String,
        couponCode: String?
    ): OrderEntity {
        // 1. Idempotency Check: if order with this key already exists, return it immediately!
        val existingOrder = getOrderByDeliveryIdempotency(idempotencyKey)
        if (existingOrder != null) {
            return existingOrder
        }

        // 2. Fetch current user cart
        val cartItems = getCartItemsSync(userId)
        if (cartItems.isEmpty()) {
            throw IllegalStateException("Your cart is empty. Please add items before checking out.")
        }

        // 3. Fetch latest store settings
        val settings = getStoreSettingsSync() ?: StoreSettingsEntity()
        if (!settings.isStoreOpen) {
            throw IllegalStateException("Store is currently closed for delivery. Please try again during operating hours.")
        }

        // 4. Validate Delivery PIN code against serviced areas
        val servicedPins = settings.servicedPinCodes.split(",").map { it.trim() }
        if (!servicedPins.contains(deliveryPinCode.trim())) {
            throw IllegalArgumentException("We currently do not deliver to PIN $deliveryPinCode in Faridabad. Serviced PINs: ${settings.servicedPinCodes}")
        }

        // 5. Revalidate Stock & Recalculate Subtotal from Live Products in DB
        var calculatedSubtotal = 0.0
        for (item in cartItems) {
            val liveProduct = getProductById(item.productId)
                ?: throw IllegalStateException("Product '${item.name}' is no longer available.")
            if (!liveProduct.isActive) {
                throw IllegalStateException("Product '${liveProduct.name}' is temporarily unavailable.")
            }
            if (liveProduct.stock < item.quantity) {
                throw IllegalStateException("Insufficient stock for '${liveProduct.name}'. Available: ${liveProduct.stock}, requested: ${item.quantity}")
            }
            calculatedSubtotal += liveProduct.sellingPrice * item.quantity
        }

        if (calculatedSubtotal < settings.minOrderValue) {
            throw IllegalStateException("Minimum order value is ₹${settings.minOrderValue.toInt()}. Your subtotal is ₹${calculatedSubtotal.toInt()}.")
        }

        // 6. Delivery Fee Calculation
        val deliveryFee = if (calculatedSubtotal >= settings.freeDeliveryThreshold) 0.0 else settings.deliveryFee

        // 7. Coupon Discount Validation
        var discount = 0.0
        var validatedCouponCode: String? = null
        if (!couponCode.isNullOrBlank()) {
            val coupon = getCouponByCode(couponCode.trim().uppercase())
            if (coupon != null && coupon.isActive && calculatedSubtotal >= coupon.minOrderValue) {
                val percentDiscount = (calculatedSubtotal * coupon.discountPercentage) / 100.0
                val totalDiscount = percentDiscount + coupon.discountAmount
                discount = totalDiscount.coerceAtMost(coupon.maxDiscount)
                validatedCouponCode = coupon.code
            }
        }

        val finalTotal = (calculatedSubtotal + deliveryFee - discount).coerceAtLeast(0.0)
        val generatedOrderId = "FX" + (100000 + (System.currentTimeMillis() % 900000))

        val orderEntity = OrderEntity(
            orderId = generatedOrderId,
            userId = userId,
            customerName = customerName,
            customerPhone = customerPhone,
            deliveryAddress = deliveryAddress,
            deliveryPinCode = deliveryPinCode,
            deliverySlot = deliverySlot,
            subtotal = calculatedSubtotal,
            deliveryFee = deliveryFee,
            discount = discount,
            couponCode = validatedCouponCode,
            total = finalTotal,
            paymentMethod = paymentMethod,
            paymentStatus = if (paymentMethod.contains("UPI")) "PAID" else "PENDING",
            status = "PLACED",
            placedAt = System.currentTimeMillis(),
            idempotencyKey = idempotencyKey
        )

        // 8. Insert Order
        insertOrder(orderEntity)

        // 9. Insert Order Items & Atomically Deduct Stock
        val orderItems = cartItems.map { item ->
            val liveProduct = getProductById(item.productId)!!
            val newStock = (liveProduct.stock - item.quantity).coerceAtLeast(0)
            updateProductStock(item.productId, newStock)

            OrderItemEntity(
                orderId = generatedOrderId,
                productId = item.productId,
                productName = item.name,
                hindiName = item.hindiName,
                unit = item.unit,
                unitPrice = item.price,
                quantity = item.quantity,
                totalPrice = item.price * item.quantity
            )
        }
        insertOrderItems(orderItems)

        // 10. Clear user's cart
        clearCart(userId)

        // 11. Record Audit Log
        insertAuditLog(
            AuditLogEntity(
                performedBy = customerName,
                action = "ORDER_PLACED",
                details = "Order $generatedOrderId created with ${orderItems.size} items, total ₹$finalTotal"
            )
        )

        return orderEntity
    }
}
