package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.QrItemEntity
import com.example.data.QrRepository
import com.example.generator.DotStyle
import com.example.generator.EccLevel
import com.example.generator.EyeStyle
import com.example.generator.LogoShape
import com.example.generator.QrBitmapGenerator
import com.example.generator.QrDataEncoder
import com.example.generator.QrOptions
import com.example.generator.QrParser
import com.example.generator.QrType
import com.example.generator.ScannedResult
import com.example.util.ExportUtils
import com.example.util.PreferenceManager
import com.example.util.ThemeOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QrRepository
    private val prefManager = PreferenceManager(application)

    val historyList: StateFlow<List<QrItemEntity>>
    val favoriteList: StateFlow<List<QrItemEntity>>

    val themeState: StateFlow<ThemeOption>
    val defaultExportState: StateFlow<ExportUtils.ExportFormat>
    val defaultEccState: StateFlow<String>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = QrRepository(db.qrDao())

        historyList = repository.allHistory.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        favoriteList = repository.favorites.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

        themeState = prefManager.themeFlow.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeOption.SYSTEM
        )
        defaultExportState = prefManager.defaultExportFlow.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), ExportUtils.ExportFormat.PNG
        )
        defaultEccState = prefManager.defaultEccFlow.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), "M"
        )
    }

    // Selected QR Creation Type
    private val _selectedType = MutableStateFlow(QrType.URL)
    val selectedType: StateFlow<QrType> = _selectedType.asStateFlow()

    // Encoded QR Content & Title
    private val _encodedContent = MutableStateFlow("")
    val encodedContent: StateFlow<String> = _encodedContent.asStateFlow()

    private val _qrTitle = MutableStateFlow("")
    val qrTitle: StateFlow<String> = _qrTitle.asStateFlow()

    // Form Field States
    val urlInput = MutableStateFlow("")
    val textInput = MutableStateFlow("")
    val phoneInput = MutableStateFlow("")

    val smsPhoneInput = MutableStateFlow("")
    val smsMessageInput = MutableStateFlow("")

    val emailAddressInput = MutableStateFlow("")
    val emailSubjectInput = MutableStateFlow("")
    val emailBodyInput = MutableStateFlow("")

    val vcardFirstNameInput = MutableStateFlow("")
    val vcardLastNameInput = MutableStateFlow("")
    val vcardPhoneInput = MutableStateFlow("")
    val vcardEmailInput = MutableStateFlow("")
    val vcardOrgInput = MutableStateFlow("")
    val vcardNoteInput = MutableStateFlow("")

    val wifiSsidInput = MutableStateFlow("")
    val wifiPasswordInput = MutableStateFlow("")
    val wifiEncryptionInput = MutableStateFlow("WPA") // WPA, WEP, nopass
    val wifiHiddenInput = MutableStateFlow(false)

    val upiIdInput = MutableStateFlow("")
    val upiNameInput = MutableStateFlow("")
    val upiAmountInput = MutableStateFlow("")
    val upiNoteInput = MutableStateFlow("")

    val locationLatInput = MutableStateFlow("")
    val locationLngInput = MutableStateFlow("")
    val locationQueryInput = MutableStateFlow("")

    val eventTitleInput = MutableStateFlow("")
    val eventLocationInput = MutableStateFlow("")
    val eventDescriptionInput = MutableStateFlow("")
    val eventStartMillis = MutableStateFlow(System.currentTimeMillis() + 3600000)
    val eventEndMillis = MutableStateFlow(System.currentTimeMillis() + 7200000)

    // Customization Options
    private val _qrOptions = MutableStateFlow(QrOptions())
    val qrOptions: StateFlow<QrOptions> = _qrOptions.asStateFlow()

    // Rendered Bitmap State
    private val _generatedBitmap = MutableStateFlow<Bitmap?>(null)
    val generatedBitmap: StateFlow<Bitmap?> = _generatedBitmap.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    // Scanned Result State
    private val _scannedResult = MutableStateFlow<ScannedResult?>(null)
    val scannedResult: StateFlow<ScannedResult?> = _scannedResult.asStateFlow()

    // Editing Existing Qr Item ID
    private val _editingItemId = MutableStateFlow<Long?>(null)
    val editingItemId: StateFlow<Long?> = _editingItemId.asStateFlow()

    fun selectType(type: QrType) {
        _selectedType.value = type
        updateEncodedData()
    }

    fun updateEncodedData() {
        val type = _selectedType.value
        val (encoded, title) = when (type) {
            QrType.URL -> {
                val e = QrDataEncoder.encodeUrl(urlInput.value)
                e to (urlInput.value.takeIf { it.isNotBlank() } ?: "Website URL")
            }
            QrType.TEXT -> {
                val e = QrDataEncoder.encodeText(textInput.value)
                e to (textInput.value.take(25).ifBlank { "Text Note" })
            }
            QrType.PHONE -> {
                val e = QrDataEncoder.encodePhone(phoneInput.value)
                e to (phoneInput.value.ifBlank { "Phone Number" })
            }
            QrType.SMS -> {
                val e = QrDataEncoder.encodeSms(smsPhoneInput.value, smsMessageInput.value)
                e to "SMS to ${smsPhoneInput.value}"
            }
            QrType.EMAIL -> {
                val e = QrDataEncoder.encodeEmail(emailAddressInput.value, emailSubjectInput.value, emailBodyInput.value)
                e to "Email to ${emailAddressInput.value}"
            }
            QrType.VCARD -> {
                val e = QrDataEncoder.encodeVCard(
                    vcardFirstNameInput.value,
                    vcardLastNameInput.value,
                    vcardPhoneInput.value,
                    vcardEmailInput.value,
                    vcardOrgInput.value,
                    vcardNoteInput.value
                )
                val fn = "${vcardFirstNameInput.value} ${vcardLastNameInput.value}".trim()
                e to (fn.ifBlank { "Contact Card" })
            }
            QrType.WIFI -> {
                val e = QrDataEncoder.encodeWifi(
                    wifiSsidInput.value,
                    wifiPasswordInput.value,
                    wifiEncryptionInput.value,
                    wifiHiddenInput.value
                )
                e to "Wi-Fi: ${wifiSsidInput.value.ifBlank { "Network" }}"
            }
            QrType.UPI -> {
                val e = QrDataEncoder.encodeUpi(
                    upiIdInput.value,
                    upiNameInput.value,
                    upiAmountInput.value,
                    upiNoteInput.value
                )
                e to "UPI: ${upiNameInput.value.ifBlank { upiIdInput.value.ifBlank { "Payment" } }}"
            }
            QrType.LOCATION -> {
                val e = QrDataEncoder.encodeLocation(
                    locationLatInput.value,
                    locationLngInput.value,
                    locationQueryInput.value
                )
                e to "Map Location"
            }
            QrType.EVENT -> {
                val e = QrDataEncoder.encodeEvent(
                    eventTitleInput.value,
                    eventLocationInput.value,
                    eventDescriptionInput.value,
                    eventStartMillis.value,
                    eventEndMillis.value
                )
                e to (eventTitleInput.value.ifBlank { "Calendar Event" })
            }
        }

        _encodedContent.value = encoded
        _qrTitle.value = title
        regenerateBitmap()
    }

    fun updateOptions(transform: (QrOptions) -> QrOptions) {
        val oldOptions = _qrOptions.value
        var newOptions = transform(oldOptions)

        // Auto warn/upgrade ECC if logo occupies significant area
        if (newOptions.logoBitmap != null && newOptions.logoScalePercent >= 20 && newOptions.eccLevel in listOf(EccLevel.L, EccLevel.M)) {
            // Auto elevate ECC to H for maximum reliability with logos
            newOptions = newOptions.copy(eccLevel = EccLevel.H)
        }

        _qrOptions.value = newOptions
        regenerateBitmap()
    }

    fun setLogoFromUri(uri: Uri?) {
        if (uri == null) {
            updateOptions { it.copy(logoUri = null, logoBitmap = null) }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val bitmap = ExportUtils.decodeBitmapFromUri(getApplication(), uri, 512, 512)
            launch(Dispatchers.Main) {
                if (bitmap != null) {
                    updateOptions {
                        it.copy(
                            logoUri = uri.toString(),
                            logoBitmap = bitmap,
                            eccLevel = EccLevel.H // Default to High error correction when adding logo
                        )
                    }
                }
            }
        }
    }

    private fun regenerateBitmap() {
        val content = _encodedContent.value
        val options = _qrOptions.value

        viewModelScope.launch {
            _isGenerating.value = true
            val bmp = QrBitmapGenerator.generateQrBitmap(content, options, 1024)
            _generatedBitmap.value = bmp
            _isGenerating.value = false
        }
    }

    fun saveCurrentQrToHistory(onSaved: (Long) -> Unit = {}) {
        val content = _encodedContent.value
        if (content.isBlank()) return

        val title = _qrTitle.value
        val type = _selectedType.value
        val opts = _qrOptions.value

        viewModelScope.launch(Dispatchers.IO) {
            val entity = QrItemEntity(
                id = _editingItemId.value ?: 0L,
                title = title,
                qrType = type.name,
                content = content,
                displaySummary = content.take(100),
                timestamp = System.currentTimeMillis(),
                isFavorite = false,
                isGenerated = true,
                fgColorHex = String.format("#%06X", (0xFFFFFF and opts.fgColor)),
                bgColorHex = String.format("#%06X", (0xFFFFFF and opts.bgColor)),
                dotStyleName = opts.dotStyle.name,
                eyeStyleName = opts.eyeStyle.name,
                eccLevelName = opts.eccLevel.name,
                margin = opts.margin,
                logoUri = opts.logoUri
            )

            val id = repository.insert(entity)
            _editingItemId.value = id
            launch(Dispatchers.Main) {
                onSaved(id)
            }
        }
    }

    fun handleScannedCode(rawText: String) {
        val parsed = QrParser.parse(rawText)
        _scannedResult.value = parsed

        // Automatically save scanned item to Room history
        viewModelScope.launch(Dispatchers.IO) {
            val entity = QrItemEntity(
                title = parsed.title,
                qrType = parsed.type.name,
                content = parsed.rawContent,
                displaySummary = parsed.summary,
                timestamp = System.currentTimeMillis(),
                isFavorite = false,
                isGenerated = false
            )
            repository.insert(entity)
        }
    }

    fun clearScannedResult() {
        _scannedResult.value = null
    }

    fun toggleFavorite(item: QrItemEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(item.id, !item.isFavorite)
        }
    }

    fun deleteHistoryItem(item: QrItemEntity) {
        viewModelScope.launch {
            repository.delete(item)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun loadFromEntity(entity: QrItemEntity) {
        _editingItemId.value = entity.id
        val type = try { QrType.valueOf(entity.qrType) } catch (_: Exception) { QrType.TEXT }
        _selectedType.value = type

        when (type) {
            QrType.URL -> urlInput.value = entity.content
            QrType.TEXT -> textInput.value = entity.content
            QrType.PHONE -> phoneInput.value = entity.content.removePrefix("tel:")
            QrType.SMS -> {
                val clean = entity.content.removePrefix("smsto:")
                val parts = clean.split(":", limit = 2)
                smsPhoneInput.value = parts.getOrNull(0) ?: ""
                smsMessageInput.value = parts.getOrNull(1) ?: ""
            }
            QrType.EMAIL -> emailAddressInput.value = entity.content.removePrefix("mailto:")
            QrType.VCARD -> {
                val parsed = QrParser.parse(entity.content)
                vcardFirstNameInput.value = parsed.detailsMap["Name"] ?: ""
                vcardPhoneInput.value = parsed.detailsMap["Phone"] ?: ""
                vcardEmailInput.value = parsed.detailsMap["Email"] ?: ""
                vcardOrgInput.value = parsed.detailsMap["Organization"] ?: ""
                vcardNoteInput.value = parsed.detailsMap["Note"] ?: ""
            }
            QrType.WIFI -> {
                val parsed = QrParser.parse(entity.content)
                wifiSsidInput.value = parsed.detailsMap["SSID"] ?: ""
                wifiPasswordInput.value = parsed.detailsMap["Password"] ?: ""
            }
            QrType.UPI -> {
                val parsed = QrParser.parse(entity.content)
                upiIdInput.value = parsed.detailsMap["UPI ID"] ?: ""
                upiNameInput.value = parsed.detailsMap["Receiver"] ?: ""
                upiAmountInput.value = parsed.detailsMap["Amount"]?.removePrefix("₹") ?: ""
                upiNoteInput.value = parsed.detailsMap["Note"] ?: ""
            }
            QrType.LOCATION -> locationQueryInput.value = entity.content
            QrType.EVENT -> eventTitleInput.value = entity.title
        }

        val dotStyle = try { DotStyle.valueOf(entity.dotStyleName) } catch (_: Exception) { DotStyle.SQUARE }
        val eyeStyle = try { EyeStyle.valueOf(entity.eyeStyleName) } catch (_: Exception) { EyeStyle.SQUARE }
        val eccLevel = try { EccLevel.valueOf(entity.eccLevelName) } catch (_: Exception) { EccLevel.M }

        val fg = try { android.graphics.Color.parseColor(entity.fgColorHex) } catch (_: Exception) { android.graphics.Color.BLACK }
        val bg = try { android.graphics.Color.parseColor(entity.bgColorHex) } catch (_: Exception) { android.graphics.Color.WHITE }

        _qrOptions.value = QrOptions(
            fgColor = fg,
            bgColor = bg,
            dotStyle = dotStyle,
            eyeStyle = eyeStyle,
            eccLevel = eccLevel,
            margin = entity.margin,
            logoUri = entity.logoUri
        )

        if (!entity.logoUri.isNullOrBlank()) {
            setLogoFromUri(Uri.parse(entity.logoUri))
        }

        updateEncodedData()
    }

    fun setThemePreference(option: ThemeOption) {
        viewModelScope.launch {
            prefManager.setTheme(option)
        }
    }

    fun setDefaultExportFormat(format: ExportUtils.ExportFormat) {
        viewModelScope.launch {
            prefManager.setDefaultExport(format)
        }
    }

    fun setDefaultEccLevel(ecc: String) {
        viewModelScope.launch {
            prefManager.setDefaultEcc(ecc)
        }
    }
}
