package org.futo.circles.feature.timeline.list

import android.annotation.SuppressLint
import android.text.method.LinkMovementMethod
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import org.futo.circles.core.base.list.ViewBindingHolder
import org.futo.circles.core.extensions.gone
import org.futo.circles.core.extensions.loadEncryptedThumbOrFullIntoWithAspect
import org.futo.circles.core.extensions.setIsVisible
import org.futo.circles.core.extensions.visible
import org.futo.circles.core.model.MediaContent
import org.futo.circles.core.model.MediaType
import org.futo.circles.core.model.PollContent
import org.futo.circles.core.model.Post
import org.futo.circles.core.model.TextContent
import org.futo.circles.databinding.ViewPollPostBinding
import org.futo.circles.databinding.ViewTextMediaPostBinding
import org.futo.circles.model.*
import org.futo.circles.view.PostLayout
import org.futo.circles.view.PostOptionsListener


sealed class PostViewHolder(view: View, private val isThread: Boolean) :
    RecyclerView.ViewHolder(view) {

    abstract val postLayout: PostLayout

    open fun bind(post: Post, userPowerLevel: Int) {
        postLayout.setData(post, userPowerLevel, isThread)
    }

    fun bindPayload(payload: PostItemPayload) {
        postLayout.setPayload(payload)
    }
}

class TextMediaPostViewHolder(
    parent: ViewGroup,
    postOptionsListener: PostOptionsListener,
    isThread: Boolean
) : PostViewHolder(inflate(parent, ViewTextMediaPostBinding::inflate), isThread),
    UploadMediaViewHolder {

    private companion object : ViewBindingHolder

    private val binding = baseBinding as ViewTextMediaPostBinding
    override val postLayout: PostLayout = binding.lPost
    override val uploadMediaTracker = UploadMediaTracker()

    init {
        binding.lPost.setListener(postOptionsListener)
        handleTextClick()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun handleTextClick() {
        binding.tvTextContent.apply {
            movementMethod = LinkMovementMethod.getInstance()
            setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) v.requestFocus()
                false
            }
        }
    }

    override fun bind(post: Post, userPowerLevel: Int) {
        super.bind(post, userPowerLevel)
        binding.vLoadingView.gone()
        when (val content = post.content) {
            is TextContent -> bindTextPost(content)
            is MediaContent -> {
                bindMediaContent(content)
                uploadMediaTracker.track(post.id, binding.vLoadingView)
            }

            else -> return
        }
    }

    private fun bindTextPost(content: TextContent) {
        binding.tvTextContent.apply {
            setText(content.messageSpanned, TextView.BufferType.SPANNABLE)
            visible()
        }
        binding.vMediaContent.lMedia.gone()
    }

    private fun bindMediaContent(content: MediaContent) {
        bindMediaCaption(content)
        bindMediaCover(content)
        binding.vMediaContent.videoGroup.setIsVisible(content.getMediaType() == MediaType.Video)
        binding.vMediaContent.tvDuration.text = content.mediaFileData.duration
    }

    private fun bindMediaCaption(content: MediaContent) {
        binding.tvTextContent.apply {
            val caption = content.captionSpanned
            setIsVisible(caption != null)
            caption?.let { setText(it, TextView.BufferType.SPANNABLE) }
        }
    }

    private fun bindMediaCover(content: MediaContent) {
        val image = binding.vMediaContent.ivCover
        image.post {
            val size = content.thumbnailOrFullSize(image.width)
            image.updateLayoutParams {
                width = size.width
                height = size.height
            }
        }
        content.loadEncryptedThumbOrFullIntoWithAspect(image)
    }
}

class PollPostViewHolder(
    parent: ViewGroup,
    private val postOptionsListener: PostOptionsListener,
    isThread: Boolean
) : PostViewHolder(inflate(parent, ViewPollPostBinding::inflate), isThread) {

    private companion object : ViewBindingHolder

    private val binding = baseBinding as ViewPollPostBinding
    override val postLayout: PostLayout = binding.lPollPost

    init {
        binding.lPollPost.setListener(postOptionsListener)
    }

    override fun bind(post: Post, userPowerLevel: Int) {
        super.bind(post, userPowerLevel)
        (post.content as? PollContent)?.let {
            binding.pollContentView.setup(it) { optionId ->
                postOptionsListener.onPollOptionSelected(post.postInfo.roomId, post.id, optionId)
            }
        }
    }
}