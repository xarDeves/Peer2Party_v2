package fragments

import adapters.chat.ChatRecyclerAdapter
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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

    private lateinit var fileSelectorLauncher: ActivityResultLauncher<Intent>
    //private lateinit var cameraLuncher: ActivityResultLauncher<Intent>
    //private lateinit var voiceRecLauncher: ActivityResultLauncher<Intent>


    private fun setupRecycler() {
        layoutManager = LinearLayoutManager(requireActivity())
        chatAdapter = ChatRecyclerAdapter(requireActivity())
        binding.chatRecycler.layoutManager = layoutManager
        binding.chatRecycler.adapter = chatAdapter
    }

    private fun attachListeners() {
        viewModel.allMessages.observe(requireActivity()) {
            chatAdapter.setMessages(it)
            if (layoutManager.findLastVisibleItemPosition() == it.size - 2) {
                layoutManager.scrollToPosition(it.size - 1)
            }
        }

        binding.buttonSend.setOnClickListener {
            sendText()
        }

        binding.selectOperation.setOnClickListener {
            val visibility = binding.operationsLayout.visibility

            if (visibility != View.VISIBLE) binding.operationsLayout.visibility = View.VISIBLE
            else binding.operationsLayout.visibility = View.GONE
        }

        //cause android.
        binding.textInput.setOnClickListener {
            binding.operationsLayout.visibility = View.GONE
        }
        binding.textInput.setOnFocusChangeListener { _, _ ->
            binding.operationsLayout.visibility = View.GONE
        }
        binding.textInput.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_SEND -> {
                    sendText()
                    true
                }
                else -> false
            }
        }

        binding.selectFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            fileSelectorLauncher.launch(intent)
            //fileSelectorLauncher.launch("*/*")
        }

        binding.openCamera.setOnClickListener {

        }
    }

    private fun sendText() {
        val textToSend = binding.textInput.text.toString()

        if (textToSend.isNotBlank() && textToSend.isNotEmpty()) {
            viewModel.insertEntity(
                Message(MessageType.TEXT_SEND, textToSend, fetchDateTime())
            )
            requireActivity().runOnUiThread {
                binding.textInput.setText("")
            }
        }
    }

    private fun sendImage(uri: Uri) {
        viewModel.insertEntity(
            Message(MessageType.IMAGE_SEND, uri.toString(), fetchDateTime())
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)

        //working file browser with responsive ui, change fileSelectorLauncher to <String>
        /*fileSelectorLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->

                val cR = requireContext().contentResolver
                val type = cR.getType(uri!!)

                if ("image" in type!!)
                    sendImage(uri)

                binding.operationsLayout.visibility = View.GONE
            }*/
        fileSelectorLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    Log.d("fuck", "$data")

                    /*val cR = requireContext().contentResolver
                    val type = cR.getType(Uri(data))

                    if ("image" in type!!)
                        sendImage(data)

                    binding.operationsLayout.visibility = View.GONE*/
                }
            }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecycler()
        attachListeners()

    }
}