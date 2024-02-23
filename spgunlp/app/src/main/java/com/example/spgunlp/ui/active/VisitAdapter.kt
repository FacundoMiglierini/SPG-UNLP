package com.example.spgunlp.ui.active

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.spgunlp.databinding.ListVisitElementBinding
import com.example.spgunlp.model.AppVisit
class VisitAdapter(private val visits:List<AppVisit>,private val clickListener: VisitClickListener): RecyclerView.Adapter<VisitViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitViewHolder {
        val from = LayoutInflater.from(parent.context)
        val cardCellBinding = ListVisitElementBinding.inflate(from, parent, false)
        return VisitViewHolder(cardCellBinding,clickListener)
    }

    override fun getItemCount(): Int {
        return visits.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: VisitViewHolder, position: Int) {
        holder.findVisit(visits[position])
    }
}