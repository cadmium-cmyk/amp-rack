package com.shajikhan.ladspa.amprack.data.model

data class ConnectTip(
    val id: String,
    val title: String,
    val description: String,
    val icon: String = "cable",
    val link: String? = null
)
