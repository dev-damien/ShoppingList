package de.codingkeks.shoppinglist.ui.imagePicker

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DynamicGridLayoutManager : GridLayoutManager {

    private var columnWidth = 0
    private var columnWidthChanged = true

    constructor(context: Context, columnWidth: Int) : super(context, 1) {
        setColumnWidth(columnWidth)
    }

    fun setColumnWidth(newColumnWidth: Int) {
        if (newColumnWidth <= 0 || newColumnWidth == columnWidth) return
        columnWidth = newColumnWidth
        columnWidthChanged = false
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        super.onLayoutChildren(recycler, state)
        if (!columnWidthChanged || columnWidth <= 0) return
        val totalSpace = if (orientation == VERTICAL) {
            width - paddingRight - paddingLeft
        } else{
            height - paddingTop - paddingBottom
        }
        val spanCount = 1.coerceAtLeast(totalSpace / columnWidth)
        setSpanCount(spanCount)
        columnWidthChanged = false
    }

}