package com.example.spgunlp.ui.visit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.spgunlp.R
import com.example.spgunlp.model.AppVisitParameters.Principle
import com.example.spgunlp.databinding.PrincipleItemBinding

class PrinciplesAdapter(private val principles: List<Principle>, private val states: List<Boolean>, private val clickListener: PrincipleClickListener):
    RecyclerView.Adapter<PrinciplesAdapter.PrinciplesViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrinciplesViewHolder {
        val from = LayoutInflater.from(parent.context)
        val cardCellBinding = PrincipleItemBinding.inflate(from, parent, false)
        return PrinciplesViewHolder(cardCellBinding,clickListener)
    }

    override fun onBindViewHolder(viewHolder: PrinciplesViewHolder, position: Int) {

        val item = principles[position]
        val state = states[position]

        viewHolder.bind(item, state)
    }
    override fun getItemCount() = principles.size

    inner class PrinciplesViewHolder(private val cardCellBinding:PrincipleItemBinding,private val clickListener: PrincipleClickListener) : RecyclerView.ViewHolder(cardCellBinding.root) {
        fun bind(principle: Principle, cumple: Boolean) {

            if (cumple){
                val color = ContextCompat.getColor(cardCellBinding.cardView.context, R.color.green)
                cardCellBinding.cardView.setStrokeColor(color)
                cardCellBinding.stateBar.setBackgroundColor(color)
                cardCellBinding.principleState.text = "Cumplido"
            }else {
                val color = ContextCompat.getColor(cardCellBinding.cardView.context, R.color.red)
                cardCellBinding.cardView.setStrokeColor(color)
                cardCellBinding.stateBar.setBackgroundColor(color)
                cardCellBinding.principleState.text = "Incumplido"
            }

            cardCellBinding.principleTitle.text = principle.nombre

            cardCellBinding.btnChecklist.setOnClickListener {
                clickListener.onClickChecklist(principle)
            }

            cardCellBinding.btnObservations.setOnClickListener {
                clickListener.onClickObservations(principle)
            }
        }

    }

}