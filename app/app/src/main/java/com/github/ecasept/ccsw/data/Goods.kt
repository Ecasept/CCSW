package com.github.ecasept.ccsw.data

import com.github.ecasept.ccsw.R

data class Good(
    val building: String,
    val name: String,
    val symbol: String,
    val res: Int,
)

private val goods = listOf(
    Good("Farm", "Cereals", "CRL", R.drawable.good_00),
    Good("Mine", "Chocolate", "CHC", R.drawable.good_01),
    Good("Factory", "Butter", "BTR", R.drawable.good_02),
    Good("Bank", "Sugar", "SUG", R.drawable.good_03),
    Good("Temple", "Nuts", "NUT", R.drawable.good_04),
    Good("Wizard tower", "Salt", "SLT", R.drawable.good_05),
    Good("Shipment", "Vanilla", "VNL", R.drawable.good_06),
    Good("Alchemy lab", "Eggs", "EGG", R.drawable.good_07),
    Good("Portal", "Cinnamon", "CNM", R.drawable.good_08),
    Good("Time machine", "Cream", "CRM", R.drawable.good_09),
    Good("Antimatter condenser", "Jam", "JAM", R.drawable.good_10),
    Good("Prism", "White chocolate", "WCH", R.drawable.good_11),
    Good("Chancemaker", "Honey", "HNY", R.drawable.good_12),
    Good("Fractal engine", "Cookies", "CKI", R.drawable.good_13),
    Good("Javascript console", "Recipes", "RCP", R.drawable.good_14),
    Good("Idleverse", "Subsidiaries", "SBD", R.drawable.good_15),
    Good("Cortex baker", "Publicists", "PBL", R.drawable.good_16),
    Good("You", "<Your name>", "YOU", R.drawable.good_17)
)

val Good.id get() = goods.indexOf(this)

fun getAllGoods(): List<Good> {
    return goods
}

private val goodMap = goods.associateBy { it.symbol }

fun getGood(symbol: String): Good? {
    return goodMap[symbol]
}
