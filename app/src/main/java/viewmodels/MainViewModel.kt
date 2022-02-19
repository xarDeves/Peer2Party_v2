package viewmodels

import android.app.Application
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import data.DatabaseHolder
import data.DbDao
import data.Message
import fragments.ChatFragment
import fragments.PeerListFragment
import kotlinx.coroutines.launch
import networker.Model
import networker.RoomKnowledge
import networker.discovery.discoverers.Discoverer
import networker.discovery.receivers.MulticastGroupReceiver
import networker.discovery.receivers.MulticastGroupSender
import networker.discovery.servers.InboundConnectionServer
import networker.helpers.NetworkInformation
import networker.peers.Peer
import networker.peers.User
import networker.sockets.ServerSocketAdapter
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.MulticastSocket
import java.net.NetworkInterface

//TODO implement livedata or peers -> (RoomKnowledge)
//TODO determine hotspot active https://stackoverflow.com/questions/12401108/how-to-check-programmatically-if-hotspot-is-enabled-or-disabled
//TODO get hotspot ip if active https://stackoverflow.com/questions/9573196/how-to-get-the-ip-of-the-wifi-hotspot-in-androidhttps://stackoverflow.com/questions/9573196/how-to-get-the-ip-of-the-wifi-hotspot-in-android
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var localInet: InetAddress
    private lateinit var username: String

    val fragments: Array<Fragment> = arrayOf(ChatFragment(), PeerListFragment())

    private val dao: DbDao = DatabaseHolder.getInstance(application).dao()
    private lateinit var roomWrapper: RoomWrapper

    var allMessages: LiveData<List<Message>> = dao.getAllMessages()
    var peers: MutableLiveData<ArrayList<Peer>> = MutableLiveData(ArrayList())

    //always instantiate last
    private val model: Model = Model(this)

    private fun getInetAddress(): String {
        NetworkInterface.getNetworkInterfaces()?.toList()?.map { networkInterface ->
            networkInterface.inetAddresses?.toList()?.find {
                !it.isLoopbackAddress && it is InetAddress
            }?.let { return it.hostAddress }
        }
        return ""
    }

    //you have achieved...L O K
    private fun initPeerDiscovery() {
        Thread {

            val wifi = getApplication<Application>().getSystemService(WIFI_SERVICE) as WifiManager

            val lock = wifi.createMulticastLock("myLock")
            lock.acquire()

            //FIXME get network interface address
            localInet = InetAddress.getLocalHost()
            val serverPort = 7788

            //Log.d("fuck", "${InetAddress.getLocalHost()}")
            //Log.d("fuck", getInetAddress())

            //for peer discovery
            val networkInformation = NetworkInformation("k")
            val inetSocketAddress = InetSocketAddress(localInet, 7778)
            val multicastSocket = MulticastSocket(inetSocketAddress)
            //FIXME specify network interface
            multicastSocket.joinGroup(networkInformation.multicastDiscoverGroup)

            val thisUser = User(localInet, username, serverPort)
            roomWrapper = RoomWrapper()
            val serverSocketAdapter = ServerSocketAdapter(localInet, serverPort, 50)
            val inboundConnectionServer = InboundConnectionServer()
            val multicastGroupSender = MulticastGroupSender(thisUser)
            val multicastGroupReceiver = MulticastGroupReceiver()

            val discoverer = Discoverer(
                multicastGroupReceiver,
                multicastGroupSender,
                inboundConnectionServer,
                serverSocketAdapter,
                multicastSocket,
                networkInformation,
                roomWrapper
            )

            while (true) discoverer.processOnce()
        }.start()
    }

    init {
        initPeerDiscovery()
    }

    fun insertEntity(entity: Message) {
        viewModelScope.launch {
            dao.insert(entity)
        }
    }

    fun setUsername(username: String) {
        this.username = username
    }

    inner class RoomWrapper : RoomKnowledge() {

        private val peerList: ArrayList<Peer> = ArrayList()

        override fun addPeer(p: Peer) {
            super.addPeer(p)
            peerList.add(p)
            peers.postValue(peerList)
        }

        override fun removePeer(p: Peer) {
            super.removePeer(p)
            peerList.remove(p)
            peers.postValue(peerList)
        }

    }

}