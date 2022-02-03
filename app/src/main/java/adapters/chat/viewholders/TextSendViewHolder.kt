package adapters.chat.viewholders

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.peer2party.R

class TextSendViewHolder(
    view: View,
    delete: ImageView = view.findViewById(R.id.deleteChatSend),
    info: LinearLayout = view.findViewById(R.id.infoChatSend),
    val date: TextView = view.findViewById(R.id.dateChatSend),
    val textView: TextView = view.findViewById(R.id.chatTextSend),
) : BaseViewHolder(view, delete, info)