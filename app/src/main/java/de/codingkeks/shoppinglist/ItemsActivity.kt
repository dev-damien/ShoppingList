package de.codingkeks.shoppinglist

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.viewpager.widget.ViewPager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import de.codingkeks.shoppinglist.ui.shoppinglists.items.FragmentPagerAdapterItems
import de.codingkeks.shoppinglist.ui.shoppinglists.items.ItemsBoughtFragment
import de.codingkeks.shoppinglist.ui.shoppinglists.items.ItemsFragment
import de.codingkeks.shoppinglist.utility.ImageMapper
import de.codingkeks.shoppinglist.utility.ThemeSetter
import kotlinx.android.synthetic.main.activity_add_new_list.*
import kotlinx.android.synthetic.main.app_bar_main.*

private const val RC_MEMBER = 99
private const val RC_CHANGE_ICON = 101

class ItemsActivity : ThemeSetter() {
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var registration: ListenerRegistration
    private val mapper = ImageMapper()

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.popup_list_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.action_member -> {
                val intent = Intent(this, MemberManagementActivity::class.java).apply {
                    putExtra("listId", intent.getStringExtra("listId"))
                }
                startActivityForResult(intent, RC_MEMBER)
                true
            }
            R.id.action_leave -> {
                AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogTheme))
                    .setTitle(R.string.leave_group)
                    .setMessage(R.string.leave_list)
                    .setPositiveButton(R.string.popup_leave) { _, _ ->
                        registration.remove()
                        val uid = FirebaseAuth.getInstance().currentUser!!.uid
                        val listId = intent.getStringExtra("listId") ?: return@setPositiveButton
                        FirebaseFirestore.getInstance().document("lists/$listId")
                            .get().addOnSuccessListener { dSnap ->
                                val list = dSnap.get("members") as ArrayList<*>
                                if (list.size > 1) {
                                    dSnap.reference.update("members", FieldValue.arrayRemove(uid))
                                        .addOnSuccessListener {
                                            if (intent.getBooleanExtra("isFav", false)) {
                                                FirebaseFirestore.getInstance()
                                                    .document("users/$uid")
                                                    .get().addOnSuccessListener {
                                                        it.reference.update(
                                                            "favorites",
                                                            FieldValue.arrayRemove(listId)
                                                        )
                                                    }
                                            }
                                        }
                                    onBackPressed()
                                } else {
                                    registration.remove()
                                    deleteItemsCollection(listId)
                                    onBackPressed()
                                }
                            }
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
                true
            }
            R.id.action_delete -> {
                AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogTheme))
                    .setTitle(R.string.delete_group)
                    .setMessage(R.string.delete_list)
                    .setPositiveButton(R.string.emailVerificationDelete) { _, _ ->
                        registration.remove()
                        val listId = intent.getStringExtra("listId") ?: return@setPositiveButton
                        deleteItemsCollection(listId)
                        onBackPressed()
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
                true
            }
            R.id.action_edit -> {
                val alertBuilder = MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme2)
                alertBuilder.setTitle(getString(R.string.edit_name))
                val alertLayout = getEditTextLayout(this, intent?.getStringExtra("listName").toString())
                alertBuilder.setView(alertLayout)

                val textInputLayout = alertLayout.findViewWithTag<TextInputLayout>("textInputLayoutTag")
                val textInputEditText = alertLayout.findViewWithTag<TextInputEditText>("textInputEditTextTag")

                alertBuilder.setPositiveButton(R.string.popup_edit) { _, _ ->
                    FirebaseFirestore.getInstance().document("lists/${intent.getStringExtra("listId")}")
                        .get().addOnSuccessListener {
                            it.reference.update("name", textInputEditText.text.toString().trim())
                            title = textInputEditText.text.toString().trim()
                            intent.putExtra("listName", textInputEditText.text.toString().trim())
                        }
                }
                alertBuilder.setNeutralButton(R.string.cancel, null)
                val dialog = alertBuilder.create()
                dialog.show()

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true

                textInputEditText.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(p0: Editable?) {
                    }

                    override fun beforeTextChanged(
                        p0: CharSequence?, p1: Int,
                        p2: Int, p3: Int
                    ) {
                    }

                    override fun onTextChanged(
                        p0: CharSequence?, p1: Int,
                        p2: Int, p3: Int
                    ) {
                        if (p0.isNullOrBlank()) {
                            textInputLayout.error = getString(R.string.enter_name)
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                .isEnabled = false
                        } else {
                            textInputLayout.error = ""
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                .isEnabled = true
                        }
                    }
                })
                true
            }
            //TODO selected item passen
            R.id.action_icon -> {
                Intent(this, ImagePickerActivity::class.java).also {
                    it.putExtra("images", ImageMapper.imagesList)
                    startActivityForResult(it, RC_CHANGE_ICON)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_items)

        title = ""
        FirebaseFirestore.getInstance()
            .document("lists/${intent.getStringExtra("listId")}")
            .get().addOnSuccessListener {
                title = it.get("name").toString()
            }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tabLayout = findViewById(R.id.tabLayoutItems)
        viewPager = findViewById(R.id.viewPagerItems)

        setUpViewPager(viewPager)
        tabLayout.setupWithViewPager(viewPager)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        registration =
            FirebaseFirestore.getInstance().document("lists/${intent.getStringExtra("listId")}")
                .addSnapshotListener { dSnap, _ ->
                    val uid = FirebaseAuth.getInstance().currentUser!!.uid
                    if (dSnap != null) {
                        if (!(dSnap.get("members") as ArrayList<*>).contains(uid)) onBackPressed()
                    } else onBackPressed()
                }
    }

    override fun onStop() {
        super.onStop()
        registration.remove()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_MEMBER) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    val memberList = data!!.getStringArrayListExtra("newMemberData")!!
                    FirebaseFirestore.getInstance()
                        .document("lists/${intent.getStringExtra("listId")}")
                        .get().addOnSuccessListener { dSnapList ->
                            var oldMembers = dSnapList.get("members") as ArrayList<*>
                            oldMembers = oldMembers.minus(memberList) as ArrayList<*>
                            if (oldMembers.isNotEmpty()) {
                                FirebaseFirestore.getInstance().collection("users")
                                    .whereIn(FieldPath.documentId(), oldMembers)
                                    .whereArrayContains(
                                        "favorites",
                                        intent.getStringExtra("listId")!!
                                    )
                                    .get().addOnSuccessListener { qSnapUsers ->
                                        qSnapUsers.forEach {
                                            it.reference.update(
                                                "favorites",
                                                FieldValue.arrayRemove(intent.getStringExtra("listId"))
                                            )
                                        }
                                    }
                            }
                            dSnapList.reference.update("members", memberList)
                        }
                } catch (ex: Exception) {
                    Log.d(MainActivity.TAG, "Error that really should not happen!")
                }
            } else if (resultCode == 12) {
                onBackPressed()
            }
        }
        if (requestCode == RC_CHANGE_ICON) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                if (data.hasExtra("image")) {
                    val selectedImage = data.getIntExtra("image", -1)
                    FirebaseFirestore.getInstance()
                        .document("lists/${intent.getStringExtra("listId")}")
                        .update("icon_id", mapper.upload(selectedImage))
                        .addOnSuccessListener {
                            invalidateOptionsMenu()
                        }
                }
            }
        }
    }

    private fun setUpViewPager(viewPager: ViewPager) {
        val adapter = FragmentPagerAdapterItems(supportFragmentManager)

        adapter.addFragment(ItemsFragment(), getString(R.string.items_tab_title))
        adapter.addFragment(ItemsBoughtFragment(), getString(R.string.items_bought_tab_title))

        viewPager.adapter = adapter
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun deleteItemsCollection(listId: String) {
        FirebaseFirestore.getInstance().collection("lists/$listId/items")
            .get().addOnSuccessListener { qSnap ->
                qSnap.forEach { qdSnap ->
                    qdSnap.reference.delete()
                }
            }.addOnSuccessListener {
                deleteListDocument(listId)
            }
    }

    private fun deleteListDocument(listId: String) {
        FirebaseFirestore.getInstance().collection("users")
            .whereArrayContains("favorites", listId)
            .get().addOnSuccessListener { qSnap ->
                qSnap.forEach { qdSnap ->
                    qdSnap.reference.update(
                        "favorites",
                        FieldValue.arrayRemove(listId)
                    )
                }
            }

        FirebaseFirestore.getInstance().document("lists/$listId").delete()
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        FirebaseFirestore.getInstance()
            .document("lists/${intent.getStringExtra("listId")}")
            .get().addOnSuccessListener {
                val item = menu?.findItem(R.id.action_icon)
                item?.setIcon(mapper.download((it.get("icon_id") as Long).toInt()))
            }
        return super.onPrepareOptionsMenu(menu)
    }

    @SuppressLint("SetTextI18n")
    private fun getEditTextLayout(context: Context, listName: String): ConstraintLayout {
        val constraintLayout = ConstraintLayout(context)
        val layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        constraintLayout.layoutParams = layoutParams
        constraintLayout.id = View.generateViewId()

        val textInputLayout = TextInputLayout(context)
        textInputLayout.boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
        layoutParams.setMargins(
            32.toDp(context),
            8.toDp(context),
            32.toDp(context),
            8.toDp(context)
        )
        textInputLayout.layoutParams = layoutParams
        textInputLayout.hint = getString(R.string.etAddNewListEnterNameHintString)
        textInputLayout.id = View.generateViewId()
        textInputLayout.tag = "textInputLayoutTag"

        val textInputEditText = TextInputEditText(context)
        textInputEditText.id = View.generateViewId()
        textInputEditText.tag = "textInputEditTextTag"
        textInputEditText.setText(listName)
        textInputEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        textInputEditText.filters = arrayOf(*textInputEditText.filters, InputFilter.LengthFilter(30))
        textInputLayout.addView(textInputEditText, 0)

        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)

        constraintLayout.addView(textInputLayout)
        return constraintLayout
    }

    private fun Int.toDp(context: Context): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics
    ).toInt()
}