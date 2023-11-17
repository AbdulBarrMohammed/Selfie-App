package com.example.selfieassignment


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.selfieassignment.databinding.ImageItemBinding

class ImageAdapter(private val onImageClick: (String) -> Unit) : ListAdapter<Image, ImageAdapter.ImageViewHolder>(ImageDiffCallback()) {

    /**
     * binds image url
     * @param none
     * @return imageviewholder
     */

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding, onImageClick)
    }
    /**
     * bind view holder
     * @param none
     * @return none
     */
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val currentImage = getItem(position)
        holder.bind(currentImage)
    }

    class ImageViewHolder(private val binding: ImageItemBinding, val onClick: (String) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        fun bind(image: Image) {
            Glide.with(binding.ivPost.context)
                .load(image.imageUrl)
                .into(binding.ivPost)
            binding.ivPost.setOnClickListener { onClick(image.imageUrl) }
        }
    }

    class ImageDiffCallback : DiffUtil.ItemCallback<Image>() {
        override fun areItemsTheSame(oldItem: Image, newItem: Image) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Image, newItem: Image) = oldItem == newItem
    }
}
