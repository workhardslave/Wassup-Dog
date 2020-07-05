package com.pyo.safe_guard.navigation.util

import com.pyo.safe_guard.navigation.model.PushModel


import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

class FcmPush {

    var JSON = MediaType.parse("application/json; charset=utf-8")
    var url = "https://fcm.googleapis.com/fcm/send"
    var serverKey = "AIzaSyAxYZf9XAFiJ3ehEK9cXZrniIuHsHi2nFU"
    var gson : Gson? = null
    var okHttpClient : OkHttpClient? = null
    companion object{
        var instance = FcmPush()
    }

    init {
        gson = Gson()
        okHttpClient = OkHttpClient()
    }
    fun sendMessage(destinationUid : String, title : String, message : String){
        FirebaseFirestore.getInstance().collection("pushtokens").document(destinationUid).get().addOnCompleteListener {
                task ->
            if(task.isSuccessful){
                var token = task?.result?.get("pushToken").toString()

                var pushModel = PushModel()
                pushModel.to = token
                pushModel.notification.title = title
                pushModel.notification.body = message

                var body = RequestBody.create(JSON,gson?.toJson(pushModel))
                var request = Request.Builder()
                    .addHeader("Content-Type","application/json")
                    .addHeader("Authorization","key="+serverKey)
                    .url(url)
                    .post(body)
                    .build()

                okHttpClient?.newCall(request)?.enqueue(object : Callback{
                    override fun onFailure(call: Call?, e: IOException?) {

                    }

                    override fun onResponse(call: Call?, response: Response?) {
                        println(response?.body()?.string())
                    }

                })
            }
        }
    }
}