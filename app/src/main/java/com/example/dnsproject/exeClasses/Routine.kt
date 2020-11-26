package com.example.dnsproject.exeClasses

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class Routine(
    var name: String?,
    var exerciseList: ArrayList<Exercise>
):Serializable{

}