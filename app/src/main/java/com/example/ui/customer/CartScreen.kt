package com.example.ui.customer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SavedAddressEntity
import com.example.data.local.UserEntity
import com.example.data.model.CartItemWithProduct
import com.example.data.model.CheckoutResult
import com.example.data.model.CheckoutSummary
import com.example.ui.components.ProductImageHelper
import com.example.ui.theme.FruitGreen
import com.example.ui.theme.FruitOrange
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun CartScreen(
    cartItems: List<CartItemWithProduct>,
    currentUser: UserEntity,
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
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAF9)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(FruitGreen.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "Empty Cart",
                        tint = FruitGreen,
                        modifier = Modifier.size(44.dp)
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Your Cart is Empty",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Add farm-fresh fruits, vegetables, and daily staples to proceed.",
                    fontSize = 15.sp,
                    color = Color(0xFF374151),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onContinueShopping,
                    colors = ButtonDefaults.buttonColors(containerColor = FruitGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Explore Fresh Produce", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                }
            }
        }
    } else {
        // Root container with fixed bottom action bar for Place Order
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAF8))
        ) {
            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 160.dp)
            ) {
                // Header section
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp, top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Your Fresh Produce Cart",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                            Text(
                                text = "Faridabad Express Delivery (30-45 mins)",
                                fontSize = 14.sp,
                                color = Color(0xFF166534),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = FruitGreen.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "${cartItems.sumOf { it.quantity }} items",
                                color = FruitGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Cart item cards
                items(cartItems, key = { it.productId }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val imgRes = ProductImageHelper.getProductDrawable(item.productId, item.name)
                            Image(
                                painter = painterResource(id = imgRes),
                                contentDescription = item.name,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF3F4F6))
                                    .padding(4.dp),
                                contentScale = ContentScale.Fit
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF111827)
                                )
                                if (item.hindiName.isNotBlank()) {
                                    Text(
                                        text = item.hindiName,
                                        fontSize = 13.sp,
                                        color = Color(0xFF4B5563)
                                    )
                                }
                                Text(
                                    text = "₹${item.price.toInt()} / ${item.unit}",
                                    fontSize = 14.sp,
                                    color = Color(0xFF15803D),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Stepper (- [qty] +)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .border(1.dp, Color(0xFFC8E6C9), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 2.dp)
                            ) {
                                IconButton(
                                    onClick = { onDecrement(item.productId) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = if (item.quantity == 1) Icons.Default.Delete else Icons.Default.Remove,
                                        contentDescription = "Decrease quantity",
                                        tint = FruitGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = item.quantity.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = FruitGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                                IconButton(
                                    onClick = { onIncrement(item.productId) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Increase quantity",
                                        tint = FruitGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "₹${(item.price * item.quantity).toInt()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF111827)
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
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocalOffer,
                                    contentDescription = null,
                                    tint = FruitOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Apply Promo Coupon",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF111827)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = couponInput,
                                    onValueChange = { couponInput = it.uppercase() },
                                    placeholder = {
                                        Text(
                                            "Enter promo code (e.g. WELCOME50)",
                                            fontSize = 13.sp,
                                            color = Color(0xFF6B7280)
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color(0xFF111827),
                                        unfocusedTextColor = Color(0xFF111827),
                                        focusedBorderColor = FruitGreen,
                                        unfocusedBorderColor = Color(0xFFD1D5DB)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                Button(
                                    onClick = {
                                        if (couponInput.isNotBlank()) {
                                            appliedCoupon = couponInput.trim()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = FruitGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(50.dp)
                                ) {
                                    Text("Apply", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                }
                            }

                            if (summary.discount > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = Color(0xFFDCFCE7),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF15803D),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Coupon '${summary.couponCode}' applied! You save ₹${summary.discount.toInt()}",
                                            fontSize = 13.sp,
                                            color = Color(0xFF15803D),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
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
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
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
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Delivery Address",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF111827)
                                    )
                                }
                                Text(
                                    text = "Change",
                                    color = FruitGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .clickable(onClick = onChangeAddress)
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            if (selectedAddress != null) {
                                Text(
                                    text = "${selectedAddress.label}: ${selectedAddress.houseFlatNo}, ${selectedAddress.buildingStreet}",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1F2937)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${selectedAddress.locality}, ${selectedAddress.sector}, ${selectedAddress.city} - ${selectedAddress.pinCode}",
                                    fontSize = 14.sp,
                                    color = Color(0xFF374151)
                                )
                            } else {
                                Text(
                                    text = "No address selected. Tap Change to pick address.",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFDC2626)
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
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = FruitGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Delivery Slot",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF111827)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            listOf(
                                Pair("Today Evening (5:00 PM - 8:00 PM)", "Express doorstep delivery today"),
                                Pair("Tomorrow Morning (6:00 AM - 9:00 AM)", "Farm fresh morning harvest slot"),
                                Pair("Tomorrow Midday (11:00 AM - 2:00 PM)", "Standard daytime scheduled slot")
                            ).forEach { (slotTitle, slotDesc) ->
                                val isSelected = selectedSlot == slotTitle
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { selectedSlot = slotTitle }
                                        .background(if (isSelected) Color(0xFFF0FDF4) else Color.Transparent)
                                        .padding(horizontal = 6.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedSlot = slotTitle },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = FruitGreen,
                                            unselectedColor = Color(0xFF4B5563)
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = slotTitle,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF111827)
                                        )
                                        Text(
                                            text = slotDesc,
                                            fontSize = 13.sp,
                                            color = Color(0xFF4B5563)
                                        )
                                    }
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
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Payment,
                                    contentDescription = null,
                                    tint = FruitGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Payment Method",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF111827)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // COD
                            val isCod = paymentMethod == "Cash on Delivery (COD)"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { paymentMethod = "Cash on Delivery (COD)" }
                                    .background(if (isCod) Color(0xFFF0FDF4) else Color.Transparent)
                                    .padding(horizontal = 6.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isCod,
                                    onClick = { paymentMethod = "Cash on Delivery (COD)" },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = FruitGreen,
                                        unselectedColor = Color(0xFF4B5563)
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        "Cash on Delivery (COD)",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF111827)
                                    )
                                    Text(
                                        "Pay cash or scan UPI QR with rider at your doorstep",
                                        fontSize = 13.sp,
                                        color = Color(0xFF4B5563)
                                    )
                                }
                            }

                            // UPI / Online
                            val isUpi = paymentMethod == "UPI Payment"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { paymentMethod = "UPI Payment" }
                                    .background(if (isUpi) Color(0xFFF0FDF4) else Color.Transparent)
                                    .padding(horizontal = 6.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isUpi,
                                    onClick = { paymentMethod = "UPI Payment" },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = FruitGreen,
                                        unselectedColor = Color(0xFF4B5563)
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        "UPI / Online Payment (GPay, PhonePe, Cards)",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF111827)
                                    )
                                    Text(
                                        "Pay via UPI App / QR code or Card upon delivery",
                                        fontSize = 13.sp,
                                        color = Color(0xFF4B5563)
                                    )
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
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                "BILL SUMMARY",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF111827)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Item Subtotal", fontSize = 14.sp, color = Color(0xFF374151))
                                Text(
                                    "₹${summary.subtotal.toInt()}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF111827)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Delivery Fee (Faridabad)", fontSize = 14.sp, color = Color(0xFF374151))
                                Text(
                                    text = if (summary.isFreeDelivery) "FREE" else "₹${summary.deliveryFee.toInt()}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (summary.isFreeDelivery) Color(0xFF15803D) else Color(0xFF111827)
                                )
                            }

                            if (summary.discount > 0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Coupon Discount (${summary.couponCode ?: ""})",
                                        fontSize = 14.sp,
                                        color = Color(0xFF15803D),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "-₹${summary.discount.toInt()}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = Color(0xFFE5E7EB), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total Payable", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF111827))
                                Text(
                                    "₹${summary.total.toInt()}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 19.sp,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }
                    }
                }
            }

            // Fixed Bottom Action Area (Always visible above system/app navigation)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = Color.White,
                shadowElevation = 14.dp,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // Checkout Error Banner (if any)
                    if (checkoutError != null) {
                        Surface(
                            color = Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFC62828),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = checkoutError!!,
                                    color = Color(0xFFC62828),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Order Summary Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL PAYABLE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4B5563)
                            )
                            Text(
                                text = "₹${summary.total.toInt()}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF15803D)
                            )
                        }
                        if (summary.discount > 0) {
                            Surface(
                                color = Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Saving ₹${summary.discount.toInt()}",
                                    color = Color(0xFF15803D),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Confirm & Place Order Button
                    Button(
                        onClick = {
                            if (selectedAddress == null) {
                                checkoutError = "Please select or add a delivery address to place your order."
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
                                val customerName = currentUser.name.ifBlank { "Customer" }
                                val customerPhone = currentUser.phone.ifBlank { "9876543210" }

                                val result = onPlaceOrder(
                                    customerName,
                                    customerPhone,
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
                            .height(50.dp)
                            .testTag("confirm_place_order_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FruitGreen,
                            disabledContainerColor = Color(0xFFA5D6A7),
                            contentColor = Color.White,
                            disabledContentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isSubmittingOrder) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Securing Order...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "Confirm & Place Order • ₹${summary.total.toInt()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
