package de.codingkeks.shoppinglist.recyclerview.members

import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import de.codingkeks.shoppinglist.MainActivity
import de.codingkeks.shoppinglist.R
import kotlinx.android.synthetic.main.rv_friend.view.*
import kotlinx.android.synthetic.main.rv_item.view.*

class MemberAdapter(var members: List<Member>, var spPos: Int, var listsFull: ArrayList<Member> = ArrayList<Member>(members)): RecyclerView.Adapter<MemberAdapter.MemberViewHolder>(), Filterable {
    inner class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.itemView.apply {

        }
    }

    override fun getItemCount(): Int {
        return members.size
    }

    override fun getFilter(): Filter {
        return filter
    }

    private var filter: Filter = object: Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            var filteredList: ArrayList<Member> = ArrayList<Member>()

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
                    filteredList.sortByDescending { it.isMember }
                }
                1 -> {
                    filteredList.sortByDescending { it.name.toLowerCase() }
                    filteredList.sortByDescending { it.isMember }
                }
            }

            var filterResults: FilterResults = FilterResults()
            filterResults.values = filteredList
            return filterResults
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            (members as ArrayList<Member>).clear()
            if (results != null) {
                (members as ArrayList<Member>).addAll(results.values as ArrayList<Member>)
            }
            notifyDataSetChanged()
        }
    }

    fun updateList() {
        listsFull = ArrayList(members)
    }

    fun updateSpinnerPos(position: Int) {
        spPos = position
    }

}