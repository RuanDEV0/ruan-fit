package br.com.ruan.fit.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val Ink = Color(0xFF152321)
internal val Muted = Color(0xFF66726D)
internal val CanvasColor = Color(0xFFF5F7F2)
internal val Lime = Color(0xFFD5F16B)
internal val Pine = Color(0xFF244F43)
internal val Line = Color(0xFFE1E8DF)

private val fitColors = lightColorScheme(
    primary = Ink,
    onPrimary = Color.White,
    primaryContainer = Lime,
    onPrimaryContainer = Ink,
    secondary = Pine,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE4F0E6),
    onSecondaryContainer = Pine,
    background = CanvasColor,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFEAF0E9),
    onSurfaceVariant = Muted,
    outline = Color(0xFFB8C6BA),
    error = Color(0xFFAD3D31),
    errorContainer = Color(0xFFFFE9E4)
)

@Composable
internal fun FitTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = fitColors,
        typography = Typography(
            headlineLarge = androidx.compose.ui.text.TextStyle(fontSize = 31.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp),
            headlineMedium = androidx.compose.ui.text.TextStyle(fontSize = 25.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
            titleLarge = androidx.compose.ui.text.TextStyle(fontSize = 21.sp, lineHeight = 27.sp, fontWeight = FontWeight.Bold),
            titleMedium = androidx.compose.ui.text.TextStyle(fontSize = 17.sp, lineHeight = 23.sp, fontWeight = FontWeight.SemiBold),
            bodyLarge = androidx.compose.ui.text.TextStyle(fontSize = 15.sp, lineHeight = 22.sp),
            bodyMedium = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
            labelLarge = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        ),
        content = content
    )
}

@Composable
internal fun PageHeading(eyebrow: String, title: String, subtitle: String? = null) {
    Column(Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 20.dp)) {
        Text(eyebrow.uppercase(), color = Pine, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(5.dp))
        Text(title, style = MaterialTheme.typography.headlineLarge)
        if (subtitle != null) {
            Spacer(Modifier.height(6.dp))
            Text(subtitle, color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
internal fun SectionHeading(title: String, trailing: String? = null) {
    Row(Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 11.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
        if (trailing != null) Text(trailing, color = Muted, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
internal fun FitCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, Line)) {
        Column(Modifier.padding(18.dp), content = content)
    }
}

@Composable
internal fun LabelPill(text: String, modifier: Modifier = Modifier, strong: Boolean = false) {
    Box(modifier.background(if (strong) Lime else CanvasColor, RoundedCornerShape(50)).padding(horizontal = 11.dp, vertical = 6.dp)) {
        Text(text, color = if (strong) Ink else Pine, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun EmptyState(symbol: String, title: String, body: String, action: (@Composable () -> Unit)? = null) {
    Column(Modifier.fillMaxWidth().padding(vertical = 34.dp, horizontal = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(70.dp).background(Color.White, RoundedCornerShape(24.dp)).border(1.dp, Line, RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center) {
            Text(symbol, fontSize = 29.sp, color = Pine)
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(body, color = Muted, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        if (action != null) { Spacer(Modifier.height(18.dp)); action() }
    }
}

@Composable
internal fun NavGlyph(name: String, modifier: Modifier = Modifier, color: Color = LocalContentColor.current) {
    Canvas(modifier.size(23.dp)) {
        val w = size.width; val h = size.height
        val stroke = 2.dp.toPx()
        when (name) {
            "plans" -> {
                drawRoundRect(color, topLeft = Offset(w * .16f, h * .13f), size = Size(w * .68f, h * .74f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()), style = Stroke(stroke))
                drawLine(color, Offset(w * .30f, h * .39f), Offset(w * .70f, h * .39f), stroke)
                drawLine(color, Offset(w * .30f, h * .58f), Offset(w * .59f, h * .58f), stroke)
            }
            "library" -> {
                drawLine(color, Offset(w * .17f, h * .50f), Offset(w * .83f, h * .50f), stroke)
                drawRoundRect(color, Offset(w * .18f, h * .30f), Size(w * .13f, h * .40f), androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()), style = Stroke(stroke))
                drawRoundRect(color, Offset(w * .69f, h * .30f), Size(w * .13f, h * .40f), androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()), style = Stroke(stroke))
            }
            "workout" -> {
                val path = Path().apply { moveTo(w * .39f, h * .25f); lineTo(w * .74f, h * .50f); lineTo(w * .39f, h * .75f); close() }
                drawPath(path, color)
            }
            "history" -> {
                drawCircle(color, radius = w * .34f, center = Offset(w * .5f, h * .5f), style = Stroke(stroke))
                drawLine(color, Offset(w * .5f, h * .5f), Offset(w * .5f, h * .30f), stroke)
                drawLine(color, Offset(w * .5f, h * .5f), Offset(w * .66f, h * .58f), stroke)
            }
            else -> {
                drawLine(color, Offset(w * .18f, h * .78f), Offset(w * .18f, h * .27f), stroke)
                drawLine(color, Offset(w * .18f, h * .78f), Offset(w * .82f, h * .78f), stroke)
                val path = Path().apply { moveTo(w * .26f, h * .65f); lineTo(w * .44f, h * .49f); lineTo(w * .58f, h * .57f); lineTo(w * .78f, h * .29f) }
                drawPath(path, color, style = Stroke(stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
            }
        }
    }
}
