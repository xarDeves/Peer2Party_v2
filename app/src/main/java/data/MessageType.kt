package data

import com.example.peer2party.R

enum class MessageType(val type: Int) {

    TEXT_RECEIVE(R.layout.chat_head_recieve),
    TEXT_SEND(R.layout.chat_head_send),
    IMAGE_RECEIVE(R.layout.image_recieve),
    IMAGE_SEND(R.layout.image_send)
}