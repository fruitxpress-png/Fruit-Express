package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.local.OrderEntity
import com.example.data.local.OrderItemEntity
import com.example.ui.theme.FruitGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InvoiceDialog(
    order: OrderEntity,
    items: List<OrderItemEntity>,
    onDismiss: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val formattedDate = dateFormatter.format(Date(order.placedAt))

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.fruit_xpress_logo),
                        contentDescription = "Fruit Xpress",
                        modifier = Modifier.height(30.dp).width(120.dp),
                        contentScale = ContentScale.Fit
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "TAX INVOICE / RECEIPT",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E281F)
                )
                Text(
                    text = "GSTIN: 06AABCF9281Z1ZQ • Faridabad, Haryana",
                    fontSize = 10.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color(0xFFE2E8F0))
                Spacer(modifier = Modifier.height(8.dp))

                // Invoice metadata
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Order ID: ${order.orderId}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(order.paymentStatus, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = FruitGreen)
                    }
                    Text("Date: $formattedDate", fontSize = 11.sp, color = Color.Gray)
                    Text("Customer: ${order.customerName} (${order.customerPhone})", fontSize = 11.sp, color = Color(0xFF334155))
                    Text("Address: ${order.deliveryAddress}", fontSize = 10.sp, color = Color.Gray, maxLines = 2)
                    Text("Slot: ${order.deliverySlot}", fontSize = 10.sp, color = Color(0xFF64748B))
                }

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = Color(0xFFCBD5E1))
                Spacer(modifier = Modifier.height(8.dp))

                // Items Table Header
                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(vertical = 4.dp, horizontal = 4.dp)) {
                    Text("Item", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Text("Qty", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Text("Price", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Text("Total", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                // Items list
                items.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 4.dp)) {
                        Text(item.productName, modifier = Modifier.weight(2f), fontSize = 11.sp, maxLines = 1)
                        Text("${item.quantity} ${item.unit}", modifier = Modifier.weight(1f), fontSize = 11.sp)
                        Text("₹${item.unitPrice.toInt()}", modifier = Modifier.weight(1f), fontSize = 11.sp)
                        Text("₹${item.totalPrice.toInt()}", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color(0xFFCBD5E1))
                Spacer(modifier = Modifier.height(8.dp))

                // Totals
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", fontSize = 11.sp, color = Color.Gray)
                        Text("₹${order.subtotal.toInt()}", fontSize = 11.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Delivery Charge", fontSize = 11.sp, color = Color.Gray)
                        Text(if (order.deliveryFee == 0.0) "FREE" else "₹${order.deliveryFee.toInt()}", fontSize = 11.sp, color = if (order.deliveryFee == 0.0) FruitGreen else Color.Black)
                    }
                    if (order.discount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Coupon Discount (${order.couponCode ?: ""})", fontSize = 11.sp, color = FruitGreen)
                            Text("-₹${order.discount.toInt()}", fontSize = 11.sp, color = FruitGreen)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Amount", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                        Text("₹${order.total.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = FruitGreen)
                    }
                    Text("Payment: ${order.paymentMethod}", fontSize = 10.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Thank you for shopping with Fruit Xpress!",
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = FruitGreen
                )

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = FruitGreen)
                ) {
                    Text("Close Invoice", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
