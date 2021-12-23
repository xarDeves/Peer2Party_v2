package viewmodels

import android.app.Application
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import fragments.ChatFragment
import fragments.PeerListFragment

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val fragments : Array<Fragment> = arrayOf(ChatFragment(), PeerListFragment())

}