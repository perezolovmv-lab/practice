package ci.nsu.mobile.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val newItemText: String = ""
)

class ShoppingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    fun onNewItemTextChanged(text: String) {
        _uiState.update { it.copy(newItemText = text) }
    }

    fun addItem() {
        val currentText = _uiState.value.newItemText
        if (currentText.isNotBlank()) {
            _uiState.update { currentState ->
                val newId = (currentState.items.maxOfOrNull { it.id } ?: 0) + 1
                val newItem = ShoppingItem(
                    id = newId,
                    name = currentText.trim()
                )
                currentState.copy(
                    items = currentState.items + newItem,
                    newItemText = ""
                )
            }
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
}