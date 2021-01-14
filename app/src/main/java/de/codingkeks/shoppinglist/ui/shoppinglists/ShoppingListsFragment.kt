package de.codingkeks.shoppinglist.ui.shoppinglists

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import de.codingkeks.shoppinglist.MainActivity
import de.codingkeks.shoppinglist.R
import de.codingkeks.shoppinglist.recyclerview.shoppinglists.ListAdapter
import de.codingkeks.shoppinglist.recyclerview.shoppinglists.ShoppingList
import kotlinx.android.synthetic.main.fragment_shoppinglists.*

class ShoppingListsFragment : Fragment() {

    private lateinit var shoppingListsViewModel: ShoppingListsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
        super.onStart()

        var shoppingList = mutableListOf(
            ShoppingList("Montag", R.drawable.ic_menu_shoppinglists, true),
            ShoppingList("WG", R.drawable.ic_menu_shoppinglists, false),
            ShoppingList("Familie", R.drawable.ic_menu_shoppinglists, true),
            ShoppingList("Deutschland", R.drawable.ic_menu_shoppinglists, false),
            ShoppingList("Schweiz", R.drawable.ic_menu_shoppinglists, true),
            ShoppingList("Polen", R.drawable.ic_menu_shoppinglists, false),
            ShoppingList("Russland", R.drawable.ic_menu_shoppinglists, true),
            ShoppingList("Dänemark", R.drawable.ic_menu_shoppinglists, true),
            ShoppingList("Lol", R.drawable.ic_menu_shoppinglists, true),
            ShoppingList("Gott", R.drawable.ic_menu_shoppinglists, true),
            ShoppingList("Allah", R.drawable.ic_menu_shoppinglists, false)
        )

        val adapter = ListAdapter(shoppingList)
        rvLists.adapter = adapter
        rvLists.layoutManager = LinearLayoutManager(requireContext())

        //add a new list by clicking on the fab
        var counter = 0
        fabAddNewList.setOnClickListener {
            shoppingList.add(ShoppingList("NewList$counter", R.drawable.ic_menu_home, true))
            counter++
            adapter.notifyDataSetChanged()
        }
    }
}