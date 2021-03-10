package de.codingkeks.shoppinglist

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import de.codingkeks.shoppinglist.ui.shoppinglists.items.FragmentPagerAdapterItems
import de.codingkeks.shoppinglist.ui.shoppinglists.items.ItemsBoughtFragment
import de.codingkeks.shoppinglist.ui.shoppinglists.items.ItemsFragment

class ItemsActivity : AppCompatActivity() {

    lateinit var viewPager: ViewPager
    lateinit var tabLayout: TabLayout

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.popup_list_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.action_leave -> {
                AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogTheme))
                    .setTitle(R.string.leave_group)
                    .setMessage(R.string.leave_list)
                    .setPositiveButton(R.string.popup_leave) { _, _ ->
                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        val listId = intent.getStringExtra("listId")
                        FirebaseFirestore.getInstance().document("lists/$listId")
                            .get().addOnSuccessListener { dSnap ->
                                val list = dSnap.get("members") as ArrayList<*>
                                if (list.size > 1) {
                                    dSnap.reference.update("members", FieldValue.arrayRemove(uid))
                                        .addOnSuccessListener {
                                        if (intent.getBooleanExtra("isFav", false)) {
                                            FirebaseFirestore.getInstance().document("users/$uid")
                                                .get().addOnSuccessListener {
                                                    it.reference.update(
                                                        "favorites",
                                                        FieldValue.arrayRemove(listId)
                                                    )
                                                }
                                        }
                                    }
                                    onBackPressed()
                                } else {
                                    //TODO delete Items Collection -> delete Document
                                    deleteItemsCollection()
                                    deleteListDocument()
                                }
                        }
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
                true
            }
            R.id.action_delete -> {
                AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogTheme))
                    .setTitle(R.string.delete_group)
                    .setMessage(R.string.delete_list)
                    .setPositiveButton(R.string.emailVerificationDelete) { _, _ ->
                        //TODO delete Items Collection -> delete Document
                        deleteItemsCollection()
                        deleteListDocument()
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_items)

        title = ""
        FirebaseFirestore.getInstance()
            .document("lists/${intent.getStringExtra("listId")}")
            .get().addOnSuccessListener {
                title = it.get("name").toString()
            }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tabLayout = findViewById(R.id.tabLayoutItems)
        viewPager = findViewById(R.id.viewPagerItems)

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
    }

    private fun setUpViewPager(viewPager: ViewPager) {
        var adapter: FragmentPagerAdapterItems = FragmentPagerAdapterItems(supportFragmentManager)

        adapter.addFragment(ItemsFragment(), getString(R.string.items_tab_title))
        adapter.addFragment(ItemsBoughtFragment(), getString(R.string.items_bought_tab_title))

        viewPager.adapter = adapter
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun deleteItemsCollection() {
        //TODO
    }

    private fun deleteListDocument() {
        //TODO
    }
}