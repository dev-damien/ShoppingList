package de.codingkeks.shoppinglist.recyclerview.items

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.codingkeks.shoppinglist.R
import kotlinx.android.synthetic.main.rv_item.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ItemAdapter(var items: List<Item>, var spPos: Int, var listId: String, var itemsFull: ArrayList<Item> = ArrayList<Item>(items))
    : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>(), Filterable {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_item, parent, false)
        return ItemViewHolder(view)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.itemView.apply {
            tvItemName.text = items[position].name
            tvQuantity.text = items[position].quantity.toString()
            tvContributor.text = items[position].addedBy
            tvAddedTime.text = items[position].addedTime
        }
        holder.itemView.btnItemOptions.setOnClickListener {
            val popupMenu = PopupMenu(holder.itemView.context, holder.itemView.btnItemOptions)
            popupMenu.menuInflater.inflate(R.menu.popup_item_options, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_bought -> {
                        val uid = FirebaseAuth.getInstance().currentUser!!.uid
                        val colRefUsers = FirebaseFirestore.getInstance().document("users/${uid}")
                        colRefUsers.get().addOnSuccessListener {
                            val username = it.get("username")
                            val docRef = FirebaseFirestore.getInstance().document("lists/${listId}/items/${items[position].itemId}")
                            docRef.update("isBought", true)
                            docRef.update("boughtBy", username.toString())
                            docRef.update("boughtAt", SimpleDateFormat("dd.MM.yyyy HH:mm").format(
                                Date()
                            ))
                            updateList()
                            notifyDataSetChanged()
                        }
                    }
                    R.id.action_edit -> {
                        val colRefItems = FirebaseFirestore.getInstance().collection("lists/${listId}/items")
                        val alertBuilder = AlertDialog.Builder(
                            ContextThemeWrapper(holder.itemView.context, R.style.AlertDialogTheme2)
                        )
                        alertBuilder.setTitle(R.string.addItem)
                        val alertLayout = getEditTextLayout(holder.itemView.context, items[position].quantity, items[position].name)
                        alertBuilder.setView(alertLayout)

                        val textInputLayout = alertLayout.
                        findViewWithTag<TextInputLayout>("textInputLayoutTag")
                        val numberInputPicker = alertLayout.
                        findViewWithTag<NumberPicker>("numberInputPicker")
                        val textInputEditText = alertLayout.
                        findViewWithTag<TextInputEditText>("textInputEditTextTag")

                        alertBuilder.setPositiveButton(R.string.change) { _, _->
                            val user = FirebaseAuth.getInstance().currentUser!!
                            val docRefUser = FirebaseFirestore.getInstance().document("users/${user.uid}")
                            docRefUser.get().addOnSuccessListener { dSnap ->
                                val itemData = hashMapOf(
                                    "name" to textInputEditText.text.toString(),
                                    "quantity" to numberInputPicker.value.toLong(),
                                    "addedBy" to items[position].addedBy,
                                    "addedTime" to items[position].addedTime,
                                    "isBought" to false,
                                    "boughtBy" to "",
                                    "boughtAt" to ""
                                )
                                val docRefItems = colRefItems.document(items[position].itemId)
                                docRefItems.update(itemData).addOnSuccessListener {
                                    when (spPos) { //position 0: Latest; 1: A-Z; 2: Z-A
                                        0 -> {
                                            (items as ArrayList<Item>).sortBy { it.name.toLowerCase() }
                                            (items as ArrayList<Item>).sortByDescending { SimpleDateFormat("dd.MM.yyyy HH:mm").parse(it.addedTime) }
                                        }
                                        1 -> {
                                            (items as ArrayList<Item>).sortBy { it.name.toLowerCase() }
                                            (items as ArrayList<Item>).sortBy { SimpleDateFormat("dd.MM.yyyy HH:mm").parse(it.addedTime) }
                                        }
                                        2 -> {
                                            (items as ArrayList<Item>).sortBy { it.name.toLowerCase() }
                                        }
                                        3 -> {
                                            (items as ArrayList<Item>).sortByDescending { it.name.toLowerCase() }
                                        }
                                    }
                                    updateList()
                                    notifyDataSetChanged()
                                }
                            }
                        }
                        alertBuilder.setNeutralButton(R.string.cancel, null)
                        alertBuilder.setCancelable(false)
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
                                    textInputLayout.error = holder.itemView.context.getString(R.string.itemNameRequired)
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
                    R.id.action_delete -> {
                        AlertDialog.Builder(ContextThemeWrapper(holder.itemView.context, R.style.AlertDialogTheme))
                            .setTitle(R.string.delete_item)
                            .setMessage(R.string.delete_item_sure)
                            .setPositiveButton(R.string.emailVerificationDelete) { _, _ ->
                                FirebaseFirestore.getInstance().document("lists/${listId}/items/${items[position].itemId}").delete()
                                updateList()
                                notifyDataSetChanged()
                            }
                            .setNegativeButton(R.string.cancel, null)
                            .show()
                    }
                }
                true
            })
            popupMenu.show()
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getFilter(): Filter {
        return filter
    }

    private var filter: Filter = object: Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            var filteredList: ArrayList<Item> = ArrayList<Item>()

            if (constraint == null || constraint.isEmpty()) {
                filteredList.addAll(itemsFull)
            } else {
                var filterPattern: String = constraint.toString().toLowerCase().trim()

                itemsFull.forEach {
                    if (it.name.toLowerCase().contains(filterPattern)) {
                        filteredList.add(it)
                    }
                }
            }

            when (spPos) { //position 0: Latest; 1: A-Z; 2: Z-A
                0 -> {
                    filteredList.sortBy { it.name.toLowerCase() }
                    filteredList.sortByDescending { SimpleDateFormat("dd.MM.yyyy HH:mm").parse(it.addedTime) }
                }
                1 -> {
                    filteredList.sortBy { it.name.toLowerCase() }
                    filteredList.sortBy { SimpleDateFormat("dd.MM.yyyy HH:mm").parse(it.addedTime) }
                }
                2 -> {
                    filteredList.sortBy { it.name.toLowerCase() }
                }
                3 -> {
                    filteredList.sortByDescending { it.name.toLowerCase() }
                }
            }

            var filterResults: FilterResults = FilterResults()
            filterResults.values = filteredList
            return filterResults
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            (items as ArrayList<Item>).clear()
            if (results != null) {
                (items as ArrayList<Item>).addAll(results.values as ArrayList<Item>)
            }
            notifyDataSetChanged()
        }
    }

    fun updateList() {
        itemsFull = ArrayList<Item>(items)
    }

    fun updateSpinnerPos(spPos: Int) {
        this.spPos = spPos
    }

    @SuppressLint("SetTextI18n")
    private fun getEditTextLayout(context: Context, quantity: Int, itemName: String): ConstraintLayout {
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
        textInputLayout.hint = context.getString(R.string.itemName)
        textInputLayout.id = View.generateViewId()
        textInputLayout.tag = "textInputLayoutTag"

        val textInputEditText = TextInputEditText(context)
        textInputEditText.id = View.generateViewId()
        textInputEditText.tag = "textInputEditTextTag"
        textInputEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        textInputEditText.filters = arrayOf(*textInputEditText.filters, InputFilter.LengthFilter(30))
        textInputEditText.setText(itemName)
        textInputLayout.addView(textInputEditText, 0)

        val textView = TextView(context)
        textView.text = context.getString(R.string.quantity) + ":"
        textView.setTextColor(Color.parseColor("#000000"))
        textView.textSize = 16f
        textInputLayout.addView(textView, 1)

        val numberInputPicker = NumberPicker(context)
        numberInputPicker.id = View.generateViewId()
        numberInputPicker.tag = "numberInputPicker"
        numberInputPicker.maxValue = 999
        numberInputPicker.minValue = 1
        numberInputPicker.value = quantity
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