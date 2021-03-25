package de.codingkeks.shoppinglist.recyclerview.shoppinglists

data class ShoppingList(
    var name: String,
    var listPicture: Int,
    var isFavorite: Boolean,
    var listId: String
)