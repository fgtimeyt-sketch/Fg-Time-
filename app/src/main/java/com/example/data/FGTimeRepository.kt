package com.example.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.R
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class FGTimeRepository(context: Context) {

    private val dbHelper = FGTimeDatabaseHelper.getInstance(context)

    // ==========================================
    // 1. AUTHENTICATION & OTP OPERATIONS
    // ==========================================

    suspend fun requestOtp(phoneRaw: String, purpose: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val phone = SecurityUtils.normalizePhone(phoneRaw)
        if (!SecurityUtils.isValidPhone(phone)) {
            return@withContext Pair(false, "Invalid phone number format. Enter at least 10 digits.")
        }

        val otp = SecurityUtils.generateOtp()
        val now = System.currentTimeMillis()
        val expiresAt = now + 5 * 60 * 1000 // 5 minutes validity

        val db = dbHelper.writableDatabase
        // Invalidate prior OTPs for this phone and purpose
        db.delete(
            FGTimeDatabaseHelper.TABLE_OTP_SESSIONS,
            "phone = ? AND purpose = ?",
            arrayOf(phone, purpose)
        )

        val cv = ContentValues().apply {
            put("id", UUID.randomUUID().toString())
            put("phone", phone)
            put("otp_code", otp)
            put("purpose", purpose)
            put("expires_at", expiresAt)
            put("is_verified", 0)
            put("created_at", now)
        }
        val inserted = db.insert(FGTimeDatabaseHelper.TABLE_OTP_SESSIONS, null, cv)
        if (inserted != -1L) {
            Pair(true, otp)
        } else {
            Pair(false, "Failed to generate OTP. Please try again.")
        }
    }

    suspend fun verifyOtp(phoneRaw: String, codeEntered: String, purpose: String): Boolean = withContext(Dispatchers.IO) {
        val phone = SecurityUtils.normalizePhone(phoneRaw)
        val cleanCode = codeEntered.trim()
        val now = System.currentTimeMillis()

        val db = dbHelper.readableDatabase
        val cursor = db.query(
            FGTimeDatabaseHelper.TABLE_OTP_SESSIONS,
            arrayOf("id", "otp_code", "expires_at"),
            "phone = ? AND purpose = ? AND is_verified = 0 AND expires_at > ?",
            arrayOf(phone, purpose, now.toString()),
            null, null, "created_at DESC", "1"
        )

        var isValid = false
        cursor.use {
            if (it.moveToFirst()) {
                val realCode = it.getString(it.getColumnIndexOrThrow("otp_code"))
                if (SecurityUtils.secureCodeEquals(realCode, cleanCode)) {
                    isValid = true
                    val sessionId = it.getString(it.getColumnIndexOrThrow("id"))
                    val wDb = dbHelper.writableDatabase
                    val cv = ContentValues().apply { put("is_verified", 1) }
                    wDb.update(FGTimeDatabaseHelper.TABLE_OTP_SESSIONS, cv, "id = ?", arrayOf(sessionId))
                }
            }
        }
        isValid
    }

    suspend fun registerUser(
        name: String,
        usernameRaw: String,
        phoneRaw: String,
        passwordRaw: String,
        dob: String,
        avatarRes: Int
    ): Result<UserProfile> = withContext(Dispatchers.IO) {
        val username = usernameRaw.trim().lowercase().removePrefix("@")
        val phone = SecurityUtils.normalizePhone(phoneRaw)
        val passwordHash = SecurityUtils.hashPassword(passwordRaw)
        val now = System.currentTimeMillis()

        val db = dbHelper.writableDatabase

        // Check uniqueness of username and phone
        val checkCursor = db.query(
            FGTimeDatabaseHelper.TABLE_USERS,
            arrayOf("id"),
            "username = ? OR phone = ?",
            arrayOf(username, phone),
            null, null, null
        )
        val exists = checkCursor.use { it.count > 0 }
        if (exists) {
            return@withContext Result.failure(Exception("Username or Mobile Number already registered!"))
        }

        val userId = "u_${UUID.randomUUID().toString().take(8)}"
        val isOwner = username.equals("muksidul", ignoreCase = true)

        val cv = ContentValues().apply {
            put("id", userId)
            put("name", name.trim())
            put("username", username)
            put("phone", phone)
            put("password_hash", passwordHash)
            put("bio", "Spreading positive green vibes on FG TIME 🌿")
            put("avatar_res", avatarRes)
            put("followers", 0)
            put("following", 1) // default following founder
            put("posts_count", 0)
            put("is_private", 0)
            put("is_verified", if (isOwner) 1 else 0)
            put("is_owner", if (isOwner) 1 else 0)
            put("date_of_birth", dob)
            put("created_at", now)
        }

        val rowId = db.insert(FGTimeDatabaseHelper.TABLE_USERS, null, cv)
        if (rowId != -1L) {
            // Auto follow owner Muksidul for welcome feed
            val cvFollow = ContentValues().apply {
                put("follower_id", userId)
                put("following_id", "u_owner_muksidul")
            }
            db.insert(FGTimeDatabaseHelper.TABLE_FOLLOWS, null, cvFollow)

            val profile = UserProfile(
                id = userId,
                name = name.trim(),
                username = username,
                contact = phone,
                bio = "Spreading positive green vibes on FG TIME 🌿",
                avatarResId = avatarRes,
                followers = 0,
                following = 1,
                postsCount = 0,
                isPrivate = false,
                isOwner = isOwner,
                isVerified = isOwner,
                dateOfBirth = dob
            )
            Result.success(profile)
        } else {
            Result.failure(Exception("Failed to save account into database."))
        }
    }

    suspend fun loginUser(identifierRaw: String, passwordRaw: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        val identifier = identifierRaw.trim()
        val normalizedPhone = SecurityUtils.normalizePhone(identifier)
        val passwordHash = SecurityUtils.hashPassword(passwordRaw)

        val db = dbHelper.readableDatabase
        val cursor = db.query(
            FGTimeDatabaseHelper.TABLE_USERS,
            null,
            "(username = ? OR phone = ? OR phone = ?) AND password_hash = ?",
            arrayOf(identifier.lowercase().removePrefix("@"), identifier, normalizedPhone, passwordHash),
            null, null, null
        )

        cursor.use {
            if (it.moveToFirst()) {
                val user = mapUserFromCursor(it)
                Result.success(user)
            } else {
                Result.failure(Exception("Incorrect username/phone or password. Please try again."))
            }
        }
    }

    suspend fun resetPasswordWithOtp(phoneRaw: String, otpCode: String, newPasswordRaw: String): Boolean = withContext(Dispatchers.IO) {
        val phone = SecurityUtils.normalizePhone(phoneRaw)
        val verified = verifyOtp(phone, otpCode, "RESET_PASSWORD")
        if (!verified) return@withContext false

        val newHash = SecurityUtils.hashPassword(newPasswordRaw)
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply { put("password_hash", newHash) }
        val updated = db.update(FGTimeDatabaseHelper.TABLE_USERS, cv, "phone = ?", arrayOf(phone))
        updated > 0
    }

    suspend fun changePassword(userId: String, oldPasswordRaw: String, newPasswordRaw: String): Boolean = withContext(Dispatchers.IO) {
        val oldHash = SecurityUtils.hashPassword(oldPasswordRaw)
        val newHash = SecurityUtils.hashPassword(newPasswordRaw)
        val db = dbHelper.writableDatabase

        val cursor = db.query(
            FGTimeDatabaseHelper.TABLE_USERS,
            arrayOf("id"),
            "id = ? AND password_hash = ?",
            arrayOf(userId, oldHash),
            null, null, null
        )
        val matches = cursor.use { it.count > 0 }
        if (!matches) return@withContext false

        val cv = ContentValues().apply { put("password_hash", newHash) }
        val rows = db.update(FGTimeDatabaseHelper.TABLE_USERS, cv, "id = ?", arrayOf(userId))
        rows > 0
    }

    // ==========================================
    // 2. USER PROFILE & SOCIAL OPERATIONS
    // ==========================================

    suspend fun getUserById(userId: String): UserProfile? = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            FGTimeDatabaseHelper.TABLE_USERS,
            null,
            "id = ?",
            arrayOf(userId),
            null, null, null
        )
        cursor.use {
            if (it.moveToFirst()) mapUserFromCursor(it) else null
        }
    }

    suspend fun updateUserProfile(
        userId: String,
        name: String,
        bio: String,
        avatarRes: Int,
        isPrivate: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("name", name.trim())
            put("bio", bio.trim())
            put("avatar_res", avatarRes)
            put("is_private", if (isPrivate) 1 else 0)
        }
        val rows = db.update(FGTimeDatabaseHelper.TABLE_USERS, cv, "id = ?", arrayOf(userId))
        rows > 0
    }

    suspend fun searchUsers(query: String, currentUserId: String): List<UserProfile> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val q = "%${query.trim().lowercase()}%"
        val cursor = db.query(
            FGTimeDatabaseHelper.TABLE_USERS,
            null,
            "LOWER(username) LIKE ? OR LOWER(name) LIKE ?",
            arrayOf(q, q),
            null, null, "followers DESC", "20"
        )
        val list = mutableListOf<UserProfile>()
        cursor.use {
            while (it.moveToNext()) {
                val user = mapUserFromCursor(it)
                if (user.id != currentUserId) {
                    val isF = isFollowing(currentUserId, user.id)
                    list.add(user.copy(isFollowing = isF))
                }
            }
        }
        list
    }

    suspend fun toggleFollow(followerId: String, followingId: String): Boolean = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val check = db.query(
            FGTimeDatabaseHelper.TABLE_FOLLOWS,
            arrayOf("follower_id"),
            "follower_id = ? AND following_id = ?",
            arrayOf(followerId, followingId),
            null, null, null
        )
        val alreadyFollowing = check.use { it.count > 0 }
        if (alreadyFollowing) {
            db.delete(
                FGTimeDatabaseHelper.TABLE_FOLLOWS,
                "follower_id = ? AND following_id = ?",
                arrayOf(followerId, followingId)
            )
            db.execSQL("UPDATE ${FGTimeDatabaseHelper.TABLE_USERS} SET followers = MAX(0, followers - 1) WHERE id = ?", arrayOf(followingId))
            db.execSQL("UPDATE ${FGTimeDatabaseHelper.TABLE_USERS} SET following = MAX(0, following - 1) WHERE id = ?", arrayOf(followerId))
            false
        } else {
            val cv = ContentValues().apply {
                put("follower_id", followerId)
                put("following_id", followingId)
            }
            db.insert(FGTimeDatabaseHelper.TABLE_FOLLOWS, null, cv)
            db.execSQL("UPDATE ${FGTimeDatabaseHelper.TABLE_USERS} SET followers = followers + 1 WHERE id = ?", arrayOf(followingId))
            db.execSQL("UPDATE ${FGTimeDatabaseHelper.TABLE_USERS} SET following = following + 1 WHERE id = ?", arrayOf(followerId))
            true
        }
    }

    suspend fun isFollowing(followerId: String, followingId: String): Boolean = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val c = db.query(
            FGTimeDatabaseHelper.TABLE_FOLLOWS,
            arrayOf("follower_id"),
            "follower_id = ? AND following_id = ?",
            arrayOf(followerId, followingId),
            null, null, null
        )
        c.use { it.count > 0 }
    }

    // ==========================================
    // 3. POSTS, LIKES, SAVED & COMMENTS
    // ==========================================

    suspend fun getAllPosts(currentUserId: String): List<Post> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            FGTimeDatabaseHelper.TABLE_POSTS,
            null,
            null, null, null, null,
            "created_at DESC"
        )
        val posts = mutableListOf<Post>()
        cursor.use {
            while (it.moveToNext()) {
                val postId = it.getString(it.getColumnIndexOrThrow("id"))
                val userId = it.getString(it.getColumnIndexOrThrow("user_id"))
                val authorName = it.getString(it.getColumnIndexOrThrow("author_name"))
                val authorUsername = it.getString(it.getColumnIndexOrThrow("author_username"))
                val authorAvatarRes = it.getInt(it.getColumnIndexOrThrow("author_avatar_res")).let { res ->
                    if (res == 0) R.drawable.fgtime_leaf_logo else res
                }
                val content = it.getString(it.getColumnIndexOrThrow("content"))
                val imageRes = it.getInt(it.getColumnIndexOrThrow("image_res")).let { res ->
                    if (res == 0) R.drawable.fgtime_banner else res
                }
                val tagsStr = it.getString(it.getColumnIndexOrThrow("tags")) ?: ""
                val location = it.getString(it.getColumnIndexOrThrow("location")) ?: "FG Green Loft"
                val mediaUri = it.getString(it.getColumnIndexOrThrow("media_uri"))
                val mediaType = it.getString(it.getColumnIndexOrThrow("media_type")) ?: "PHOTO"
                val likesCount = it.getInt(it.getColumnIndexOrThrow("likes_count"))
                val commentsCount = it.getInt(it.getColumnIndexOrThrow("comments_count"))
                val createdAt = it.getLong(it.getColumnIndexOrThrow("created_at"))

                // Check if liked and saved by current user
                val isLiked = checkItemExists(db, FGTimeDatabaseHelper.TABLE_LIKES, currentUserId, postId)
                val isSaved = checkItemExists(db, FGTimeDatabaseHelper.TABLE_SAVED, currentUserId, postId)

                val author = UserProfile(
                    id = userId,
                    name = authorName,
                    username = authorUsername,
                    avatarResId = authorAvatarRes
                )

                val timeAgoStr = formatTimeAgo(createdAt)

                posts.add(
                    Post(
                        id = postId,
                        author = author,
                        content = content,
                        imageResId = imageRes,
                        mediaUri = mediaUri,
                        mediaType = mediaType,
                        tags = if (tagsStr.isNotBlank()) tagsStr.split(" ") else emptyList(),
                        likesCount = likesCount,
                        commentsCount = commentsCount,
                        isLiked = isLiked,
                        isSaved = isSaved,
                        timeAgo = timeAgoStr,
                        location = location
                    )
                )
            }
        }
        posts
    }

    suspend fun createPost(
        currentUserId: String,
        author: UserProfile,
        content: String,
        imageRes: Int,
        mediaUri: String? = null,
        mediaType: String = "PHOTO",
        tags: String,
        location: String
    ): Post = withContext(Dispatchers.IO) {
        val postId = "p_${UUID.randomUUID().toString().take(8)}"
        val now = System.currentTimeMillis()
        val db = dbHelper.writableDatabase

        val cv = ContentValues().apply {
            put("id", postId)
            put("user_id", currentUserId)
            put("author_name", author.name)
            put("author_username", author.username)
            put("author_avatar_res", author.avatarResId)
            put("content", content.trim())
            put("image_res", imageRes)
            put("media_uri", mediaUri)
            put("media_type", mediaType)
            put("tags", tags.trim())
            put("location", location.trim())
            put("likes_count", 0)
            put("comments_count", 0)
            put("created_at", now)
        }
        db.insert(FGTimeDatabaseHelper.TABLE_POSTS, null, cv)
        db.execSQL("UPDATE ${FGTimeDatabaseHelper.TABLE_USERS} SET posts_count = posts_count + 1 WHERE id = ?", arrayOf(currentUserId))

        Post(
            id = postId,
            author = author,
            content = content,
            imageResId = imageRes,
            mediaUri = mediaUri,
            mediaType = mediaType,
            tags = if (tags.isNotBlank()) tags.split(" ") else emptyList(),
            likesCount = 0,
            commentsCount = 0,
            isLiked = false,
            isSaved = false,
            timeAgo = "Just now",
            location = location
        )
    }

    suspend fun editPost(postId: String, userId: String, newContent: String): Boolean = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply { put("content", newContent.trim()) }
        val rows = db.update(FGTimeDatabaseHelper.TABLE_POSTS, cv, "id = ? AND user_id = ?", arrayOf(postId, userId))
        rows > 0
    }

    suspend fun deletePost(postId: String, userId: String): Boolean = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val deleted = db.delete(FGTimeDatabaseHelper.TABLE_POSTS, "id = ? AND user_id = ?", arrayOf(postId, userId))
        if (deleted > 0) {
            db.delete(FGTimeDatabaseHelper.TABLE_LIKES, "post_id = ?", arrayOf(postId))
            db.delete(FGTimeDatabaseHelper.TABLE_SAVED, "post_id = ?", arrayOf(postId))
            db.delete(FGTimeDatabaseHelper.TABLE_COMMENTS, "post_id = ?", arrayOf(postId))
            db.execSQL("UPDATE ${FGTimeDatabaseHelper.TABLE_USERS} SET posts_count = MAX(0, posts_count - 1) WHERE id = ?", arrayOf(userId))
            true
        } else false
    }

    suspend fun toggleLike(userId: String, postId: String): Pair<Boolean, Int> = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val isLiked = checkItemExists(db, FGTimeDatabaseHelper.TABLE_LIKES, userId, postId)
        if (isLiked) {
            db.delete(FGTimeDatabaseHelper.TABLE_LIKES, "user_id = ? AND post_id = ?", arrayOf(userId, postId))
            db.execSQL("UPDATE ${FGTimeDatabaseHelper.TABLE_POSTS} SET likes_count = MAX(0, likes_count - 1) WHERE id = ?", arrayOf(postId))
            val newCount = getPostLikesCount(db, postId)
            Pair(false, newCount)
        } else {
            val cv = ContentValues().apply {
                put("user_id", userId)
                put("post_id", postId)
            }
            db.insert(FGTimeDatabaseHelper.TABLE_LIKES, null, cv)
            db.execSQL("UPDATE ${FGTimeDatabaseHelper.TABLE_POSTS} SET likes_count = likes_count + 1 WHERE id = ?", arrayOf(postId))
            val newCount = getPostLikesCount(db, postId)
            Pair(true, newCount)
        }
    }

    suspend fun toggleSave(userId: String, postId: String): Boolean = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val isSaved = checkItemExists(db, FGTimeDatabaseHelper.TABLE_SAVED, userId, postId)
        if (isSaved) {
            db.delete(FGTimeDatabaseHelper.TABLE_SAVED, "user_id = ? AND post_id = ?", arrayOf(userId, postId))
            false
        } else {
            val cv = ContentValues().apply {
                put("user_id", userId)
                put("post_id", postId)
            }
            db.insert(FGTimeDatabaseHelper.TABLE_SAVED, null, cv)
            true
        }
    }

    suspend fun getComments(postId: String): List<PostComment> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            FGTimeDatabaseHelper.TABLE_COMMENTS,
            null,
            "post_id = ?",
            arrayOf(postId),
            null, null, "created_at ASC"
        )
        val list = mutableListOf<PostComment>()
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    PostComment(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        postId = postId,
                        authorName = it.getString(it.getColumnIndexOrThrow("author_name")),
                        authorUsername = it.getString(it.getColumnIndexOrThrow("author_username")),
                        authorAvatarResId = it.getInt(it.getColumnIndexOrThrow("author_avatar_res")).let { r -> if (r == 0) R.drawable.fgtime_leaf_logo else r },
                        commentText = it.getString(it.getColumnIndexOrThrow("comment_text")),
                        timeAgo = it.getString(it.getColumnIndexOrThrow("time_ago")) ?: "Recently",
                        likesCount = it.getInt(it.getColumnIndexOrThrow("likes_count"))
                    )
                )
            }
        }
        list
    }

    suspend fun addComment(postId: String, author: UserProfile, text: String): PostComment = withContext(Dispatchers.IO) {
        val commentId = "c_${UUID.randomUUID().toString().take(8)}"
        val now = System.currentTimeMillis()
        val db = dbHelper.writableDatabase

        val cv = ContentValues().apply {
            put("id", commentId)
            put("post_id", postId)
            put("user_id", author.id)
            put("author_name", author.name)
            put("author_username", author.username)
            put("author_avatar_res", author.avatarResId)
            put("comment_text", text.trim())
            put("likes_count", 0)
            put("time_ago", "Just now")
            put("created_at", now)
        }
        db.insert(FGTimeDatabaseHelper.TABLE_COMMENTS, null, cv)
        db.execSQL("UPDATE ${FGTimeDatabaseHelper.TABLE_POSTS} SET comments_count = comments_count + 1 WHERE id = ?", arrayOf(postId))

        PostComment(
            id = commentId,
            postId = postId,
            authorName = author.name,
            authorUsername = author.username,
            authorAvatarResId = author.avatarResId,
            commentText = text.trim(),
            timeAgo = "Just now",
            likesCount = 0
        )
    }

    // ==========================================
    // 4. MESSAGES & GROUP CHATS
    // ==========================================

    suspend fun getMessagesForChat(chatId: String, currentUserId: String): List<ChatMessage> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = if (chatId.startsWith("grp_")) {
            db.query(
                FGTimeDatabaseHelper.TABLE_MESSAGES,
                null,
                "chat_id = ?",
                arrayOf(chatId),
                null, null, "created_at ASC"
            )
        } else {
            db.query(
                FGTimeDatabaseHelper.TABLE_MESSAGES,
                null,
                "chat_id = ? AND (sender_id = ? OR receiver_id = ?)",
                arrayOf(chatId, currentUserId, currentUserId),
                null, null, "created_at ASC"
            )
        }
        val list = mutableListOf<ChatMessage>()
        cursor.use {
            while (it.moveToNext()) {
                val senderId = it.getString(it.getColumnIndexOrThrow("sender_id"))
                list.add(
                    ChatMessage(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        text = it.getString(it.getColumnIndexOrThrow("message_text")),
                        isMe = senderId == currentUserId,
                        senderName = it.getString(it.getColumnIndexOrThrow("sender_name")),
                        timestamp = it.getString(it.getColumnIndexOrThrow("timestamp_str")),
                        isVoiceNote = it.getInt(it.getColumnIndexOrThrow("is_voice_note")) == 1,
                        voiceDurationSec = it.getInt(it.getColumnIndexOrThrow("voice_duration_sec")),
                        isRead = it.getInt(it.getColumnIndexOrThrow("is_read")) == 1
                    )
                )
            }
        }
        list
    }

    suspend fun sendMessage(
        chatId: String,
        sender: UserProfile,
        receiverId: String,
        text: String,
        isVoiceNote: Boolean = false,
        voiceDuration: Int = 0
    ): ChatMessage = withContext(Dispatchers.IO) {
        val msgId = "m_${UUID.randomUUID().toString().take(8)}"
        val now = System.currentTimeMillis()
        val timeStr = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(now))
        val db = dbHelper.writableDatabase

        val cv = ContentValues().apply {
            put("id", msgId)
            put("chat_id", chatId)
            put("sender_id", sender.id)
            put("sender_name", sender.name)
            put("receiver_id", receiverId)
            put("message_text", text.trim())
            put("is_voice_note", if (isVoiceNote) 1 else 0)
            put("voice_duration_sec", voiceDuration)
            put("timestamp_str", timeStr)
            put("is_read", 1)
            put("created_at", now)
        }
        db.insert(FGTimeDatabaseHelper.TABLE_MESSAGES, null, cv)

        // Update group last message if this is a group chat
        if (chatId.startsWith("grp_")) {
            val cvGrp = ContentValues().apply {
                put("last_message", "${sender.name}: ${text.take(30)}")
            }
            db.update(FGTimeDatabaseHelper.TABLE_GROUPS, cvGrp, "id = ?", arrayOf(chatId))
        }

        ChatMessage(
            id = msgId,
            text = text.trim(),
            isMe = true,
            senderName = sender.name,
            timestamp = timeStr,
            isVoiceNote = isVoiceNote,
            voiceDurationSec = voiceDuration,
            isRead = true
        )
    }

    suspend fun getGroups(): List<GroupChat> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            FGTimeDatabaseHelper.TABLE_GROUPS,
            null, null, null, null, null,
            "created_at DESC"
        )
        val list = mutableListOf<GroupChat>()
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    GroupChat(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        name = it.getString(it.getColumnIndexOrThrow("name")),
                        iconResId = it.getInt(it.getColumnIndexOrThrow("icon_res")).let { r -> if (r == 0) R.drawable.fgtime_leaf_logo else r },
                        memberCount = it.getInt(it.getColumnIndexOrThrow("member_count")),
                        lastMessage = it.getString(it.getColumnIndexOrThrow("last_message")) ?: "",
                        adminUserId = it.getString(it.getColumnIndexOrThrow("admin_user_id"))
                    )
                )
            }
        }
        list
    }

    suspend fun createGroup(name: String, adminUser: UserProfile, initialMembers: Int = 2): GroupChat = withContext(Dispatchers.IO) {
        val grpId = "grp_${UUID.randomUUID().toString().take(8)}"
        val now = System.currentTimeMillis()
        val db = dbHelper.writableDatabase

        val cv = ContentValues().apply {
            put("id", grpId)
            put("name", name.trim())
            put("icon_res", R.drawable.fgtime_leaf_logo)
            put("member_count", initialMembers)
            put("last_message", "Group created by ${adminUser.name}")
            put("admin_user_id", adminUser.id)
            put("created_at", now)
        }
        db.insert(FGTimeDatabaseHelper.TABLE_GROUPS, null, cv)

        GroupChat(
            id = grpId,
            name = name.trim(),
            iconResId = R.drawable.fgtime_leaf_logo,
            memberCount = initialMembers,
            lastMessage = "Group created by ${adminUser.name}",
            adminUserId = adminUser.id
        )
    }

    // ==========================================
    // 5. NOTIFICATIONS
    // ==========================================

    suspend fun getNotifications(userId: String): List<NotificationItem> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            FGTimeDatabaseHelper.TABLE_NOTIFICATIONS,
            null,
            "user_id = ? OR user_id = 'global'",
            arrayOf(userId),
            null, null, "created_at DESC"
        )
        val list = mutableListOf<NotificationItem>()
        cursor.use {
            while (it.moveToNext()) {
                val typeStr = it.getString(it.getColumnIndexOrThrow("type"))
                val nType = try { NotificationType.valueOf(typeStr) } catch (_: Exception) { NotificationType.SYSTEM }
                list.add(
                    NotificationItem(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        type = nType,
                        actorName = it.getString(it.getColumnIndexOrThrow("actor_name")),
                        actorAvatarResId = it.getInt(it.getColumnIndexOrThrow("actor_avatar_res")).let { r -> if (r == 0) R.drawable.fgtime_leaf_logo else r },
                        content = it.getString(it.getColumnIndexOrThrow("content")),
                        timeAgo = it.getString(it.getColumnIndexOrThrow("time_ago")) ?: "Recently",
                        isRead = it.getInt(it.getColumnIndexOrThrow("is_read")) == 1
                    )
                )
            }
        }
        list
    }

    // ==========================================
    // 5. STORY & STATUS VIEWERS OPERATIONS
    // ==========================================

    suspend fun getStoryViewers(storyId: String): List<StoryViewer> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            FGTimeDatabaseHelper.TABLE_STORY_VIEWERS,
            null,
            "story_id = ?",
            arrayOf(storyId),
            null, null, "viewed_at DESC"
        )
        val list = mutableListOf<StoryViewer>()
        cursor.use {
            while (it.moveToNext()) {
                val viewerId = it.getString(it.getColumnIndexOrThrow("viewer_id"))
                val viewerName = it.getString(it.getColumnIndexOrThrow("viewer_name"))
                val viewerUsername = it.getString(it.getColumnIndexOrThrow("viewer_username"))
                val avatarRes = it.getInt(it.getColumnIndexOrThrow("viewer_avatar_res")).let { res ->
                    if (res == 0) R.drawable.fgtime_leaf_logo else res
                }
                val viewedAt = it.getLong(it.getColumnIndexOrThrow("viewed_at"))
                val viewedAtAgo = formatTimeAgo(viewedAt)

                val user = UserProfile(
                    id = viewerId,
                    name = viewerName,
                    username = viewerUsername,
                    avatarResId = avatarRes
                )
                list.add(StoryViewer(user = user, viewedAtAgo = viewedAtAgo))
            }
        }
        list
    }

    suspend fun recordStoryView(storyId: String, viewer: UserProfile): Boolean = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val now = System.currentTimeMillis()
        val cv = ContentValues().apply {
            put("story_id", storyId)
            put("viewer_id", viewer.id)
            put("viewer_name", viewer.name)
            put("viewer_username", viewer.username)
            put("viewer_avatar_res", viewer.avatarResId)
            put("viewed_at", now)
        }
        val inserted = db.insertWithOnConflict(
            FGTimeDatabaseHelper.TABLE_STORY_VIEWERS,
            null,
            cv,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
        inserted != -1L
    }

    // ==========================================
    // HELPERS
    // ==========================================

    private fun checkItemExists(db: android.database.sqlite.SQLiteDatabase, table: String, userId: String, postId: String): Boolean {
        val c = db.query(table, arrayOf("post_id"), "user_id = ? AND post_id = ?", arrayOf(userId, postId), null, null, null)
        return c.use { it.count > 0 }
    }

    private fun getPostLikesCount(db: android.database.sqlite.SQLiteDatabase, postId: String): Int {
        val c = db.query(FGTimeDatabaseHelper.TABLE_POSTS, arrayOf("likes_count"), "id = ?", arrayOf(postId), null, null, null)
        return c.use { if (it.moveToFirst()) it.getInt(0) else 0 }
    }

    private fun mapUserFromCursor(cursor: Cursor): UserProfile {
        val avatarRes = cursor.getInt(cursor.getColumnIndexOrThrow("avatar_res")).let {
            if (it == 0) R.drawable.fgtime_leaf_logo else it
        }
        return UserProfile(
            id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
            name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
            username = cursor.getString(cursor.getColumnIndexOrThrow("username")),
            contact = cursor.getString(cursor.getColumnIndexOrThrow("phone")) ?: "",
            bio = cursor.getString(cursor.getColumnIndexOrThrow("bio")) ?: "Spreading positive green vibes on FG TIME 🌿",
            avatarResId = avatarRes,
            followers = cursor.getInt(cursor.getColumnIndexOrThrow("followers")),
            following = cursor.getInt(cursor.getColumnIndexOrThrow("following")),
            postsCount = cursor.getInt(cursor.getColumnIndexOrThrow("posts_count")),
            isPrivate = cursor.getInt(cursor.getColumnIndexOrThrow("is_private")) == 1,
            isVerified = cursor.getInt(cursor.getColumnIndexOrThrow("is_verified")) == 1,
            isOwner = cursor.getInt(cursor.getColumnIndexOrThrow("is_owner")) == 1,
            dateOfBirth = cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")) ?: "2001-08-15"
        )
    }

    private fun formatTimeAgo(timestampMillis: Long): String {
        val diff = System.currentTimeMillis() - timestampMillis
        val mins = diff / (60 * 1000)
        return when {
            mins < 1 -> "Just now"
            mins < 60 -> "${mins}m ago"
            mins < 1440 -> "${mins / 60}h ago"
            else -> "${mins / 1440}d ago"
        }
    }
}
