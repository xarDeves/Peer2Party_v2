package activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.peer2party.databinding.ActivityStartupBinding
import kotlinx.coroutines.launch


class StartupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartupBinding
    private lateinit var sharedPrefs: SharedPreferences
    private var alias: String = ""

    private val networkCallback = getNetworkCallBack()
    private val networkRequest = getNetworkRequest()


    private fun acquireStoragePermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                0
            )
        }
    }

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
    }

    private fun idIsEligible(): Boolean {
        if (alias.isNotEmpty() || alias.isNotBlank()) return true
        return false
    }

    private fun initChat() {
        if (idIsEligible()) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("ID", alias)
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
                    if (binding.connect.isEnabled) initChat()
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
        acquireStoragePermissions()

    }

}