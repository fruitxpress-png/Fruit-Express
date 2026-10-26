package com.example.ui.customer

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.local.OrderEntity
import com.example.data.local.OrderItemEntity
import com.example.data.model.OrderStatus
import com.example.ui.components.InvoiceDialog
import com.example.ui.theme.FruitGreen
import com.example.ui.theme.FruitOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrdersScreen(
    orders: List<OrderEntity>,
    onFetchOrderItems: suspend (String) -> List<OrderItemEntity>,
    onCancelOrder: (String, String) -> Unit,
    onContinueShopping: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedOrderForInvoice by remember { mutableStateOf<OrderEntity?>(null) }
    var invoiceItems by remember { mutableStateOf<List<OrderItemEntity>>(emptyList()) }

    val dateFormatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    if (orders.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize().background(Color(0xFFF9FAF9)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Orders Yet",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E281F)
                )
                Text(
                    text = "Your placed grocery orders and tracking will show up here.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onContinueShopping,
                    colors = ButtonDefaults.buttonColors(containerColor = FruitGreen)
                ) {
                    Text("Start Shopping", fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAF8))
                .padding(12.dp)
        ) {
            item {
                Text(
                    text = "MY ORDERS & TRACKING",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            items(orders, key = { it.orderId }) { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("order_card_${order.orderId}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Order #${order.orderId}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E281F)
                                )
                                Text(
                                    text = dateFormatter.format(Date(order.placedAt)),
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }

                            // Status Chip
                            val statusBg = when (order.status) {
                                "DELIVERED" -> Color(0xFFE8F5E9)
                                "OUT_FOR_DELIVERY" -> Color(0xFFFFF3E0)
                                "CANCELLED" -> Color(0xFFFFEBEE)
                                else -> Color(0xFFEDE7F6)
                            }
                            val statusColor = when (order.status) {
                                "DELIVERED" -> FruitGreen
                                "OUT_FOR_DELIVERY" -> FruitOrange
                                "CANCELLED" -> Color.Red
                                else -> Color(0xFF673AB7)
                            }

                            Surface(
                                color = statusBg,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = order.status.replace("_", " "),
                                    color = statusColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Status Timeline
                        OrderStatusTimeline(currentStatus = order.status)

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = Color(0xFFF1F5F2))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Address and Slot
                        Text(
                            text = "📍 ${order.deliveryAddress}",
                            fontSize = 11.sp,
                            color = Color(0xFF334155),
                            maxLines = 2
                        )
                        Text(
                            text = "🕒 Slot: ${order.deliverySlot}",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )

                        // Assigned Rider Info (if assigned)
                        if (!order.assignedRiderName.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = Color(0xFFF0FDF4),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.TwoWheeler,
                                            contentDescription = null,
                                            tint = FruitGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Rider: ${order.assignedRiderName}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = Color(0xFF1B4D24)
                                            )
                                            Text(
                                                text = "Fruit Xpress Delivery Partner",
                                                fontSize = 9.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    if (!order.assignedRiderPhone.isNullOrBlank()) {
                                        IconButton(
                                            onClick = {
                                                val callIntent = Intent(Intent.ACTION_DIAL).apply {
                                                    data = Uri.parse("tel:${order.assignedRiderPhone}")
                                                }
                                                context.startActivity(callIntent)
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Call,
                                                contentDescription = "Call Rider",
                                                tint = FruitGreen,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Amount & Actions Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total: ₹${order.total.toInt()}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E281F)
                                )
                                Text(
                                    text = order.paymentMethod,
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        selectedOrderForInvoice = order
                                    },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Invoice", fontSize = 11.sp)
                                }

                                if (order.status == "PLACED" || order.status == "ACCEPTED") {
                                    OutlinedButton(
                                        onClick = {
                                            onCancelOrder(order.orderId, "Customer requested cancellation")
                                        },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Cancel", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Invoice Dialog display
    selectedOrderForInvoice?.let { ord ->
        LaunchedEffect(ord.orderId) {
            invoiceItems = onFetchOrderItems(ord.orderId)
        }

        InvoiceDialog(
            order = ord,
            items = invoiceItems,
            onDismiss = { selectedOrderForInvoice = null }
        )
    }
}

@Composable
fun OrderStatusTimeline(currentStatus: String) {
    val steps = listOf(
        "Placed" to "PLACED",
        "Accepted" to "ACCEPTED",
        "Packed" to "PACKED",
        "Out for Delivery" to "OUT_FOR_DELIVERY",
        "Delivered" to "DELIVERED"
    )

    val currentIndex = when (currentStatus) {
        "PLACED" -> 0
        "ACCEPTED" -> 1
        "PACKED" -> 2
        "OUT_FOR_DELIVERY" -> 3
        "DELIVERED" -> 4
        "CANCELLED" -> -1
        else -> 0
    }

    if (currentStatus == "CANCELLED") {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Cancel, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Order was cancelled.", fontSize = 11.sp, color = Color.Red, fontWeight = FontWeight.SemiBold)
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            steps.forEachIndexed { index, pair ->
                val isCompleted = index <= currentIndex
                val isCurrent = index == currentIndex

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(if (isCompleted) FruitGreen else Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = pair.first,
                        fontSize = 8.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCompleted) FruitGreen else Color.Gray,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
