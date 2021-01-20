package de.codingkeks.shoppinglist

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_add_new_list.*

class AddNewListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_list)

        buAddNewList_Done.setOnClickListener {
            if(etAddNewList_nameInput.text.toString().trim() != ""){
                intent.putExtra("name", etAddNewList_nameInput.text.toString().trim())
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
            else{
                Toast.makeText(this, "You have to enter a name", Toast.LENGTH_SHORT).show()
            }
        }
    }



}