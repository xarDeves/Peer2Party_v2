package fragments

import adapters.peers.PeersRecyclerAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.peer2party.databinding.FragmentPeerListBinding
import viewmodels.MainViewModel

class PeerListFragment : Fragment() {

    private val viewModel by activityViewModels<MainViewModel>()

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var peersAdapter: PeersRecyclerAdapter
    private lateinit var binding: FragmentPeerListBinding

    private fun setupRecycler() {
        layoutManager = LinearLayoutManager(requireActivity())
        peersAdapter = PeersRecyclerAdapter(requireActivity())
        binding.peersRecycler.layoutManager = layoutManager
        binding.peersRecycler.adapter = peersAdapter
    }

    private fun attachListeners() {
        viewModel.peers.observe(requireActivity()) {
            Log.d("xristos", "observer: " + it.size.toString())
            peersAdapter.setPeers(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecycler()
        attachListeners()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPeerListBinding.inflate(inflater, container, false)

        return binding.root
    }
}