package com.barbosa.marketcompare.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// ============================================================================
// Material Design 3 — Light Scheme Tokens
// Palette: Deep Pine (Primary), Warm Amber (Secondary), Electric Indigo (Tertiary)
// ============================================================================

val PrimaryLight = Color(0xFF006A60)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFF73F8E6)
val OnPrimaryContainerLight = Color(0xFF00201C)
val InversePrimaryLight = Color(0xFF53DBC9)

val SecondaryLight = Color(0xFF7C5800)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFFFDF9E)
val OnSecondaryContainerLight = Color(0xFF261900)

val TertiaryLight = Color(0xFF4355B9)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFDEE0FF)
val OnTertiaryContainerLight = Color(0xFF00105C)

val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

val BackgroundLight = Color(0xFFF4FAF7)
val OnBackgroundLight = Color(0xFF161D1B)
val SurfaceLight = Color(0xFFF4FAF7)
val OnSurfaceLight = Color(0xFF161D1B)
val SurfaceVariantLight = Color(0xFFDAE5E1)
val OnSurfaceVariantLight = Color(0xFF3F4946)

val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFEEF5F1)
val SurfaceContainerLight = Color(0xFFE8EFEB)
val SurfaceContainerHighLight = Color(0xFFE2E9E6)
val SurfaceContainerHighestLight = Color(0xFFDCE4E0)

val OutlineLight = Color(0xFF6F7976)
val OutlineVariantLight = Color(0xFFBEC9C5)
val ScrimLight = Color(0xFF000000)
val InverseSurfaceLight = Color(0xFF2B3230)
val InverseOnSurfaceLight = Color(0xFFECF2EE)

// ============================================================================
// Material Design 3 — Dark Scheme Tokens
// ============================================================================

val PrimaryDark = Color(0xFF53DBC9)
val OnPrimaryDark = Color(0xFF003731)
val PrimaryContainerDark = Color(0xFF005048)
val OnPrimaryContainerDark = Color(0xFF73F8E6)
val InversePrimaryDark = Color(0xFF006A60)

val SecondaryDark = Color(0xFFFBBB42)
val OnSecondaryDark = Color(0xFF412D00)
val SecondaryContainerDark = Color(0xFF5E4200)
val OnSecondaryContainerDark = Color(0xFFFFDF9E)

val TertiaryDark = Color(0xFFB9C3FF)
val OnTertiaryDark = Color(0xFF08218A)
val TertiaryContainerDark = Color(0xFF2A3C9F)
val OnTertiaryContainerDark = Color(0xFFDEE0FF)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

val BackgroundDark = Color(0xFF0E1513)
val OnBackgroundDark = Color(0xFFDEE4E1)
val SurfaceDark = Color(0xFF0E1513)
val OnSurfaceDark = Color(0xFFDEE4E1)
val SurfaceVariantDark = Color(0xFF3F4946)
val OnSurfaceVariantDark = Color(0xFFBEC9C5)

val SurfaceContainerLowestDark = Color(0xFF09100E)
val SurfaceContainerLowDark = Color(0xFF161D1B)
val SurfaceContainerDark = Color(0xFF1A211F)
val SurfaceContainerHighDark = Color(0xFF252C2A)
val SurfaceContainerHighestDark = Color(0xFF303734)

val OutlineDark = Color(0xFF89938F)
val OutlineVariantDark = Color(0xFF3F4946)
val ScrimDark = Color(0xFF000000)
val InverseSurfaceDark = Color(0xFFDEE4E1)
val InverseOnSurfaceDark = Color(0xFF2B3230)

// ============================================================================
// Semantic Tokens (OCR Overlays & Price Freshness)
// ============================================================================

@Immutable
data class MarketCompareSemanticColors(
    val ocrStoreBlue: Color,
    val ocrProductGreen: Color,
    val ocrPriceGold: Color,
    val ocrQuantityPurple: Color,
    val ocrDiscountRed: Color,
    val freshnessRecent: Color,
    val freshnessStale: Color,
    val freshnessUnknown: Color
)

val SemanticColorsLight = MarketCompareSemanticColors(
    ocrStoreBlue = Color(0xFF1565C0),
    ocrProductGreen = Color(0xFF2E7D32),
    ocrPriceGold = Color(0xFFC67C00),
    ocrQuantityPurple = Color(0xFF6A1B9A),
    ocrDiscountRed = Color(0xFFC62828),
    freshnessRecent = Color(0xFF2E7D32),
    freshnessStale = Color(0xFFE65100),
    freshnessUnknown = Color(0xFF616161)
)

val SemanticColorsDark = MarketCompareSemanticColors(
    ocrStoreBlue = Color(0xFF90CAF9),
    ocrProductGreen = Color(0xFF81C784),
    ocrPriceGold = Color(0xFFFFD54F),
    ocrQuantityPurple = Color(0xFFCE93D8),
    ocrDiscountRed = Color(0xFFE57373),
    freshnessRecent = Color(0xFF81C784),
    freshnessStale = Color(0xFFFFB74D),
    freshnessUnknown = Color(0xFFBDBDBD)
)
