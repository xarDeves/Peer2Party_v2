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
import helpers.db.DatabaseBridgeImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import networker.RoomKnowledge
import networker.discovery.discoverers.MulticastGroupPeerDiscoverer
import networker.discovery.io.announcers.MulticastGroupPeerAnnouncer
import networker.discovery.io.receivers.MulticastGroupPeerReceiver
import networker.discovery.servers.InboundConnectionServer
import networker.helpers.NetworkInformation
import networker.helpers.NetworkUtilities
import networker.messages.MessageIntent
import networker.messages.io.IOManager
import networker.messages.io.announcers.MulticastGroupMessageAnnouncer
import networker.messages.io.processors.inbound.InboundProcessor
import networker.messages.io.processors.outbound.OutboundProcessor
import networker.messages.io.receivers.MulticastGroupMessageReceiver
import networker.peers.Peer
import networker.peers.Status
import networker.peers.user.User
import networker.sockets.ServerSocketAdapter
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.MulticastSocket
import java.net.NetworkInterface
import java.util.*
import java.util.concurrent.Executors

//TODO determine hotspot active https://stackoverflow.com/questions/12401108/how-to-check-programmatically-if-hotspot-is-enabled-or-disabled
//TODO get hotspot ip if active https://stackoverflow.com/questions/9573196/how-to-get-the-ip-of-the-wifi-hotspot-in-androidhttps://stackoverflow.com/questions/9573196/how-to-get-the-ip-of-the-wifi-hotspot-in-android
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val THREAD_COUNT = Runtime.getRuntime().availableProcessors()
    private val SERVER_PORT = 7788

    private lateinit var netIface: NetworkInterface
    private lateinit var networkInetAddress: InetAddress
    private lateinit var username: String

    private val dao: DbDao = DatabaseHolder.getInstance(application).dao()
    val fragments: Array<Fragment> = arrayOf(ChatFragment(), PeerListFragment())
    var allMessages: LiveData<List<Message>> = dao.getAllMessages()
    var peers: MutableLiveData<ArrayList<Peer>> = MutableLiveData(ArrayList())

    var roomWrapper: RoomWrapper = RoomWrapper()
    lateinit var ourself: User
    private lateinit var networkInformation: NetworkInformation
    private lateinit var discoverer: MulticastGroupPeerDiscoverer
    private lateinit var ioManager: IOManager
    private val ioManagerSendExecutor = Executors.newSingleThreadExecutor()
    private val dbBridge = DatabaseBridgeImpl(this)

    private fun getInetAddress(): String {
        NetworkInterface.getNetworkInterfaces()?.toList()?.map { networkInterface ->
            networkInterface.inetAddresses?.toList()?.find {
                !it.isLoopbackAddress && it is InetAddress
            }?.let { return it.hostAddress!! }
        }
        return ""
    }

    private fun initLateinitObjects() {
        val wifi = getApplication<Application>().getSystemService(WIFI_SERVICE) as WifiManager

        val lock = wifi.createMulticastLock("mCastLock")
        lock.acquire() // ACQUIRED

        val netIfaces = NetworkUtilities.getViableNetworkInterfaces()
        //TODO THIS GETS THE NETWORK IFACES. GET THE INTERFACE REQUIRED, SOMEHOW, FROM THE USER
        netIface = netIfaces["wlan0"]!!
        networkInetAddress = netIface.inetAddresses.nextElement()

        val r = Random()
        r.setSeed(System.currentTimeMillis()-r.nextInt())

        ourself = User(
            networkInetAddress,
            username,
            SERVER_PORT,
            Status.AVAILABLE,
            r.nextInt(Int.MAX_VALUE)
        )
        networkInformation = NetworkInformation(netIface.displayName, ourself)

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                launch { initPeerDiscovery() }
                launch { initIOManager() }
            }
        }
    }

    //you have achieved...L O K
    private fun initPeerDiscovery() {
        //for peer discovery
        val discoverInetSocketAddress = InetSocketAddress(
            networkInformation.multicastDiscoverGroup,
            networkInformation.discoverPort
        )
        val multicastSocket = MulticastSocket(networkInformation.discoverPort)
        multicastSocket.joinGroup(discoverInetSocketAddress, netIface)
        multicastSocket.timeToLive = 255 // sidagi giatrou
        multicastSocket.loopbackMode = false
        multicastSocket.broadcast = true

        val serverSocketAdapter = ServerSocketAdapter(networkInetAddress, SERVER_PORT, 25)
        val inboundConnectionServer = InboundConnectionServer(networkInformation, roomWrapper)
        val multicastGroupSender = MulticastGroupPeerAnnouncer()
        val multicastGroupReceiver = MulticastGroupPeerReceiver()

        discoverer = MulticastGroupPeerDiscoverer(
            multicastGroupReceiver,
            multicastGroupSender,
            inboundConnectionServer,
            serverSocketAdapter,
            multicastSocket,
            networkInformation,
            roomWrapper,
            250
        )

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                launch { runPeerListener() }
                launch { runPeerDiscovery() }
            }
        }
    }

    private fun runPeerDiscovery() {
        discoverer.highSpeedDiscover()
        while (true) discoverer.discover()
    }

    private fun runPeerListener() {
        while (true) discoverer.listen()
    }

    private fun initIOManager() {
        val inboundExecutor = Executors.newFixedThreadPool(THREAD_COUNT)
        val outboundExecutor = Executors.newFixedThreadPool(THREAD_COUNT)

        val msgDiscoverInetSocketAddress = InetSocketAddress(
            networkInformation.multicastMessagesGroup,
            networkInformation.messagePort
        )
        val multicastSocket = MulticastSocket(networkInformation.messagePort)
        multicastSocket.joinGroup(msgDiscoverInetSocketAddress, netIface)
        multicastSocket.timeToLive = 255 // sidagi giatrou
        multicastSocket.loopbackMode = false
        multicastSocket.broadcast = true

        val inboundProcessor = InboundProcessor(inboundExecutor, roomWrapper, dbBridge)
        val outboundProcessor = OutboundProcessor(outboundExecutor, networkInformation, roomWrapper, dbBridge)
        val multicastGroupMessageAnnouncer = MulticastGroupMessageAnnouncer()
        val multicastGroupMessageReceiver = MulticastGroupMessageReceiver()

        ioManager = IOManager(
            multicastGroupMessageReceiver,
            multicastGroupMessageAnnouncer,
            multicastSocket,
            outboundProcessor,
            inboundProcessor,
            roomWrapper,
            networkInformation,
        )

        while (true) ioManager.discover()
    }

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                initLateinitObjects()
            }
        }
    }

    fun send(intent: MessageIntent) {
        ioManagerSendExecutor.execute {
            ioManager.send(intent)
        }
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

        fun getEnabledUsers(): LinkedList<String> {

            val enabledPeers: LinkedList<String> = LinkedList<String>()

            peerList.forEach {
                if (it.isEnabled) enabledPeers += it.user.identifier
            }

            return enabledPeers
        }

    }

}