package com.andreacioccarelli.musicdownloader.extensions

import android.content.Context
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.EditText
import com.andreacioccarelli.musicdownloader.App

/**
 *  Designed and developed by Andrea Cioccarelli
 */

fun EditText.dismissKeyboard() {
    val imm = App.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
}


fun EditText.popUpKeyboard() {
    val imm = App.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, SHOW_IMPLICIT)
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