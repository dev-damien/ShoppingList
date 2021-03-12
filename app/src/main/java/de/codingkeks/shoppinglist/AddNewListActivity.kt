package de.codingkeks.shoppinglist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_add_new_list.*

private const val RC_IMAGEPICKER = 1
private const val RC_MEMBER = 99

class AddNewListActivity : AppCompatActivity() {

    private var selectedImage: Int = R.drawable.ic_menu_shoppinglists
    private var isFav = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_list)

        title = getString(R.string.create_list)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        etAddNewList_nameInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES

        buAddNewList_Done.setOnClickListener {
            if (etAddNewList_nameInput.text.toString().trim() != "") {
                intent.putExtra("name", etAddNewList_nameInput.text.toString().trim())
                intent.putExtra("icon", selectedImage)
                intent.putExtra("isFav", isFav)
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else {
                Toast.makeText(this, R.string.enter_name, Toast.LENGTH_SHORT).show()
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
            val images = arrayListOf(
                R.drawable.ic_menu_settings,
                R.drawable.ic_menu_home,
                R.drawable.common_google_signin_btn_icon_dark_focused,
                R.drawable.ic_account_image,
                R.drawable.ic_friends_person_add,
                R.drawable.ic_launcher_background,
                R.drawable.ic_menu_friends,
                R.drawable.ic_friends_star_border,
                R.drawable.ic_menu_account
            )
            Intent(this, ImagePickerActivity::class.java).also {
                it.putExtra("images", images)
                startActivityForResult(it, RC_IMAGEPICKER)
            }
        }

        buAddMember.setOnClickListener {
            val intent = Intent(this, MemberManagementActivity::class.java)
            startActivityForResult(intent, RC_MEMBER)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(MainActivity.TAG, "AddNewListActivity_onActivityResult()_Start")
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_IMAGEPICKER) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                if (data.hasExtra("image")){
                    selectedImage = data.getIntExtra("image", -1)
                    ivAddNewList_icon.setImageResource(selectedImage)
                }
            }
        }
        else if (requestCode == RC_MEMBER && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                intent.putExtra("memberData", data.getStringArrayListExtra("newMemberData"))
            }
        }
        Log.d(MainActivity.TAG, "AddNewListActivity_onActivityResult()_End")
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}