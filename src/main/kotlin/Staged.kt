//Staged.kt
package com.sunnygg95.mathplay

import de.fabmax.kool.KoolContext
import de.fabmax.kool.scene.defaultOrbitCamera
import de.fabmax.kool.scene.scene
import de.fabmax.kool.util.Color
import de.fabmax.kool.util.launchOnMainThread


fun stagedScene(ctx: KoolContext) = launchOnMainThread {
    //define multivector
    val a = Multivector(x=0.5,y=0.5,z=0.5)
    val aScaled = a*2.0
    val b = Multivector(x=1.0,z=1.0)
    val c = Multivector(y=1.0,xyz=8.0)

    //operator
    println(aScaled)
    println(3.0*b)
    println(a*b)
    println(a.outer(b))
    println(a.inner(b))
    println(a.leftCon(b))
    println(a.rightCon(b))
    println(a.reverse())

    //visualization
    ctx.scenes += scene {
        defaultOrbitCamera()
        defaultStage(this)
        a.drawVector(this,zero,Color.LIGHT_GRAY) //zero is built-in multivector, use as "base" to put object
        b.drawVector(this,c,Color.LIGHT_RED)
        a.biFromVector(this,b,zero, Color.CYAN)
        c.trivectorDraw(this)
    }
}