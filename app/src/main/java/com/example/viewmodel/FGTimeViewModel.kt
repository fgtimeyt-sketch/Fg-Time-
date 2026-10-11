package com.example.viewmodel

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Screen {
    WELCOME_AUTH,
    FEED,
    EXPLORE,
    CREATE_POST,
    REELS,
    REMIX_STUDIO,
    NOTIFICATIONS,
    PROFILE,
    DIRECT_MESSAGES,
    STORY_VIEWER
}

enum class Language {
    ENGLISH,
    HINDI
}

data class FGTimeUiState(
    val currentScreen: Screen = Screen.WELCOME_AUTH,
    val previousScreen: Screen = Screen.FEED,
    val isDarkMode: Boolean = true,
    val language: Language = Language.ENGLISH,
    val isLoggedIn: Boolean = false,
    val currentUser: UserProfile = UserProfile(
        id = "me",
        name = "Aarav Sharma",
        username = "aarav_fg",
        contact = "aarav@fgtime.com",
        bio = "Eco-creator & digital storyteller 🌿 Creating the future on FG TIME",
        avatarResId = R.drawable.fgtime_creator,
        followers = 342,
        following = 188,
        postsCount = 6
    ),
    val activeStoryIndex: Int = 0,
    val stories: List<StoryItem> = emptyList(),
    val posts: List<Post> = emptyList(),
    val commentsByPostId: Map<String, List<PostComment>> = emptyMap(),
    val activePostCommentsPostId: String? = null,
    val reels: List<ReelItem> = emptyList(),
    val currentReelIndex: Int = 0,
    val notifications: List<NotificationItem> = emptyList(),
    val searchUsers: List<UserProfile> = emptyList(),
    val searchQuery: String = "",
    val activeSearchFilter: String = "All",
    val chatMessages: List<ChatMessage> = emptyList(),
    // Remix Studio state
    val remixTracks: List<MusicTrack> = emptyList(),
    val selectedRemixTrackId: String = "t1",
    val isStudioPlaying: Boolean = false,
    val studioTrimStartSec: Float = 0f,
    val studioTrimEndSec: Float = 15f,
    val studioVolume: Float = 0.8f,
    val studioExportProgress: Int? = null,
    val studioExportSuccessMessage: String? = null,
    // Snackbar feedback message
    val snackbarMessage: String? = null
)

class FGTimeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FGTimeUiState())
    val uiState: StateFlow<FGTimeUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val userMaya = UserProfile(
            id = "u1",
            name = "Maya Green",
            username = "maya_creative",
            contact = "maya@example.com",
            bio = "Visual artist & sustainable living advocate 🍃",
            avatarResId = R.drawable.fgtime_creator,
            followers = 1420,
            following = 412,
            postsCount = 28,
            isFollowing = true
        )

        val userLeo = UserProfile(
            id = "u2",
            name = "Rohan Verma",
            username = "rohan_sound",
            contact = "rohan@example.com",
            bio = "Music producer • Lo-Fi & organic frequencies 🎧",
            avatarResId = R.drawable.fgtime_leaf_logo,
            followers = 890,
            following = 210,
            postsCount = 14,
            isFollowing = false
        )

        val userSara = UserProfile(
            id = "u3",
            name = "Priya Patel",
            username = "priya_botanics",
            contact = "priya@example.com",
            bio = "Urban greenhouse enthusiast 🌱 Green vibes only!",
            avatarResId = R.drawable.fgtime_banner,
            followers = 630,
            following = 150,
            postsCount = 9,
            isFollowing = true
        )

        val initialStories = listOf(
            StoryItem("s0", _uiState.value.currentUser, R.drawable.fgtime_creator, "My Daily Creative Space 🌿", "Just now", false),
            StoryItem("s1", userMaya, R.drawable.fgtime_banner, "Morning sunshine through the leaves ✨", "2h ago", false),
            StoryItem("s2", userLeo, R.drawable.fgtime_reel, "Remixing new organic soundscapes 🎵", "4h ago", false),
            StoryItem("s3", userSara, R.drawable.fgtime_creator, "New sprouts in the vertical garden!", "6h ago", false)
        )

        val initialPosts = listOf(
            Post(
                id = "p1",
                author = userMaya,
                content = "Embracing growth and creative stillness. When you surround yourself with green energy, creativity flows naturally. 🌿📷 #FGTime #GreenGrowth #NatureVibes",
                imageResId = R.drawable.fgtime_creator,
                tags = listOf("#FGTime", "#GreenGrowth", "#CreativeStudio"),
                likesCount = 158,
                commentsCount = 14,
                isLiked = false,
                isSaved = true,
                timeAgo = "1 hour ago",
                location = "Botanical Design Loft"
            ),
            Post(
                id = "p2",
                author = userLeo,
                content = "Fresh ambient session recorded live in the greenhouse! Tested it with the FG TIME Remix Studio and added lo-fi bird echoes. Swipe up to vibe with us. 🎧✨ #LoFiBeats #RemixStudio",
                imageResId = R.drawable.fgtime_banner,
                tags = listOf("#FGTime", "#RemixStudio", "#MusicCommunity"),
                likesCount = 230,
                commentsCount = 26,
                isLiked = true,
                isSaved = false,
                timeAgo = "3 hours ago",
                location = "Emerald Sound Studio"
            ),
            Post(
                id = "p3",
                author = userSara,
                content = "Welcome to FG TIME everyone! So excited to have a dedicated social network focused on positivity, creativity, and true connection. Drop your thoughts below! 🌱💫",
                imageResId = R.drawable.fgtime_reel,
                tags = listOf("#FGTimeFamily", "#Community", "#PositiveSocial"),
                likesCount = 94,
                commentsCount = 8,
                isLiked = false,
                isSaved = false,
                timeAgo = "5 hours ago",
                location = "Green City Sanctuary"
            )
        )

        val initialComments = mapOf(
            "p1" to listOf(
                PostComment("c1", "p1", "Rohan Verma", "rohan_sound", R.drawable.fgtime_leaf_logo, "The lighting and leaf contrast in this shot is unreal! 💚", "45m ago", 8, true),
                PostComment("c2", "p1", "Priya Patel", "priya_botanics", R.drawable.fgtime_banner, "Incredible energy Maya, keep shining! 🌱", "30m ago", 4, false)
            ),
            "p2" to listOf(
                PostComment("c3", "p2", "Aarav Sharma", "aarav_fg", R.drawable.fgtime_creator, "Listening to this on repeat right now! The remix sound is super crisp.", "1h ago", 12, true)
            )
        )

        val initialReels = listOf(
            ReelItem(
                id = "r1",
                creatorName = "Maya Green",
                creatorUsername = "maya_creative",
                creatorAvatarResId = R.drawable.fgtime_creator,
                caption = "Dancing inside the greenhouse when the golden hour hits 🌿✨ What inspires your art today?",
                musicTitle = "Emerald Pulse - LoFi Mix",
                musicArtist = "FG Beats Official",
                likesCount = 1840,
                commentsCount = 142,
                sharesCount = 88,
                isLiked = true,
                isSaved = true,
                coverResId = R.drawable.fgtime_reel
            ),
            ReelItem(
                id = "r2",
                creatorName = "Rohan Verma",
                creatorUsername = "rohan_sound",
                creatorAvatarResId = R.drawable.fgtime_leaf_logo,
                caption = "Quick remix breakdown: Adding acoustic leaves and synth bass inside FG TIME Remix Studio 🎚️",
                musicTitle = "Green Horizon Synthwave",
                musicArtist = "Rohan Verma",
                likesCount = 952,
                commentsCount = 74,
                sharesCount = 41,
                isLiked = false,
                isSaved = false,
                coverResId = R.drawable.fgtime_banner
            )
        )

        val initialNotifications = listOf(
            NotificationItem("n1", NotificationType.LIKE, "Maya Green", R.drawable.fgtime_creator, "liked your post 'Welcome to my space'", "10m ago", false),
            NotificationItem("n2", NotificationType.COMMENT, "Rohan Verma", R.drawable.fgtime_leaf_logo, "commented: 'Great sound design! 🌿'", "1h ago", false),
            NotificationItem("n3", NotificationType.FOLLOW, "Priya Patel", R.drawable.fgtime_banner, "started following your journey", "3h ago", true),
            NotificationItem("n4", NotificationType.REMIX, "FG Community", R.drawable.fgtime_leaf_icon, "Your reel was featured in Trending Remixes!", "1d ago", true)
        )

        val initialTracks = listOf(
            MusicTrack("t1", "Emerald Horizon (Lo-Fi)", "FG Studio Beats", 30, "Chillhop", 85),
            MusicTrack("t2", "Leaves in the Breeze", "Acoustic Green", 45, "Acoustic", 95),
            MusicTrack("t3", "Neon Botanic Pulse", "SynthFlora", 60, "Electronic", 120),
            MusicTrack("t4", "Morning Dew Ambient", "Nature Soundscapes", 30, "Ambient", 72)
        )

        val initialMessages = listOf(
            ChatMessage("m1", "Hey! Welcome to FG TIME 🌿 So excited you joined!", false, "10:14 AM"),
            ChatMessage("m2", "Thanks! Loving the green theme and the remix studio features.", true, "10:15 AM"),
            ChatMessage("m3", "Feel free to remix any of my tracks anytime! 🎧", false, "10:16 AM")
        )

        _uiState.update {
            it.copy(
                stories = initialStories,
                posts = initialPosts,
                commentsByPostId = initialComments,
                reels = initialReels,
                notifications = initialNotifications,
                searchUsers = listOf(userMaya, userLeo, userSara),
                remixTracks = initialTracks,
                chatMessages = initialMessages
            )
        }
    }

    fun navigateTo(screen: Screen) {
        _uiState.update {
            it.copy(
                previousScreen = if (screen != Screen.STORY_VIEWER) it.currentScreen else it.previousScreen,
                currentScreen = screen
            )
        }
    }

    fun toggleDarkMode() {
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    fun toggleLanguage() {
        _uiState.update {
            val newLang = if (it.language == Language.ENGLISH) Language.HINDI else Language.ENGLISH
            it.copy(
                language = newLang,
                snackbarMessage = if (newLang == Language.HINDI) "भाषा बदलकर हिन्दी की गई" else "Language switched to English"
            )
        }
    }

    fun performLogin(name: String, username: String, contact: String) {
        val updatedUser = _uiState.value.currentUser.copy(
            name = name.ifBlank { "FG Explorer" },
            username = username.ifBlank { "fg_explorer" },
            contact = contact
        )
        _uiState.update {
            it.copy(
                currentUser = updatedUser,
                isLoggedIn = true,
                currentScreen = Screen.FEED,
                snackbarMessage = if (it.language == Language.HINDI) "FG TIME में आपका स्वागत है! 🌿" else "Welcome to FG TIME! 🌿"
            )
        }
    }

    fun logout() {
        _uiState.update {
            it.copy(
                isLoggedIn = false,
                currentScreen = Screen.WELCOME_AUTH,
                snackbarMessage = if (it.language == Language.HINDI) "आप लॉग आउट हो चुके हैं" else "Logged out successfully"
            )
        }
    }

    fun toggleLikePost(postId: String) {
        _uiState.update { state ->
            val updatedPosts = state.posts.map { post ->
                if (post.id == postId) {
                    val wasLiked = post.isLiked
                    post.copy(
                        isLiked = !wasLiked,
                        likesCount = if (wasLiked) post.likesCount - 1 else post.likesCount + 1
                    )
                } else post
            }
            state.copy(posts = updatedPosts)
        }
    }

    fun toggleSavePost(postId: String) {
        _uiState.update { state ->
            val updatedPosts = state.posts.map { post ->
                if (post.id == postId) post.copy(isSaved = !post.isSaved) else post
            }
            val isNowSaved = updatedPosts.find { it.id == postId }?.isSaved == true
            state.copy(
                posts = updatedPosts,
                snackbarMessage = if (isNowSaved) "Saved to your Bookmarks 🔖" else "Removed from Bookmarks"
            )
        }
    }

    fun openCommentsFor(postId: String) {
        _uiState.update { it.copy(activePostCommentsPostId = postId) }
    }

    fun closeComments() {
        _uiState.update { it.copy(activePostCommentsPostId = null) }
    }

    fun addComment(postId: String, text: String) {
        if (text.isBlank()) return
        val newComment = PostComment(
            id = "c_${System.currentTimeMillis()}",
            postId = postId,
            authorName = _uiState.value.currentUser.name,
            authorUsername = _uiState.value.currentUser.username,
            authorAvatarResId = _uiState.value.currentUser.avatarResId,
            commentText = text,
            timeAgo = "Just now",
            likesCount = 0
        )
        _uiState.update { state ->
            val existing = state.commentsByPostId[postId] ?: emptyList()
            val updatedMap = state.commentsByPostId.toMutableMap()
            updatedMap[postId] = listOf(newComment) + existing
            val updatedPosts = state.posts.map {
                if (it.id == postId) it.copy(commentsCount = it.commentsCount + 1) else it
            }
            state.copy(
                commentsByPostId = updatedMap,
                posts = updatedPosts,
                snackbarMessage = if (state.language == Language.HINDI) "टिप्पणी पोस्ट की गई! 💬" else "Comment posted! 💬"
            )
        }
    }

    fun createNewPost(content: String, tagsStr: String, @DrawableRes imageRes: Int) {
        if (content.isBlank()) return
        val tags = tagsStr.split(" ").filter { it.startsWith("#") && it.length > 1 }
        val newPost = Post(
            id = "p_${System.currentTimeMillis()}",
            author = _uiState.value.currentUser,
            content = content,
            imageResId = imageRes,
            tags = if (tags.isNotEmpty()) tags else listOf("#FGTime", "#Growth"),
            likesCount = 1,
            commentsCount = 0,
            isLiked = false,
            isSaved = false,
            timeAgo = "Just now",
            location = "FG Creative Space"
        )
        _uiState.update { state ->
            state.copy(
                posts = listOf(newPost) + state.posts,
                currentScreen = Screen.FEED,
                currentUser = state.currentUser.copy(postsCount = state.currentUser.postsCount + 1),
                snackbarMessage = if (state.language == Language.HINDI) "पोस्ट सफलतापूर्वक प्रकाशित हुई! 🌿" else "Post published to feed! 🌿"
            )
        }
    }

    fun toggleFollowUser(userId: String) {
        _uiState.update { state ->
            val updatedUsers = state.searchUsers.map { user ->
                if (user.id == userId) {
                    val newFollowing = !user.isFollowing
                    user.copy(
                        isFollowing = newFollowing,
                        followers = if (newFollowing) user.followers + 1 else user.followers - 1
                    )
                } else user
            }
            state.copy(
                searchUsers = updatedUsers,
                snackbarMessage = if (state.language == Language.HINDI) "फॉलो स्थिति अपडेट हुई" else "Follow state updated"
            )
        }
    }

    fun openStory(index: Int) {
        _uiState.update {
            it.copy(
                activeStoryIndex = index,
                currentScreen = Screen.STORY_VIEWER
            )
        }
    }

    fun nextStory() {
        val nextIdx = _uiState.value.activeStoryIndex + 1
        if (nextIdx < _uiState.value.stories.size) {
            _uiState.update { it.copy(activeStoryIndex = nextIdx) }
        } else {
            navigateTo(Screen.FEED)
        }
    }

    fun prevStory() {
        val prevIdx = _uiState.value.activeStoryIndex - 1
        if (prevIdx >= 0) {
            _uiState.update { it.copy(activeStoryIndex = prevIdx) }
        }
    }

    fun toggleLikeReel(reelId: String) {
        _uiState.update { state ->
            val updatedReels = state.reels.map { reel ->
                if (reel.id == reelId) {
                    val wasLiked = reel.isLiked
                    reel.copy(
                        isLiked = !wasLiked,
                        likesCount = if (wasLiked) reel.likesCount - 1 else reel.likesCount + 1
                    )
                } else reel
            }
            state.copy(reels = updatedReels)
        }
    }

    fun toggleSaveReel(reelId: String) {
        _uiState.update { state ->
            val updatedReels = state.reels.map { reel ->
                if (reel.id == reelId) reel.copy(isSaved = !reel.isSaved) else reel
            }
            state.copy(reels = updatedReels)
        }
    }

    fun selectRemixTrack(trackId: String) {
        _uiState.update { it.copy(selectedRemixTrackId = trackId) }
    }

    fun toggleStudioPlayback() {
        _uiState.update { it.copy(isStudioPlaying = !it.isStudioPlaying) }
    }

    fun updateStudioTrim(start: Float, end: Float) {
        _uiState.update { it.copy(studioTrimStartSec = start, studioTrimEndSec = end) }
    }

    fun setStudioVolume(vol: Float) {
        _uiState.update { it.copy(studioVolume = vol) }
    }

    fun exportRemix() {
        viewModelScope.launch {
            _uiState.update { it.copy(studioExportProgress = 10, studioExportSuccessMessage = null) }
            delay(500)
            _uiState.update { it.copy(studioExportProgress = 45) }
            delay(500)
            _uiState.update { it.copy(studioExportProgress = 85) }
            delay(400)
            _uiState.update {
                val track = it.remixTracks.find { t -> t.id == it.selectedRemixTrackId }?.title ?: "Original Track"
                it.copy(
                    studioExportProgress = 100,
                    studioExportSuccessMessage = "Remix Exported Successfully! 🎬 Track: $track (Length: ${(it.studioTrimEndSec - it.studioTrimStartSec).toInt()}s)"
                )
            }
        }
    }

    fun clearStudioExportModal() {
        _uiState.update { it.copy(studioExportProgress = null, studioExportSuccessMessage = null) }
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val myMsg = ChatMessage(
            id = "m_${System.currentTimeMillis()}",
            text = text,
            isMe = true,
            timestamp = "Just now"
        )
        _uiState.update { it.copy(chatMessages = it.chatMessages + myMsg) }

        // Creative interactive response
        viewModelScope.launch {
            delay(1200)
            val replies = listOf(
                "Loved your post! Such a vibrant aesthetic 🌿",
                "Have you tried the new audio track in the Remix Studio?",
                "FG TIME community is growing so fast today! ✨",
                "Awesome! Let's collaborate on a creative reel soon 🎧"
            )
            val replyMsg = ChatMessage(
                id = "m_reply_${System.currentTimeMillis()}",
                text = replies.random(),
                isMe = false,
                timestamp = "Just now"
            )
            _uiState.update { it.copy(chatMessages = it.chatMessages + replyMsg) }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSearchFilter(filter: String) {
        _uiState.update { it.copy(activeSearchFilter = filter) }
    }

    fun markNotificationRead(id: String) {
        _uiState.update { state ->
            val updated = state.notifications.map {
                if (it.id == id) it.copy(isRead = true) else it
            }
            state.copy(notifications = updated)
        }
    }

    fun markAllNotificationsRead() {
        _uiState.update { state ->
            val updated = state.notifications.map { it.copy(isRead = true) }
            state.copy(notifications = updated)
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
