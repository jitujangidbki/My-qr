package com.example.generator

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object QrDataEncoder {

    fun encodeUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        return if (!trimmed.startsWith("http://", ignoreCase = true) &&
            !trimmed.startsWith("https://", ignoreCase = true)
        ) {
            "https://$trimmed"
        } else {
            trimmed
        }
    }

    fun encodeText(text: String): String = text.trim()

    fun encodePhone(phone: String): String {
        val clean = phone.replace(Regex("[^0-9+]"), "")
        return "tel:$clean"
    }

    fun encodeSms(phone: String, message: String): String {
        val clean = phone.replace(Regex("[^0-9+]"), "")
        return "smsto:$clean:${message.trim()}"
    }

    fun encodeEmail(email: String, subject: String, body: String): String {
        val sb = StringBuilder("mailto:${email.trim()}")
        val params = mutableListOf<String>()
        if (subject.isNotBlank()) params.add("subject=${encodeParam(subject.trim())}")
        if (body.isNotBlank()) params.add("body=${encodeParam(body.trim())}")
        if (params.isNotEmpty()) {
            sb.append("?").append(params.joinToString("&"))
        }
        return sb.toString()
    }

    fun encodeVCard(
        firstName: String,
        lastName: String,
        phone: String,
        email: String,
        org: String,
        note: String
    ): String {
        val fn = "$firstName $lastName".trim()
        val sb = StringBuilder()
        sb.appendLine("BEGIN:VCARD")
        sb.appendLine("VERSION:3.0")
        sb.appendLine("N:${lastName.trim()};${firstName.trim()};;;")
        if (fn.isNotBlank()) sb.appendLine("FN:$fn")
        if (org.isNotBlank()) sb.appendLine("ORG:${org.trim()}")
        if (phone.isNotBlank()) sb.appendLine("TEL;TYPE=CELL:${phone.trim()}")
        if (email.isNotBlank()) sb.appendLine("EMAIL;TYPE=INTERNET:${email.trim()}")
        if (note.isNotBlank()) sb.appendLine("NOTE:${note.trim()}")
        sb.append("END:VCARD")
        return sb.toString()
    }

    fun encodeWifi(
        ssid: String,
        password: String,
        encryption: String, // "WPA", "WEP", "nopass"
        isHidden: Boolean
    ): String {
        val enc = when (encryption.uppercase()) {
            "WPA", "WPA2", "WPA3" -> "WPA"
            "WEP" -> "WEP"
            else -> "nopass"
        }
        val pass = if (enc == "nopass") "" else password.trim()
        return "WIFI:S:${ssid.trim()};T:$enc;P:$pass;H:${if (isHidden) "true" else "false"};;"
    }

    fun encodeUpi(
        upiId: String,
        name: String,
        amount: String?,
        note: String?
    ): String {
        val cleanUpi = upiId.trim()
        val cleanName = name.trim()
        val sb = StringBuilder("upi://pay?pa=$cleanUpi&pn=${encodeParam(cleanName)}&cu=INR")
        
        amount?.trim()?.takeIf { it.isNotBlank() }?.let { amt ->
            sb.append("&am=$amt")
        }
        note?.trim()?.takeIf { it.isNotBlank() }?.let { n ->
            sb.append("&tn=${encodeParam(n)}")
        }
        return sb.toString()
    }

    fun encodeLocation(lat: String, lng: String, locationName: String?): String {
        val cleanLat = lat.trim()
        val cleanLng = lng.trim()
        return if (cleanLat.isNotBlank() && cleanLng.isNotBlank()) {
            val q = locationName?.trim()?.takeIf { it.isNotBlank() } ?: "$cleanLat,$cleanLng"
            "geo:$cleanLat,$cleanLng?q=${encodeParam(q)}"
        } else if (!locationName.isNullOrBlank()) {
            "https://maps.google.com/?q=${encodeParam(locationName.trim())}"
        } else {
            "geo:0,0"
        }
    }

    fun encodeEvent(
        title: String,
        location: String?,
        description: String?,
        startTimeMillis: Long,
        endTimeMillis: Long
    ): String {
        val sdf = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val startStr = sdf.format(Date(startTimeMillis))
        val endStr = sdf.format(Date(endTimeMillis))

        val sb = StringBuilder()
        sb.appendLine("BEGIN:VEVENT")
        sb.appendLine("SUMMARY:${title.trim()}")
        if (!description.isNullOrBlank()) sb.appendLine("DESCRIPTION:${description.trim()}")
        if (!location.isNullOrBlank()) sb.appendLine("LOCATION:${location.trim()}")
        sb.appendLine("DTSTART:$startStr")
        sb.appendLine("DTEND:$endStr")
        sb.append("END:VEVENT")
        return sb.toString()
    }

    private fun encodeParam(str: String): String {
        return java.net.URLEncoder.encode(str, "UTF-8").replace("+", "%20")
    }
}
