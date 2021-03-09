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
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import de.codingkeks.shoppinglist.MainActivity
import de.codingkeks.shoppinglist.R
import de.codingkeks.shoppinglist.recyclerview.friendRequests.FriendRequest
import de.codingkeks.shoppinglist.recyclerview.friends.Friend
import de.codingkeks.shoppinglist.recyclerview.friends.FriendAdapter
import kotlinx.android.synthetic.main.fragment_friends_added.*
import kotlinx.android.synthetic.main.fragment_friends_requests.*
import kotlinx.android.synthetic.main.fragment_shoppinglists.*

class FriendsAddedFragment : Fragment() {

    private lateinit var friendsViewModel: FriendsViewModel
    var friendList: MutableList<Friend> = mutableListOf()
    private lateinit var adapter: FriendAdapter
    private lateinit var registration: ListenerRegistration

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(MainActivity.TAG, "FriendsFragment()_onCreateView()_Start")
        friendsViewModel =
            ViewModelProviders.of(this).get(FriendsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_friends_added, container, false)
        /*val textView: TextView = root.findViewById(R.id.text_slideshow)
        friendsViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })*/

        Log.d(MainActivity.TAG, "FriendsFragment()_onCreateView()_End")
        return root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(MainActivity.TAG, "FriendsFragment()_onCreate()_Start")
        super.onCreate(savedInstanceState)
        Log.d(MainActivity.TAG, "FriendsFragment()_onCreate()_Start")
    }

    override fun onStart() {
        super.onStart()
        svFriends.clearFocus()
        svFriends.setQuery("", false)

        adapter = FriendAdapter(friendList)
        rvFriends.adapter = adapter
        rvFriends.layoutManager = LinearLayoutManager(requireContext())
        rvFriends.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

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
            val friendIds = userDocSnap.get("friends") as ArrayList<String> //get all IDs of the friends
            if (friendIds.isEmpty()) {
                tvNoFriends.text =
                    getString(R.string.friend_requests_no_friends)
                friendList.clear()
                adapter.notifyDataSetChanged()
                return@addSnapshotListener
            }
            tvNoFriends.text = ""

            //get friends data and add to the list
            //friendList.clear() TODO test if useless
            db.collection("users")
                .whereIn(FieldPath.documentId(), friendIds).get()
                .addOnSuccessListener { friendsDocs  ->
                    Log.d(MainActivity.TAG, "read friends was successful: ${friendsDocs.size()} friend(s)")
                    friendList.clear()
                    for (friend in friendsDocs) {
                        friendList.add(
                            Friend(
                                friend.getString("username")!!,
                                (friend.getLong("icon_id") as Long).toInt(),
                                friend.id
                            )
                        )
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener{
                    Log.d(MainActivity.TAG, "read friends failed")
                }
            adapter.notifyDataSetChanged()
        }

        spFriends.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    sortingFriendsList(position)
                    adapter.notifyDataSetChanged()
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

        Log.d(MainActivity.TAG, "FriendsFragment()_onCreate()_End")
    }

    fun sortingFriendsList(position: Int) {
        when (position) { //0: A-Z; 1: Z-A
            0 -> {
                friendList.sortBy { it.name }
            }
            1 -> {
                friendList.sortByDescending { it.name }
            }
        }
    }
}