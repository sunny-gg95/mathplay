//Main.kt
package com.sunnygg95.mathplay

import de.fabmax.kool.KoolApplication
import de.fabmax.kool.KoolConfigJvm
import java.io.OutputStream
import java.io.PrintStream
import java.io.ByteArrayOutputStream
import org.lwjgl.glfw.GLFW

fun main() {

    if (GLFW.glfwInit()) {
        // Sets the class name for XWayland fallback and native Wayland app_id
        GLFW.glfwWindowHintString(GLFW.GLFW_X11_CLASS_NAME, "mathplay")
        GLFW.glfwWindowHintString(GLFW.GLFW_WAYLAND_APP_ID, "mathplay")
    }

    val originalErr = System.err
    System.setErr(PrintStream(object : OutputStream() {
        private val buffer = ByteArrayOutputStream()
        private val currentBlock = mutableListOf<String>()
        private var collectingBlock = false

        override fun write(b: Int) {
            if (b == '\n'.code) {
                val line = buffer.toString("UTF-8").trimEnd('\r')
                buffer.reset()
                processLine(line)
            } else {
                buffer.write(b)
            }
        }

        private fun processLine(line: String) {
            val trimmed = line.trim()

            if (trimmed.isEmpty()) {
                if (collectingBlock) currentBlock.add(line) else originalErr.println(line)
                return
            }

            // If a line starts with [LWJGL], it's the beginning of an error report block
            if (line.startsWith("[LWJGL]")) {
                flushBlock()
                collectingBlock = true
                currentBlock.add(line)
            }
            // If we are tracking an error block and the line is indented, it's part of the stacktrace
            else if (collectingBlock && (line.startsWith(" ") || line.startsWith("\t"))) {
                currentBlock.add(line)
            }
            // Normal system error text completely unrelated to LWJGL errors
            else {
                flushBlock()
                originalErr.println(line)
            }
        }

        private fun flushBlock() {
            if (!collectingBlock) return

            // Check if this specific block is the harmless Wayland layout warning
            val isWaylandSpam = currentBlock.any {
                it.contains("Wayland: Failed to encode keysym") ||
                it.contains("Wayland: The platform does not provide the window position") ||
                it.contains("Wayland: The platform does not support setting the window icon")
            }

            if (!isWaylandSpam) {
                // If it's a legitimate driver/context error, make sure you see it!
                for (blockLine in currentBlock) {
                    originalErr.println(blockLine)
                }
            }

            currentBlock.clear()
            collectingBlock = false
        }

        override fun close() {
            flushBlock()
            super.close()
        }
    }, true))

    KoolApplication(
        config = KoolConfigJvm(
            windowTitle = "Kotlin Geometric Algebra"
        )
    ) {
        createStaged(ctx)
    }
}