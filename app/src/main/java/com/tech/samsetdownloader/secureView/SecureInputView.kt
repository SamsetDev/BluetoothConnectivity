package com.tech.samsetdownloader.secureView

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.*
import com.google.android.material.textfield.TextInputEditText


/*
  Copyright (C)  - All Rights Reserved Ⓒ
  Unauthorized copying of this file, via any medium is strictly prohibited
  Proprietary and confidential.
  See the License for the specific language governing permissions and limitations under the License.
 
  Created by Sanjay Singh  on 20,December,2022 at 9:49 PM for Downloader.
  New Delhi,India
 */


open class SecureInputView : TextInputEditText {
    var ides:Int=0
    var status:Boolean=true
    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    override fun getAutofillType(): Int {
        return AUTOFILL_TYPE_NONE
    }

     override fun isSuggestionsEnabled() : Boolean
    {
        return false
    }

    override fun getSelectionStart(): Int {
        for (element in Thread.currentThread().stackTrace) {
            if (element.methodName == "canPaste") {
                return -1
            }
        }
        return super.getSelectionStart()
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        when (id) {
            android.R.id.paste, android.R.id.pasteAsPlainText -> return false
        }
        return super.onTextContextMenuItem(id)
    }

    @SuppressLint("NewApi")
    public fun init(context: Context, attrs: AttributeSet?) {
        //attrs?.let { disableCopyAndPaste(context, it) }
        var timestamp = 0L
        //this.disableCopyPaste()

        this.customInsertionActionModeCallback= object : ActionMode.Callback{
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }
            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }
            override fun onActionItemClicked(mode: ActionMode, item: MenuItem?): Boolean {
                mode.finish()
                return false
            }
            override fun onDestroyActionMode(mode: ActionMode?) {
            }
        }
        this.customSelectionActionModeCallback = object : ActionMode.Callback{
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }
            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }
            override fun onActionItemClicked(mode: ActionMode, item: MenuItem?): Boolean {
                mode.finish()
                return false
            }
            override fun onDestroyActionMode(mode: ActionMode?) {
            }

        }


        this.isLongClickable = false
        this.setTextIsSelectable(false)
        this.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO)

        if (getInputtype(this@SecureInputView)!=InputType.TYPE_TEXT_VARIATION_PASSWORD){
           // this@SecureInputView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD )
            Log.e("tag"," TYPE_TEXT_VARIATION_PASSWORD found ")
        }
        val clipService = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        while (clipService.hasPrimaryClip()) {
            clipService.clearPrimaryClip()
            Log.e("tag"," set  ")
        }

        //clearClipboard(context)
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                if (count!=after && after - count > 1) {
                    Log.e("tag"," start "+start+" after "+after+" count "+count)
                     //this@SecureInputView.setText("")
                    // this@SecureInputView.setSelection(s.toString().length)
                }
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {



            }
            override fun afterTextChanged(s: Editable) {

            }
        })
    }



     private fun getInputtype(editText: TextInputEditText) : Int{
     val strInputType: Int
     val inputType: Int = editText.getInputType()
        strInputType = when (inputType) {
        InputType.TYPE_TEXT_FLAG_CAP_WORDS or InputType.TYPE_CLASS_TEXT -> {
            InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        }
        InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT -> {
            InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS or InputType.TYPE_CLASS_TEXT -> {
            InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
        InputType.TYPE_CLASS_PHONE -> {
            InputType.TYPE_CLASS_PHONE
        }
        InputType.TYPE_CLASS_DATETIME -> {
            InputType.TYPE_CLASS_DATETIME
        }
        InputType.TYPE_CLASS_NUMBER -> {
            InputType.TYPE_CLASS_NUMBER
        }
        InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS -> {
            InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS
        }
        else -> {
            InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        }
    }

         return strInputType
}

    @SuppressLint("NewApi")
    fun clearClipboard(activity: Context){
        val clipService = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            var clip=ClipData.newPlainText("", "")
            clipService.setPrimaryClip(clip)
        }else{
            clipService.text=""
        }
        clipService.clearPrimaryClip()
    }

}
