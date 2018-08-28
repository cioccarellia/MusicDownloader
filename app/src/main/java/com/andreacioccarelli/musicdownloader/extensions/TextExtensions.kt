package com.andreacioccarelli.musicdownloader.extensions

import com.andreacioccarelli.musicdownloader.App
import android.content.Context
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText


/**
 * Created by andrea on 2018/Aug.
 * Part of the package andreacioccarelli.musicdownloader.extensions
 */

fun EditText.dismissKeyboard() {
    val imm = App.instance.baseContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
}

fun EditText.popUpKeyboard() {
    val imm = App.instance.baseContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, 0)
}

fun EditText.onSubmit(code: () -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE
                || actionId == EditorInfo.IME_ACTION_NONE
                || actionId == EditorInfo.IME_ACTION_GO
                || actionId == EditorInfo.IME_ACTION_SEARCH
                || actionId == EditorInfo.IME_ACTION_SEND
                || actionId == EditorInfo.IME_ACTION_NEXT) {
            code()
        }
        true
    }
}