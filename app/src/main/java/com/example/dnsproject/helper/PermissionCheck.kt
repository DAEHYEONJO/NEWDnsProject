package com.example.dnsproject.helper

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionCheck(private val permissionActivity: Activity, private val requirePermissions:Array<String>) {
    private val permissionRequestCode=100
    init {
        var failRequestPermissionList=ArrayList<String>()
        for(permission in requirePermissions){
            if(ContextCompat.checkSelfPermission(permissionActivity.applicationContext,permission)!=PackageManager.PERMISSION_GRANTED){
                failRequestPermissionList.add(permission)
            }
        }
        if (failRequestPermissionList.isNotEmpty()){
            val array= arrayOfNulls<String>(failRequestPermissionList.size)
            ActivityCompat.requestPermissions(permissionActivity,failRequestPermissionList.toArray(array),permissionRequestCode)
        }
    }
}