package adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.peer2party.R

/*
class ChatRecyclerAdapter internal constructor(
    private val context: Context,
    //private val dao: DbDao = DataBaseHolder.getInstance(context).dao(),
    private val inflater: LayoutInflater = LayoutInflater.from(context)
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val RECEIVE_TEXT = 0
    private val SEND_TEXT = 1
    private val RECEIVE_IMAGE = 2
    private val SEND_IMAGE = 3

    class TextSendViewHolder(
        view: View,
        val textView: TextView = view.findViewById(R.id.chatTextSend),
        val delete: ImageView = view.findViewById(R.id.deleteChatSend),
        val info: LinearLayout = view.findViewById(R.id.infoChatSend),
        val date: TextView = view.findViewById(R.id.dateChatSend)
    ) : RecyclerView.ViewHolder(view)
    class TextReceivedViewHolder(
        view: View,
        val textView: TextView = view.findViewById(R.id.chatTextRec),
        val alias: TextView = view.findViewById(R.id.userText),
        val delete: ImageView = view.findViewById(R.id.deleteChatRec),
        val info: LinearLayout = view.findViewById(R.id.infoChatRec),
        val date: TextView = view.findViewById(R.id.dateChatRec)
    ) : RecyclerView.ViewHolder(view)
    class ImageSendViewHolder(
        view: View,
        val imageView: ImageView = view.findViewById(R.id.imageViewSend),
        val delete: ImageView = view.findViewById(R.id.deleteImageSend),
        val info: LinearLayout = view.findViewById(R.id.infoImageSend),
        val date: TextView = view.findViewById(R.id.dateImageSend),
        val size: TextView = view.findViewById(R.id.sizeImageSend)
    ) : RecyclerView.ViewHolder(view)
    class ImageReceivedViewHolder(
        view: View,
        val imageView: ImageView = view.findViewById(R.id.imageViewReceived),
        val alias: TextView = view.findViewById(R.id.userTextImage),
        val delete: ImageView = view.findViewById(R.id.deleteImageRec),
        val info: LinearLayout = view.findViewById(R.id.infoImageRec),
        val date: TextView = view.findViewById(R.id.dateImageRec),
        val size: TextView = view.findViewById(R.id.sizeImageRec)
    ) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

}*/
