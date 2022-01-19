package data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DbDao {

    @Insert
    suspend fun insert(entity: Message)

    @Query("DELETE FROM message WHERE `key` = :position")
    suspend fun delete(position: Int)

    @Query("Select * from message")
    fun getAllMessages(): LiveData<List<Message>>

}