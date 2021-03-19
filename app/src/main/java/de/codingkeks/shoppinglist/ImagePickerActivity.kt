package de.codingkeks.shoppinglist

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import de.codingkeks.shoppinglist.recyclerview.images.Image
import de.codingkeks.shoppinglist.recyclerview.images.ImageAdapter
import de.codingkeks.shoppinglist.utility.ThemeSetter
import kotlinx.android.synthetic.main.activity_image_picker.*

private const val TAG = MainActivity.TAG

class ImagePickerActivity : ThemeSetter() {

    private lateinit var images: MutableList<Image>
    private val numberOfColumns = 4 //number of columns for the image picker selection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_picker)

        title = getString(R.string.image_picker_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val selected = intent.getIntExtra("selected", R.drawable.ic_no_image_found)

        if (intent.hasExtra("images")) {
            Log.d(TAG, "intent has an extra")
            val imagesInt = intent.getIntegerArrayListExtra("images")
            if (imagesInt != null && imagesInt.isNotEmpty()) {
                Log.d(TAG, "not null and not empty")
                images = mutableListOf()
                imagesInt.forEach {
                    images.add(
                        Image(
                            it,
                            it == selected //.toInt bei it?
                        )
                    )
                }
            }
        } else {
            Toast.makeText(this, "An Exception occurred", Toast.LENGTH_LONG).show()
        }

        buSelect.setOnClickListener {
            var image = R.drawable.ic_menu_shoppinglists
            images.forEach {
                if (it.isSelected) {
                    image = it.src
                }
            }
            intent.putExtra("image", image)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        rvImages.adapter = ImageAdapter(images)
        Log.d(TAG, "adapter set")
        rvImages.layoutManager = GridLayoutManager(this, numberOfColumns)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}