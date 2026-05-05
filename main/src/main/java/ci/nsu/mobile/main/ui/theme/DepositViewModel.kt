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
    // Инициализация DAO через наш синглтон БД
    private val dao = AppDatabase.getDatabase(application).depositDao()

    // Переменная для хранения списка истории, которая будет "наблюдаться" в UI
    val history: LiveData<List<Deposit>> = dao.getAllHistory()

    // mutableStateOf позволяет Compose перерисовывать текстовые поля при вводе
    var amount = mutableStateOf("")
    var months = mutableStateOf("")
    var rate = mutableStateOf(15.0)
    var topUp = mutableStateOf("")

    // Основная математическая логика расчёта
    fun calculateResult(): Deposit {
        val p = amount.value.toDoubleOrNull() ?: 0.0     // Стартовая сумма
        val n = months.value.toIntOrNull() ?: 0         // Кол-во месяцев
        val r = rate.value / 100 / 12                   // Месячная ставка
        val pmt = topUp.value.toDoubleOrNull() ?: 0.0   // Ежемесячный взнос

        // Формула сложного процента: сумма * (1+r)^n + пополнения
        val finalAmount = if (r > 0) {
            p * (1 + r).pow(n.toDouble()) + pmt * (((1 + r).pow(n.toDouble()) - 1) / r)
        } else {
            p + (pmt * n) // Если процент 0
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

    // Сохранение в БД через корутину (viewModelScope), чтобы не вешать экран
    fun save(deposit: Deposit) = viewModelScope.launch {
        dao.insert(deposit)
    }

    // Удаление всех записей из БД
    fun clearHistory() = viewModelScope.launch {
        dao.deleteAll()
    }

    // Очистка полей ввода для нового расчёта
    fun clearInputs() {
        amount.value = ""
        months.value = ""
        topUp.value = ""
    }
}