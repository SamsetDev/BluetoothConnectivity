package com.tech.samsetdownloader.secureView

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.tech.samsetdownloader.R
import com.tech.samsetdownloader.secureView.SecureConst.DEFAULT_GBOARD
import com.tech.samsetdownloader.secureView.SecureConst.GBOARD
import com.tech.samsetdownloader.secureView.SecureConst.SAMSUNGBOARD_NEW
import com.tech.samsetdownloader.secureView.SecureConst.SAMSUNGBOARD_OLD
import com.tech.samsetdownloader.secureView.SecureConst.SWIFTBOARD

/*
  Copyright (C) Downloader - All Rights Reserved Ⓒ
  Unauthorized copying of this file, via any medium is strictly prohibited
  Proprietary and confidential.
  See the License for the specific language governing permissions and limitations under the License.
 
  Created by Sanjay Singh  on 21,December,2022 at 12:27 AM for Downloader.
  New Delhi,India
 */


object Utils {

    fun isAvailableDefaultboard() : ArrayList<String>{
        val list=ArrayList<String>()
        list.add(GBOARD)
        list.add(SAMSUNGBOARD_NEW)
        list.add(SAMSUNGBOARD_OLD)
        list.add(SWIFTBOARD)
        return list
    }

    fun showWarningDialog(activity: Context, desc: String?, onClickListener: DialogInterface.OnClickListener?) {
       if (activity!=null){
           val dialog= AlertDialog.Builder(activity)
               .setMessage(desc).setCancelable(false)
               .setPositiveButton("OK", onClickListener).create()
               dialog.dismiss()
           if (!dialog.isShowing) dialog.show()
       }
    }

    fun openDefaultKeyboard(act: Context) {

    }

    fun openInputSettings(activity: Context){
        if (activity!=null) {
            val enableIntent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
            enableIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        }
    }

    fun isAvailableGBoard(context: Context): Boolean {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return imm.enabledInputMethodList.any { it.id.equals(GBOARD, true) ||
                it.id.equals(SAMSUNGBOARD_NEW, true) ||
                it.id.equals(SAMSUNGBOARD_OLD, true) || it.id.equals(SWIFTBOARD, true) }
    }
}