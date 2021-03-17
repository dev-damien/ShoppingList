package de.codingkeks.shoppinglist

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import de.codingkeks.shoppinglist.utility.ImageMapper
import de.codingkeks.shoppinglist.utility.ThemeSetter

private const val RC_AUTH = 69
private const val defaultIconId = R.drawable.ic_account_image

class LoginActivity : ThemeSetter() {

    private val mapper = ImageMapper()

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val fb = FirebaseAuth.getInstance()
        if (fb.currentUser == null) {
            login()
        } else if (!fb.currentUser!!.isEmailVerified) {
            emailVerified()
        } else {
            val uidUser = FirebaseAuth.getInstance().currentUser!!.uid
            val docRef = FirebaseFirestore.getInstance().document("users/$uidUser")
            docRef.get()
                .addOnSuccessListener { docSnap ->
                    if (!docSnap.exists()) {
                        createUserDoc()
                    }
                    if (docSnap.exists()) {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        this.finish()
                    }
                }
        }
    }

    private fun login() {
        //if: firebase user does not exist && not online -> Error
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
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(MainActivity.TAG, "MainActivity_onActivityResult()_Start")
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_AUTH) {
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                if (!FirebaseAuth.getInstance().currentUser!!.isEmailVerified) {
                    FirebaseAuth.getInstance().useAppLanguage()
                    FirebaseAuth.getInstance().currentUser!!.sendEmailVerification()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(MainActivity.TAG, "Email sent.")
                            }
                        }
                }

                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                this.finishAffinity()
            } else {
                Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show()
                login()
            }
        }
        Log.d(MainActivity.TAG, "MainActivity_onActivityResult()_End")
    }

    /**
     * Method creates the user document in the collection users
     */
    private fun createUserDoc() {
        Log.d(MainActivity.TAG, "Create User Doc")
        val uidUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val docRef = FirebaseFirestore.getInstance().document("users/$uidUser")

        docRef.set(
            mapOf(
                "username" to "",
                "icon_id" to mapper.upload(defaultIconId)
            )
        )
            .addOnSuccessListener {
                Log.d(MainActivity.TAG, "User Document created")
            }
            .addOnFailureListener {
                Log.d(MainActivity.TAG, "put data in userDoc failed")
            }

        docRef.update("friends", FieldValue.arrayUnion())
        docRef.update("favorites", FieldValue.arrayUnion())
        docRef.update("friendRequests", FieldValue.arrayUnion())

        findingUsername()
    }

    private fun findingUsername() {
        Log.d(MainActivity.TAG, "Finding Username Start")
        var username = FirebaseAuth.getInstance().currentUser?.displayName.toString() + "#"
        username = username.trim().replace(
            "\\s+".toRegex(),
            " "
        ) //clean the user name input; no leading, trailing or consecutive whitespaces
        Log.d(MainActivity.TAG, "findingUsername username: $username")
        val colRef = FirebaseFirestore.getInstance().collection("users")

        val uidUser = FirebaseAuth.getInstance().currentUser?.uid.toString()

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
                userRef.update("username", username).addOnSuccessListener {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    this.finish()
                }
            }
    }

    private fun emailVerified() {
        val user = FirebaseAuth.getInstance().currentUser!!
        val intent = Intent(this, LoginActivity::class.java)

        AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogTheme))
            .setTitle(R.string.emailVerificationTitle)
            .setMessage(R.string.emailVerificationBody)
            .setCancelable(false)
            .setPositiveButton(R.string.emailVerificationDelete) { _, _ ->
                val uid = user.uid
                val docRef = FirebaseFirestore.getInstance().document("users/$uid")
                docRef.delete()
                    .addOnSuccessListener { Log.d(MainActivity.TAG, "Database Data deleted") }
                user.delete()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(MainActivity.TAG, "user account deleted")
                            startActivity(intent)
                            this.finish()
                        }
                    }
            }
            .setNegativeButton(R.string.emailVerificationSendNew) { _, _ ->
                FirebaseAuth.getInstance().useAppLanguage()
                user.sendEmailVerification().addOnSuccessListener {
                    Log.d(MainActivity.TAG, "Email sent.")
                }
                startActivity(intent)
                this.finish()
            }
            .setNeutralButton(R.string.emailVerificationReload) { _, _ ->
                user.reload().addOnSuccessListener {
                    startActivity(intent)
                    this.finish()
                }
            }
            .show()
    }

    /**
     *@return returns true if the user has any internet connection
     */
    private fun isOnline(context: Context): Boolean {
        Log.d(MainActivity.TAG, "MainActivity_isOnline()_Start")
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
        Log.d(MainActivity.TAG, "MainActivity_isOnline()_End")
        return false
    }
}