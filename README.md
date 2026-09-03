# SprachCafé Polnisch e.V. — Offizielle Mitglieder-App & Digitaler Ausweis

Moderne Android-App für Vereinsmitglieder des **SprachCafé Polnisch e.V.** (Berlin-Pankow) mit digitalem Mitgliedsausweis, Google Wallet Integration, Live-Kaffeekontingent und Rabatt-Tracking.

## ✨ Kernfunktionen

1. **💳 Digitaler Mitgliedsausweis:**
   * Individuelles Kartendesign für die Mitgliedsstufen:
     * ⚪ **Silver** (50 € / 4 Freikaffees / 5 % Event-Rabatt)
     * 🟡 **Gold** (120 € / 8 Freikaffees / 10 % Event-Rabatt / 1x 6h Raum)
     * 🟣 **Platinum** (200 € / 10 Freikaffees / 15 % Event-Rabatt / 2x 6h Raum)
     * 🏢 **Firmen** (ab 300 € / 10 Freikaffees / 20 % Event-Rabatt / 3x 6h Raum)
2. **☕ Live-Kaffeeguthaben:**
   * Restanzeige der jährlichen Freikaffees mit Fortschrittsbalken.
   * Direkte Abrechnung an der Café-Theke per 1-Klick GoBD `0,00 €` Verbuchung.
3. **🔍 QR-Code Vollbild-Scan:**
   * Sofortige Erkennung durch die Kassen-App (`sprachcafe-android`) und die Hausbibliothek ([hausbibliothek.org](https://hausbibliothek.org/)).
   * Tippen auf den QR-Code öffnet maximale Helligkeit für schnelles Theken-Scanning.
4. **💳 Google Wallet:**
   * 1-Klick Export des Passes direkt in die Google Wallet App auf dem Smartphone.
5. **🔐 Magic Link Login:**
   * Passwortloser Zugang über den persönlichen Einladungslink per E-Mail.
   * Offline-fähig durch verschlüsselte lokale Zwischenspeicherung.

## 🛠️ Technologie & Architektur

* **UI Framework:** Jetpack Compose (Material 3)
* **Sprache:** Kotlin 2.0.20
* **QR-Code:** ZXing Core (100% on-device)
* **Build System:** Gradle Kotlin DSL (AGP 8.5.2)
* **CI/CD:** GitHub Actions Headless Cloud Build (`.aab` & `.apk`)
* **Backend API:** Express REST API auf `https://team.xn--sprachcaf-j4a.org/api`

## 🚀 Google Play Release (Headless)

Die App wird vollautomatisch bei jedem Push auf den `main`-Branch über GitHub Actions signiert und als Release bereitgestellt:
* **`app-release.aab`:** Android App Bundle für die Google Play Console (Closed Testing Track).
* **`app-release.apk`:** Direkte APK-Datei für manuelles Testen.
