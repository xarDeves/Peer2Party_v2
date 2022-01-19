package viewmodels

import Networking.Model
import android.app.Application
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import data.DatabaseHolder
import data.DbDao
import data.Message
import data.Repository
import fragments.ChatFragment
import fragments.PeerListFragment
import kotlinx.coroutines.launch


class MainViewModel(application: Application) : AndroidViewModel(application) {

    val fragments: Array<Fragment> = arrayOf(ChatFragment(), PeerListFragment())

    private val model: Model
    private val repository: Repository
    private var dao: DbDao = DatabaseHolder.getInstance(application).dao()
    var allMessages: LiveData<List<Message>>

    init {
        repository = Repository(dao)
        allMessages = repository.allMessages

        model = Model(this)
    }

    fun insertEntity(entity: Message) {
        viewModelScope.launch {
            dao.insert(entity)
        }
    }

}