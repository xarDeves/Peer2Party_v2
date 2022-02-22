package helpers.db

import networker.messages.MessageDeclaration
import networker.messages.content.providers.MultimediaProvider
import networker.messages.content.providers.TextProvider
import networker.peers.user.User

//FIXME change name
interface DatabaseBridge {

    fun onTextReceived(provider: TextProvider, user: User)
    fun onTextSend(msgDecl: MessageDeclaration)

    fun onMultimediaReceived(provider: MultimediaProvider, user: User)
    fun onMultimediaSend(msgDecl: MessageDeclaration)
}