package de.codingkeks.shoppinglist.ui.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import de.codingkeks.shoppinglist.MainActivity
import de.codingkeks.shoppinglist.R
import de.codingkeks.shoppinglist.utility.DataStoreUtility
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onStart() {
        super.onStart()

        var isSpinnerInitial = true

        spThemes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                Log.d(MainActivity.TAG, "isSpinnerInitial: $isSpinnerInitial")
                if (isSpinnerInitial) {
                    isSpinnerInitial = false
                    return
                }
                lifecycleScope.launch {
                    DataStoreUtility.saveInt("theme", position, requireContext())
                    Intent(requireContext(), MainActivity::class.java).let {
                        startActivity(it)
                        activity?.finishAffinity()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        lifecycleScope.launch {
            isSpinnerInitial = true
            spThemes.setSelection(DataStoreUtility.readInt("theme", requireContext()))
        }
    }

}