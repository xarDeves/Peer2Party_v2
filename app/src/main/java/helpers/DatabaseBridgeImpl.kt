package helpers

import data.Message
import data.MessageType
import helpers.DateTimeHelper.fetchDateTime
import networker.messages.MessageDeclaration
import networker.messages.content.ContentProvider
import networker.peers.User
import viewmodels.MainViewModel

//FIXME change name
class DatabaseBridgeImpl(
    private val viewModel: MainViewModel
) : DatabaseBridge {

    //multimedia :
    //provider.header = payload
    //provider.body = empty

    //text:
    //provider.header = empty
    //provider.body = text

    override fun onTextReceived(provider: ContentProvider<Any, String>, user: User) {
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
        TODO("Not yet implemented")
    }

    override fun onMultimediaReceived(provider: ContentProvider<String, Any>, user: User) {
        viewModel.insertEntity(
            Message(
                MessageType.IMAGE_RECEIVE,
                provider.header,
                fetchDateTime(),
                provider.totalSize.toString(),
                user.username
            )
        )
    }

    override fun onMultimediaSend(msgDecl: MessageDeclaration) {
        TODO("Not yet implemented")
    }

}