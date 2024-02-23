package com.example.spgunlp.ui.visit

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spgunlp.databinding.ObsMeItemBinding
import com.example.spgunlp.databinding.ObsOtherItemBinding
import com.example.spgunlp.model.AppMessage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ObservationsAdapter(private val messages: List<AppMessage>, private val email: String, private val clickListener: MessageClickListener):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        if (messages[position].sender?.email == email){
            return 0
        }
            return 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> ObservationsMeViewHolder(ObsMeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false), clickListener)
            1 -> ObservationsOtherViewHolder(ObsOtherItemBinding.inflate(LayoutInflater.from(parent.context), parent, false), clickListener)
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {

        when (viewHolder.itemViewType) {
            0 -> (viewHolder as ObservationsMeViewHolder).bind(messages[position], position)
            1 -> (viewHolder as ObservationsOtherViewHolder).bind(messages[position], position)
        }
    }

    override fun getItemCount() = messages.size

    inner class ObservationsMeViewHolder(private val cardCellBinding: ObsMeItemBinding, private val clickListener: MessageClickListener) : RecyclerView.ViewHolder(cardCellBinding.root) {
        @SuppressLint("NewApi")
        fun bind(message: AppMessage, position: Int) {

            val formatter = DateTimeFormatter.ISO_DATE_TIME
            val date = LocalDateTime.parse(message.date, formatter)
            val timestamp = date.format(DateTimeFormatter.ofPattern("HH:mm"))
            val dateFormatted = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

            if (position != 0) {
                val previousDate = LocalDateTime.parse(messages[position - 1].date, formatter)
                if (previousDate.year != date.year || previousDate.monthValue != date.monthValue || previousDate.dayOfMonth != date.dayOfMonth) {
                    cardCellBinding.chatDate.visibility = View.VISIBLE
                }
            } else {
                cardCellBinding.chatDate.visibility = View.VISIBLE
            }

            cardCellBinding.textChat.text = message.data
            cardCellBinding.chatDate.text = dateFormatted
            cardCellBinding.chatTimestamp.text = timestamp
        }
    }

    inner class ObservationsOtherViewHolder(private val cardCellBinding: ObsOtherItemBinding, private val clickListener: MessageClickListener) : RecyclerView.ViewHolder(cardCellBinding.root) {
        @SuppressLint("NewApi")
        fun bind(message: AppMessage, position: Int) {


            val formatter = DateTimeFormatter.ISO_DATE_TIME
            val date = LocalDateTime.parse(message.date, formatter)
            val timestamp = date.format(DateTimeFormatter.ofPattern("HH:mm"))
            val dateFormatted = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

            if (position != 0) {
                val previousDate = LocalDateTime.parse(messages[position - 1].date, formatter)
                if (previousDate.year != date.year || previousDate.monthValue != date.monthValue || previousDate.dayOfMonth != date.dayOfMonth) {
                    cardCellBinding.chatDate.visibility = View.VISIBLE
                }

                if (messages[position - 1].sender?.email != message.sender?.email) {
                    cardCellBinding.chatUsername.visibility = View.VISIBLE
                } else {
                    (cardCellBinding.messageLayout.layoutParams as RecyclerView.LayoutParams).setMargins(0,0,0,0)
                }
            } else {
                cardCellBinding.chatDate.visibility = View.VISIBLE
                cardCellBinding.chatUsername.visibility = View.VISIBLE
            }

            cardCellBinding.textChat.text = message.data
            //cardCellBinding.imageGchatProfileOther.context = message.sender
            cardCellBinding.chatDate.text = dateFormatted
            cardCellBinding.chatTimestamp.text = timestamp
            cardCellBinding.chatUsername.text = message.sender?.nombre

        }
    }

}