//Staged.kt
package com.sunnygg95.mathplay

import de.fabmax.kool.Assets
import de.fabmax.kool.KoolContext
import de.fabmax.kool.loadTexture2d
import de.fabmax.kool.scene.defaultOrbitCamera
import de.fabmax.kool.scene.scene
import de.fabmax.kool.util.Color
import de.fabmax.kool.util.launchOnMainThread
import de.fabmax.kool.pipeline.ClearColorFill
import de.fabmax.kool.util.Time
import de.fabmax.kool.util.debugOverlay
import de.fabmax.kool.scene.Node
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.PI
import kotlin.math.sqrt

fun createStaged(ctx: KoolContext) = launchOnMainThread {
    val pic = Assets.loadTexture2d("return-90.png").getOrThrow()
    val pic2 = Assets.loadTexture2d("fliped-return-90.png").getOrThrow()
    //   ,picture=pic,picture2=pic2
    val blade = Multivector(zx = -1)
    val v1 = Multivector(x=1)
    val v2 = Multivector(z=1)
    ctx.scenes += scene {
        mainRenderPass.clearColor = ClearColorFill(Color.BLACK)
        defaultOrbitCamera()
        updateTransparentOrdering()
        //setupDefaultStage()


        if (false) {
            fun Path(t: Double): Multivector {
                return Multivector(x = cos(t), y = 0, z = sin(t))
            }

            drawPath({t -> Path(t)},0.0,2*PI)

            drawField(
                -2.0, 2.0, -2.0, 2.0, -2.0, 0.0,
                0.4, 0.2, pic, pic2, vector = false, bivector = true, trivector = false , fieldFunc = {P -> Magnetic(P,{t -> Path(t)},0.0,2*PI) }
            )
        }

        if (false) {
            val allCharges = arrayOf(Multivector(1, 0, 0, 0), Multivector(-3, 0, 1, 0))

            for (charge in allCharges) {
                scalarDraw(charge, charge)
            }

            drawField(
                -2.0, 2.0, -2.0, 2.0, 0.0, 0.0,
                0.1, 0.2, pic, pic2, fieldFunc = {P -> Electric(P, *allCharges)}
            )

            drawField(
                -2.0, 2.0, -2.0, 2.0, 0.0, 0.0,
                0.1, 0.2, pic, pic2, fieldFunc = {P -> Voltage(P, *allCharges)}
            )
        }

        if (true) {
            val transientNodes = mutableListOf<Node>()
            var t = 0.0
            onUpdate {
                transientNodes.forEach {
                    removeNode(it)
                    it.release()
                }
                transientNodes.clear()
                transparentFaces.clear()
                val permanentNodeCount = children.size

                t += 0.5 * Time.deltaT.toDouble()
                val rotor = Multivector(cos(t)) + blade * sin(t)
                val v1_prime = rotor.reverse() * v1 * rotor
                val v2_prime = rotor.reverse() * v2 * rotor
                biFromVector(v1_prime, v2_prime, -0.5 * (v1_prime + v2_prime), picture = pic, picture2 = pic2)

                for (i in permanentNodeCount until children.size) {
                    transientNodes.add(children[i])
                }
            }
        }
    }
    if (false) {ctx.scenes += debugOverlay()}
}
