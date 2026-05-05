package ci.nsu.mobile.main.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DepositDao {
    // Вставка новой записи. suspend означает, что функция будет работать в фоновом потоке
    @Insert
    suspend fun insert(deposit: Deposit)

    // Запрос всех записей, отсортированных от новых к старым.
    // LiveData позволяет UI автоматически обновляться при изменении данных.
    @Query("SELECT * FROM deposits ORDER BY id DESC")
    fun getAllHistory(): LiveData<List<Deposit>>

    // Полная очистка таблицы
    @Query("DELETE FROM deposits")
    suspend fun deleteAll()
}