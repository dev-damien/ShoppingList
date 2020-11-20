package de.codingkeks.shoppinglist.ui.account

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import de.codingkeks.shoppinglist.MainActivity
import de.codingkeks.shoppinglist.R
import kotlinx.android.synthetic.main.fragment_account.*
import java.util.*

class AccountFragment : Fragment() {

    private lateinit var accountViewModel: AccountViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        //show all account information of the current user
        val fb = FirebaseAuth.getInstance()
        val user = fb.currentUser
        tv_userName.text = if (user?.displayName == null) "value is null" else user.displayName
        tv_userEmail.text = if (user?.email == null) "value is null" else user.email
        tv_userID.text = if (user?.uid == null) "value is null" else user.uid

        buVerify.setOnClickListener {
            fb.useAppLanguage()
            user!!.sendEmailVerification().addOnCompleteListener { task ->
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
                        //login() TODO send back to login screen
                    }
                }
        }

        buDeleteAccount.setOnClickListener {
            user?.delete()
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(MainActivity.TAG, "user account deleted")
                    }
                }
        }

        buResetPassword.setOnClickListener {
            val eMail = user?.email
            if (eMail.isNullOrBlank()) {
                Log.d(
                    MainActivity.TAG,
                    "user tried to reset password but no user was logged in: error"
                )
                Toast.makeText(
                    requireContext(),
                    "no user is logged in right now",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                fb.sendPasswordResetEmail(eMail)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(MainActivity.TAG, "user requested password reset: success")
                        }
                    }
            }
        }

        Log.d(MainActivity.TAG, "AccountFragment_onStart()_End")
    }

}