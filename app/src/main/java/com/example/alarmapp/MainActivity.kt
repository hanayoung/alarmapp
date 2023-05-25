package com.example.alarmapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.alarmapp.MyAlarmReceiver.Constant.Companion.NOTIFICATION_ID
import com.example.alarmapp.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.min

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var textToSpeech: TextToSpeech? = null
    private var isTTSInitialized = false
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestNotificationPermission()
        val alarmManager: AlarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, MyAlarmReceiver::class.java)
//        val pendingIntent = PendingIntent.getBroadcast(
//            this,
//            NOTIFICATION_ID,
//            intent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )

        binding.switch01.setOnCheckedChangeListener { _, check ->
//            val toastMessage = if (check) {
//                val triggerTime = (SystemClock.elapsedRealtime() + ALARM_TIMER * 1000)
//                alarmManager.set(
//                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
//                    triggerTime,
//                    pendingIntent
//                ) // set : 일회성 알림
//                "$ALARM_TIMER 초 후에 알림이 발생합니다."
//            } else {
//                alarmManager.cancel(pendingIntent)
//                "알림 예약을 취소하였습니다."
//            }
            /*
            1. ELAPSED_REALTIME : ELAPSED_REALTIME 사용. 절전모드에 있을 때는 알람을 발생시키지 않고 해제되면 발생시킴.
            2. ELAPSED_REALTIME_WAKEUP : ELAPSED_REALTIME 사용. 절전모드일 때도 알람을 발생시킴.
            3. RTC : Real Time Clock 사용. 절전모드일 때는 알람을 발생시키지 않음.
            4. RTC_WAKEUP : Real Time Clock 사용. 절전모드 일 때도 알람을 발생시킴.
             */

//            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
        }
        binding.switch02.setOnCheckedChangeListener { _, check ->
            var toastMessage : String
            if(check){
                viewModel.getData()
                var ts : Time
                viewModel.timeList.observe(this, Observer {
                    if(viewModel.timeList.value!!.isNotEmpty()){
                        Log.d("not empty",viewModel.timeList.value.toString())
                        ts = viewModel.timeList.value!![viewModel.timeList.value!!.size-1]
                        val dateString = ts.time
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")
                        val dateTime = LocalDateTime.parse(dateString, formatter)
                        var date = dateTime.dayOfMonth
                        val hour = dateTime.hour
                        val minute = dateTime.minute
                        val curMoment = LocalDateTime.now()
                        val curMon = curMoment.month
                        val curDate = curMoment.dayOfMonth
                        val curHour = curMoment.hour
                        val curMin = curMoment.minute

                        if(curMon==dateTime.month&&curDate == dateTime.dayOfMonth){
                            if(curHour>hour){
                                date+=1
                            }else if(curHour == hour){
                                if(curMin>=minute)
                                    date+=1
                            }
                        } // 추청으로 현재 시간보다 이전의 알람을 설정할 경우 바로 알람이 실행되는 오류가 있는 것 같아서 오류잡기위한 분기처리

                        if(hour!=-1 && minute!=-1){
                            val calendar = Calendar.getInstance().apply {
                                timeInMillis = System.currentTimeMillis()
                                set(Calendar.DAY_OF_MONTH,date)
                                set(Calendar.HOUR_OF_DAY,hour)
                                set(Calendar.MINUTE, minute)
                                set(Calendar.SECOND,0)
                            }
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            dateFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                            val formattedDateTime = dateFormat.format(calendar.timeInMillis)
                            Log.d("시간:",formattedDateTime.toString())
                            intent.putExtra("hour",hour)
                            intent.putExtra("min",minute)
                            intent.putExtra("todo",ts.todo)
                            val pendingIntent = PendingIntent.getBroadcast(
                                this,
                                NOTIFICATION_ID,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                calendar.timeInMillis,
                                pendingIntent)
                        }
                    }
                })
                toastMessage =  "알림이 발생합니다."
            }
            else {
                val pendingIntent = PendingIntent.getBroadcast(
                    this,
                    NOTIFICATION_ID,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
                toastMessage = "알림 예약을 취소하였습니다."
            }
            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
        }
    }
    private val requestNotificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { ok ->
            if (ok) {
                // 알림권한 허용 o
            } else {
                // 알림권한 허용 x. 자유롭게 대응..
            }
        }

    fun requestNotificationPermission() {
        if (
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // 다른 런타임 퍼미션이랑 비슷한 과정
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                    // 왜 알림을 허용해야하는지 유저에게 알려주기를 권장
                } else {
                    requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                // 안드로이드 12 이하는 알림에 런타임 퍼미션 없으니, 설정가서 켜보라고 권해볼 수 있겠다.
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}