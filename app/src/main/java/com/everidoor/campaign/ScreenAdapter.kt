package com.everidoor.campaign

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ScreenAdapter(private val screens: List<Screen>, private val context: Context) :
    RecyclerView.Adapter<ScreenAdapter.ScreenViewHolder>() {

    class ScreenViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val screenId: TextView = itemView.findViewById(R.id.screenId)
        val screenName: TextView = itemView.findViewById(R.id.screenName)
        val type: TextView = itemView.findViewById(R.id.type)
        val typeVal: TextView = itemView.findViewById(R.id.typeVal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScreenViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.screen_layout, parent, false)
        return ScreenViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScreenViewHolder, position: Int) {
        val screen = screens[position]
        holder.screenId.text = screen.screenId
        holder.screenName.text = screen.screenName
        holder.type.text = screen.type
        holder.typeVal.text = screen.typeVal

        holder.itemView.setOnClickListener {
            val intent = Intent(context, Display::class.java).apply {
                putExtra("screenId", screen.screenId)
                putExtra("username", screen.username)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = screens.size
}
