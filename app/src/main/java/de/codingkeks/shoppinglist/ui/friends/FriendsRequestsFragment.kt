package de.codingkeks.shoppinglist.ui.friends

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import de.codingkeks.shoppinglist.MainActivity
import de.codingkeks.shoppinglist.R
import de.codingkeks.shoppinglist.recyclerview.friendRequests.FriendRequest
import de.codingkeks.shoppinglist.recyclerview.friendRequests.FriendRequestAdapter
import kotlinx.android.synthetic.main.fragment_friends_added.*
import kotlinx.android.synthetic.main.fragment_friends_requests.*
import kotlinx.android.synthetic.main.fragment_items.*

class FriendsRequestsFragment : Fragment() {

    var friendRequestsList: MutableList<FriendRequest> = mutableListOf()
    private lateinit var adapter: FriendRequestAdapter
    private lateinit var registration: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends_requests, container, false)
    }

    override fun onStart() {
        super.onStart()

        adapter = FriendRequestAdapter(friendRequestsList)
        rvFriendRequests.adapter = adapter
        rvFriendRequests.layoutManager = LinearLayoutManager(requireContext())
        rvFriendRequests.addItemDecoration(
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
                Log.w(MainActivity.TAG, "Listen to user doc failed (in friend Requests)")
                return@addSnapshotListener
            }
            if (userDocSnap == null || !userDocSnap.exists()) {
                Log.w(MainActivity.TAG, "Data of user doc (in friend request): null")
                return@addSnapshotListener
            }
            val friendRequestIds = userDocSnap.get("friendRequests") as ArrayList<String> //get all IDs of the users that requested current user
            if (friendRequestIds.isEmpty()) {
                tvFriendRequestsNoFriends.text =
                    getString(R.string.friend_requests_no_requests)
                friendRequestsList.clear()
                adapter.notifyDataSetChanged()
                return@addSnapshotListener
            }
            tvFriendRequestsNoFriends.text = ""

            //get requested user data and add to the list
            db.collection("users")
                .whereIn(FieldPath.documentId(), friendRequestIds).get()
                .addOnSuccessListener { userDocs  ->
                    Log.d(MainActivity.TAG, "read friend requests was successful: ${userDocs.size()} request(s)")
                    friendRequestsList.clear()
                    for (userRequestDoc in userDocs) {
                        friendRequestsList.add(
                            FriendRequest(
                                userRequestDoc.getString("username")!!,
                                (userRequestDoc.getLong("icon_id") as Long).toInt(),
                                userRequestDoc.id
                            )
                        )
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener{
                    Log.d(MainActivity.TAG, "read friend requests failed")
                }
        }
    }

    override fun onStop() {
        super.onStop()
        registration.remove()
    }
}