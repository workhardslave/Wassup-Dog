package com.pyo.safe_guard.navigation.model

class ChatModel (val myUid: String, val yourUid: String, val message: String, val time : Long, val who : String){
    constructor() : this("", "", "", 0, "")
}