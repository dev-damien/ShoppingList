package de.codingkeks.shoppinglist

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_image_picker.*

class ImagePickerActivity : AppCompatActivity() {

    private val TAG = MainActivity.TAG

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "pick image onCreate Start")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_picker)
        if (intent.hasExtra("images")){
            Log.d(TAG, "intent has an extra")
            val images = intent.getIntegerArrayListExtra("images")
            if (images != null && images.isNotEmpty()) {
                Log.d(TAG, "not null and not empty")
                var counter = 0
                images.forEach {
                    Log.d(TAG, it.toString())
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
}