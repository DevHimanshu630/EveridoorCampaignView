package com.everidoor.campaign

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CampaignAdapter(private val campaigns: List<Campaign>, private val context: Context) :
    RecyclerView.Adapter<CampaignAdapter.CampaignViewHolder>() {

    class CampaignViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val campaignName: TextView = itemView.findViewById(R.id.campaignname)
        val fromDate: TextView = itemView.findViewById(R.id.fromdate)
        val toDate: TextView = itemView.findViewById(R.id.todate)
        val noOfScreens: TextView = itemView.findViewById(R.id.noofscreen)
        val campaignImage: ImageView = itemView.findViewById(R.id.campaignname1)
        val cardView: View = itemView.findViewById(R.id.card_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CampaignViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.campaign_layout, parent, false)
        return CampaignViewHolder(view)
    }

    override fun onBindViewHolder(holder: CampaignViewHolder, position: Int) {
        val campaign = campaigns[position]
        holder.campaignName.text = campaign.campaignName
        holder.fromDate.text = campaign.fromDate
        holder.toDate.text = campaign.toDate
        holder.noOfScreens.text = campaign.noOfScreens.toString()

        holder.cardView.setOnClickListener {
            val intent = Intent(context, ScreenActivity::class.java).apply {
                putExtra("CAMPAIGN_ID", campaign.campaignId)
                putExtra("USERNAME", campaign.username)
                putParcelableArrayListExtra("SCREENS", ArrayList(campaign.screens))
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = campaigns.size
}
