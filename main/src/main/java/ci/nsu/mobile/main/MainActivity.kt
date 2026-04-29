package ci.nsu.mobile.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ci.nsu.mobile.main.ui.theme.PracticeTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PracticeTheme {
                ShoppingListScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(viewModel: ShoppingViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage.isNotEmpty() &&
            (uiState.errorMessage.contains("✅") || uiState.errorMessage.contains("❌") || uiState.errorMessage.contains("⚠️") || uiState.errorMessage.contains("🗑️"))) {
            delay(3000)
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📝 Список покупок") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    // Кнопка "Удалить всё" в верхней панели
                    if (uiState.items.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.deleteAllItems() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Удалить всё",
                                tint = Color.Red
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Поле ввода и кнопка добавления
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = uiState.newItemText,
                            onValueChange = { viewModel.onNewItemTextChanged(it) },
                            label = { Text("Название товара") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            isError = uiState.errorMessage.contains("❌") || uiState.errorMessage.contains("⚠️")
                        )

                        Button(
                            onClick = { viewModel.addItem() },
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Text("➕ Добавить")
                        }
                    }

                    // Кнопки массовых операций
                    if (uiState.items.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Кнопка "Удалить всё"
                            Button(
                                onClick = { viewModel.deleteAllItems() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text("Удалить всё")
                            }

                            // Кнопка "Удалить купленные" (дополнительно, по желанию)
                            if (uiState.items.any { it.isBought }) {
                                Button(
                                    onClick = { viewModel.deleteBoughtItems() },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFF9800)
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                    Text("Удалить купленные")
                                }
                            }
                        }
                    }

                    // Отображение сообщений
                    if (uiState.errorMessage.isNotEmpty()) {
                        Text(
                            text = uiState.errorMessage,
                            modifier = Modifier.padding(top = 8.dp),
                            color = when {
                                uiState.errorMessage.contains("✅") -> Color.Green
                                uiState.errorMessage.contains("⚠️") -> Color(0xFFFF9800)
                                uiState.errorMessage.contains("🗑️") -> Color(0xFF2196F3)
                                else -> Color.Red
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Статистика списка
            if (uiState.items.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(
                            text = "📊 Всего: ${uiState.items.size}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "✅ Куплено: ${uiState.items.count { it.isBought }}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Green
                        )
                        Text(
                            text = "⏳ Осталось: ${uiState.items.count { !it.isBought }}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFF9800)
                        )
                    }
                }
            } else {
                // Пустое состояние
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = "🛒 Список покупок пуст. Добавьте товары!",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }

            // Список товаров
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.items) { item ->
                    ShoppingItemRow(
                        item = item,
                        onToggleBought = { viewModel.toggleItemBought(item.id) },
                        onDelete = { viewModel.deleteItem(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ShoppingItemRow(
    item: ShoppingItem,
    onToggleBought: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = item.isBought,
                    onCheckedChange = { onToggleBought() }
                )

                Text(
                    text = item.name,
                    modifier = Modifier.padding(start = 8.dp),
                    style = if (item.isBought) {
                        MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    } else {
                        MaterialTheme.typography.bodyLarge
                    }
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}