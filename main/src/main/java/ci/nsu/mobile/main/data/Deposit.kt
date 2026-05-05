package ci.nsu.mobile.main.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity помечает класс как таблицу в базе данных SQLite
@Entity(tableName = "deposits")
data class Deposit(
    // @PrimaryKey(autoGenerate = true) создает уникальный ID для каждой записи автоматически
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val initialAmount: Double,
    val months: Int,
    val rate: Double,
    val monthlyTopUp: Double,
    val finalAmount: Double,
    val interestEarned: Double,
    val date: String // Дата сохранения в формате строки
)