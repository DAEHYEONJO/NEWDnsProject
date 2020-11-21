package com.example.dnsproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_stop_watch.*
import java.util.*
import kotlin.concurrent.timer

class StopWatchActivity : AppCompatActivity() {
    private var timerTask: Timer? = null
    private var isRunning = false
    private var time = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stop_watch)

        onClick()
    }

    private fun onClick() {
        start.setOnClickListener {
            isRunning = !isRunning
            if (isRunning) start()
            else pause()
        }
        end.setOnClickListener { reset() }
    }

    private fun start() {
        timerTask = timer(period = 10) {
            time++
            val min = time / 6000
            var sec = time / 100
            sec %= 60
            var secString=if(sec in 0..9) {
               "0"+sec.toString()
            }else{
                sec.toString()
            }
            val milli = time % 100
            var minString = if (min in 0..9) {
                "0" + min.toString()
            } else {
                min.toString()
            }
            var milliString = if (milli in 0..9) {
                "0" + milli.toString()
            } else {
                milli.toString()
            }
            runOnUiThread {
                setUIText(minString, secString, milliString)
            }
        }
    }

    private fun pause() {
        timerTask?.cancel()
    }

    private fun reset() {
        timerTask?.cancel()
        time = 0
        isRunning = false
        setUIText("00", "00", "00")
    }

    private fun setUIText(min: String, sec: String, milli: String) {
        minTV.text = min
        secTV.text = sec
        milliTV.text = milli
    }
}