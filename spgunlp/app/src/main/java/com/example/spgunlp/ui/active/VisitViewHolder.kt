package com.example.spgunlp.ui.active

import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.spgunlp.R
import com.example.spgunlp.databinding.ListVisitElementBinding
import com.example.spgunlp.model.AppVisit
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class VisitViewHolder(private val cardCellBinding:ListVisitElementBinding,private val clickListener:VisitClickListener): RecyclerView.ViewHolder(cardCellBinding.root)
{

    @RequiresApi(Build.VERSION_CODES.O)
    fun findVisit(visit: AppVisit)
    {
        if(!visit.imagenes.isNullOrEmpty()){
            val image=BitmapFactory.decodeByteArray(visit.imagenes[0].contenido, 0, visit.imagenes[0].contenido!!.size)
            cardCellBinding.iconImageView.setImageBitmap(image)
        }else{
            cardCellBinding.iconImageView.setImageResource(R.drawable.visita_default)
        }
        val title="${visit.quintaResponse?.organizacion} | ${visit.quintaResponse?.nombreProductor}"
        cardCellBinding.visitName.text = title

        val date=ZonedDateTime.parse(visit.fechaActualizacion)
        val res=date.withZoneSameInstant(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))

        val dateString="Última modificación: $res"
        cardCellBinding.visitDateModif.text = dateString

        cardCellBinding.cardView.setOnClickListener{
            clickListener.onClick(visit)
        }
    }
}