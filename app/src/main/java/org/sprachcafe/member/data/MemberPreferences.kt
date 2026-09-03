package org.sprachcafe.member.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

class MemberPreferences private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("sprachcafe_member_prefs", Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var instance: MemberPreferences? = null

        fun getInstance(context: Context): MemberPreferences {
            return instance ?: synchronized(this) {
                instance ?: MemberPreferences(context.applicationContext).also { instance = it }
            }
        }
    }

    var savedToken: String?
        get() = prefs.getString("saved_invite_token", null)
        set(value) = prefs.edit().putString("saved_invite_token", value).apply()

    fun saveProfile(profile: MemberProfile) {
        val j = JSONObject().apply {
            put("memberNumber", profile.memberNumber)
            put("firstName", profile.firstName)
            put("lastName", profile.lastName)
            put("email", profile.email)
            put("tier", profile.tier)
            put("status", profile.status)
            put("coffeeRemaining", profile.coffeeRemaining)
            put("coffeeTotal", profile.coffeeTotal)
            put("eventDiscount", profile.eventDiscount)
            put("validUntil", profile.validUntil ?: "")
            put("qrToken", profile.qrToken)
            put("activatedAt", profile.activatedAt ?: "")
        }
        prefs.edit().putString("saved_profile_json", j.toString()).apply()
    }

    fun getProfile(): MemberProfile? {
        val str = prefs.getString("saved_profile_json", null) ?: return null
        return try {
            val j = JSONObject(str)
            MemberProfile(
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
                activatedAt = j.optString("activatedAt").takeIf { it.isNotEmpty() }
            )
        } catch (e: Exception) {
            null
        }
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
