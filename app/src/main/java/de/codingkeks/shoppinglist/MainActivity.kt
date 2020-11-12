package de.codingkeks.shoppinglist

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_account.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private val RC_AUTH = 20
    private val TAG = "shoppinglist_1234abcd"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_shoppinglists, R.id.nav_friends, R.id.nav_account, R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        login()

        buLogout.setOnClickListener {
            AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener {
                    Log.d(TAG, "User has been logged out")
                    login()
                }
        }

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

    override fun onStart() {
        Log.d(TAG, "onStart_Start")
        super.onStart()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "onActivityResult_Start")
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_AUTH) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser
                displayUserInformation()
                Log.d(TAG, user?.email.toString())
                // ...
            } else {
                //TODO
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show()
                login()
            }
        }
        Log.d(TAG, "onActivityResult_End")
    }

    private fun login() {
        //if: firebase user does not exist && not online -> Error
        if (FirebaseAuth.getInstance().currentUser == null && !isOnline(this)) {
            val message = AlertDialog.Builder(this)
            message.setMessage("No Internet Connection!")
            message.setNeutralButton("RELOAD") { _, _ -> login() }
            message.setCancelable(false)
            message.show()
        }
        else {
            //authentication providers
            var fb = FirebaseAuth.getInstance()
            if (fb.currentUser == null) {
                val providers = arrayListOf(
                    AuthUI.IdpConfig.EmailBuilder().build(),
                    AuthUI.IdpConfig.GoogleBuilder().build()
                )
                Log.d(TAG, "startActivityForResult_Pre")
                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                    RC_AUTH
                )
            }
            displayUserInformation()
            Log.d(TAG, "startActivityForResult_Post")
        }
    }

    private fun displayUserInformation() {
        val fb = FirebaseAuth.getInstance()
        val user = fb.currentUser
        val tvName = nav_view.getHeaderView(0).findViewById<TextView>(R.id.tvName)
        val tvEmail = nav_view.getHeaderView(0).findViewById<TextView>(R.id.tvEmail)
        tvName.text = if (user?.displayName == null) "No Name" else user.displayName
        tvEmail.text = if (user?.email == null) "No Email" else user.email
    }

    /**
     *@return returns true if the user has any internet connection
     */
    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                        return true
                    }
                }
            }
        }
        return false
    }
}