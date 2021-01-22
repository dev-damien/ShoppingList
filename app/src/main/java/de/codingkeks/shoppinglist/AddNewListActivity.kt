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

        var isFav = false

        buAddNewList_Done.setOnClickListener {
            if(etAddNewList_nameInput.text.toString().trim() != ""){
                intent.putExtra("name", etAddNewList_nameInput.text.toString().trim())
                intent.putExtra("icon", R.drawable.ic_menu_settings) //TODO get chosen icon
                intent.putExtra("isFav", isFav)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
            else{
                Toast.makeText(this, "You have to enter a name", Toast.LENGTH_SHORT).show()
            }
        }

        ivAddNewList_isFav.setOnClickListener {
            isFav = !isFav
            if (isFav) ivAddNewList_isFav.setImageResource(R.drawable.ic_friends_star)
            else ivAddNewList_isFav.setImageResource(R.drawable.ic_friends_star_border)
        }
    }



}