package com.example.dnsproject.exeClasses

import java.io.Serializable

class FixExercise():Serializable {
    var benchPressCount=0
    var shoulderPressCount=0
    var barbellCurlsCount=0
    var deadLiftCount=0
    var squatCount=0
    var crunchCount=0
    var plankJumpCount=0
    var burpeeTestCount=0
    var lungeCount=0
    var pullUpCount=0

    fun setExerciseCount(name:String,count:Int){
        when(name){
            "benchpress"->{benchPressCount=count}
            "shoulderpress"->{shoulderPressCount=count}
            "barbellcurls"->{barbellCurlsCount=count}
            "deadlift"->{deadLiftCount=count}
            "squat"->{squatCount=count}
            "crunch"->{crunchCount=count}
            "plankJump"->{plankJumpCount=count}
            "burpee"->{burpeeTestCount=count}
            "lunge"->{lungeCount=count}
            "pullUp"->{pullUpCount=count}
        }
    }

}