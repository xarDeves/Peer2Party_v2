package fragments

import adapters.ChatRecyclerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.peer2party.databinding.FragmentChatBinding
import data.Message
import data.MessageType
import helpers.DateTimeHelper.fetchDateTime
import viewmodels.MainViewModel

class ChatFragment : Fragment() {

    private val viewModel by activityViewModels<MainViewModel>()
    private lateinit var chatAdapter: ChatRecyclerAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var binding: FragmentChatBinding

    private fun setupRecycler() {
        layoutManager = LinearLayoutManager(requireActivity())
        chatAdapter = ChatRecyclerAdapter(requireActivity())
        binding.chatRecycler.layoutManager = layoutManager
        binding.chatRecycler.adapter = chatAdapter
    }

    private fun attachListeners() {
        viewModel.allMessages.observe(activity!!, {
            chatAdapter.setMessages(it)
            if (layoutManager.findLastVisibleItemPosition() == it.size - 2) {
                layoutManager.scrollToPosition(it.size - 1)
            }
        })

        binding.buttonSend.setOnClickListener {
            initTextSend()
        }
    }

    private fun initTextSend() {
        val textToSend = binding.textInput.text.toString()

        if (textToSend.isNotBlank() && textToSend.isNotEmpty()) {
            viewModel.insertEntity(
                Message(MessageType.TEXT_SEND, textToSend, fetchDateTime())
            )
            activity!!.runOnUiThread {
                binding.textInput.setText("")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecycler()
        attachListeners()
    }
}