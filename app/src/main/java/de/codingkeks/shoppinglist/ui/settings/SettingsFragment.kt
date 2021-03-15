package de.codingkeks.shoppinglist.ui.settings

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.codingkeks.shoppinglist.MainActivity
import de.codingkeks.shoppinglist.R
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(MainActivity.TAG, "SettingsFragment()_onCreateView()_Start")
        val root = inflater.inflate(R.layout.fragment_settings, container, false)
        Log.d(MainActivity.TAG, "SettingsFragment()_onCreateView()_End")
        return root
    }

    override fun onStart() {
        super.onStart()

        button.setOnClickListener {
            activity?.setTheme(R.style.AppThemeDark)
        }
    }

}