package com.ttchain.githubusers.ui.bank

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ttchain.githubusers.R

class BankAdapter : RecyclerView.Adapter<BankAdapter.BankViewHolder>() {

    private var dataSet: MutableList<String> = arrayListOf()

    fun updateData(dataSet: List<String>) {
        this.dataSet.addAll(dataSet)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) =
        BankViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_bank, parent, false)
        )

    override fun getItemCount() = dataSet.size

    override fun onBindViewHolder(holder: BankViewHolder, position: Int) {
        dataSet.let { holder.setData(it[position]) }
    }

    inner class BankViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun setData(data: String) {
            itemView.apply {
//                textViewName.text = data.login
//                Glide.with(imageView)
//                    .load(data.avatarUrl)
//                    .apply(RequestOptions.circleCropTransform())
//                    .into(imageView)
//                if (data.siteAdmin) {
//                    textViewSiteAdmin.visibility = View.VISIBLE
//                    textViewSiteAdmin.text = "Admin"
//                } else {
//                    textViewSiteAdmin.visibility = View.GONE
//                }
            }
        }
    }
}