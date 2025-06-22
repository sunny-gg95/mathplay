// Drawing MV.kt
package com.sunnygg95.mathplay

import de.fabmax.kool.util.Color
import kotlin.math.cbrt
import de.fabmax.kool.math.*
import de.fabmax.kool.scene.*

fun defaultStage(scene: Scene) {
    fun addAxis(dir: Vec3f, color: Color) {
        scene.addLineMesh {
            this.color = color
            addLine(Vec3f.ZERO, dir)
        }
    }

    addAxis(Vec3f(1f, 0f, 0f), Color.RED)   // X axis
    addAxis(Vec3f(0f, 1f, 0f), Color.GREEN) // Y axis
    addAxis(Vec3f(0f, 0f, 1f), Color.BLUE)  // Z axis
}

fun Multivector.drawVector(scene: Scene,home: Multivector, c: Color = Color.YELLOW) {
    val arrow = home + this
    val vp = this.perpendicular()/80.0
    val vpp = this.anotherPerpendicular()/80.0
    val arrow1 = home + this*0.95 + vp
    val arrow2 = home + this*0.95 + vpp
    val arrow3 = home + this*0.95 - vp
    val arrow4 = home + this*0.95 - vpp
    val base = Vec3f(home.x.toFloat(),home.y.toFloat(),home.z.toFloat())
    val tip = Vec3f(arrow.x.toFloat(),arrow.y.toFloat(),arrow.z.toFloat())
    val tip1 = Vec3f(arrow1.x.toFloat(),arrow1.y.toFloat(),arrow1.z.toFloat())
    val tip2 = Vec3f(arrow2.x.toFloat(),arrow2.y.toFloat(),arrow2.z.toFloat())
    val tip3 = Vec3f(arrow3.x.toFloat(),arrow3.y.toFloat(),arrow3.z.toFloat())
    val tip4 = Vec3f(arrow4.x.toFloat(),arrow4.y.toFloat(),arrow4.z.toFloat())
    scene += scene{
        addLineMesh{
            addLine(base,tip,c)
            addLine(tip1,tip,c)
            addLine(tip2,tip,c)
            addLine(tip3,tip,c)
            addLine(tip4,tip,c)
        }
    }
}

fun Multivector.trivectorDraw(scene: Scene) {
    var c = Color.LIGHT_BLUE
    if(xyz<0){
        c = Color.LIGHT_RED
    }
    val hl = (cbrt(xyz.toFloat()))/2 //half-length
    val fhl = -hl //flipped-half length
    val v000 = Vec3f(fhl,fhl,fhl)
    val v001 = Vec3f(fhl,fhl,hl)
    val v010 = Vec3f(fhl,hl,fhl)
    val v011 = Vec3f(fhl,hl,hl)
    val v100 = Vec3f(hl,fhl,fhl)
    val v101 = Vec3f(hl,fhl,hl)
    val v110 = Vec3f(hl,hl,fhl)
    val v111 = Vec3f(hl,hl,hl)
    scene += scene{
        addLineMesh{addLine(v000,v001,c)}
        addLineMesh{addLine(v000,v010,c)}
        addLineMesh{addLine(v000,v100,c)}
        addLineMesh{addLine(v111,v110,c)}
        addLineMesh{addLine(v111,v101,c)}
        addLineMesh{addLine(v111,v011,c)}
        addLineMesh{addLine(v001,v101,c)}
        addLineMesh{addLine(v001,v011,c)}
        addLineMesh{addLine(v100,v101,c)}
        addLineMesh{addLine(v100,v110,c)}
        addLineMesh{addLine(v010,v011,c)}
        addLineMesh{addLine(v010,v110,c)}
    }
}

fun Multivector.biFromVector(scene: Scene,other: Multivector,home: Multivector , c: Color = Color.WHITE) {
    this.drawVector(scene,home,c)
    other.drawVector(scene,home+this,c)
    (this*-1.0).drawVector(scene,home+this+other,c)
    (other*-1.0).drawVector(scene,home+other,c)
}