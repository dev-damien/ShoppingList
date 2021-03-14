package de.codingkeks.shoppinglist

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
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
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import de.codingkeks.shoppinglist.utility.ImageMapper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

private const val defaultIconId = R.drawable.ic_account_image
private const val RC_AUTH = 69

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "shoppinglist_1234abcd"
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private var firstLogin = true
    private val mapper = ImageMapper()

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
                R.id.nav_shoppinglists,
                R.id.nav_friends,
                R.id.nav_account,
                R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        login()

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
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                emailVerified()
                val uidUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
                val docRef = FirebaseFirestore.getInstance().document("users/$uidUser")
                docRef.get()
                    .addOnSuccessListener { docSnap ->
                        if (!docSnap.exists()) {
                            createUserDoc()
                        }
                        displayUserInformation()
                    }
                displayUserInformation()
            } else {
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
            val message = AlertDialog.Builder(this, R.style.AlertDialogTheme)
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
                        .setIsSmartLockEnabled(false, true)
                        .build(),
                    RC_AUTH
                )
            }

            displayUserInformation()
            Log.d(TAG, "MainActivity_login()_End")
        }
    }

    private fun displayUserInformation() {
        Log.d(TAG, "display user information in navBar")
        val fb = FirebaseAuth.getInstance()
        val user = fb.currentUser
        val tvEmail = nav_view.getHeaderView(0).findViewById<TextView>(R.id.tvEmail)
        tvEmail.text = if (user?.email == null) "Loading Email..." else user.email

        val uidUser = user?.uid.toString()
        val userRef = FirebaseFirestore.getInstance().document("users/$uidUser")
        userRef.get().addOnSuccessListener { documentSnapshot ->
            tvName.text = documentSnapshot.get("username") as String? ?: "Loading Username..."
            ivNavUserIcon.setImageResource(mapper.download((documentSnapshot.get("icon_id") as Long).toInt()))
        }
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

        docRef.set(
            mapOf(
                "username" to "",
                "icon_id" to mapper.upload(defaultIconId)
            )
        )
            .addOnSuccessListener {
                Log.d(TAG, "User Document created")
            }
            .addOnFailureListener {
                Log.d(TAG, "put data in userDoc failed")
            }

        findingUsername()

        docRef.update("friends", FieldValue.arrayUnion())
        docRef.update("favorites", FieldValue.arrayUnion())
        docRef.update("friendRequests", FieldValue.arrayUnion())
    }

    private fun findingUsername() {
        Log.d(TAG, "Finding Username Start")
        var username = FirebaseAuth.getInstance().currentUser?.displayName.toString() + "#"
        username = username.trim().replace(
            "\\s+".toRegex(),
            " "
        ) //clean the user name input; no leading, trailing or consecutive whitespaces
        Log.d(TAG, "findingUsername username: $username")
        val colRef = FirebaseFirestore.getInstance().collection("users")

        val uidUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val docRef = colRef.document(uidUser)
        docRef
            .addSnapshotListener { _, _ ->
                displayUserInformation()
            }

        colRef
            .whereGreaterThan("username", username)
            .whereLessThan("username", username + "999999999")
            .orderBy("username", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener {
                var numberOfUsername: Int? = 1

                if (it.documents.size >= 1) {
                    val username2 = it.documents[0].get("username") as String?
                    numberOfUsername = username2?.substring(username2.lastIndexOf("#") + 1)?.toInt()
                    if (numberOfUsername != null) numberOfUsername++ else numberOfUsername = 1
                }

                val userRef = FirebaseFirestore.getInstance().document("users/$uidUser")
                username += "$numberOfUsername"
                userRef.update("username", username)
            }
    }

    private fun emailVerified() {
        if (FirebaseAuth.getInstance().currentUser?.isEmailVerified == false) {
            val user = FirebaseAuth.getInstance().currentUser!!

            if (firstLogin) {
                FirebaseAuth.getInstance().useAppLanguage()
                user.sendEmailVerification().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Email sent.")
                    }
                }
                firstLogin = false
            }

            AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogTheme))
                .setTitle(R.string.emailVerificationTitle)
                .setMessage(R.string.emailVerificationBody)
                .setCancelable(false)
                .setPositiveButton(R.string.emailVerificationDelete) { _, _ ->
                    val uid = user.uid
                    val docRef = FirebaseFirestore.getInstance().document("users/$uid")
                    docRef.delete().addOnSuccessListener { Log.d(TAG, "Database Data deleted") }
                    user.delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "user account deleted")
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                this.finishAffinity()
                            }
                        }
                }
                .setNegativeButton(R.string.emailVerificationSendNew) { _, _ ->
                    FirebaseAuth.getInstance().useAppLanguage()
                    user.sendEmailVerification().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "Email sent.")
                        }
                    }
                    emailVerified()
                }
                .setNeutralButton(R.string.emailVerificationReload) { _, _ ->
                    user.reload()
                    emailVerified()
                }
                .show()
        }
    }

}