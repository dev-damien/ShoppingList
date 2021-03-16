package de.codingkeks.shoppinglist

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import de.codingkeks.shoppinglist.recyclerview.members.Member
import de.codingkeks.shoppinglist.recyclerview.members.MemberAdapter
import de.codingkeks.shoppinglist.utility.DataStoreUtility
import de.codingkeks.shoppinglist.utility.ImageMapper
import de.codingkeks.shoppinglist.utility.ThemeSetter
import kotlinx.android.synthetic.main.activity_member_management.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class MemberManagementActivity : ThemeSetter() {

    private var memberList: MutableList<Member> = mutableListOf()
    private lateinit var adapter: MemberAdapter
    private lateinit var registration: ListenerRegistration

    private val mapper = ImageMapper()

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
        if (rvMembers.itemDecorationCount <= 0) {
            rvMembers.addItemDecoration(
                DividerItemDecoration(
                    this,
                    DividerItemDecoration.VERTICAL
                )
            )
        }

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
                                        mapper.download((it.get("icon_id") as Long).toInt()),
                                        it.id,
                                        true
                                    )
                                )
                            }
                            adapter.updateList()
                            sortingMembersList()
                            addingFriends()
                        }
                }
        }

        if (intent.hasExtra("alreadyAddedMember")) {
            val newMemberData =
                intent.getStringArrayListExtra("alreadyAddedMember") as MutableList<String>
            if (newMemberData.isNotEmpty()) {
                FirebaseFirestore.getInstance().collection("users")
                    .whereIn(FieldPath.documentId(), newMemberData)
                    .get().addOnSuccessListener { memberDocs ->
                        memberList.clear()
                        memberDocs.forEach {
                            memberList.add(
                                Member(
                                    it.get("username").toString(),
                                    mapper.download((it.get("icon_id") as Long).toInt()),
                                    it.id,
                                    true
                                )
                            )
                        }
                        adapter.updateList()
                        sortingMembersList()
                        addingFriends()
                    }
            } else addingFriends()
        }

        if (!intent.hasExtra("alreadyAddedMember") && !intent.hasExtra("listId")) {
            addingFriends()
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
                        DataStoreUtility.saveInt("memberSpinnerPos", position, this@MemberManagementActivity)
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
            spMembers.setSelection(DataStoreUtility.readInt("memberSpinnerPos", this@MemberManagementActivity))
        }

        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        registration =
            FirebaseFirestore.getInstance().document("lists/${intent.getStringExtra("listId")}")
                .addSnapshotListener { dSnap, _ ->
                    if (dSnap != null) {
                        var isMember = dSnap.get("members") ?: return@addSnapshotListener
                        isMember = isMember as ArrayList<*>
                        if (!isMember.contains(uid)) {
                            setResult(12)
                            finish()
                        }
                    }
                }
    }

    override fun onStop() {
        super.onStop()
        registration.remove()
    }

    fun sortingMembersList() {
        lifecycleScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser!!.uid
            val position = DataStoreUtility.readInt("memberSpinnerPos", this@MemberManagementActivity)
            when (position) { //0: A-Z; 1: Z-A
                0 -> {
                    memberList.sortBy { it.name.toLowerCase(Locale.ROOT) }
                }
                1 -> {
                    memberList.sortByDescending { it.name.toLowerCase(Locale.ROOT) }
                }
            }
            memberList.sortByDescending { it.isMember }
            memberList.sortByDescending { it.friendId == uid }
            adapter.updateSpinnerPos(position)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun addingFriends() {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        FirebaseFirestore.getInstance().document("users/$uid")
            .get().addOnSuccessListener { dSnapFriends ->
                val friendIds = dSnapFriends.get("friends") as ArrayList<*>
                if (friendIds.isEmpty()) return@addOnSuccessListener
                FirebaseFirestore.getInstance().collection("users")
                    .whereIn(FieldPath.documentId(), friendIds)
                    .get().addOnSuccessListener { friendsDocs ->
                        friendsDocs.forEach {
                            val newMember = Member(
                                it.get("username").toString(),
                                mapper.download((it.get("icon_id") as Long).toInt()),
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
    }
}