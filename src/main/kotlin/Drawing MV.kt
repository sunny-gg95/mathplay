// Drawing MV.kt
package com.sunnygg95.mathplay

import de.fabmax.kool.util.Color
import de.fabmax.kool.math.*
import de.fabmax.kool.scene.*
import de.fabmax.kool.modules.ksl.KslUnlitShader
import de.fabmax.kool.pipeline.BlendMode
import de.fabmax.kool.pipeline.Texture2d
import de.fabmax.kool.scene.addTextureMesh
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cbrt
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.acos
import kotlin.math.max
import kotlin.math.floor

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

fun Multivector.faceGen(other: Multivector) : Multivector {
    return I * other.outer(this) * (I*other.outer(this)).vectorSize()/max(this.vectorSize(),other.vectorSize()).pow(2)
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

fun Scene.setupDefaultStage(): LineMesh {
    fun addAxis(dir: Vec3f, color: Color) {
        this.addLineMesh {
            this.color = color
            addLine(Vec3f.ZERO, dir)
        }
    }

    addAxis(Vec3f(1f, 0f, 0f), Color.RED)   // X-axis
    addAxis(Vec3f(0f, 1f, 0f), Color.GREEN) // Y-axis
    addAxis(Vec3f(0f, 0f, 1f), Color.BLUE)  // Z-axis

    val sharedLineMesh = this.addLineMesh {
        color = Color.WHITE
    }

    return sharedLineMesh
}

fun Scene.vectorDraw(first: Multivector, anchor: Multivector = zero, c: Color = Color.WHITE) {
    val arrow = anchor + first
    val vp = first.perpendicular() / 80.0
    val vpp = first.anotherPerpendicular() / 80.0
    val arrow1 = anchor + first * 0.95 + vp
    val arrow2 = anchor + first * 0.95 + vpp
    val arrow3 = anchor + first * 0.95 - vp
    val arrow4 = anchor + first * 0.95 - vpp
    this.addLineMesh {
        addLine(anchor.toVec3f(), arrow.toVec3f(), c)
        addLine(arrow1.toVec3f(), arrow.toVec3f(), c)
        addLine(arrow2.toVec3f(), arrow.toVec3f(), c)
        addLine(arrow3.toVec3f(), arrow.toVec3f(), c)
        addLine(arrow4.toVec3f(), arrow.toVec3f(), c)
    }
}

fun Scene.trivectorDraw(first: Multivector, anchor: Multivector = zero,picture: Texture2d ,picture2: Texture2d,customColor: Boolean = false,customC: Color = Color.WHITE) {
    val isPositive = first.xyz >= 0.0
    val pic = if (isPositive) picture else picture2
    val color = if (customColor) customC.withAlpha(0.5f) else if (isPositive) Color.LIGHT_BLUE.withAlpha(0.5f) else Color.LIGHT_RED.withAlpha(0.5f)
    val c = if (customColor) customC else if (isPositive) Color.LIGHT_BLUE else Color.LIGHT_RED
    val sign = if (isPositive) 1.0 else -1.0
    val hl = (sign * cbrt(first.xyz) / 2.0)
    val fhl = -hl
    val v000 = Multivector(x = fhl + anchor.x, y = fhl + anchor.y, z = fhl + anchor.z)
    val v001 = Multivector(x = fhl + anchor.x, y = fhl + anchor.y, z = hl + anchor.z)
    val v010 = Multivector(x = fhl + anchor.x, y = hl + anchor.y, z = fhl + anchor.z)
    val v011 = Multivector(x = fhl + anchor.x, y = hl + anchor.y, z = hl + anchor.z)
    val v100 = Multivector(x = hl + anchor.x, y = fhl + anchor.y, z = fhl + anchor.z)
    val v101 = Multivector(x = hl + anchor.x, y = fhl + anchor.y, z = hl + anchor.z)
    val v110 = Multivector(x = hl + anchor.x, y = hl + anchor.y, z = fhl + anchor.z)
    val v111 = Multivector(x = hl + anchor.x, y = hl + anchor.y, z = hl + anchor.z)
    this.addTransparentFace(v111, v101, v100, v110,color)// +x
    picture(this,pic,anchor+ Multivector(x=hl),Multivector(x=first.xyz)/cbrt(first.xyz))
    this.addTransparentFace(v111, v110, v010, v011,color)// +y
    picture(this,pic,anchor+ Multivector(y=hl),Multivector(y=first.xyz)/cbrt(first.xyz))
    this.addTransparentFace(v111, v011, v001, v101,color)// +z
    picture(this,pic,anchor+ Multivector(z=hl),Multivector(z=first.xyz)/cbrt(first.xyz))
    this.addTransparentFace(v000, v001, v011, v010,color)// -x
    picture(this,pic,anchor+ Multivector(x=fhl),Multivector(x=-first.xyz)/cbrt(first.xyz))
    this.addTransparentFace(v000, v100, v101, v001,color)// -y
    picture(this,pic,anchor+ Multivector(y=fhl),Multivector(y=-first.xyz)/cbrt(first.xyz))
    this.addTransparentFace(v000, v010, v110, v100,color)// -z
    picture(this,pic,anchor+ Multivector(z=fhl),Multivector(z=-first.xyz)/cbrt(first.xyz))
    this.addLineMesh{
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

fun Scene.biFromVector(first: Multivector,other: Multivector,anchor: Multivector = zero ,c: Color = Color.WHITE ,picture: Texture2d ,picture2: Texture2d) {
    val transparentColor = Color(c.r,c.g,c.b,0.5f)
    val center = anchor + (first + other)/2.0
    val normal = first.faceGen(other)
    this.vectorDraw(first,anchor,c)
    this.vectorDraw(other,anchor+first,c)
    this.vectorDraw((first*-1.0),anchor+first+other,c)
    this.vectorDraw((other*-1.0),anchor+other,c)
    this.addTransparentFace(anchor,anchor+first,anchor+first+other,anchor+other,transparentColor)
    this.addTransparentFace(anchor,anchor+other,anchor+first+other,anchor+first,transparentColor)
    picture(this,picture,center,normal)
    picture(this,picture2,center,-1.0*normal)
}

fun Scene.bivectorDraw(first: Multivector,anchor: Multivector = zero, c: Color = Color.WHITE,picture: Texture2d,picture2: Texture2d) {
    val normalVector = I * first.grade(2)
    val scaleFactor = (normalVector.x.pow(2)+normalVector.y.pow(2)+normalVector.z.pow(2)).pow(-0.25)
    //scale factor = 1/sqrt(A)
    val pen1 = normalVector.perpendicular() * scaleFactor
    val pen2 = normalVector.anotherPerpendicular() * scaleFactor
    //Length = A * 1/sqrt(A) = sqrt(A)
    val offset = anchor-(pen1+pen2)/2.0 // anchor - center
    this.biFromVector(pen1,pen2,offset,c,picture,picture2)
    //Area = sqrt(A) * sqrt(A) = A
}

fun Scene.triFromVector(first: Multivector,other: Multivector,another: Multivector,anchor: Multivector = zero,picture: Texture2d,picture2: Texture2d) {
    val isPositive = (first.outer(other).outer(another)).xyz >= 0.0
    val pic = if (isPositive) picture else picture2
    val sign = if (isPositive) 1.0 else -1.0
    val color = if (isPositive) Color.LIGHT_BLUE.withAlpha(0.5f) else Color.LIGHT_RED.withAlpha(0.5f)
    val c = if (isPositive) Color.LIGHT_BLUE else Color.LIGHT_RED
    val v001 = anchor + first
    val v010 = anchor + other
    val v011 = anchor + first + other
    val v100 = anchor + another
    val v101 = anchor + first + another
    val v110 = anchor + other + another
    val v111 = anchor + first + other + another
    this.addLineMesh{
        addLine(anchor.toVec3f(),v001.toVec3f(),color=c)
        addLine(anchor.toVec3f(),v010.toVec3f(),color=c)
        addLine(anchor.toVec3f(),v100.toVec3f(),color=c)
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
    this.addTransparentFace(v111, v101, v100, v110,color)
    picture(this,pic,(v100+(first+other)/2),sign*first.faceGen(other))
    this.addTransparentFace(v111, v110, v010, v011,color)
    picture(this,pic,(v010+(first+another)/2),sign*another.faceGen(first))
    this.addTransparentFace(v111, v011, v001, v101,color)
    picture(this,pic,(v001+(other+another)/2),sign*other.faceGen(another))
    this.addTransparentFace(anchor, v001, v011, v010,color)
    picture(this,pic,(anchor+(first+other)/2),sign*other.faceGen(first))
    this.addTransparentFace(anchor, v100, v101, v001,color)
    picture(this,pic,(anchor+(first+another)/2),sign*first.faceGen(another))
    this.addTransparentFace(anchor, v010, v110, v100,color)
    picture(this,pic,(anchor+(other+another)/2),sign*another.faceGen(other))
}

fun Scene.scalarDraw(first: Multivector,anchor: Multivector = zero,customColor: Boolean = false,customC: Color = Color.WHITE) {
    val c = if (customColor) {customC} else {if (first.s > 0) {Color.RED} else {Color.BLUE}}
    this.addColorMesh {
        generate {
            color = c.toLinear()
            uvSphere {
                steps = 20
                radius = abs(first.s*0.1).toFloat()
                translate(anchor.toVec3f())
            }
        }
        shader = KslUnlitShader {
            color { vertexColor() }
        }
    }
}

fun hsv2rgb(h: Double,s: Double = 1.0, v: Double = 1.0): Color{
    val C = v * s
    val hp = h / 60
    val X = C * (1 - abs(hp.mod(2.0)-1))
    val colorP = if (hp <1) {
        Color(C.toFloat(),X.toFloat(),0f)
    } else if (hp <2) {
        Color(X.toFloat(),C.toFloat(),0f)
    } else if (hp <3) {
        Color(0f,C.toFloat(),X.toFloat())
    } else if (hp <4) {
        Color(0f,X.toFloat(),C.toFloat())
    } else if (hp <5) {
        Color(X.toFloat(),0f,C.toFloat())
    } else {
        Color(C.toFloat(),0f,X.toFloat())
    }
    val m = (v-C).toFloat()
    val color = Color((colorP.r)+m,(colorP.g)+m,(colorP.b)+m)
    return color
}

fun getScalarColor(value: Double, maxAbs: Float): Color {
    if (maxAbs == 0f) return Color.WHITE

    val linearT = (value / maxAbs).coerceIn(-1.0, 1.0)

    val sign = if (linearT >= 0.0) 1f else -1f
    val absT = abs(linearT)
    val t = (sign * absT.pow(0.3)).toFloat()

    return if (t >= 0f) {
        if (t < 0.5f) {
            val k = t * 2f // Scale to 0.0 .. 1.0 for this half
            Color(1f, 1f, 1f - k) // Blue channel fades out, leaving Red + Green (Yellow)
        } else {
            val k = (t - 0.5f) * 2f // Scale to 0.0 .. 1.0 for this half
            Color(1f, 1f - k, 0f) // Green channel fades out, leaving pure Red
        }
    } else {
        val absT = abs(t)
        if (absT < 0.5f) {
            val k = absT * 2f
            Color(1f - k, 1f, 1f) // Red channel fades out, leaving Green + Blue (Cyan)
        } else {
            val k = (absT - 0.5f) * 2f
            Color(0f, 1f - k, 1f) // Green channel fades out, leaving pure Blue
        }
    }
}

fun Scene.drawField(xMin: Double,xMax: Double,yMin: Double,yMax: Double,zMin: Double,zMax: Double, space: Double, scale: Double,
              picture: Texture2d,picture2: Texture2d,scalar: Boolean = false,vector: Boolean = true, bivector: Boolean = false,trivector: Boolean = false,
              fieldFunc: (Multivector) -> Multivector) {
    val stepX = floor((xMax - xMin)/space).toInt()
    val stepY = floor((yMax - yMin)/space).toInt()
    val stepZ = floor((zMax - zMin)/space).toInt()

    var maxAbs = 0f
    var maxLength = 0f
    var maxArea = 0f
    var maxVolume = 0f
    val gridPoints = mutableListOf<FieldPoint>()
    for (i in 0..stepX) {
        for (j in 0..stepY) {
            for (k in 0..stepZ) {
                val anchor = Multivector(x=(xMin+i*space),y=(yMin+j*space),z=(zMin+k*space))
                val M = fieldFunc(anchor)
                gridPoints.add(FieldPoint(anchor, M))
                if (scalar) {
                    val scalar_size = abs(M.s)
                    if (scalar_size > maxAbs) maxAbs = scalar_size.toFloat()
                }
                if (vector) {
                    val vector_size = M.vectorSize()
                    if (vector_size > maxLength) maxLength = vector_size.toFloat()
                }
                if (bivector) {
                    val bivector_size = (M*I).vectorSize()
                    if (bivector_size > maxArea) maxArea = bivector_size.toFloat()
                }
                if (trivector) {
                    val trivector_size = abs(M.xyz)
                    if (trivector_size > maxVolume) maxVolume = trivector_size.toFloat()
                }
            }
        }
    }

    val areaScale = scale.pow(2)
    val volumeScale = scale.pow(3)
    for (point in gridPoints) {
        val anchor = point.anchor
        val M = point.value


        if (scalar) { this.scalarDraw(Multivector(scale),anchor,true,getScalarColor(M.s,maxAbs))}
        if (vector) {
            val length = M.vectorSize()
            if (length != 0.0) {
                this.vectorDraw(M * scale / length, anchor, hsv2rgb((1 - length / maxLength) * 240))
            }
        }
        if (bivector) {
            val area = (M * I).vectorSize()
            if (area != 0.0) {
                this.bivectorDraw(M * areaScale / area, anchor, hsv2rgb((1 - area / maxArea) * 240), picture, picture2)
            }
        }
        if (trivector) {
            val volume = abs(M.xyz)
            if (volume != 0.0) {
                this.trivectorDraw(I * volumeScale, anchor, picture, picture2, true, hsv2rgb((1 - volume / maxVolume) * 240))
            }
        }
    }
}

fun Scene.drawPath(pathFunc: (Double) -> Multivector,from: Double,to: Double,n: Int = 50,c: Color = Color.YELLOW) {
    val del_t = (to-from)/n
    this.addLineMesh {
        for (i in 0 until n) {
            addLine(pathFunc(from + i*del_t).toVec3f(),pathFunc(from + (i+1)*del_t).toVec3f(),c)
        }
    }
}