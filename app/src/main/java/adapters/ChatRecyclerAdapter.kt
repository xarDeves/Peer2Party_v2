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
import data.DatabaseHolder
import data.DbDao
import data.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class ChatRecyclerAdapter internal constructor(
    private val context: Context,
    private val dao: DbDao = DatabaseHolder.getInstance(context).dao(),
    private val inflater: LayoutInflater = LayoutInflater.from(context)
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var allMessages = emptyList<Message>()

    abstract class TextViewHolder(
        view: View,
        val textView: TextView = view.findViewById(R.id.chatTextSend),
        val delete: ImageView = view.findViewById(R.id.deleteChatSend),
        val info: LinearLayout = view.findViewById(R.id.infoChatSend),
        val date: TextView = view.findViewById(R.id.dateChatSend),
        var clicked: Boolean = false
    ) : RecyclerView.ViewHolder(view)

    abstract class ImageViewHolder(
        view: View,
        val imageView: ImageView = view.findViewById(R.id.imageViewSend),
        val delete: ImageView = view.findViewById(R.id.deleteImageSend),
        val info: LinearLayout = view.findViewById(R.id.infoImageSend),
        val date: TextView = view.findViewById(R.id.dateImageSend),
        val size: TextView = view.findViewById(R.id.sizeImageSend)
    ) : RecyclerView.ViewHolder(view)

    class TextSendViewHolder(
        view: View
    ) : TextViewHolder(view)

    class TextReceivedViewHolder(
        view: View,
        val alias: TextView = view.findViewById(R.id.userText),
    ) : TextViewHolder(view)

    class ImageSendViewHolder(
        view: View,
    ) : ImageViewHolder(view)

    class ImageReceivedViewHolder(
        view: View,
        val alias: TextView = view.findViewById(R.id.userTextImage),
    ) : ImageViewHolder(view)

    internal fun setMessages(entities: List<Message>) {

        this.allMessages = entities
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return allMessages[position].messageType.type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val view = inflater.inflate(viewType, parent, false)
        return TextSendViewHolder(view)
    }

    //FIXME viewholders retain toggle state after deletion
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {

            R.layout.chat_head_send -> {

                (holder as TextSendViewHolder).textView.text = allMessages[position].payload
                holder.date.text = allMessages[position].date

                /*if (holder.clicked) {
                    holder.delete.visibility = View.VISIBLE
                    holder.info.visibility = View.VISIBLE
                } else {
                    holder.delete.visibility = View.GONE
                    holder.info.visibility = View.GONE
                }*/
                //toggleBubbleState(holder)

                holder.delete.setOnClickListener {
                    holder.clicked = false
                    CoroutineScope(IO).launch { dao.delete(allMessages[position].key!!) }
                    notifyItemRemoved(position)
                }

                holder.itemView.setOnClickListener {
                    /*clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    clip = ClipData.newPlainText("copied", holder.textView.text)
                    clipboard.primaryClip = clip*/

                    holder.clicked = holder.clicked != true

                    toggleBubbleState(holder)
                }

            }
        }
    }

    private fun toggleBubbleState(holder: TextSendViewHolder) {
        if (holder.clicked) {
            holder.delete.visibility = View.GONE
            holder.info.visibility = View.GONE
        } else {
            holder.delete.visibility = View.VISIBLE
            holder.info.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return allMessages.size
    }

}
