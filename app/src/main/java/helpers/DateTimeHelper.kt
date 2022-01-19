package helpers

import java.text.SimpleDateFormat
import java.util.*

object DateTimeHelper {

    @JvmStatic
    fun fetchDateTime(): String {
        val calendar: Calendar = Calendar.getInstance()
        val format = SimpleDateFormat("dd-MM-yyyy 'at' k:mm:ss", Locale.getDefault())
        return format.format(calendar.time).toString()
    }
}