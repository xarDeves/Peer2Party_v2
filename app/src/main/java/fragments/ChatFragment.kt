package fragments

import adapters.chat.ChatRecyclerAdapter
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.format.Formatter
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
import networker.helpers.NetworkUtilities
import networker.messages.MessageDeclaration
import networker.messages.MessageIntent
import viewmodels.MainViewModel


class ChatFragment : Fragment() {

    private val viewModel by activityViewModels<MainViewModel>()
    private lateinit var chatAdapter: ChatRecyclerAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var binding: FragmentChatBinding

    private lateinit var fileSelectorLauncher: ActivityResultLauncher<Intent>
    private lateinit var camPhotoLauncher: ActivityResultLauncher<Intent>
    private lateinit var camVideoLauncher: ActivityResultLauncher<Intent>
    private var cameraMediaUri: Uri = Uri.EMPTY

    //FIXME scroll recycler to bottom on app start
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
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).also {
                it.addCategory(Intent.CATEGORY_OPENABLE)
                it.type = "*/*"
                //it.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                it.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                it.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                it.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
            fileSelectorLauncher.launch(intent)
            binding.operationsLayout.visibility = View.GONE
        }

        binding.openCamPhoto.setOnClickListener {

            cameraMediaUri = requireContext().contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ContentValues()
            )!!

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraMediaUri)
            camPhotoLauncher.launch(intent)
            binding.operationsLayout.visibility = View.GONE
        }

        binding.openCamVideo.setOnClickListener {

            cameraMediaUri = requireContext().contentResolver.insert(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                ContentValues()
            )!!

            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraMediaUri)
            camVideoLauncher.launch(intent)
            binding.operationsLayout.visibility = View.GONE
        }
    }

    private fun setupActivityLaunchers() {
        fileSelectorLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {

                    val data: Intent? = result.data
                    val parsedUri = Uri.parse(data!!.dataString)

                    val contentResolver = requireContext().contentResolver
                    contentResolver.takePersistableUriPermission(
                        parsedUri!!,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    val type = contentResolver.getType(parsedUri)

                    if ("image" in type!!)
                        sendImage(parsedUri)
                }
            }

        //TODO save images ? (fetch state from settings activity)
        camPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    sendImage(cameraMediaUri)
                }
            }

        camVideoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    //sendVideo(cameraMediaUri)
                }
            }
    }

    private fun sendText() {
        val textToSend = binding.textInput.text.toString()

        if (textToSend.isNotBlank() && textToSend.isNotEmpty()) {
            val size = NetworkUtilities.convertUTF8StringToBytes(textToSend).size

            val messageIntent = MessageIntent(
                viewModel.ourself,
                viewModel.roomWrapper.getEnabledUsers()
            )

            messageIntent.addMessageDeclaration(
                MessageDeclaration(
                    "", textToSend, null, 0, size, networker.messages.MessageType.TEXT
                )
            )


            viewModel.send(messageIntent)

            binding.textInput.setText("")
        }
    }

    private fun sendImage(uri: Uri) {

        val fd: AssetFileDescriptor =
            requireContext().contentResolver.openAssetFileDescriptor(uri, "r")!!

        val size = Formatter.formatFileSize(requireContext(), fd.length)

        viewModel.insertEntity(
            Message(
                MessageType.IMAGE_SEND,
                uri.toString(),
                fetchDateTime(),
                size.toString()
            )
        )

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)

        setupActivityLaunchers()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecycler()
        attachListeners()
    }
}