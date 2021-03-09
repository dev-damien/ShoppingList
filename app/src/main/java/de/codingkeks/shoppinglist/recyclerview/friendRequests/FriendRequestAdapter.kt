package de.codingkeks.shoppinglist.recyclerview.friendRequests

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import de.codingkeks.shoppinglist.R
import kotlinx.android.synthetic.main.rv_friend_request.view.*

class FriendRequestAdapter(var friendRequests: List<FriendRequest>):
    RecyclerView.Adapter<FriendRequestAdapter.FriendRequestViewHolder>() {

    inner class FriendRequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val db = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendRequestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_friend_request, parent, false)
        return FriendRequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendRequestViewHolder, position: Int) {
        holder.itemView.apply {
            ivFriendRequestImage.setImageResource(friendRequests[position].profilePicture)
            tvFriendRequestName.text = friendRequests[position].name

            val userDoc = db.document("users/${user.uid}")
            val requestedFriendId = friendRequests[position].friendId
            val requestedFriendDoc = db.document("users/${requestedFriendId}")

            ivFriendRequestAccept.setOnClickListener {
                // accept friend request and remove request
                userDoc.update("friendRequests", FieldValue.arrayRemove(requestedFriendId))
                userDoc.update("friends", FieldValue.arrayUnion(requestedFriendId))
                requestedFriendDoc.update("friends", FieldValue.arrayUnion(user.uid))
            }

            ivFriendRequestDecline.setOnClickListener {
                //decline friend request
                userDoc.update("friendRequests", FieldValue.arrayRemove(requestedFriendId))
            }
        }
    }

    override fun getItemCount(): Int {
        return friendRequests.size
    }

}