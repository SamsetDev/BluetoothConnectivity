package com.tech.samsetdownloader

import android.os.Bundle
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    //@SuppressLint("MissingInflatedId")
    lateinit var input:EditText
    lateinit var input2:EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        input=findViewById(R.id.etinput1)
        input2=findViewById(R.id.etinput2)
        input.addTextChangedListener(textWatcher)
        input2.addTextChangedListener(textWatcher2)


    }
    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            val text_value: String = input.getText().toString()

            if (s.length === 14) {
                //input.setText(input.getText().toString())
                input.clearFocus()
                Log.e("tag"," onTextChange clear focus "+s)
                // Util.hideKeyboard(getActivity(), etMobileNumber)
            }
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {



        }
    }

    private val textWatcher2 = object : TextWatcher {

        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            val text_value: String = input.getText().toString()
            if (s.length === 14) {
                //input2.clearFocus()
               // input2.setText("Done "+s)
               // Selection.setSelection(input2.getText(), input2.getText().length)
                Log.e("tag"," onTextChange clear focus "+s)
                // Util.hideKeyboard(getActivity(), etMobileNumber)
            }


        }
    }
}