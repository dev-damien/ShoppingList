package de.codingkeks.shoppinglist.recyclerview.friends

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import de.codingkeks.shoppinglist.R
import kotlinx.android.synthetic.main.rv_friend.view.*

class FriendAdapter(var friends: List<Friend>, var spPos: Int, var listsFull: ArrayList<Friend> = ArrayList<Friend>(friends)): RecyclerView.Adapter<FriendAdapter.FriendViewHolder>(), Filterable {
    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.itemView.apply {
            tvFriendName.text = friends[position].name
            ivFriendImage.setImageResource(friends[position].profilePicture)
        }
    }

    override fun getItemCount(): Int {
        return friends.size
    }

    override fun getFilter(): Filter {
        return filter
    }

    private var filter: Filter = object: Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            var filteredList: ArrayList<Friend> = ArrayList<Friend>()

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

            when (spPos) {
                0 -> {
                    filteredList.sortBy { it.name.toLowerCase() }
                }
                1 -> {
                    filteredList.sortByDescending { it.name.toLowerCase() }
                }
            }

            var filterResults: FilterResults = FilterResults()
            filterResults.values = filteredList
            return filterResults
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            (friends as ArrayList<Friend>).clear()
            if (results != null) {
                (friends as ArrayList<Friend>).addAll(results.values as ArrayList<Friend>)
            }
            notifyDataSetChanged()
        }
    }

    fun updateList() {
        listsFull = ArrayList(friends)
    }

    fun updateSpinnerPos(position: Int) {
        spPos = position
    }
}