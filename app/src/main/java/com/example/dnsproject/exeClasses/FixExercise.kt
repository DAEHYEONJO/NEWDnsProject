package com.example.dnsproject.exeClasses

import java.io.Serializable

class FixExercise():Serializable {
    var benchPressCount=0
    var shoulderPressCount=0
    var barbellCurlsCount=0
    var deadLiftCount=0
    var squatCount=0

    fun setExerciseCount(name:String,count:Int){
        when(name){
            "benchpress"->{benchPressCount=count}
            "shoulderpress"->{shoulderPressCount=count}
            "barbellcurls"->{barbellCurlsCount=count}
            "deadlift"->{deadLiftCount=count}
            "squat"->{squatCount=count}
        }
    }

}