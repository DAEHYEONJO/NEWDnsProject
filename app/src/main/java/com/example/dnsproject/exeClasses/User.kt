package com.example.dnsproject.exeClasses

import androidx.annotation.Keep
import java.io.Serializable


@Keep
class User(
    var id: String,
    var pw: String,
    var routine: ArrayList<Routine>,
    var fixExercise: FixExercise) : Serializable {

}