package org.futo.circles.feature.timeline.post.menu

import org.futo.circles.core.model.PostContent


interface PostMenuListener {
    fun onShare(content: PostContent)
    fun onIgnore(senderId: String)
    fun onSaveToDevice(content: PostContent)
    fun onRemove(roomId: String, eventId: String)
    fun endPoll(roomId: String, eventId: String)
}