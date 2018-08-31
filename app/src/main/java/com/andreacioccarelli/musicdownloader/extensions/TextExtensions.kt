package com.andreacioccarelli.musicdownloader.extensions

import android.content.Context
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.andreacioccarelli.musicdownloader.App


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
        when (actionId) {
            EditorInfo.IME_ACTION_DONE,
            EditorInfo.IME_ACTION_NONE,
            EditorInfo.IME_ACTION_GO,
            EditorInfo.IME_ACTION_SEARCH,
            EditorInfo.IME_ACTION_SEND,
            EditorInfo.IME_ACTION_NEXT -> code()
        }
        true
    }
}