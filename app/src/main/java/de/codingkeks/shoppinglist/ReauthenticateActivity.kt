package de.codingkeks.shoppinglist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_reauthenticate.*

class ReauthenticateActivity : AppCompatActivity()  {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_reauthenticate)

            buOK.setOnClickListener {
                var password: String = editTextPassword.text.toString()

                if (password == "" || password == null) {
                    password = "*"
                }

                val intent = Intent()
                intent.putExtra("password", password)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }

}