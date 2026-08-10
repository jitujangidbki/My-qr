package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("QR Studio", appName)
  }

  @Test
  fun `verify url encoding and parsing`() {
    val rawUrl = "google.com"
    val encoded = com.example.generator.QrDataEncoder.encodeUrl(rawUrl)
    assertEquals("https://google.com", encoded)

    val parsed = com.example.generator.QrParser.parse(encoded)
    assertEquals(com.example.generator.QrType.URL, parsed.type)
    assertEquals("https://google.com", parsed.summary)
  }

  @Test
  fun `verify upi encoding and parsing`() {
    val encoded = com.example.generator.QrDataEncoder.encodeUpi(
      upiId = "merchant@upi",
      name = "Store Name",
      amount = "500",
      note = "Dinner"
    )
    val parsed = com.example.generator.QrParser.parse(encoded)
    assertEquals(com.example.generator.QrType.UPI, parsed.type)
    assertEquals("merchant@upi", parsed.detailsMap["UPI ID"])
  }
}
