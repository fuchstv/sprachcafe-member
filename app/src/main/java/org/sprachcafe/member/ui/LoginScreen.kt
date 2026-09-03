package org.sprachcafe.member.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.sprachcafe.member.data.MemberApiClient
import org.sprachcafe.member.data.MemberPreferences
import org.sprachcafe.member.data.MemberProfile
import org.sprachcafe.member.ui.theme.SprachCafeCream
import org.sprachcafe.member.ui.theme.SprachCafeRed

@Composable
fun LoginScreen(
    initialToken: String? = null,
    onLoginSuccess: (MemberProfile) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefs = remember { MemberPreferences.getInstance(context) }

    var tokenInput by remember { mutableStateOf(initialToken ?: "") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun processLogin(tokenToUse: String) {
        val cleanToken = tokenToUse.trim().let { raw ->
            if (raw.contains("token=")) {
                raw.substringAfter("token=").substringBefore("&")
            } else if (raw.startsWith("http")) {
                raw.substringAfterLast("/")
            } else {
                raw
            }
        }

        if (cleanToken.isBlank()) {
            errorMessage = "Bitte gib deinen Aktivierungstoken ein."
            return
        }

        isLoading = true
        errorMessage = null

        coroutineScope.launch {
            val res = MemberApiClient.fetchMemberByToken(cleanToken)
            isLoading = false
            if (res.isSuccess) {
                val profile = res.getOrThrow()
                prefs.savedToken = cleanToken
                prefs.saveProfile(profile)
                // Confirm activation asynchronously
                MemberApiClient.confirmActivation(cleanToken)
                Toast.makeText(context, "Willkommen, ${profile.firstName}!", Toast.LENGTH_SHORT).show()
                onLoginSuccess(profile)
            } else {
                errorMessage = res.exceptionOrNull()?.message ?: "Ungültiger Token oder Verbindungsfehler."
            }
        }
    }

    LaunchedEffect(initialToken) {
        if (!initialToken.isNullOrBlank()) {
            processLogin(initialToken)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SprachCafeCream)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2D8CC)),
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SprachCafeRed,
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("☕", fontSize = 28.sp)
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SprachCafé Polnisch e.V.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SprachCafeRed
                    )
                    Text(
                        text = "Digitaler Mitgliedsausweis",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                Text(
                    text = "Gib deinen persönlichen Aktivierungslink oder Token aus deiner Einladungs-E-Mail ein, um deinen Mitgliedsausweis auf diesem Smartphone zu laden.",
                    fontSize = 12.sp,
                    color = Color(0xFF5B403D),
                    lineHeight = 18.sp
                )

                OutlinedTextField(
                    value = tokenInput,
                    onValueChange = { tokenInput = it },
                    label = { Text("Aktivierungs-Token / Link") },
                    placeholder = { Text("z. B. inv_xxxxx oder Link einfügen") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { processLogin(tokenInput) },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SprachCafeRed)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Ausweis aktivieren & laden", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Text(
                    text = "Noch keine Einladung erhalten? Wende dich bitte an vorstand@sprachcafe-polnisch.org.",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
