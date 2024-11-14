package com.example.mood


import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mood.ui.theme.MoodTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.ui.geometry.Size


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
    HOME, SCREEN1, SAVED_EMOTIONS, WEEKLY_STATS, WEEKLY_SELECTION
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
            Screen.WEEKLY_SELECTION -> WeeklySelectionScreen(
                savedEmotions = savedEmotions,
                onBack = { currentScreen = Screen.HOME }
            )
        }
    }
}




// Modificación de HomeScreen
// Modificación de HomeScreen
@Composable
fun HomeScreen(onNavigate: (Screen) -> Unit) {
    // Color de fondo gris claro para toda la pantalla
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0E0E0)), // Fondo gris claro
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .padding(16.dp)
        ) {
            // Título del menú
            Text(
                "Menú Principal",
                fontSize = 28.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Fila 1: Añadir Emoción y Ver Emociones Guardadas
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularButton(text = "Añadir Emoción", onClick = { onNavigate(Screen.SCREEN1) })
                CircularButton(text = "Ver Emociones Guardadas", onClick = { onNavigate(Screen.SAVED_EMOTIONS) })
            }

            // Fila 2: Estadística Semanal y Última Semana
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularButton(text = "Estadística Semanal", onClick = { onNavigate(Screen.WEEKLY_STATS) })
                CircularButton(text = "Última Semana", onClick = { onNavigate(Screen.WEEKLY_SELECTION) })
            }
        }
    }
}


@Composable
fun CircularButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(120.dp) // Tamaño cuadrado para mantener la forma circular
            .background(Color(0xFF424242), shape = CircleShape) // Fondo gris oscuro y circular
            .clickable(onClick = onClick), // Hacer que sea clickeable
        contentAlignment = Alignment.Center // Centrar contenido en el centro
    ) {
        Text(text, color = Color.White, style = MaterialTheme.typography.button)
    }
}



@Composable
fun Screen1(onBack: () -> Unit, onSaveEmotion: (EmotionEntry) -> Unit) {
    val emotions = listOf("Feliz", "Triste", "Ansioso", "Relajado", "Emocionado", "Enojado", "Frustrado", "Satisfecho")
    var selectedEmotion by remember { mutableStateOf("") }
    var showDropdown by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }
    val currentDate = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) }
    val currentTime = remember { LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) }

    // Añadimos un modificador de desplazamiento vertical
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Habilita el desplazamiento vertical
    ) {
        // Encabezado superior con fondo azul y botón de volver
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2196F3)) // Fondo azul
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onBack,
                    modifier = Modifier,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.White)
                ) {
                    Text("Volver", color = Color(0xFF2196F3))
                }

                Spacer(modifier = Modifier.weight(1f)) // Espacio flexible para centrar el título

                Text(
                    text = "Añadir emoción",
                    style = MaterialTheme.typography.h6,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }

        // Contenido principal con fondo blanco
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Fecha actual: $currentDate - $currentTime",
                fontSize = 18.sp,
                color = Color.DarkGray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "¿Cómo te sientes?",
                fontSize = 20.sp,
                style = MaterialTheme.typography.body1,
                color = Color.DarkGray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Black)
                    .background(Color.White)
                    .padding(vertical = 8.dp)
            ) {
                Button(
                    onClick = { showDropdown = true },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (selectedEmotion.isEmpty()) "Selecciona una emoción" else selectedEmotion, color = Color.Black)
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

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "¿Por qué te sientes así?",
                fontSize = 20.sp,
                style = MaterialTheme.typography.body1,
                color = Color.DarkGray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                enabled = selectedEmotion.isNotEmpty() && reason.isNotBlank(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF00BFA5))
            ) {
                Text("Guardar emoción", color = Color.White)
            }
        }
    }
}






// Pantalla de emociones guardadas con filtro
@Composable
fun SavedEmotionsScreen(savedEmotions: List<EmotionEntry>, onBack: () -> Unit) {
    val emotions = listOf("Todas") + savedEmotions.map { it.emotion }.distinct()
    var selectedFilter by remember { mutableStateOf("Todas") }
    var showDropdown by remember { mutableStateOf(false) }

    // Filtrar las emociones según el filtro seleccionado
    val filteredEmotions = if (selectedFilter == "Todas") {
        savedEmotions
    } else {
        savedEmotions.filter { it.emotion == selectedFilter }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Encabezado con fondo azul y botón de volver
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2196F3)) // Fondo azul
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onBack,
                    modifier = Modifier,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.White)
                ) {
                    Text("Volver", color = Color(0xFF2196F3))
                }

                Spacer(modifier = Modifier.weight(1f)) // Espacio flexible para centrar el título

                Text(
                    text = "Emociones Guardadas",
                    style = MaterialTheme.typography.h6,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }

        // Contenido con fondo blanco
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Dropdown para seleccionar el filtro de emociones
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Black) // Borde negro
                    .background(Color.White)   // Fondo blanco
                    .padding(vertical = 8.dp)
            ) {
                Button(
                    onClick = { showDropdown = true },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (selectedFilter == "Todas") "Filtrar por emoción" else selectedFilter, color = Color.Black)
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

            // Mostrar emociones filtradas o mensaje de vacío
            if (filteredEmotions.isEmpty()) {
                Text("No hay emociones para mostrar.", color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredEmotions) { entry ->
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
    }
}



@Composable
fun WeeklyStatsScreen(savedEmotions: List<EmotionEntry>, onBack: () -> Unit) {
    val sevenDaysAgo = LocalDate.now().minusDays(7)
    val recentEmotions = savedEmotions.filter { emotion ->
        val emotionDate = LocalDate.parse(emotion.date, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        emotionDate.isAfter(sevenDaysAgo) || emotionDate.isEqual(sevenDaysAgo)
    }
    val emotionCounts = recentEmotions.groupingBy { it.emotion }.eachCount()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Encabezado con fondo azul y botón de volver
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2196F3))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onBack,
                    modifier = Modifier,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.White)
                ) {
                    Text("Volver", color = Color(0xFF2196F3))
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Estadística Semanal",
                    style = MaterialTheme.typography.h6,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }

        // Contenido con fondo blanco
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {
            if (emotionCounts.isEmpty()) {
                Text(
                    "No hay datos para mostrar.",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    // Gráfico de pastel
                    PieChart(data = emotionCounts)

                    Spacer(modifier = Modifier.width(16.dp))

                    // Leyenda
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        emotionCounts.keys.forEach { emotion ->
                            val color = emotionColors[emotion] ?: Color.Gray
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(color = color)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(emotion, style = MaterialTheme.typography.body2)
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun PieChart(data: Map<String, Int>) {
    val total = data.values.sum()

    Canvas(modifier = Modifier.size(200.dp)) {
        var startAngle = 0f

        data.forEach { (emotion, count) ->
            val sweepAngle = (count.toFloat() / total) * 360f
            val color = emotionColors[emotion] ?: Color.Gray  // Usar color del mapa o gris como respaldo
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                size = Size(size.width, size.height)
            )
            startAngle += sweepAngle
        }
    }
}

val emotionColors = mapOf(
    "Feliz" to Color(0xFFFFEB3B),       // Amarillo
    "Triste" to Color(0xFF2196F3),      // Azul
    "Ansioso" to Color(0xFF9C27B0),     // Morado
    "Relajado" to Color(0xFF00BCD4),    // Celeste
    "Emocionado" to Color(0xFFFF9800),  // Naranja
    "Enojado" to Color(0xFFF44336),     // Rojo
    "Frustrado" to Color(0xFF000000),   // Negro
    "Satisfecho" to Color(0xFF4CAF50)   // Verde
)



@Composable
fun WeeklySelectionScreen(savedEmotions: List<EmotionEntry>, onBack: () -> Unit) {
    // Calcula la fecha de hace 7 días
    val sevenDaysAgo = LocalDate.now().minusDays(7)

    // Filtrar emociones de los últimos 7 días
    val recentEmotions = savedEmotions.filter { emotion ->
        val emotionDate = LocalDate.parse(emotion.date, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        emotionDate.isAfter(sevenDaysAgo) || emotionDate.isEqual(sevenDaysAgo)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Encabezado con fondo azul y botón de volver
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2196F3))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onBack,
                    modifier = Modifier,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.White)
                ) {
                    Text("Volver", color = Color(0xFF2196F3))
                }

                Spacer(modifier = Modifier.weight(1f)) // Espacio flexible para centrar el título

                Text(
                    text = "Emociones Últimos 7 Días",
                    style = MaterialTheme.typography.h6,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }

        // Contenido con fondo blanco
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {
            if (recentEmotions.isEmpty()) {
                Text(
                    "No hay emociones guardadas en los últimos 7 días.",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentEmotions) { entry ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            backgroundColor = Color(0xFFF3E5F5),
                            elevation = 4.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Fecha: ${entry.date}")
                                Text("Emoción: ${entry.emotion}")
                                Text("Motivo: ${entry.reason}")
                            }
                        }
                    }
                }
            }
        }
    }
}
