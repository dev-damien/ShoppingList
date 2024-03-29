package de.codingkeks.shoppinglist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.widget.Toast
import de.codingkeks.shoppinglist.utility.ImageMapper
import de.codingkeks.shoppinglist.utility.ThemeSetter
import kotlinx.android.synthetic.main.activity_add_new_list.*

private const val RC_IMAGEPICKER = 1
private const val RC_MEMBER = 99

class AddNewListActivity : ThemeSetter() {

    private val defaultImage = R.drawable.ic_menu_shoppinglists
    private var selectedImage: Int = defaultImage
    private var isFav = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_list)

        title = getString(R.string.create_list)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        etAddNewList_nameInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        etAddNewList_nameInput.filters = arrayOf(*etAddNewList_nameInput.filters, InputFilter.LengthFilter(30))

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
            Intent(this, ImagePickerActivity::class.java).also {
                it.putExtra("images", ImageMapper.imagesList)
                it.putExtra("selected", selectedImage)
                startActivityForResult(it, RC_IMAGEPICKER)
            }
        }

        buAddMember.setOnClickListener {
            Intent(this, MemberManagementActivity::class.java).apply {
                if (intent.hasExtra("memberData"))
                    putExtra("alreadyAddedMember", intent.getStringArrayListExtra("memberData"))
                startActivityForResult(this, RC_MEMBER)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}