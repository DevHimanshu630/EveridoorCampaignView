package com.everidoor.campaign

data class Campaign(
    val campaignId: String,
    val campaignName: String,
    val fromDate: String,
    val toDate: String,
    val noOfScreens: Int,
    val screens: List<Screen>,
    //val screenId: String,
    val username: String
)

