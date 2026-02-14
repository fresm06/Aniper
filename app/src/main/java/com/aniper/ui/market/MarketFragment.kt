package com.aniper.ui.market

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aniper.R
import com.aniper.data.LocalPetData
import com.aniper.model.MarketItem
import com.google.android.material.progressindicator.CircularProgressIndicator

class MarketFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_market, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_market)
        val emptyView = view.findViewById<TextView>(R.id.tv_market_empty)
        view.findViewById<CircularProgressIndicator>(R.id.progress_bar).visibility = View.GONE

        // Sample local market items for testing
        val sampleItems = listOf(
            MarketItem(
                id = "market_1",
                name = "Orange Cat",
                description = "A cute orange cat",
                creatorName = "Aniper Team",
                assetId = "default_cat",
                thumbnailRes = R.drawable.pet_idle_right,
                downloads = 42
            )
        )

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = MarketAdapter(sampleItems) { item ->
            Toast.makeText(requireContext(), "Downloaded: ${item.name}!", Toast.LENGTH_SHORT).show()
        }
        emptyView.visibility = View.GONE
    }

    inner class MarketAdapter(
        private val items: List<MarketItem>,
        private val onClick: (MarketItem) -> Unit
    ) : RecyclerView.Adapter<MarketAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val thumbnail: ImageView = view.findViewById(R.id.iv_market_thumbnail)
            val name: TextView = view.findViewById(R.id.tv_market_item_name)
            val creator: TextView = view.findViewById(R.id.tv_market_item_creator)
            val downloads: TextView = view.findViewById(R.id.tv_market_item_downloads)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_market, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.name.text = item.name
            holder.creator.text = "by ${item.creatorName}"
            holder.downloads.text = "${item.downloads} downloads"
            if (item.thumbnailRes != 0) {
                holder.thumbnail.setImageDrawable(
                    ContextCompat.getDrawable(holder.itemView.context, item.thumbnailRes)
                )
            }
            holder.itemView.setOnClickListener { onClick(item) }
        }

        override fun getItemCount() = items.size
    }
}
