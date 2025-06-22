//Multivector.kt
package com.sunnygg95.mathplay

import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.math.pow

data class Multivector (
    val s: Double = 0.0,
    val x: Double = 0.0,
    val y: Double = 0.0,
    val z: Double = 0.0,
    val xy: Double = 0.0,
    val yz: Double = 0.0,
    val zx: Double = 0.0,
    val xyz: Double = 0.0
) {

    override fun toString(): String {
        val terms = mutableListOf<String>()

        fun addTerm(value: Double, label: String) {
            if (value != 0.0) {
                val formatted = when {
                    terms.isEmpty() -> "$value$label"
                    value < 0        -> "- ${-value}$label"
                    else             -> "+ $value$label"
                }
                terms.add(formatted)
            }
        }

        addTerm(s, "")
        addTerm(x, "x")
        addTerm(y, "y")
        addTerm(z, "z")
        addTerm(xy, "xy")
        addTerm(yz, "yz")
        addTerm(zx, "zx")
        addTerm(xyz, "xyz")

        return if (terms.isEmpty()) "0" else terms.joinToString(" ")
    }

    operator fun plus(other: Multivector): Multivector{
        return Multivector(
            s = this.s + other.s,
            x = this.x + other.x,
            y = this.y + other.y,
            z = this.z + other.z,
            xy = this.xy + other.xy,
            yz = this.yz + other.yz,
            zx = this.zx + other.zx,
            xyz = this.xyz + other.xyz
        )
    }

    operator fun times(t: Double): Multivector{
        return Multivector(
            s = t * this.s,
            x = t * this.x,
            y = t * this.y,
            z = t * this.z,
            xy = t * this.xy,
            yz = t * this.yz,
            zx = t * this.zx,
            xyz = t * this.xyz
        )
    }

    operator fun minus(other: Multivector): Multivector{
        return this + (other*(-1.0))
    }

    fun grade(n:Int): Multivector{
        return when (n) {
            0 -> Multivector(s= this.s)
            1 -> Multivector(x = this.x, y = this.y, z = this.z)
            2 -> Multivector(xy = this.xy, yz = this.yz, zx = this.zx)
            3 -> Multivector(xyz = this.xyz)
            else -> Multivector()
        }
    }

    fun reverse(): Multivector {
        return this.grade(0) + this.grade(1) - this.grade(2) - this.grade(3)
    }

    operator fun times(other: Multivector): Multivector{
        return Multivector(
            s   = other.s*this.s   + other.x*this.x   + other.y*this.y   + other.z*this.z   + other.xy*this.xy  + other.yz*this.yz  + other.zx*this.zx + other.xyz*this.xyz,
            x   = other.x*this.s   + other.s*this.x   + other.y*this.xy  + other.zx*this.z  - other.yz*this.xyz - other.xyz*this.yz - other.z*this.zx  - other.xy*this.y,
            y   = other.y*this.s   + other.s*this.y   + other.z*this.yz  + other.xy*this.x  - other.zx*this.xyz - other.xyz*this.zx - other.x*this.xy  - other.yz*this.z,
            z   = other.z*this.s   + other.s*this.z   + other.x*this.zx  + other.yz*this.y  - other.xy*this.xyz - other.xyz*this.xy - other.y*this.yz  - other.zx*this.x,
            xy  = other.xy*this.s  + other.s*this.xy  + other.z*this.xyz + other.xyz*this.z + other.y*this.x    + other.yz*this.zx  - other.x*this.y   - other.zx*this.yz,
            yz  = other.yz*this.s  + other.s*this.yz  + other.x*this.xyz + other.xyz*this.x + other.z*this.y    + other.zx*this.xy  - other.y*this.z   - other.xy*this.zx,
            zx  = other.zx*this.s  + other.s*this.zx  + other.y*this.xyz + other.xyz*this.y + other.x*this.z    + other.xy*this.yz  - other.z*this.x   - other.yz*this.xy,
            xyz = other.xyz*this.s + other.s*this.xyz + other.x*this.yz  + other.yz*this.x  + other.y*this.zx   + other.zx*this.y   + other.z*this.xy  + other.xy*this.z
        )
    }

    operator fun div(t: Double): Multivector{
        return Multivector(
            s = this.s/t,
            x = this.x/t,
            y = this.y/t,
            z = this.z/t,
            xy = this.xy/t,
            yz = this.yz/t,
            zx = this.zx/t,
            xyz = this.xyz/t
        )
    }

    fun getGrades(): Set<Int> {
        val grades = mutableSetOf<Int>()
        if (s != 0.0) grades.add(0)
        if (x != 0.0 || y != 0.0 || z != 0.0) grades.add(1)
        if (xy != 0.0 || yz != 0.0 || zx != 0.0) grades.add(2)
        if (xyz != 0.0) grades.add(3)
        return grades
    }

    fun outer(other: Multivector): Multivector {
        val product = this * other

        val gradeList = mutableSetOf<Int>()
        for (j in this.getGrades()) {
            for (k in other.getGrades()) {
                gradeList.add(j + k)
            }
        }

        var result = Multivector()
        for (g in gradeList) {
            result += product.grade(g)
        }

        return result
    }

    fun leftCon(other: Multivector): Multivector{
        var sum = Multivector()
        for (j in this.getGrades()) {
            for (k in other.getGrades()){
                sum += ((this.grade(j))*(other.grade(k))).grade(j-k)
            }
        }
        return sum
    }

    fun inner(other: Multivector): Multivector{//Hestenes's inner product
        var sum = Multivector()
        for (j in this.getGrades()) {
            for (k in other.getGrades()){
                sum += ((this.grade(j))*(other.grade(k))).grade(abs(j-k))
            }
        }
        return sum
    }
}

val zero = Multivector(0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0)

fun Multivector.rightCon(other: Multivector): Multivector {
    return other.leftCon(this) // just flip the arguments
}

fun Multivector.perpendicular(): Multivector {
    return if (this.x != 0.0 || this.y != 0.0) {
        Multivector(x = -this.y, y = this.x, z = 0.0)
    } else {
        Multivector(x = 0.0, y = -this.z, z = this.y)
    }
}

fun Multivector.anotherPerpendicular(): Multivector{
    return (Multivector(xyz=1.0)*this*this.perpendicular())/(sqrt(this.x.pow(2)+this.y.pow(2)+this.z.pow(2)))
}

operator fun Double.times(m: Multivector): Multivector {
    return m * this
}