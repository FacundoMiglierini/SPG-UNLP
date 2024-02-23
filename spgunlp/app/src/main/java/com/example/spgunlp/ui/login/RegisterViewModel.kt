package com.example.spgunlp.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegisterViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is register Fragment"
    }
    val text: LiveData<String> = _text
}