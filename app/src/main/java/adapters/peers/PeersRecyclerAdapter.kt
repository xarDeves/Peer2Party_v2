package adapters.peers

import adapters.peers.viewholders.PeerViewHolder
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.peer2party.R
import networker.peers.Peer

class PeersRecyclerAdapter(
    private val context: Context,
    private val inflater: LayoutInflater = LayoutInflater.from(context)
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var peers = emptyList<Peer>()

    fun setPeers(peers: ArrayList<Peer>) {
        this.peers = peers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = inflater.inflate(R.layout.peer_inflatable, parent, false)
        return PeerViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as PeerViewHolder)

        val currentPeer = peers[position]
        holder.checkBox.isChecked = currentPeer.isEnabled
        holder.peerName.text = currentPeer.user.username

        holder.checkBox.setOnClickListener {
            currentPeer.isEnabled = !currentPeer.isEnabled
        }
    }

    override fun getItemCount(): Int {
        return peers.size
    }
}
