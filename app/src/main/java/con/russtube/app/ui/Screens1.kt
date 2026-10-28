package com.russtube.app.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.russtube.app.data.AppVm
import com.russtube.app.data.FakeRepo

@Composable
fun Chip(text: String, sel: Boolean, onClick: () -> Unit) = Box(
    Modifier.clip(RoundedCornerShape(10.dp)).background(if (sel) Color.White else Surf).clickable(onClick = onClick)
        .padding(horizontal = 16.dp, vertical = 9.dp)) {
    Text(text, color = if (sel) Color.Black else Color.White, fontSize = 14.sp)
}

@Composable
fun HomeScreen(vm: AppVm, nav: NavController) = LazyColumn(Modifier.fillMaxSize()) {
    item { TopBar(onSearch = { nav.navigate("search") }) }
    item {
        LazyRow(contentPadding = PaddingValues(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Box(Modifier.clip(RoundedCornerShape(10.dp)).background(Surf).clickable { nav.navigate("navigator") }.padding(10.dp)) { Icon(Icons.Filled.Explore, "Навигатор") } }
            items(FakeRepo.categories) { c -> Chip(c, vm.category == c) { vm.category = c } }
        }
    }
    item {
        Row(Modifier.padding(12.dp, 18.dp, 12.dp, 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.SlowMotionVideo, null, tint = Red); Spacer(Modifier.width(8.dp))
            Text("Shorts", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
    val shorts = vm.shorts.take(4).chunked(2)
    if (vm.loading) items(2) { Row(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SkeletonCard(0.9f, Modifier.weight(1f)); SkeletonCard(0.9f, Modifier.weight(1f)) } }
    else items(shorts) { pair -> Row(Modifier.padding(horizontal = 12.dp).padding(bottom = 14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        pair.forEach { ShortCard(it, Modifier.weight(1f)) { nav.navigate("shorts") } }
        if (pair.size == 1) Spacer(Modifier.weight(1f)) } }
    if (vm.loading) items(3) { SkeletonCard(modifier = Modifier.padding(horizontal = 12.dp)) }
    else items(vm.feed, key = { it.id }) { v -> VideoCard(v) { nav.navigate("player/${v.id}") } }
}

@Composable
fun NavigatorScreen(vm: AppVm, nav: NavController) = LazyColumn(Modifier.fillMaxSize()) {
    item { TopBar(onSearch = { nav.navigate("search") }) }
    item { Text("Навигатор", fontSize = 26.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp)) }
    items(FakeRepo.categories.drop(1).chunked(2)) { pair ->
        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            pair.forEach { c -> Box(Modifier.weight(1f).height(72.dp).clip(RoundedCornerShape(14.dp)).background(Surf)
                .clickable { vm.category = c; nav.navigate("home") }.padding(14.dp), Alignment.CenterStart) { Text(c, fontWeight = FontWeight.Medium) } }
        }
    }
    item { Text("В тренде во всём мире", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp, 22.dp, 12.dp, 10.dp)) }
    items(vm.trending) { v -> VideoCard(v) { nav.navigate("player/${v.id}") } }
    item { Text("Новые популярные авторы", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp)) }
    item {
        LazyRow(contentPadding = PaddingValues(12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(vm.channelNames) { ch -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(64.dp).clip(CircleShape2).background(Surf), Alignment.Center) { Text(ch.take(1), fontSize = 22.sp) }
                Text(ch, fontSize = 12.sp, modifier = Modifier.width(72.dp).padding(top = 4.dp), maxLines = 1) } }
        }
    }
}

@Composable
fun SubsScreen(vm: AppVm, nav: NavController) = LazyColumn(Modifier.fillMaxSize()) {
    item { TopBar(onSearch = { nav.navigate("search") }) }
    item {
        LazyRow(contentPadding = PaddingValues(12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(vm.channelNames) { ch ->
                val on = ch in vm.subs
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { vm.toggle(vm.subs, ch) }) {
                    Box(Modifier.size(56.dp).clip(CircleShape2).background(if (on) Red else Surf), Alignment.Center) { Text(ch.take(1), fontSize = 20.sp) }
                    Text(ch, fontSize = 11.sp, modifier = Modifier.width(64.dp), maxLines = 1, color = if (on) Color.White else Sub)
                }
            }
        }
    }
    val feed = vm.videos.filter { it.channel in vm.subs && !it.isShort }
    if (feed.isEmpty()) item { Text("Нажмите на канал, чтобы подписаться. Здесь появятся свежие видео подписок.",
        color = Sub, modifier = Modifier.padding(24.dp)) }
    items(feed) { v -> VideoCard(v) { nav.navigate("player/${v.id}") } }
}

@Composable
fun SearchScreen(vm: AppVm, nav: NavController) {
    var q by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton({ nav.popBackStack() }) { Icon(Icons.Filled.ArrowBack, "Назад") }
            OutlinedTextField(q, { q = it }, Modifier.weight(1f), placeholder = { Text("Поиск в RussTube") }, singleLine = true,
                shape = RoundedCornerShape(24.dp))
        }
        LazyColumn {
            items(if (q.isBlank()) emptyList() else vm.videos.filter { it.title.contains(q, true) || it.channel.contains(q, true) }) { v ->
                VideoCard(v) { nav.navigate("player/${v.id}") } }
        }
    }
}
