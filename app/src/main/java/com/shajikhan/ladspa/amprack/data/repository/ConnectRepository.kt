package com.shajikhan.ladspa.amprack.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.shajikhan.ladspa.amprack.data.model.ConnectTip
import java.io.InputStreamReader

class ConnectRepository(private val context: Context) {
    fun getConnectTips(): List<ConnectTip> {
        val tips = mutableListOf<ConnectTip>()
        try {
            val stream = context.assets.open("connect.json")
            val reader = InputStreamReader(stream)
            val jsonObject = Gson().fromJson(reader, JsonObject::class.java)

            for (key in jsonObject.keySet()) {
                val obj = jsonObject.getAsJsonObject(key)
                val title = if (obj.has("title")) obj.get("title").asString else key
                val desc = if (obj.has("description")) obj.get("description").asString else ""
                val icon = if (obj.has("icon")) obj.get("icon").asString else "cable"
                val link = if (obj.has("link")) obj.get("link").asString else null

                tips.add(ConnectTip(key, title, desc, icon, link))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (tips.isEmpty()) {
            tips.addAll(getFallbackTips())
        }
        return tips
    }

    private fun getFallbackTips(): List<ConnectTip> {
        return listOf(
            ConnectTip("trs", "1/4\" TRS to 3.5mm Converter", "Use this converter to connect your guitar cable to the phone's 3.5mm audio port or splitter.", "trs"),
            ConnectTip("splitter", "3.5mm Mic/Earphone Splitter", "Connect guitar into the mic input and headphones into the headphone port for real-time monitoring.", "splitter"),
            ConnectTip("cable", "Guitar USB Cable (with built-in ADC)", "Plug standard 1/4\" jack into guitar and USB end into phone via USB OTG adapter.", "cable"),
            ConnectTip("otg", "USB OTG Adapter (Type-C to USB-A)", "Allows connecting USB audio interfaces and guitar cables directly into Android devices.", "otg"),
            ConnectTip("interface", "External USB Audio Interface", "Pro-grade portable audio interface (e.g., Focusrite, Behringer, iRig HD) for zero latency.", "interface")
        )
    }
}
