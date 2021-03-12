package de.codingkeks.shoppinglist.recyclerview.members

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import de.codingkeks.shoppinglist.R
import kotlinx.android.synthetic.main.rv_member.view.*
import java.util.*
import kotlin.collections.ArrayList

class MemberAdapter(
    var members: List<Member>,
    var spPos: Int,
    var listsFull: ArrayList<Member> = ArrayList(members)
) : RecyclerView.Adapter<MemberAdapter.MemberViewHolder>(), Filterable {
    inner class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.itemView.apply {
            val uid = FirebaseAuth.getInstance().currentUser!!.uid
            tvMemberName.text = members[position].name
            ivMemberImage.setImageResource(members[position].profilePicture)
            if (members[position].friendId != uid) {
                if (members[position].isMember) ivMemberOptions.setImageResource(R.drawable.ic_remove_circle_outline)
                else ivMemberOptions.setImageResource(R.drawable.ic_add_circle_outline)
                ivMemberOptions.setOnClickListener {
                    members[position].isMember = !members[position].isMember
                    sortingMembersList()
                    notifyDataSetChanged()
                }
            } else ivMemberOptions.setImageResource(0)
        }
    }

    override fun getItemCount(): Int {
        return members.size
    }

    override fun getFilter(): Filter {
        return filter
    }

    private var filter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList: ArrayList<Member> = ArrayList()

            if (constraint == null || constraint.isEmpty()) {
                filteredList.addAll(listsFull)
            } else {
                val filterPattern: String = constraint.toString().toLowerCase(Locale.ROOT).trim()

                listsFull.forEach {
                    if (it.name.toLowerCase(Locale.ROOT).contains(filterPattern)) {
                        filteredList.add(it)
                    }
                }
            }

            val uid = FirebaseAuth.getInstance().currentUser!!.uid
            when (spPos) {
                0 -> {
                    filteredList.sortBy { it.name.toLowerCase(Locale.ROOT) }
                }
                1 -> {
                    filteredList.sortByDescending { it.name.toLowerCase(Locale.ROOT) }
                }
            }
            filteredList.sortByDescending { it.isMember }
            filteredList.sortByDescending { it.friendId == uid }

            val filterResults = FilterResults()
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

    private fun sortingMembersList() {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        when (spPos) { //0: A-Z; 1: Z-A
            0 -> {
                (members as ArrayList<Member>).sortBy { it.name.toLowerCase(Locale.ROOT) }
            }
            1 -> {
                (members as ArrayList<Member>).sortByDescending { it.name.toLowerCase(Locale.ROOT) }
            }
        }
        (members as ArrayList<Member>).sortByDescending { it.isMember }
        (members as ArrayList<Member>).sortByDescending { it.friendId == uid }
    }
}