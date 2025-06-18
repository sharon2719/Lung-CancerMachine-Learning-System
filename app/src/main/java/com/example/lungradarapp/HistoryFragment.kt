package com.example.lungradarapp

import HistoryViewModel
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyImageView: ImageView
    private lateinit var emptyTitleTextView: TextView
    private lateinit var emptySubtitleTextView: TextView
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var historyViewModel: HistoryViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_history, container, false)

        recyclerView = rootView.findViewById(R.id.recyclerView)
        emptyImageView = rootView.findViewById(R.id.iv_empty_state)
        emptyTitleTextView = rootView.findViewById(R.id.tv_empty_message_title)
        emptySubtitleTextView = rootView.findViewById(R.id.tv_empty_message_subtitle)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        historyAdapter = HistoryAdapter(emptyList())  // Empty list initially
        recyclerView.adapter = historyAdapter

        // Initialize the ViewModel
        historyViewModel = ViewModelProvider(this).get(HistoryViewModel::class.java)

        // Observe LiveData from ViewModel and update UI
        historyViewModel.allResults.observe(viewLifecycleOwner) { analysisResults ->
            Log.d("com.example.lungradarapp.HistoryFragment", "Observed data: $analysisResults")  // Debugging log
            historyAdapter.updateData(analysisResults)
            updateEmptyState(analysisResults.isEmpty())
        }

        // Load results from the database
        historyViewModel.loadResults()

        return rootView
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        emptyImageView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        emptyTitleTextView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        emptySubtitleTextView.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }
}
