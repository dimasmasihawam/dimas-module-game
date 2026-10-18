package com.dimas.gamemodule

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import rikka.shizuku.Shizuku

class MainActivity : Activity() {

    private val sandi = "DIMAS GANTENG"
    private val reqCode = 100

    private val games = listOf(
        "com.dts.freefireth",
        "com.dts.freefiremax",
        "com.mobile.legends",
        "com.tencent.ig"
    )
    private val killApps = listOf(
        "com.zhiliaoapp.musically",
        "com.ss.android.ugc.trill",
        "com.instagram.android",
        "com.facebook.katana",
        "com.spotify.music"
    )

    private var mainShown = false
    private lateinit var statusView: TextView
    private lateinit var logView: TextView
    private lateinit var scroll: ScrollView
    private lateinit var btnPerm: Button
    private lateinit var btnInject: Button
    private lateinit var btnReset: Button

    private val permListener = Shizuku.OnRequestPermissionResultListener { _, _ ->
        runOnUiThread { refresh() }
    }
    private val binderListener = Shizuku.OnBinderReceivedListener {
        runOnUiThread { refresh() }
    }
    private val deadListener = Shizuku.OnBinderDeadListener {
        runOnUiThread { refresh() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Shizuku.addRequestPermissionResultListener(permListener)
        Shizuku.addBinderReceivedListener(binderListener)
        Shizuku.addBinderDeadListener(deadListener)
        showLogin()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(permListener)
        Shizuku.removeBinderReceivedListener(binderListener)
        Shizuku.removeBinderDeadListener(deadListener)
        super.onDestroy()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun lp(top: Int = 0) = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply { topMargin = dp(top) }

    // ---------------- LOGIN ----------------
    private fun showLogin() {
        mainShown = false
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#121212"))
            setPadding(dp(24), dp(24), dp(24), dp(24))
        }
        val title = TextView(this).apply {
            text = "Dimas Game Module"
            setTextColor(Color.WHITE)
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        val input = EditText(this).apply {
            hint = "Masukkan sandi"
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
            isSingleLine = true
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val err = TextView(this).apply {
            setTextColor(Color.parseColor("#FF5252"))
            gravity = Gravity.CENTER
        }
        val btn = Button(this).apply { text = "MASUK" }
        btn.setOnClickListener {
            if (input.text.toString().trim().equals(sandi, ignoreCase = true)) {
                showMain()
            } else {
                err.text = "Sandi salah"
            }
        }
        root.addView(title, lp())
        root.addView(input, lp(24))
        root.addView(err, lp(8))
        root.addView(btn, lp(16))
        setContentView(root)
    }

    // ---------------- MAIN ----------------
    private fun showMain() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#121212"))
            setPadding(dp(16), dp(32), dp(16), dp(16))
        }
        val title = TextView(this).apply {
            text = "Dimas Game Module"
            setTextColor(Color.WHITE)
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
        }
        statusView = TextView(this).apply { textSize = 14f }
        btnPerm = Button(this)
        btnInject = Button(this).apply { text = "INJECT" }
        btnReset = Button(this).apply { text = "RESET KE DEFAULT" }
        logView = TextView(this).apply {
            setTextColor(Color.parseColor("#B0FFB0"))
            typeface = Typeface.MONOSPACE
            textSize = 12f
        }
        scroll = ScrollView(this)
        scroll.addView(logView)

        btnPerm.setOnClickListener {
            val alive = try { Shizuku.pingBinder() } catch (e: Throwable) { false }
            if (!alive) {
                val i = packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
                if (i != null) startActivity(i)
                else Toast.makeText(this, "Shizuku belum terinstall", Toast.LENGTH_LONG).show()
            } else {
                Shizuku.requestPermission(reqCode)
            }
        }
        btnInject.setOnClickListener { runTask(false) }
        btnReset.setOnClickListener { runTask(true) }

        root.addView(title, lp())
        root.addView(statusView, lp(12))
        root.addView(btnPerm, lp(12))
        root.addView(btnInject, lp(4))
        root.addView(btnReset, lp(4))
        root.addView(
            scroll,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f).apply { topMargin = dp(12) }
        )
        mainShown = true
        setContentView(root)
        refresh()
    }

    private fun refresh() {
        if (!mainShown) return
        val alive = try { Shizuku.pingBinder() } catch (e: Throwable) { false }
        if (!alive) {
            statusView.text = "Shizuku: BELUM AKTIF\nBuka app Shizuku lalu tekan Start."
            statusView.setTextColor(Color.parseColor("#FF5252"))
            btnPerm.text = "BUKA SHIZUKU"
            btnPerm.isEnabled = true
            btnInject.isEnabled = false
            btnReset.isEnabled = false
            return
        }
        val granted = try {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Throwable) { false }
        if (!granted) {
            statusView.text = "Shizuku aktif, izin untuk app ini belum diberikan."
            statusView.setTextColor(Color.parseColor("#FFC107"))
            btnPerm.text = "MINTA IZIN SHIZUKU"
            btnPerm.isEnabled = true
            btnInject.isEnabled = false
            btnReset.isEnabled = false
            return
        }
        statusView.text = "Shizuku: SIAP"
        statusView.setTextColor(Color.parseColor("#4CAF50"))
        btnPerm.text = "SHIZUKU OK"
        btnPerm.isEnabled = false
        btnInject.isEnabled = true
        btnReset.isEnabled = true
    }

    private fun log(s: String) {
        runOnUiThread {
            logView.append(s + "\n")
            scroll.post { scroll.fullScroll(ScrollView.FOCUS_DOWN) }
        }
    }

    // Jalankan perintah lewat Shizuku (level shell, tanpa root)
    private fun sh(cmd: String): Pair<Int, String> {
        return try {
            val m = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            m.isAccessible = true
            val p = m.invoke(null, arrayOf("sh", "-c", "$cmd 2>&1"), null, null) as Process
            val out = p.inputStream.bufferedReader().readText()
            Pair(p.waitFor(), out)
        } catch (e: Throwable) {
            Pair(-1, e.toString())
        }
    }

    private fun runTask(reset: Boolean) {
        btnInject.isEnabled = false
        btnReset.isEnabled = false
        logView.text = ""
        Thread {
            fun run(c: String) {
                val (code, out) = sh(c)
                if (code == 0) {
                    log("[OK]    $c")
                } else {
                    log("[GAGAL] $c")
                    if (out.isNotBlank()) log("   -> " + out.trim().take(120))
                }
            }

            val sdk = sh("getprop ro.build.version.sdk").second.trim()
            log(if (reset) "== RESET (SDK $sdk) ==" else "== INJECT (SDK $sdk) ==")
            val pkgs = sh("pm list packages").second.lines().map { it.trim() }
            fun installed(p: String) = pkgs.contains("package:$p")

            if (reset) {
                run("settings put global window_animation_scale 1.0")
                run("settings put global transition_animation_scale 1.0")
                run("settings put global animator_duration_scale 1.0")
                for (g in games) {
                    if (!installed(g)) continue
                    run("cmd game mode standard $g")
                    run("cmd deviceidle whitelist -$g")
                }
                log("Selesai. Refresh rate atur manual di Settings > Display.")
            } else {
                log("-- Display & animasi")
                run("settings put system peak_refresh_rate 120.0")
                run("settings put system user_refresh_rate 120.0")
                run("settings put system min_refresh_rate 60.0")
                run("settings put global window_animation_scale 0.5")
                run("settings put global transition_animation_scale 0.5")
                run("settings put global animator_duration_scale 0.5")

                log("-- Per game")
                for (g in games) {
                    if (!installed(g)) {
                        log("[SKIP]  $g (tidak terinstall)")
                        continue
                    }
                    run("cmd game mode performance $g")
                    run("cmd deviceidle whitelist +$g")
                    run("cmd appops set $g RUN_ANY_IN_BACKGROUND allow")
                    run("cmd appops set $g RUN_IN_BACKGROUND allow")
                }

                log("-- Bebaskan RAM")
                for (k in killApps) {
                    if (!installed(k)) continue
                    run("am force-stop $k")
                }
                log("Selesai. Buka game sekarang.")
            }
            runOnUiThread { refresh() }
        }.start()
    }
}
