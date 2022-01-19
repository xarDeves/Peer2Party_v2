package activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build.ID
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.peer2party.databinding.ActivityStartupBinding

class StartupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartupBinding
    private lateinit var sharedPrefs: SharedPreferences
    private var alias: String = ""

    private fun idIsEligible(): Boolean {
        if (alias.isNotEmpty() || alias.isNotBlank()) return true
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar!!.hide()

        sharedPrefs = getSharedPreferences("id", Context.MODE_PRIVATE)
        alias = sharedPrefs.getString("ID", "")!!
        binding.usernameText.setText(alias)

        binding.usernameText.doAfterTextChanged {
            alias = binding.usernameText.text.toString()
            if (idIsEligible()) sharedPrefs.edit().putString("ID", alias).apply()
        }

        binding.connect.setOnClickListener {
            if (idIsEligible()) {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("ID", ID)
                startActivity(intent)
                finish()
            } else Toast.makeText(this, "Please Enter A Username", Toast.LENGTH_SHORT).show()
        }

    }

}