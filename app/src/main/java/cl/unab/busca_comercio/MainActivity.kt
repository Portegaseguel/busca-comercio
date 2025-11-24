package cl.unab.busca_comercio

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomNav: BottomNavigationView
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        bottomNav = findViewById(R.id.bottomNav)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> bottomNav.visibility = View.GONE
                else -> bottomNav.visibility = View.VISIBLE
            }
        }

        bottomNav.setOnItemSelectedListener { item ->
            val user = auth.currentUser

            when (item.itemId) {
                R.id.searchFragment -> {
                    navController.navigate(R.id.searchFragment)
                    true
                }

                R.id.myBusinessesFragment -> {
                    if (user == null) {
                        navController.navigate(R.id.loginFragment)
                    } else {
                        navController.navigate(R.id.myBusinessesFragment)
                    }
                    true
                }

                R.id.welcomeFragment -> {
                    if (user == null) {
                        navController.navigate(R.id.loginFragment)
                    } else {
                        navController.navigate(R.id.welcomeFragment)
                    }
                    true
                }

                else -> false
            }
        }
    }
}
