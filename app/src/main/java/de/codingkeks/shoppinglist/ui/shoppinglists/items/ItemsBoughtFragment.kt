package de.codingkeks.shoppinglist.ui.shoppinglists.items

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import de.codingkeks.shoppinglist.R
import de.codingkeks.shoppinglist.recyclerview.items.Item
import de.codingkeks.shoppinglist.recyclerview.items.ItemAdapter
import kotlinx.android.synthetic.main.fragment_items_bought.*

class ItemsBoughtFragment : Fragment() {

    private var items: MutableList<Item> = mutableListOf()
    private lateinit var adapter: ItemAdapter
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
        val listId = activity?.intent?.getStringExtra("listId").toString()

        adapter = ItemAdapter(items, spItemsBought.selectedItemPosition, listId)
        rvItemsBought.adapter = adapter
        rvItemsBought.layoutManager = LinearLayoutManager(requireContext())
        rvItemsBought.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

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
                            "",
                            ""
                        )
                    )
                }
            }
            sortingItems(spItemsBought.selectedItemPosition)
            adapter.updateList()
            adapter.notifyDataSetChanged()
        }

        spItemsBought.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
    }

    override fun onStop() {
        super.onStop()
        registration.remove()
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