package com.example.model

import androidx.annotation.DrawableRes

enum class NotificationType {
    LIKE, COMMENT, FOLLOW, REMIX, SYSTEM
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
    val isPrivate: Boolean = false
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
    val tags: List<String> = emptyList(),
    val likesCount: Int = 24,
    val commentsCount: Int = 3,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
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
    val timestamp: String
)

data class MusicTrack(
    val id: String,
    val title: String,
    val artist: String,
    val durationSeconds: Int,
    val genre: String,
    val bpm: Int
)
