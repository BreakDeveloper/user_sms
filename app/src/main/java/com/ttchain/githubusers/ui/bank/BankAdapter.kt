package com.ttchain.githubusers.ui.bank

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ttchain.githubusers.R
import com.ttchain.githubusers.data.Order
import com.ttchain.githubusers.date
import com.ttchain.githubusers.format
import com.ttchain.githubusers.p4Str
import kotlinx.android.synthetic.main.item_bank.view.*

class BankAdapter : RecyclerView.Adapter<BankAdapter.BankViewHolder>() {

    private var dataSet: MutableList<Order> = arrayListOf()

    fun updateData(dataSet: List<Order>) {
        this.dataSet.clear()
        this.dataSet.addAll(dataSet)
        notifyDataSetChanged()
    }

    var clickProveCallback: ((String) -> Unit)? = null

    var confirmCallback: ((Order) -> Unit)? = null

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
        fun setData(order: Order) {
            itemView.apply {
                order.orderNo?.let { order_id.text = "訂單ID： $it" }
                order.currency?.let { currency_type.text = "付款金額($it)：" }
                order.currencyAmount?.let { currency_value.text = it.p4Str() }
                order.paymentOn?.let { date.text = "時間：${it.date()?.format()}" }
                order.payeeBankName?.let { bank_name.text = "收款銀行：$it" }
                order.payeeBankAccountNo?.let { bank_account.text = "收款帳號：$it" }
                order.payeeName?.let { user_name.text = "收款戶名：$it" }
                order.payerName?.let { pay_user_name.text = it }

                if (order.paymentVouchers != null) {
                    when (order.paymentVouchers?.size) {
                        2 -> {
                            prove1.visibility = View.VISIBLE
                            prove2.visibility = View.VISIBLE

                            prove1.setOnClickListener { clickProveCallback?.invoke(order.paymentVouchers!![0]) }
                            prove2.setOnClickListener { clickProveCallback?.invoke(order.paymentVouchers!![1]) }
                        }
                        1 -> {
                            prove1.visibility = View.VISIBLE
                            prove2.visibility = View.GONE
                            prove1.setOnClickListener { clickProveCallback?.invoke(order.paymentVouchers!![0]) }
                            prove2.setOnClickListener(null)
                        }
                        else -> {
                            prove1.visibility = View.GONE
                            prove2.visibility = View.GONE
                            prove1.setOnClickListener(null)
                            prove2.setOnClickListener(null)
                        }
                    }
                } else {
                    prove1.visibility = View.GONE
                    prove2.visibility = View.GONE
                    prove1.setOnClickListener(null)
                    prove2.setOnClickListener(null)
                }

                confirm_button.setOnClickListener { confirmCallback?.invoke(order) }
            }
        }
    }
}