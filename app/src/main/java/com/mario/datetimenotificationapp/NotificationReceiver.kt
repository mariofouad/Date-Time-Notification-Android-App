package com.mario.datetimenotificationapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        sendNotification(
            title = intent!!.getStringExtra("title")!!,
            text = intent.getStringExtra("text")!!,
            context = context!!
        )
    }
}