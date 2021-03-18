package de.codingkeks.shoppinglist.ui.account

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import de.codingkeks.shoppinglist.*
import de.codingkeks.shoppinglist.MainActivity.Companion.TAG
import de.codingkeks.shoppinglist.utility.ImageMapper
import kotlinx.android.synthetic.main.fragment_account.*

private const val RC_DELETE_ACC = 42
private const val RC_REAUTH_USER = 69
private const val RC_CHANGE_IMAGE = 420

class AccountFragment : Fragment() {

    private val mapper = ImageMapper()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_account, container, false)
        return root
    }

    override fun onStart() {
        super.onStart()
        val fb = FirebaseAuth.getInstance()
        val user = fb.currentUser!!
        FirebaseFirestore.getInstance().document("users/${user.uid}").get()
            .addOnSuccessListener {
                if (it.get("icon_id") == null) return@addOnSuccessListener
                val image = (it.getLong("icon_id") as Long).toInt()
                ivAccountImage.setImageResource(mapper.download(image))
            }
        tv_userEmail.text = user.email
        tv_userID.text = user.uid

        val uidUser = user.uid
        val userRef = FirebaseFirestore.getInstance().document("users/$uidUser")
        userRef.get().addOnSuccessListener { documentSnapshot ->
            tv_userName.text =
                documentSnapshot.get("username") as String? ?: getString(R.string.load_username)
        }

        buAccountLogout.setOnClickListener {
            AuthUI.getInstance()
                .signOut(requireContext())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "user has been logged out")
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(context, LoginActivity::class.java)
                        startActivity(intent)
                        activity?.finish()
                    }
                }
        }

        buDeleteAccount.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
                .setTitle(R.string.deleteTitle)
                .setMessage(R.string.deleteAccount)
                .setPositiveButton(R.string.yes) { _, _ ->
                    if (user.providerData[1].providerId == EmailAuthProvider.PROVIDER_ID) {
                        val intent = Intent(requireContext(), ReauthenticateActivity::class.java)
                        startActivityForResult(intent, RC_REAUTH_USER)
                    } else {
                        val providers = arrayListOf(
                            AuthUI.IdpConfig.GoogleBuilder().build()
                        )
                        startActivityForResult(
                            AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(providers)
                                .setLogo(R.drawable.ic_shopping_list_app_icon)
                                .setTheme(R.style.AppTheme)
                                .setIsSmartLockEnabled(false, true)
                                .build(),
                            RC_DELETE_ACC
                        )
                    }
                }
                .setNegativeButton(R.string.no) { _, _ -> }
                .create()
                .show()
        }

        buResetPassword.setOnClickListener {
            if (user.providerData[1].providerId == EmailAuthProvider.PROVIDER_ID) {
                val eMail = user.email!!
                fb.useAppLanguage()
                fb.sendPasswordResetEmail(eMail)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "password reset eMail sent")
                        }
                    }
            } else {
                Toast.makeText(context, R.string.google_password_reset, Toast.LENGTH_LONG).show()
            }
        }

        ivAccountImage.setOnClickListener {
            editAccountImage()
        }

        ivAccount_editImage.setOnClickListener {
            editAccountImage()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_REAUTH_USER && resultCode == Activity.RESULT_OK) {
            val user = FirebaseAuth.getInstance().currentUser!!
            val password: String = data!!.getStringExtra("password")!!
            val credential = EmailAuthProvider.getCredential(user.email.toString(), password)

            user.reauthenticate(credential)
                .addOnSuccessListener {
                    Log.d(TAG, "User re-authenticated.")
                    deleteAccount()
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
                        deleteAccount()
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
                if (data.hasExtra("image")) {
                    //set selected image to new account image
                    val selectedImage = data.getIntExtra("image", -1)
                    ivAccountImage.setImageResource(selectedImage)
                    val user = FirebaseAuth.getInstance().currentUser!!
                    FirebaseFirestore.getInstance().document("users/${user.uid}")
                        .update("icon_id", mapper.upload(selectedImage))
                }
            }
        }
    }

    private fun editAccountImage() {
        Log.d(TAG, "Image view to change account image has been clicked")
        //TODO pass the right images as an arrayList to the ImagePickerActivity;
        //just a testing arrayList with random images
        val images = arrayListOf(
            R.drawable.ic_account_image,
            R.drawable.ic_pregnant_woman,
            R.drawable.ic_male,
            R.drawable.ic_female,
            R.drawable.ic_transgender,
            R.drawable.ic_accessible,
            R.drawable.ic_catching_pokemon,
            R.drawable.ic_child,
            R.drawable.ic_theater_mask,
            R.drawable.ic_support_agent,
            R.drawable.ic_hiking,
            R.drawable.ic_neutral,
            R.drawable.ic_very_satisfied,
            R.drawable.ic_very_dissatisfied,
            R.drawable.ic_self_improvement,
            R.drawable.ic_elderly,
            R.drawable.ic_flutter_dash,
            R.drawable.ic_nature_people
        )
        Intent(context, ImagePickerActivity::class.java).also {
            it.putExtra("images", images)
            startActivityForResult(it, RC_CHANGE_IMAGE)
        }
    }

    private fun deleteAccount() {
        val user = FirebaseAuth.getInstance().currentUser!!
        val uid = user.uid

        val docRef = FirebaseFirestore.getInstance().document("users/$uid")
        docRef.delete().addOnSuccessListener {
            Log.d(TAG, "Database Data deleted")
            FirebaseFirestore.getInstance().collection("users")
                .whereArrayContains("friends", uid)
                .get().addOnSuccessListener { qSnap ->
                    qSnap.forEach { qdSnap ->
                        qdSnap.reference.update("friends", FieldValue.arrayRemove(uid))
                        //TODO friend Requests löschen
                    }
                    FirebaseFirestore.getInstance().collection("lists")
                        .whereArrayContains("members", uid)
                        .get().addOnSuccessListener { qSnap ->
                            qSnap.forEach { qdSnap ->
                                qdSnap.reference.update("members", FieldValue.arrayRemove(uid))
                                val membersList = qdSnap.get("members") as ArrayList<*>
                                Log.d(TAG, membersList.size.toString() + ": " + membersList.toString())
                                if (membersList.size == 0 || (membersList.size == 1 && membersList[0].equals(uid))) {
                                    deleteItemsCollection(qdSnap.id)
                                    qdSnap.reference.delete()
                                }
                            }
                        }.addOnSuccessListener {
                            user.delete()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.d(TAG, "user account deleted")
                                        val intent = Intent(context, LoginActivity::class.java)
                                        startActivity(intent)
                                        activity?.finish()
                                    }
                                }
                        }
                }
        }
    }

    private fun deleteItemsCollection(listId: String) {
        FirebaseFirestore.getInstance().collection("lists/$listId/items")
            .get().addOnSuccessListener { qSnap ->
                Log.d(TAG, "Succes 1")
                qSnap.forEach { qdSnap ->
                    qdSnap.reference.delete()
                }
                Log.d(TAG, "Succes 2")
            }
    }

}