package activities

import adapters.MainViewPagerAdapter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.widget.ViewPager2
import com.example.peer2party.R
import viewmodels.MainViewModel
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel>()
    private lateinit var ID: String
    private val bundle: Bundle = Bundle()
    private var doubleBackCheck = false

    private lateinit var fragmentManager: FragmentManager
    private lateinit var transactionManager: androidx.fragment.app.FragmentTransaction

    override fun onBackPressed() {

        if (doubleBackCheck) {
            super.onBackPressed()
            finishAffinity()
            exitProcess(0)
        }

        doubleBackCheck = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({ doubleBackCheck = false }, 2000)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewPager: ViewPager2 = findViewById(R.id.viewpager)
        val adapter = MainViewPagerAdapter(viewModel.fragments, this)
        viewPager.adapter = adapter

        if (savedInstanceState == null) {
            ID = intent.extras!!.getString("ID")!!
            bundle.putString("ID", ID)
        }
    }

}