package com.coptimize.openinventory.data.model

data class SaleWithItems(
    val sale: Sale,
    val items: List<SaleItem>
)