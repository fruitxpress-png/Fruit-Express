package com.example.ui.customer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.ui.components.ProductImageHelper
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SavedAddressEntity
import com.example.data.model.CartItemWithProduct
import com.example.data.model.CheckoutResult
import com.example.data.model.CheckoutSummary
import com.example.ui.theme.FruitGreen
import com.example.ui.theme.FruitOrange
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun CartScreen(
    cartItems: List<CartItemWithProduct>,
    selectedAddress: SavedAddressEntity?,
    servicedPinCodes: String,
    onIncrement: (String) -> Unit,
    onDecrement: (String) -> Unit,
    onRemove: (String) -> Unit,
    onChangeAddress: () -> Unit,
    onCalculateSummary: suspend (String?) -> CheckoutSummary,
    onPlaceOrder: suspend (
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        deliveryPinCode: String,
        deliverySlot: String,
        paymentMethod: String,
        couponCode: String?,
        token: String
    ) -> CheckoutResult,
    onOrderPlacedSuccessfully: (String) -> Unit,
    onContinueShopping: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var couponInput by remember { mutableStateOf("") }
    var appliedCoupon by remember { mutableStateOf<String?>("WELCOME50") }
    var selectedSlot by remember { mutableStateOf("Today Evening (5:00 PM - 8:00 PM)") }
    var paymentMethod by remember { mutableStateOf("Cash on Delivery (COD)") }

    var summary by remember {
        mutableStateOf(
            CheckoutSummary(
                subtotal = 0.0,
                deliveryFee = 30.0,
                discount = 0.0,
                couponCode = null,
                total = 0.0,
                isFreeDelivery = false,
                minOrderMet = true,
                minOrderDeficit = 0.0
            )
        )
    }

    // Submission & Duplicate Order Protection State
    var isSubmittingOrder by remember { mutableStateOf(false) }
    var checkoutError by remember { mutableStateOf<String?>(null) }
    val submissionToken = remember { UUID.randomUUID().toString() }

    // Recompute summary whenever cartItems or appliedCoupon changes
    LaunchedEffect(cartItems, appliedCoupon) {
        summary = onCalculateSummary(appliedCoupon)
    }

    if (cartItems.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize().background(Color(0xFFF9FAF9)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(FruitGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = FruitGreen,
                        modifier = Modifier.size(42.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Your Cart is Empty",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E281F)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Add farm-fresh fruits, vegetables, and daily staples to proceed.",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onContinueShopping,
                    colors = ButtonDefaults.buttonColors(containerColor = FruitGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Explore Fresh Produce", fontWeight = FontWeight.Bold)
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
            // Cart Items List
            item {
                Text(
                    text = "MY CART (${cartItems.size} ITEMS)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            items(cartItems, key = { it.productId }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val imgRes = ProductImageHelper.getProductDrawable(item.productId, item.name)
                        Image(
                            painter = painterResource(id = imgRes),
                            contentDescription = item.name,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF7FAF7))
                                .padding(3.dp),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF1E281F)
                            )
                            if (item.hindiName.isNotBlank()) {
                                Text(
                                    text = item.hindiName,
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            Text(
                                text = "₹${item.price.toInt()} / ${item.unit}",
                                fontSize = 11.sp,
                                color = FruitGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Stepper (- [qty] +)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(horizontal = 2.dp)
                        ) {
                            IconButton(
                                onClick = { onDecrement(item.productId) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (item.quantity == 1) Icons.Default.Delete else Icons.Default.Remove,
                                    contentDescription = "Decrease",
                                    tint = FruitGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = item.quantity.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = FruitGreen,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                            IconButton(
                                onClick = { onIncrement(item.productId) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase",
                                    tint = FruitGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "₹${(item.price * item.quantity).toInt()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF1E281F)
                        )
                    }
                }
            }

            // Coupon Code Card
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalOffer,
                                contentDescription = null,
                                tint = FruitOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Apply Promo Coupon",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = couponInput,
                                onValueChange = { couponInput = it.uppercase() },
                                placeholder = { Text("Enter code (e.g. FRESH100)", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            Button(
                                onClick = {
                                    if (couponInput.isNotBlank()) {
                                        appliedCoupon = couponInput.trim()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = FruitGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Apply")
                            }
                        }

                        if (summary.discount > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "🎉 Coupon '${summary.couponCode}' applied! You save ₹${summary.discount.toInt()}",
                                fontSize = 11.sp,
                                color = FruitGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Delivery Address Card
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = FruitGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Delivery Address", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Text(
                                text = "Change",
                                color = FruitGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.clickable(onClick = onChangeAddress)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        if (selectedAddress != null) {
                            Text(
                                text = "${selectedAddress.label}: ${selectedAddress.houseFlatNo}, ${selectedAddress.buildingStreet}",
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = Color(0xFF1E281F)
                            )
                            Text(
                                text = "${selectedAddress.locality}, ${selectedAddress.sector}, ${selectedAddress.city} - ${selectedAddress.pinCode}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        } else {
                            Text(
                                text = "No address selected. Tap Change to pick address.",
                                fontSize = 12.sp,
                                color = Color.Red
                            )
                        }
                    }
                }
            }

            // Delivery Slot Selector
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = FruitGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Delivery Slot", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        listOf(
                            "Today Evening (5:00 PM - 8:00 PM)",
                            "Tomorrow Morning (6:00 AM - 9:00 AM)",
                            "Tomorrow Midday (11:00 AM - 2:00 PM)"
                        ).forEach { slot ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedSlot = slot }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedSlot == slot,
                                    onClick = { selectedSlot = slot },
                                    colors = RadioButtonDefaults.colors(selectedColor = FruitGreen)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = slot, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // Payment Method
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = null,
                                tint = FruitGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Payment Method", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { paymentMethod = "Cash on Delivery (COD)" }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = paymentMethod == "Cash on Delivery (COD)",
                                onClick = { paymentMethod = "Cash on Delivery (COD)" },
                                colors = RadioButtonDefaults.colors(selectedColor = FruitGreen)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text("Cash on Delivery (COD)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Pay cash or UPI to delivery rider at your doorstep", fontSize = 10.sp, color = Color.Gray)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { paymentMethod = "UPI Payment" }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = paymentMethod == "UPI Payment",
                                onClick = { paymentMethod = "UPI Payment" },
                                colors = RadioButtonDefaults.colors(selectedColor = FruitGreen)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text("UPI Payment (GPay, PhonePe, Paytm)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Direct UPI transfer (Merchant credentials live check)", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            // Bill Summary Card
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("BILL SUMMARY", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Item Subtotal", fontSize = 12.sp, color = Color(0xFF334155))
                            Text("₹${summary.subtotal.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Delivery Fee (Faridabad)", fontSize = 12.sp, color = Color(0xFF334155))
                            Text(
                                text = if (summary.isFreeDelivery) "FREE" else "₹${summary.deliveryFee.toInt()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (summary.isFreeDelivery) FruitGreen else Color.Black
                            )
                        }

                        if (summary.discount > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Coupon Discount", fontSize = 12.sp, color = FruitGreen)
                                Text("-₹${summary.discount.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FruitGreen)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = Color(0xFFE2E8F0))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Pay", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                            Text("₹${summary.total.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = FruitGreen)
                        }
                    }
                }
            }

            // Checkout Error Banner (if any)
            if (checkoutError != null) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = checkoutError!!,
                            color = Color(0xFFC62828),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            // Checkout Button
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (selectedAddress == null) {
                            checkoutError = "Please select or add a delivery address."
                            return@Button
                        }

                        val servicedList = servicedPinCodes.split(",").map { it.trim() }
                        if (!servicedList.contains(selectedAddress.pinCode.trim())) {
                            checkoutError = "Delivery outside Faridabad serviced zone (PIN: ${selectedAddress.pinCode})."
                            return@Button
                        }

                        if (!summary.minOrderMet) {
                            checkoutError = "Minimum order value is ₹99. Add items worth ₹${summary.minOrderDeficit.toInt()} more."
                            return@Button
                        }

                        // Atomic submission with immediate disable & spinner
                        isSubmittingOrder = true
                        checkoutError = null

                        coroutineScope.launch {
                            val fullAddressText = "${selectedAddress.houseFlatNo}, ${selectedAddress.buildingStreet}, ${selectedAddress.locality}, ${selectedAddress.sector}, ${selectedAddress.city} - ${selectedAddress.pinCode}"
                            val result = onPlaceOrder(
                                "Sachin Kumar",
                                "9876543210",
                                fullAddressText,
                                selectedAddress.pinCode,
                                selectedSlot,
                                paymentMethod,
                                appliedCoupon,
                                submissionToken
                            )

                            isSubmittingOrder = false
                            when (result) {
                                is CheckoutResult.Success -> {
                                    onOrderPlacedSuccessfully(result.orderId)
                                }
                                is CheckoutResult.Error -> {
                                    checkoutError = result.message
                                }
                            }
                        }
                    },
                    enabled = !isSubmittingOrder && selectedAddress != null && summary.minOrderMet,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_place_order_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = FruitGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isSubmittingOrder) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Securing Order...", fontWeight = FontWeight.Bold)
                    } else {
                        Text(
                            text = "Confirm & Place Order • ₹${summary.total.toInt()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
