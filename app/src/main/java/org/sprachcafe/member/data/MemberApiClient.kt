package org.sprachcafe.member.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object MemberApiClient {

    private const val BASE_URL = "https://team.xn--sprachcaf-j4a.org/api"
    private const val TIMEOUT_MS = 6000

    suspend fun fetchMemberByToken(token: String): Result<MemberProfile> = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(token, "UTF-8")
            val url = URL("$BASE_URL/member/activate/$encoded")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = TIMEOUT_MS
            conn.readTimeout = TIMEOUT_MS
            conn.requestMethod = "GET"

            if (conn.responseCode == 200) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val j = JSONObject(body)
                Result.success(parseProfile(j))
            } else {
                val err = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                val msg = try { JSONObject(err).optString("error", "Ungültiger Link") } catch (_: Exception) { "HTTP ${conn.responseCode}" }
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun confirmActivation(token: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(token, "UTF-8")
            val url = URL("$BASE_URL/member/activate/$encoded/confirm")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = TIMEOUT_MS
            conn.readTimeout = TIMEOUT_MS
            conn.requestMethod = "POST"

            if (conn.responseCode in 200..204) {
                Result.success(true)
            } else {
                Result.failure(Exception("HTTP ${conn.responseCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshMemberByQr(qrToken: String): Result<MemberProfile> = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(qrToken, "UTF-8")
            val url = URL("$BASE_URL/club-members/verify/$encoded")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = TIMEOUT_MS
            conn.readTimeout = TIMEOUT_MS
            conn.requestMethod = "GET"

            if (conn.responseCode == 200) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(body)
                val m = json.getJSONObject("member")
                val nameParts = m.getString("name").split(" ", limit = 2)
                val first = nameParts.getOrNull(0) ?: ""
                val last = nameParts.getOrNull(1) ?: ""

                val tier = m.getString("tier")
                val totalDefaults = mapOf("SILVER" to 4, "GOLD" to 8, "PLATINUM" to 10, "COMPANY" to 10)
                val total = totalDefaults[tier] ?: 4

                Result.success(
                    MemberProfile(
                        memberNumber = m.getString("member_number"),
                        firstName = first,
                        lastName = last,
                        email = "",
                        tier = tier,
                        status = m.getString("status"),
                        coffeeRemaining = m.getInt("coffee_quota_remaining"),
                        coffeeTotal = total,
                        eventDiscount = m.optInt("event_discount_pct", 0),
                        validUntil = m.optString("valid_until").takeIf { it.isNotEmpty() },
                        qrToken = qrToken,
                        activatedAt = "active"
                    )
                )
            } else {
                Result.failure(Exception("HTTP ${conn.responseCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseProfile(j: JSONObject): MemberProfile {
        return MemberProfile(
            memberNumber = j.getString("memberNumber"),
            firstName = j.getString("firstName"),
            lastName = j.getString("lastName"),
            email = j.optString("email", ""),
            tier = j.getString("tier"),
            status = j.getString("status"),
            coffeeRemaining = j.getInt("coffeeRemaining"),
            coffeeTotal = j.getInt("coffeeTotal"),
            eventDiscount = j.optInt("eventDiscount", 0),
            validUntil = j.optString("validUntil").takeIf { it.isNotEmpty() },
            qrToken = j.getString("qrToken"),
            activatedAt = j.optString("activatedAt").takeIf { it.isNotEmpty() && it != "null" }
        )
    }
}
