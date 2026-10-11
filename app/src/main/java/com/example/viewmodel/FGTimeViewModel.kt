package com.example.viewmodel

import android.app.Application
import androidx.annotation.DrawableRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.FGTimeRepository
import com.example.data.SecurityUtils
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
        id = "u_owner_muksidul",
        name = "Muksidul",
        username = "muksidul",
        contact = "+919876543210",
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
    ),

    // Real OTP & Authentication State
    val activeOtpNotification: String? = null,
    val otpTargetPhone: String = "",
    val otpPurpose: String = "SIGNUP",
    val isOtpVerified: Boolean = false,
    val isOtpLoading: Boolean = false,
    val otpError: String? = null,

    // Active Chat State
    val activeChatId: String = "dm_maya",
    val activeChatTitle: String = "Maya Green 🌿",
    val activeChatAvatar: Int = R.drawable.fgtime_creator,
    val activeChatIsOnline: Boolean = true,
    val activeChatRecipientId: String = "u_maya",
    val activeChatIsGroup: Boolean = false,

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

class FGTimeViewModel(application: Application) : AndroidViewModel(application) {

    val repository = FGTimeRepository(application)

    private val _uiState = MutableStateFlow(FGTimeUiState())
    val uiState: StateFlow<FGTimeUiState> = _uiState.asStateFlow()

    private var callTimerJob: Job? = null

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // Load persistent posts from SQLite
            val postsFromDb = repository.getAllPosts(_uiState.value.currentUser.id)
            val groupsFromDb = repository.getGroups()
            val initialDmMessages = repository.getMessagesForChat("dm_maya", _uiState.value.currentUser.id)
            val notificationsFromDb = repository.getNotifications(_uiState.value.currentUser.id)

            val userMuksidul = UserProfile(
                id = "u_owner_muksidul",
                name = "Muksidul",
                username = "muksidul",
                contact = "+919876543210",
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
                id = "u_maya",
                name = "Maya Green",
                username = "maya_creative",
                contact = "+919876543211",
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
                id = "u_rohan",
                name = "Rohan Verma",
                username = "rohan_sound",
                contact = "+919876543212",
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
                contact = "+919876543213",
                bio = "Urban greenhouse enthusiast 🌱 Green vibes only!",
                avatarResId = R.drawable.fgtime_banner,
                followers = 630,
                following = 150,
                postsCount = 9,
                isFollowing = true,
                isOnline = true
            )

            val initialStories = listOf(
                StoryItem("s_owner", userMuksidul, R.drawable.fgtime_banner, "Welcome to FG TIME 🌿 Real OTP & Persistent Database active!", "1h ago", false),
                StoryItem("s1", userMaya, R.drawable.fgtime_creator, "Morning sunshine through the leaves ✨", "2h ago", false),
                StoryItem("s2", userLeo, R.drawable.fgtime_reel, "Remixing new organic soundscapes 🎵", "4h ago", false),
                StoryItem("s3", userSara, R.drawable.fgtime_creator, "New vertical garden sprouts!", "6h ago", false)
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

            val initialMusicTracks = listOf(
                MusicTrack("t1", "Emerald Pulse (Lo-Fi)", "FG Beats Official", 28, "Ambient Lo-Fi", 82),
                MusicTrack("t2", "Botanical Breeze", "Acoustic Greenery", 34, "Chill Acoustic", 95),
                MusicTrack("t3", "Organic Echoes", "DJ Leafy", 22, "Electronic Ambient", 110),
                MusicTrack("t4", "Morning Chlorophyll", "Symphony of Plants", 40, "Classical Crossover", 78)
            )

            _uiState.update {
                it.copy(
                    posts = postsFromDb,
                    stories = initialStories,
                    reels = initialReels,
                    groupChats = groupsFromDb,
                    chatMessages = initialDmMessages,
                    notifications = notificationsFromDb,
                    remixTracks = initialMusicTracks,
                    searchUsers = listOf(userMuksidul, userMaya, userLeo, userSara)
                )
            }
        }
    }

    // ==========================================
    // AUTHENTICATION & OTP METHODS
    // ==========================================

    fun requestSignupOtp(phone: String, onCodeReady: ((String) -> Unit)? = null) {
        if (!SecurityUtils.isValidPhone(phone)) {
            _uiState.update { it.copy(otpError = "Please enter a valid phone number with at least 10 digits.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isOtpLoading = true, otpError = null) }
            val (success, result) = repository.requestOtp(phone, "SIGNUP")
            if (success) {
                val notificationText = "🌿 FG TIME SMS VERIFICATION\nYour OTP Code: $result\nValid for 5 minutes. Never share this code."
                _uiState.update {
                    it.copy(
                        isOtpLoading = false,
                        activeOtpNotification = notificationText,
                        otpTargetPhone = SecurityUtils.normalizePhone(phone),
                        otpPurpose = "SIGNUP",
                        isOtpVerified = false,
                        snackbarMessage = "SMS OTP sent to $phone 📲"
                    )
                }
                onCodeReady?.invoke(result)
            } else {
                _uiState.update {
                    it.copy(isOtpLoading = false, otpError = result)
                }
            }
        }
    }

    fun verifySignupOtp(enteredCode: String, onVerified: (() -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOtpLoading = true, otpError = null) }
            val phone = _uiState.value.otpTargetPhone
            val isMatch = repository.verifyOtp(phone, enteredCode, "SIGNUP")
            if (isMatch) {
                _uiState.update {
                    it.copy(
                        isOtpLoading = false,
                        isOtpVerified = true,
                        activeOtpNotification = null,
                        snackbarMessage = "Phone verified successfully! ✅ Set your password to complete."
                    )
                }
                onVerified?.invoke()
            } else {
                _uiState.update {
                    it.copy(
                        isOtpLoading = false,
                        otpError = "Invalid or expired OTP. Please check the SMS banner and re-enter."
                    )
                }
            }
        }
    }

    fun completeSignup(
        name: String,
        username: String,
        phone: String,
        password: String,
        dob: String,
        avatarRes: Int
    ) {
        viewModelScope.launch {
            val result = repository.registerUser(
                name = name,
                usernameRaw = username,
                phoneRaw = phone,
                passwordRaw = password,
                dob = dob,
                avatarRes = avatarRes
            )
            result.onSuccess { newUser ->
                val posts = repository.getAllPosts(newUser.id)
                val groups = repository.getGroups()
                val messages = repository.getMessagesForChat("dm_maya", newUser.id)
                _uiState.update {
                    it.copy(
                        currentUser = newUser,
                        isLoggedIn = true,
                        currentScreen = Screen.FEED,
                        posts = posts,
                        groupChats = groups,
                        chatMessages = messages,
                        isOtpVerified = false,
                        activeOtpNotification = null,
                        snackbarMessage = "Welcome to FG TIME, ${newUser.name}! 🌿 Data saved securely in database."
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(otpError = err.message ?: "Account registration failed.")
                }
            }
        }
    }

    fun loginWithCredentials(
        identifier: String,
        password: String,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.loginUser(identifier, password)
            result.onSuccess { loggedInUser ->
                val posts = repository.getAllPosts(loggedInUser.id)
                val groups = repository.getGroups()
                val messages = repository.getMessagesForChat(_uiState.value.activeChatId, loggedInUser.id)
                val notifs = repository.getNotifications(loggedInUser.id)

                _uiState.update {
                    it.copy(
                        currentUser = loggedInUser,
                        isLoggedIn = true,
                        currentScreen = Screen.FEED,
                        posts = posts,
                        groupChats = groups,
                        chatMessages = messages,
                        notifications = notifs,
                        snackbarMessage = if (it.language == Language.HINDI) "FG TIME में आपका स्वागत है, ${loggedInUser.name}! 🌿" else "Welcome back, ${loggedInUser.name}! 🌿"
                    )
                }
            }.onFailure { err ->
                onError(err.message ?: "Login failed. Check your credentials.")
            }
        }
    }

    fun requestPasswordResetOtp(phone: String, onCodeReady: ((String) -> Unit)? = null) {
        if (!SecurityUtils.isValidPhone(phone)) {
            _uiState.update { it.copy(otpError = "Please enter a valid registered phone number.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isOtpLoading = true, otpError = null) }
            val (success, result) = repository.requestOtp(phone, "RESET_PASSWORD")
            if (success) {
                val notificationText = "🌿 FG TIME PASSWORD RESET\nVerification OTP: $result\nValid for 5 minutes. Enter this code to set your new password."
                _uiState.update {
                    it.copy(
                        isOtpLoading = false,
                        activeOtpNotification = notificationText,
                        otpTargetPhone = SecurityUtils.normalizePhone(phone),
                        otpPurpose = "RESET_PASSWORD",
                        snackbarMessage = "Password reset OTP sent to $phone 📲"
                    )
                }
                onCodeReady?.invoke(result)
            } else {
                _uiState.update {
                    it.copy(isOtpLoading = false, otpError = result)
                }
            }
        }
    }

    fun verifyAndResetPassword(
        phone: String,
        otpCode: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val success = repository.resetPasswordWithOtp(phone, otpCode, newPassword)
            if (success) {
                _uiState.update {
                    it.copy(
                        activeOtpNotification = null,
                        snackbarMessage = "Password successfully reset! 🔒 You can now log in."
                    )
                }
                onSuccess()
            } else {
                onError("Invalid or expired OTP. Password was not changed.")
            }
        }
    }

    fun changePasswordInSettings(
        oldPassword: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val success = repository.changePassword(_uiState.value.currentUser.id, oldPassword, newPassword)
            if (success) {
                _uiState.update { it.copy(snackbarMessage = "Password changed successfully! 🔐") }
                onSuccess()
            } else {
                onError("Current password incorrect. Please verify and try again.")
            }
        }
    }

    fun clearOtpNotification() {
        _uiState.update { it.copy(activeOtpNotification = null, otpError = null) }
    }

    fun logout() {
        _uiState.update {
            it.copy(
                isLoggedIn = false,
                currentScreen = Screen.WELCOME_AUTH,
                isOtpVerified = false,
                activeOtpNotification = null,
                snackbarMessage = if (it.language == Language.HINDI) "आप सफलतापूर्वक लॉग आउट हो चुके हैं" else "Logged out safely. Your database data is preserved."
            )
        }
    }

    // ==========================================
    // POSTS & FEED (CREATE, EDIT, DELETE, LIKE, SAVE)
    // ==========================================

    fun createNewPost(
        content: String,
        tagsStr: String,
        imageRes: Int,
        mediaType: String = "PHOTO",
        location: String = "FG TIME Loft"
    ) {
        viewModelScope.launch {
            val newPost = repository.createPost(
                currentUserId = _uiState.value.currentUser.id,
                author = _uiState.value.currentUser,
                content = content,
                imageRes = imageRes,
                mediaType = mediaType,
                tags = tagsStr,
                location = location
            )
            val updatedUser = _uiState.value.currentUser.copy(
                postsCount = _uiState.value.currentUser.postsCount + 1
            )
            _uiState.update {
                it.copy(
                    posts = listOf(newPost) + it.posts,
                    currentUser = updatedUser,
                    currentScreen = Screen.FEED,
                    snackbarMessage = "Post published to community feed! 🌿"
                )
            }
        }
    }

    fun editPost(postId: String, newContent: String) {
        viewModelScope.launch {
            val ok = repository.editPost(postId, newContent)
            if (ok) {
                _uiState.update { state ->
                    val updated = state.posts.map {
                        if (it.id == postId) it.copy(content = newContent) else it
                    }
                    state.copy(posts = updated, snackbarMessage = "Post updated in database! ✏️")
                }
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            val ok = repository.deletePost(postId, _uiState.value.currentUser.id)
            if (ok) {
                val updatedUser = _uiState.value.currentUser.copy(
                    postsCount = (_uiState.value.currentUser.postsCount - 1).coerceAtLeast(0)
                )
                _uiState.update { state ->
                    val updated = state.posts.filterNot { it.id == postId }
                    state.copy(
                        posts = updated,
                        currentUser = updatedUser,
                        snackbarMessage = "Post deleted from feed & database 🗑️"
                    )
                }
            }
        }
    }

    fun toggleLikePost(postId: String) {
        viewModelScope.launch {
            val (isLiked, newLikes) = repository.toggleLike(_uiState.value.currentUser.id, postId)
            _uiState.update { state ->
                val updatedPosts = state.posts.map { post ->
                    if (post.id == postId) {
                        post.copy(isLiked = isLiked, likesCount = newLikes)
                    } else post
                }
                state.copy(posts = updatedPosts)
            }
        }
    }

    fun toggleSavePost(postId: String) {
        viewModelScope.launch {
            val isSaved = repository.toggleSave(_uiState.value.currentUser.id, postId)
            _uiState.update { state ->
                val updatedPosts = state.posts.map { post ->
                    if (post.id == postId) post.copy(isSaved = isSaved) else post
                }
                state.copy(
                    posts = updatedPosts,
                    snackbarMessage = if (isSaved) "Saved to your Bookmarks 🔖" else "Removed from Bookmarks"
                )
            }
        }
    }

    fun loadComments(postId: String) {
        viewModelScope.launch {
            val comments = repository.getComments(postId)
            _uiState.update {
                it.copy(
                    commentsByPostId = it.commentsByPostId + (postId to comments),
                    activePostCommentsPostId = postId
                )
            }
        }
    }

    fun addCommentToPost(postId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val newComment = repository.addComment(postId, _uiState.value.currentUser, text)
            _uiState.update { state ->
                val currentList = state.commentsByPostId[postId] ?: emptyList()
                val updatedMap = state.commentsByPostId + (postId to (currentList + newComment))
                val updatedPosts = state.posts.map {
                    if (it.id == postId) it.copy(commentsCount = it.commentsCount + 1) else it
                }
                state.copy(commentsByPostId = updatedMap, posts = updatedPosts)
            }
        }
    }

    fun openStory(index: Int) {
        _uiState.update { it.copy(activeStoryIndex = index, currentScreen = Screen.STORY_VIEWER) }
    }

    fun openCommentsFor(postId: String) {
        loadComments(postId)
    }

    fun closeComments() {
        closeCommentsSheet()
    }

    fun addComment(postId: String, text: String) {
        addCommentToPost(postId, text)
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
                snackbarMessage = "Report submitted to FG TIME safety team 🛡️"
            )
        }
    }

    fun closeCommentsSheet() {
        _uiState.update { it.copy(activePostCommentsPostId = null) }
    }

    // ==========================================
    // MESSAGING & GROUPS
    // ==========================================

    fun selectChat(
        chatId: String,
        title: String,
        avatarRes: Int,
        isOnline: Boolean,
        recipientId: String,
        isGroup: Boolean = false
    ) {
        viewModelScope.launch {
            val messages = repository.getMessagesForChat(chatId, _uiState.value.currentUser.id)
            _uiState.update {
                it.copy(
                    activeChatId = chatId,
                    activeChatTitle = title,
                    activeChatAvatar = avatarRes,
                    activeChatIsOnline = isOnline,
                    activeChatRecipientId = recipientId,
                    activeChatIsGroup = isGroup,
                    chatMessages = messages
                )
            }
        }
    }

    fun sendChatMessage(text: String, isVoiceNote: Boolean = false, voiceDuration: Int = 0) {
        if (text.isBlank() && !isVoiceNote) return
        val currentChatId = _uiState.value.activeChatId
        val sender = _uiState.value.currentUser
        val recipientId = _uiState.value.activeChatRecipientId

        viewModelScope.launch {
            val sentMsg = repository.sendMessage(
                chatId = currentChatId,
                sender = sender,
                receiverId = recipientId,
                text = text,
                isVoiceNote = isVoiceNote,
                voiceDuration = voiceDuration
            )
            _uiState.update {
                it.copy(chatMessages = it.chatMessages + sentMsg)
            }

            // Auto-reply simulation for contacts if 1-on-1
            if (!currentChatId.startsWith("grp_") && recipientId != "u_owner_muksidul") {
                delay(1200)
                val replyText = when {
                    text.contains("hello", ignoreCase = true) || text.contains("hey", ignoreCase = true) ->
                        "Hey ${sender.name}! Loving the new FG TIME updates 🌿"
                    text.contains("video", ignoreCase = true) || text.contains("remix", ignoreCase = true) ->
                        "The video trimming and remix tools in the studio work really well!"
                    isVoiceNote -> "Received your voice note! Loud and clear 🎧"
                    else -> "Thanks for reaching out! Spreading positive green vibes 🌱✨"
                }
                val autoMsg = repository.sendMessage(
                    chatId = currentChatId,
                    sender = UserProfile(id = recipientId, name = _uiState.value.activeChatTitle, username = "contact", avatarResId = _uiState.value.activeChatAvatar),
                    receiverId = sender.id,
                    text = replyText
                )
                if (_uiState.value.activeChatId == currentChatId) {
                    _uiState.update { it.copy(chatMessages = it.chatMessages + autoMsg) }
                }
            }
        }
    }

    fun createGroupChat(name: String, initialMembers: Int = 3) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val newGrp = repository.createGroup(name, _uiState.value.currentUser, initialMembers)
            val allGroups = repository.getGroups()
            _uiState.update {
                it.copy(
                    groupChats = allGroups,
                    snackbarMessage = "Group '${newGrp.name}' created! 🌿"
                )
            }
            selectChat(
                chatId = newGrp.id,
                title = newGrp.name,
                avatarRes = newGrp.iconResId,
                isOnline = true,
                recipientId = newGrp.id,
                isGroup = true
            )
        }
    }

    // ==========================================
    // SOCIAL & PROFILE
    // ==========================================

    fun toggleFollowUser(targetUserId: String) {
        viewModelScope.launch {
            val isNowFollowing = repository.toggleFollow(_uiState.value.currentUser.id, targetUserId)
            val updatedUsers = _uiState.value.searchUsers.map {
                if (it.id == targetUserId) {
                    it.copy(
                        isFollowing = isNowFollowing,
                        followers = if (isNowFollowing) it.followers + 1 else (it.followers - 1).coerceAtLeast(0)
                    )
                } else it
            }
            val updatedMe = _uiState.value.currentUser.copy(
                following = if (isNowFollowing) _uiState.value.currentUser.following + 1 else (_uiState.value.currentUser.following - 1).coerceAtLeast(0)
            )
            _uiState.update {
                it.copy(
                    searchUsers = updatedUsers,
                    currentUser = updatedMe,
                    snackbarMessage = if (isNowFollowing) "Following user 🌿" else "Unfollowed user"
                )
            }
        }
    }

    fun updateProfile(name: String, bio: String, avatarRes: Int, isPrivate: Boolean) {
        viewModelScope.launch {
            val ok = repository.updateUserProfile(_uiState.value.currentUser.id, name, bio, avatarRes, isPrivate)
            if (ok) {
                val updated = _uiState.value.currentUser.copy(
                    name = name.trim(),
                    bio = bio.trim(),
                    avatarResId = avatarRes,
                    isPrivate = isPrivate
                )
                _uiState.update {
                    it.copy(
                        currentUser = updated,
                        snackbarMessage = "Profile updated in database! ✨"
                    )
                }
            }
        }
    }

    fun searchUsersAndPosts(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        viewModelScope.launch {
            val results = if (query.isBlank()) {
                _uiState.value.searchUsers
            } else {
                repository.searchUsers(query, _uiState.value.currentUser.id)
            }
            _uiState.update { it.copy(searchUsers = results) }
        }
    }

    fun setSearchFilter(filter: String) {
        _uiState.update { it.copy(activeSearchFilter = filter) }
    }

    // ==========================================
    // REMIX STUDIO & VIDEO EDITING
    // ==========================================

    fun selectRemixTrack(trackId: String) {
        _uiState.update { it.copy(selectedRemixTrackId = trackId) }
    }

    fun toggleStudioPlayback() {
        _uiState.update { it.copy(isStudioPlaying = !it.isStudioPlaying) }
    }

    fun setStudioTrim(startSec: Float, endSec: Float) {
        _uiState.update { it.copy(studioTrimStartSec = startSec, studioTrimEndSec = endSec) }
    }

    fun setStudioVolume(volume: Float) {
        _uiState.update { it.copy(studioVolume = volume.coerceIn(0f, 1f)) }
    }

    fun exportRemixVideoAndPublish(caption: String, videoRes: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(studioExportProgress = 10) }
            delay(500)
            _uiState.update { it.copy(studioExportProgress = 50) }
            delay(500)
            _uiState.update { it.copy(studioExportProgress = 100) }
            delay(300)

            val track = _uiState.value.remixTracks.find { it.id == _uiState.value.selectedRemixTrackId }
            val tags = "#FGTime #RemixStudio #${track?.genre?.replace(" ", "") ?: "Music"}"
            createNewPost(
                content = if (caption.isNotBlank()) caption else "New video remix created in FG Remix Studio 🎬🎵 (${track?.title})",
                tagsStr = tags,
                imageRes = videoRes,
                mediaType = "VIDEO",
                location = "FG Remix Studio"
            )
            _uiState.update {
                it.copy(
                    studioExportProgress = null,
                    currentScreen = Screen.FEED,
                    snackbarMessage = "Video remix published to feed! 🎬🌿"
                )
            }
        }
    }

    fun exportRemix() {
        viewModelScope.launch {
            _uiState.update { it.copy(studioExportProgress = 15) }
            delay(400)
            _uiState.update { it.copy(studioExportProgress = 60) }
            delay(500)
            _uiState.update { it.copy(studioExportProgress = 100, studioExportSuccessMessage = "Video rendered with selected audio track! You can post it to Feed or Reels.") }
        }
    }

    fun clearStudioExportModal() {
        _uiState.update { it.copy(studioExportProgress = null, studioExportSuccessMessage = null) }
    }

    // ==========================================
    // CALL SIMULATION, AI, NAVIGATION & THEME
    // ==========================================

    fun startCall(contactName: String, contactAvatarRes: Int, type: CallType) {
        initiateCall(contactName, contactAvatarRes, type)
    }

    fun initiateCall(contactName: String, contactAvatarRes: Int, type: CallType) {
        val record = CallRecord(
            id = "call_${System.currentTimeMillis()}",
            contactName = contactName,
            contactAvatarResId = contactAvatarRes,
            type = type,
            isIncoming = false,
            timestamp = "Just now"
        )
        _uiState.update {
            it.copy(
                activeCall = record,
                callState = CallState.OUTGOING,
                currentScreen = Screen.CALL_SCREEN,
                callDurationSeconds = 0
            )
        }
        viewModelScope.launch {
            delay(2500)
            if (_uiState.value.callState == CallState.OUTGOING) {
                _uiState.update { it.copy(callState = CallState.CONNECTED) }
                startCallTimer()
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

    fun endCall() {
        callTimerJob?.cancel()
        val duration = _uiState.value.callDurationSeconds
        val active = _uiState.value.activeCall
        if (active != null) {
            val record = active.copy(durationSec = duration)
            _uiState.update {
                it.copy(
                    callHistory = listOf(record) + it.callHistory,
                    callState = CallState.ENDED,
                    activeCall = null
                )
            }
        }
        _uiState.update { it.copy(currentScreen = Screen.DIRECT_MESSAGES) }
    }

    fun toggleCallMute() {
        _uiState.update { it.copy(isCallMuted = !it.isCallMuted) }
    }

    fun toggleCallCamera() {
        _uiState.update { it.copy(isCallCameraEnabled = !it.isCallCameraEnabled) }
    }

    fun switchCallCamera() {
        _uiState.update { it.copy(isCallFrontCamera = !it.isCallFrontCamera) }
    }

    fun sendAIMessage(prompt: String) {
        if (prompt.isBlank()) return
        val userMsg = AIChatMessage("ai_u_${System.currentTimeMillis()}", prompt, true, "Just now")
        _uiState.update { it.copy(aiMessages = it.aiMessages + userMsg, isAILoading = true) }

        viewModelScope.launch {
            delay(1000)
            val responseText = when {
                prompt.contains("caption", ignoreCase = true) ->
                    "Here's a poetic caption for FG TIME: 'Finding stillness in the green, growing at the pace of nature. 🌿✨ #FGTime #GreenLiving'"
                prompt.contains("hashtag", ignoreCase = true) ->
                    "#FGTime #GreenGrowth #EcoCreators #PeacefulLiving #CommunityVibes #CreativeSoul"
                prompt.contains("remix", ignoreCase = true) ->
                    "In the Remix Studio, combine the 82 BPM Lo-Fi stems with your nature videos for max aesthetic engagement!"
                else -> "Great prompt! FG TIME is all about mindful creation. Try using the green leaf filter with your latest clip."
            }
            val aiMsg = AIChatMessage("ai_b_${System.currentTimeMillis()}", responseText, false, "Just now")
            _uiState.update { it.copy(aiMessages = it.aiMessages + aiMsg, isAILoading = false) }
        }
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

    // Admin report actions
    fun dismissReport(id: String) {
        _uiState.update { state ->
            state.copy(adminReports = state.adminReports.map { if (it.id == id) it.copy(status = "DISMISSED") else it })
        }
    }

    fun resolveReport(id: String) {
        _uiState.update { state ->
            state.copy(adminReports = state.adminReports.map { if (it.id == id) it.copy(status = "RESOLVED") else it })
        }
    }

    // Call Screen actions
    fun acceptIncomingCall() {
        _uiState.update { it.copy(callState = CallState.CONNECTED) }
        startCallTimer()
    }

    fun flipCallCamera() {
        switchCallCamera()
    }

    // Explore / Search action
    fun updateSearchQuery(query: String) {
        searchUsersAndPosts(query)
    }

    // Profile performLogin action
    fun performLogin(name: String, username: String, contact: String, dob: String = "2001-08-15") {
        loginWithCredentials(username.ifBlank { contact }, "FGTime@123") {
            completeSignup(name, username, contact, "FGTime@123", dob, R.drawable.fgtime_creator)
        }
    }

    // Reels actions
    fun toggleLikeReel(reelId: String) {
        _uiState.update { state ->
            val updated = state.reels.map { reel ->
                if (reel.id == reelId) {
                    val liked = !reel.isLiked
                    reel.copy(isLiked = liked, likesCount = if (liked) reel.likesCount + 1 else reel.likesCount - 1)
                } else reel
            }
            state.copy(reels = updated)
        }
    }

    fun toggleSaveReel(reelId: String) {
        _uiState.update { state ->
            val updated = state.reels.map { reel ->
                if (reel.id == reelId) reel.copy(isSaved = !reel.isSaved) else reel
            }
            state.copy(reels = updated)
        }
    }

    // Remix Studio action
    fun updateStudioTrim(start: Float, end: Float) {
        setStudioTrim(start, end)
    }

    // Story Viewer actions
    fun nextStory() {
        val next = _uiState.value.activeStoryIndex + 1
        if (next < _uiState.value.stories.size) {
            _uiState.update { it.copy(activeStoryIndex = next) }
        } else {
            _uiState.update { it.copy(currentScreen = Screen.FEED) }
        }
    }

    fun prevStory() {
        val prev = _uiState.value.activeStoryIndex - 1
        if (prev >= 0) {
            _uiState.update { it.copy(activeStoryIndex = prev) }
        }
    }

    fun toggleDarkMode() {
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    fun toggleLanguage() {
        _uiState.update {
            it.copy(language = if (it.language == Language.ENGLISH) Language.HINDI else Language.ENGLISH)
        }
    }

    fun navigateTo(screen: Screen) {
        _uiState.update {
            it.copy(previousScreen = it.currentScreen, currentScreen = screen)
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
