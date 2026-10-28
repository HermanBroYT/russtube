package com.russtube.app.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.russtube.app.data.Video

val Bg = Color(0xFF0B0C0F)
val Surf = Color(0xFF2B2D31)
val Red = Color(0xFFFF0033)
val Sub = Color(0xFF9A9CA3)

@Composable
fun RussTubeTheme(content: @Composable () -> Unit) = MaterialTheme(
    colorScheme = darkColorScheme(
        primary = Red, background = Bg, surface = Bg, surfaceVariant = Surf,
        onBackground = Color.White, onSurface = Color.White
    ), content = content
)

@Composable
fun Logo() = Row(verticalAlignment = Alignment.CenterVertically) {
    Box(Modifier.size(34.dp, 24.dp).clip(RoundedCornerShape(7.dp)).background(Red), Alignment.Center) {
        Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(18.dp))
    }
    Spacer(Modifier.width(6.dp))
    Text("RussTube", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp)
}

@Composable
fun TopBar(onBell: () -> Unit = {}, onSearch: () -> Unit = {}, onSettings: (() -> Unit)? = null) =
    Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Logo(); Spacer(Modifier.weight(1f))
        IconButton(onBell) { Icon(Icons.Filled.NotificationsNone, "Уведомления") }
        IconButton(onSearch) { Icon(Icons.Filled.Search, "Поиск") }
        if (onSettings != null) IconButton(onSettings) { Icon(Icons.Filled.Settings, "Настройки") }
    }

@Composable
fun Skeleton(modifier: Modifier = Modifier, radius: Int = 14) {
    val a by rememberInfiniteTransition(label = "sk").animateFloat(
        0.5f, 1f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "a")
    Box(modifier.alpha(a).clip(RoundedCornerShape(radius.dp)).background(Surf))
}

@Composable
fun SkeletonCard(ratio: Float = 16 / 9f, modifier: Modifier = Modifier) = Column(modifier.padding(bottom = 18.dp)) {
    Skeleton(Modifier.fillMaxWidth().aspectRatio(ratio))
    Spacer(Modifier.height(10.dp))
    Skeleton(Modifier.fillMaxWidth(0.75f).height(12.dp), 8); Spacer(Modifier.height(8.dp))
    Skeleton(Modifier.fillMaxWidth(0.4f).height(10.dp), 8)
}

fun views(n: Int) = when { n >= 1_000_000 -> "%.1f млн".format(n / 1e6); n >= 1000 -> "${n / 1000} тыс."; else -> "$n" } + " просмотров"
fun dur(s: Int) = "%d:%02d".format(s / 60, s % 60)

@Composable
fun Thumb(v: Video, ratio: Float, modifier: Modifier = Modifier) {
    val h = (v.id.hashCode() and 0xFF) / 255f
    Box(modifier.aspectRatio(ratio).clip(RoundedCornerShape(14.dp))
        .background(Brush.linearGradient(listOf(Color.hsv(h * 360, .5f, .45f), Color.hsv((h * 360 + 60) % 360, .6f, .25f))))) {
        Icon(Icons.Filled.PlayCircleOutline, null, tint = Color.White.copy(.7f), modifier = Modifier.align(Alignment.Center).size(40.dp))
        if (v.live) Badge("● LIVE", Red, Modifier.align(Alignment.BottomEnd))
        else if (v.durationSec > 0) Badge(dur(v.durationSec), Color.Black.copy(.7f), Modifier.align(Alignment.BottomEnd))
    }
}
@Composable
private fun Badge(t: String, c: Color, m: Modifier) =
    Text(t, fontSize = 11.sp, modifier = m.padding(6.dp).background(c, RoundedCornerShape(4.dp)).padding(horizontal = 5.dp, vertical = 2.dp))

@Composable
fun VideoCard(v: Video, onClick: () -> Unit) = Column(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(bottom = 18.dp)) {
    Thumb(v, 16 / 9f, Modifier.fillMaxWidth())
    Row(Modifier.padding(top = 10.dp, start = 12.dp, end = 12.dp)) {
        Box(Modifier.size(36.dp).clip(CircleShape2).background(Surf), Alignment.Center) { Text(v.channel.take(1), fontWeight = FontWeight.Bold) }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(v.title, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
            Text("${v.channel} · ${views(v.views)} · ${v.age}", color = Sub, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.Filled.MoreVert, null, tint = Sub)
    }
}
val CircleShape2 = RoundedCornerShape(50)

@Composable
fun ShortCard(v: Video, modifier: Modifier, onClick: () -> Unit) = Column(modifier.clickable(onClick = onClick)) {
    Thumb(v, 0.9f, Modifier.fillMaxWidth())
    Text(v.title, maxLines = 2, overflow = TextOverflow.Ellipsis, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
    Text(views(v.views), color = Sub, fontSize = 12.sp)
}
