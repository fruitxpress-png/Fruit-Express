package com.example.ui.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.ui.components.ProductImageHelper
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.AuditLogEntity
import com.example.data.local.CouponEntity
import com.example.data.local.OrderEntity
import com.example.data.local.ProductEntity
import com.example.data.local.StoreSettingsEntity
import com.example.data.local.UserEntity
import com.example.data.model.OrderStatus
import com.example.ui.theme.FruitGreen
import com.example.ui.theme.FruitOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun AdminErpScreen(
    orders: List<OrderEntity>,
    products: List<ProductEntity>,
    riders: List<UserEntity>,
    storeSettings: StoreSettingsEntity,
    auditLogs: List<AuditLogEntity>,
    onUpdateOrderStatus: (String, OrderStatus) -> Unit,
    onAssignRider: (orderId: String, riderId: String, riderName: String, riderPhone: String) -> Unit,
    onSaveProduct: (ProductEntity) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onUpdateStock: (String, Int) -> Unit,
    onUpdateSettings: (StoreSettingsEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Dashboard", "Orders (${orders.size})", "Inventory (${products.size})", "Store Settings")

    // Modals
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var isAddingProduct by remember { mutableStateOf(false) }
    var assigningOrder by remember { mutableStateOf<OrderEntity?>(null) }

    Column(modifier = modifier.fillMaxSize().background(Color(0xFFF8FAF8))) {
        // Tab Header
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = FruitGreen,
            edgePadding = 12.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> AdminDashboardTab(orders, products)
            1 -> AdminOrdersTab(orders, riders, onUpdateOrderStatus, onAssignRiderClick = { assigningOrder = it })
            2 -> AdminInventoryTab(
                products,
                onEdit = { editingProduct = it },
                onAdd = { isAddingProduct = true },
                onDelete = onDeleteProduct,
                onUpdateStock = onUpdateStock
            )
            3 -> AdminSettingsTab(storeSettings, auditLogs, onUpdateSettings)
        }
    }

    // Assign Rider Dialog
    assigningOrder?.let { order ->
        Dialog(onDismissRequest = { assigningOrder = null }) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Assign Order #${order.orderId}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "To Delivery Partner in Faridabad",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (riders.isEmpty()) {
                        Text("No registered riders available.", color = Color.Red, fontSize = 12.sp)
                    } else {
                        riders.forEach { rider ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        onAssignRider(order.orderId, rider.id, rider.name, rider.phone)
                                        assigningOrder = null
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8F3))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AssignmentInd,
                                        contentDescription = null,
                                        tint = FruitGreen
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(rider.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(rider.phone, fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { assigningOrder = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }

    // Edit / Add Product Dialog
    if (editingProduct != null || isAddingProduct) {
        val prod = editingProduct
        ProductEditorDialog(
            initialProduct = prod,
            onDismiss = {
                editingProduct = null
                isAddingProduct = false
            },
            onSave = { saved ->
                onSaveProduct(saved)
                editingProduct = null
                isAddingProduct = false
            }
        )
    }
}

@Composable
fun AdminDashboardTab(orders: List<OrderEntity>, products: List<ProductEntity>) {
    val totalRevenue = orders.filter { it.status != "CANCELLED" }.sumOf { it.total }
    val pendingOrders = orders.count { it.status == "PLACED" }
    val outForDelivery = orders.count { it.status == "OUT_FOR_DELIVERY" }
    val deliveredCount = orders.count { it.status == "DELIVERED" }
    val lowStockCount = products.count { it.stock < 15 }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        item {
            Text("STORE OVERVIEW — FARIDABAD", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            // Revenue and Total Orders Cards
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KpiCard(
                    title = "Total Sales",
                    value = "₹${totalRevenue.toInt()}",
                    subtitle = "${orders.size} Total Orders",
                    color = FruitGreen,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Pending Action",
                    value = pendingOrders.toString(),
                    subtitle = "Needs Acceptance",
                    color = FruitOrange,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KpiCard(
                    title = "Out for Delivery",
                    value = outForDelivery.toString(),
                    subtitle = "Active on road",
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Delivered",
                    value = deliveredCount.toString(),
                    subtitle = "Completed",
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Low Stock Alert Card
            if (lowStockCount > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = FruitOrange)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Low Stock Alert: $lowStockCount Items",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF7A3E00)
                            )
                            Text(
                                text = "Items with less than 15 units remaining in Faridabad store.",
                                fontSize = 11.sp,
                                color = Color(0xFF9E5300)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text("RECENT ORDERS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(6.dp))
        }

        items(orders.take(5), key = { it.orderId }) { order ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Order #${order.orderId}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("${order.customerName} • ₹${order.total.toInt()}", fontSize = 11.sp, color = Color.Gray)
                    }
                    Surface(
                        color = FruitGreen.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = order.status,
                            color = FruitGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 10.sp, color = Color(0xFF64748B))
        }
    }
}

@Composable
fun AdminOrdersTab(
    orders: List<OrderEntity>,
    riders: List<UserEntity>,
    onUpdateOrderStatus: (String, OrderStatus) -> Unit,
    onAssignRiderClick: (OrderEntity) -> Unit
) {
    var statusFilter by remember { mutableStateOf("ALL") }

    val filteredOrders = remember(orders, statusFilter) {
        if (statusFilter == "ALL") orders else orders.filter { it.status == statusFilter }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        item {
            // Filter Pills
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("ALL", "PLACED", "ACCEPTED", "PACKED", "OUT_FOR_DELIVERY", "DELIVERED").forEach { st ->
                    val isSel = statusFilter == st
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSel) FruitGreen else Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) FruitGreen else Color(0xFFD6DFD7)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { statusFilter = st }
                    ) {
                        Text(
                            text = if (st == "ALL") "All (${orders.size})" else st.take(6),
                            color = if (isSel) Color.White else Color(0xFF334155),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        items(filteredOrders, key = { it.orderId }) { order ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Order #${order.orderId}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${order.customerName} • ${order.customerPhone}", fontSize = 11.sp, color = Color(0xFF475569))
                        }
                        Text("₹${order.total.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = FruitGreen)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("📍 ${order.deliveryAddress}", fontSize = 11.sp, color = Color.Gray, maxLines = 1)
                    Text("Slot: ${order.deliverySlot} • Status: ${order.status}", fontSize = 10.sp, color = Color(0xFF64748B))

                    if (!order.assignedRiderName.isNullOrBlank()) {
                        Text("🛵 Assigned Rider: ${order.assignedRiderName}", fontSize = 11.sp, color = FruitGreen, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = Color(0xFFF1F5F2))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Action Buttons depending on status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when (order.status) {
                            "PLACED" -> {
                                Button(
                                    onClick = { onUpdateOrderStatus(order.orderId, OrderStatus.ACCEPTED) },
                                    colors = ButtonDefaults.buttonColors(containerColor = FruitGreen),
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                ) {
                                    Text("Accept Order", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            "ACCEPTED" -> {
                                OutlinedButton(
                                    onClick = { onAssignRiderClick(order) },
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                ) {
                                    Text("Assign Rider", fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = { onUpdateOrderStatus(order.orderId, OrderStatus.PACKED) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                ) {
                                    Text("Mark Packed", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            "PACKED" -> {
                                Button(
                                    onClick = { onUpdateOrderStatus(order.orderId, OrderStatus.OUT_FOR_DELIVERY) },
                                    colors = ButtonDefaults.buttonColors(containerColor = FruitOrange),
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                ) {
                                    Text("Dispatch (Out for Delivery)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            "OUT_FOR_DELIVERY" -> {
                                Button(
                                    onClick = { onUpdateOrderStatus(order.orderId, OrderStatus.DELIVERED) },
                                    colors = ButtonDefaults.buttonColors(containerColor = FruitGreen),
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                ) {
                                    Text("Complete Delivery", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            else -> {
                                Text("No actions", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminInventoryTab(
    products: List<ProductEntity>,
    onEdit: (ProductEntity) -> Unit,
    onAdd: () -> Unit,
    onDelete: (String) -> Unit,
    onUpdateStock: (String, Int) -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = remember(products, search) {
        if (search.isBlank()) products else products.filter {
            it.name.contains(search, ignoreCase = true) || it.hindiName.contains(search, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Search catalog...", fontSize = 12.sp) },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onAdd,
                colors = ButtonDefaults.buttonColors(containerColor = FruitGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Item", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filtered, key = { it.id }) { product ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Real product produce image thumbnail
                        val imgRes = ProductImageHelper.getProductDrawable(product.id, product.name, product.category)
                        Image(
                            painter = painterResource(id = imgRes),
                            contentDescription = product.name,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF7FAF7))
                                .padding(3.dp),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${product.hindiName} • ${product.unit}", fontSize = 11.sp, color = Color.Gray)
                            Text("Sell: ₹${product.sellingPrice.toInt()} (Cost: ₹${product.purchaseCost.toInt()})", fontSize = 11.sp, color = FruitGreen)
                        }

                        // Stock editor
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onUpdateStock(product.id, (product.stock - 5).coerceAtLeast(0)) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Text("-5", fontSize = 10.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = "${product.stock}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (product.stock < 15) Color.Red else Color(0xFF1E281F),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            IconButton(
                                onClick = { onUpdateStock(product.id, product.stock + 10) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Text("+10", fontSize = 10.sp, color = FruitGreen, fontWeight = FontWeight.Bold)
                            }

                            IconButton(onClick = { onEdit(product) }, modifier = Modifier.size(32.dp)) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSettingsTab(
    settings: StoreSettingsEntity,
    auditLogs: List<AuditLogEntity>,
    onUpdateSettings: (StoreSettingsEntity) -> Unit
) {
    var isStoreOpen by remember { mutableStateOf(settings.isStoreOpen) }
    var servicedPins by remember { mutableStateOf(settings.servicedPinCodes) }
    var deliveryFee by remember { mutableStateOf(settings.deliveryFee.toInt().toString()) }
    var freeThreshold by remember { mutableStateOf(settings.freeDeliveryThreshold.toInt().toString()) }
    var minOrder by remember { mutableStateOf(settings.minOrderValue.toInt().toString()) }
    var contactPhone by remember { mutableStateOf(settings.contactPhone) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("STORE CONFIGURATION (FARIDABAD)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Store Open for Orders", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Toggle to temporarily halt order placement", fontSize = 10.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = isStoreOpen,
                            onCheckedChange = { isStoreOpen = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = FruitGreen)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = servicedPins,
                        onValueChange = { servicedPins = it },
                        label = { Text("Serviced PIN Codes (Faridabad)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = deliveryFee,
                            onValueChange = { deliveryFee = it },
                            label = { Text("Delivery Fee (₹)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = freeThreshold,
                            onValueChange = { freeThreshold = it },
                            label = { Text("Free Above (₹)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = minOrder,
                            onValueChange = { minOrder = it },
                            label = { Text("Min Order (₹)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        label = { Text("Store Helpline Phone") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            onUpdateSettings(
                                settings.copy(
                                    isStoreOpen = isStoreOpen,
                                    servicedPinCodes = servicedPins.trim(),
                                    deliveryFee = deliveryFee.toDoubleOrNull() ?: 30.0,
                                    freeDeliveryThreshold = freeThreshold.toDoubleOrNull() ?: 299.0,
                                    minOrderValue = minOrder.toDoubleOrNull() ?: 99.0,
                                    contactPhone = contactPhone.trim()
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FruitGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Store Settings", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("AUDIT LOGS (${auditLogs.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(6.dp))
        }

        items(auditLogs.take(20), key = { it.id }) { log ->
            val df = SimpleDateFormat("dd MMM hh:mm a", Locale.getDefault())
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(6.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(log.action, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = FruitGreen)
                        Text(df.format(Date(log.timestamp)), fontSize = 10.sp, color = Color.Gray)
                    }
                    Text(log.details, fontSize = 11.sp, color = Color(0xFF334155))
                    Text("By: ${log.performedBy}", fontSize = 9.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun ProductEditorDialog(
    initialProduct: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var hindiName by remember { mutableStateOf(initialProduct?.hindiName ?: "") }
    var category by remember { mutableStateOf(initialProduct?.category ?: "fruits") }
    var unit by remember { mutableStateOf(initialProduct?.unit ?: "1 kg") }
    var purchaseCost by remember { mutableStateOf(initialProduct?.purchaseCost?.toInt()?.toString() ?: "50") }
    var sellingPrice by remember { mutableStateOf(initialProduct?.sellingPrice?.toInt()?.toString() ?: "75") }
    var mrp by remember { mutableStateOf(initialProduct?.mrp?.toInt()?.toString() ?: "90") }
    var stock by remember { mutableStateOf(initialProduct?.stock?.toString() ?: "50") }
    var description by remember { mutableStateOf(initialProduct?.description ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (initialProduct == null) "Add New Product" else "Edit Product",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name (English)*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = hindiName,
                    onValueChange = { hindiName = it },
                    label = { Text("Hindi Name (e.g. देसी टमाटर)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sellingPrice,
                        onValueChange = { sellingPrice = it },
                        label = { Text("Sell Price*") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = mrp,
                        onValueChange = { mrp = it },
                        label = { Text("MRP") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it },
                        label = { Text("Stock*") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val id = initialProduct?.id ?: ("p_" + UUID.randomUUID().toString().take(8))
                                onSave(
                                    ProductEntity(
                                        id = id,
                                        name = name.trim(),
                                        hindiName = hindiName.trim(),
                                        category = category.trim(),
                                        unit = unit.trim(),
                                        purchaseCost = purchaseCost.toDoubleOrNull() ?: 0.0,
                                        sellingPrice = sellingPrice.toDoubleOrNull() ?: 50.0,
                                        mrp = mrp.toDoubleOrNull() ?: 60.0,
                                        stock = stock.toIntOrNull() ?: 0,
                                        isActive = true,
                                        description = description.trim()
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FruitGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
