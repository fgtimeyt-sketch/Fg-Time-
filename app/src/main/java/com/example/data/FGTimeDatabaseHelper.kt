package com.example.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.R
import com.example.model.*

class FGTimeDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "fgtime_social.db"
        const val DATABASE_VERSION = 1

        // Table names
        const val TABLE_USERS = "users"
        const val TABLE_OTP_SESSIONS = "otp_sessions"
        const val TABLE_POSTS = "posts"
        const val TABLE_LIKES = "likes"
        const val TABLE_SAVED = "saved_posts"
        const val TABLE_COMMENTS = "comments"
        const val TABLE_MESSAGES = "messages"
        const val TABLE_GROUPS = "groups"
        const val TABLE_FOLLOWS = "follows"
        const val TABLE_BLOCKED = "blocked_users"
        const val TABLE_NOTIFICATIONS = "notifications"

        @Volatile
        private var instance: FGTimeDatabaseHelper? = null

        fun getInstance(context: Context): FGTimeDatabaseHelper {
            return instance ?: synchronized(this) {
                instance ?: FGTimeDatabaseHelper(context.applicationContext).also { instance = it }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_USERS (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                username TEXT NOT NULL UNIQUE,
                phone TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL,
                bio TEXT,
                avatar_res INTEGER,
                avatar_uri TEXT,
                followers INTEGER DEFAULT 0,
                following INTEGER DEFAULT 0,
                posts_count INTEGER DEFAULT 0,
                is_private INTEGER DEFAULT 0,
                is_verified INTEGER DEFAULT 0,
                is_owner INTEGER DEFAULT 0,
                date_of_birth TEXT,
                created_at INTEGER
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_OTP_SESSIONS (
                id TEXT PRIMARY KEY,
                phone TEXT NOT NULL,
                otp_code TEXT NOT NULL,
                purpose TEXT NOT NULL,
                expires_at INTEGER NOT NULL,
                is_verified INTEGER DEFAULT 0,
                created_at INTEGER NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_POSTS (
                id TEXT PRIMARY KEY,
                user_id TEXT NOT NULL,
                author_name TEXT NOT NULL,
                author_username TEXT NOT NULL,
                author_avatar_res INTEGER,
                author_avatar_uri TEXT,
                content TEXT NOT NULL,
                image_res INTEGER,
                media_uri TEXT,
                media_type TEXT DEFAULT 'PHOTO',
                tags TEXT,
                location TEXT,
                likes_count INTEGER DEFAULT 0,
                comments_count INTEGER DEFAULT 0,
                created_at INTEGER NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_LIKES (
                user_id TEXT NOT NULL,
                post_id TEXT NOT NULL,
                PRIMARY KEY (user_id, post_id)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_SAVED (
                user_id TEXT NOT NULL,
                post_id TEXT NOT NULL,
                PRIMARY KEY (user_id, post_id)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_COMMENTS (
                id TEXT PRIMARY KEY,
                post_id TEXT NOT NULL,
                user_id TEXT NOT NULL,
                author_name TEXT NOT NULL,
                author_username TEXT NOT NULL,
                author_avatar_res INTEGER,
                comment_text TEXT NOT NULL,
                likes_count INTEGER DEFAULT 0,
                time_ago TEXT,
                created_at INTEGER NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_MESSAGES (
                id TEXT PRIMARY KEY,
                chat_id TEXT NOT NULL,
                sender_id TEXT NOT NULL,
                sender_name TEXT NOT NULL,
                receiver_id TEXT NOT NULL,
                message_text TEXT NOT NULL,
                is_voice_note INTEGER DEFAULT 0,
                voice_duration_sec INTEGER DEFAULT 0,
                timestamp_str TEXT NOT NULL,
                is_read INTEGER DEFAULT 1,
                created_at INTEGER NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_GROUPS (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                icon_res INTEGER,
                member_count INTEGER DEFAULT 1,
                last_message TEXT,
                admin_user_id TEXT NOT NULL,
                created_at INTEGER NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_FOLLOWS (
                follower_id TEXT NOT NULL,
                following_id TEXT NOT NULL,
                PRIMARY KEY (follower_id, following_id)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_BLOCKED (
                user_id TEXT NOT NULL,
                blocked_id TEXT NOT NULL,
                PRIMARY KEY (user_id, blocked_id)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_NOTIFICATIONS (
                id TEXT PRIMARY KEY,
                user_id TEXT NOT NULL,
                type TEXT NOT NULL,
                actor_name TEXT NOT NULL,
                actor_avatar_res INTEGER,
                content TEXT NOT NULL,
                time_ago TEXT,
                is_read INTEGER DEFAULT 0,
                created_at INTEGER NOT NULL
            )
        """.trimIndent())

        seedInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Migration strategy if schema changes
    }

    private fun seedInitialData(db: SQLiteDatabase) {
        val now = System.currentTimeMillis()
        val defaultPasswordHash = SecurityUtils.hashPassword("FGTime@123")

        // 1. Seed Owner Muksidul
        val cvMuksidul = ContentValues().apply {
            put("id", "u_owner_muksidul")
            put("name", "Muksidul")
            put("username", "muksidul")
            put("phone", "+919876543210")
            put("password_hash", defaultPasswordHash)
            put("bio", "Creator & Founder of FG TIME 🌿 Empowering authentic growth and creative connection.")
            put("avatar_res", R.drawable.fgtime_leaf_logo)
            put("followers", 5840)
            put("following", 142)
            put("posts_count", 42)
            put("is_private", 0)
            put("is_verified", 1)
            put("is_owner", 1)
            put("date_of_birth", "1999-05-20")
            put("created_at", now - 864000000L)
        }
        db.insert(TABLE_USERS, null, cvMuksidul)

        // 2. Seed Maya Green
        val cvMaya = ContentValues().apply {
            put("id", "u_maya")
            put("name", "Maya Green")
            put("username", "maya_creative")
            put("phone", "+919876543211")
            put("password_hash", defaultPasswordHash)
            put("bio", "Visual artist & sustainable living advocate 🍃")
            put("avatar_res", R.drawable.fgtime_creator)
            put("followers", 1420)
            put("following", 412)
            put("posts_count", 28)
            put("is_private", 0)
            put("is_verified", 1)
            put("is_owner", 0)
            put("date_of_birth", "2000-03-14")
            put("created_at", now - 600000000L)
        }
        db.insert(TABLE_USERS, null, cvMaya)

        // 3. Seed Rohan Verma
        val cvRohan = ContentValues().apply {
            put("id", "u_rohan")
            put("name", "Rohan Verma")
            put("username", "rohan_sound")
            put("phone", "+919876543212")
            put("password_hash", defaultPasswordHash)
            put("bio", "Music producer • Lo-Fi & organic frequencies 🎧")
            put("avatar_res", R.drawable.fgtime_leaf_logo)
            put("followers", 890)
            put("following", 210)
            put("posts_count", 14)
            put("is_private", 0)
            put("is_verified", 0)
            put("is_owner", 0)
            put("date_of_birth", "2002-11-28")
            put("created_at", now - 400000000L)
        }
        db.insert(TABLE_USERS, null, cvRohan)

        // Seed initial posts
        val p1 = ContentValues().apply {
            put("id", "p_owner")
            put("user_id", "u_owner_muksidul")
            put("author_name", "Muksidul")
            put("author_username", "muksidul")
            put("author_avatar_res", R.drawable.fgtime_leaf_logo)
            put("content", "Welcome everyone to FG TIME! 🌿 We built this platform for creativity, peace of mind, and genuine community. Check out the new Remix Studio, Audio & Video Calls, and instant messaging. Let us know your thoughts! #FGTime #FounderNote")
            put("image_res", R.drawable.fgtime_banner)
            put("media_type", "PHOTO")
            put("tags", "#FGTime #FounderNote #Community")
            put("location", "FG TIME Global HQ")
            put("likes_count", 485)
            put("comments_count", 2)
            put("created_at", now - 1800000L)
        }
        db.insert(TABLE_POSTS, null, p1)

        val p2 = ContentValues().apply {
            put("id", "p_maya_1")
            put("user_id", "u_maya")
            put("author_name", "Maya Green")
            put("author_username", "maya_creative")
            put("author_avatar_res", R.drawable.fgtime_creator)
            put("content", "Embracing growth and creative stillness. When you surround yourself with green energy, creativity flows naturally. 🌿📷 #FGTime #GreenGrowth #NatureVibes")
            put("image_res", R.drawable.fgtime_creator)
            put("media_type", "PHOTO")
            put("tags", "#FGTime #GreenGrowth #CreativeStudio")
            put("location", "Botanical Design Loft")
            put("likes_count", 158)
            put("comments_count", 1)
            put("created_at", now - 3600000L)
        }
        db.insert(TABLE_POSTS, null, p2)

        val p3 = ContentValues().apply {
            put("id", "p_rohan_1")
            put("user_id", "u_rohan")
            put("author_name", "Rohan Verma")
            put("author_username", "rohan_sound")
            put("author_avatar_res", R.drawable.fgtime_leaf_logo)
            put("content", "Fresh ambient session recorded live in the greenhouse! Tested it with the FG TIME Remix Studio and added lo-fi bird echoes. 🎧✨ #LoFiBeats #RemixStudio")
            put("image_res", R.drawable.fgtime_reel)
            put("media_type", "VIDEO")
            put("tags", "#FGTime #RemixStudio #MusicCommunity")
            put("location", "Emerald Sound Studio")
            put("likes_count", 230)
            put("comments_count", 0)
            put("created_at", now - 7200000L)
        }
        db.insert(TABLE_POSTS, null, p3)

        // Seed initial comments
        val c1 = ContentValues().apply {
            put("id", "c_1")
            put("post_id", "p_owner")
            put("user_id", "u_maya")
            put("author_name", "Maya Green")
            put("author_username", "maya_creative")
            put("author_avatar_res", R.drawable.fgtime_creator)
            put("comment_text", "Congratulations Muksidul! The new features feel so smooth and vibrant 💚")
            put("likes_count", 15)
            put("time_ago", "20m ago")
            put("created_at", now - 1200000L)
        }
        db.insert(TABLE_COMMENTS, null, c1)

        val c2 = ContentValues().apply {
            put("id", "c_2")
            put("post_id", "p_owner")
            put("user_id", "u_rohan")
            put("author_name", "Rohan Verma")
            put("author_username", "rohan_sound")
            put("author_avatar_res", R.drawable.fgtime_leaf_logo)
            put("comment_text", "The calling and audio remix studio integration is game-changing!")
            put("likes_count", 9)
            put("time_ago", "10m ago")
            put("created_at", now - 600000L)
        }
        db.insert(TABLE_COMMENTS, null, c2)

        // Seed initial groups
        val g1 = ContentValues().apply {
            put("id", "grp_creators")
            put("name", "FG Emerald Creators 🌿")
            put("icon_res", R.drawable.fgtime_leaf_logo)
            put("member_count", 8)
            put("last_message", "Maya: Sharing new botanical color palettes!")
            put("admin_user_id", "u_owner_muksidul")
            put("created_at", now - 86400000L)
        }
        db.insert(TABLE_GROUPS, null, g1)

        val g2 = ContentValues().apply {
            put("id", "grp_remix")
            put("name", "Sound & Remix Lab 🎧")
            put("icon_res", R.drawable.fgtime_banner)
            put("member_count", 12)
            put("last_message", "Rohan: New 85 BPM lo-fi stem dropped in studio.")
            put("admin_user_id", "u_rohan")
            put("created_at", now - 43200000L)
        }
        db.insert(TABLE_GROUPS, null, g2)

        // Seed initial direct messages (Chat with Maya)
        val m1 = ContentValues().apply {
            put("id", "m_1")
            put("chat_id", "dm_maya")
            put("sender_id", "u_maya")
            put("sender_name", "Maya Green")
            put("receiver_id", "u_owner_muksidul")
            put("message_text", "Hey! The new FG TIME look and green leaf theme is so peaceful 🌿")
            put("timestamp_str", "10:14 AM")
            put("is_read", 1)
            put("created_at", now - 3600000L)
        }
        db.insert(TABLE_MESSAGES, null, m1)

        val m2 = ContentValues().apply {
            put("id", "m_2")
            put("chat_id", "dm_maya")
            put("sender_id", "u_owner_muksidul")
            put("sender_name", "You")
            put("receiver_id", "u_maya")
            put("message_text", "Thank you Maya! We worked hard on OTP security, video trimming, and instant group messaging.")
            put("timestamp_str", "10:16 AM")
            put("is_read", 1)
            put("created_at", now - 3500000L)
        }
        db.insert(TABLE_MESSAGES, null, m2)

        // Seed group message
        val mg1 = ContentValues().apply {
            put("id", "m_grp_1")
            put("chat_id", "grp_creators")
            put("sender_id", "u_maya")
            put("sender_name", "Maya Green")
            put("receiver_id", "grp_creators")
            put("message_text", "Welcome to FG Emerald Creators! Feel free to share your works in progress.")
            put("timestamp_str", "Yesterday")
            put("is_read", 1)
            put("created_at", now - 86000000L)
        }
        db.insert(TABLE_MESSAGES, null, mg1)

        // Seed follows
        val f1 = ContentValues().apply {
            put("follower_id", "u_owner_muksidul")
            put("following_id", "u_maya")
        }
        db.insert(TABLE_FOLLOWS, null, f1)

        val f2 = ContentValues().apply {
            put("follower_id", "u_maya")
            put("following_id", "u_owner_muksidul")
        }
        db.insert(TABLE_FOLLOWS, null, f2)
    }
}
