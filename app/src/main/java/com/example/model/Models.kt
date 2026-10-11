package com.example.model

import androidx.annotation.DrawableRes

enum class NotificationType {
    LIKE, COMMENT, FOLLOW, REMIX, CALL, SYSTEM
}

enum class CallType {
    AUDIO, VIDEO
}

enum class CallState {
    IDLE, INCOMING, OUTGOING, CONNECTED, ENDED
}

data class UserProfile(
    val id: String,
    val name: String,
    val username: String,
    val contact: String = "",
    val bio: String = "Spreading positive green vibes on FG TIME 🌿✨",
    @DrawableRes val avatarResId: Int,
    val followers: Int = 124,
    val following: Int = 89,
    val postsCount: Int = 12,
    val isFollowing: Boolean = false,
    val isPrivate: Boolean = false,
    val isOwner: Boolean = false,
    val isVerified: Boolean = false,
    val dateOfBirth: String = "2001-08-15",
    val isOnline: Boolean = true,
    val hasPendingFollowRequest: Boolean = false
)

data class PostComment(
    val id: String,
    val postId: String,
    val authorName: String,
    val authorUsername: String,
    @DrawableRes val authorAvatarResId: Int,
    val commentText: String,
    val timeAgo: String,
    val likesCount: Int = 2,
    val isLiked: Boolean = false
)

data class Post(
    val id: String,
    val author: UserProfile,
    val content: String,
    @DrawableRes val imageResId: Int,
    val mediaList: List<Int> = emptyList(), // multi-photo carousel support
    val tags: List<String> = emptyList(),
    val mentions: List<String> = emptyList(),
    val likesCount: Int = 24,
    val commentsCount: Int = 3,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val isReported: Boolean = false,
    val timeAgo: String = "2h ago",
    val location: String = "FG Green Oasis"
)

data class StoryItem(
    val id: String,
    val author: UserProfile,
    @DrawableRes val imageResId: Int,
    val caption: String = "Growth in motion 🌿",
    val timeAgo: String = "3h ago",
    val isViewed: Boolean = false
)

data class ReelItem(
    val id: String,
    val creatorName: String,
    val creatorUsername: String,
    @DrawableRes val creatorAvatarResId: Int,
    val caption: String,
    val musicTitle: String,
    val musicArtist: String,
    val likesCount: Int,
    val commentsCount: Int,
    val sharesCount: Int,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    @DrawableRes val coverResId: Int
)

data class NotificationItem(
    val id: String,
    val type: NotificationType,
    val actorName: String,
    @DrawableRes val actorAvatarResId: Int,
    val content: String,
    val timeAgo: String,
    val isRead: Boolean = false
)

data class ChatMessage(
    val id: String,
    val text: String,
    val isMe: Boolean,
    val senderName: String = "",
    val timestamp: String,
    val isVoiceNote: Boolean = false,
    val voiceDurationSec: Int = 0,
    val isRead: Boolean = true
)

data class GroupChat(
    val id: String,
    val name: String,
    @DrawableRes val iconResId: Int,
    val memberCount: Int,
    val lastMessage: String,
    val adminUserId: String = "muksidul_owner"
)

data class CallRecord(
    val id: String,
    val contactName: String,
    @DrawableRes val contactAvatarResId: Int,
    val type: CallType,
    val isIncoming: Boolean,
    val timestamp: String,
    val durationSec: Int = 0,
    val wasMissed: Boolean = false
)

data class AIChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: String
)

data class AdminReport(
    val id: String,
    val postId: String,
    val postAuthorUsername: String,
    val postSnippet: String,
    val reportedByUsername: String,
    val reason: String,
    val timestamp: String,
    var status: String = "PENDING" // PENDING, RESOLVED, DISMISSED
)

data class MusicTrack(
    val id: String,
    val title: String,
    val artist: String,
    val durationSeconds: Int,
    val genre: String,
    val bpm: Int
)
