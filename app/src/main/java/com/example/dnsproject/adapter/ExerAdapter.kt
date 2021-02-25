package com.example.dnsproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dnsproject.R
import com.example.dnsproject.exeClasses.Routine
import kotlinx.android.synthetic.main.row_exer.view.*

class ExerAdapter(val mData: ArrayList<Routine>, private val layoutInflater: LayoutInflater) :
    RecyclerView.Adapter<ExerAdapter.ViewHolder>() {

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var routineName:TextView
        init {
            routineName=itemView.findViewById(R.id.routineName)
            val position=adapterPosition
            itemView.setOnClickListener {

            }
        }
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = layoutInflater.inflate(R.layout.row_exer, parent, false)
        return ViewHolder(view)
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.routineName.text =mData[position].name
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    override fun getItemCount(): Int {
        return mData.size
    }

}