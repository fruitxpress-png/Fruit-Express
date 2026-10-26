package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.UserEntity
import com.example.data.model.UserRole
import com.example.ui.theme.FruitGreen
import com.example.ui.theme.FruitOrange

@Composable
fun FruitXpressTopBar(
    currentRole: UserRole,
    currentUser: UserEntity,
    selectedAddressText: String,
    cartItemCount: Int,
    onLocationClick: () -> Unit,
    onCartClick: () -> Unit,
    onRoleChange: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            // Row 1: Brand Logo + Hub Status Badge + Cart Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Official Fruit Xpress Brand Logo
                Image(
                    painter = painterResource(id = R.drawable.fruit_xpress_logo),
                    contentDescription = "Fruit Xpress Logo",
                    modifier = Modifier
                        .height(34.dp)
                        .width(138.dp)
                        .testTag("app_logo"),
                    contentScale = ContentScale.Fit
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Hub / Role indicator chip
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF3F7F4)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(FruitGreen)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Faridabad Hub",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E4032)
                            )
                        }
                    }

                    if (currentRole == UserRole.CUSTOMER) {
                        Spacer(modifier = Modifier.width(6.dp))
                        BadgedBox(
                            badge = {
                                if (cartItemCount > 0) {
                                    Badge(containerColor = FruitOrange) {
                                        Text(
                                            text = cartItemCount.toString(),
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            IconButton(
                                onClick = onCartClick,
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("cart_icon_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Cart",
                                    tint = FruitGreen
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Row 2: Permanent 3-Tab Segmented Navigation Bar (Directly Clickable)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("role_navigation_tabs"),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFEEF3EE)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 1. Customer App Tab
                    val isCustomerSelected = currentRole == UserRole.CUSTOMER
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isCustomerSelected) FruitGreen else Color.Transparent,
                        shadowElevation = if (isCustomerSelected) 2.dp else 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onRoleChange(UserRole.CUSTOMER) }
                            .testTag("tab_customer_app")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = if (isCustomerSelected) Color.White else Color(0xFF475569)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Customer App",
                                fontSize = 11.sp,
                                fontWeight = if (isCustomerSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (isCustomerSelected) Color.White else Color(0xFF475569),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // 2. Admin ERP Panel Tab
                    val isAdminSelected = currentRole == UserRole.ADMIN
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isAdminSelected) Color(0xFF5E35B1) else Color.Transparent,
                        shadowElevation = if (isAdminSelected) 2.dp else 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onRoleChange(UserRole.ADMIN) }
                            .testTag("tab_admin_erp")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = if (isAdminSelected) Color.White else Color(0xFF475569)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Admin ERP Panel",
                                fontSize = 11.sp,
                                fontWeight = if (isAdminSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (isAdminSelected) Color.White else Color(0xFF475569),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // 3. Delivery Boy App Tab
                    val isRiderSelected = currentRole == UserRole.RIDER
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isRiderSelected) FruitOrange else Color.Transparent,
                        shadowElevation = if (isRiderSelected) 2.dp else 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onRoleChange(UserRole.RIDER) }
                            .testTag("tab_delivery_boy")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeliveryDining,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isRiderSelected) Color.White else Color(0xFF475569)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Delivery Boy App",
                                fontSize = 11.sp,
                                fontWeight = if (isRiderSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (isRiderSelected) Color.White else Color(0xFF475569),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Row 3: Location selector bar (shown in Customer mode)
            if (currentRole == UserRole.CUSTOMER) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF3F6F4))
                        .clickable(onClick = onLocationClick)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("location_selector_bar"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        modifier = Modifier.size(16.dp),
                        tint = FruitOrange
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Delivering to: ",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = selectedAddressText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E281F),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}
