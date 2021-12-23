package fragments

//import adapters.ChatRecyclerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.peer2party.databinding.FragmentChatBinding
import viewmodels.MainViewModel

class ChatFragment : Fragment() {

    private val viewModel by activityViewModels<MainViewModel>()
    private lateinit var binding: FragmentChatBinding

    private fun setupRecycler() {
        /*val layoutManager = LinearLayoutManager(requireActivity())
        val chatAdapter = ChatRecyclerAdapter(requireActivity())
        binding.chatRecycler.layoutManager = layoutManager
        binding.chatRecycler.adapter = chatAdapter*/
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
        binding = FragmentChatBinding.inflate(inflater, container, false)

        //setupRecycler()

        return binding.root
    }
}