//Main.kt
package com.sunnygg95.mathplay

import de.fabmax.kool.KoolApplication
import de.fabmax.kool.KoolConfigJvm

fun main() = KoolApplication(
    config = KoolConfigJvm(
        windowTitle = "Kotlin Geometric Algebra"
    )
) {
    stagedScene(ctx)
}