package com.github.ecasept.ccsw.utils

fun formatPrice(price: Double): String {
    return "$${"%.2f".format(price)}"
}