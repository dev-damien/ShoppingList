package de.codingkeks.shoppinglist.recyclerview.images

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.codingkeks.shoppinglist.R
import kotlinx.android.synthetic.main.rv_image.view.*

class ImageAdapter(private var images: List<Image>): RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private var selectedImagePos:Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.itemView.apply {
            ivImage.setImageResource(images[position].src)
            if (position != selectedImagePos){
                ivImage.imageAlpha = 100
            }
            else{
                ivImage.imageAlpha = 255
            }
        }

        holder.itemView.setOnClickListener {
            if (selectedImagePos != position) {
                images[selectedImagePos].isSelected = false
                images[position].isSelected = true
                selectedImagePos = position
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return images.size
    }

}