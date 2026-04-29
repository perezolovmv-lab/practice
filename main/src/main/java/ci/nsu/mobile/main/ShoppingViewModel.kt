package ci.nsu.mobile.main

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ShoppingItem(
    val id: Int,
    val name: String,
    val isBought: Boolean = false
)

data class ShoppingListUiState(
    val items: List<ShoppingItem> = emptyList(),
    val newItemText: String = "",
    val errorMessage: String = ""
)

class ShoppingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    fun onNewItemTextChanged(text: String) {
        _uiState.update {
            it.copy(
                newItemText = text,
                errorMessage = ""
            )
        }
    }

    private fun hasInvalidCharacters(text: String): Boolean {
        val allowedPattern = Regex("^[a-zA-Zа-яА-ЯёЁ0-9\\s\\-\\.]+$")
        return !allowedPattern.matches(text)
    }

    private fun isDuplicateItem(name: String): Boolean {
        val trimmedName = name.trim()
        return _uiState.value.items.any {
            it.name.equals(trimmedName, ignoreCase = true)
        }
    }

    fun addItem() {
        val currentText = _uiState.value.newItemText

        if (currentText.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "❌ Ошибка: Название товара не может быть пустым!")
            }
            return
        }

        val trimmedText = currentText.trim()

        if (hasInvalidCharacters(trimmedText)) {
            _uiState.update {
                it.copy(errorMessage = "❌ Ошибка: Недопустимые символы! Используйте только буквы, цифры, пробелы, дефис и точку.")
            }
            return
        }

        if (trimmedText.length > 50) {
            _uiState.update {
                it.copy(errorMessage = "❌ Ошибка: Название не должно превышать 50 символов! (сейчас ${trimmedText.length})")
            }
            return
        }

        if (trimmedText.length < 2) {
            _uiState.update {
                it.copy(errorMessage = "❌ Ошибка: Название должно содержать минимум 2 символа!")
            }
            return
        }

        if (isDuplicateItem(trimmedText)) {
            _uiState.update {
                it.copy(errorMessage = "⚠️ Внимание: Товар \"$trimmedText\" уже есть в списке!")
            }
            return
        }

        _uiState.update { currentState ->
            val newId = (currentState.items.maxOfOrNull { it.id } ?: 0) + 1
            val newItem = ShoppingItem(
                id = newId,
                name = trimmedText
            )
            currentState.copy(
                items = currentState.items + newItem,
                newItemText = "",
                errorMessage = "✅ Товар \"$trimmedText\" успешно добавлен!"
            )
        }
    }

    fun toggleItemBought(itemId: Int) {
        _uiState.update { currentState ->
            val updatedItems = currentState.items.map { item ->
                if (item.id == itemId) {
                    item.copy(isBought = !item.isBought)
                } else {
                    item
                }
            }
            currentState.copy(items = updatedItems)
        }
    }

    fun deleteItem(itemId: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                items = currentState.items.filter { it.id != itemId }
            )
        }
    }

    /**
     * 🗑️ УДАЛЕНИЕ ВСЕГО СПИСКА
     * Очищает все товары и показывает сообщение
     */
    fun deleteAllItems() {
        _uiState.update { currentState ->
            if (currentState.items.isEmpty()) {
                // Если список уже пуст - показываем сообщение
                currentState.copy(
                    errorMessage = "📭 Список и так пуст!"
                )
            } else {
                // Очищаем список и показываем сколько удалили
                currentState.copy(
                    items = emptyList(),
                    errorMessage = "🗑️ Удалено ${currentState.items.size} товаров!",
                    newItemText = "" // очищаем поле ввода
                )
            }
        }
    }

    /**
     * Удаление только купленных товаров
     */
    fun deleteBoughtItems() {
        _uiState.update { currentState ->
            val boughtItems = currentState.items.filter { it.isBought }
            val remainingItems = currentState.items.filter { !it.isBought }

            if (boughtItems.isEmpty()) {
                currentState.copy(
                    errorMessage = "⚠️ Нет купленных товаров для удаления!"
                )
            } else {
                currentState.copy(
                    items = remainingItems,
                    errorMessage = "✅ Удалено ${boughtItems.size} купленных товаров!"
                )
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = "") }
    }
}