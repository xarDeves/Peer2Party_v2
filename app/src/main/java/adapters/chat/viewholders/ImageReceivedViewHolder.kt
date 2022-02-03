package adapters.chat.viewholders

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.peer2party.R

class ImageReceivedViewHolder(
    view: View,
    val delete: ImageView = view.findViewById(R.id.deleteImageRec),
    val info: LinearLayout = view.findViewById(R.id.infoImageRec),
    val date: TextView = view.findViewById(R.id.dateImageRec),
    val imageView: ImageView = view.findViewById(R.id.imageViewRec),
    val size: TextView = view.findViewById(R.id.sizeImageRec),
    val alias: TextView = view.findViewById(R.id.userTextImage)
) : RecyclerView.ViewHolder(view)