package ci.nsu.mobile.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Модель данных для одного товара в списке покупок
 * @param id уникальный идентификатор товара
 * @param name название товара
 * @param isBought статус: куплен/не куплен
 */
data class ShoppingItem(
    val id: Int,
    val name: String,
    val isBought: Boolean = false // по умолчанию false (не куплен)
)

/**
 * Состояние UI для экрана списка покупок
 * @param items список всех товаров
 * @param newItemText текст из поля ввода нового товара
 */
data class ShoppingListUiState(
    val items: List<ShoppingItem> = emptyList(), // пустой список по умолчанию
    val newItemText: String = "" // пустая строка по умолчанию
)

/**
 * ViewModel для управления списком покупок
 * Хранит состояние и бизнес-логику
 */
class ShoppingViewModel : ViewModel() {

    // Приватное изменяемое состояние (MutableStateFlow)
    private val _uiState = MutableStateFlow(ShoppingListUiState())

    // Публичное неизменяемое состояние (StateFlow) - только для чтения из UI
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    /**
     * Обновляет текст из поля ввода
     * Вызывается при каждом изменении текста пользователем
     * @param text новый текст из TextField
     */
    fun onNewItemTextChanged(text: String) {
        _uiState.update { it.copy(newItemText = text) } // обновляем только поле newItemText
    }

    /**
     * Добавляет новый товар в список
     * Проверяет, что текст не пустой, затем добавляет товар
     */
    fun addItem() {
        val currentText = _uiState.value.newItemText // берем текущий текст из состояния

        if (currentText.isNotBlank()) { // проверка: не пустой и не пробелы
            _uiState.update { currentState ->
                // генерируем новый ID: максимальный существующий + 1, или 1 если список пуст
                val newId = (currentState.items.maxOfOrNull { it.id } ?: 0) + 1

                // создаем новый товар
                val newItem = ShoppingItem(
                    id = newId,
                    name = currentText.trim() // удаляем лишние пробелы по краям
                )

                // обновляем состояние: добавляем товар и очищаем поле ввода
                currentState.copy(
                    items = currentState.items + newItem, // добавляем новый товар в конец списка
                    newItemText = "" // очищаем поле ввода
                )
            }
        }
    }

    /**
     * Переключает статус товара (куплен/не куплен)
     * @param itemId ID товара, который нужно изменить
     */
    fun toggleItemBought(itemId: Int) {
        _uiState.update { currentState ->
            val updatedItems = currentState.items.map { item ->
                if (item.id == itemId) {
                    // если нашли нужный товар - инвертируем его статус
                    item.copy(isBought = !item.isBought)
                } else {
                    // остальные товары оставляем без изменений
                    item
                }
            }
            // обновляем список во всем состоянии
            currentState.copy(items = updatedItems)
        }
    }

    /**
     * Удаляет товар из списка
     * @param itemId ID товара для удаления
     */
    fun deleteItem(itemId: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                // оставляем только те товары, у которых ID не совпадает с удаляемым
                items = currentState.items.filter { it.id != itemId }
            )
        }
    }
}