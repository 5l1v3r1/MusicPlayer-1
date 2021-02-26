package com.ftmusic.hadise

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class MusicAdapter(private val context:Context,
                   private val musicListName:ArrayList<String>): BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


    override fun getView(position: Int, view: View?, parent: ViewGroup?): View {
        val rowView = inflater.inflate(R.layout.item_leftlist, parent, false)
        var tvMusic:TextView = rowView.findViewById(R.id.itemMusic)
        tvMusic.setText(musicListName.get(position))
        return rowView
    }

    override fun getItem(p0: Int): Any {
        return musicListName.get(p0)
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return musicListName.size
    }



}