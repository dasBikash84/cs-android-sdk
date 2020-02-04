package com.dasbikash.cs_android_sdk.example_app.model

import androidx.annotation.Keep

@Keep
internal data class User(
        var userId:String?=null,
        var firstName:String?=null,
        var lastName:String?=null,
        var isCustomerManager: Boolean=false,
        var isEndUser: Boolean=true
)