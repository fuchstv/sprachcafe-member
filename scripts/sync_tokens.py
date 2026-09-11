#!/usr/bin/env python3
"""
Sync Design Tokens (JSON) -> Jetpack Compose (Kotlin) & Figma Variables
SprachCafé Polnisch e.V. — Member App
"""

import json
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
TOKENS_FILE = REPO_ROOT / "design-tokens" / "tokens.json"
COLOR_KT = REPO_ROOT / "app" / "src" / "main" / "java" / "org" / "sprachcafe" / "member" / "ui" / "theme" / "Color.kt"
DIMENS_KT = REPO_ROOT / "app" / "src" / "main" / "java" / "org" / "sprachcafe" / "member" / "ui" / "theme" / "Dimens.kt"
FIGMA_JSON = REPO_ROOT / "design-tokens" / "figma" / "figma-variables.json"


def hex_to_compose_color(hex_str: str) -> str:
    cleaned = hex_str.strip().lstrip("#")
    if len(cleaned) == 6:
        return f"0xFF{cleaned.upper()}"
    elif len(cleaned) == 8:
        return f"0x{cleaned.upper()}"
    return f"0xFF{cleaned.upper()}"


def generate_color_kt(tokens: dict) -> str:
    color = tokens["color"]
    brand = color["brand"]
    neutral = color["neutral"]
    tier = color["tier"]
    status = color.get("status", {})

    return f"""package org.sprachcafe.member.ui.theme

import androidx.compose.ui.graphics.Color

// GENERATED FROM design-tokens/tokens.json — DO NOT EDIT MANUALLY
val SprachCafeRed = Color({hex_to_compose_color(brand['red']['$value'])})
val SprachCafeDarkRed = Color({hex_to_compose_color(brand['darkRed']['$value'])})
val SprachCafeGold = Color({hex_to_compose_color(brand['gold']['$value'])})
val SprachCafeCream = Color({hex_to_compose_color(brand['cream']['$value'])})
val CardBackground = Color({hex_to_compose_color(neutral.get('cardBackground', neutral['dark'])['$value'])})

val SilverGradientStart = Color({hex_to_compose_color(tier['silver']['gradientStart']['$value'])})
val SilverGradientEnd = Color({hex_to_compose_color(tier['silver']['gradientEnd']['$value'])})

val GoldGradientStart = Color({hex_to_compose_color(tier['gold']['gradientStart']['$value'])})
val GoldGradientEnd = Color({hex_to_compose_color(tier['gold']['gradientEnd']['$value'])})

val PlatinumGradientStart = Color({hex_to_compose_color(tier['platinum']['gradientStart']['$value'])})
val PlatinumGradientEnd = Color({hex_to_compose_color(tier['platinum']['gradientEnd']['$value'])})

val CompanyGradientStart = Color({hex_to_compose_color(tier['company']['gradientStart']['$value'])})
val CompanyGradientEnd = Color({hex_to_compose_color(tier['company']['gradientEnd']['$value'])})

val CoffeeHighlight = Color({hex_to_compose_color(status.get('coffeeHighlight', {}).get('$value', '#FDE047'))})
"""


def generate_dimens_kt(tokens: dict) -> str:
    dimen = tokens["dimension"]
    radius = dimen.get("radius", {})
    spacing = dimen.get("spacing", {})
    card = dimen.get("card", {})

    def parse_dp(val_str: str) -> str:
        return val_str.replace("px", "").replace("dp", "")

    return f"""package org.sprachcafe.member.ui.theme

import androidx.compose.ui.unit.dp

// GENERATED FROM design-tokens/tokens.json — DO NOT EDIT MANUALLY
object Dimens {{
    val CardCornerRadius = {parse_dp(radius.get('card', {}).get('$value', '24px'))}.dp
    val ModalCornerRadius = {parse_dp(radius.get('modal', {}).get('$value', '28px'))}.dp
    val BenefitCornerRadius = {parse_dp(radius.get('benefit', {}).get('$value', '14px'))}.dp
    val ButtonCornerRadius = {parse_dp(radius.get('button', {}).get('$value', '16px'))}.dp
    val QrCornerRadius = {parse_dp(radius.get('qr', {}).get('$value', '16px'))}.dp

    val SpacingXs = {parse_dp(spacing.get('xs', {}).get('$value', '4px'))}.dp
    val SpacingSm = {parse_dp(spacing.get('sm', {}).get('$value', '8px'))}.dp
    val SpacingMd = {parse_dp(spacing.get('md', {}).get('$value', '12px'))}.dp
    val SpacingBase = {parse_dp(spacing.get('base', {}).get('$value', '16px'))}.dp
    val SpacingLg = {parse_dp(spacing.get('lg', {}).get('$value', '20px'))}.dp
    val SpacingXl = {parse_dp(spacing.get('xl', {}).get('$value', '24px'))}.dp

    val CardHeight = {parse_dp(card.get('height', {}).get('$value', '210px'))}.dp
    val CardBorderWidth = {parse_dp(card.get('borderWidth', {}).get('$value', '2px'))}.dp
}}
"""


def main():
    if not TOKENS_FILE.exists():
        print(f"Error: {TOKENS_FILE} does not exist", file=sys.stderr)
        sys.exit(1)

    with open(TOKENS_FILE, "r", encoding="utf-8") as f:
        tokens = json.load(f)

    color_kt_content = generate_color_kt(tokens)
    dimens_kt_content = generate_dimens_kt(tokens)

    if "--check" in sys.argv:
        mismatch = False
        if COLOR_KT.exists():
            current_color = COLOR_KT.read_text(encoding="utf-8")
            if current_color.strip() != color_kt_content.strip():
                print("Mismatch in Color.kt with tokens.json", file=sys.stderr)
                mismatch = True
        else:
            mismatch = True

        if DIMENS_KT.exists():
            current_dimens = DIMENS_KT.read_text(encoding="utf-8")
            if current_dimens.strip() != dimens_kt_content.strip():
                print("Mismatch in Dimens.kt with tokens.json", file=sys.stderr)
                mismatch = True
        else:
            mismatch = True

        if mismatch:
            print("Run python3 scripts/sync_tokens.py to update Kotlin theme files.", file=sys.stderr)
            sys.exit(1)
        else:
            print("All tokens are up to date.")
            sys.exit(0)

    # Write Color.kt
    COLOR_KT.parent.mkdir(parents=True, exist_ok=True)
    with open(COLOR_KT, "w", encoding="utf-8") as f:
        f.write(color_kt_content)
    print(f"Updated {COLOR_KT}")

    # Write Dimens.kt
    with open(DIMENS_KT, "w", encoding="utf-8") as f:
        f.write(dimens_kt_content)
    print(f"Updated {DIMENS_KT}")


if __name__ == "__main__":
    main()
