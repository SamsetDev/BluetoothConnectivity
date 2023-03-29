package com.tech.samsetdownloader.secureView

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText

/*
  Copyright (C) Downloader - All Rights Reserved Ⓒ
  Unauthorized copying of this file, via any medium is strictly prohibited
  Proprietary and confidential.
  See the License for the specific language governing permissions and limitations under the License.
 
  Created by Sanjay Singh  on 10,March,2023 at 7:14 PM for Downloader.
  New Delhi,India
 */


/*

@SuppressLint("NewApi")
fun EditText.disableCopyPaste() {
    this.customInsertionActionModeCallback= object : ActionMode.Callback{
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }
        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
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
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return false
        }
        override fun onDestroyActionMode(mode: ActionMode?) {
        }

    }

    this.isLongClickable = false
    this.setTextIsSelectable(false)
    this.addTextChangedListener( object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            if (after - count > 1) {
                this@disableCopyPaste.setText("")
                //this@disableCopyPaste.setSelection(s.toString().length)
            }
        }
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {}
    })
}*/
