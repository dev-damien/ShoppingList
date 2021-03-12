package de.codingkeks.shoppinglist

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import de.codingkeks.shoppinglist.recyclerview.members.Member
import de.codingkeks.shoppinglist.recyclerview.members.MemberAdapter
import kotlinx.android.synthetic.main.activity_member_management.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "members_settings")

class MemberManagementActivity : AppCompatActivity() {

    var memberList: MutableList<Member> = mutableListOf()
    private lateinit var adapter: MemberAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member_management)

        title = getString(R.string.popup_member)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        svMembers.clearFocus()
        svMembers.setQuery("", false)

        adapter = MemberAdapter(memberList, spMembers.selectedItemPosition)
        rvMembers.adapter = adapter
        rvMembers.layoutManager = LinearLayoutManager(this)
        rvMembers.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        if (intent.hasExtra("listId")) {
            FirebaseFirestore.getInstance().document("lists/${intent.getStringExtra("listId")}")
                .get().addOnSuccessListener { dSnapList ->
                    val memberIds = dSnapList.get("members") as ArrayList<*>
                    FirebaseFirestore.getInstance().collection("users")
                        .whereIn(FieldPath.documentId(), memberIds)
                        .get().addOnSuccessListener { memberDocs ->
                            memberList.clear()
                            memberDocs.forEach {
                                memberList.add(
                                    Member(
                                        it.get("username").toString(),
                                        (it.get("icon_id") as Long).toInt(),
                                        it.id,
                                        true
                                    )
                                )
                            }
                            adapter.updateList()
                            sortingMembersList()
                        }
                }
        }

        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        FirebaseFirestore.getInstance().document("users/$uid")
            .get().addOnSuccessListener { dSnapFriends ->
                val friendIds = dSnapFriends.get("friends") as ArrayList<*>
                FirebaseFirestore.getInstance().collection("users")
                    .whereIn(FieldPath.documentId(), friendIds)
                    .get().addOnSuccessListener { friendsDocs ->
                        friendsDocs.forEach {
                            val newMember = Member(
                                it.get("username").toString(),
                                (it.get("icon_id") as Long).toInt(),
                                it.id,
                                true
                            )
                            if (!memberList.contains(newMember)) {
                                newMember.isMember = false
                                memberList.add(newMember)
                            }
                        }
                        adapter.updateList()
                        sortingMembersList()
                    }
            }

        spMembers.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    lifecycleScope.launch {
                        save("spinnerPos", position)
                        sortingMembersList()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
        svMembers.imeOptions = EditorInfo.IME_ACTION_DONE
        svMembers.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    adapter.filter.filter(newText)
                    return false
                }
            })

        fabConfirmMembers.setOnClickListener {
            val memberIds = ArrayList<String>()
            memberList.forEach {
                if (it.isMember) memberIds.add(it.friendId)
            }
            intent.putExtra("newMemberData", memberIds)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        lifecycleScope.launch {
            spMembers.setSelection(read("spinnerPos"))
        }
    }

    fun sortingMembersList() {
        lifecycleScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser!!.uid
            val position = read("spinnerPos")
            when (position) { //0: A-Z; 1: Z-A
                0 -> {
                    memberList.sortBy { it.name.toLowerCase() }
                }
                1 -> {
                    memberList.sortByDescending { it.name.toLowerCase() }
                }
            }
            memberList.sortByDescending { it.isMember }
            memberList.sortByDescending { it.friendId == uid }
            adapter.updateSpinnerPos(position)
            adapter.notifyDataSetChanged()
        }
    }

    private suspend fun save(key: String, value: Int) {
        val dataStoreKey = intPreferencesKey(key)
        dataStore.edit { settings ->
            settings[dataStoreKey] = value
        }
    }

    private suspend fun read(key: String): Int {
        val dataStoreKey = intPreferencesKey(key)
        val preferences = dataStore.data.first()
        return preferences[dataStoreKey] ?: 0
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}