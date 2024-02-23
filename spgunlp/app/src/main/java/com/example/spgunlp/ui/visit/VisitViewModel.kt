package com.example.spgunlp.ui.visit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.spgunlp.model.AppVisit

class VisitViewModel : ViewModel() {
    private val _visit = MutableLiveData<AppVisit>()
    val visit: LiveData<AppVisit> get() = _visit
    fun setVisit(value: AppVisit){
        _visit.postValue(value)
    }
}