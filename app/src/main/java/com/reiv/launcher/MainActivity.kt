package com.reiv.launcher

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        val prefs = getSharedPreferences("reiv", MODE_PRIVATE)
        val profile = Profile.load(this, "low_end.json")

        val pad = 32
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
            gravity = Gravity.CENTER_HORIZONTAL
        }
        val info = TextView(this).apply { text = profile.summary(); textSize = 15f }
        val pkg = EditText(this).apply {
            hint = "Container app package (e.g. com.winlator)"
            setText(prefs.getString("pkg", "com.winlator"))
        }
        val launch = Button(this).apply { text = "Launch container" }
        val status = TextView(this)

        launch.setOnClickListener {
            val p = pkg.text.toString().trim()
            prefs.edit().putString("pkg", p).apply()
            val intent: Intent? = packageManager.getLaunchIntentForPackage(p)
            if (intent == null) status.text = "App not installed: $p"
            else {
                status.text = "Launching... apply the profile above in the container settings."
                startActivity(intent)
            }
        }

        val touchTest = Button(this).apply { text = "Test touch layout" }
        touchTest.setOnClickListener {
            val v = TouchOverlayView(this)
            v.onStick = { x, y -> status.text = "stick %.2f, %.2f".format(x, y) }
            v.onButton = { n, d -> status.text = "button $n ${if (d) "down" else "up"}" }
            setContentView(FrameLayout(this).apply {
                addView(v)
                addView(status)
            })
        }

        root.addView(info); root.addView(pkg); root.addView(launch)
        root.addView(touchTest); root.addView(status)
        setContentView(ScrollView(this).apply { addView(root) })
    }
}
