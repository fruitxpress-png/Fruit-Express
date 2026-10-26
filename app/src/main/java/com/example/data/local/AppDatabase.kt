package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        SavedAddressEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        CouponEntity::class,
        StoreSettingsEntity::class,
        DeliverySlotEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fruit_xpress.db"
                )
                    .addCallback(SeedDataCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SeedDataCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.let { database ->
                    seedInitialData(database.appDao())
                }
            }
        }
    }
}

suspend fun seedInitialData(dao: AppDao) {
    // 1. Prepopulate standard users
    dao.insertUsers(
        listOf(
            UserEntity(
                id = "cust_sachin",
                name = "Sachin Kumar",
                phone = "9876543210",
                email = "sachin@fruitxpress.in",
                role = "CUSTOMER"
            ),
            UserEntity(
                id = "admin_main",
                name = "Fruit Xpress Admin",
                phone = "9999900000",
                email = "admin@fruitxpress.in",
                role = "ADMIN"
            ),
            UserEntity(
                id = "rider_ravi",
                name = "Ravi Sharma (Partner)",
                phone = "9811122233",
                email = "rider.ravi@fruitxpress.in",
                role = "RIDER"
            )
        )
    )

    // 2. Prepopulate Store Settings
    dao.updateStoreSettings(
        StoreSettingsEntity(
            id = 1,
            storeName = "Fruit Xpress",
            contactPhone = "+91 98765 43210",
            contactEmail = "orders@fruitxpress.in",
            operatingCity = "Faridabad, Haryana",
            servicedPinCodes = "121001, 121002, 121003, 121004, 121005, 121006, 121007, 121008",
            freeDeliveryThreshold = 299.0,
            deliveryFee = 30.0,
            minOrderValue = 99.0,
            isStoreOpen = true,
            upiId = "fruitxpress@okaxis"
        )
    )

    // 3. Prepopulate Delivery Slots
    dao.insertDeliverySlots(
        listOf(
            DeliverySlotEntity("slot_1", "Today Express", "6:00 AM - 9:00 AM"),
            DeliverySlotEntity("slot_2", "Today Midday", "11:00 AM - 2:00 PM"),
            DeliverySlotEntity("slot_3", "Today Evening", "5:00 PM - 8:00 PM"),
            DeliverySlotEntity("slot_4", "Tomorrow Early", "6:00 AM - 9:00 AM")
        )
    )

    // 4. Prepopulate Coupons
    dao.insertCoupons(
        listOf(
            CouponEntity(
                code = "FRESH100",
                description = "Flat ₹100 OFF on orders above ₹499",
                discountPercentage = 0.0,
                discountAmount = 100.0,
                minOrderValue = 499.0,
                maxDiscount = 100.0,
                expiryDate = "31 Dec 2026",
                isActive = true
            ),
            CouponEntity(
                code = "WELCOME50",
                description = "Flat ₹50 OFF for all fresh produce above ₹249",
                discountPercentage = 0.0,
                discountAmount = 50.0,
                minOrderValue = 249.0,
                maxDiscount = 50.0,
                expiryDate = "31 Dec 2026",
                isActive = true
            ),
            CouponEntity(
                code = "FARIDABAD15",
                description = "15% discount up to ₹75 on local orders",
                discountPercentage = 15.0,
                discountAmount = 0.0,
                minOrderValue = 199.0,
                maxDiscount = 75.0,
                expiryDate = "31 Dec 2026",
                isActive = true
            )
        )
    )

    // 5. Prepopulate Saved Addresses for Customer
    dao.insertAddress(
        SavedAddressEntity(
            id = "addr_home",
            userId = "cust_sachin",
            label = "Home",
            houseFlatNo = "Flat 402, Block B",
            buildingStreet = "Pashupati Residency",
            locality = "Near HUDA Market",
            sector = "Sector 19",
            city = "Faridabad",
            state = "Haryana",
            pinCode = "121002",
            landmark = "Opposite Community Center",
            latitude = 28.4089,
            longitude = 77.3178,
            isDefault = true
        )
    )
    dao.insertAddress(
        SavedAddressEntity(
            id = "addr_work",
            userId = "cust_sachin",
            label = "Work",
            houseFlatNo = "SCO 45, 1st Floor",
            buildingStreet = "Commercial Complex",
            locality = "Main Commercial Hub",
            sector = "Sector 15",
            city = "Faridabad",
            state = "Haryana",
            pinCode = "121007",
            landmark = "Behind Metro Station",
            latitude = 28.4180,
            longitude = 77.3125,
            isDefault = false
        )
    )

    // 6. Prepopulate Comprehensive Products
    dao.insertProducts(
        listOf(
            // Fresh Fruits
            ProductEntity(
                id = "p_apple_shimla",
                name = "Shimla Royal Apple",
                hindiName = "सेब (शिमला)",
                category = "fruits",
                unit = "1 kg",
                purchaseCost = 110.0,
                sellingPrice = 159.0,
                mrp = 199.0,
                stock = 45,
                isActive = true,
                isFeatured = true,
                isSeasonal = false,
                description = "Crisp, sweet, handpicked Royal Delicious apples directly sourced from Himachal orchards."
            ),
            ProductEntity(
                id = "p_banana_robusta",
                name = "Fresh Robusta Banana",
                hindiName = "केला (रोबस्टा)",
                category = "fruits",
                unit = "1 kg (~6 pcs)",
                purchaseCost = 25.0,
                sellingPrice = 40.0,
                mrp = 55.0,
                stock = 60,
                isActive = true,
                isFeatured = true,
                isSeasonal = false,
                description = "Naturally ripened, nutrient-packed sweet bananas ideal for breakfast and daily energy."
            ),
            ProductEntity(
                id = "p_orange_nagpur",
                name = "Nagpur Sweet Orange",
                hindiName = "संतरा (नागपुर)",
                category = "fruits",
                unit = "1 kg",
                purchaseCost = 50.0,
                sellingPrice = 79.0,
                mrp = 99.0,
                stock = 30,
                isActive = true,
                isFeatured = false,
                isSeasonal = true,
                description = "Juicy, citrus-rich fresh Nagpur oranges loaded with natural Vitamin C."
            ),
            ProductEntity(
                id = "p_pomegranate_anar",
                name = "Fresh Pomegranate (Anar)",
                hindiName = "अनार",
                category = "fruits",
                unit = "1 kg",
                purchaseCost = 100.0,
                sellingPrice = 149.0,
                mrp = 180.0,
                stock = 25,
                isActive = true,
                isFeatured = false,
                isSeasonal = false,
                description = "Ruby red, sweet arils rich in antioxidants and iron."
            ),
            ProductEntity(
                id = "p_papaya_desi",
                name = "Farm Fresh Papaya",
                hindiName = "पपीता",
                category = "fruits",
                unit = "1 pc (~1 kg)",
                purchaseCost = 30.0,
                sellingPrice = 49.0,
                mrp = 65.0,
                stock = 20,
                isActive = true,
                isFeatured = false,
                isSeasonal = false,
                description = "Sweet, digestive farm papaya picked at optimum ripeness."
            ),
            ProductEntity(
                id = "p_mango_alphonso",
                name = "Ratnagiri Alphonso Mango",
                hindiName = "हापुस आम (अल्फांसो)",
                category = "fruits",
                unit = "1 kg (~4-5 pcs)",
                purchaseCost = 280.0,
                sellingPrice = 380.0,
                mrp = 450.0,
                stock = 30,
                isActive = true,
                isFeatured = true,
                isSeasonal = true,
                description = "King of mangoes! Handpicked, naturally ripened, rich aromatic golden Alphonso."
            ),
            ProductEntity(
                id = "p_grapes_sonaka",
                name = "Fresh Sonaka Seedless Grapes",
                hindiName = "अंगूर (सोनाका)",
                category = "fruits",
                unit = "500 g",
                purchaseCost = 45.0,
                sellingPrice = 69.0,
                mrp = 90.0,
                stock = 40,
                isActive = true,
                isFeatured = true,
                isSeasonal = true,
                description = "Sweet, crisp elongated green seedless grapes packed with freshness."
            ),

            // Daily Vegetables
            ProductEntity(
                id = "p_tomato_desi",
                name = "Farm Desi Tomato",
                hindiName = "देसी टमाटर",
                category = "vegetables",
                unit = "1 kg",
                purchaseCost = 18.0,
                sellingPrice = 30.0,
                mrp = 40.0,
                stock = 80,
                isActive = true,
                isFeatured = true,
                isSeasonal = false,
                description = "Plump, red, farm-fresh local tomatoes with authentic tangy flavor."
            ),
            ProductEntity(
                id = "p_potato_pahadi",
                name = "Fresh Potato (Aloo)",
                hindiName = "आलू (पहाड़ी)",
                category = "vegetables",
                unit = "1 kg",
                purchaseCost = 18.0,
                sellingPrice = 28.0,
                mrp = 35.0,
                stock = 100,
                isActive = true,
                isFeatured = false,
                isSeasonal = false,
                description = "Solid, clean mountain potatoes perfect for daily curries and frying."
            ),
            ProductEntity(
                id = "p_onion_nasik",
                name = "Nasik Red Onion (Pyaz)",
                hindiName = "नासिक प्याज",
                category = "vegetables",
                unit = "1 kg",
                purchaseCost = 22.0,
                sellingPrice = 35.0,
                mrp = 45.0,
                stock = 90,
                isActive = true,
                isFeatured = false,
                isSeasonal = false,
                description = "Crisp, pungent red onions sourced directly from Nasik mandis."
            ),
            ProductEntity(
                id = "p_cauliflower_gobhi",
                name = "Fresh Cauliflower (Gobhi)",
                hindiName = "फूल गोभी",
                category = "vegetables",
                unit = "1 pc (~700g)",
                purchaseCost = 20.0,
                sellingPrice = 35.0,
                mrp = 48.0,
                stock = 35,
                isActive = true,
                isFeatured = false,
                isSeasonal = false,
                description = "Tightly packed snow-white florets, insect-free and clean."
            ),
            ProductEntity(
                id = "p_bhindi_ladyfinger",
                name = "Tender Bhindi (Okra)",
                hindiName = "भिंडी",
                category = "vegetables",
                unit = "500 g",
                purchaseCost = 26.0,
                sellingPrice = 42.0,
                mrp = 55.0,
                stock = 40,
                isActive = true,
                isFeatured = false,
                isSeasonal = false,
                description = "Small, tender green ladyfingers without hard seeds."
            ),

            // Leafy Herbs
            ProductEntity(
                id = "p_palak_spinach",
                name = "Fresh Palak (Spinach)",
                hindiName = "पालक",
                category = "leafy",
                unit = "1 bunch (~250g)",
                purchaseCost = 14.0,
                sellingPrice = 25.0,
                mrp = 35.0,
                stock = 30,
                isActive = true,
                isFeatured = true,
                isSeasonal = false,
                description = "Washed, tender green spinach leaves rich in iron and dietary fiber."
            ),
            ProductEntity(
                id = "p_coriander_dhaniya",
                name = "Green Coriander (Dhaniya)",
                hindiName = "हरा धनिया",
                category = "leafy",
                unit = "1 bunch (~100g)",
                purchaseCost = 8.0,
                sellingPrice = 15.0,
                mrp = 20.0,
                stock = 50,
                isActive = true,
                isFeatured = false,
                isSeasonal = false,
                description = "Aromatic fresh coriander bunch harvested fresh each morning."
            ),
            ProductEntity(
                id = "p_mint_pudina",
                name = "Fresh Mint (Pudina)",
                hindiName = "पुदीना",
                category = "leafy",
                unit = "1 bunch (~100g)",
                purchaseCost = 7.0,
                sellingPrice = 12.0,
                mrp = 18.0,
                stock = 25,
                isActive = true,
                isFeatured = false,
                isSeasonal = false,
                description = "Cooling aromatic mint sprigs for chutneys, beverages, and garnishing."
            ),

            // Seasonal Picks
            ProductEntity(
                id = "p_green_peas_matar",
                name = "Sweet Green Peas (Matar)",
                hindiName = "हरी मटर",
                category = "seasonal",
                unit = "500 g",
                purchaseCost = 32.0,
                sellingPrice = 50.0,
                mrp = 65.0,
                stock = 35,
                isActive = true,
                isFeatured = true,
                isSeasonal = true,
                description = "Freshly harvested sweet pods with plump green peas."
            ),
            ProductEntity(
                id = "p_guava_amrood",
                name = "Safeda Guava (Amrood)",
                hindiName = "सफेदा अमरुद",
                category = "seasonal",
                unit = "1 kg",
                purchaseCost = 35.0,
                sellingPrice = 59.0,
                mrp = 75.0,
                stock = 25,
                isActive = true,
                isFeatured = false,
                isSeasonal = true,
                description = "Crisp, sweet Allahabadi Safeda guavas with pink blush."
            ),

            // Family Combos
            ProductEntity(
                id = "p_combo_daily_basket",
                name = "Daily Veggie Saver Basket",
                hindiName = "दैनिक सब्ज़ी बास्केट",
                category = "combos",
                unit = "1 basket (5 kg)",
                purchaseCost = 135.0,
                sellingPrice = 199.0,
                mrp = 260.0,
                stock = 20,
                isActive = true,
                isFeatured = true,
                isSeasonal = false,
                description = "Includes Potato 2kg, Onion 1.5kg, Tomato 1kg, and Fresh Ginger 100g + Green Chillies 100g free."
            ),
            ProductEntity(
                id = "p_combo_fruit_fiesta",
                name = "Weekly Fruit Fiesta Box",
                hindiName = "फ्रूट फीएस्टा बॉक्स",
                category = "combos",
                unit = "1 box (3.5 kg)",
                purchaseCost = 240.0,
                sellingPrice = 349.0,
                mrp = 430.0,
                stock = 15,
                isActive = true,
                isFeatured = true,
                isSeasonal = false,
                description = "Includes Apple 1kg, Banana 1kg, Pomegranate 500g, and Nagpur Oranges 1kg."
            ),

            // Exotic Products
            ProductEntity(
                id = "p_kiwi_imported",
                name = "Fresh Zespri Kiwi",
                hindiName = "कीवी (पैक 3)",
                category = "exotic",
                unit = "Pack of 3",
                purchaseCost = 65.0,
                sellingPrice = 99.0,
                mrp = 130.0,
                stock = 20,
                isActive = true,
                isFeatured = false,
                isSeasonal = false,
                description = "Imported Vitamin C powerhouse with tangy emerald green flesh."
            ),
            ProductEntity(
                id = "p_avocado_hass",
                name = "Hass Avocado",
                hindiName = "एवोकाडो",
                category = "exotic",
                unit = "1 pc (~200g)",
                purchaseCost = 120.0,
                sellingPrice = 175.0,
                mrp = 220.0,
                stock = 15,
                isActive = true,
                isFeatured = false,
                isSeasonal = false,
                description = "Buttery, creamy ripe Hass avocado ideal for guacamole and salads."
            ),

            // Dry Fruits
            ProductEntity(
                id = "p_almonds_badam",
                name = "California Almonds (Badam)",
                hindiName = "कैलिफ़ोर्निया बादाम",
                category = "dry_fruits",
                unit = "250 g",
                purchaseCost = 165.0,
                sellingPrice = 220.0,
                mrp = 280.0,
                stock = 40,
                isActive = true,
                isFeatured = true,
                isSeasonal = false,
                description = "Premium crunch, 100% natural, sorted whole almonds."
            ),
            ProductEntity(
                id = "p_cashews_kaju",
                name = "Whole Cashews (Kaju W320)",
                hindiName = "काजू (W320)",
                category = "dry_fruits",
                unit = "250 g",
                purchaseCost = 180.0,
                sellingPrice = 245.0,
                mrp = 310.0,
                stock = 30,
                isActive = true,
                isFeatured = false,
                isSeasonal = false,
                description = "King-size white whole cashews with natural sweetness."
            )
        )
    )

    // Prepopulate a sample order for Sachin Kumar so order history and tracking are visible
    val initialOrder = OrderEntity(
        orderId = "FX102480",
        userId = "cust_sachin",
        customerName = "Sachin Kumar",
        customerPhone = "9876543210",
        deliveryAddress = "Flat 402, Block B, Pashupati Residency, Near HUDA Market, Sector 19, Faridabad - 121002",
        deliveryPinCode = "121002",
        deliverySlot = "Today Evening (5:00 PM - 8:00 PM)",
        subtotal = 229.0,
        deliveryFee = 0.0,
        discount = 0.0,
        couponCode = null,
        total = 229.0,
        paymentMethod = "Cash on Delivery (COD)",
        paymentStatus = "PENDING",
        status = "OUT_FOR_DELIVERY",
        placedAt = System.currentTimeMillis() - 3600000,
        assignedRiderId = "rider_ravi",
        assignedRiderName = "Ravi Sharma",
        assignedRiderPhone = "9811122233",
        idempotencyKey = "initial_seed_order_fx102480"
    )
    dao.insertOrder(initialOrder)
    dao.insertOrderItems(
        listOf(
            OrderItemEntity(
                orderId = "FX102480",
                productId = "p_apple_shimla",
                productName = "Shimla Royal Apple",
                hindiName = "सेब (शिमला)",
                unit = "1 kg",
                unitPrice = 159.0,
                quantity = 1,
                totalPrice = 159.0
            ),
            OrderItemEntity(
                orderId = "FX102480",
                productId = "p_banana_robusta",
                productName = "Fresh Robusta Banana",
                hindiName = "केला (रोबस्टा)",
                unit = "1 kg",
                unitPrice = 40.0,
                quantity = 1,
                totalPrice = 40.0
            ),
            OrderItemEntity(
                orderId = "FX102480",
                productId = "p_tomato_desi",
                productName = "Farm Desi Tomato",
                hindiName = "देसी टमाटर",
                unit = "1 kg",
                unitPrice = 30.0,
                quantity = 1,
                totalPrice = 30.0
            )
        )
    )
}
