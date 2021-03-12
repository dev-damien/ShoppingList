package de.codingkeks.shoppinglist.recyclerview.friends

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
import java.util.*
import kotlin.collections.ArrayList

class FriendAdapter(var friends: List<Friend>, var spPos: Int, var listsFull: ArrayList<Friend> = ArrayList(friends)): RecyclerView.Adapter<FriendAdapter.FriendViewHolder>(), Filterable {
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
        holder.itemView.buFriendOptions.setOnClickListener {
            val popupMenu = PopupMenu(holder.itemView.context, holder.itemView.buFriendOptions)
            popupMenu.menuInflater.inflate(R.menu.popup_friend_options, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.action_friend_remove -> {
                        val alertBuilder = AlertDialog.Builder(
                            ContextThemeWrapper(holder.itemView.context, R.style.AlertDialogTheme)
                        )
                        with(alertBuilder){
                            val message = holder.itemView.context.getString(R.string.friend_dialog_remove_message) + "\n${friends[position].name}"
                            setMessage(message)
                            setTitle(R.string.friend_remove)
                            setPositiveButton(R.string.dialog_remove){ _, _ ->
                                //remove friend in user doc
                                val uid = FirebaseAuth.getInstance().currentUser!!.uid
                                val friendId = friends[position].friendId
                                val db = FirebaseFirestore.getInstance()
                                db.document("users/${uid}")
                                    .update("friends", FieldValue.arrayRemove(friendId))
                                    .addOnSuccessListener {
                                        Log.d(MainActivity.TAG, "friend was successfully removed from user-friendslist")
                                    }
                                //remove user in friend doc
                                db.document("users/${friendId}")
                                    .update("friends", FieldValue.arrayRemove(uid))
                                    .addOnSuccessListener {
                                        Log.d(MainActivity.TAG, "user was successfully removed from friend-friendslist")
                                    }

                            }
                            setNegativeButton(R.string.cancel){ _, _ ->
                                Log.d(MainActivity.TAG, "remove friend canceled")
                            }
                            show()
                        }
                    }
                }
                true
            }
            popupMenu.show()
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
            val filteredList: ArrayList<Friend> = ArrayList()

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

            when (spPos) {
                0 -> {
                    filteredList.sortBy { it.name.toLowerCase(Locale.ROOT) }
                }
                1 -> {
                    filteredList.sortByDescending { it.name.toLowerCase(Locale.ROOT) }
                }
            }

            val filterResults = FilterResults()
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