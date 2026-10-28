package com.russtube.app.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.russtube.app.data.AppVm
import com.russtube.app.data.Video

@Composable
fun rememberPlayer(): ExoPlayer {
    val ctx = LocalContext.current
    val p = remember { ExoPlayer.Builder(ctx).build() }
    DisposableEffect(Unit) { onDispose { p.release() } }
    return p
}

@Composable
fun PlayerScreen(vm: AppVm, id: String, nav: NavController) {
    val v = vm.byId(id) ?: return
    val player = rememberPlayer()
    LaunchedEffect(id) { player.setMediaItem(MediaItem.fromUri(v.localUri ?: v.url)); player.prepare(); player.playWhenReady = true; vm.watched(v) }
    var text by remember { mutableStateOf("") }
    var sponsor by remember { mutableStateOf(false) }
    val subscribed = v.channel in vm.subs
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        AndroidView({ PlayerView(it).apply { this.player = player } }, Modifier.fillMaxWidth().aspectRatio(16 / 9f))
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(12.dp)) {
            item {
                Text(v.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("${views(v.views)} · ${v.age}", color = Sub, fontSize = 12.sp)
                Row(Modifier.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(CircleShape2).background(Surf), Alignment.Center) { Text(v.channel.take(1)) }
                    Text(v.channel, Modifier.padding(horizontal = 10.dp).weight(1f), fontWeight = FontWeight.Medium)
                    OutlinedButton({ sponsor = true }, Modifier.padding(end = 8.dp)) { Text("Спонсор") }
                    Button({ vm.toggle(vm.subs, v.channel) }, colors = ButtonDefaults.buttonColors(
                        if (subscribed) Surf else Color.White, if (subscribed) Color.White else Color.Black)) {
                        Text(if (subscribed) "Вы подписаны" else "Подписаться") }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { Action(if (v.id in vm.liked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp, "Нравится") { vm.toggle(vm.liked, v.id) } }
                    item { Action(Icons.Outlined.Share, "Поделиться") {} }
                    item { Action(Icons.Outlined.Download, if (vm.premium) "Скачать" else "Скачать · Premium") {} }
                    item { Action(if (v.id in vm.later) Icons.Filled.WatchLater else Icons.Outlined.WatchLater, "Позже") { vm.toggle(vm.later, v.id) } }
                }
                Text("Комментарии · ${vm.commentsOf(id).size}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 18.dp, bottom = 8.dp))
                OutlinedTextField(text, { text = it }, Modifier.fillMaxWidth(), placeholder = { Text("Добавьте комментарий") },
                    trailingIcon = { IconButton({ if (text.isNotBlank()) { vm.addComment(id, text); text = "" } }) { Icon(Icons.Filled.Send, "Отправить") } })
            }
            items(vm.commentsOf(id)) { c ->
                Column(Modifier.padding(vertical = 8.dp)) {
                    Text(c.author + if (c.sponsor) "  ★ Спонсор" else "", fontSize = 12.sp, color = if (c.sponsor) Color(0xFF3EA6FF) else Sub)
                    Text(c.text)
                }
            }
            item { Text("Далее", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp)) }
            items(vm.feed.filter { it.id != id }.take(6)) { n -> VideoCard(n) { nav.navigate("player/${n.id}") { popUpTo("home") } } }
        }
    }
    if (sponsor) AlertDialog({ sponsor = false },
        confirmButton = { TextButton({ sponsor = false }) { Text("Оформить") } },
        dismissButton = { TextButton({ sponsor = false }) { Text("Отмена") } },
        title = { Text("Спонсорство: ${v.channel}") },
        text = { Text("Ежемесячный взнос, эксклюзивные бонусы и значок в чатах и комментариях.") })
}

@Composable
private fun Action(i: androidx.compose.ui.graphics.vector.ImageVector, t: String, on: () -> Unit) =
    Row(Modifier.clip(RoundedCornerShape(20.dp)).background(Surf).clickable(onClick = on).padding(12.dp, 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(i, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text(t, fontSize = 13.sp)
    }

@Composable
fun ShortsScreen(vm: AppVm) {
    val list = vm.shorts
    if (list.isEmpty()) { Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = Red) }; return }
    val st = rememberPagerState { list.size }
    val player = rememberPlayer()
    LaunchedEffect(st.settledPage) {
        player.setMediaItem(MediaItem.fromUri(list[st.settledPage].url)); player.repeatMode = ExoPlayer.REPEAT_MODE_ONE
        player.prepare(); player.playWhenReady = true
    }
    VerticalPager(st, Modifier.fillMaxSize()) { p ->
        val v = list[p]
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            if (p == st.currentPage) AndroidView({ PlayerView(it).apply { this.player = player; useController = false } }, Modifier.fillMaxSize())
            Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                Text("@${v.channel}", fontWeight = FontWeight.Bold); Text(v.title)
            }
            Column(Modifier.align(Alignment.BottomEnd).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton({ vm.toggle(vm.liked, v.id) }) { Icon(if (v.id in vm.liked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp, "Нравится") }
                Icon(Icons.Outlined.Comment, null, Modifier.padding(12.dp)); Icon(Icons.Outlined.Share, null, Modifier.padding(12.dp))
            }
        }
    }
}

@Composable
fun YouScreen(vm: AppVm, nav: NavController) = LazyColumn(Modifier.fillMaxSize()) {
    item { TopBar(onSearch = { nav.navigate("search") }, onSettings = { nav.navigate("settings") }) }
    item {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Skeleton(Modifier.size(72.dp), 50); Spacer(Modifier.width(14.dp))
            Column { Text("Название_Канала", fontSize = 24.sp, fontWeight = FontWeight.Bold); Text("@Название_Канала", color = Sub) }
        }
        Button({}, Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = ButtonDefaults.buttonColors(Surf, Color.White)) { Text("Перейти на канал") }
        Text("История  ›", fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp, 24.dp, 16.dp, 10.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (vm.history.isEmpty()) items(3) { Column { Skeleton(Modifier.size(160.dp, 90.dp)); Spacer(Modifier.height(8.dp)); Skeleton(Modifier.size(120.dp, 10.dp), 8) } }
            else items(vm.history) { h -> Column(Modifier.width(160.dp).clickable { nav.navigate("player/${h.id}") }) {
                Thumb(h, 16 / 9f); Text(h.title, fontSize = 12.sp, maxLines = 2) } }
        }
        Text("Библиотека", fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp, 28.dp, 16.dp, 10.dp))
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) { Chip("Недавние ⌄", false) {}; Chip("Плейлисты", false) {} }
    }
    val lib = listOf("Понравившиеся" to vm.liked.size, "Ваши видео" to vm.mine.size, "Смотреть позже" to vm.later.size, "Плейлисты" to 0)
    items(lib) { (name, n) ->
        Row(Modifier.fillMaxWidth().padding(16.dp, 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Skeleton(Modifier.size(90.dp, 70.dp), 10); Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) { Text(name, fontSize = 18.sp); Text("$n видео", color = Sub) }
            Icon(Icons.Filled.MoreHoriz, null)
        }
    }
    items(vm.mine) { v -> VideoCard(v) { nav.navigate("player/${v.id}") } }
}

@Composable
fun SettingsScreen(vm: AppVm, nav: NavController) = Column(Modifier.fillMaxSize().statusBarsPadding()) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton({ nav.popBackStack() }) { Icon(Icons.Filled.ArrowBack, "Назад") }
        Text("Настройки", fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
    @Composable fun sw(t: String, d: String, v: Boolean, c: (Boolean) -> Unit) = Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(t, fontSize = 17.sp); Text(d, color = Sub, fontSize = 13.sp) }
        Switch(v, c)
    }
    sw("RussTube Premium", "Без рекламы, фон, офлайн-просмотр, Music Premium", vm.premium) { vm.premium = it }
    sw("Родительский контроль", "Ограничение контента для детей (rusttube.com/myfamily)", vm.parental) { vm.parental = it }
}
