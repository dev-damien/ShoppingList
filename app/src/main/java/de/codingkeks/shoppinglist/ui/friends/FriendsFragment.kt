package de.codingkeks.shoppinglist.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import de.codingkeks.shoppinglist.R

class FriendsFragment : Fragment() {

    private lateinit var friendsViewModel: FriendsViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        friendsViewModel =
                ViewModelProviders.of(this).get(FriendsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_friends, container, false)
        val textView: TextView = root.findViewById(R.id.text_slideshow)
        friendsViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}