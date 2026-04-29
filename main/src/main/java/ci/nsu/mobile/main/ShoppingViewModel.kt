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
    val errorMessage: String = "" // добавляем поле для сообщений об ошибках
)

class ShoppingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    fun onNewItemTextChanged(text: String) {
        _uiState.update {
            it.copy(
                newItemText = text,
                errorMessage = "" // очищаем ошибку при изменении текста
            )
        }
    }

    /**
     * Проверка: есть ли в строке недопустимые символы
     * Разрешаем: буквы (RU/EN), цифры, пробелы, дефис, точку
     * Запрещаем: спецсимволы типа !@#$%^&*()_+={}[]|\\:;\"'<>,?/`~
     */
    private fun hasInvalidCharacters(text: String): Boolean {
        val allowedPattern = Regex("^[a-zA-Zа-яА-ЯёЁ0-9\\s\\-\\.]+$")
        return !allowedPattern.matches(text)
    }

    /**
     * Проверка: есть ли товар с таким названием в списке (без учета регистра)
     */
    private fun isDuplicateItem(name: String): Boolean {
        val trimmedName = name.trim()
        return _uiState.value.items.any {
            it.name.equals(trimmedName, ignoreCase = true)
        }
    }

    fun addItem() {
        val currentText = _uiState.value.newItemText

        // ПРОВЕРКА 1: Пустая строка
        if (currentText.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "❌ Ошибка: Название товара не может быть пустым!")
            }
            return
        }

        val trimmedText = currentText.trim()

        // ПРОВЕРКА 2: Недопустимые символы
        if (hasInvalidCharacters(trimmedText)) {
            _uiState.update {
                it.copy(errorMessage = "❌ Ошибка: Недопустимые символы! Используйте только буквы, цифры, пробелы, дефис и точку.")
            }
            return
        }

        // ПРОВЕРКА 3: Слишком длинное название (максимум 50 символов)
        if (trimmedText.length > 50) {
            _uiState.update {
                it.copy(errorMessage = "❌ Ошибка: Название не должно превышать 50 символов! (сейчас ${trimmedText.length})")
            }
            return
        }

        // ПРОВЕРКА 4: Слишком короткое название (минимум 2 символа)
        if (trimmedText.length < 2) {
            _uiState.update {
                it.copy(errorMessage = "❌ Ошибка: Название должно содержать минимум 2 символа!")
            }
            return
        }

        // ПРОВЕРКА 5: Дубликат товара
        if (isDuplicateItem(trimmedText)) {
            _uiState.update {
                it.copy(errorMessage = "⚠️ Внимание: Товар \"$trimmedText\" уже есть в списке!")
            }
            return
        }

        // ВСЕ ПРОВЕРКИ ПРОЙДЕНЫ - добавляем товар
        _uiState.update { currentState ->
            val newId = (currentState.items.maxOfOrNull { it.id } ?: 0) + 1
            val newItem = ShoppingItem(
                id = newId,
                name = trimmedText
            )
            currentState.copy(
                items = currentState.items + newItem,
                newItemText = "",
                errorMessage = "✅ Товар \"$trimmedText\" успешно добавлен!" // сообщение об успехе
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

    // Очистить сообщение об ошибке (можно вызвать через таймер)
    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = "") }
    }
}