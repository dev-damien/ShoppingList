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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import de.codingkeks.shoppinglist.ImagePickerActivity
import de.codingkeks.shoppinglist.MainActivity
import de.codingkeks.shoppinglist.MainActivity.Companion.TAG
import de.codingkeks.shoppinglist.R
import de.codingkeks.shoppinglist.ReauthenticateActivity
import de.codingkeks.shoppinglist.utility.ImageMapping
import kotlinx.android.synthetic.main.activity_add_new_list.*
import kotlinx.android.synthetic.main.fragment_account.*

private const val RC_DELETE_ACC = 42
private const val RC_REAUTH_USER = 69
private const val RC_CHANGE_IMAGE = 420

class AccountFragment : Fragment() {

    private lateinit var accountViewModel: AccountViewModel
    private val mapper = null

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
        FirebaseFirestore.getInstance().document("users/${user.uid}").get()
            .addOnSuccessListener {
                val image = (it.getLong("icon_id") as Long).toInt()
                ivAccountImage.setImageResource(image)
            }
        tv_userEmail.text = user.email
        tv_userID.text = user.uid

        val uidUser = user.uid
        val userRef = FirebaseFirestore.getInstance().document("users/$uidUser")
        userRef.get().addOnSuccessListener { documentSnapshot ->
            tv_userName.text = documentSnapshot.get("username") as String? ?: "Loading Username..." //TODO stings.xml
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
                        startActivityForResult(intent, RC_REAUTH_USER)
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
                            RC_DELETE_ACC
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

        ivAccountImage.setOnClickListener {
            editAccountImage()
        }

        ivAccount_editImage.setOnClickListener {
            editAccountImage()
        }

        Log.d(MainActivity.TAG, "AccountFragment_onStart()_End")
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_REAUTH_USER && resultCode == Activity.RESULT_OK) {
            val user = FirebaseAuth.getInstance().currentUser!!
            var password: String = data!!.getStringExtra("password")
            val credential = EmailAuthProvider.getCredential(user.email.toString(), password)
            password = ""
            val uid = user.uid
            val docRefUser = FirebaseFirestore.getInstance().document("users/$uid")
            docRefUser.delete().addOnSuccessListener { Log.d(TAG, "Database Data deleted") }
            FirebaseFirestore.getInstance().collection("users")
                .whereArrayContains("friends", uid)
                .get().addOnSuccessListener { qSnap ->
                    qSnap.forEach { qdSnap ->
                        qdSnap.reference.update("friends", FieldValue.arrayRemove(uid))
                    }
                }
            FirebaseFirestore.getInstance().collection("lists")
                .whereArrayContains("members", uid)
                .get().addOnSuccessListener { qSnap ->
                    qSnap.forEach { qdSnap ->
                        qdSnap.reference.update("members", FieldValue.arrayRemove(uid))
                    }
                }
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
        if (requestCode == RC_DELETE_ACC && resultCode == Activity.RESULT_OK) {
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
        if (requestCode == RC_CHANGE_IMAGE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                if (data.hasExtra("image")){
                    //set selected image to new account image
                    val selectedImage = data.getIntExtra("image", -1)
                    ivAccountImage.setImageResource(selectedImage)
                    val user = FirebaseAuth.getInstance().currentUser!!
                    FirebaseFirestore.getInstance().document("users/${user.uid}")
                        .update("icon_id", selectedImage)
                }
            }
        }
    }

    private fun editAccountImage(){
        Log.d(MainActivity.TAG, "Image view to change account image has been clicked")
        //TODO pass the right images as an arrayList to the ImagePickerActivity;
        //just a testing arrayList with random images
        val images = arrayListOf(
            R.drawable.ic_menu_settings,
            R.drawable.ic_menu_home,
            R.drawable.common_google_signin_btn_icon_dark_focused,
            R.drawable.ic_account_image,
            R.drawable.ic_friends_person_add,
            R.drawable.ic_launcher_background,
            R.drawable.ic_menu_friends,
            R.drawable.ic_friends_star_border,
            R.drawable.ic_menu_account
        )
        Intent(context, ImagePickerActivity::class.java).also {
            it.putExtra("images", images)
            startActivityForResult(it, RC_CHANGE_IMAGE)
        }
    }

}