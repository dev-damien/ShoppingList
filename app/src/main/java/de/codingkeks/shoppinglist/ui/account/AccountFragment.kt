package de.codingkeks.shoppinglist.ui.account

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import de.codingkeks.shoppinglist.MainActivity
import de.codingkeks.shoppinglist.MainActivity.Companion.TAG
import de.codingkeks.shoppinglist.R
import de.codingkeks.shoppinglist.ReauthenticateActivity
import kotlinx.android.synthetic.main.fragment_account.*

class AccountFragment : Fragment() {

    private lateinit var accountViewModel: AccountViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(MainActivity.TAG, "AccountFragment_onCreateView()_Start")
        /*
        accountViewModel =
            ViewModelProviders.of(this).get(AccountViewModel::class.java)
        */
        val root = inflater.inflate(R.layout.fragment_account, container, false)
        /*
        val textView: TextView = root.findViewById(R.id.tv_userName)
        accountViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
         */
        Log.d(MainActivity.TAG, "AccountFragment_onCreateView()_End")
        return root
    }

    override fun onStart() {
        Log.d(MainActivity.TAG, "AccountFragment_onStart()_Start")
        super.onStart()
        val fb = FirebaseAuth.getInstance()
        val user = fb.currentUser!!
        tv_userEmail.text = user.email
        tv_userID.text = user.uid

        val uidUser = user?.uid.toString()
        val userRef = FirebaseFirestore.getInstance().document("users/$uidUser")
        userRef.get().addOnSuccessListener { documentSnapshot ->
            tv_userName.text = documentSnapshot.get("username") as String? ?: "Loading Username..."
        }

        //TODO remove after verification is included in sign up process
        //just for testing
        buVerify.setOnClickListener {
            fb.useAppLanguage()
            user.sendEmailVerification().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(MainActivity.TAG, "Email sent.")
                }
            }
        }

        buAccountLogout.setOnClickListener {
            AuthUI.getInstance()
                .signOut(requireContext())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(MainActivity.TAG, "user has been logged out")
                        FirebaseAuth.getInstance().signOut()
                        val intent: Intent = Intent(context, MainActivity::class.java)
                        startActivity(intent)
                        activity?.finishAffinity()
                    }
                }
        }

        buDeleteAccount.setOnClickListener {
            AlertDialog.Builder(ContextThemeWrapper(context, R.style.AlertDialogTheme))
                .setTitle(R.string.deleteTitle)
                .setMessage(R.string.deleteAccount)
                .setPositiveButton(R.string.yes){_, _->
                    if (user.providerData[1].providerId == EmailAuthProvider.PROVIDER_ID) {
                        val intent: Intent =
                            Intent(requireContext(), ReauthenticateActivity::class.java)
                        startActivityForResult(intent, 155)
                    } else {
                        val providers = arrayListOf(
                            AuthUI.IdpConfig.GoogleBuilder().build()
                        )
                        startActivityForResult(
                            AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(providers)
                                .setIsSmartLockEnabled(false, true)
                                .build(),
                            55
                        )
                    }
                }
                .setNegativeButton(R.string.no){_, _->}
                .show()
        }

        buResetPassword.setOnClickListener {
            val eMail = user.email!!
            fb.useAppLanguage()
            fb.sendPasswordResetEmail(eMail)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(MainActivity.TAG, "password reset eMail sent")
                    }
                }
        }

        Log.d(MainActivity.TAG, "AccountFragment_onStart()_End")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 155 && resultCode == Activity.RESULT_OK) {
            val user = FirebaseAuth.getInstance().currentUser!!
            var password: String = data!!.getStringExtra("password")
            val credential = EmailAuthProvider.getCredential(user.email.toString(), password)
            password = ""
            val uid = user.uid
            val docRef = FirebaseFirestore.getInstance().document("users/$uid")
            docRef.delete().addOnSuccessListener { Log.d(TAG, "Database Data deleted") }
            user.reauthenticate(credential)
                .addOnSuccessListener {
                    Log.d(TAG, "User re-authenticated.")
                    user.delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(MainActivity.TAG, "user account deleted")
                                val intent: Intent = Intent(context, MainActivity::class.java)
                                startActivity(intent)
                                activity?.finishAffinity()
                            }
                        }
                }
                .addOnFailureListener {
                    Log.d(TAG, "PW wrong")
                    Toast.makeText(context, R.string.wrongPassword, Toast.LENGTH_LONG).show()
                }
        }
        if (requestCode == 55 && resultCode == Activity.RESULT_OK) {
            val user = FirebaseAuth.getInstance().currentUser!!
            try {
                val id = IdpResponse.fromResultIntent(data)!!.idpToken
                val credential = GoogleAuthProvider.getCredential(id, null)

                user.reauthenticate(credential)
                    .addOnSuccessListener {
                        Log.d(TAG, "User re-authenticated.")
                        val uid = user.uid
                        val docRef = FirebaseFirestore.getInstance().document("users/$uid")
                        docRef.delete().addOnSuccessListener { Log.d(TAG, "Database Data deleted") }
                        user.delete()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d(MainActivity.TAG, "user account deleted")
                                    val intent: Intent = Intent(context, MainActivity::class.java)
                                    startActivity(intent)
                                    activity?.finishAffinity()
                                }
                            }
                    }
                    .addOnFailureListener {
                        Log.d(TAG, "PW wrong 1")
                        Toast.makeText(context, R.string.wrongPassword, Toast.LENGTH_LONG).show()
                    }
            } catch (ex: Exception) {
                Log.d(TAG, "PW wrong 2")
                Toast.makeText(context, R.string.wrongPassword, Toast.LENGTH_LONG).show()
            }
        }
    }

}