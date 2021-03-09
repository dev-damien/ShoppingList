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
import de.codingkeks.shoppinglist.MainActivity
import de.codingkeks.shoppinglist.R
import de.codingkeks.shoppinglist.recyclerview.friends.Friend
import de.codingkeks.shoppinglist.recyclerview.friends.FriendAdapter
import de.codingkeks.shoppinglist.recyclerview.shoppinglists.ShoppingList
import kotlinx.android.synthetic.main.fragment_friends_added.*
import kotlinx.android.synthetic.main.fragment_shoppinglists.*

class FriendsAddedFragment : Fragment() {

    private lateinit var friendsViewModel: FriendsViewModel
    var friendList: MutableList<Friend> = mutableListOf()
    private lateinit var adapter: FriendAdapter

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

        adapter = FriendAdapter(friendList)
        rvFriends.adapter = adapter
        rvFriends.layoutManager = LinearLayoutManager(requireContext())
        rvFriends.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        //get all friends of the user
        val user = FirebaseAuth.getInstance().currentUser!!
        val docRef = FirebaseFirestore.getInstance().document("users/${user.uid}")
        docRef.get()
            .addOnSuccessListener { userDSnap ->
                if (userDSnap == null) {
                    Log.d(
                        MainActivity.TAG,
                        "cant read friends, because user does not exist in database"
                    )
                } else {
                    Log.d(MainActivity.TAG, "friends will be read")
                    var friendsIDs = userDSnap.get("friends") as ArrayList<Friend>
                    if (friendsIDs.isEmpty()) return@addOnSuccessListener
                    //get the documents of the friends
                    val colRef = FirebaseFirestore.getInstance().collection("users")
                    //TODO get only the documents with a name/ID which is in the array
                    colRef.whereIn(FieldPath.documentId(), friendsIDs).get()
                        .addOnSuccessListener { querySnapshot ->
                            friendList.clear()
                            querySnapshot.forEach {
                                friendList.add(
                                    Friend(
                                        it.getString("username") ?: "ERROR",
                                        (it.getLong("icon_id") as Long).toInt(),
                                        it.id
                                    )
                                )
                            }
                            adapter.updateList()
                            adapter.notifyDataSetChanged()
                        }
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