package activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build.ID
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.peer2party.databinding.ActivityStartupBinding


class StartupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartupBinding
    private lateinit var sharedPrefs: SharedPreferences
    private var alias: String = ""

    /*private val networkCallback = getNetworkCallBack()
    private val networkRequest = getNetworkRequest()

    private fun getConnectivityManager() =
        this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private fun getNetworkRequest(): NetworkRequest {
        return NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
    }

    override fun onResume() {
        super.onResume()

        getConnectivityManager().registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onPause() {
        super.onPause()

        getConnectivityManager().unregisterNetworkCallback(networkCallback)
    }

    //FIXME does not work for hotspot ?
    private fun getNetworkCallBack(): ConnectivityManager.NetworkCallback {
        return object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)

                lifecycleScope.launch {
                    binding.connect.isEnabled = true
                    binding.noWifiTextView.visibility = View.INVISIBLE
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)

                lifecycleScope.launch {
                    binding.connect.isEnabled = false
                    binding.noWifiTextView.visibility = View.VISIBLE
                }
            }
        }
    }*/

    private fun idIsEligible(): Boolean {
        if (alias.isNotEmpty() || alias.isNotBlank()) return true
        return false
    }

    private fun initChat() {
        if (idIsEligible()) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("ID", ID)
            startActivity(intent)
            finish()
        } else Toast.makeText(this, "Please Enter A Username", Toast.LENGTH_SHORT).show()
    }

    private fun attachListeners() {
        binding.usernameText.doAfterTextChanged {
            alias = binding.usernameText.text.toString()
            if (idIsEligible()) sharedPrefs.edit().putString("ID", alias).apply()
        }

        binding.usernameText.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_GO -> {
                    initChat()
                    true
                }
                else -> false
            }
        }

        binding.connect.setOnClickListener {
            initChat()

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar!!.hide()

        sharedPrefs = getSharedPreferences("id", MODE_PRIVATE)
        alias = sharedPrefs.getString("ID", "")!!
        binding.usernameText.setText(alias)

        attachListeners()
    }
}