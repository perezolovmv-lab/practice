package ci.nsu.mobile.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import ci.nsu.mobile.main.ui.theme.DepositViewModel
import ci.nsu.mobile.main.ui.theme.PracticeTheme
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Применяем тему приложения
            PracticeTheme {
                // Основной контейнер Surface для поддержки фона темы
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController() // Контроллер для переключения экранов
                    val vm: DepositViewModel = viewModel()    // Подключаем ViewModel для обмена данными

                    // NavHost определяет "карту" навигации. startDestination - экран при запуске.
                    NavHost(navController = navController, startDestination = "main") {
                        composable("main") { MainMenu(navController) }
                        composable("step1") { Step1(navController, vm) }
                        composable("step2") { Step2(navController, vm) }
                        composable("result") { ResultScreen(navController, vm) }
                        composable("history") { HistoryScreen(navController, vm) }
                    }
                }
            }
        }
    }
}

// --- ЭКРАН 1: ГЛАВНОЕ МЕНЮ ---
@Composable
fun MainMenu(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Расчёт вкладов", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(32.dp))

        // Кнопки навигации по маршрутам, указанным в NavHost
        Button(onClick = { navController.navigate("step1") }, Modifier.fillMaxWidth()) { Text("Рассчитать") }
        Button(onClick = { navController.navigate("history") }, Modifier.fillMaxWidth()) { Text("История расчётов") }

        // Кнопка закрытия процесса приложения
        OutlinedButton(onClick = { exitProcess(0) }, Modifier.fillMaxWidth()) { Text("Закрыть приложение") }
    }
}

// --- ЭКРАН 2: ВВОД СУММЫ И СРОКА ---
@Composable
fun Step1(navController: NavController, vm: DepositViewModel) {
    Column(Modifier.padding(24.dp)) {
        Text("Этап 1: Основные параметры", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        // Поля ввода связаны напрямую с переменными внутри ViewModel
        OutlinedTextField(
            value = vm.amount.value,
            onValueChange = { vm.amount.value = it },
            label = { Text("Стартовый взнос (руб)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = vm.months.value,
            onValueChange = { vm.months.value = it },
            label = { Text("Срок (месяцев)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { navController.navigate("main") }) { Text("В начало") }
            Button(onClick = {
                // Простая валидация перед переходом на следующий шаг
                if (vm.amount.value.isNotBlank() && vm.months.value.isNotBlank()) {
                    navController.navigate("step2")
                }
            }) { Text("Далее") }
        }
    }
}

// --- ЭКРАН 3: ПРОЦЕНТ И ПОПОЛНЕНИЕ ---
@Composable
fun Step2(navController: NavController, vm: DepositViewModel) {
    val months = vm.months.value.toIntOrNull() ?: 0
    var expanded by remember { mutableStateOf(false) }

    // Логика выбора ставки согласно заданию
    val availableRate = when {
        months < 6 -> 15.0
        months < 12 -> 10.0
        else -> 5.0
    }

    // Принудительно обновляем ставку во ViewModel при загрузке экрана
    LaunchedEffect(Unit) { vm.rate.value = availableRate }

    Column(Modifier.padding(24.dp)) {
        Text("Этап 2: Доп. параметры", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        Text("Процентная ставка (автоматически):")
        // Выпадающее меню для выбора ставки
        Box(modifier = Modifier.fillMaxWidth().clickable { expanded = true }.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${vm.rate.value}%", style = MaterialTheme.typography.bodyLarge)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text("$availableRate%") },
                    onClick = { vm.rate.value = availableRate; expanded = false }
                )
            }
        }

        OutlinedTextField(
            value = vm.topUp.value,
            onValueChange = { vm.topUp.value = it },
            label = { Text("Ежемесячное пополнение (руб)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            // popBackStack возвращает на предыдущую страницу в стеке
            TextButton(onClick = { navController.popBackStack() }) { Text("Назад") }
            Button(onClick = { navController.navigate("result") }) { Text("Рассчитать") }
        }
    }
}

// --- ЭКРАН 4: РЕЗУЛЬТАТ ---
@Composable
fun ResultScreen(navController: NavController, vm: DepositViewModel) {
    // Вызываем расчет функции из ViewModel
    val result = vm.calculateResult()

    Card(Modifier.padding(24.dp).fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Итоги расчета", fontWeight = FontWeight.Bold)
            Divider(Modifier.padding(vertical = 8.dp))

            Text("Стартовый взнос: ${result.initialAmount} руб")
            Text("Срок: ${result.months} мес")
            Text("Ставка: ${result.rate}%")
            // Форматируем вывод до 2 знаков после запятой
            Text("Итоговая сумма: ${String.format("%.2f", result.finalAmount)} руб",
                color = Color(0xFF388E3C), fontWeight = FontWeight.Bold)
            Text("Чистая прибыль: ${String.format("%.2f", result.interestEarned)} руб")

            Spacer(Modifier.height(16.dp))

            // Кнопка сохранения отправляет объект Deposit в БД Room
            Button(onClick = {
                vm.save(result)
                navController.navigate("main")
            }, Modifier.fillMaxWidth()) { Text("Сохранить и выйти") }

            TextButton(onClick = { navController.navigate("main") }, Modifier.fillMaxWidth()) { Text("В начало") }
        }
    }
}

// --- ЭКРАН 5: ИСТОРИЯ ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController, vm: DepositViewModel) {
    // Наблюдаем за списком из БД. При удалении или добавлении экран обновится сам.
    val history by vm.history.observeAsState(emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История расчётов") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    // Иконка корзины для полной очистки таблицы Room
                    if (history.isNotEmpty()) {
                        IconButton(onClick = { vm.clearHistory() }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Очистить всё", tint = Color.Red)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (history.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("История пуста")
            }
        } else {
            // LazyColumn - эффективный список (аналог RecyclerView)
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)) {
                items(history) { item ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Дата: ${item.date}", fontSize = 12.sp, color = Color.Gray)
                            Spacer(Modifier.height(4.dp))
                            Text("Взнос: ${item.initialAmount} руб")
                            Text("Итог: ${String.format("%.2f", item.finalAmount)} руб",
                                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}