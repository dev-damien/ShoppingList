package de.codingkeks.shoppinglist.ui.shoppinglists.items

import android.annotation.SuppressLint
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import de.codingkeks.shoppinglist.MainActivity
import de.codingkeks.shoppinglist.R
import de.codingkeks.shoppinglist.recyclerview.items.Item
import de.codingkeks.shoppinglist.recyclerview.items.ItemBoughtAdapter
import de.codingkeks.shoppinglist.utility.DataStoreUtility
import kotlinx.android.synthetic.main.fragment_items_bought.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ItemsBoughtFragment : Fragment() {

    private var items: MutableList<Item> = mutableListOf()
    private lateinit var adapter: ItemBoughtAdapter
    private lateinit var registration: ListenerRegistration

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_items_bought, container, false)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onStart() {
        super.onStart()
        svItemsBought.clearFocus()
        svItemsBought.setQuery("", false)
        val listId = activity?.intent?.getStringExtra("listId").toString()

        adapter = ItemBoughtAdapter(items, spItemsBought.selectedItemPosition, listId)
        rvItemsBought.adapter = adapter
        rvItemsBought.layoutManager = LinearLayoutManager(requireContext())
        if (rvItemsBought.itemDecorationCount <= 0) {
            rvItemsBought.addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
        }

        val colRefItems = FirebaseFirestore.getInstance().collection("lists/${listId}/items")
        registration = colRefItems.addSnapshotListener { qSnap, _ ->
            items.clear()
            qSnap?.documents?.forEach { dSnap ->
                if (dSnap.get("isBought") as Boolean) {
                    items.add(
                        Item(
                            dSnap.get("name").toString(),
                            (dSnap.get("quantity") as Long).toInt(),
                            dSnap.get("addedBy").toString(),
                            dSnap.get("addedTime").toString(),
                            true,
                            dSnap.id,
                            dSnap.get("boughtBy").toString(),
                            dSnap.get("boughtAt").toString()
                        )
                    )
                }
            }
            adapter.updateList()
            sortingItems()
        }

        spItemsBought.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                lifecycleScope.launch {
                    DataStoreUtility.saveInt("itemBoughtSpinnerPos", position, requireContext())
                    sortingItems()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        svItemsBought.imeOptions = EditorInfo.IME_ACTION_DONE
        svItemsBought.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })

        lifecycleScope.launch {
            spItemsBought.setSelection(DataStoreUtility.readInt("itemBoughtSpinnerPos", requireContext()))
        }
    }

    override fun onStop() {
        super.onStop()
        registration.remove()
    }

    @SuppressLint("SimpleDateFormat")
    fun sortingItems() {
        lifecycleScope.launch {
            val position = DataStoreUtility.readInt("itemBoughtSpinnerPos", requireContext())
            when (position) { //position 0: Latest; 1: A-Z; 2: Z-A
                0 -> {
                    items.sortBy { it.name.toLowerCase(Locale.ROOT) }
                    try {
                        items.sortByDescending { SimpleDateFormat("dd.MM.yyyy HH:mm").parse(it.boughtAt) }
                    } catch (ex: Exception) {
                        Log.d(MainActivity.TAG, "Cant parse because values are empty")
                    }
                }
                1 -> {
                    items.sortBy { it.name.toLowerCase(Locale.ROOT) }
                    try {
                        items.sortBy { SimpleDateFormat("dd.MM.yyyy HH:mm").parse(it.boughtAt) }
                    } catch (ex: Exception) {
                        Log.d(MainActivity.TAG, "Cant parse because values are empty")
                    }
                }
                2 -> {
                    items.sortBy { it.name.toLowerCase(Locale.ROOT) }
                }
                3 -> {
                    items.sortByDescending { it.name.toLowerCase(Locale.ROOT) }
                }
            }
            adapter.notifyDataSetChanged()
            adapter.updateSpinnerPos(position)
        }
    }
}