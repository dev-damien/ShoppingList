package de.codingkeks.shoppinglist.recyclerview.items

data class Item(
    var name: String,
    var quantity: Int,
    var addedBy: String,
    var addedTime: String,
    var isBought: Boolean
)