package de.codingkeks.shoppinglist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import de.codingkeks.shoppinglist.ui.shoppinglists.items.FragmentPagerAdapterItems
import de.codingkeks.shoppinglist.ui.shoppinglists.items.ItemsBoughtFragment
import de.codingkeks.shoppinglist.ui.shoppinglists.items.ItemsFragment

class ItemsActivity : AppCompatActivity() {

    lateinit var viewPager: ViewPager
    lateinit var tabLayout: TabLayout

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

        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
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
}