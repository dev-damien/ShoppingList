package de.codingkeks.shoppinglist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
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

        ivAddNewList_editIcon.setOnClickListener {
            Log.d(MainActivity.TAG, "Image view to change image has been clicked")
            //TODO pass the right images as an arrayList to the ImagePickerActivity;
            //just a testing arrayList with random images
            val images = arrayListOf(R.drawable.common_full_open_on_phone,
                R.drawable.common_google_signin_btn_icon_dark,
                R.drawable.common_google_signin_btn_icon_dark_focused,
                R.drawable.ic_account_image,
                R.drawable.ic_friends_person_add,
                R.drawable.ic_launcher_background,
                R.drawable.ic_menu_friends,
                R.drawable.ic_friends_star_border,
                R.drawable.ic_menu_account)
            Intent(this, ImagePickerActivity::class.java).also {
                it.putExtra("images", images)
                startActivityForResult(it, 1)
            }
        }
    }



}