package de.codingkeks.shoppinglist.recyclerview.items

import android.annotation.SuppressLint
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import de.codingkeks.shoppinglist.R
import kotlinx.android.synthetic.main.rv_item_bought.view.*

class ItemBoughtAdapter(var items: List<Item>, var spPos: Int, var listId: String, var itemsFull: ArrayList<Item> = ArrayList<Item>(items))
    : RecyclerView.Adapter<ItemBoughtAdapter.ItemViewHolder>(), Filterable {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_item_bought, parent, false)
        return ItemViewHolder(view)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.itemView.apply {
            tvItemNameBought.text = items[position].name
            tvQuantityBought.text = items[position].quantity.toString()
            tvBoughtBy.text = items[position].boughtBy
            tvBoughtTime.text = items[position].boughtAt
        }
        holder.itemView.btnItemOptionsBought.setOnClickListener {
            val popupMenu = PopupMenu(holder.itemView.context, holder.itemView.btnItemOptionsBought)
            popupMenu.menuInflater.inflate(R.menu.popup_item_bought_options, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_bought -> {
                        val docRef = FirebaseFirestore.getInstance().document("lists/${listId}/items/${items[position].itemId}")
                        docRef.update("isBought", false)
                        updateList()
                        notifyDataSetChanged()
                    }
                    R.id.action_edit -> {

                    }
                    R.id.action_delete -> {
                        AlertDialog.Builder(ContextThemeWrapper(holder.itemView.context, R.style.AlertDialogTheme))
                            .setTitle(R.string.delete_item)
                            .setMessage(R.string.delete_item_sure)
                            .setPositiveButton(R.string.emailVerificationDelete) { _, _ ->
                                FirebaseFirestore.getInstance().document("lists/${listId}/items/${items[position].itemId}").delete()
                                updateList()
                                notifyDataSetChanged()
                            }
                            .setNegativeButton(R.string.cancel, null)
                            .show()
                    }
                }
                true
            })
            popupMenu.show()
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getFilter(): Filter {
        return filter
    }

    private var filter: Filter = object: Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            var filteredList: ArrayList<Item> = ArrayList<Item>()

            if (constraint == null || constraint.isEmpty()) {
                filteredList.addAll(itemsFull)
            } else {
                var filterPattern: String = constraint.toString().toLowerCase().trim()

                itemsFull.forEach {
                    if (it.name.toLowerCase().contains(filterPattern)) {
                        filteredList.add(it)
                    }
                }
            }

            when (spPos) { //position 0: Latest; 1: A-Z; 2: Z-A
                0 -> {
                    filteredList.sortBy { it.name }
                    filteredList.sortBy { it.addedTime }
                }
                1 -> {
                    filteredList.sortBy { it.name }
                }
                2 -> {
                    filteredList.sortByDescending { it.name }
                }
            }

            var filterResults: FilterResults = FilterResults()
            filterResults.values = filteredList
            return filterResults
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            (items as ArrayList<Item>).clear()
            if (results != null) {
                (items as ArrayList<Item>).addAll(results.values as ArrayList<Item>)
            }
            notifyDataSetChanged()
        }
    }

    fun updateList() {
        itemsFull = ArrayList<Item>(items)
    }

    fun updateSpinnerPos(spPos: Int) {
        this.spPos = spPos
    }
}