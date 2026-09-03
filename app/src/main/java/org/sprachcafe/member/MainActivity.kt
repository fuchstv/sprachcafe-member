package org.sprachcafe.member

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import org.sprachcafe.member.data.MemberPreferences
import org.sprachcafe.member.data.MemberProfile
import org.sprachcafe.member.ui.LoginScreen
import org.sprachcafe.member.ui.MemberCardScreen
import org.sprachcafe.member.ui.theme.SprachCafeMemberTheme

class MainActivity : ComponentActivity() {

    private lateinit var prefs: MemberPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prefs = MemberPreferences.getInstance(this)

        checkForUpdates()

        val deepLinkToken = extractTokenFromIntent(intent)

        setContent {
            SprachCafeMemberTheme {
                var currentProfile by remember { mutableStateOf(prefs.getProfile()) }

                if (currentProfile != null) {
                    MemberCardScreen(
                        profile = currentProfile!!,
                        onProfileUpdated = { currentProfile = it },
                        onLogout = { currentProfile = null }
                    )
                } else {
                    LoginScreen(
                        initialToken = deepLinkToken,
                        onLoginSuccess = { currentProfile = it }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val deepLinkToken = extractTokenFromIntent(intent)
        if (!deepLinkToken.isNullOrBlank()) {
            setContent {
                SprachCafeMemberTheme {
                    var currentProfile by remember { mutableStateOf<MemberProfile?>(null) }
                    LoginScreen(
                        initialToken = deepLinkToken,
                        onLoginSuccess = { currentProfile = it }
                    )
                }
            }
        }
    }

    private fun extractTokenFromIntent(intent: Intent?): String? {
        val uri = intent?.data ?: return null
        return uri.getQueryParameter("token")
            ?: uri.lastPathSegment?.takeIf { it != "activate" }
    }

    private fun checkForUpdates() {
        try {
            val appUpdateManager = AppUpdateManagerFactory.create(this)
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo
            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                ) {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this,
                        9001
                    )
                }
            }
        } catch (_: Exception) {
            // Ignored if not installed from Google Play yet
        }
    }
}
