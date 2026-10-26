package com.example.ui.customer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.local.ProductEntity
import com.example.data.model.CartItemWithProduct
import com.example.data.model.ProductCategory
import com.example.ui.components.ProductCard
import com.example.ui.theme.FruitGreen
import com.example.ui.theme.FruitOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen(
    products: List<ProductEntity>,
    categories: List<ProductCategory>,
    cartItems: List<CartItemWithProduct>,
    wishlistProductIds: Set<String>,
    onAddToCart: (String) -> Unit,
    onIncrementCart: (String) -> Unit,
    onDecrementCart: (String) -> Unit,
    onToggleWishlist: (String) -> Unit,
    onNavigateToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("all") }
    var inStockOnly by remember { mutableStateOf(false) }
    var selectedDetailProduct by remember { mutableStateOf<ProductEntity?>(null) }

    val cartQtyMap = remember(cartItems) {
        cartItems.associate { it.productId to it.quantity }
    }

    val totalCartCount = remember(cartItems) { cartItems.sumOf { it.quantity } }
    val totalCartValue = remember(cartItems) { cartItems.sumOf { it.price * it.quantity } }

    // Stable filtered list
    val filteredProducts = remember(products, searchQuery, selectedCategoryId, inStockOnly) {
        products.filter { p ->
            val matchesCategory = (selectedCategoryId == "all" || p.category == selectedCategoryId)
            val matchesSearch = searchQuery.isBlank() ||
                    p.name.contains(searchQuery, ignoreCase = true) ||
                    p.hindiName.contains(searchQuery, ignoreCase = true) ||
                    p.category.contains(searchQuery, ignoreCase = true)
            val matchesStock = !inStockOnly || p.stock > 0
            matchesCategory && matchesSearch && matchesStock
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (totalCartCount > 0) 70.dp else 0.dp),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Search Bar
            item(span = { GridItemSpan(2) }) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search fresh fruits, veggies, combos...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = FruitGreen)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("home_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FruitGreen,
                        unfocusedBorderColor = Color(0xFFD6DFD7),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )
            }

            // 2. Official Brand Banner
            item(span = { GridItemSpan(2) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("brand_hero_banner"),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.fruit_xpress_banner),
                        contentDescription = "Fruit Xpress Fresh Grocery & Daily Needs",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(86.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // 3. Category Filter Chips / Pills
            item(span = { GridItemSpan(2) }) {
                Column {
                    Text(
                        text = "CATEGORIES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories, key = { it.id }) { cat ->
                            val isSelected = cat.id == selectedCategoryId
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) FruitGreen else Color.White,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) FruitGreen else Color(0xFFD6DFD7)
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { selectedCategoryId = cat.id }
                                    .testTag("cat_chip_${cat.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Eco,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else FruitGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = cat.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) Color.White else Color(0xFF263238)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // In Stock Filter & Count Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${filteredProducts.size} items available",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF556855)
                        )

                        FilterChip(
                            selected = inStockOnly,
                            onClick = { inStockOnly = !inStockOnly },
                            label = { Text("In Stock Only", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FruitGreen.copy(alpha = 0.15f),
                                selectedLabelColor = FruitGreen
                            )
                        )
                    }
                }
            }

            // 4. Products Grid items (Stable Key = product.id!)
            if (filteredProducts.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No fresh products found",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Text(
                                text = "Try clearing search or picking another category",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            } else {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        cartQuantity = cartQtyMap[product.id] ?: 0,
                        isWishlisted = wishlistProductIds.contains(product.id),
                        onAddToCart = { onAddToCart(product.id) },
                        onIncrement = { onIncrementCart(product.id) },
                        onDecrement = { onDecrementCart(product.id) },
                        onWishlistToggle = { onToggleWishlist(product.id) },
                        onClick = { selectedDetailProduct = product }
                    )
                }
            }
        }

        // Floating Quick Cart Bar (Bottom)
        if (totalCartCount > 0) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onNavigateToCart)
                    .testTag("floating_quick_cart_bar"),
                color = FruitGreen,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "$totalCartCount Items • ₹${totalCartValue.toInt()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Delivery in Faridabad",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "View Cart",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Product Detail Dialog
        selectedDetailProduct?.let { product ->
            Dialog(onDismissRequest = { selectedDetailProduct = null }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Product Details",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            IconButton(onClick = { selectedDetailProduct = null }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = product.name,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color(0xFF1E281F)
                        )
                        if (product.hindiName.isNotBlank()) {
                            Text(
                                text = product.hindiName,
                                fontSize = 14.sp,
                                color = FruitGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Unit: ${product.unit}", fontSize = 12.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "₹${product.sellingPrice.toInt()}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = FruitGreen
                            )
                            if (product.mrp > product.sellingPrice) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "MRP ₹${product.mrp.toInt()}",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Surface(
                                color = if (product.stock > 0) FruitGreen.copy(alpha = 0.15f) else Color(0xFFFFEBEE),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (product.stock > 0) "In Stock (${product.stock} left)" else "Out of stock",
                                    color = if (product.stock > 0) FruitGreen else Color.Red,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = product.description.ifBlank { "Farm-fresh quality guaranteed by Fruit Xpress Faridabad. Hand-sorted and hygienically packed." },
                            fontSize = 13.sp,
                            color = Color(0xFF334155),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = {
                                onAddToCart(product.id)
                                selectedDetailProduct = null
                            },
                            enabled = product.stock > 0 && product.isActive,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = FruitGreen)
                        ) {
                            Text(
                                text = if (product.stock > 0) "Add to Cart • ₹${product.sellingPrice.toInt()}" else "Currently Unavailable",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
