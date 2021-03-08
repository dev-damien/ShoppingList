package de.codingkeks.shoppinglist.recyclerview.friendRequests

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.codingkeks.shoppinglist.R
import kotlinx.android.synthetic.main.rv_friend_request.view.*

class FriendRequestAdapter(var friendRequests: List<FriendRequest>): RecyclerView.Adapter<FriendRequestAdapter.FriendRequestViewHolder>() {
    inner class FriendRequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private var selectedImagePos:Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendRequestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_friend_request, parent, false)
        return FriendRequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendRequestViewHolder, position: Int) {

        holder.itemView.apply {
            ivFriendRequestImage.setImageResource(friendRequests[position].profilePicture)
            tvFriendRequestName.text = friendRequests[position].name

            ivFriendRequestAccept.setOnClickListener {
                //TODO accept friends request
            }

            ivFriendRequestDecline.setOnClickListener {
                //TODO decline friend request
            }
        }

    }

    override fun getItemCount(): Int {
        return friendRequests.size
    }

}