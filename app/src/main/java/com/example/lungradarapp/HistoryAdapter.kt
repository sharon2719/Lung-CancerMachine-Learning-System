package com.example.lungradarapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(private var historyList: List<AnalysisResult>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val ivImage: ImageView = itemView.findViewById(R.id.ivImage)
        val tvResult: TextView = itemView.findViewById(R.id.tvResult)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = historyList[position]

        // Format the timestamp to a readable date
        val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(item.timestamp))
        holder.tvDate.text = formattedDate

        // Use resultType instead of result
        holder.tvResult.text = item.resultType  // Use resultType from AnalysisResult

        // Load image using Glide
        Glide.with(holder.itemView.context)
            .load(item.imagePath)  // Use the image path from the `AnalysisResult` object
            .placeholder(R.drawable.lungs)  // Placeholder image
            .error(R.drawable.broken)  // Error image
            .into(holder.ivImage)
    }

    override fun getItemCount(): Int = historyList.size

    // Update the data and notify the adapter
    fun updateData(newData: List<AnalysisResult>) {
        historyList = newData
        notifyDataSetChanged()
    }
}
