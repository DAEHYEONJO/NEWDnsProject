package com.example.dnsproject

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.saved_exer_row.view.*

class MakeAdapter(var mData: ArrayList<String>?) :
    RecyclerView.Adapter<MakeAdapter.ViewHolder>() {

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {


        init {

            // 뷰 객체에 대한 참조. (hold strong reference)

        }
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.saved_exer_row, parent, false)
        return ViewHolder(view)
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.savedText.text = mData!![position].toString()
        holder.itemView.setOnClickListener {
            //Toast.makeText(holder.itemView.context, holder.itemView.exerName.text, Toast.LENGTH_SHORT).show()
        }
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    override fun getItemCount(): Int {
        return mData!!.size
    }

}