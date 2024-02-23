package com.example.spgunlp.ui.stats

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.spgunlp.R
import com.example.spgunlp.databinding.GridPrincipleItemBinding
import com.example.spgunlp.model.AppVisitParameters
import kotlin.math.roundToInt

class StatsAdapter(private val context: Context, private val principles: List<AppVisitParameters.Principle>, private val percentages: List<String>, private val clickListener: StatsClickListener) :
    RecyclerView.Adapter<StatsAdapter.StatsViewHolder>() {

    inner class StatsViewHolder(private val cardCellBinding: GridPrincipleItemBinding, private val clickListener: StatsClickListener) :
        RecyclerView.ViewHolder(cardCellBinding.root) {

        private val fullNames = mutableMapOf<String,String>()
        fun bind(principle: AppVisitParameters.Principle, percentage: String) {

            // limit nombre to 20 characters
            val nombre = if (principle.nombre!= null && principle.nombre.length > 60) {
                principle.nombre.substring(0, 60) + "..."
            } else {
                principle.nombre
            }

            fullNames[nombre.toString()] = principle.nombre.toString()
            cardCellBinding.principleName.text = nombre
            cardCellBinding.principlePercentage.text = percentage
            when (percentage.substring(0,percentage.length - 1).toInt()) {
                in 0..25 -> cardCellBinding.cardviewApprovedprinciples.backgroundTintList = ContextCompat.getColorStateList(context, R.color.red)
                in 26..74 -> cardCellBinding.cardviewApprovedprinciples.backgroundTintList = ContextCompat.getColorStateList(context, R.color.yellow)
                in 75..100 -> cardCellBinding.cardviewApprovedprinciples.backgroundTintList = ContextCompat.getColorStateList(context, R.color.green)
            }

            cardCellBinding.layoutStatPrinciple.setOnClickListener{
                (cardCellBinding.principleName.text as String?)?.let { partialName ->
                    fullNames[partialName]?.let { it1 ->
                        clickListener.onClick(cardCellBinding.principlePercentage.text as String,
                            it1
                        )
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatsViewHolder {
        val from = LayoutInflater.from(parent.context)
        val cardCellBinding = GridPrincipleItemBinding.inflate(from, parent, false)
        return StatsViewHolder(cardCellBinding, clickListener)
    }

    override fun getItemCount(): Int {
        return principles.size
    }

    override fun onBindViewHolder(holder: StatsViewHolder, position: Int) {
        val item = principles[position]
        val percentage = percentages[position]
        holder.bind(item, percentage)
    }
}