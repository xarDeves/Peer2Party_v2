package adapters.chat

import adapters.chat.viewholders.*
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.documentfile.provider.DocumentFile
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
    private var toggled: Int = -1

    private lateinit var viewHolder: RecyclerView.ViewHolder
    private lateinit var view: View

    fun setMessages(entities: List<Message>) {
        this.allMessages = entities
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return allMessages.size
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
            R.layout.chat_head_recieve -> {
                view = inflater.inflate(viewType, parent, false)
                viewHolder = TextReceivedViewHolder(view)
            }
            R.layout.image_receive -> {
                view = inflater.inflate(viewType, parent, false)
                viewHolder = ImageReceivedViewHolder(view)
            }
            R.layout.image_send -> {
                view = inflater.inflate(viewType, parent, false)
                viewHolder = ImageSendViewHolder(view)
            }
        }

        return viewHolder
    }

    //FIXME can toggle multiple views, if view gets recycled, only the last toggled state persists
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val key = allMessages[position].key!!

        if (toggled == key)
            toggleOn(holder as BaseViewHolder)
        else toggleOff(holder as BaseViewHolder)

        when (holder.itemViewType) {

            R.layout.chat_head_recieve -> {
                (holder as TextReceivedViewHolder)

                holder.textView.text = allMessages[position].payload
                holder.date.text = allMessages[position].date
                holder.alias.text = allMessages[position].alias

                holder.delete.setOnClickListener {
                    deleteFromDb(key, position)
                }

                holder.itemView.setOnClickListener {
                    /*clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    clip = ClipData.newPlainText("copied", holder.textView.text)
                    clipboard.primaryClip = clip*/

                    toggleFromListener(holder, key)
                }
            }

            R.layout.chat_head_send -> {
                (holder as TextSendViewHolder)

                holder.textView.text = allMessages[position].payload
                holder.date.text = allMessages[position].date

                holder.delete.setOnClickListener {
                    deleteFromDb(key, position)
                }

                holder.itemView.setOnClickListener {
                    /*clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    clip = ClipData.newPlainText("copied", holder.textView.text)
                    clipboard.primaryClip = clip*/

                    toggleFromListener(holder, key)
                }
            }

            R.layout.image_receive -> {
                (holder as ImageReceivedViewHolder)

                val parsedUri = Uri.parse(allMessages[position].payload!!)
                val sourceFile = DocumentFile.fromSingleUri(context, parsedUri)

                if (sourceFile!!.exists()) {
                    holder.imageView.setImageURI(parsedUri)
                } else {
                    holder.imageView.setImageResource(
                        R.drawable.not_found
                    )
                }

                holder.date.text = allMessages[position].date
                holder.size.text = allMessages[position].size
                holder.alias.text = allMessages[position].alias

                holder.delete.setOnClickListener {
                    deleteFromDb(key, position)
                }

                holder.itemView.setOnClickListener {
                    toggleFromListener(holder, key)
                }
            }

            R.layout.image_send -> {
                (holder as ImageSendViewHolder)

                val parsedUri = Uri.parse(allMessages[position].payload!!)
                val sourceFile = DocumentFile.fromSingleUri(context, parsedUri)

                if (sourceFile!!.exists()) {
                    holder.imageView.setImageURI(parsedUri)
                } else {
                    holder.imageView.setImageResource(
                        R.drawable.not_found
                    )
                }

                holder.date.text = allMessages[position].date
                holder.size.text = allMessages[position].size

                holder.delete.setOnClickListener {
                    deleteFromDb(key, position)
                }

                holder.itemView.setOnClickListener {
                    toggleFromListener(holder, key)
                }
            }

        }
    }

    private fun toggleOn(holder: BaseViewHolder) {
        holder.delete.visibility = View.VISIBLE
        holder.info.visibility = View.VISIBLE
    }

    private fun toggleOff(holder: BaseViewHolder) {
        holder.delete.visibility = View.GONE
        holder.info.visibility = View.GONE
    }

    private fun toggleFromListener(holder: BaseViewHolder, key: Int) {
        if (toggled == key) {
            toggled = -1
            toggleOff(holder)
        } else {
            toggled = key
            toggleOn(holder)
        }
    }

    private fun deleteFromDb(key: Int, position: Int) {
        CoroutineScope(IO).launch { dao.delete(key) }
        notifyItemRemoved(position)
    }

}
