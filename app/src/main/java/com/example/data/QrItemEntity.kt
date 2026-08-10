package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qr_history")
data class QrItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val qrType: String, // e.g. "URL", "TEXT", "PHONE", "SMS", "EMAIL", "VCARD", "WIFI", "UPI", "LOCATION", "EVENT"
    val content: String,
    val displaySummary: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isGenerated: Boolean = true, // true if user generated, false if scanned
    val fgColorHex: String = "#000000",
    val bgColorHex: String = "#FFFFFF",
    val dotStyleName: String = "SQUARE",
    val eyeStyleName: String = "SQUARE",
    val eccLevelName: String = "M",
    val margin: Int = 2,
    val logoUri: String? = null
)
