package de.codingkeks.shoppinglist.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import de.codingkeks.shoppinglist.R
import kotlinx.android.synthetic.main.fragment_account.*
import java.util.*

class AccountFragment : Fragment() {

    private lateinit var accountViewModel: AccountViewModel

    override fun onCreateView(
        //TODO clean up... if you know how
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

        return root
    }

    override fun onStart() {
        super.onStart()
        //show all account information of the current user
        val fb = FirebaseAuth.getInstance()
        val user = fb.currentUser
        tvAccountEmail.text = if (user?.email == null) "value is null" else user.email
        tvAccountName.text = if (user?.displayName == null) "value is null" else user.displayName
        tvAccountID.text = if (user?.uid == null) "value is null" else user.uid

        buVerify.setOnClickListener {
            FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
        }
    }

}
/* TODO send eMail in right language
val fb = FirebaseAuth.getInstance()
buEnglish.setOnClickListener {
    fb.setLanguageCode(Locale.getDefault().language)
    fb.currentUser?.sendEmailVerification()
}

 */