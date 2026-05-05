package ci.nsu.mobile.main.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Указываем список таблиц (entities) и версию БД
@Database(entities = [Deposit::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun depositDao(): DepositDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        // Singleton: гарантирует, что у приложения будет только один экземпляр базы данных
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, "deposit_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}