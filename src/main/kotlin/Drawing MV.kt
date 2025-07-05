// Drawing MV.kt
package com.sunnygg95.mathplay

import de.fabmax.kool.util.Color
import de.fabmax.kool.math.*
import de.fabmax.kool.scene.*
import de.fabmax.kool.modules.ksl.KslUnlitShader
import de.fabmax.kool.pipeline.BlendMode
import de.fabmax.kool.modules.ksl.KslPbrShader
import de.fabmax.kool.pipeline.Texture2d
import de.fabmax.kool.scene.addTextureMesh
import kotlin.math.PI
import kotlin.math.cbrt
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.acos
import kotlin.math.max

fun Multivector.toVec3f(): Vec3f {
    return Vec3f(x.toFloat(), y.toFloat(), z.toFloat())
}

fun picture(scene: Scene,picture: Texture2d,anchor: Multivector = zero,face: Multivector) {
    val y = Vec3f(0f, 1f, 0f)
    val n = face.grade(1).toVec3f().normed()

    val rotationAxis = MutableVec3f()
    y.cross(n, rotationAxis)
    rotationAxis.normed()

    val dot = y.dot(n).coerceIn(-1f, 1f)
    val angle = AngleF(acos(dot))

    val flip = QuatF(AngleF(PI.toFloat()),Vec3f(1f, 0f, 0f))
    val q = QuatF(angle,rotationAxis)

    scene.addTextureMesh {
        generate {
            grid { }
        }
        shader = KslUnlitShader {
            color { textureColor(picture) }
            pipeline {
                blendMode = BlendMode.BLEND_PREMULTIPLIED_ALPHA
                isWriteDepth = false
            }
        }
        transform.scale(0.1f*(sqrt(face.vectorSize())).toFloat())
        transform.translate(anchor.toVec3f())
        if(face.grade(1).toVec3f().normed()!=Vec3f(0f,1f,0f)){
            if(face.grade(1).toVec3f().normed()!=Vec3f(0f,-1f,0f)){
                transform.rotate(q)
            }
            else
                transform.rotate(flip)
        }
    }
}

data class FaceMesh(val mesh: Mesh, val center: Vec3f)
val transparentFaces = mutableListOf<FaceMesh>()

fun Scene.updateTransparentOrdering() {
    onUpdate {
        val camPos = camera.globalPos
        transparentFaces.sortedByDescending { it.center.distance(camPos) }
            .forEach {
                removeNode(it.mesh)
                addNode(it.mesh)
            }
    }
}

fun Scene.addTransparentFace(a: Multivector, b: Multivector, c: Multivector, d: Multivector, color: Color) {
    val mesh = addColorMesh {
        shader = KslUnlitShader {
            pipeline {
                blendMode = BlendMode.BLEND_PREMULTIPLIED_ALPHA
                isWriteDepth = false
            }
            color { vertexColor() }
        }
        generate {
            val i0 = geometry.addVertex(a.toVec3f(), color=color)
            val i1 = geometry.addVertex(b.toVec3f(), color=color)
            val i2 = geometry.addVertex(c.toVec3f(), color=color)
            val i3 = geometry.addVertex(d.toVec3f(), color=color)
            addTriIndices(i0, i1, i2)
            addTriIndices(i0, i2, i3)
        }
    }

    val center = (a + b + c + d) / 4.0
    transparentFaces += FaceMesh(mesh, center.toVec3f())
}

fun setupDefaultStage(scene: Scene): LineMesh {
    fun addAxis(dir: Vec3f, color: Color) {
        scene.addLineMesh {
            this.color = color
            addLine(Vec3f.ZERO, dir)
        }
    }

    addAxis(Vec3f(1f, 0f, 0f), Color.RED)   // X axis
    addAxis(Vec3f(0f, 1f, 0f), Color.GREEN) // Y axis
    addAxis(Vec3f(0f, 0f, 1f), Color.BLUE)  // Z axis

    val sharedLineMesh = scene.addLineMesh {
        color = Color.WHITE
    }

    return sharedLineMesh
}

fun Multivector.vectorDraw(scene: Scene, anchor: Multivector = zero, c: Color = Color.WHITE) {
    val arrow = anchor + this
    val vp = this.perpendicular() / 80.0
    val vpp = this.anotherPerpendicular() / 80.0
    val arrow1 = anchor + this * 0.95 + vp
    val arrow2 = anchor + this * 0.95 + vpp
    val arrow3 = anchor + this * 0.95 - vp
    val arrow4 = anchor + this * 0.95 - vpp
    scene.addLineMesh {
        addLine(anchor.toVec3f(), arrow.toVec3f(), c)
        addLine(arrow1.toVec3f(), arrow.toVec3f(), c)
        addLine(arrow2.toVec3f(), arrow.toVec3f(), c)
        addLine(arrow3.toVec3f(), arrow.toVec3f(), c)
        addLine(arrow4.toVec3f(), arrow.toVec3f(), c)
    }
}

fun Multivector.trivectorDraw(scene: Scene, anchor: Multivector = zero,picture: Texture2d ,picture2: Texture2d) {
    val isPositive = xyz >= 0.0
    val pic = if (isPositive) picture else picture2
    val color = if (isPositive) Color.LIGHT_BLUE.withAlpha(0.5f) else Color.LIGHT_RED.withAlpha(0.5f)
    val c = if (isPositive) Color.LIGHT_BLUE else Color.LIGHT_RED
    val sign = if (isPositive) 1.0 else -1.0
    val hl = (sign * cbrt(xyz) / 2.0)
    val fhl = -hl
    val v000 = Multivector(x = fhl + anchor.x, y = fhl + anchor.y, z = fhl + anchor.z)
    val v001 = Multivector(x = fhl + anchor.x, y = fhl + anchor.y, z = hl + anchor.z)
    val v010 = Multivector(x = fhl + anchor.x, y = hl + anchor.y, z = fhl + anchor.z)
    val v011 = Multivector(x = fhl + anchor.x, y = hl + anchor.y, z = hl + anchor.z)
    val v100 = Multivector(x = hl + anchor.x, y = fhl + anchor.y, z = fhl + anchor.z)
    val v101 = Multivector(x = hl + anchor.x, y = fhl + anchor.y, z = hl + anchor.z)
    val v110 = Multivector(x = hl + anchor.x, y = hl + anchor.y, z = fhl + anchor.z)
    val v111 = Multivector(x = hl + anchor.x, y = hl + anchor.y, z = hl + anchor.z)
    scene.addTransparentFace(v111, v101, v100, v110,color)// +x
    picture(scene,pic,anchor+ Multivector(x=hl),Multivector(x=xyz)/cbrt(xyz))
    scene.addTransparentFace(v111, v110, v010, v011,color)// +y
    picture(scene,pic,anchor+ Multivector(y=hl),Multivector(y=xyz)/cbrt(xyz))
    scene.addTransparentFace(v111, v011, v001, v101,color)// +z
    picture(scene,pic,anchor+ Multivector(z=hl),Multivector(z=xyz)/cbrt(xyz))
    scene.addTransparentFace(v000, v001, v011, v010,color)// -x
    picture(scene,pic,anchor+ Multivector(x=fhl),Multivector(x=-xyz)/cbrt(xyz))
    scene.addTransparentFace(v000, v100, v101, v001,color)// -y
    picture(scene,pic,anchor+ Multivector(y=fhl),Multivector(y=-xyz)/cbrt(xyz))
    scene.addTransparentFace(v000, v010, v110, v100,color)// -z
    picture(scene,pic,anchor+ Multivector(z=fhl),Multivector(z=-xyz)/cbrt(xyz))
    scene.addLineMesh{
        addLine(v000.toVec3f(),v001.toVec3f(),color=c)
        addLine(v000.toVec3f(),v010.toVec3f(),color=c)
        addLine(v000.toVec3f(),v100.toVec3f(),color=c)
        addLine(v111.toVec3f(),v110.toVec3f(),color=c)
        addLine(v111.toVec3f(),v101.toVec3f(),color=c)
        addLine(v111.toVec3f(),v011.toVec3f(),color=c)
        addLine(v001.toVec3f(),v101.toVec3f(),color=c)
        addLine(v001.toVec3f(),v011.toVec3f(),color=c)
        addLine(v100.toVec3f(),v101.toVec3f(),color=c)
        addLine(v100.toVec3f(),v110.toVec3f(),color=c)
        addLine(v010.toVec3f(),v011.toVec3f(),color=c)
        addLine(v010.toVec3f(),v110.toVec3f(),color=c)
    }
}

fun Multivector.biFromVector(scene: Scene,other: Multivector,anchor: Multivector = zero ,c: Color = Color.WHITE ,picture: Texture2d ,picture2: Texture2d) {
    val transparentColor = Color(c.r,c.g,c.b,0.5f)
    val center = anchor + (this + other)/2.0
    val normal: Multivector = if (this.vectorSize()==other.vectorSize() && this.inner(other)==zero){
        -1.0*I*this.grade(1).outer(other.grade(1))
    }
    else {
        val scaler = ((I*this.outer(other))).vectorSize()/max(this.vectorSize(),other.vectorSize())
        -1.0*I*this.grade(1).outer(other.grade(1))*scaler.pow(2)/(I*this.grade(1).outer(other.grade(1))).vectorSize()
    }
    this.vectorDraw(scene,anchor,c)
    other.vectorDraw(scene,anchor+this,c)
    (this*-1.0).vectorDraw(scene,anchor+this+other,c)
    (other*-1.0).vectorDraw(scene,anchor+other,c)
    scene.addTransparentFace(anchor,anchor+this,anchor+this+other,anchor+other,transparentColor)
    scene.addTransparentFace(anchor,anchor+other,anchor+this+other,anchor+this,transparentColor)
    picture(scene,picture,center,normal)
    picture(scene,picture2,center,-1.0*normal)
}

fun Multivector.bivectorDraw(scene: Scene,anchor: Multivector = zero, c: Color = Color.WHITE,picture: Texture2d,picture2: Texture2d) {
    val normalVector = I * this.grade(2)
    val scaleFactor = (normalVector.x.pow(2)+normalVector.y.pow(2)+normalVector.z.pow(2)).pow(-0.25)
    val pen1 = normalVector.perpendicular() * scaleFactor
    val pen2 = normalVector.anotherPerpendicular() * scaleFactor
    val offset = anchor-(pen1+pen2)/2.0 // anchor - center
    (pen1).biFromVector(scene,pen2,offset,c,picture,picture2)
}