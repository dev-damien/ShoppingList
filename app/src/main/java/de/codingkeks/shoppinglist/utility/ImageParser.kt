package de.codingkeks.shoppinglist.utility

import de.codingkeks.shoppinglist.R
import kotlinx.android.synthetic.main.fragment_account.*

class ImageMapping {

    //Map which maps all resource ids to database ids
    private val app2database = mapOf<Int, Int>(
        R.drawable.ic_menu_home to 0
    )

    //reversed Map
    private lateinit var database2App: HashMap<Int, Int>

    constructor(){
        app2database.entries.forEach {
            database2App[it.value] = it.key
        }

    }

    /**
     * @param resourceId the ID of the resource within the app
     * @return the matching resource id in firebase
     */
    fun upload(resourceId: Int): Int {
        return app2database[resourceId] ?: -1
    }

    /**
     * @param firebaseId the id of the resource saved in firebase
     * @return the matching resource id within the app
     */
    fun download(firebaseId: Int): Int {
        return database2App[firebaseId] ?: R.drawable.ic_no_image_found
    }
}