package com.example.recipemanager.core.model

/** A single aggregated line in the generated grocery list. */
data class GroceryItem(
    val name: String,
    val totalQty: Double,
    val unit: String,
    val category: GroceryCategory
) {
    /** Human-readable quantity string: omits qty when it is 0 (count-only). */
    fun displayQty(): String = when {
        totalQty <= 0.0 -> ""
        unit.isBlank() -> formatQty(totalQty)
        else -> "${formatQty(totalQty)} $unit"
    }

    private fun formatQty(qty: Double): String {
        val long = qty.toLong()
        return if (qty == long.toDouble()) long.toString() else "%.2g".format(qty)
    }
}

enum class GroceryCategory(val order: Int) {
    PRODUCE(0),
    DAIRY(1),
    MEAT(2),
    SEAFOOD(3),
    DRY_GOODS(4),
    SPICES(5),
    BAKERY(6),
    FROZEN(7),
    BEVERAGES(8),
    OTHER(9);
}
