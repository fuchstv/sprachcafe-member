package org.sprachcafe.member.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import org.sprachcafe.member.data.MemberApiClient
import org.sprachcafe.member.data.MemberPreferences
import org.sprachcafe.member.data.MemberProfile
import org.sprachcafe.member.ui.components.QrCodeGenerator
import org.sprachcafe.member.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberCardScreen(
    profile: MemberProfile,
    onProfileUpdated: (MemberProfile) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefs = remember { MemberPreferences.getInstance(context) }

    var isRefreshing by remember { mutableStateOf(false) }
    var isFullScreenQr by remember { mutableStateOf(false) }

    val qrBitmap = remember(profile.qrToken) {
        try {
            QrCodeGenerator.generate(profile.qrToken, 600)
        } catch (e: Exception) {
            null
        }
    }

    val cardBrush = remember(profile.tier) {
        when (profile.tier.uppercase()) {
            "PLATINUM" -> Brush.linearGradient(listOf(PlatinumGradientStart, PlatinumGradientEnd))
            "GOLD" -> Brush.linearGradient(listOf(GoldGradientStart, GoldGradientEnd))
            "COMPANY" -> Brush.linearGradient(listOf(CompanyGradientStart, CompanyGradientEnd))
            else -> Brush.linearGradient(listOf(SilverGradientStart, SilverGradientEnd))
        }
    }

    fun doRefresh() {
        isRefreshing = true
        coroutineScope.launch {
            val token = prefs.savedToken
            val res = if (!token.isNullOrEmpty()) {
                MemberApiClient.fetchMemberByToken(token)
            } else {
                MemberApiClient.refreshMemberByQr(profile.qrToken)
            }

            isRefreshing = false
            if (res.isSuccess) {
                val updated = res.getOrThrow()
                prefs.saveProfile(updated)
                onProfileUpdated(updated)
                Toast.makeText(context, "Mitgliedsdaten aktualisiert", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Konnte nicht aktualisieren: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("☕", fontSize = 20.sp)
                        Text(
                            text = "SprachCafé Polnisch e.V.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = SprachCafeRed
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { doRefresh() }, enabled = !isRefreshing) {
                        if (isRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = SprachCafeRed)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Aktualisieren", tint = Color.Gray)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SprachCafeCream)
            )
        },
        containerColor = SprachCafeCream
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 1. Digital Member Card (Plastic Card Visual)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
                shadowElevation = 10.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(cardBrush)
                        .padding(22.dp)
                ) {
                    // Header inside card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("☕", fontSize = 18.sp)
                            Text(
                                text = "MITGLIEDSAUSWEIS",
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = profile.tier.uppercase(),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // Member Name & Number (Center)
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(top = 10.dp)
                    ) {
                        Text(
                            text = profile.fullName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = profile.memberNumber,
                            color = Color.White.copy(alpha = 0.8f),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    // Footer Row inside card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "KAFFEE-KONTINGENT",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "☕ ${profile.coffeeRemaining} / ${profile.coffeeTotal} verfügbar",
                                color = Color(0xFFFDE047),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "GÜLTIG BIS",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = profile.validUntil ?: "31.12.2026",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 2. Barcode / QR-Code Card for Scanning
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2D8CC)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Dein Scan-Code für Theke & Bibliothek",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF1D1B1A)
                    )
                    Text(
                        text = "Beim Bezahlen oder Ausleihen einfach vorzeigen. Tippe zum Vergrößern.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    if (qrBitmap != null) {
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(2.dp, Color(0xFFE2D8CC), RoundedCornerShape(16.dp))
                                .clickable { isFullScreenQr = true }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "Mitglieds-QR-Code",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Text(
                        text = profile.qrToken,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }

            // 3. Google Wallet Button
            Button(
                onClick = {
                    val walletUrl = "https://team.xn--sprachcaf-j4a.org/member/activate?token=${prefs.savedToken ?: profile.memberNumber}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(walletUrl))
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("💳", fontSize = 18.sp)
                    Text(
                        text = "Zu Google Wallet hinzufügen",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // 4. Benefits Overview
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2D8CC)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "DEINE MITGLIEDSVORTEILE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = SprachCafeRed,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BenefitCard(
                            modifier = Modifier.weight(1f),
                            icon = "☕",
                            title = "Freikaffees",
                            subtitle = "${profile.coffeeRemaining} übrig (${profile.coffeeTotal}/Jahr)"
                        )
                        BenefitCard(
                            modifier = Modifier.weight(1f),
                            icon = "🎟️",
                            title = "Event-Rabatt",
                            subtitle = "${profile.eventDiscount}% Rabatt"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BenefitCard(
                            modifier = Modifier.weight(1f),
                            icon = "📚",
                            title = "Hausbibliothek",
                            subtitle = "0 € Jahresgebühr"
                        )
                        BenefitCard(
                            modifier = Modifier.weight(1f),
                            icon = "🏛️",
                            title = "Raumnutzung",
                            subtitle = if (profile.tier.equals("PLATINUM", true)) "2x bis 6 Std. frei" else (if (profile.tier.equals("GOLD", true)) "1x bis 6 Std. frei" else "Auf Anfrage")
                        )
                    }
                }
            }

            // Footer Logout / Switch account
            TextButton(
                onClick = {
                    prefs.clear()
                    onLogout()
                }
            ) {
                Text(
                    text = "Mit anderem Ausweis anmelden",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // Full-screen QR Dialog for Counter Scanning
    if (isFullScreenQr && qrBitmap != null) {
        Dialog(
            onDismissRequest = { isFullScreenQr = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .clickable { isFullScreenQr = false }
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = profile.fullName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "${profile.memberNumber} • ${profile.tier}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )

                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Großer QR-Code",
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .aspectRatio(1f)
                    )

                    Text(
                        text = "Tippe irgendwo zum Schließen",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun BenefitCard(
    modifier: Modifier = Modifier,
    icon: String,
    title: String,
    subtitle: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFFAF5EB),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2D8CC))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1D1B1A))
            Text(text = subtitle, fontSize = 10.sp, color = Color.Gray)
        }
    }
}
