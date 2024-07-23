package com.mario.datetimenotificationapp

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mario.datetimenotificationapp.ui.theme.DatetimeNotificationAppTheme
import java.text.SimpleDateFormat
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DatetimeNotificationAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    createNotificationChannel(LocalContext.current)
                    DateTime(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTime(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        var timeButton by remember { mutableStateOf("Choose a time") }
        var dateButton by remember { mutableStateOf("Choose a date") }
        var isTimePickerShown by remember { mutableStateOf(false) }
        var isDatePickerShown by remember { mutableStateOf(false) }
        var hour by remember { mutableIntStateOf(0) }
        var minute by remember { mutableIntStateOf(0) }
        val context = LocalContext.current
        var timeInMillis by remember { mutableLongStateOf(0) }

        if (isDatePickerShown) {
            DatePickerChooser(onConfirm = { dateState ->
                val c = Calendar.getInstance()
                c.timeInMillis = dateState.selectedDateMillis ?: 0
                timeInMillis = dateState.selectedDateMillis ?: 0
                val dateFormatter = SimpleDateFormat("dd-MM-yyyy", java.util.Locale.US)
                dateButton = dateFormatter.format(c.time)
                isDatePickerShown = false
            }, onDismiss = { isDatePickerShown = false })
        }
        if (isTimePickerShown) {
            TimePickerChooser(onConfirm = { timeState ->
                hour = timeState.hour
                minute = timeState.minute
                timeButton = "$hour:$minute"
                isTimePickerShown = false
                timeInMillis += (hour * 3600 * 1000) + (minute * 60 * 1000) - (3 * 3600 * 1000)
            }, onDismiss = { isTimePickerShown = false })
        }
        OutlinedButton(onClick = { isTimePickerShown = true }) { Text(text = timeButton) }
        OutlinedButton(onClick = { isDatePickerShown = true }) { Text(text = dateButton) }
        OutlinedButton(onClick = {
            sendNotification(
                title = "Notification Scheduled",
                text = "Your notification is scheduled on $dateButton $timeButton",
                context = context
            )
            scheduleNotification(
                context = context,
                timeInMillis = timeInMillis
            )
        }) {
            Text(text = "Send notification")
        }


    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerChooser(
    modifier: Modifier = Modifier, onConfirm: (TimePickerState) -> Unit, onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(is24Hour = true)
    AlertDialog(
        title = { TimePicker(state = timePickerState) },
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(onClick = { onConfirm(timePickerState) }) {
                Text(text = "OK")

            }

        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = "Cancel")
            }
        })

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerChooser(onConfirm: (DatePickerState) -> Unit, onDismiss: () -> Unit) {
    val datePickerState = rememberDatePickerState()
    AlertDialog(
        title = { DatePicker(state = datePickerState) },
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(onClick = { onConfirm(datePickerState) }) {
                Text(text = "OK")

            }

        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = "Cancel")
            }
        })

}

private fun createNotificationChannel(context: Context) {
    val name = "DateTime Channel"
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val channel = NotificationChannel("1", name, importance)
    channel.description = "DataTime Scheduled Notification"
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.createNotificationChannel(channel)

}

fun sendNotification(title: String, text: String, context: Context) {
    val builder = NotificationCompat.Builder(context, "1")
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(title)
        .setContentText(text)
        .setAutoCancel(true)
    NotificationManagerCompat.from(context).notify(99, builder.build())


}

fun scheduleNotification(context: Context, timeInMillis: Long) {
    val i = Intent(context, NotificationReceiver::class.java)
    i.putExtra("title", "New Notification")
    i.putExtra("text", "Your notification was scheduled successfully")

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        200,
        i,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    try {
        manager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timeInMillis,
            pendingIntent

        )
    } catch (e: SecurityException) {
        Log.d("trace", "Error: $e")
    }

}

@Preview(showBackground = true)
@Composable
fun DateTimePreview() {
    DateTime()
}