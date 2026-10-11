package com.example.viewmodel

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.model.*
import kotlinx.coroutines.Job
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
    STORY_VIEWER,
    CALL_SCREEN,
    AI_STUDIO,
    ADMIN_DASHBOARD
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
        postsCount = 6,
        isVerified = false,
        dateOfBirth = "2002-04-12"
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
    val groupChats: List<GroupChat> = emptyList(),
    // Remix Studio state
    val remixTracks: List<MusicTrack> = emptyList(),
    val selectedRemixTrackId: String = "t1",
    val isStudioPlaying: Boolean = false,
    val studioTrimStartSec: Float = 0f,
    val studioTrimEndSec: Float = 15f,
    val studioVolume: Float = 0.8f,
    val studioExportProgress: Int? = null,
    val studioExportSuccessMessage: String? = null,
    // Calling state
    val activeCall: CallRecord? = null,
    val callState: CallState = CallState.IDLE,
    val isCallMuted: Boolean = false,
    val isCallCameraEnabled: Boolean = true,
    val isCallFrontCamera: Boolean = true,
    val callDurationSeconds: Int = 0,
    val callHistory: List<CallRecord> = emptyList(),
    // AI Assistant state
    val aiMessages: List<AIChatMessage> = emptyList(),
    val isAILoading: Boolean = false,
    // Admin & Moderation state
    val adminReports: List<AdminReport> = emptyList(),
    // Snackbar feedback message
    val snackbarMessage: String? = null
)

class FGTimeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FGTimeUiState())
    val uiState: StateFlow<FGTimeUiState> = _uiState.asStateFlow()

    private var callTimerJob: Job? = null

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val userMuksidul = UserProfile(
            id = "u_owner_muksidul",
            name = "Muksidul",
            username = "muksidul",
            contact = "muksidul@fgtime.com",
            bio = "Creator & Founder of FG TIME 🌿 Empowering authentic growth and creative connection.",
            avatarResId = R.drawable.fgtime_leaf_logo,
            followers = 5840,
            following = 142,
            postsCount = 42,
            isFollowing = true,
            isOwner = true,
            isVerified = true,
            isOnline = true,
            dateOfBirth = "1999-05-20"
        )

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
            isFollowing = true,
            isVerified = true,
            isOnline = true
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
            isFollowing = false,
            isOnline = false
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
            isFollowing = true,
            isOnline = true
        )

        val initialStories = listOf(
            StoryItem("s_owner", userMuksidul, R.drawable.fgtime_banner, "Welcome to the new FG TIME upgrade! 🌿✨", "1h ago", false),
            StoryItem("s1", userMaya, R.drawable.fgtime_creator, "Morning sunshine through the leaves ✨", "2h ago", false),
            StoryItem("s2", userLeo, R.drawable.fgtime_reel, "Remixing new organic soundscapes 🎵", "4h ago", false),
            StoryItem("s3", userSara, R.drawable.fgtime_creator, "New vertical garden sprouts!", "6h ago", false)
        )

        val initialPosts = listOf(
            Post(
                id = "p_owner",
                author = userMuksidul,
                content = "Welcome everyone to FG TIME! 🌿 We built this platform for creativity, peace of mind, and genuine community. Check out the new Remix Studio, Audio & Video Calls, and AI Creator Assistant. Let us know your thoughts! #FGTime #FounderNote #CreativityUnleashed",
                imageResId = R.drawable.fgtime_banner,
                tags = listOf("#FGTime", "#FounderNote", "#Community"),
                mentions = listOf("@muksidul", "@maya_creative"),
                likesCount = 485,
                commentsCount = 38,
                isLiked = true,
                isSaved = true,
                timeAgo = "30m ago",
                location = "FG TIME Global HQ"
            ),
            Post(
                id = "p1",
                author = userMaya,
                content = "Embracing growth and creative stillness. When you surround yourself with green energy, creativity flows naturally. 🌿📷 #FGTime #GreenGrowth #NatureVibes",
                imageResId = R.drawable.fgtime_creator,
                mediaList = listOf(R.drawable.fgtime_creator, R.drawable.fgtime_banner),
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
                imageResId = R.drawable.fgtime_reel,
                tags = listOf("#FGTime", "#RemixStudio", "#MusicCommunity"),
                likesCount = 230,
                commentsCount = 26,
                isLiked = true,
                isSaved = false,
                timeAgo = "3 hours ago",
                location = "Emerald Sound Studio"
            )
        )

        val initialComments = mapOf(
            "p_owner" to listOf(
                PostComment("c_owner_1", "p_owner", "Maya Green", "maya_creative", R.drawable.fgtime_creator, "Congratulations Muksidul! The new features feel so smooth and vibrant 💚", "20m ago", 15, true),
                PostComment("c_owner_2", "p_owner", "Rohan Verma", "rohan_sound", R.drawable.fgtime_leaf_logo, "The calling and audio studio integration is game-changing!", "10m ago", 9, false)
            ),
            "p1" to listOf(
                PostComment("c1", "p1", "Rohan Verma", "rohan_sound", R.drawable.fgtime_leaf_logo, "The lighting and leaf contrast in this shot is unreal! 💚", "45m ago", 8, true),
                PostComment("c2", "p1", "Priya Patel", "priya_botanics", R.drawable.fgtime_banner, "Incredible energy Maya, keep shining! 🌱", "30m ago", 4, false)
            )
        )

        val initialReels = listOf(
            ReelItem(
                id = "r_owner",
                creatorName = "Muksidul",
                creatorUsername = "muksidul",
                creatorAvatarResId = R.drawable.fgtime_leaf_logo,
                caption = "Behind the scenes building FG TIME 🌿 Coding in nature, where every idea grows into reality.",
                musicTitle = "FG Time Anthem (Acoustic)",
                musicArtist = "Muksidul",
                likesCount = 3420,
                commentsCount = 256,
                sharesCount = 190,
                isLiked = true,
                isSaved = true,
                coverResId = R.drawable.fgtime_banner
            ),
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
                isSaved = false,
                coverResId = R.drawable.fgtime_reel
            )
        )

        val initialNotifications = listOf(
            NotificationItem("n_owner", NotificationType.SYSTEM, "Muksidul (Founder)", R.drawable.fgtime_leaf_logo, "welcomed you to FG TIME 🌿", "Just now", false),
            NotificationItem("n1", NotificationType.LIKE, "Maya Green", R.drawable.fgtime_creator, "liked your post 'Welcome to my space'", "10m ago", false),
            NotificationItem("n2", NotificationType.COMMENT, "Rohan Verma", R.drawable.fgtime_leaf_logo, "commented: 'Great sound design! 🌿'", "1h ago", false),
            NotificationItem("n3", NotificationType.FOLLOW, "Priya Patel", R.drawable.fgtime_banner, "started following your journey", "3h ago", true),
            NotificationItem("n4", NotificationType.CALL, "Maya Green", R.drawable.fgtime_creator, "missed video call", "5h ago", true)
        )

        val initialTracks = listOf(
            MusicTrack("t1", "Emerald Horizon (Lo-Fi)", "FG Studio Beats", 30, "Chillhop", 85),
            MusicTrack("t2", "Leaves in the Breeze", "Acoustic Green", 45, "Acoustic", 95),
            MusicTrack("t3", "Neon Botanic Pulse", "SynthFlora", 60, "Electronic", 120),
            MusicTrack("t4", "Morning Dew Ambient", "Nature Soundscapes", 30, "Ambient", 72)
        )

        val initialMessages = listOf(
            ChatMessage("m1", "Hey! Welcome to FG TIME 🌿 Founder Muksidul and the community are so glad you are here!", false, "Maya Green", "10:14 AM"),
            ChatMessage("m2", "Thanks! The calling, remix studio, and dark green design look incredible.", true, "Aarav", "10:15 AM"),
            ChatMessage("m3", "Feel free to voice call me or remix any of my tracks anytime! 🎧", false, "Maya Green", "10:16 AM")
        )

        val initialGroups = listOf(
            GroupChat("g1", "FG TIME Creators Hub 🌿", R.drawable.fgtime_banner, 48, "Muksidul: New creator tools just went live!", "u_owner_muksidul"),
            GroupChat("g2", "Sound & Remix Lab 🎧", R.drawable.fgtime_reel, 24, "Rohan: Dropping a fresh ambient pack today", "u2")
        )

        val initialCalls = listOf(
            CallRecord("cl1", "Muksidul (Founder)", R.drawable.fgtime_leaf_logo, CallType.VIDEO, true, "Today, 11:30 AM", 240, false),
            CallRecord("cl2", "Maya Green", R.drawable.fgtime_creator, CallType.AUDIO, false, "Yesterday, 4:15 PM", 185, false),
            CallRecord("cl3", "Rohan Verma", R.drawable.fgtime_leaf_logo, CallType.VIDEO, true, "Oct 10, 2:00 PM", 0, true)
        )

        val initialAIMessages = listOf(
            AIChatMessage("ai_welcome", "Hello! I am your FG TIME AI Creative Assistant 🌿 I can help you draft viral post captions, find trending botanical hashtags, outline video remix ideas, or assist with translations. How can I help you create today?", false, "Just now")
        )

        val initialReports = listOf(
            AdminReport("rep1", "p_ext", "unknown_spammer", "Spam links to external gambling", "priya_botanics", "Inappropriate advertising", "1h ago", "PENDING")
        )

        _uiState.update {
            it.copy(
                stories = initialStories,
                posts = initialPosts,
                commentsByPostId = initialComments,
                reels = initialReels,
                notifications = initialNotifications,
                searchUsers = listOf(userMuksidul, userMaya, userLeo, userSara),
                remixTracks = initialTracks,
                chatMessages = initialMessages,
                groupChats = initialGroups,
                callHistory = initialCalls,
                aiMessages = initialAIMessages,
                adminReports = initialReports
            )
        }
    }

    fun navigateTo(screen: Screen) {
        _uiState.update {
            it.copy(
                previousScreen = if (screen != Screen.STORY_VIEWER && screen != Screen.CALL_SCREEN) it.currentScreen else it.previousScreen,
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

    fun performLogin(name: String, username: String, contact: String, dob: String = "2001-08-15") {
        val isMuksidul = username.trim().equals("muksidul", ignoreCase = true)
        val updatedUser = if (isMuksidul) {
            UserProfile(
                id = "u_owner_muksidul",
                name = "Muksidul",
                username = "muksidul",
                contact = contact.ifBlank { "muksidul@fgtime.com" },
                bio = "Creator & Founder of FG TIME 🌿 Empowering authentic growth and creative connection.",
                avatarResId = R.drawable.fgtime_leaf_logo,
                followers = 5840,
                following = 142,
                postsCount = 42,
                isOwner = true,
                isVerified = true,
                dateOfBirth = dob
            )
        } else {
            _uiState.value.currentUser.copy(
                name = name.ifBlank { "FG Explorer" },
                username = username.ifBlank { "fg_explorer" },
                contact = contact,
                dateOfBirth = dob
            )
        }

        _uiState.update {
            it.copy(
                currentUser = updatedUser,
                isLoggedIn = true,
                currentScreen = Screen.FEED,
                snackbarMessage = if (it.language == Language.HINDI) "FG TIME में आपका स्वागत है! 🌿" else "Welcome to FG TIME, ${updatedUser.name}! 🌿"
            )
        }
    }

    fun recoverAccount(contact: String) {
        _uiState.update {
            it.copy(
                snackbarMessage = "Password reset instructions sent to $contact. Check your inbox!"
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

    fun editPost(postId: String, newContent: String) {
        _uiState.update { state ->
            val updated = state.posts.map {
                if (it.id == postId) it.copy(content = newContent) else it
            }
            state.copy(posts = updated, snackbarMessage = "Post updated successfully! ✏️")
        }
    }

    fun deletePost(postId: String) {
        _uiState.update { state ->
            val updated = state.posts.filterNot { it.id == postId }
            state.copy(
                posts = updated,
                currentUser = state.currentUser.copy(postsCount = (state.currentUser.postsCount - 1).coerceAtLeast(0)),
                snackbarMessage = "Post deleted from feed 🗑️"
            )
        }
    }

    fun reportPost(postId: String, reason: String) {
        val post = _uiState.value.posts.find { it.id == postId }
        val newReport = AdminReport(
            id = "rep_${System.currentTimeMillis()}",
            postId = postId,
            postAuthorUsername = post?.author?.username ?: "unknown",
            postSnippet = post?.content?.take(50) ?: "",
            reportedByUsername = _uiState.value.currentUser.username,
            reason = reason,
            timestamp = "Just now"
        )
        _uiState.update { state ->
            val updatedPosts = state.posts.map {
                if (it.id == postId) it.copy(isReported = true) else it
            }
            state.copy(
                posts = updatedPosts,
                adminReports = listOf(newReport) + state.adminReports,
                snackbarMessage = "Report submitted. Thank you for keeping FG TIME safe! 🛡️"
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

    fun createNewPost(content: String, tagsStr: String, @DrawableRes imageRes: Int, mentionsStr: String = "") {
        if (content.isBlank()) return
        val tags = tagsStr.split(" ").filter { it.startsWith("#") && it.length > 1 }
        val mentions = mentionsStr.split(" ").filter { it.startsWith("@") && it.length > 1 }
        val newPost = Post(
            id = "p_${System.currentTimeMillis()}",
            author = _uiState.value.currentUser,
            content = content,
            imageResId = imageRes,
            mediaList = listOf(imageRes, R.drawable.fgtime_banner),
            tags = if (tags.isNotEmpty()) tags else listOf("#FGTime", "#Growth"),
            mentions = mentions,
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
                snackbarMessage = if (state.language == Language.HINDI) "फ़ॉलो स्थिति अपडेट हुई" else "Follow state updated"
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

    fun sendChatMessage(text: String, isVoice: Boolean = false, durationSec: Int = 0) {
        if (text.isBlank() && !isVoice) return
        val myMsg = ChatMessage(
            id = "m_${System.currentTimeMillis()}",
            text = if (isVoice) "Voice message (${durationSec}s) 🎙️" else text,
            isMe = true,
            senderName = _uiState.value.currentUser.name,
            timestamp = "Just now",
            isVoiceNote = isVoice,
            voiceDurationSec = durationSec
        )
        _uiState.update { it.copy(chatMessages = it.chatMessages + myMsg) }

        // Creative interactive response
        viewModelScope.launch {
            delay(1200)
            val replies = listOf(
                "Loved your post! Such a vibrant aesthetic 🌿",
                "Have you tried the new audio track in the Remix Studio?",
                "FG TIME community is growing so fast today! ✨",
                "Awesome! Let's collaborate on a creative reel soon 🎧",
                "Founder Muksidul just dropped a new update, check the announcement!"
            )
            val replyMsg = ChatMessage(
                id = "m_reply_${System.currentTimeMillis()}",
                text = replies.random(),
                isMe = false,
                senderName = "Maya Green",
                timestamp = "Just now"
            )
            _uiState.update { it.copy(chatMessages = it.chatMessages + replyMsg) }
        }
    }

    // CALLING LOGIC
    fun startCall(contactName: String, @DrawableRes avatarResId: Int, type: CallType) {
        val newCall = CallRecord(
            id = "call_${System.currentTimeMillis()}",
            contactName = contactName,
            contactAvatarResId = avatarResId,
            type = type,
            isIncoming = false,
            timestamp = "Just now"
        )
        _uiState.update {
            it.copy(
                activeCall = newCall,
                callState = CallState.OUTGOING,
                callDurationSeconds = 0,
                isCallMuted = false,
                currentScreen = Screen.CALL_SCREEN
            )
        }

        // Simulate contact picking up after 2.5s
        viewModelScope.launch {
            delay(2500)
            if (_uiState.value.callState == CallState.OUTGOING) {
                _uiState.update { it.copy(callState = CallState.CONNECTED) }
                startCallTimer()
            }
        }
    }

    fun acceptIncomingCall() {
        _uiState.update { it.copy(callState = CallState.CONNECTED) }
        startCallTimer()
    }

    fun endCall() {
        callTimerJob?.cancel()
        val finishedCall = _uiState.value.activeCall?.copy(
            durationSec = _uiState.value.callDurationSeconds
        )
        _uiState.update { state ->
            state.copy(
                callState = CallState.ENDED,
                callHistory = if (finishedCall != null) listOf(finishedCall) + state.callHistory else state.callHistory,
                snackbarMessage = "Call ended (${state.callDurationSeconds}s)"
            )
        }
        viewModelScope.launch {
            delay(800)
            _uiState.update {
                it.copy(
                    activeCall = null,
                    callState = CallState.IDLE,
                    currentScreen = Screen.DIRECT_MESSAGES
                )
            }
        }
    }

    private fun startCallTimer() {
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (_uiState.value.callState == CallState.CONNECTED) {
                delay(1000)
                _uiState.update { it.copy(callDurationSeconds = it.callDurationSeconds + 1) }
            }
        }
    }

    fun toggleCallMute() {
        _uiState.update { it.copy(isCallMuted = !it.isCallMuted) }
    }

    fun toggleCallCamera() {
        _uiState.update { it.copy(isCallCameraEnabled = !it.isCallCameraEnabled) }
    }

    fun flipCallCamera() {
        _uiState.update { it.copy(isCallFrontCamera = !it.isCallFrontCamera) }
    }

    // AI ASSISTANT LOGIC
    fun sendAIMessage(userPrompt: String) {
        if (userPrompt.isBlank()) return
        val userMsg = AIChatMessage("u_${System.currentTimeMillis()}", userPrompt, true, "Just now")
        _uiState.update {
            it.copy(
                aiMessages = it.aiMessages + userMsg,
                isAILoading = true
            )
        }

        viewModelScope.launch {
            delay(1200)
            val response = when {
                userPrompt.contains("caption", ignoreCase = true) ->
                    "Here are 3 aesthetic captions for FG TIME:\n\n1. \"Rooted in passion, branching into new creations. 🌿✨\"\n2. \"Catching the green hour golden glow. 🌱📷\"\n3. \"Where ideas breathe and growth begins. #FGTime #CreatorVibes\""
                userPrompt.contains("hashtag", ignoreCase = true) || userPrompt.contains("tag", ignoreCase = true) ->
                    "Recommended high-reach tags for FG TIME:\n#FGTime #GreenCommunity #OrganicGrowth #CreativeLoft #SustainableVibes #EcoArt #DailyCreator"
                userPrompt.contains("muksidul", ignoreCase = true) || userPrompt.contains("founder", ignoreCase = true) ->
                    "Muksidul is the Creator and Founder of FG TIME! His vision is to give creators an authentic, positive social platform with built-in remix studios and calling tools."
                else ->
                    "Great creative concept! On FG TIME, posts with vibrant visuals and storytelling captions get 2.4x more engagement. Would you like me to draft a caption or suggest a music soundtrack for this?"
            }
            val aiMsg = AIChatMessage("ai_${System.currentTimeMillis()}", response, false, "Just now")
            _uiState.update {
                it.copy(
                    aiMessages = it.aiMessages + aiMsg,
                    isAILoading = false
                )
            }
        }
    }

    // ADMIN MODERATION LOGIC
    fun resolveReport(reportId: String) {
        _uiState.update { state ->
            val updated = state.adminReports.map {
                if (it.id == reportId) it.copy(status = "RESOLVED") else it
            }
            state.copy(adminReports = updated, snackbarMessage = "Report marked as RESOLVED by admin 🛡️")
        }
    }

    fun dismissReport(reportId: String) {
        _uiState.update { state ->
            val updated = state.adminReports.map {
                if (it.id == reportId) it.copy(status = "DISMISSED") else it
            }
            state.copy(adminReports = updated, snackbarMessage = "Report dismissed 🛡️")
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
