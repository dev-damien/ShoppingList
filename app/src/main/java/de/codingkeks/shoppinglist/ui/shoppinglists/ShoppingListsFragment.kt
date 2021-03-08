package de.codingkeks.shoppinglist.ui.shoppinglists

import android.app.Activity
import android.content.Intent
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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import de.codingkeks.shoppinglist.AddNewListActivity
import de.codingkeks.shoppinglist.MainActivity
import de.codingkeks.shoppinglist.R
import de.codingkeks.shoppinglist.recyclerview.shoppinglists.ListAdapter
import de.codingkeks.shoppinglist.recyclerview.shoppinglists.ShoppingList
import kotlinx.android.synthetic.main.fragment_shoppinglists.*

class ShoppingListsFragment : Fragment() {

    private lateinit var shoppingListsViewModel: ShoppingListsViewModel
    private val RC_ADD_NEW_LIST = 0
    private var shoppingList: MutableList<ShoppingList> = mutableListOf()
    private lateinit var adapter: ListAdapter
    private lateinit var registration: ListenerRegistration

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(MainActivity.TAG, "ShoppingListsFragment()_onCreateView()_Start")
        shoppingListsViewModel =
            ViewModelProviders.of(this).get(ShoppingListsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_shoppinglists, container, false)
        /*val textView: TextView = root.findViewById(R.id.tv_userName)
        shoppingListsViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })*/

        Log.d(MainActivity.TAG, "ShoppingListsFragment()_onCreateView()_End")
        return root
    }

    override fun onStart() {
        Log.d(MainActivity.TAG, "ShoppingListsFragment()_onStart()_Start")
        super.onStart()

        adapter = ListAdapter(shoppingList, spLists.selectedItemPosition)
        rvLists.adapter = adapter
        rvLists.layoutManager = LinearLayoutManager(requireContext())
        rvLists.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        val user = FirebaseAuth.getInstance().currentUser!!
        val docRef = FirebaseFirestore.getInstance().collection("lists")
        registration = docRef.whereArrayContains("members", user.uid).addSnapshotListener { qSnap, _ ->
                val userRef = FirebaseFirestore.getInstance().document("users/${user.uid}")
                userRef.get().addOnSuccessListener { userDSnap ->
                    val arrayFavorites = userDSnap.get("favorites") as ArrayList<*>
                    shoppingList.clear()
                    qSnap?.forEach {
                        shoppingList.add(
                            ShoppingList(
                                it.getString("name") ?: "Error 69",
                                (it.get("icon_id") as Long).toInt(),
                                arrayFavorites.contains(it.id),
                                it.id
                            )
                        )
                    }
                    sortingShoppingList(spLists.selectedItemPosition)
                    adapter.updateList()
                    adapter.notifyDataSetChanged()
                }
            }

        spLists.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                sortingShoppingList(position)
                adapter.notifyDataSetChanged()
                adapter.updateSpinnerPos(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        svLists.imeOptions = EditorInfo.IME_ACTION_DONE
        svLists.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })

        //add a new list by clicking on the fab
        fabAddNewList.setOnClickListener {
            Intent(context, AddNewListActivity::class.java).also {
                startActivityForResult(it, RC_ADD_NEW_LIST)
            }
        }
        Log.d(MainActivity.TAG, "ShoppingListsFragment()_onStart()_End")
    }

    override fun onStop() {
        super.onStop()
        registration.remove()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(MainActivity.TAG, "ShoppingListsFragment()_onActivityResult()_Start")
        super.onActivityResult(requestCode, resultCode, data)
        //data to create a new list
        if (requestCode == RC_ADD_NEW_LIST) {
            var listName = ""
            var listIcon = R.drawable.ic_menu_shoppinglists
            var isFav = false
            if (resultCode == Activity.RESULT_OK && data != null) {
                if (data.hasExtra("name")) {
                    listName = data.getStringExtra("name") ?: "ErrorList: How tf did you do this"
                }
                if (data.hasExtra("icon")) {
                    listIcon = data.getIntExtra("icon", R.drawable.ic_menu_shoppinglists)
                }
                if (data.hasExtra("isFav")) {
                    isFav = data.getBooleanExtra("isFav", false)
                }
                createNewGroup(listName, listIcon, isFav, arrayListOf()) //TODO get selected members
            }
        }
        Log.d(MainActivity.TAG, "ShoppingListsFragment()_onActivityResult()_End")
    }

    /**
     * create a new list with selected name and icon
     * @param listName the name of the new list
     * @param listIcon the icon of the new list
     */
    private fun createNewGroup(
        listName: String,
        listIcon: Int,
        isFav: Boolean,
        members: ArrayList<String>
    ) {
        Log.d(MainActivity.TAG, "ShoppingListsFragment()_createNewGroup_Start")
        try {
            val user = FirebaseAuth.getInstance().currentUser!!
            val colRefLists = FirebaseFirestore.getInstance().collection("lists")
            members.add(user.uid)
            var listData = hashMapOf(
                "name" to listName,
                "icon_id" to listIcon,
                "description" to "",
                "members" to members
            )
            colRefLists
                .add(listData)
                .addOnSuccessListener {
                    if (isFav) {
                        val docRefUser =
                            FirebaseFirestore.getInstance().document("users/${user.uid}")
                        docRefUser.update("favorites", FieldValue.arrayUnion(it.id))
                    }
                }
                .addOnFailureListener {
                    throw Exception("error when adding the listID to the favs")
                }
            sortingShoppingList(spLists.selectedItemPosition)
        } catch (ex: Exception) {
            Log.wtf(MainActivity.TAG, "create new group failed")
        }
        Log.d(MainActivity.TAG, "ShoppingListsFragment()_createNewGroup_End")
    }

    fun sortingShoppingList(position: Int) {
        Log.d(MainActivity.TAG, "ShoppingListsFragment()_sortingShoppingList_Start")
        when (position) { //position 0: Favorites; 1: A-Z; 2: Z-A
            0 -> {
                shoppingList.sortBy { it.name }
                shoppingList.sortByDescending { it.isFavorite }
            }
            1 -> {
                shoppingList.sortBy { it.name }
            }
            2 -> {
                shoppingList.sortByDescending { it.name }
            }
        }
        Log.d(MainActivity.TAG, "ShoppingListsFragment()_sortingShoppingList_End")
    }

}
