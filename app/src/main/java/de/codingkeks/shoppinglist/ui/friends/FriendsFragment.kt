package de.codingkeks.shoppinglist.ui.friends

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import de.codingkeks.shoppinglist.MainActivity
import de.codingkeks.shoppinglist.R
import de.codingkeks.shoppinglist.recyclerview.items.Item
import kotlinx.android.synthetic.main.fragment_friends.*
import kotlinx.android.synthetic.main.fragment_items.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FriendsFragment : Fragment() {

    lateinit var myFragment: View
    lateinit var viewPager: ViewPager
    lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        myFragment = inflater.inflate(R.layout.fragment_friends, container, false)

        viewPager = myFragment.findViewById(R.id.viewPagerFriends)
        tabLayout = myFragment.findViewById(R.id.tabLayoutFriends)

        return myFragment
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setUpViewPager(viewPager)
        tabLayout.setupWithViewPager(viewPager)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        //add a new friend
        fabAddFriend.setOnClickListener {
            val alertBuilder =
                AlertDialog.Builder(ContextThemeWrapper(context, R.style.AlertDialogTheme2))
            alertBuilder.setTitle(getString(R.string.addFriendDialogTitle))
            val alertLayout = getEditTextLayout(requireContext())
            alertBuilder.setView(alertLayout)

            val textInputLayout = alertLayout.findViewWithTag<TextInputLayout>("textInputLayoutTag")
            val textInputEditText =
                alertLayout.findViewWithTag<TextInputEditText>("textInputEditTextTag")

            alertBuilder.setPositiveButton(R.string.addFriendDialogPositiveButton) { _, _ ->
                val db = FirebaseFirestore.getInstance()
                val user = FirebaseAuth.getInstance().currentUser!!
                val docRefUser = db.document("users/${user.uid}")
                docRefUser.get().addOnSuccessListener { dSnapUser ->
                    var friendName = textInputEditText.text.toString()
                    if (friendName.matches(".+#[0-9]{1,9}".toRegex())) {
                        db.collection("users")
                            .whereEqualTo("username", friendName)
                            .get()
                            .addOnSuccessListener {
                                //test if requested user exists
                                if (it.size() == 0) {
                                    //no user with this name was found
                                    Toast.makeText(
                                        requireContext(),
                                        getString(R.string.addFriendDialogFriendNotFound),
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    //user was found
                                    if (it.size() > 1) Log.wtf(
                                        MainActivity.TAG,
                                        "there are ${it.size()} users with the name $friendName"
                                    )
                                    //test if requested user is already added
                                    val docRefFriend = it.documents[0]
                                    if ((dSnapUser.get("friends") as ArrayList<String>).contains(
                                            docRefFriend.id
                                        )
                                    ) {
                                        //requested user is an already added friend
                                        Toast.makeText(
                                            requireContext(),
                                            R.string.addFriendDialogFriendAlreadyAdded,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        //requested user is not a friend yet
                                        //test if requested-user already requested the user
                                        if ((dSnapUser.get("friendRequests") as ArrayList<String>).contains(
                                                docRefFriend.id
                                            )
                                        ) {
                                            //other user already requested this user -> they will be added as friends
                                            Toast.makeText(
                                                requireContext(),
                                                getString(R.string.addFriendDialogFriendRequestedEachOther),
                                                Toast.LENGTH_LONG
                                            ).show()
                                            docRefUser.update(
                                                "friends",
                                                FieldValue.arrayUnion(docRefFriend.id)
                                            )
                                            val requestedUser =
                                                db.document("users/${docRefFriend.id}")
                                            requestedUser.update(
                                                "friends",
                                                FieldValue.arrayUnion(docRefUser.id)
                                            )
                                            docRefUser.update(
                                                "friendRequests",
                                                FieldValue.arrayRemove(docRefFriend.id)
                                            ) //remove id of requested user from requestArray
                                        } else {
                                            //test if input is own name (weird loser, get some real friends)
                                            if (docRefFriend.id == docRefUser.id) {
                                                //user requested himself
                                                Toast.makeText(
                                                    requireContext(),
                                                    getString(R.string.addFriendDialogSelfRequest),
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            } else {
                                                //test if user already requested friend (second time request)
                                                if ((docRefFriend.get("friendRequests") as ArrayList<String>).contains(
                                                        docRefUser.id
                                                    )
                                                ) {
                                                    //friend request was send already
                                                    Toast.makeText(
                                                        requireContext(),
                                                        getText(R.string.addFriendDialogFriendAlreadyRequested),
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                } else {
                                                    //normal request process
                                                    val requestedUser =
                                                        db.document("users/${docRefFriend.id}")
                                                    requestedUser.update(
                                                        "friendRequests",
                                                        FieldValue.arrayUnion(docRefUser.id)
                                                    )
                                                    Toast.makeText(requireContext(), getString(R.string.addFriendDialogFriendRequestedSucess), Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                    } else {
                        Log.d(MainActivity.TAG, "friend search input is invalid")
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.addFriendDialogInputInvalidToast),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            alertBuilder.setNeutralButton(R.string.cancel, null)
            alertBuilder.setCancelable(false)
            val dialog = alertBuilder.create()
            dialog.show()

            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).isEnabled = false

            textInputEditText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                }

                override fun beforeTextChanged(
                    p0: CharSequence?, p1: Int,
                    p2: Int, p3: Int
                ) {
                }

                override fun onTextChanged(
                    p0: CharSequence?, p1: Int,
                    p2: Int, p3: Int
                ) {
                    if (p0.isNullOrBlank()) {
                        textInputLayout.error = getString(R.string.itemNameRequired)
                        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                            .isEnabled = false
                    } else {
                        textInputLayout.error = ""
                        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                            .isEnabled = true
                    }
                }
            })
        }
    }

    private fun setUpViewPager(viewPager: ViewPager) {
        var adapter: FragmentPagerAdapterFriends = FragmentPagerAdapterFriends(childFragmentManager)

        adapter.addFragment(FriendsAddedFragment(), getString(R.string.friends_added_tab_title))
        adapter.addFragment(
            FriendsRequestsFragment(),
            getString(R.string.friends_requests_tab_title)
        )

        viewPager.adapter = adapter
    }

    @SuppressLint("SetTextI18n")
    private fun getEditTextLayout(context: Context): ConstraintLayout {
        val constraintLayout = ConstraintLayout(context)
        val layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        constraintLayout.layoutParams = layoutParams
        constraintLayout.id = View.generateViewId()

        val textInputLayout = TextInputLayout(context)
        textInputLayout.boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
        layoutParams.setMargins(
            32.toDp(context),
            8.toDp(context),
            32.toDp(context),
            8.toDp(context)
        )
        textInputLayout.layoutParams = layoutParams
        textInputLayout.hint = getString(R.string.addFriendDialogNameInputHint)
        textInputLayout.id = View.generateViewId()
        textInputLayout.tag = "textInputLayoutTag"

        val textInputEditText = TextInputEditText(context)
        textInputEditText.id = View.generateViewId()
        textInputEditText.tag = "textInputEditTextTag"
        textInputEditText.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        textInputLayout.addView(textInputEditText, 0)

        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)

        constraintLayout.addView(textInputLayout)
        return constraintLayout
    }

    private fun Int.toDp(context: Context): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics
    ).toInt()
}