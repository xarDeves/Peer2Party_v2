package helpers

import networker.messages.MessageDeclaration
import networker.messages.content.ContentProvider
import networker.peers.User

//FIXME change name
class DatabaseBridgeImpl : DatabaseBridge {

    //multimedia :
    //provider.header = payload
    //provider.body = empty

    //text:
    //provider.header = empty
    //provider.body = text

    override fun onTextReceived(provider: ContentProvider<Any, String>, user: User) {
        TODO("Not yet implemented")
    }

    override fun onTextSend(msgDecl: MessageDeclaration) {
        TODO("Not yet implemented")
    }

    override fun onMultimediaReceived(provider: ContentProvider<String, Any>, user: User) {
        TODO("Not yet implemented")
    }

    override fun onMultimediaSend(msgDecl: MessageDeclaration) {
        TODO("Not yet implemented")
    }
}