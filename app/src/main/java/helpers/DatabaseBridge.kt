package helpers

import networker.messages.MessageDeclaration
import networker.messages.content.ContentProvider
import networker.peers.User

//FIXME change name
interface DatabaseBridge {

    fun onTextReceived(provider: ContentProvider<Any, String>, user: User)
    fun onTextSend(msgDecl: MessageDeclaration)

    fun onMultimediaReceived(provider: ContentProvider<String, Any>, user: User)
    fun onMultimediaSend(msgDecl: MessageDeclaration)
}