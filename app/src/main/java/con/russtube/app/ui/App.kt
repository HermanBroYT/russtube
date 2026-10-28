package com.russtube.app.ui

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.russtube.app.data.AppVm

private data class Tab(val route: String, val label: String, val icon: ImageVector, val sel: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RussTubeApp(vm: AppVm = viewModel()) {
    val nav = rememberNavController()
    val ctx = LocalContext.current
    val back by nav.currentBackStackEntryAsState()
    val route = back?.destination?.route ?: "home"
    var sheet by remember { mutableStateOf(false) }
    val tabs = listOf(
        Tab("home", "Главная", Icons.Outlined.Home, Icons.Filled.Home),
        Tab("shorts", "Shorts", Icons.Outlined.SlowMotionVideo, Icons.Filled.SlowMotionVideo),
        Tab("subs", "Подписки", Icons.Outlined.Subscriptions, Icons.Filled.Subscriptions),
        Tab("you", "Вы", Icons.Outlined.AccountCircle, Icons.Filled.AccountCircle)
    )
    val upload = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { vm.addMine(it.toString(), "Мой ролик ${vm.mine.size + 1}"); Toast.makeText(ctx, "Видео загружено в «Ваши видео»", Toast.LENGTH_SHORT).show() }
    }
    val record = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        if (r.resultCode == Activity.RESULT_OK) r.data?.data?.let {
            vm.addMine(it.toString(), "Новое видео ${vm.mine.size + 1}"); Toast.makeText(ctx, "Видео сохранено", Toast.LENGTH_SHORT).show()
        }
    }
    val showBar = route in setOf("home", "shorts", "subs", "you", "navigator")

    Scaffold(
        containerColor = Bg,
        bottomBar = {
            if (showBar) NavigationBar(containerColor = Bg) {
                fun item(t: Tab) = @Composable {
                    NavigationBarItem(
                        selected = route == t.route, onClick = { nav.navigate(t.route) { popUpTo("home"); launchSingleTop = true } },
                        icon = { Icon(if (route == t.route) t.sel else t.icon, t.label) },
                        label = { Text(t.label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Bg))
                }
                item(tabs[0])(); item(tabs[1])()
                NavigationBarItem(false, { sheet = true }, {
                    Icon(Icons.Filled.Add, "Создать", Modifier.background(Surf, androidx.compose.foundation.shape.CircleShape).padding(10.dp).size(28.dp))
                }, label = {})
                item(tabs[2])(); item(tabs[3])()
            }
        }
    ) { pad ->
        NavHost(nav, "home", Modifier.padding(bottom = pad.calculateBottomPadding())) {
            composable("home") { HomeScreen(vm, nav) }
            composable("navigator") { NavigatorScreen(vm, nav) }
            composable("shorts") { ShortsScreen(vm) }
            composable("subs") { SubsScreen(vm, nav) }
            composable("you") { YouScreen(vm, nav) }
            composable("search") { SearchScreen(vm, nav) }
            composable("settings") { SettingsScreen(vm, nav) }
            composable("player/{id}") { PlayerScreen(vm, it.arguments?.getString("id") ?: "", nav) }
        }
    }
    if (sheet) ModalBottomSheet({ sheet = false }, containerColor = Surf) {
        @Composable fun row(i: ImageVector, t: String, a: () -> Unit) = Row(
            Modifier.fillMaxWidth().clickable { sheet = false; a() }.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(i, null); Spacer(Modifier.width(16.dp)); Text(t)
        }
        row(Icons.Filled.Videocam, "Снять видео") { record.launch(Intent(MediaStore.ACTION_VIDEO_CAPTURE)) }
        row(Icons.Filled.Upload, "Загрузить видео") { upload.launch("video/*") }
        row(Icons.Filled.Sensors, "Начать трансляцию") { Toast.makeText(ctx, "Трансляции: требуется сервер RTMP", Toast.LENGTH_LONG).show() }
        Spacer(Modifier.height(24.dp))
    }
}
