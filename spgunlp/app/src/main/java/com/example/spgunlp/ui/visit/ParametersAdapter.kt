package com.example.spgunlp.ui.visit

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spgunlp.databinding.ParameterItemBinding
import com.example.spgunlp.model.AppVisitParameters

class ParametersAdapter(private val parameters: List<AppVisitParameters>, private val clickListener: ParameterClickListener):
    RecyclerView.Adapter<ParametersAdapter.ParametersViewHolder>() {
    private val checkedMap = HashMap<Int, Boolean>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParametersViewHolder {
        val from = LayoutInflater.from(parent.context)
        val cardCellBinding = ParameterItemBinding.inflate(from, parent, false)
        return ParametersViewHolder(cardCellBinding,clickListener)
    }

    override fun onBindViewHolder(viewHolder: ParametersViewHolder, position: Int) {

        val item = parameters[position]
        viewHolder.bind(item, position)
    }

    override fun getItemCount() = parameters.size

    fun getCheckedMap(): HashMap<Int, Boolean> { return this.checkedMap }

    inner class ParametersViewHolder(private val cardCellBinding: ParameterItemBinding, private val clickListener: ParameterClickListener) : RecyclerView.ViewHolder(cardCellBinding.root) {
        fun bind(parameter: AppVisitParameters, position: Int) {

            cardCellBinding.checkbox.text = parameter.nombre
            if (parameter.cumple == true){
                cardCellBinding.checkbox.isChecked = true
                checkedMap[position] = true
            } else {
                cardCellBinding.checkbox.isChecked = false
                checkedMap[position] = false
            }

            cardCellBinding.checkbox.setOnCheckedChangeListener { _, _->
                checkedMap[position] = !checkedMap[position]!!
            }

            if (position == parameters.size - 1)
                cardCellBinding.divider.visibility= View.GONE
        }
    }

}