# Fruit Xpress — Complete Grocery Ordering & Delivery Platform

**Operating Zone:** Faridabad, Haryana, India (PIN Codes: 121001 – 121008)  
**Brand Colors:** Deep Green (`#006633`), Energetic Orange (`#FF6A00`), White, Neutral Green Tint (`#F8FAF8`)

---

## Architecture Overview

Fruit Xpress is built as a native Android application using Kotlin and Jetpack Compose with three role-based modules unified into a coherent system:

1. **Customer Storefront:**
   - Home screen with official Fruit Xpress brand banner and search bar.
   - Category filtering: Fresh Fruits, Daily Vegetables, Leafy Herbs, Seasonal Picks, Family Combos, Exotic Produce, Dry Fruits.
   - Stable product cards with Hindi names (e.g., सेब, केला, देसी टमाटर, पालक), weight units, discount percentages, stock indicators, and quantity steppers (+ / -).
   - Dedicated BigBasket-inspired **Delivery Location Selector**:
     - Manual Faridabad address entry with validation against serviced PIN codes.
     - Device GPS Location with permission and reverse geocoding fallback.
     - Saved addresses with Home/Work/Other tags and quick switching.
   - Cart with live item tally, promo coupons (`FRESH100`, `WELCOME50`, `FARIDABAD15`), delivery slot selector, payment methods (Cash on Delivery & UPI).
   - **Atomic Checkout & Duplicate Order Prevention:** Server-grade Room `@Transaction` with idempotency token, re-validating live product prices and decrementing stock.
   - Order tracking with visual status timeline (Placed → Accepted → Packed → Out for Delivery → Delivered) and 1-tap call to assigned delivery partner.
   - Printable/viewable **Tax Invoice & Receipt** dialog.

2. **Admin ERP (Store Management):**
   - KPI Dashboard: Total Sales, Pending Orders, Orders Out for Delivery, Delivered, Low-Stock items alert (< 15 units).
   - Order Management: Status transitions (Accept, Pack, Dispatch, Deliver, Cancel) and **Delivery Partner Assignment**.
   - Product Catalog Management: Add item, Edit item, stock adjustments (+10 / -5), and active/inactive toggles.
   - Store Settings: Serviced Faridabad PIN codes, free delivery threshold (₹299), delivery fee (₹30), min order value (₹99), store open/closed toggle, and audit log.

3. **Delivery Boy App (Rider Portal):**
   - Rider authentication & profile (Ravi Sharma - Delivery Partner).
   - Filtered view of assigned delivery runs only.
   - Order checklist with items, customer details, and 1-tap **Phone Call** intent.
   - 1-tap **Google Maps Navigation** intent launching navigation to the customer's delivery destination.
   - Delivery run workflow: Start Delivery Run → Confirm Delivered with mandatory Cash on Delivery (COD) collection acknowledgment.

---

## Role Switcher (For Testing & Verification)

At the top-right of the top bar, tap the role badge to switch between:
- **Customer Storefront** (Sachin Kumar, Pashupati Residency, Sector 19, Faridabad)
- **Admin ERP** (Fruit Xpress Store Owner / Hub Manager)
- **Delivery Partner** (Ravi Sharma, Faridabad Delivery Rider)

All three roles share the reactive database layer, so any order placed by Customer instantly appears in Admin ERP, can be assigned to Rider, and rider actions update customer tracking in real time.

---

## Production Setup & External Services Checklist

1. **Firebase Firestore & Authentication (Cloud Sync):**
   - Cloud schema blueprint defined in `firebase-blueprint.json`.
   - Zero-trust security rules defined in `firestore.rules`.
   - To link to your cloud Firebase project, accept Firebase setup in the AI Studio dialog.

2. **Online Payment Gateway (UPI / Cards):**
   - Baseline Cash on Delivery (COD) is 100% operational.
   - For live UPI callbacks, configure Razorpay or Cashfree merchant API keys in the Secrets panel.

3. **Google Play Store Release Checklist:**
   - App Name: Fruit Xpress (Short launcher label).
   - Package Name: `com.aistudio.fruitxpress.fxqmgr`
   - Adaptive Launcher Icon: Configured across all mipmap densities with white background and clean FX vector emblem.
   - Target SDK: Android 16 (API 36).
   - Insets: Full Edge-to-Edge (`enableEdgeToEdge`) with NavigationBar insets.
