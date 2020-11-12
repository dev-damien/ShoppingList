package de.codingkeks.shoppinglist.ui.shoppinglists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import de.codingkeks.shoppinglist.R

class ShoppingListsFragment : Fragment() {

    private lateinit var shoppingListsViewModel: ShoppingListsViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        shoppingListsViewModel =
                ViewModelProviders.of(this).get(ShoppingListsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_shoppinglists, container, false)
        val textView: TextView = root.findViewById(R.id.tv_userName)
        shoppingListsViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}