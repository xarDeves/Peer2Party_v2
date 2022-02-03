package adapters.chat.viewholders

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder(
    view: View,
    val delete: ImageView,
    val info: LinearLayout
) : RecyclerView.ViewHolder(view)