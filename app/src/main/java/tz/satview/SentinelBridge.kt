package tz.satview

import android.content.Context
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebView
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.concurrent.thread

/**
 * Talks to the Copernicus Data Space Ecosystem (Sentinel Hub Process API).
 *
 * 1. Gets a login token using your OAuth client ID + secret.
 * 2. Sends the image request built by the web page.
 * 3. Returns the PNG picture (as base64 text) back to the web page.
 */
class SentinelBridge(context: Context, private val webView: WebView) {

    private val prefs = context.getSharedPreferences("satview", Context.MODE_PRIVATE)
    private var token: String? = null
    private var tokenExpiresAt = 0L

    // ---------- Credentials ----------

    @JavascriptInterface
    fun saveCredentials(clientId: String, clientSecret: String) {
        prefs.edit()
            .putString("client_id", clientId.trim())
            .putString("client_secret", clientSecret.trim())
            .apply()
        token = null // force a fresh login with the new keys
    }

    @JavascriptInterface
    fun hasCredentials(): Boolean =
        !prefs.getString("client_id", "").isNullOrEmpty() &&
            !prefs.getString("client_secret", "").isNullOrEmpty()

    @JavascriptInterface
    fun getClientId(): String = prefs.getString("client_id", "") ?: ""

    // ---------- Image request ----------

    /** Called from JavaScript. Runs in the background so the app never freezes. */
    @JavascriptInterface
    fun process(requestJson: String, callbackId: String) {
        thread {
            try {
                val png = postProcess(requestJson)
                val b64 = Base64.encodeToString(png, Base64.NO_WRAP)
                reply(callbackId, true, b64)
            } catch (e: Exception) {
                reply(callbackId, false, e.message ?: e.toString())
            }
        }
    }

    private fun reply(callbackId: String, ok: Boolean, data: String) {
        val js = "window.onSatResult(${JSONObject.quote(callbackId)}, $ok, ${JSONObject.quote(data)})"
        webView.post { webView.evaluateJavascript(js, null) }
    }

    private fun getToken(): String {
        val now = System.currentTimeMillis()
        token?.let { if (now < tokenExpiresAt) return it }

        val id = prefs.getString("client_id", "") ?: ""
        val secret = prefs.getString("client_secret", "") ?: ""
        if (id.isEmpty() || secret.isEmpty()) {
            throw Exception("Add your Copernicus client ID and secret in Settings (⚙).")
        }

        val body = "grant_type=client_credentials" +
            "&client_id=" + URLEncoder.encode(id, "UTF-8") +
            "&client_secret=" + URLEncoder.encode(secret, "UTF-8")

        val conn = URL(TOKEN_URL).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.connectTimeout = 20000
        conn.readTimeout = 30000
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        conn.outputStream.use { it.write(body.toByteArray()) }

        val code = conn.responseCode
        val text = readAll(if (code in 200..299) conn.inputStream else conn.errorStream)
            .toString(Charsets.UTF_8)
        conn.disconnect()
        if (code !in 200..299) {
            throw Exception("Login failed ($code). Check your client ID and secret. $text")
        }

        val json = JSONObject(text)
        val t = json.getString("access_token")
        val expiresIn = json.optLong("expires_in", 600)
        token = t
        tokenExpiresAt = now + (expiresIn - 60) * 1000 // refresh a minute early
        return t
    }

    private fun postProcess(requestJson: String): ByteArray {
        val conn = URL(PROCESS_URL).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.connectTimeout = 20000
        conn.readTimeout = 90000
        conn.doOutput = true
        conn.setRequestProperty("Authorization", "Bearer " + getToken())
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Accept", "image/png")
        conn.outputStream.use { it.write(requestJson.toByteArray(Charsets.UTF_8)) }

        val code = conn.responseCode
        if (code !in 200..299) {
            val err = readAll(conn.errorStream).toString(Charsets.UTF_8)
            conn.disconnect()
            if (code == 401) token = null
            val msg = try {
                JSONObject(err).optJSONObject("error")?.optString("message") ?: err
            } catch (_: Exception) { err }
            throw Exception("Copernicus error ($code): $msg")
        }
        val bytes = readAll(conn.inputStream)
        conn.disconnect()
        return bytes
    }

    private fun readAll(stream: InputStream?): ByteArray {
        if (stream == null) return ByteArray(0)
        val out = ByteArrayOutputStream()
        stream.use { it.copyTo(out) }
        return out.toByteArray()
    }

    companion object {
        private const val TOKEN_URL =
            "https://identity.dataspace.copernicus.eu/auth/realms/CDSE/protocol/openid-connect/token"
        private const val PROCESS_URL =
            "https://sh.dataspace.copernicus.eu/api/v1/process"
    }
}
