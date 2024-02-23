package com.example.spgunlp.ui.visit

import com.example.spgunlp.model.AppVisitParameters

interface PrincipleClickListener {
    fun onClickChecklist(principle: AppVisitParameters.Principle)
    fun onClickObservations(principle: AppVisitParameters.Principle)
}