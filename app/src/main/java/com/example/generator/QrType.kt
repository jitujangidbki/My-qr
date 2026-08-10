package com.example.generator

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.ShortText
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.vector.ImageVector

enum class QrType(
    val title: String,
    val description: String,
    val icon: ImageVector
) {
    URL("Website / URL", "Link to any webpage or social media profile", Icons.Default.Language),
    TEXT("Plain Text", "Custom text message, note, or raw code", Icons.AutoMirrored.Filled.ShortText),
    PHONE("Phone Number", "Direct call trigger for mobile or landline", Icons.Default.Phone),
    SMS("SMS Message", "Pre-filled text message to a specific number", Icons.AutoMirrored.Filled.Message),
    EMAIL("Email", "Pre-addressed email with subject and body", Icons.Default.Email),
    VCARD("Contact / vCard", "Complete contact details for address book import", Icons.Default.AccountBox),
    WIFI("Wi-Fi Network", "Instant Wi-Fi auto-connect credentials", Icons.Default.Wifi),
    UPI("UPI Payment", "Indian UPI payment QR for GPay, PhonePe, Paytm", Icons.Default.Payments),
    LOCATION("Map Location", "GPS coordinates or address for navigation", Icons.Default.LocationOn),
    EVENT("Calendar Event", "Event title, schedule, and venue details", Icons.Default.CalendarMonth)
}
