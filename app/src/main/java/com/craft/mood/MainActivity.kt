package com.craft.mood

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.craft.mood.ui.theme.MoodTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoodTheme {
                MainScreen(context = this)
            }
        }
    }
}

// Enum para manejar las pantallas
enum class Screen {
    HOME, SCREEN1, SAVED_EMOTIONS, WEEKLY_STATS
}

// Data class para almacenar las emociones guardadas
data class EmotionEntry(val date: String, val time: String, val emotion: String, val reason: String)

// Funciones para guardar y cargar emociones en SharedPreferences
fun saveEmotionsToPrefs(context: Context, emotions: List<EmotionEntry>) {
    val prefs = context.getSharedPreferences("emotion_prefs", Context.MODE_PRIVATE)
    val editor = prefs.edit()
    val emotionsString = emotions.joinToString(";") { "${it.date},${it.time},${it.emotion},${it.reason}" }
    editor.putString("saved_emotions", emotionsString)
    editor.apply()
}

fun loadEmotionsFromPrefs(context: Context): MutableList<EmotionEntry> {
    val prefs = context.getSharedPreferences("emotion_prefs", Context.MODE_PRIVATE)
    val emotionsString = prefs.getString("saved_emotions", "") ?: ""
    if (emotionsString.isEmpty()) return mutableListOf()

    return emotionsString.split(";").map { entry ->
        val parts = entry.split(",")
        EmotionEntry(parts[0], parts[1], parts[2], parts[3])
    }.toMutableList()
}

@Composable
fun MainScreen(context: Context) {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    val savedEmotions = remember { mutableStateListOf<EmotionEntry>().apply { addAll(loadEmotionsFromPrefs(context)) } }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (currentScreen) {
            Screen.HOME -> HomeScreen(onNavigate = { screen -> currentScreen = screen })
            Screen.SCREEN1 -> Screen1(
                onBack = { currentScreen = Screen.HOME },
                onSaveEmotion = { entry ->
                    savedEmotions.add(entry)
                    saveEmotionsToPrefs(context, savedEmotions)
                    currentScreen = Screen.HOME
                }
            )
            Screen.SAVED_EMOTIONS -> SavedEmotionsScreen(
                savedEmotions = savedEmotions,
                onBack = { currentScreen = Screen.HOME }
            )
            Screen.WEEKLY_STATS -> WeeklyStatsScreen(
                savedEmotions = savedEmotions,
                onBack = { currentScreen = Screen.HOME }
            )
        }
    }
}

// Pantalla principal (menú)
@Composable
fun HomeScreen(onNavigate: (Screen) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        Text("Menú Principal", fontSize = 24.sp, color = Color.DarkGray)

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { onNavigate(Screen.SCREEN1) }) {
            Text("Añadir Emoción")
        }

        Button(onClick = { onNavigate(Screen.SAVED_EMOTIONS) }) {
            Text("Ver Emociones Guardadas")
        }

        Button(onClick = { onNavigate(Screen.WEEKLY_STATS) }) {
            Text("Estadística Semanal")
        }
    }
}

// Pantalla "Añadir emoción"
@Composable
fun Screen1(onBack: () -> Unit, onSaveEmotion: (EmotionEntry) -> Unit) {
    val emotions = listOf("Feliz", "Triste", "Ansioso", "Relajado", "Emocionado")
    var selectedEmotion by remember { mutableStateOf("") }
    var showDropdown by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }
    val currentDate = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) }
    val currentTime = remember { LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onBack, modifier = Modifier.padding(end = 8.dp)) {
                Text("Volver")
            }
            Text(text = "Añadir emoción", style = MaterialTheme.typography.h6)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Fecha: $currentDate", style = MaterialTheme.typography.body1)
        Text(text = "Hora: $currentTime", style = MaterialTheme.typography.body1)

        Spacer(modifier = Modifier.height(16.dp))

        Text("¿Cómo te sientes?", style = MaterialTheme.typography.body1)

        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Button(onClick = { showDropdown = true }) {
                Text(if (selectedEmotion.isEmpty()) "Selecciona una emoción" else selectedEmotion)
            }
            DropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { showDropdown = false }
            ) {
                emotions.forEach { emotion ->
                    DropdownMenuItem(onClick = {
                        selectedEmotion = emotion
                        showDropdown = false
                    }) {
                        Text(text = emotion)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("¿Por qué te sientes así?", style = MaterialTheme.typography.body1)

        TextField(
            value = reason,
            onValueChange = { reason = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            placeholder = { Text("Escribe aquí tu razón") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onSaveEmotion(EmotionEntry(date = currentDate, time = currentTime, emotion = selectedEmotion, reason = reason))
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            enabled = selectedEmotion.isNotEmpty() && reason.isNotBlank()
        ) {
            Text("Guardar emoción")
        }
    }
}

// Pantalla de emociones guardadas con filtro
@Composable
fun SavedEmotionsScreen(savedEmotions: List<EmotionEntry>, onBack: () -> Unit) {
    val emotions = listOf("Todas") + savedEmotions.map { it.emotion }.distinct()
    var selectedFilter by remember { mutableStateOf("Todas") }
    var showDropdown by remember { mutableStateOf(false) }

    val filteredEmotions = if (selectedFilter == "Todas") {
        savedEmotions
    } else {
        savedEmotions.filter { it.emotion == selectedFilter }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onBack, modifier = Modifier.padding(end = 8.dp)) {
                Text("Volver")
            }
            Text(text = "Emociones Guardadas", style = MaterialTheme.typography.h6)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Button(onClick = { showDropdown = true }) {
                Text(if (selectedFilter == "Todas") "Filtrar por emoción" else selectedFilter)
            }
            DropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { showDropdown = false }
            ) {
                emotions.forEach { emotion ->
                    DropdownMenuItem(onClick = {
                        selectedFilter = emotion
                        showDropdown = false
                    }) {
                        Text(text = emotion)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredEmotions.isEmpty()) {
            Text("No hay emociones para mostrar.", color = Color.Gray)
        } else {
            filteredEmotions.forEach { entry ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFFE3F2FD),
                    elevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Fecha: ${entry.date}")
                        Text("Hora: ${entry.time}")
                        Text("Emoción: ${entry.emotion}")
                        Text("Motivo: ${entry.reason}")
                    }
                }
            }
        }
    }
}

// Pantalla de estadísticas semanales
@Composable
fun WeeklyStatsScreen(savedEmotions: List<EmotionEntry>, onBack: () -> Unit) {
    val sevenDaysAgo = LocalDate.now().minusDays(7)
    val weeklyEmotions = savedEmotions.filter { LocalDate.parse(it.date, DateTimeFormatter.ofPattern("dd-MM-yyyy")).isAfter(sevenDaysAgo) }
    val emotionCounts = weeklyEmotions.groupingBy { it.emotion }.eachCount()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onBack, modifier = Modifier.padding(end = 8.dp)) {
                Text("Volver")
            }
            Text(text = "Estadística Semanal", style = MaterialTheme.typography.h6)
        }

        Spacer(modifier = Modifier.height(16.dp))

        SimpleBarChart(data = emotionCounts)
    }
}

// Gráfico de barras simple
@Composable
fun SimpleBarChart(data: Map<String, Int>) {
    val maxCount = data.values.maxOrNull() ?: 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        data.forEach { (emotion, count) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .height((count.toFloat() / maxCount * 200).dp)
                        .width(30.dp)
                        .background(Color.Blue)
                )
                Text(text = emotion, fontSize = 12.sp)
                Text(text = "$count", fontSize = 10.sp)
            }
        }
    }
}