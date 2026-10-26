package com.example.ui.components

import androidx.annotation.DrawableRes
import com.example.R

object ProductImageHelper {
    /**
     * Resolves the appropriate bundled high-resolution produce image for any product.
     * Checks product ID and name keywords, and falls back to appropriate category produce photo.
     */
    @DrawableRes
    fun getProductDrawable(productId: String, productName: String = "", category: String = ""): Int {
        val id = productId.lowercase()
        val name = productName.lowercase()

        return when {
            // Match by specific product ID
            id.contains("apple") -> R.drawable.prod_apple
            id.contains("banana") -> R.drawable.prod_banana
            id.contains("orange") || id.contains("santra") -> R.drawable.prod_orange
            id.contains("mango") || id.contains("aam") -> R.drawable.prod_mango
            id.contains("grape") || id.contains("angoor") -> R.drawable.prod_grapes
            id.contains("guava") || id.contains("amrood") -> R.drawable.prod_guava
            id.contains("pomegranate") || id.contains("anar") -> R.drawable.prod_pomegranate
            id.contains("papaya") || id.contains("papita") -> R.drawable.prod_papaya
            id.contains("tomato") || id.contains("tamatar") -> R.drawable.prod_tomato
            id.contains("potato") || id.contains("aloo") -> R.drawable.prod_potato
            id.contains("onion") || id.contains("pyaz") -> R.drawable.prod_onion
            id.contains("cauliflower") || id.contains("gobhi") -> R.drawable.prod_cauliflower
            id.contains("bhindi") || id.contains("okra") -> R.drawable.prod_bhindi
            id.contains("palak") || id.contains("spinach") -> R.drawable.prod_palak
            id.contains("coriander") || id.contains("dhaniya") -> R.drawable.prod_coriander
            id.contains("mint") || id.contains("pudina") -> R.drawable.prod_mint
            id.contains("peas") || id.contains("matar") -> R.drawable.prod_peas
            id.contains("kiwi") -> R.drawable.prod_kiwi
            id.contains("avocado") -> R.drawable.prod_avocado
            id.contains("almond") || id.contains("badam") -> R.drawable.prod_almonds
            id.contains("cashew") || id.contains("kaju") -> R.drawable.prod_cashews
            id.contains("combo") || id.contains("basket") || id.contains("box") -> R.drawable.prod_combo

            // Match by product name (English or Hindi)
            name.contains("apple") || name.contains("सेब") -> R.drawable.prod_apple
            name.contains("banana") || name.contains("केला") -> R.drawable.prod_banana
            name.contains("orange") || name.contains("संतरा") -> R.drawable.prod_orange
            name.contains("mango") || name.contains("आम") -> R.drawable.prod_mango
            name.contains("grape") || name.contains("अंगूर") -> R.drawable.prod_grapes
            name.contains("guava") || name.contains("अमरुद") -> R.drawable.prod_guava
            name.contains("pomegranate") || name.contains("अनार") -> R.drawable.prod_pomegranate
            name.contains("papaya") || name.contains("पपीता") -> R.drawable.prod_papaya
            name.contains("tomato") || name.contains("टमाटर") -> R.drawable.prod_tomato
            name.contains("potato") || name.contains("आलू") -> R.drawable.prod_potato
            name.contains("onion") || name.contains("प्याज़") || name.contains("प्याज") -> R.drawable.prod_onion
            name.contains("cauliflower") || name.contains("गोभी") -> R.drawable.prod_cauliflower
            name.contains("bhindi") || name.contains("भिंडी") || name.contains("okra") -> R.drawable.prod_bhindi
            name.contains("palak") || name.contains("पालक") || name.contains("spinach") -> R.drawable.prod_palak
            name.contains("coriander") || name.contains("धनिया") -> R.drawable.prod_coriander
            name.contains("mint") || name.contains("पुदीना") -> R.drawable.prod_mint
            name.contains("peas") || name.contains("मटर") -> R.drawable.prod_peas
            name.contains("kiwi") || name.contains("कीवी") -> R.drawable.prod_kiwi
            name.contains("avocado") || name.contains("एवोकाडो") -> R.drawable.prod_avocado
            name.contains("almond") || name.contains("बादाम") -> R.drawable.prod_almonds
            name.contains("cashew") || name.contains("काजू") -> R.drawable.prod_cashews
            name.contains("combo") || name.contains("बास्केट") || name.contains("बॉक्स") -> R.drawable.prod_combo

            // Category fallback
            category == "fruits" -> R.drawable.prod_apple
            category == "vegetables" -> R.drawable.prod_tomato
            category == "leafy" -> R.drawable.prod_palak
            category == "seasonal" -> R.drawable.prod_orange
            category == "combos" -> R.drawable.prod_combo
            category == "exotic" -> R.drawable.prod_kiwi
            category == "dry_fruits" -> R.drawable.prod_almonds
            else -> R.drawable.prod_apple
        }
    }
}
