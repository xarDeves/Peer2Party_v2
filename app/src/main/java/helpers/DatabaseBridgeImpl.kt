package helpers

import data.Message
import data.MessageType
import helpers.DateTimeHelper.fetchDateTime
import networker.messages.MessageDeclaration
import networker.messages.content.providers.MultimediaProvider
import networker.messages.content.providers.TextProvider
import networker.peers.User
import viewmodels.MainViewModel

//FIXME change name
class DatabaseBridgeImpl(
    private val viewModel: MainViewModel
) : DatabaseBridge {

    override fun onTextReceived(provider: TextProvider, user: User) {
        viewModel.insertEntity(
            Message(
                MessageType.TEXT_RECEIVE,
                provider.data,
                fetchDateTime(),
                null,
                user.username
            )
        )
    }

    override fun onTextSend(msgDecl: MessageDeclaration) {
        viewModel.insertEntity(
            Message(MessageType.TEXT_SEND, msgDecl.body, fetchDateTime())
        )
    }

    override fun onMultimediaReceived(provider: MultimediaProvider, user: User) {
        viewModel.insertEntity(
            Message(
                MessageType.IMAGE_RECEIVE,
                provider.data,
                fetchDateTime(),
                provider.totalSize.toString(),
                user.username
            )
        )
    }

    override fun onMultimediaSend(msgDecl: MessageDeclaration) {
        viewModel.insertEntity(
            Message(
                MessageType.IMAGE_SEND,
                msgDecl.body,
                fetchDateTime(),
                msgDecl.headerSize.toString()
            )
        )
    }

}