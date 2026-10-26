package com.example.ui.rider

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.OrderEntity
import com.example.data.local.OrderItemEntity
import com.example.data.local.UserEntity
import com.example.ui.theme.FruitGreen
import com.example.ui.theme.FruitOrange

@Composable
fun RiderPortalScreen(
    rider: UserEntity,
    assignedOrders: List<OrderEntity>,
    allOrders: List<OrderEntity> = emptyList(),
    onAcceptAssignment: (String) -> Unit = {},
    onFetchOrderItems: suspend (String) -> List<OrderItemEntity>,
    onStartDelivery: (String) -> Unit,
    onCompleteDelivery: (orderId: String, isCodCollected: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    var deliveryCompletionOrder by remember { mutableStateOf<OrderEntity?>(null) }

    val activeAssignedOrders = assignedOrders.filter { it.status != "DELIVERED" && it.status != "CANCELLED" }
    val availableHubOrders = allOrders.filter {
        (it.status == "PLACED" || it.status == "ACCEPTED" || it.status == "PACKED") &&
                (it.assignedRiderId == null || it.assignedRiderId != rider.id)
    }
    val completedOrders = assignedOrders.filter { it.status == "DELIVERED" }

    Column(modifier = modifier.fillMaxSize().background(Color(0xFFF8FAF8))) {
        // Rider Identity Banner
        Surface(
            color = FruitOrange,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 3.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TwoWheeler,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${rider.name} (Delivery Partner)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Fruit Xpress Rider • Faridabad Hub • Ph: ${rider.phone}",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${activeAssignedOrders.size} Active",
                        color = FruitOrange,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = FruitOrange,
            edgePadding = 12.dp
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("My Tasks (${activeAssignedOrders.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Hub Orders (${availableHubOrders.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Delivered (${completedOrders.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        val displayList = when (selectedTab) {
            0 -> activeAssignedOrders
            1 -> availableHubOrders
            else -> completedOrders
        }

        if (displayList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.TwoWheeler,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = when (selectedTab) {
                            0 -> "No active delivery runs"
                            1 -> "No pending hub orders awaiting pickup"
                            else -> "No completed deliveries yet"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = when (selectedTab) {
                            0 -> "Accept orders from Hub Orders tab to begin delivery"
                            1 -> "New customer orders placed in Faridabad will appear here"
                            else -> "Orders you deliver today will be logged here"
                        },
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                items(displayList, key = { it.orderId }) { order ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("rider_order_card_${order.orderId}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Header: Order ID & Status Chip
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Order #${order.orderId}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E281F)
                                )

                                Surface(
                                    color = when (order.status) {
                                        "OUT_FOR_DELIVERY" -> FruitOrange.copy(alpha = 0.15f)
                                        "DELIVERED" -> FruitGreen.copy(alpha = 0.15f)
                                        else -> Color(0xFFE8F5E9)
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = order.status.replace("_", " "),
                                        color = when (order.status) {
                                            "OUT_FOR_DELIVERY" -> FruitOrange
                                            "DELIVERED" -> FruitGreen
                                            else -> Color(0xFF2E7D32)
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Customer Name & 1-tap Phone Call Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Customer: ${order.customerName}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Phone: ${order.customerPhone}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF475569)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val callIntent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:${order.customerPhone}")
                                        }
                                        context.startActivity(callIntent)
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(FruitGreen.copy(alpha = 0.12f))
                                        .testTag("call_customer_btn_${order.orderId}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Call Customer",
                                        tint = FruitGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = Color(0xFFF1F5F2))
                            Spacer(modifier = Modifier.height(8.dp))

                            // Address & 1-tap Google Maps Navigation Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Delivery Destination:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = order.deliveryAddress,
                                        fontSize = 12.sp,
                                        color = Color(0xFF334155),
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = {
                                        val mapIntent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("geo:0,0?q=${Uri.encode(order.deliveryAddress)}")
                                        ).apply {
                                            setPackage("com.google.android.apps.maps")
                                        }
                                        if (mapIntent.resolveActivity(context.packageManager) != null) {
                                            context.startActivity(mapIntent)
                                        } else {
                                            val webMaps = Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse("https://maps.google.com/?q=${Uri.encode(order.deliveryAddress)}")
                                            )
                                            context.startActivity(webMaps)
                                        }
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(FruitOrange.copy(alpha = 0.15f))
                                        .testTag("navigate_btn_${order.orderId}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = "Open in Maps",
                                        tint = FruitOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Payment & Amount details
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Collect: ₹${order.total.toInt()}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = FruitGreen
                                    )
                                    Text(
                                        text = "Payment: ${order.paymentMethod}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                                Text("Slot: ${order.deliverySlot}", fontSize = 10.sp, color = Color(0xFF64748B))
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Controls to accept assignment or update delivery status
                            if (selectedTab == 1) {
                                // Available Hub order -> Accept Assignment
                                Button(
                                    onClick = { onAcceptAssignment(order.orderId) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .testTag("accept_assignment_btn_${order.orderId}"),
                                    colors = ButtonDefaults.buttonColors(containerColor = FruitGreen),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Accept Assignment", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            } else if (selectedTab == 0) {
                                // My Tasks -> Controls to update status
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (order.status != "OUT_FOR_DELIVERY") {
                                        Button(
                                            onClick = { onStartDelivery(order.orderId) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(38.dp)
                                                .testTag("start_delivery_btn_${order.orderId}"),
                                            colors = ButtonDefaults.buttonColors(containerColor = FruitOrange),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Start Delivery Run", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Button(
                                        onClick = { deliveryCompletionOrder = order },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                            .testTag("mark_delivered_btn_${order.orderId}"),
                                        colors = ButtonDefaults.buttonColors(containerColor = FruitGreen),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Mark Delivered", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog for Marking Delivered & Cash Collection
    deliveryCompletionOrder?.let { order ->
        var isCodConfirmed by remember { mutableStateOf(order.paymentStatus == "PAID" || !order.paymentMethod.contains("COD")) }

        Dialog(onDismissRequest = { deliveryCompletionOrder = null }) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Complete Delivery #${order.orderId}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1E281F)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Customer: ${order.customerName}",
                        fontSize = 12.sp,
                        color = Color(0xFF334155)
                    )
                    Text(
                        text = "Destination: ${order.deliveryAddress}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (order.paymentMethod.contains("COD") || order.paymentStatus != "PAID") {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FruitOrange.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isCodConfirmed,
                                    onCheckedChange = { isCodConfirmed = it },
                                    colors = CheckboxDefaults.colors(checkedColor = FruitOrange)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Received Cash of ₹${order.total.toInt()} from customer",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7C2D12)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Payment verified prepaid via UPI.",
                            fontSize = 12.sp,
                            color = FruitGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { deliveryCompletionOrder = null }
                        ) {
                            Text("Back")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                onCompleteDelivery(order.orderId, isCodConfirmed)
                                deliveryCompletionOrder = null
                            },
                            enabled = isCodConfirmed || order.paymentStatus == "PAID",
                            colors = ButtonDefaults.buttonColors(containerColor = FruitGreen)
                        ) {
                            Text("Confirm Delivered")
                        }
                    }
                }
            }
        }
    }
}
