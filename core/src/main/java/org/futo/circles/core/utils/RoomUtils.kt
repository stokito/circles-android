package org.futo.circles.core.utils

import org.futo.circles.core.model.TIMELINE_TYPE
import org.futo.circles.core.provider.MatrixSessionProvider
import org.matrix.android.sdk.api.session.getRoom
import org.matrix.android.sdk.api.session.room.Room
import org.matrix.android.sdk.api.session.room.model.Membership

fun Room.getTimelineRoom(): Room? {
    val session = MatrixSessionProvider.currentSession ?: return null
    val childId = roomSummary()?.spaceChildren?.firstOrNull {
        val room = session.getRoom(it.childRoomId)?.roomSummary()
        room?.inviterId == null && room?.roomType == TIMELINE_TYPE
    }?.childRoomId
    return childId?.let { session.getRoom(it) }
}

fun getTimelineRoomFor(circleId: String): Room? {
    val session = MatrixSessionProvider.currentSession ?: return null
    return session.getRoom(circleId)?.getTimelineRoom()
}

fun getTimelineRoomIdOrThrow(circleId: String) = getTimelineRoomFor(circleId)?.roomId
    ?: throw IllegalArgumentException("Timeline not found")


fun getJoinedRoomById(roomId: String): Room? {
    val session = MatrixSessionProvider.currentSession ?: return null
    return session.roomService().getRoom(roomId)
        ?.takeIf { it.roomSummary()?.membership == Membership.JOIN }
}