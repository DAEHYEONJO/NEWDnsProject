package com.example.dnsproject.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dnsproject.R
import com.example.dnsproject.exeClasses.Exercise
import kotlinx.android.synthetic.main.saved_exer_row.view.*

class MakeAdapter(var mData: ArrayList<Exercise>,var layoutInflater: LayoutInflater) :
    RecyclerView.Adapter<MakeAdapter.ViewHolder>() {

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        lateinit var exeName:TextView
        lateinit var exeWeight:TextView
        lateinit var exeCount:TextView
        lateinit var exeSetCount:TextView

        init {
            exeName=itemView.findViewById(R.id.exeName)
            exeWeight=itemView.findViewById(R.id.exeWeight)
            exeCount=itemView.findViewById(R.id.exeCount)
            exeSetCount=itemView.findViewById(R.id.exeSetCount)

        }
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = layoutInflater.inflate(R.layout.saved_exer_row, parent, false)
        return ViewHolder(view)
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.exeName.text=mData[position].name
        holder.itemView.exeWeight.text=mData[position].weight+"kg"
        holder.itemView.exeCount.text=mData[position].count+"회"
        holder.itemView.exeSetCount.text=mData[position].setCount+"세트"
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    override fun getItemCount(): Int {
        return mData!!.size
    }

}