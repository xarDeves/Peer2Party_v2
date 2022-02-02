package adapters.peers

import adapters.peers.viewholders.PeerViewHolder
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import networker.peers.Peer

class PeersRecyclerAdapter internal constructor(
    private val context: Context,
    private val inflater: LayoutInflater = LayoutInflater.from(context)
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var peers = emptyList<Peer>()

    fun setPeers(peers: List<Peer>) {
        this.peers = peers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = inflater.inflate(viewType, parent, false)
        return PeerViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val currentPeer = peers[position]
        (holder as PeerViewHolder).checkBox.isChecked = currentPeer.isEnabled
        holder.peerName.text = currentPeer.user.username
    }

    override fun getItemCount(): Int {
        return peers.size
    }
}
