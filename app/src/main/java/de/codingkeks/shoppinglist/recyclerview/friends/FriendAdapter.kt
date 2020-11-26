package de.codingkeks.shoppinglist.recyclerview.friends

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.codingkeks.shoppinglist.R
import kotlinx.android.synthetic.main.nav_header_main.view.*
import kotlinx.android.synthetic.main.rv_friend.view.*

class FriendAdapter(var friends: List<Friend>): RecyclerView.Adapter<FriendAdapter.FriendViewHolder>()  {
    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.itemView.apply {
            tvFriendName.text = friends[position].name
            ivFriendImage.setImageResource(friends[position].profilePicture)
            if (friends[position].isFavorite) ivFriendFav.setImageResource(R.drawable.ic_friends_star)
            else ivFriendFav.setImageResource(R.drawable.ic_friends_star_border)

            ivFriendFav.setOnClickListener() {
                friends[position].isFavorite = !friends[position].isFavorite
            }
        }
    }

    override fun getItemCount(): Int {
        return friends.size
    }
}