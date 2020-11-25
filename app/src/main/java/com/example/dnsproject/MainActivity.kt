package com.example.dnsproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.dnsproject.exeClasses.Exercise
import com.example.dnsproject.exeClasses.Routine
import com.example.dnsproject.helper.PermissionCheck

class MainActivity : AppCompatActivity() {
    //private val filePath="/sdcard/dnsTTS/10_seconds.pcm"
    private val requestPermission=arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PermissionCheck(
            this@MainActivity,
            requestPermission
        )

    }

}