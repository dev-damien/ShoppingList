package de.codingkeks.shoppinglist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import de.codingkeks.shoppinglist.recyclerview.items.Item
import de.codingkeks.shoppinglist.recyclerview.items.ItemAdapter
import de.codingkeks.shoppinglist.recyclerview.shoppinglists.ListAdapter
import de.codingkeks.shoppinglist.recyclerview.shoppinglists.ShoppingList
import kotlinx.android.synthetic.main.activity_items.*
import kotlinx.android.synthetic.main.fragment_shoppinglists.*

class ItemsActivity : AppCompatActivity() {

    private val RC_ADD_NEW_ITEM = 0
    private var items: MutableList<Item> = mutableListOf()
    private lateinit var adapter: ItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_items)

        adapter = ItemAdapter(items, spItems.selectedItemPosition)
        rvItems.adapter = adapter
        rvItems.layoutManager = LinearLayoutManager(this)
        rvItems.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        var listId = intent.getStringExtra("listId")
        val colRefItems = FirebaseFirestore.getInstance().collection("lists/${listId}/items")
        colRefItems.get().addOnSuccessListener { qSnap ->
            qSnap.documents.forEach { dSnap ->
                if (!(dSnap.get("isBought") as Boolean)) {
                    items.add(
                        Item(
                            dSnap.get("name").toString(),
                            (dSnap.get("quantity") as Long).toInt(),
                            dSnap.get("addedBy").toString(),
                            dSnap.get("addedTime").toString(),
                            false
                        )
                    )
                }
                sortingItems(spItems.selectedItemPosition)
                adapter.updateList()
                adapter.notifyDataSetChanged()
            }
        }

        spItems.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                sortingItems(position)
                adapter.notifyDataSetChanged()
                adapter.updateSpinnerPos(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        svItems.imeOptions = EditorInfo.IME_ACTION_DONE
        svItems.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })
    }

    fun sortingItems(position: Int) {
        when (position) { //position 0: Latest; 1: A-Z; 2: Z-A
            0 -> {
                items.sortBy { it.name }
                items.sortBy { it.addedTime }
            }
            1 -> {
                items.sortBy { it.name }
            }
            2 -> {
                items.sortByDescending { it.name }
            }
        }
    }
}