package com.starqr.starcatcher

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LevelAdapter(private val levelCount: Int, private val highestLevelUnlocked: Int) : RecyclerView.Adapter<LevelAdapter.LevelViewHolder>() {

    class LevelViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val levelNumberTextView: TextView = view.findViewById(R.id.levelNumberTextView)
        val lockOverlay: View = view.findViewById(R.id.lockOverlay)
        val lockIcon: ImageView = view.findViewById(R.id.lockIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LevelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_level_card, parent, false)
        return LevelViewHolder(view)
    }

    override fun getItemCount(): Int = levelCount

    override fun onBindViewHolder(holder: LevelViewHolder, position: Int) {
        val levelNumber = position + 1
        holder.levelNumberTextView.text = "Level $levelNumber"

        if (levelNumber > highestLevelUnlocked) {
            // Уровень заблокирован
            holder.lockOverlay.visibility = View.VISIBLE
            holder.lockIcon.visibility = View.VISIBLE
            holder.itemView.isClickable = false
        } else {
            // Уровень открыт
            holder.lockOverlay.visibility = View.GONE
            holder.lockIcon.visibility = View.GONE
            holder.itemView.isClickable = true
            holder.itemView.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, GameActivity::class.java).apply {
                    putExtra("LEVEL_ID", levelNumber)
                }
                context.startActivity(intent)
            }
        }
    }
}