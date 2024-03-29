package de.codingkeks.shoppinglist.utility

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import de.codingkeks.shoppinglist.R
import kotlinx.coroutines.launch

open class ThemeSetter : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            when (DataStoreUtility.readInt("theme", this@ThemeSetter)) { //0: Light, 1: Dark
                0 -> {
                    setTheme(R.style.AppTheme)
                    window.statusBarColor =
                        ContextCompat.getColor(this@ThemeSetter, R.color.colorPrimaryDark)
                }
                1 -> {
                    setTheme(R.style.AppThemeDark)
                    window.statusBarColor =
                        ContextCompat.getColor(this@ThemeSetter, R.color.pure_black)
                }
            }
        }
    }

}