package de.codingkeks.shoppinglist.recyclerview.shoppinglists

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import de.codingkeks.shoppinglist.MainActivity
import de.codingkeks.shoppinglist.R
import kotlinx.android.synthetic.main.rv_list.view.*

class ListAdapter(var lists: List<ShoppingList>): RecyclerView.Adapter<ListAdapter.ListViewHolder>()  {

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
                lists[position].isFavorite = !lists[position].isFavorite
                if (lists[position].isFavorite) ivListFav.setImageResource(R.drawable.ic_friends_star)
                else ivListFav.setImageResource(R.drawable.ic_friends_star_border)
            }
        }
        holder.itemView.setOnClickListener {
            Log.d(MainActivity.TAG, "Clicked on Item #$position: " + lists[position].name)
        }
    }

    override fun getItemCount(): Int {
        return lists.size
    }
}