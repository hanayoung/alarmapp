package com.example.alarmapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel : ViewModel() {

    private val client = RetrofitInstance.getInstance().create(TimeApi::class.java)

    private val _timeList = MutableLiveData<List<Time>>()
    val timeList : LiveData<List<Time>>
    get() = _timeList

    fun getData() {
        client.getAllData().enqueue(object : Callback<List<Time>> {
            override fun onResponse(call: Call<List<Time>>, response: Response<List<Time>>) {
                Log.d("response",response.body().toString())
                _timeList.value = response.body()
                Log.d("response",timeList.value.toString())
            }

            override fun onFailure(call: Call<List<Time>>, t: Throwable) {
                Log.d("failure",t.message.toString())
            }

        })
    }
}