package de.codingkeks.shoppinglist

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import de.codingkeks.shoppinglist.utility.ImageMapper
import de.codingkeks.shoppinglist.utility.ThemeSetter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : ThemeSetter() {

    companion object {
        const val TAG = "shoppinglist_1234abcd"
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private val mapper = ImageMapper()
    private lateinit var registration: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_shoppinglists,
                R.id.nav_friends,
                R.id.nav_account,
                R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onStart() {
        super.onStart()

        displayUserInformation()
    }

    override fun onStop() {
        super.onStop()
        registration.remove()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    /**
     * method to display all important user information in the navBar Tab
     */
    private fun displayUserInformation() {
        Log.d(TAG, "display user information in navBar")
        val fb = FirebaseAuth.getInstance()
        val user = fb.currentUser
        val tvEmail = nav_view.getHeaderView(0).findViewById<TextView>(R.id.tvEmail)
        tvEmail.text = if (user?.email == null) "Loading Email..." else user.email

        val uidUser = user?.uid.toString()
        val userRef = FirebaseFirestore.getInstance().document("users/$uidUser")
        registration = userRef.addSnapshotListener { documentSnapshot, _ ->
        //userRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot != null) {
                val tvName = nav_view.getHeaderView(0).findViewById<TextView>(R.id.tvName)
                tvName.text = documentSnapshot.get("username") as String? ?: "Loading Username..."
                if (documentSnapshot.get("icon_id") != null)
                    ivNavUserIcon.setImageResource(mapper.download((documentSnapshot.get("icon_id") as Long).toInt()))
            }
        }
    }
}