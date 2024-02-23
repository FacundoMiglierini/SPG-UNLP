package com.example.spgunlp.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CONTENT_TYPE {
    TEXT,
    AUDIO,
    IMAGE
}

@Entity(tableName = "messages_table")
data class AppMessage (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val idVisit: Int,
    val idPrinciple: Int,
    val content_type: CONTENT_TYPE?, // TEXT, AUDIO, IMAGE
    val data: String?,
    val date: String?,
    @Embedded
    val sender: ChatUser?,
) {
    data class ChatUser (
        val email: String?,
        val nombre: String?
    )
}