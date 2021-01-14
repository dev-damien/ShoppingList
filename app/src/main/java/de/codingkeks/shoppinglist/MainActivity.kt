package de.codingkeks.shoppinglist

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "shoppinglist_1234abcd"
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private val RC_AUTH = 69

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "MainActivity_onCreate()_Start")
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
                R.id.nav_home,
                R.id.nav_shoppinglists,
                R.id.nav_friends,
                R.id.nav_account,
                R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        login()

        buLogout.setOnClickListener {
            /*AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "User has been logged out")
                        login()
                    }
                }*/
            val uidUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
            val docRef = FirebaseFirestore.getInstance().document("users/$uidUser")

            var test123: HashMap<String, HashMap<String, Int>> = HashMap<String, HashMap<String, Int>>()
            var test456: HashMap<String, Int> = HashMap<String, Int>()
            test456.put(uidUser, 2)

            test123.put("description", test456)

            docRef.set(test123, SetOptions.merge()).addOnSuccessListener(OnSuccessListener<Void>() {
                Log.d(TAG, "Database Daten Test Erfolgreich!")
            })
        }
        Log.d(TAG, "MainActivity_onCreate()_End")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.d(TAG, "MainActivity_onCreateOptionsMenu()_Start")
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        Log.d(TAG, "MainActivity_onCreateOptionsMenu()_End")
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        Log.d(TAG, "MainActivity_onSupportNavigateUp()_Start")
        val navController = findNavController(R.id.nav_host_fragment)
        val res = navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
        Log.d(TAG, "MainActivity_onSupportNavigateUp()_End")
        return res
    }

    override fun onStart() {
        Log.d(TAG, "MainActivity_onStart()_Start")
        super.onStart()
        Log.d(TAG, "MainActivity_onStart()_End")
    }

    override fun onStop() {
        Log.d(TAG, "MainActivity_onStop()_Start")
        super.onStop()
        Log.d(TAG, "MainActivity_onStop()_End")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "MainActivity_onActivityResult()_Start")
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_AUTH) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                displayUserInformation()
                //TODO Ist Datenbankeintrag da? Wenn nein: Benutzernamen setzen und setze Datenbankeintrag
                val uidUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
                val docRef = FirebaseFirestore.getInstance().document("users/$uidUser")
                docRef.get()
                    .addOnSuccessListener { document ->
                        Log.d(TAG, "DOKUMENT $document")
                        if (document == null) {
                            Log.d(TAG, "IM IF")
                            createUserDoc()
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d(TAG, "get failed with ", exception)
                    }
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
        Log.d(TAG, "MainActivity_onActivityResult()_End")
    }

    private fun login() {
        //if: firebase user does not exist && not online -> Error
        Log.d(TAG, "MainActivity_login()_Start")
        if (FirebaseAuth.getInstance().currentUser == null && !isOnline(this)) {
            val message = AlertDialog.Builder(this)
            message.setMessage("No Internet Connection!")
            message.setNeutralButton("RELOAD") { _, _ -> login() }
            message.setCancelable(false)
            message.show()
        } else {
            //authentication providers
            val fb = FirebaseAuth.getInstance()
            if (fb.currentUser == null) {
                val providers = arrayListOf(
                    AuthUI.IdpConfig.EmailBuilder().build(),
                    AuthUI.IdpConfig.GoogleBuilder().build()
                )
                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                    RC_AUTH
                )
            }
            displayUserInformation()
            Log.d(TAG, "MainActivity_login()_End")
        }
    }

    private fun displayUserInformation() {
        Log.d(TAG, "MainActivity_displayUserInformation()_Start")
        val fb = FirebaseAuth.getInstance()
        val user = fb.currentUser
        val tvName = nav_view.getHeaderView(0).findViewById<TextView>(R.id.tvName)
        val tvEmail = nav_view.getHeaderView(0).findViewById<TextView>(R.id.tvEmail)
        tvName.text = if (user?.displayName == null) "No Name" else user.displayName
        tvEmail.text = if (user?.email == null) "No Email" else user.email
        //TODO user Icon, Datenbank Daten anzeigen
        Log.d(TAG, "MainActivity_displayUserInformation()_End")
    }

    /**
     *@return returns true if the user has any internet connection
     */
    private fun isOnline(context: Context): Boolean {
        Log.d(TAG, "MainActivity_isOnline()_Start")
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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
        Log.d(TAG, "MainActivity_isOnline()_End")
        return false
    }

    /**
     * Method creates the user document in the collection users
     */
    private fun createUserDoc() {
        Log.d(TAG, "Create User Doc")
        val uidUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val docRef = FirebaseFirestore.getInstance().document("users/$uidUser")

        docRef.set(mapOf(
            "username" to "Jabadabadu",
            "icon_id" to 1))
            .addOnSuccessListener(OnSuccessListener<Void>() {
                Log.d(TAG, "User Document created")
        })

        docRef.update("friends", FieldValue.arrayUnion())
    }


}