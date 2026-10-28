package com.russtube.app.data

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class Video(
    val id: String, val title: String, val channel: String, val views: Int,
    val age: String, val durationSec: Int, val category: String, val url: String,
    val isShort: Boolean = false, val live: Boolean = false, val localUri: String? = null
)
data class Comment(val author: String, val text: String, val likes: Int = 0, val sponsor: Boolean = false)

object FakeRepo {
    val categories = listOf("Все", "Музыка", "Видеоигры", "Джемы", "Новости", "Обучение", "Спорт", "Трансляции")
    private val urls = listOf(
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
    )
    private val channels = listOf("Русский Звук", "ГеймЛаб", "Новости 24", "Школа Кодинга", "СпортLive", "Джем Сейшн")
    private val titles = listOf(
        "Лучшие хиты недели", "Прохождение без комментариев", "Главное за сутки",
        "Kotlin за 30 минут", "Обзор матча тура", "Живой джем в студии"
    )

    suspend fun load(): List<Video> {
        delay(1200) // имитация сети
        return List(36) { i ->
            val c = i % categories.size.coerceAtMost(channels.size)
            Video(
                id = "v$i", title = "${titles[c]} #${i + 1}", channel = channels[c],
                views = 1_000 + (i * 73_411) % 2_400_000, age = "${i % 11 + 1} дн. назад",
                durationSec = 45 + (i * 97) % 1500, category = categories[(c + 1) % categories.size],
                url = urls[i % urls.size], isShort = i % 4 == 3, live = i % 9 == 8
            )
        }
    }
    fun comments(): List<Comment> = listOf(
        Comment("Алексей", "Отличное видео, спасибо!", 12),
        Comment("Мария", "Подписалась, жду продолжения", 5, sponsor = true),
        Comment("Игорь", "Первый 😄", 2)
    )
}

class AppVm : ViewModel() {
    var videos by mutableStateOf<List<Video>>(emptyList()); private set
    var loading by mutableStateOf(true); private set
    var category by mutableStateOf("Все")
    var premium by mutableStateOf(false)
    var parental by mutableStateOf(false)
    val history = mutableStateListOf<Video>()
    val liked = mutableStateListOf<String>()
    val later = mutableStateListOf<String>()
    val subs = mutableStateListOf<String>()
    val mine = mutableStateListOf<Video>()
    val comments = mutableStateMapOf<String, List<Comment>>()

    init { viewModelScope.launch { videos = FakeRepo.load(); loading = false } }

    val feed get() = videos.filter { !it.isShort && (category == "Все" || it.category == category || (category == "Трансляции" && it.live)) }
    val shorts get() = videos.filter { it.isShort }
    val trending get() = videos.sortedByDescending { it.views }.take(10)
    val channelNames get() = videos.map { it.channel }.distinct()
    fun byId(id: String) = (videos + mine).firstOrNull { it.id == id }
    fun toggle(list: MutableList<String>, v: String) { if (!list.remove(v)) list.add(v) }
    fun watched(v: Video) { history.removeAll { it.id == v.id }; history.add(0, v) }
    fun commentsOf(id: String) = comments.getOrPut(id) { FakeRepo.comments() }
    fun addComment(id: String, text: String) { comments[id] = listOf(Comment("Вы", text)) + commentsOf(id) }
    fun addMine(uri: String, title: String) {
        mine.add(0, Video("m${mine.size}", title, "Название_Канала", 0, "только что", 0, "Все", uri, localUri = uri))
    }
}
