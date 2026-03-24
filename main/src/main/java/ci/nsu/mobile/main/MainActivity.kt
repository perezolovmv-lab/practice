package ci.nsu.mobile.main

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ci.nsu.mobile.main.ui.theme.PeopleTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PeopleTheme {
                Main()
            }
        }
    }
}

@Composable
fun Main() {
    // Состояние для текста из поля ввода
    var inputText by remember { mutableStateOf("") }

    // Состояние для цвета кнопки
    var buttonColor by remember { mutableStateOf(Color.Gray) }

    // Состояние для списка цветов
    var colorsList by remember { mutableStateOf(emptyList<Pair<String, Color>>()) }

    // Инициализация списка цветов
    LaunchedEffect(Unit) {
        colorsList = colorsMap.toList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Заголовок
        Text(
            text = "Color Changer App",
            fontSize = 24.sp,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Текстовое поле для ввода названия цвета
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("Введите название цвета") },
            placeholder = { Text("Например: Red, Green, Blue") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Кнопка для смены цвета
        Button(
            onClick = {
                changeColor(inputText) { newColor ->
                    buttonColor = newColor
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Сменить цвет",
                color = if (buttonColor == Color.Black ||
                    buttonColor == Color.DarkGray ||
                    buttonColor == Color.Gray) Color.White else Color.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Разделитель
        Divider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp
        )

        // Заголовок палитры
        Text(
            text = "Доступные цвета:",
            fontSize = 20.sp,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )

        // Список цветов (палитра)
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(colorsList) { (name, color) ->
                ColorItem(
                    colorName = name,
                    colorValue = color,
                    onColorClick = {
                        // При клике на цвет из палитры меняем цвет кнопки
                        buttonColor = color
                        // Логируем выбор цвета из палитры
                        Log.d("ColorApp", "Выбран цвет из палитры: $name")
                    }
                )
            }
        }
    }
}

@Composable
fun ColorItem(colorName: String, colorValue: Color, onColorClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(35.dp),
        onClick = onColorClick,
        colors = CardDefaults.cardColors(
            containerColor = colorValue
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = colorName,
                color = if (colorValue == Color.Black ||
                    colorValue == Color.DarkGray ||
                    colorValue == Color.Gray) Color.White else Color.Black,
                fontSize = 16.sp
            )
            Text(
                text = "●",
                color = if (colorValue == Color.Black ||
                    colorValue == Color.DarkGray ||
                    colorValue == Color.Gray) Color.White else Color.Black,
                fontSize = 20.sp
            )
        }
    }
}

// Функция для смены цвета кнопки
private fun changeColor(colorName: String, onColorFound: (Color) -> Unit) {
    // Удаляем все пробелы и приводим к нижнему регистру
    val formattedName = colorName
        .trim()                    // Удаляем пробелы в начале и конце
        .replace("\\s".toRegex(), "")   // Удаляем все пробелы внутри
        .lowercase()               // Приводим к нижнему регистру
        .replaceFirstChar { it.uppercase() } // Первую букву делаем заглавной для сравнения с мапой

    // Ищем цвет в мапе (сравниваем без учета регистра)
    val color = colorsMap.entries.find {
        it.key.lowercase() == formattedName.lowercase()
    }?.value

    if (color != null) {
        // Цвет найден - меняем цвет кнопки
        onColorFound(color)
        // Логируем успешное изменение цвета
        Log.d("ColorApp", "Цвет '$colorName' найден, применяем цвет к кнопке")
    } else {
        // Цвет не найден - кнопка остается неизменной
        Log.w("ColorApp", "Цвет '$colorName' не найден в палитре")
        // Дополнительно логируем доступные цвета
        Log.d("ColorApp", "Доступные цвета: ${colorsMap.keys.joinToString()}")
    }
}

// Карта цветов (структура данных для хранения цветов по названиям)
private val colorsMap = mapOf(
    "Red" to Color.Red,
    "Orange" to Color(0xFFFFA500),
    "Yellow" to Color.Yellow,
    "Green" to Color.Green,
    "Blue" to Color.Blue,
    "Indigo" to Color(0xFF4B0082),
    "Violet" to Color(0xFFEE82EE)
)