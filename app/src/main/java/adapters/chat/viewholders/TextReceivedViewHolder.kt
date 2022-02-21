package adapters.chat.viewholders

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.peer2party.R

class TextReceivedViewHolder(
    view: View,
    delete: ImageView = view.findViewById(R.id.deleteChatRec),
    info: LinearLayout = view.findViewById(R.id.infoChatRec),
    val date: TextView = view.findViewById(R.id.dateChatRec),
    val alias: TextView = view.findViewById(R.id.userText),
    val textView: TextView = view.findViewById(R.id.chatTextRec)
) : BaseViewHolder(view, delete, info)