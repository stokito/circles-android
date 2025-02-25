package org.futo.circles.gallery.feature.select.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.futo.circles.core.extensions.loadProfileIcon
import org.futo.circles.core.extensions.onClick
import org.futo.circles.core.extensions.setIsVisible
import org.futo.circles.core.base.list.ViewBindingHolder
import org.futo.circles.core.model.SelectableRoomListItem
import org.futo.circles.gallery.databinding.ListItemSelectGalleryBinding

class SelectGalleryViewHolder(
    parent: ViewGroup,
    onGalleryClicked: (Int) -> Unit
) : RecyclerView.ViewHolder(inflate(parent, ListItemSelectGalleryBinding::inflate)) {

    private companion object : ViewBindingHolder

    private val binding = baseBinding as ListItemSelectGalleryBinding

    init {
        onClick(binding.baseGalleryItem.root) { position -> onGalleryClicked(position) }
    }

    fun bind(data: SelectableRoomListItem) {
        with(binding) {
            baseGalleryItem.ivGalleryImage.loadProfileIcon(data.info.avatarUrl, "")
            baseGalleryItem.tvGalleryName.text = data.info.title
            ivSelect.setImageResource(
                if (data.isSelected) org.futo.circles.core.R.drawable.ic_check_circle
                else org.futo.circles.core.R.drawable.ic_unselected
            )
            vSelectBackground.setIsVisible(data.isSelected)
        }
    }
}