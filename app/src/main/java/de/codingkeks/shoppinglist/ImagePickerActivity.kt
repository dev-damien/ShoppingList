package de.codingkeks.shoppinglist

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import de.codingkeks.shoppinglist.recyclerview.images.Image
import de.codingkeks.shoppinglist.recyclerview.images.ImageAdapter
import de.codingkeks.shoppinglist.ui.imagePicker.DynamicGridLayoutManager
import kotlinx.android.synthetic.main.activity_image_picker.*

class ImagePickerActivity : AppCompatActivity() {

    private val TAG = MainActivity.TAG
    private lateinit var images: MutableList<Image>

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "pick image onCreate Start")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_picker)

        if (intent.hasExtra("images")){
            Log.d(TAG, "intent has an extra")
            val imagesInt = intent.getIntegerArrayListExtra("images")
            if (imagesInt != null && imagesInt.isNotEmpty()) {
                Log.d(TAG, "not null and not empty")
                images = mutableListOf()
                imagesInt.forEach {
                    Log.d(TAG, it.toString())
                    images.add(Image(it))
                }
            }
        }
        else{
            Toast.makeText(this, "An Exception occurred", Toast.LENGTH_LONG).show()
        }

        buSelect.setOnClickListener {
            var image = 0 //TODO get right image resource
            intent.putExtra("image", image)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
        Log.d(TAG, "pick image onCreate End")
    }

    override fun onStart() {
        Log.d(TAG, "pick image onStart Start")
        super.onStart()
        rvImages.adapter = ImageAdapter(images)
        Log.d(TAG, "adapter set")
        rvImages.layoutManager = GridLayoutManager(this, 4)
        Log.d(TAG, "pick image Start End")
    }
}