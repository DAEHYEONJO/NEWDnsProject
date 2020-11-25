package com.example.dnsproject

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.view.View
import kotlinx.android.synthetic.main.make_layout.*

open class DialogFrag(context: Context) : Dialog(context) {
    open val layoutResourceId = R.layout.make_layout

    open class Builder(val mContext: Context) {
        open val dialog = DialogFrag(mContext)
//        open val binding:WidgetBaseDialogBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext),layoutResourceId,null,false)

        open val builder = this
        open fun create(): Builder {
            dialog.create()
            dialog.setContentView(dialog.layoutResourceId)

            return builder
        }


        open fun setOkButton(onClick: View.OnClickListener): Builder {
            dialog.btnOk?.setOnClickListener(onClick)
            dialog.btnOk?.visibility = View.VISIBLE
            return builder
        }


        fun dismissDialog() {
            dialog.dismiss()
        }
        open fun show(): DialogFrag {
            dialog.show()
            return dialog
        }


    }
}