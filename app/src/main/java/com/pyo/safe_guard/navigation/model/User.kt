package com.pyo.safe_guard.navigation.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(val uid: String, val username: String): Parcelable {
    constructor() : this("", "")
}