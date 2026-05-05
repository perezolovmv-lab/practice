package ci.nsu.mobile.main.ui.theme

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import ci.nsu.mobile.main.data.AppDatabase
import ci.nsu.mobile.main.data.Deposit
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.pow

class DepositViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).depositDao()
    val history: LiveData<List<Deposit>> = dao.getAllHistory()

    // Состояния полей ввода
    var amount = mutableStateOf("")
    var months = mutableStateOf("")
    var rate = mutableStateOf(15.0)
    var topUp = mutableStateOf("")

    fun calculateResult(): Deposit {
        val p = amount.value.toDoubleOrNull() ?: 0.0
        val n = months.value.toIntOrNull() ?: 0
        val r = rate.value / 100 / 12
        val pmt = topUp.value.toDoubleOrNull() ?: 0.0

        val finalAmount = if (r > 0) {
            p * (1 + r).pow(n.toDouble()) + pmt * (((1 + r).pow(n.toDouble()) - 1) / r)
        } else {
            p + (pmt * n)
        }

        return Deposit(
            initialAmount = p,
            months = n,
            rate = rate.value,
            monthlyTopUp = pmt,
            finalAmount = finalAmount,
            interestEarned = finalAmount - p - (pmt * n),
            date = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
        )
    }

    // Сохранение и автоматический сброс полей
    fun saveAndReset(deposit: Deposit) = viewModelScope.launch {
        dao.insert(deposit)
        clearInputs() // Обнуляем поля сразу после записи в БД
    }

    fun clearHistory() = viewModelScope.launch {
        dao.deleteAll()
    }

    // Метод для обнуления всех строк ввода
    fun clearInputs() {
        amount.value = ""
        months.value = ""
        topUp.value = ""
    }
}