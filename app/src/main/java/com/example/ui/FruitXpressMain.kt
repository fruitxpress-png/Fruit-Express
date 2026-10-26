package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.SavedAddressEntity
import com.example.data.model.OrderStatus
import com.example.data.model.UserRole
import com.example.data.repository.FruitXpressRepository
import com.example.ui.admin.AdminErpScreen
import com.example.ui.components.FruitXpressTopBar
import com.example.ui.components.SelectLocationDialog
import com.example.ui.customer.CartScreen
import com.example.ui.customer.CustomerHomeScreen
import com.example.ui.customer.OrdersScreen
import com.example.ui.rider.RiderPortalScreen
import com.example.ui.theme.FruitGreen
import com.example.ui.theme.FruitOrange
import kotlinx.coroutines.launch

@Composable
fun FruitXpressApp() {
    val context = LocalContext.current
    val repository = remember(context) { FruitXpressRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    val currentUser by repository.currentUser.collectAsState()

    // Explicit active role state for instant, synchronous UI tab switching
    var activeRole by remember { mutableStateOf(UserRole.CUSTOMER) }

    // Screen State for Customer navigation
    var customerTab by remember { mutableStateOf("home") } // "home", "cart", "orders"

    // BackHandler: return to Customer view if back is pressed on Admin or Delivery Boy screen
    BackHandler(enabled = activeRole != UserRole.CUSTOMER) {
        activeRole = UserRole.CUSTOMER
        repository.switchUserRole(UserRole.CUSTOMER)
    }

    // Dialog States
    var showLocationDialog by remember { mutableStateOf(false) }
    var confirmedOrderId by remember { mutableStateOf<String?>(null) }
    var wishlistIds by remember { mutableStateOf(setOf<String>()) }

    // Repository Flows
    val products by repository.getActiveProducts().collectAsState(initial = emptyList())
    val allProducts by repository.getAllProducts().collectAsState(initial = emptyList())
    val cartItems by repository.observeCart().collectAsState(initial = emptyList())
    val savedAddresses by repository.getSavedAddresses().collectAsState(initial = emptyList())
    val storeSettings by repository.observeStoreSettings().collectAsState(initial = null)
    val customerOrders by repository.getCustomerOrders().collectAsState(initial = emptyList())
    val allOrders by repository.getAllOrders().collectAsState(initial = emptyList())
    val riders by repository.getRidersList().collectAsState(initial = emptyList())
    val auditLogs by repository.getAuditLogs().collectAsState(initial = emptyList())
    val riderAssignedOrders by remember(activeRole, currentUser.id) {
        repository.getRiderOrders(currentUser.id)
    }.collectAsState(initial = emptyList())

    // Active Selected Address
    val selectedAddress = remember(savedAddresses) {
        savedAddresses.firstOrNull { it.isDefault } ?: savedAddresses.firstOrNull()
    }
    val selectedAddressText = remember(selectedAddress) {
        if (selectedAddress != null) {
            "${selectedAddress.label}: ${selectedAddress.buildingStreet}, ${selectedAddress.sector}"
        } else {
            "Select Delivery Location (Faridabad)"
        }
    }

    val totalCartCount = remember(cartItems) { cartItems.sumOf { it.quantity } }

    Scaffold(
        topBar = {
            FruitXpressTopBar(
                currentRole = activeRole,
                currentUser = currentUser,
                selectedAddressText = selectedAddressText,
                cartItemCount = totalCartCount,
                onLocationClick = { showLocationDialog = true },
                onCartClick = { customerTab = "cart" },
                onRoleChange = { role ->
                    activeRole = role
                    repository.switchUserRole(role)
                    if (role == UserRole.CUSTOMER) {
                        customerTab = "home"
                    }
                }
            )
        },
        bottomBar = {
            if (activeRole == UserRole.CUSTOMER) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp,
                    modifier = Modifier.navigationBarsPadding().testTag("customer_bottom_nav")
                ) {
                    NavigationBarItem(
                        selected = customerTab == "home",
                        onClick = { customerTab = "home" },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Shop", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = FruitGreen,
                            selectedTextColor = FruitGreen,
                            indicatorColor = FruitGreen.copy(alpha = 0.12f)
                        )
                    )

                    NavigationBarItem(
                        selected = customerTab == "cart",
                        onClick = { customerTab = "cart" },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (totalCartCount > 0) {
                                        Badge(containerColor = FruitOrange) {
                                            Text(text = totalCartCount.toString(), color = Color.White)
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                            }
                        },
                        label = { Text("Cart", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = FruitGreen,
                            selectedTextColor = FruitGreen,
                            indicatorColor = FruitGreen.copy(alpha = 0.12f)
                        )
                    )

                    NavigationBarItem(
                        selected = customerTab == "orders",
                        onClick = { customerTab = "orders" },
                        icon = {
                            BadgedBox(
                                badge = {
                                    val activeOrderCount = customerOrders.count { it.status != "DELIVERED" && it.status != "CANCELLED" }
                                    if (activeOrderCount > 0) {
                                        Badge(containerColor = FruitGreen) {
                                            Text(text = activeOrderCount.toString(), color = Color.White)
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Receipt, contentDescription = "Orders")
                            }
                        },
                        label = { Text("Orders", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = FruitGreen,
                            selectedTextColor = FruitGreen,
                            indicatorColor = FruitGreen.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeRole) {
                UserRole.CUSTOMER -> {
                    when (customerTab) {
                        "home" -> {
                            CustomerHomeScreen(
                                products = products,
                                categories = repository.categories,
                                cartItems = cartItems,
                                wishlistProductIds = wishlistIds,
                                onAddToCart = { pid -> coroutineScope.launch { repository.addToCart(pid) } },
                                onIncrementCart = { pid -> coroutineScope.launch { repository.addToCart(pid, 1) } },
                                onDecrementCart = { pid -> coroutineScope.launch { repository.decrementCartItem(pid) } },
                                onToggleWishlist = { pid ->
                                    wishlistIds = if (wishlistIds.contains(pid)) wishlistIds - pid else wishlistIds + pid
                                },
                                onNavigateToCart = { customerTab = "cart" }
                            )
                        }

                        "cart" -> {
                            CartScreen(
                                cartItems = cartItems,
                                currentUser = currentUser,
                                selectedAddress = selectedAddress,
                                servicedPinCodes = storeSettings?.servicedPinCodes ?: "121001, 121002, 121003, 121004, 121005, 121006, 121007, 121008",
                                onIncrement = { pid -> coroutineScope.launch { repository.addToCart(pid, 1) } },
                                onDecrement = { pid -> coroutineScope.launch { repository.decrementCartItem(pid) } },
                                onRemove = { pid -> coroutineScope.launch { repository.removeFromCart(pid) } },
                                onChangeAddress = { showLocationDialog = true },
                                onCalculateSummary = { coupon -> repository.calculateCheckoutSummary(coupon) },
                                onPlaceOrder = { name, phone, addr, pin, slot, payment, coupon, token ->
                                    repository.placeOrder(name, phone, addr, pin, slot, payment, coupon, token)
                                },
                                onOrderPlacedSuccessfully = { orderId ->
                                    confirmedOrderId = orderId
                                },
                                onContinueShopping = { customerTab = "home" }
                            )
                        }

                        "orders" -> {
                            OrdersScreen(
                                orders = customerOrders,
                                onFetchOrderItems = { oid -> repository.getOrderItems(oid) },
                                onCancelOrder = { oid, reason ->
                                    coroutineScope.launch { repository.cancelOrder(oid, reason) }
                                },
                                onContinueShopping = { customerTab = "home" }
                            )
                        }
                    }
                }

                UserRole.ADMIN -> {
                    AdminErpScreen(
                        orders = allOrders,
                        products = allProducts,
                        riders = riders,
                        storeSettings = storeSettings ?: com.example.data.local.StoreSettingsEntity(),
                        auditLogs = auditLogs,
                        onUpdateOrderStatus = { oid, status ->
                            coroutineScope.launch { repository.updateOrderStatus(oid, status) }
                        },
                        onAssignRider = { oid, rid, rname, rphone ->
                            coroutineScope.launch { repository.assignOrderToRider(oid, rid, rname, rphone) }
                        },
                        onSaveProduct = { p ->
                            coroutineScope.launch { repository.saveProduct(p) }
                        },
                        onDeleteProduct = { pid ->
                            coroutineScope.launch { repository.deleteProduct(pid) }
                        },
                        onUpdateStock = { pid, stock ->
                            coroutineScope.launch { repository.updateStock(pid, stock) }
                        },
                        onUpdateSettings = { s ->
                            coroutineScope.launch { repository.updateStoreSettings(s) }
                        }
                    )
                }

                UserRole.RIDER -> {
                    RiderPortalScreen(
                        rider = currentUser,
                        assignedOrders = riderAssignedOrders,
                        allOrders = allOrders,
                        onAcceptAssignment = { oid ->
                            coroutineScope.launch {
                                repository.riderAcceptAssignment(oid, currentUser)
                            }
                        },
                        onFetchOrderItems = { oid -> repository.getOrderItems(oid) },
                        onStartDelivery = { oid ->
                            coroutineScope.launch { repository.updateOrderStatus(oid, OrderStatus.OUT_FOR_DELIVERY) }
                        },
                        onCompleteDelivery = { oid, codCollected ->
                            coroutineScope.launch { repository.markOrderDelivered(oid, codCollected) }
                        }
                    )
                }
            }
        }
    }

    // Select Delivery Location Dialog (BigBasket inspired)
    if (showLocationDialog) {
        SelectLocationDialog(
            savedAddresses = savedAddresses,
            selectedAddressId = selectedAddress?.id,
            servicedPinCodes = storeSettings?.servicedPinCodes ?: "121001, 121002, 121003, 121004, 121005, 121006, 121007, 121008",
            onSelectAddress = { addr ->
                coroutineScope.launch {
                    repository.saveAddress(addr.copy(isDefault = true))
                }
            },
            onSaveNewAddress = { newAddr ->
                coroutineScope.launch {
                    repository.saveAddress(newAddr)
                }
            },
            onDeleteAddress = { aid ->
                coroutineScope.launch {
                    repository.deleteAddress(aid)
                }
            },
            onDismiss = { showLocationDialog = false }
        )
    }

    // Order Placed Success Dialog (Inspired by Section 13 Screen 7)
    confirmedOrderId?.let { orderId ->
        Dialog(onDismissRequest = { confirmedOrderId = null }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(FruitGreen.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = FruitGreen,
                            modifier = Modifier.size(46.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Order Placed Successfully!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF111827)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = Color(0xFFFFF7ED),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFFED7AA))
                    ) {
                        Text(
                            text = "Order ID: $orderId",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = FruitOrange,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Thank you for shopping with Fruit Xpress! Your farm-fresh items are being packed at our Faridabad hub and will reach your doorstep on schedule.",
                        fontSize = 14.sp,
                        color = Color(0xFF374151),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            confirmedOrderId = null
                            customerTab = "orders"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FruitGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("view_orders_success_button")
                    ) {
                        Text("View Orders & Track", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
