package com.example

import com.example.data.model.OrderStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testFaridabadServicedPinValidation() {
        val servicedPins = listOf("121001", "121002", "121003", "121004", "121005", "121006", "121007", "121008")

        assertTrue("Sector 19 Faridabad (121002) should be serviced", servicedPins.contains("121002"))
        assertTrue("Sector 15 Faridabad (121007) should be serviced", servicedPins.contains("121007"))
        assertFalse("Delhi PIN (110001) should not be serviced", servicedPins.contains("110001"))
        assertFalse("Gurugram PIN (122001) should not be serviced", servicedPins.contains("122001"))
    }

    @Test
    fun testDeliveryFeeCalculation() {
        val freeThreshold = 299.0
        val standardFee = 30.0

        val cartUnderThreshold = 229.0
        val feeUnder = if (cartUnderThreshold >= freeThreshold) 0.0 else standardFee
        assertEquals(30.0, feeUnder, 0.01)

        val cartOverThreshold = 349.0
        val feeOver = if (cartOverThreshold >= freeThreshold) 0.0 else standardFee
        assertEquals(0.0, feeOver, 0.01)
    }

    @Test
    fun testCouponDiscountLogic() {
        // Flat 100 off on >= 499
        val minOrder = 499.0
        val discountAmount = 100.0

        val subtotalEligible = 550.0
        val discount = if (subtotalEligible >= minOrder) discountAmount else 0.0
        assertEquals(100.0, discount, 0.01)

        val subtotalIneligible = 400.0
        val discountIneligible = if (subtotalIneligible >= minOrder) discountAmount else 0.0
        assertEquals(0.0, discountIneligible, 0.01)
    }

    @Test
    fun testOrderStatusSteps() {
        assertEquals(0, OrderStatus.PLACED.stepIndex)
        assertEquals(1, OrderStatus.ACCEPTED.stepIndex)
        assertEquals(2, OrderStatus.PACKED.stepIndex)
        assertEquals(3, OrderStatus.OUT_FOR_DELIVERY.stepIndex)
        assertEquals(4, OrderStatus.DELIVERED.stepIndex)
        assertEquals(-1, OrderStatus.CANCELLED.stepIndex)
    }

    @Test
    fun testProductImageHelperResolutions() {
        assertEquals(R.drawable.prod_apple, com.example.ui.components.ProductImageHelper.getProductDrawable("p_apple_shimla"))
        assertEquals(R.drawable.prod_banana, com.example.ui.components.ProductImageHelper.getProductDrawable("p_banana_robusta"))
        assertEquals(R.drawable.prod_mango, com.example.ui.components.ProductImageHelper.getProductDrawable("p_mango_alphonso"))
        assertEquals(R.drawable.prod_orange, com.example.ui.components.ProductImageHelper.getProductDrawable("p_orange_nagpur"))
        assertEquals(R.drawable.prod_grapes, com.example.ui.components.ProductImageHelper.getProductDrawable("p_grapes_sonaka"))
        assertEquals(R.drawable.prod_potato, com.example.ui.components.ProductImageHelper.getProductDrawable("p_potato_pahadi"))
        assertEquals(R.drawable.prod_tomato, com.example.ui.components.ProductImageHelper.getProductDrawable("p_tomato_desi"))
        assertEquals(R.drawable.prod_onion, com.example.ui.components.ProductImageHelper.getProductDrawable("p_onion_nasik"))
        assertEquals(R.drawable.prod_combo, com.example.ui.components.ProductImageHelper.getProductDrawable("p_combo_fruit_fiesta"))
    }
}
