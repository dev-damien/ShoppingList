package de.codingkeks.shoppinglist.utility

import de.codingkeks.shoppinglist.R

class ImageMapper {

    companion object{
        val imagesList = arrayListOf(
            R.drawable.ic_menu_shoppinglists,
            R.drawable.ic_pets,
            R.drawable.ic_sick,
            R.drawable.ic_drink,
            R.drawable.ic_baseline_icecream_24,
            R.drawable.ic_flatware,
            R.drawable.ic_fastfood,
            R.drawable.ic_family,
            R.drawable.ic_android,
            R.drawable.ic_duo,
            R.drawable.ic_beer,
            R.drawable.ic_outdoor_grill,
            R.drawable.ic_school,
            R.drawable.ic_esports,
            R.drawable.ic_travel,
            R.drawable.ic_cake,
            R.drawable.ic_clean,
            R.drawable.ic_bike
        )

        val imagesUser = arrayListOf(
            R.drawable.ic_account_image,
            R.drawable.ic_pregnant_woman,
            R.drawable.ic_male,
            R.drawable.ic_female,
            R.drawable.ic_transgender,
            R.drawable.ic_accessible,
            R.drawable.ic_catching_pokemon,
            R.drawable.ic_child,
            R.drawable.ic_theater_mask,
            R.drawable.ic_support_agent,
            R.drawable.ic_hiking,
            R.drawable.ic_neutral,
            R.drawable.ic_very_satisfied,
            R.drawable.ic_very_dissatisfied,
            R.drawable.ic_self_improvement,
            R.drawable.ic_elderly,
            R.drawable.ic_flutter_dash,
            R.drawable.ic_nature_people
        )
    }

    //Map which maps all resource ids to database ids
    private val app2database: HashMap<Int, Int> = hashMapOf(
        //user icons
        R.drawable.ic_account_image to 0,
        R.drawable.ic_pregnant_woman to 1,
        R.drawable.ic_male to 2,
        R.drawable.ic_female to 3,
        R.drawable.ic_transgender to 4,
        R.drawable.ic_accessible to 5,
        R.drawable.ic_catching_pokemon to 6,
        R.drawable.ic_child to 7,
        R.drawable.ic_theater_mask to 8,
        R.drawable.ic_support_agent to 9,
        R.drawable.ic_hiking to 10,
        R.drawable.ic_neutral to 11,
        R.drawable.ic_very_satisfied to 12,
        R.drawable.ic_very_dissatisfied to 13,
        R.drawable.ic_self_improvement to 14,
        R.drawable.ic_elderly to 15,
        R.drawable.ic_flutter_dash to 16,
        R.drawable.ic_nature_people to 17,

        //list icons
        R.drawable.ic_pets to 100,
        R.drawable.ic_sick to 101,
        R.drawable.ic_drink to 102,
        R.drawable.ic_baseline_icecream_24 to 103,
        R.drawable.ic_flatware to 104,
        R.drawable.ic_fastfood to 105,
        R.drawable.ic_family to 106,
        R.drawable.ic_duo to 107,
        R.drawable.ic_beer to 108,
        R.drawable.ic_outdoor_grill to 109,
        R.drawable.ic_school to 110,
        R.drawable.ic_esports to 111,
        R.drawable.ic_travel to 112,
        R.drawable.ic_cake to 113,
        R.drawable.ic_clean to 114,
        R.drawable.ic_bike to 115,
        R.drawable.ic_menu_shoppinglists to 116,

        //private user icon
        R.drawable.ic_wolly to 201,
        R.drawable.ic_android to 202
    )

    //reversed Map
    private var database2App: HashMap<Int, Int> = hashMapOf()

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