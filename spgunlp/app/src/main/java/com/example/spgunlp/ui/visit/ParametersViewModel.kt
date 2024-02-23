package com.example.spgunlp.ui.visit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.spgunlp.model.AppVisitParameters

class ParametersViewModel : ViewModel() {
    private val _parameters = MutableLiveData<List<AppVisitParameters?>?>()
    val parameters: LiveData<List<AppVisitParameters?>?> = _parameters
    private val _parametersCurrentPrinciple = MutableLiveData<List<AppVisitParameters?>?>()
    val parametersCurrentPrinciple: LiveData<List<AppVisitParameters?>?> = _parametersCurrentPrinciple

    fun setParameters(value: List<AppVisitParameters?>?){
        _parameters.value = value
    }

    fun setParametersCurrentPrinciple(value: List<AppVisitParameters?>?){
        _parametersCurrentPrinciple.value = value
    }

}