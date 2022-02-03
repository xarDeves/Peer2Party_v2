package adapters.chat.viewholders

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.peer2party.R

class ImageSendViewHolder(
    view: View,
    delete: ImageView = view.findViewById(R.id.deleteImageSend),
    info: LinearLayout = view.findViewById(R.id.infoImageSend),
    val date: TextView = view.findViewById(R.id.dateImageSend),
    val imageView: ImageView = view.findViewById(R.id.imageViewSend),
    val size: TextView = view.findViewById(R.id.sizeImageSend)
) : BaseViewHolder(view, delete, info)