package ci.nsu.mobile.main.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DepositDao {
    @Insert
    suspend fun insert(deposit: Deposit)

    @Query("SELECT * FROM deposits ORDER BY id DESC")
    fun getAllHistory(): LiveData<List<Deposit>>
}