package com.example.alarmapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.alarmapp.MyAlarmReceiver.Constant.Companion.CHANNEL_ID
import com.example.alarmapp.MyAlarmReceiver.Constant.Companion.NOTIFICATION_ID
import java.util.*


class MyAlarmReceiver: BroadcastReceiver() {
    lateinit var notificationManager : NotificationManager
    private var textToSpeech: TextToSpeech? = null
    private var isTTSInitialized = false

    class Constant {
        companion object{
            const val NOTIFICATION_ID = 0
            const val CHANNEL_ID = "notification_channel"
            const val ALARM_TIMER = 10
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
       notificationManager = context.getSystemService(
           Context.NOTIFICATION_SERVICE
       ) as NotificationManager
        val todo = intent.getStringExtra("todo")!!
        Log.d("todo",todo)
        createNotificationChannel()
        deliverNotification(context,todo)
        setAlarm(context,todo)
    }

    private fun createNotificationChannel(){ // 체널 등록 (Oreo 버전 이상부터는 알림을 띄워주기 위해서는 channel 등록 필요)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "channel name",
                NotificationManager.IMPORTANCE_HIGH
                /*
                   1. IMPORTANCE_HIGH = 알림음이 울리고 헤드업 알림으로 표시
                   2. IMPORTANCE_DEFAULT = 알림음 울림
                   3. IMPORTANCE_LOW = 알림음 없음
                   4. IMPORTANCE_MIN = 알림음 없고 상태줄 표시 X
                    */
            )
            notificationChannel.enableLights(true) // 불빛 // 적용이 안됨
            notificationChannel.lightColor = Color.BLUE // 색상  // 적용이 안됨
            notificationChannel.enableVibration(true) // 진동
            notificationChannel.description = "channel description" // 채널 정보
            notificationChannel.vibrationPattern = longArrayOf(100, 200, 100, 200)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
    private fun deliverNotification(context: Context, todo: String){ // 알림 등록
        val contentIntent = Intent(context, MainActivity::class.java)
        var contentPendingIntent : PendingIntent
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            contentPendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID, // RequestCode
                contentIntent, // 알림 클릭 시 이동할 인텐트
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                /*
               1. FLAG_UPDATE_CURRENT : 현재 PendingIntent를 유지하고, 대신 인텐트의 extra data는 새로 전달된 Intent로 교체
               2. FLAG_CANCEL_CURRENT : 현재 인텐트가 이미 등록되어있다면 삭제, 다시 등록
               3. FLAG_NO_CREATE : 이미 등록된 인텐트가 있다면, null
               4. FLAG_ONE_SHOT : 한번 사용되면, 그 다음에 다시 사용하지 않음
                */
            )
        }else{
            contentPendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID, // RequestCode
                contentIntent, // 알림 클릭 시 이동할 인텐트
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        val title = "Todo"
        val content = "$todo 할 시간입니다"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_alarm_on_24) // 아이콘
            .setContentTitle(title) // 제목
            .setContentText(content) // 내용
            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
    fun playAlarm(string: String) {
        Log.d("here","here")
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {}

            override fun onDone(utteranceId: String) {
                    shutdownTTS()
            }
            override fun onError(utteranceId: String) {}
        })
        textToSpeech?.speak(string, TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID)
    }
    private fun shutdownTTS() {
        // TTS 엔진이 초기화된 경우에만 종료합니다.
        if (isTTSInitialized) {
            Log.d("shutdown","shutdown!")
            textToSpeech!!.shutdown()
            isTTSInitialized = false
        }
    }
    fun setAlarm(context: Context,todo:String) {
        textToSpeech = TextToSpeech(context, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                isTTSInitialized = true
                val result = textToSpeech!!.setLanguage(Locale.KOREAN)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS","해당언어는 지원되지 않습니다.")
                    return@OnInitListener
                }else{
                    val string = "안녕하세요 $todo 할 시간입니다"
                    playAlarm(string)
                }
            }
        })
    }

}