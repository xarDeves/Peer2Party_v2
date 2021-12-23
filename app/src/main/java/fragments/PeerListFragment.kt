package fragments

import android.os.Bundle
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
    private lateinit var binding: FragmentPeerListBinding

    private fun setupRecycler() {
        /*val layoutManager = LinearLayoutManager(requireActivity())
        val chatAdapter = ChatRecyclerAdapter(requireActivity())
        binding.peersRecycler.layoutManager = layoutManager
        binding.peersRecycler.adapter = chatAdapter*/
    }

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }*/

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //set listeners here ? -> https://developer.android.com/topic/libraries/architecture/viewmodel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPeerListBinding.inflate(inflater, container, false)

        //setupRecycler()

        return binding.root
    }
}