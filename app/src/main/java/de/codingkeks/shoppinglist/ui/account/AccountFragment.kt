package de.codingkeks.shoppinglist.ui.account

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import de.codingkeks.shoppinglist.MainActivity
import de.codingkeks.shoppinglist.R
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
        tv_userName.text = user.displayName
        tv_userEmail.text = user.email
        tv_userID.text = user.uid

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
                        //login() TODO send back to login screen and remove all information in the app (such as email, name, lists, friends, ...)
                    }
                }
        }

        //TODO reauthenticate the user to prevent a FirebaseAuthRecentLoginRequiredException
        buDeleteAccount.setOnClickListener {
            user.delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(MainActivity.TAG, "user account deleted")
                    }
                }
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

}