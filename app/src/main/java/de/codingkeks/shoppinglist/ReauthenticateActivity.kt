package de.codingkeks.shoppinglist

import android.app.Activity
import android.os.Bundle
import de.codingkeks.shoppinglist.utility.ThemeSetter
import kotlinx.android.synthetic.main.activity_reauthenticate.*

class ReauthenticateActivity : ThemeSetter()  {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_reauthenticate)

            buOK.setOnClickListener {
                var password: String = editTextPassword.text.toString()

                if (password == "") {
                    password = "*"
                }

                intent.putExtra("password", password)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }

}