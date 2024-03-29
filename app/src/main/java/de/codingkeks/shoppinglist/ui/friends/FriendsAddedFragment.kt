
package de.codingkeks.shoppinglist.ui.friends

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import de.codingkeks.shoppinglist.MainActivity
import de.codingkeks.shoppinglist.R
import de.codingkeks.shoppinglist.recyclerview.friends.Friend
import de.codingkeks.shoppinglist.recyclerview.friends.FriendAdapter
import de.codingkeks.shoppinglist.utility.DataStoreUtility
import de.codingkeks.shoppinglist.utility.ImageMapper
import kotlinx.android.synthetic.main.fragment_friends_added.*
import kotlinx.android.synthetic.main.fragment_friends_requests.*
import kotlinx.android.synthetic.main.fragment_shoppinglists.*
import kotlinx.coroutines.launch
import java.util.*

class FriendsAddedFragment : Fragment() {

    private var friendList: MutableList<Friend> = mutableListOf()
    private lateinit var adapter: FriendAdapter
    private lateinit var registration: ListenerRegistration

    private val mapper = ImageMapper()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_friends_added, container, false)
    }


    override fun onStop() {
        super.onStop()
        registration.remove()
    }

    override fun onStart() {
        super.onStart()
        svFriends.clearFocus()
        svFriends.setQuery("", false)

        adapter = FriendAdapter(friendList, spFriends.selectedItemPosition)
        rvFriends.adapter = adapter
        rvFriends.layoutManager = LinearLayoutManager(requireContext())

        if (rvFriends.itemDecorationCount <= 0) {
            rvFriends.addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
        }

        val user = FirebaseAuth.getInstance().currentUser!!
        val db = FirebaseFirestore.getInstance()
        val userDoc = db.document("users/${user.uid}")
        registration = userDoc.addSnapshotListener { userDocSnap, e ->
            if (e != null) {
                Log.w(MainActivity.TAG, "Listen to user doc failed (in friends)")
                return@addSnapshotListener
            }
            if (userDocSnap == null || !userDocSnap.exists()) {
                Log.w(MainActivity.TAG, "Data of user doc (in friends): null")
                return@addSnapshotListener
            }
            val friendIds = userDocSnap.get("friends") as ArrayList<*> //get all IDs of the friends
            if (friendIds.isEmpty()) {
                tvNoFriends.text =
                    getString(R.string.friend_requests_no_friends)
                friendList.clear()
                adapter.updateList()
                adapter.notifyDataSetChanged()
                return@addSnapshotListener
            }
            tvNoFriends.text = ""

            //get friends data and add to the list
            db.collection("users")
                .whereIn(FieldPath.documentId(), friendIds).get()
                .addOnSuccessListener { friendsDocs  ->
                    Log.d(MainActivity.TAG, "read friends was successful: ${friendsDocs.size()} friend(s)")
                    friendList.clear()
                    for (friend in friendsDocs) {
                        friendList.add(
                            Friend(
                                friend.getString("username")!!,
                                mapper.download((friend.getLong("icon_id") as Long).toInt()),
                                friend.id
                            )
                        )
                    }
                    adapter.updateList()
                    sortingFriendsList()
                }
                .addOnFailureListener{
                    Log.d(MainActivity.TAG, "read friends failed")
                }
        }

        spFriends.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    lifecycleScope.launch {
                        DataStoreUtility.saveInt("friendSpinnerPos", position, requireContext())
                        sortingFriendsList()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
        svFriends.imeOptions = EditorInfo.IME_ACTION_DONE
        svFriends.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    adapter.filter.filter(newText)
                    return false
                }
            })

        lifecycleScope.launch {
            spFriends.setSelection(DataStoreUtility.readInt("friendSpinnerPos", requireContext()))
        }
    }

    /**
     * method to sort the ArrayList with all the friends
     */
    fun sortingFriendsList() {
        lifecycleScope.launch {
            val position = DataStoreUtility.readInt("friendSpinnerPos", requireContext())
            when (position) { //0: A-Z; 1: Z-A
                0 -> {
                    friendList.sortBy { it.name.toLowerCase(Locale.ROOT) }
                }
                1 -> {
                    friendList.sortByDescending { it.name.toLowerCase(Locale.ROOT) }
                }
            }
            adapter.updateSpinnerPos(position)
            adapter.notifyDataSetChanged()
        }
    }
}