//Staged.kt
package com.sunnygg95.mathplay

import de.fabmax.kool.Assets
import de.fabmax.kool.KoolContext
import de.fabmax.kool.loadTexture2d
import de.fabmax.kool.scene.defaultOrbitCamera
import de.fabmax.kool.scene.scene
import de.fabmax.kool.util.Color
import de.fabmax.kool.util.launchOnMainThread



fun createStaged(ctx: KoolContext) = launchOnMainThread {
    //load orientation picture
    val pic = Assets.loadTexture2d("return-90.png").getOrThrow()
    val pic2 = Assets.loadTexture2d("fliped-return-90.png").getOrThrow()

    //declare Multivector
    val a = Multivector(x=1.3,yz=1.0)
    val b = Multivector(s=2.5,xyz=-0.4)
    val s = 3.0
    val block = Multivector(xyz=1.0)
    val blade1 = Multivector(x=1.0)
    val blade2 = Multivector(x=0.5,y=1.0)
    val vt1 = Multivector(x=-1.0,y=1.0,z=1.0)
    val vt2 = Multivector(x=0.5,y=0.5)

    //all operation
    println ("Multivector a $a and Multivector b $b")
    println (a.grade(1))
    println (b.getGrades())
    println (a.reverse())
    println (a+b)
    println (a-b)
    println (a*b)
    println (a*s)
    println (a/s)
    println (a.outer(b))
    println (a.inner(b))// use Hestenes's inner product
    println (a.leftCon(b))
    println (a.rightCon(b))

    ctx.scenes += scene {
        defaultOrbitCamera()
        updateTransparentOrdering()
        setupDefaultStage(this)

        //start all visualization here
        vt1.vectorDraw(this)
        vt2.vectorDraw(this)
        blade1.biFromVector(this,blade2,vt2,Color.YELLOW,pic,pic2)
        (blade1*blade2).bivectorDraw(this,zero,Color.LIGHT_CYAN,pic,pic2)
        block.trivectorDraw(this,vt1,pic,pic2)
    }
}