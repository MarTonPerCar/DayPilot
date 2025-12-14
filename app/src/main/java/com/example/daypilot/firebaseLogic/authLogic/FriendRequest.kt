package com.example.daypilot.firebaseLogic.authLogic

data class FriendRequest(
    val fromUid: String = "",
    val fromUsername: String = "",
    val fromName: String = "",
    val since: Long = 0L
)