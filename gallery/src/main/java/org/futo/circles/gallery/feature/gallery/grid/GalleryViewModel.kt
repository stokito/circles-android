package org.futo.circles.gallery.feature.gallery.grid

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import dagger.hilt.android.lifecycle.HiltViewModel
import org.futo.circles.core.base.SingleEventLiveData
import org.futo.circles.core.extensions.getOrThrow
import org.futo.circles.core.extensions.launchBg
import org.futo.circles.core.feature.timeline.BaseTimelineViewModel
import org.futo.circles.core.feature.timeline.data_source.AccessLevelDataSource
import org.futo.circles.core.feature.timeline.data_source.SingleTimelineDataSource
import org.futo.circles.core.feature.timeline.post.PostContentDataSource
import org.futo.circles.core.feature.timeline.post.PostOptionsDataSource
import org.futo.circles.core.feature.timeline.post.SendMessageDataSource
import org.futo.circles.core.model.GalleryContentListItem
import org.futo.circles.core.model.MediaContent
import org.futo.circles.core.model.MediaType
import org.futo.circles.core.model.ShareableContent
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    timelineDataSource: SingleTimelineDataSource,
    private val sendMessageDataSource: SendMessageDataSource,
    private val mediaDataSource: PostContentDataSource,
    private val postOptionsDataSource: PostOptionsDataSource,
    private val accessLevelDataSource: AccessLevelDataSource
) : BaseTimelineViewModel(timelineDataSource) {

    private val roomId: String = savedStateHandle.getOrThrow("roomId")

    val accessLevelLiveData = accessLevelDataSource.accessLevelFlow.asLiveData()

    val galleryItemsLiveData = timelineDataSource.getTimelineEventFlow().asLiveData().map { list ->
        list.mapNotNull { post ->
            (post.content as? MediaContent)?.let {
                GalleryContentListItem(post.id, post.postInfo, it)
            }
        }
    }

    val shareLiveData = SingleEventLiveData<ShareableContent>()
    val downloadLiveData = SingleEventLiveData<Unit>()

    fun uploadMedia(uri: Uri, mediaType: MediaType) {
        launchBg {
            sendMessageDataSource.sendMedia(roomId, uri, null, null, mediaType)
        }
    }

    fun share(position: Int) {
        val eventId = galleryItemsLiveData.value?.getOrNull(position)?.id ?: return
        val content = mediaDataSource.getPostContent(roomId, eventId) ?: return
        launchBg {
            shareLiveData.postValue(postOptionsDataSource.getShareableContent(content))
        }
    }

    fun removeImage(position: Int) {
        val eventId = galleryItemsLiveData.value?.getOrNull(position)?.id ?: return
        postOptionsDataSource.removeMessage(roomId, eventId)
    }

    fun save(position: Int) {
        val eventId = galleryItemsLiveData.value?.getOrNull(position)?.id ?: return
        val content = mediaDataSource.getPostContent(roomId, eventId) ?: return
        launchBg {
            postOptionsDataSource.saveMediaToDevice(content)
            downloadLiveData.postValue(Unit)
        }
    }
}