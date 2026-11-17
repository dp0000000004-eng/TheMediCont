package com.example.focusblocker

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class BlockerService : AccessibilityService() {

    // Tune these on your phone. Keep them specific, a bare "game" will match the whole Play Store home.
    private val blocked = listOf(
        "casino", "betting", "sports betting", "rummy", "fantasy cricket",
        "poker", "teen patti", "real money", "ludo", "battle royale"
    )

    private var lastBlock = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.packageName?.toString() != "com.android.vending") return
        val root = rootInActiveWindow ?: return

        val texts = mutableListOf<String>()
        collect(root, texts)
        val screen = texts.joinToString(" ").lowercase()

        val hasInstall = screen.contains("install")
        if (hasInstall && blocked.any { screen.contains(it) }) {
            val now = System.currentTimeMillis()
            if (now - lastBlock > 1500) {          // avoid repeat triggers
                lastBlock = now
                performGlobalAction(GLOBAL_ACTION_BACK)
                Toast.makeText(this, "Blocked. Stay focused.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun collect(node: AccessibilityNodeInfo?, out: MutableList<String>) {
        if (node == null) return
        node.text?.let { out.add(it.toString()) }
        node.contentDescription?.let { out.add(it.toString()) }
        for (i in 0 until node.childCount) collect(node.getChild(i), out)
    }

    override fun onInterrupt() {}
}
