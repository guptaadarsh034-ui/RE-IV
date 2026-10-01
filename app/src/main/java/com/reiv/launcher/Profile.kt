package com.reiv.launcher

import android.content.Context
import org.json.JSONObject

data class Profile(
    val name: String,
    val resolution: String,
    val driver: String,
    val dxvk: Boolean,
    val box64Preset: String,
    val cpuAffinity: String,
    val fpsCap: Int,
    val memoryLimitMb: Int,
    val notes: String
) {
    fun summary() = """
        $name
        Resolution: $resolution
        Driver: $driver (DXVK: $dxvk)
        Box64 preset: $box64Preset
        CPU affinity: $cpuAffinity
        FPS cap: $fpsCap
        Memory limit: $memoryLimitMb MB
        $notes
    """.trimIndent()

    companion object {
        fun load(ctx: Context, file: String): Profile {
            val text = ctx.assets.open("profiles/$file").bufferedReader().use { it.readText() }
            val j = JSONObject(text)
            return Profile(
                j.getString("name"), j.getString("resolution"), j.getString("graphics_driver"),
                j.getBoolean("dxvk"), j.getString("box64_preset"), j.getString("cpu_affinity"),
                j.getInt("fps_cap"), j.getInt("memory_limit_mb"), j.optString("notes")
            )
        }
    }
}
