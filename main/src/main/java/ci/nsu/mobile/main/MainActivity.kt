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
            PracticeTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    val vm: DepositViewModel = viewModel()

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

@Composable
fun MainMenu(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Расчёт вкладов", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(32.dp))
        Button(onClick = { navController.navigate("step1") }, Modifier.fillMaxWidth()) { Text("Рассчитать") }
        Button(onClick = { navController.navigate("history") }, Modifier.fillMaxWidth()) { Text("История расчётов") }
        OutlinedButton(onClick = { exitProcess(0) }, Modifier.fillMaxWidth()) { Text("Закрыть приложение") }
    }
}

@Composable
fun Step1(navController: NavController, vm: DepositViewModel) {
    Column(Modifier.padding(24.dp)) {
        Text("Этап 1: Основные параметры", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = vm.amount.value, onValueChange = { vm.amount.value = it }, label = { Text("Стартовый взнос (руб)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = vm.months.value, onValueChange = { vm.months.value = it }, label = { Text("Срок (месяцев)") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { navController.navigate("main") }) { Text("В начало") }
            Button(onClick = {
                if (vm.amount.value.isNotBlank() && vm.months.value.isNotBlank()) navController.navigate("step2")
            }) { Text("Далее") }
        }
    }
}

@Composable
fun Step2(navController: NavController, vm: DepositViewModel) {
    val months = vm.months.value.toIntOrNull() ?: 0
    var expanded by remember { mutableStateOf(false) }
    val availableRate = when {
        months < 6 -> 15.0
        months < 12 -> 10.0
        else -> 5.0
    }
    LaunchedEffect(Unit) { vm.rate.value = availableRate }

    Column(Modifier.padding(24.dp)) {
        Text("Этап 2: Доп. параметры", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        Text("Выберите ставку:")
        Box(modifier = Modifier.fillMaxWidth().clickable { expanded = true }.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${vm.rate.value}%", style = MaterialTheme.typography.bodyLarge)
                Icon(Icons.Default.ArrowDropDown, null)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(text = { Text("$availableRate%") }, onClick = { vm.rate.value = availableRate; expanded = false })
            }
        }
        OutlinedTextField(value = vm.topUp.value, onValueChange = { vm.topUp.value = it }, label = { Text("Ежемесячное пополнение (руб)") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { navController.popBackStack() }) { Text("Назад") }
            Button(onClick = { navController.navigate("result") }) { Text("Рассчитать") }
        }
    }
}

@Composable
fun ResultScreen(navController: NavController, vm: DepositViewModel) {
    val result = vm.calculateResult()
    Card(Modifier.padding(24.dp).fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Итоги расчета", fontWeight = FontWeight.Bold)
            Divider(Modifier.padding(vertical = 8.dp))
            Text("Итоговая сумма: ${String.format("%.2f", result.finalAmount)} руб", color = Color(0xFF388E3C), fontWeight = FontWeight.Bold)
            Text("Чистая прибыль: ${String.format("%.2f", result.interestEarned)} руб")
            Spacer(Modifier.height(16.dp))
            Button(onClick = { vm.save(result); navController.navigate("main") }, Modifier.fillMaxWidth()) { Text("Сохранить и выйти") }
            TextButton(onClick = { navController.navigate("main") }, Modifier.fillMaxWidth()) { Text("В начало") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController, vm: DepositViewModel) {
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
                    if (history.isNotEmpty()) {
                        IconButton(onClick = { vm.clearHistory() }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Очистить", tint = Color.Red)
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
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)) {
                items(history) { item ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Дата: ${item.date}", fontSize = 12.sp, color = Color.Gray)
                            Text("Взнос: ${item.initialAmount} руб")
                            Text("Итого: ${String.format("%.2f", item.finalAmount)} руб", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}