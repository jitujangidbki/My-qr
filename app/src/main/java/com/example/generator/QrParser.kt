package com.example.generator

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import com.example.generator.QrType

data class ScannedResult(
    val rawContent: String,
    val type: QrType,
    val title: String,
    val summary: String,
    val detailsMap: Map<String, String> = emptyMap()
)

object QrParser {

    fun parse(raw: String): ScannedResult {
        val trimmed = raw.trim()

        return when {
            // UPI Payment
            trimmed.startsWith("upi://pay", ignoreCase = true) -> {
                val params = parseQueryParams(trimmed)
                val pa = params["pa"] ?: ""
                val pn = params["pn"] ?: "UPI Receiver"
                val am = params["am"]
                val tn = params["tn"]
                
                val details = mutableMapOf("UPI ID" to pa, "Receiver" to pn)
                if (!am.isNullOrBlank()) details["Amount"] = "₹$am"
                if (!tn.isNullOrBlank()) details["Note"] = tn

                ScannedResult(
                    rawContent = raw,
                    type = QrType.UPI,
                    title = "UPI Payment",
                    summary = "Pay ₹${am ?: "Any"} to $pn ($pa)",
                    detailsMap = details
                )
            }

            // Wi-Fi
            trimmed.startsWith("WIFI:", ignoreCase = true) -> {
                val body = trimmed.substring(5)
                val parts = body.split(";")
                var ssid = ""
                var pass = ""
                var enc = "WPA"
                var hidden = "false"

                for (part in parts) {
                    when {
                        part.startsWith("S:", ignoreCase = true) -> ssid = part.substring(2)
                        part.startsWith("P:", ignoreCase = true) -> pass = part.substring(2)
                        part.startsWith("T:", ignoreCase = true) -> enc = part.substring(2)
                        part.startsWith("H:", ignoreCase = true) -> hidden = part.substring(2)
                    }
                }

                ScannedResult(
                    rawContent = raw,
                    type = QrType.WIFI,
                    title = "Wi-Fi Network: $ssid",
                    summary = "Network: $ssid ($enc)",
                    detailsMap = mapOf(
                        "SSID" to ssid,
                        "Password" to if (pass.isBlank()) "(Open Network)" else pass,
                        "Encryption" to enc,
                        "Hidden" to hidden
                    )
                )
            }

            // vCard / Contact
            trimmed.contains("BEGIN:VCARD", ignoreCase = true) -> {
                var name = ""
                var phone = ""
                var email = ""
                var org = ""
                var note = ""

                val lines = trimmed.lines()
                for (line in lines) {
                    val l = line.trim()
                    when {
                        l.startsWith("FN:", ignoreCase = true) -> name = l.substring(3)
                        l.startsWith("N:", ignoreCase = true) && name.isBlank() -> {
                            val parts = l.substring(2).split(";")
                            val lastName = parts.getOrNull(0) ?: ""
                            val firstName = parts.getOrNull(1) ?: ""
                            name = "$firstName $lastName".trim()
                        }
                        l.startsWith("TEL", ignoreCase = true) -> {
                            val idx = l.indexOf(":")
                            if (idx != -1) phone = l.substring(idx + 1)
                        }
                        l.startsWith("EMAIL", ignoreCase = true) -> {
                            val idx = l.indexOf(":")
                            if (idx != -1) email = l.substring(idx + 1)
                        }
                        l.startsWith("ORG:", ignoreCase = true) -> org = l.substring(4)
                        l.startsWith("NOTE:", ignoreCase = true) -> note = l.substring(5)
                    }
                }

                val details = mutableMapOf<String, String>()
                if (name.isNotBlank()) details["Name"] = name
                if (phone.isNotBlank()) details["Phone"] = phone
                if (email.isNotBlank()) details["Email"] = email
                if (org.isNotBlank()) details["Organization"] = org
                if (note.isNotBlank()) details["Note"] = note

                ScannedResult(
                    rawContent = raw,
                    type = QrType.VCARD,
                    title = name.ifBlank { "Contact Card" },
                    summary = listOfNotNull(name.takeIf { it.isNotBlank() }, phone.takeIf { it.isNotBlank() }).joinToString(" • "),
                    detailsMap = details
                )
            }

            // SMS
            trimmed.startsWith("smsto:", ignoreCase = true) -> {
                val body = trimmed.substring(6)
                val parts = body.split(":", limit = 2)
                val phone = parts.getOrNull(0) ?: ""
                val msg = parts.getOrNull(1) ?: ""

                ScannedResult(
                    rawContent = raw,
                    type = QrType.SMS,
                    title = "SMS to $phone",
                    summary = msg.ifBlank { "Send SMS to $phone" },
                    detailsMap = mapOf("Recipient" to phone, "Message" to msg)
                )
            }

            // Phone
            trimmed.startsWith("tel:", ignoreCase = true) -> {
                val phone = trimmed.substring(4)
                ScannedResult(
                    rawContent = raw,
                    type = QrType.PHONE,
                    title = "Phone Call",
                    summary = phone,
                    detailsMap = mapOf("Phone Number" to phone)
                )
            }

            // Email
            trimmed.startsWith("mailto:", ignoreCase = true) -> {
                val emailUri = Uri.parse(trimmed)
                val email = emailUri.schemeSpecificPart.split("?").firstOrNull() ?: ""
                val subject = emailUri.getQueryParameter("subject") ?: ""
                val body = emailUri.getQueryParameter("body") ?: ""

                ScannedResult(
                    rawContent = raw,
                    type = QrType.EMAIL,
                    title = "Email to $email",
                    summary = if (subject.isNotBlank()) subject else email,
                    detailsMap = mapOf("Email" to email, "Subject" to subject, "Body" to body)
                )
            }

            // Location
            trimmed.startsWith("geo:", ignoreCase = true) || trimmed.contains("maps.google.com") -> {
                ScannedResult(
                    rawContent = raw,
                    type = QrType.LOCATION,
                    title = "Map Location",
                    summary = trimmed,
                    detailsMap = mapOf("Location" to trimmed)
                )
            }

            // Calendar Event
            trimmed.contains("BEGIN:VEVENT", ignoreCase = true) -> {
                var title = "Calendar Event"
                var loc = ""
                var desc = ""

                for (line in trimmed.lines()) {
                    val l = line.trim()
                    when {
                        l.startsWith("SUMMARY:", ignoreCase = true) -> title = l.substring(8)
                        l.startsWith("LOCATION:", ignoreCase = true) -> loc = l.substring(9)
                        l.startsWith("DESCRIPTION:", ignoreCase = true) -> desc = l.substring(12)
                    }
                }

                ScannedResult(
                    rawContent = raw,
                    type = QrType.EVENT,
                    title = title,
                    summary = listOfNotNull(title, loc.takeIf { it.isNotBlank() }).joinToString(" • "),
                    detailsMap = mapOf("Title" to title, "Location" to loc, "Description" to desc)
                )
            }

            // Website / URL
            trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("www.", ignoreCase = true) -> {
                val url = if (trimmed.startsWith("www.", ignoreCase = true)) "https://$trimmed" else trimmed
                ScannedResult(
                    rawContent = raw,
                    type = QrType.URL,
                    title = "Website Link",
                    summary = url,
                    detailsMap = mapOf("URL" to url)
                )
            }

            // Fallback: Plain Text
            else -> {
                ScannedResult(
                    rawContent = raw,
                    type = QrType.TEXT,
                    title = "Plain Text",
                    summary = trimmed.take(80),
                    detailsMap = mapOf("Text" to trimmed)
                )
            }
        }
    }

    private fun parseQueryParams(url: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val idx = url.indexOf("?")
        if (idx != -1) {
            val query = url.substring(idx + 1)
            val pairs = query.split("&")
            for (pair in pairs) {
                val parts = pair.split("=", limit = 2)
                if (parts.size == 2) {
                    map[parts[0]] = java.net.URLDecoder.decode(parts[1], "UTF-8")
                }
            }
        }
        return map
    }
}
