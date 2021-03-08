package de.codingkeks.shoppinglist.recyclerview.shoppinglists

import android.content.Intent
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
import de.codingkeks.shoppinglist.ItemsActivity
import de.codingkeks.shoppinglist.MainActivity
import de.codingkeks.shoppinglist.R
import kotlinx.android.synthetic.main.rv_list.view.*

class ListAdapter(var lists: List<ShoppingList>, var spPos: Int, var listsFull: ArrayList<ShoppingList> = ArrayList<ShoppingList>(lists))
    : RecyclerView.Adapter<ListAdapter.ListViewHolder>(), Filterable {

    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_list, parent, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.itemView.apply {
            tvListName.text = lists[position].name
            ivListImage.setImageResource(lists[position].listPicture)
            if (lists[position].isFavorite) ivListFav.setImageResource(R.drawable.ic_friends_star)
            else ivListFav.setImageResource(R.drawable.ic_friends_star_border)

            ivListFav.setOnClickListener() {
                val user = FirebaseAuth.getInstance().currentUser!!
                val docRefUser = FirebaseFirestore.getInstance().document("users/${user.uid}")

                lists[position].isFavorite = !lists[position].isFavorite
                if (lists[position].isFavorite) {
                    docRefUser.update("favorites", FieldValue.arrayUnion(lists[position].listId))
                }
                else {
                    docRefUser.update("favorites", FieldValue.arrayRemove(lists[position].listId))
                }
                when (spPos) { //position 0: Favorites; 1: A-Z; 2: Z-A
                    0 -> {
                        (lists as ArrayList<ShoppingList>).sortBy { it.name }
                        (lists as ArrayList<ShoppingList>).sortByDescending { it.isFavorite }
                    }
                    1 -> {
                        (lists as ArrayList<ShoppingList>).sortBy { it.name }
                    }
                    2 -> {
                        (lists as ArrayList<ShoppingList>).sortByDescending { it.name }
                    }
                }
                notifyDataSetChanged()
            }
        }
        holder.itemView.setOnClickListener {
            Log.d(MainActivity.TAG, "Clicked on Item #$position: " + lists[position].name)
            val intent = Intent(holder.itemView.context, ItemsActivity::class.java)
            intent.putExtra("listId", lists[position].listId)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return lists.size
    }

    override fun getFilter(): Filter {
        return filter
    }

    private var filter: Filter = object: Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            var filteredList: ArrayList<ShoppingList> = ArrayList<ShoppingList>()

            if (constraint == null || constraint.isEmpty()) {
                filteredList.addAll(listsFull)
            } else {
                var filterPattern: String = constraint.toString().toLowerCase().trim()

                listsFull.forEach {
                    if (it.name.toLowerCase().contains(filterPattern)) {
                        filteredList.add(it)
                    }
                }
            }

            when (spPos) { //position 0: Favorites; 1: A-Z; 2: Z-A
                0 -> {
                    filteredList.sortBy { it.name }
                    filteredList.sortByDescending { it.isFavorite }
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
            (lists as ArrayList<ShoppingList>).clear()
            if (results != null) {
                (lists as ArrayList<ShoppingList>).addAll(results.values as ArrayList<ShoppingList>)
            }
            notifyDataSetChanged()
        }
    }

    fun updateList() {
        listsFull = ArrayList<ShoppingList>(lists)
    }

    fun updateSpinnerPos(spPos: Int) {
        this.spPos = spPos
    }
}