package data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message")
data class Message(

    var messageType: MessageType,
    var payload: String?,
    var date: String? = null,
    var alias: String? = null,
) {
    @PrimaryKey(autoGenerate = true)
    var key: Int? = null
}
