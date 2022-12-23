package com.example.HangiBitki

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.HangiBitki.model.UserModelClass
import kotlinx.android.synthetic.main.item_user_layout.view.*

class UserAdapter(val context: Context, val items: ArrayList<UserModelClass>) :
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_user_layout,
                parent,
                false
            )
        )
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = items.get(position)

        holder.tvBitkiAdi.text = item.bitkiAdi
        holder.tvSicaklik.text = item.sicaklik
        holder.tvSulama.text = item.sulama
        holder.tvToprakTuru.text = item.toprakTuru
        holder.tvGunesIsigi.text = item.gunesIsigi
        holder.tvSaksiDegisim.text = item.saksiDegisim
        holder.tvDestekBesin.text = item.destekBesin
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return items.size
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Holds the TextView that will add each item to
        val tvBitkiAdi = view.tv_bitki_adi
        val tvSicaklik = view.tv_sicaklik
        val tvSulama = view.tv_sulama
        val tvToprakTuru = view.tv_toprak_turu
        val tvGunesIsigi = view.tv_gunes_isigi
        val tvSaksiDegisim = view.tv_saksi_degisim
        val tvDestekBesin = view.tv_destek_besin
    }
}