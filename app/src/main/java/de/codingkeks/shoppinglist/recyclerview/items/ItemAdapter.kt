package de.codingkeks.shoppinglist.recyclerview.items

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import de.codingkeks.shoppinglist.MainActivity
import de.codingkeks.shoppinglist.R
import kotlinx.android.synthetic.main.rv_item.view.*
import kotlinx.android.synthetic.main.rv_list.view.*

class ItemAdapter(var items: List<Item>, var spPos: Int, var itemsFull: ArrayList<Item> = ArrayList<Item>(items))
    : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>(), Filterable {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.itemView.apply {
            tvItemName.text = items[position].name
            tvQuantity.text = items[position].quantity.toString()
            tvContributor.text = items[position].addedBy
            tvAddedTime.text = items[position].addedTime
        }
        holder.itemView.setOnClickListener {
            Log.d(MainActivity.TAG, "Clicked on Item #$position: " + items[position].name)
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

            when (spPos) { //position 0: Favorites; 1: A-Z; 2: Z-A
                0 -> {
                    filteredList.sortBy { it.name }
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