package de.codingkeks.shoppinglist.ui.shoppinglists.items

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.NumberPicker
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.codingkeks.shoppinglist.R
import de.codingkeks.shoppinglist.recyclerview.items.Item
import de.codingkeks.shoppinglist.recyclerview.items.ItemAdapter
import kotlinx.android.synthetic.main.fragment_items.*
import java.text.SimpleDateFormat
import java.util.*

class ItemsFragment : Fragment() {

    private var items: MutableList<Item> = mutableListOf()
    private lateinit var adapter: ItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_items, container, false)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onStart() {
        super.onStart()

        adapter = ItemAdapter(items, spItems.selectedItemPosition)
        rvItems.adapter = adapter
        rvItems.layoutManager = LinearLayoutManager(requireContext())
        rvItems.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        val listId = activity?.intent?.getStringExtra("listId")
        val colRefItems = FirebaseFirestore.getInstance().collection("lists/${listId}/items")
        colRefItems.get().addOnSuccessListener { qSnap ->
            qSnap.documents.forEach { dSnap ->
                if (!(dSnap.get("isBought") as Boolean)) {
                    items.add(
                        Item(
                            dSnap.get("name").toString(),
                            (dSnap.get("quantity") as Long).toInt(),
                            dSnap.get("addedBy").toString(),
                            dSnap.get("addedTime").toString(),
                            false,
                            dSnap.id
                        )
                    )
                }
                sortingItems(spItems.selectedItemPosition)
                adapter.updateList()
                adapter.notifyDataSetChanged()
            }
        }

        spItems.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                sortingItems(position)
                adapter.notifyDataSetChanged()
                adapter.updateSpinnerPos(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        svItems.imeOptions = EditorInfo.IME_ACTION_DONE
        svItems.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })

        fabAddNewItem.setOnClickListener {
            val alertBuilder = AlertDialog.Builder(
                ContextThemeWrapper(requireContext(), R.style.AlertDialogTheme2)
            )
            alertBuilder.setTitle(R.string.addItem)
            val alertLayout = getEditTextLayout(requireContext())
            alertBuilder.setView(alertLayout)

            val textInputLayout = alertLayout.
            findViewWithTag<TextInputLayout>("textInputLayoutTag")
            val numberInputPicker = alertLayout.
            findViewWithTag<NumberPicker>("numberInputPicker")
            val textInputEditText = alertLayout.
            findViewWithTag<TextInputEditText>("textInputEditTextTag")

            alertBuilder.setPositiveButton(R.string.submit) { _, _->
                val user = FirebaseAuth.getInstance().currentUser!!
                val docRefUser = FirebaseFirestore.getInstance().document("users/${user.uid}")
                docRefUser.get().addOnSuccessListener { dSnap ->
                    val itemData = hashMapOf(
                        "name" to textInputEditText.text.toString(),
                        "quantity" to numberInputPicker.value.toLong(),
                        "addedBy" to dSnap.get("username").toString(),
                        "addedTime" to SimpleDateFormat("dd.MM.yyyy HH:mm").format(Date()),
                        "isBought" to false
                    )
                    val docRefItems = colRefItems.document()
                    docRefItems.set(itemData).addOnSuccessListener {
                        items.add(Item(
                            textInputEditText.text.toString(),
                            numberInputPicker.value, dSnap.get("username").toString(),
                            SimpleDateFormat("dd.MM.yyyy HH:mm").format(Date()),
                            false,
                            docRefItems.id
                        ))
                        sortingItems(spItems.selectedItemPosition)
                        adapter.updateList()
                        adapter.notifyDataSetChanged()
                    }
                }
            }
            alertBuilder.setNeutralButton(R.string.cancel, null)
            alertBuilder.setCancelable(false)
            val dialog = alertBuilder.create()
            dialog.show()

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

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
                        textInputLayout.error = getString(R.string.itemNameRequired)
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .isEnabled = false
                    } else {
                        textInputLayout.error = ""
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .isEnabled = true
                    }
                }
            })
        }
    }

    fun sortingItems(position: Int) {
        when (position) { //position 0: Latest; 1: A-Z; 2: Z-A
            0 -> {
                items.sortBy { it.name }
                items.sortBy { it.addedTime }
            }
            1 -> {
                items.sortBy { it.name }
            }
            2 -> {
                items.sortByDescending { it.name }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getEditTextLayout(context: Context): ConstraintLayout {
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
        textInputLayout.hint = getString(R.string.itemName)
        textInputLayout.id = View.generateViewId()
        textInputLayout.tag = "textInputLayoutTag"

        val textInputEditText = TextInputEditText(context)
        textInputEditText.id = View.generateViewId()
        textInputEditText.tag = "textInputEditTextTag"
        textInputEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        textInputLayout.addView(textInputEditText, 0)

        val textView = TextView(context)
        textView.text = getString(R.string.quantity) + ":"
        textView.setTextColor(Color.parseColor("#000000"))
        textView.textSize = 16f
        textInputLayout.addView(textView, 1)

        val numberInputPicker = NumberPicker(context)
        numberInputPicker.id = View.generateViewId()
        numberInputPicker.tag = "numberInputPicker"
        numberInputPicker.maxValue = 999
        numberInputPicker.minValue = 1
        numberInputPicker.value = 1
        numberInputPicker.wrapSelectorWheel = false
        textInputLayout.addView(numberInputPicker, 2)

        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)

        constraintLayout.addView(textInputLayout)
        return constraintLayout
    }

    private fun Int.toDp(context: Context):Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics
    ).toInt()
}