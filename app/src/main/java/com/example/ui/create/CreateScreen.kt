package com.example.ui.create

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator.QrType
import com.example.ui.navigation.Screen
import com.example.viewmodel.MainViewModel

@Composable
fun CreateScreen(
    viewModel: MainViewModel,
    onNavigate: (Screen) -> Unit
) {
    val selectedType by viewModel.selectedType.collectAsState()
    val encodedContent by viewModel.encodedContent.collectAsState()
    val qrTitle by viewModel.qrTitle.collectAsState()
    val generatedBitmap by viewModel.generatedBitmap.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()

    // Form input states
    val urlInput by viewModel.urlInput.collectAsState()
    val textInput by viewModel.textInput.collectAsState()
    val phoneInput by viewModel.phoneInput.collectAsState()

    val smsPhoneInput by viewModel.smsPhoneInput.collectAsState()
    val smsMessageInput by viewModel.smsMessageInput.collectAsState()

    val emailAddressInput by viewModel.emailAddressInput.collectAsState()
    val emailSubjectInput by viewModel.emailSubjectInput.collectAsState()
    val emailBodyInput by viewModel.emailBodyInput.collectAsState()

    val vcardFirstNameInput by viewModel.vcardFirstNameInput.collectAsState()
    val vcardLastNameInput by viewModel.vcardLastNameInput.collectAsState()
    val vcardPhoneInput by viewModel.vcardPhoneInput.collectAsState()
    val vcardEmailInput by viewModel.vcardEmailInput.collectAsState()
    val vcardOrgInput by viewModel.vcardOrgInput.collectAsState()
    val vcardNoteInput by viewModel.vcardNoteInput.collectAsState()

    val wifiSsidInput by viewModel.wifiSsidInput.collectAsState()
    val wifiPasswordInput by viewModel.wifiPasswordInput.collectAsState()
    val wifiEncryptionInput by viewModel.wifiEncryptionInput.collectAsState()
    val wifiHiddenInput by viewModel.wifiHiddenInput.collectAsState()

    val upiIdInput by viewModel.upiIdInput.collectAsState()
    val upiNameInput by viewModel.upiNameInput.collectAsState()
    val upiAmountInput by viewModel.upiAmountInput.collectAsState()
    val upiNoteInput by viewModel.upiNoteInput.collectAsState()

    val locationLatInput by viewModel.locationLatInput.collectAsState()
    val locationLngInput by viewModel.locationLngInput.collectAsState()
    val locationQueryInput by viewModel.locationQueryInput.collectAsState()

    val eventTitleInput by viewModel.eventTitleInput.collectAsState()
    val eventLocationInput by viewModel.eventLocationInput.collectAsState()
    val eventDescriptionInput by viewModel.eventDescriptionInput.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Create QR Code",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Select format and enter content for live preview",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Type Selector Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(QrType.values()) { type ->
                val selected = type == selectedType
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.selectType(type) },
                    label = { Text(type.title) },
                    leadingIcon = {
                        Icon(
                            imageVector = type.icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.testTag("type_chip_${type.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Preview Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("live_preview_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Live Preview",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = selectedType.title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    } else if (generatedBitmap != null && encodedContent.isNotBlank()) {
                        Image(
                            bitmap = generatedBitmap!!.asImageBitmap(),
                            contentDescription = "QR Code Preview",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Enter details below",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = qrTitle.ifBlank { "Untitled QR" },
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Form Fields Container
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedType) {
                    QrType.URL -> {
                        OutlinedTextField(
                            value = urlInput,
                            onValueChange = {
                                viewModel.urlInput.value = it
                                viewModel.updateEncodedData()
                            },
                            label = { Text("Website URL *") },
                            placeholder = { Text("https://example.com") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("url_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                        )
                    }

                    QrType.TEXT -> {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = {
                                viewModel.textInput.value = it
                                viewModel.updateEncodedData()
                            },
                            label = { Text("Plain Text *") },
                            placeholder = { Text("Enter text content here...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("text_input")
                        )
                    }

                    QrType.PHONE -> {
                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = {
                                viewModel.phoneInput.value = it
                                viewModel.updateEncodedData()
                            },
                            label = { Text("Phone Number *") },
                            placeholder = { Text("+1 234 567 8900") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phone_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                    }

                    QrType.SMS -> {
                        OutlinedTextField(
                            value = smsPhoneInput,
                            onValueChange = {
                                viewModel.smsPhoneInput.value = it
                                viewModel.updateEncodedData()
                            },
                            label = { Text("Phone Number *") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sms_phone_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        OutlinedTextField(
                            value = smsMessageInput,
                            onValueChange = {
                                viewModel.smsMessageInput.value = it
                                viewModel.updateEncodedData()
                            },
                            label = { Text("Preset Message") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sms_message_input")
                        )
                    }

                    QrType.EMAIL -> {
                        OutlinedTextField(
                            value = emailAddressInput,
                            onValueChange = {
                                viewModel.emailAddressInput.value = it
                                viewModel.updateEncodedData()
                            },
                            label = { Text("Recipient Email *") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_address_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        OutlinedTextField(
                            value = emailSubjectInput,
                            onValueChange = {
                                viewModel.emailSubjectInput.value = it
                                viewModel.updateEncodedData()
                            },
                            label = { Text("Subject") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_subject_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = emailBodyInput,
                            onValueChange = {
                                viewModel.emailBodyInput.value = it
                                viewModel.updateEncodedData()
                            },
                            label = { Text("Body") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_body_input")
                        )
                    }

                    QrType.VCARD -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = vcardFirstNameInput,
                                onValueChange = {
                                    viewModel.vcardFirstNameInput.value = it
                                    viewModel.updateEncodedData()
                                },
                                label = { Text("First Name *") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("vcard_first_name_input"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = vcardLastNameInput,
                                onValueChange = {
                                    viewModel.vcardLastNameInput.value = it
                                    viewModel.updateEncodedData()
                                },
                                label = { Text("Last Name") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("vcard_last_name_input"),
                                singleLine = true
                            )
                        }
                        OutlinedTextField(
                            value = vcardPhoneInput,
                            onValueChange = {
                                viewModel.vcardPhoneInput.value = it
                                viewModel.updateEncodedData()
                            },
                            label = { Text("Mobile Phone") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("vcard_phone_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        OutlinedTextField(
                            value = vcardEmailInput,
                            onValueChange = {
                                viewModel.vcardEmailInput.value = it
                                viewModel.updateEncodedData()
                            },
                            label = { Text("Email Address") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("vcard_email_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        OutlinedTextField(
                            value = vcardOrgInput,
                            onValueChange = {
                                viewModel.vcardOrgInput.value = it
                                viewModel.updateEncodedData()
                            },
                            label = { Text("Company / Org") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("vcard_org_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = vcardNoteInput,
                            onValueChange = {
                                viewModel.vcardNoteInput.value = it
                                viewModel.updateEncodedData()
                            },
                            label = { Text("Note / Title") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("vcard_note_input")
                        )
                    }

                    QrType.WIFI -> {
                        OutlinedTextField(
                            value = wifiSsidInput,
                            onValueChange = {
                                viewModel.wifiSsidInput.value = it
                                viewModel.updateEncodedData()
                            },
                            label = { Text("Wi-Fi Network Name (SSID) *") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("wifi_ssid_input"),
                            singleLine = true
                        )

                        var showPass by remember { mutableStateOf(false) }
                        OutlinedTextField(
                            value = wifiPasswordInput,
                            onValueChange = {
                                viewModel.wifiPasswordInput.value = it
                                viewModel.updateEncodedData()
                            },
                            label = { Text("Password") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("wifi_password_input"),
                            singleLine = true,
                            visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showPass = !showPass }) {
                                    Icon(
                                        imageVector = if (showPass) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null
                                    )
                                }
                            }
                        )

                        Text("Encryption Standard", style = MaterialTheme.typography.labelSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("WPA", "WEP", "nopass").forEach { enc ->
                                FilterChip(
                                    selected = wifiEncryptionInput == enc,
                                    onClick = {
                                        viewModel.wifiEncryptionInput.value = enc
                                        viewModel.updateEncodedData()
                                    },
                                    label = { Text(if (enc == "nopass") "None" else enc) }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Hidden Network", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = wifiHiddenInput,
                                onCheckedChange = {
                                    viewModel.wifiHiddenInput.value = it
                                    viewModel.updateEncodedData()
                                }
                            )
                        }
                    }

                    QrType.UPI -> {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Generates standard UPI URI. QR Studio does not process financial payments.",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        OutlinedTextField(
                            value = upiIdInput,
                            onValueChange = {
                                viewModel.upiIdInput.value = it
                                viewModel.updateEncodedData()
                            },
                            label = { Text("UPI ID (VPA) *") },
                            placeholder = { Text("merchant@upi") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("upi_id_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = upiNameInput,
                            onValueChange = {
                                viewModel.upiNameInput.value = it
                                viewModel.updateEncodedData()
                            },
                            label = { Text("Payee Name *") },
                            placeholder = { Text("John Doe") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("upi_name_input"),
                            singleLine = true
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = upiAmountInput,
                                onValueChange = {
                                    viewModel.upiAmountInput.value = it
                                    viewModel.updateEncodedData()
                                },
                                label = { Text("Amount (₹) Optional") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("upi_amount_input"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = upiNoteInput,
                                onValueChange = {
                                    viewModel.upiNoteInput.value = it
                                    viewModel.updateEncodedData()
                                },
                                label = { Text("Note Optional") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("upi_note_input"),
                                singleLine = true
                            )
                        }
                    }

                    QrType.LOCATION -> {
                        OutlinedTextField(
                            value = locationQueryInput,
                            onValueChange = {
                                viewModel.locationQueryInput.value = it
                                viewModel.updateEncodedData()
                            },
                            label = { Text("Location Name or Address") },
                            placeholder = { Text("Times Square, NY") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("location_query_input")
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = locationLatInput,
                                onValueChange = {
                                    viewModel.locationLatInput.value = it
                                    viewModel.updateEncodedData()
                                },
                                label = { Text("Latitude") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("location_lat_input"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = locationLngInput,
                                onValueChange = {
                                    viewModel.locationLngInput.value = it
                                    viewModel.updateEncodedData()
                                },
                                label = { Text("Longitude") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("location_lng_input"),
                                singleLine = true
                            )
                        }
                    }

                    QrType.EVENT -> {
                        OutlinedTextField(
                            value = eventTitleInput,
                            onValueChange = {
                                viewModel.eventTitleInput.value = it
                                viewModel.updateEncodedData()
                            },
                            label = { Text("Event Title *") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("event_title_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = eventLocationInput,
                            onValueChange = {
                                viewModel.eventLocationInput.value = it
                                viewModel.updateEncodedData()
                            },
                            label = { Text("Venue / Location") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("event_location_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = eventDescriptionInput,
                            onValueChange = {
                                viewModel.eventDescriptionInput.value = it
                                viewModel.updateEncodedData()
                            },
                            label = { Text("Description") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("event_desc_input")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { onNavigate(Screen.Customize) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("customize_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Palette, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Customize Style")
            }

            Button(
                onClick = {
                    if (encodedContent.isNotBlank()) {
                        viewModel.saveCurrentQrToHistory {
                            onNavigate(Screen.Preview)
                        }
                    }
                },
                enabled = encodedContent.isNotBlank(),
                modifier = Modifier
                    .weight(1f)
                    .testTag("save_and_preview_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save & Export")
            }
        }
    }
}
