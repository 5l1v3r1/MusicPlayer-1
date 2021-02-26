package com.ftmusic.hadise

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.mikhaellopez.circularimageview.CircularImageView
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

class AppListAdapter(val context:Context,
                     val AppNameList:ArrayList<String>,
                     val packageNameList:ArrayList<String>,
                     val imageNameList:ArrayList<String>): BaseAdapter() {

    private val imagesUrl:String = "https://storage.googleapis.com/androidfilespace/music_app_sources/images/"
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


    override fun getView(position: Int, vv: View?, parent: ViewGroup?): View {
            val convertView:View?
            convertView = inflater.inflate(R.layout.item_rightlist, parent, false)
            val tvMusic:TextView = convertView.findViewById(R.id.appNameId)
            val imgAppIcon: CircularImageView = convertView.findViewById(R.id.appIconId)
            Picasso.get()
                .load(imagesUrl + imageNameList.get(position))
                .placeholder(R.mipmap.ic_placeholder_round)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(imgAppIcon, object : Callback {
                    override fun onSuccess() {

                    }

                    override fun onError(e: Exception) {
                        Picasso.get()
                            .load(imagesUrl + imageNameList.get(position))
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                            .placeholder(R.mipmap.ic_placeholder_round)
                            .into(imgAppIcon)
                    }
                })

            tvMusic.setText(AppNameList.get(position))

        return convertView

    }

    override fun getItem(p0: Int): Any {
        return AppNameList.get(p0)
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return AppNameList.size
    }



}
