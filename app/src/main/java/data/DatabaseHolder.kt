package data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [Message::class], version = 1, exportSchema = false)
abstract class DatabaseHolder : RoomDatabase() {

    abstract fun dao(): DbDao

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: DatabaseHolder? = null

        fun getInstance(context: Context): DatabaseHolder {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): DatabaseHolder {
            return Room.databaseBuilder(context, DatabaseHolder::class.java, "entitiesDb")
                .build()
        }
    }

}