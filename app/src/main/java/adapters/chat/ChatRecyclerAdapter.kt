package adapters.chat

import adapters.chat.viewholders.ImageSendViewHolder
import adapters.chat.viewholders.TextSendViewHolder
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private lateinit var viewHolder: RecyclerView.ViewHolder
    private lateinit var view: View

    fun setMessages(entities: List<Message>) {
        this.allMessages = entities
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return allMessages[position].messageType.type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        when (viewType) {
            R.layout.chat_head_send -> {
                view = inflater.inflate(viewType, parent, false)
                viewHolder = TextSendViewHolder(view)
            }
            R.layout.image_send -> {
                view = inflater.inflate(viewType, parent, false)
                viewHolder = ImageSendViewHolder(view)
            }
        }

        return viewHolder
    }

    //FIXME viewholders retain toggle state after deletion
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {

            R.layout.chat_head_send -> {

                (holder as TextSendViewHolder).textView.text = allMessages[position].payload
                holder.date.text = allMessages[position].date

                holder.delete.setOnClickListener {
                    CoroutineScope(IO).launch { dao.delete(allMessages[position].key!!) }
                    toggleBubbleState(holder)
                    notifyItemRemoved(position)
                }

                holder.itemView.setOnClickListener {
                    /*clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    clip = ClipData.newPlainText("copied", holder.textView.text)
                    clipboard.primaryClip = clip*/

                    toggleBubbleState(holder)
                }

            }

            R.layout.image_send -> {
                (holder as ImageSendViewHolder)
                holder.imageView.setImageURI(Uri.parse(allMessages[position].payload!!))

                /*if (file.absoluteFile.exists()) {
                    holder.imageView.setImageURI(Uri.parse(path))
                } else {
                    holder.imageView.setImageResource(
                        R.drawable.not_found
                    )
                }*/

                //holder.date.text = allMessages[position].date
                //holder.size.text = allMessages[position].size
            }

        }
    }

    private fun toggleBubbleState(holder: TextSendViewHolder) {
        /*if (holder.clicked) {
            holder.delete.visibility = View.GONE
            holder.info.visibility = View.GONE
        } else {
            holder.delete.visibility = View.VISIBLE
            holder.info.visibility = View.VISIBLE
        }
        holder.clicked = holder.clicked == false*/
    }

    override fun getItemCount(): Int {
        return allMessages.size
    }

}
