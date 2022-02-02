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
import networker.peers.Peer
import java.net.InetAddress
import java.net.NetworkInterface

//TODO implement livedata or peers -> (RoomKnowledge)
//TODO determine hotspot active https://stackoverflow.com/questions/12401108/how-to-check-programmatically-if-hotspot-is-enabled-or-disabled
//TODO get hotspot ip if active https://stackoverflow.com/questions/9573196/how-to-get-the-ip-of-the-wifi-hotspot-in-androidhttps://stackoverflow.com/questions/9573196/how-to-get-the-ip-of-the-wifi-hotspot-in-android
class MainViewModel(application: Application) : AndroidViewModel(application) {

    val fragments: Array<Fragment> = arrayOf(ChatFragment(), PeerListFragment())

    private val dao: DbDao = DatabaseHolder.getInstance(application).dao()

    var allMessages: LiveData<List<Message>> = dao.getAllMessages()
    var peers: MutableLiveData<List<Peer>> = MutableLiveData(emptyList())

    //always instantiate last
    private val model: Model = Model(this)

    fun getInetAddress(): String {
        NetworkInterface.getNetworkInterfaces()?.toList()?.map { networkInterface ->
            networkInterface.inetAddresses?.toList()?.find {
                !it.isLoopbackAddress && it is InetAddress
            }?.let { return it.hostAddress }
        }
        return ""
    }

    //TODO instantiate MulticastDiscoverer
    //you have achieved...L O K
    private fun initPeerDiscovery() {
        val wifi = getApplication<Application>().getSystemService(WIFI_SERVICE) as WifiManager

        val lock = wifi.createMulticastLock("myLock")
        lock.acquire()

        /*val multicastSocket = MulticastSocket()
        User(InetAddress.getLocalHost(), "inject username here", multicastSocket.port)*/
    }

    init {
        //initPeerDiscovery()
    }

    fun insertEntity(entity: Message) {
        viewModelScope.launch {
            dao.insert(entity)
        }
    }

}