package adapters.peers.viewholders

import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.peer2party.R

class PeerViewHolder(
    view: View,
    val peerName: TextView = view.findViewById(R.id.peerName),
    val checkBox: CheckBox = view.findViewById(R.id.checkBox)
) : RecyclerView.ViewHolder(view)