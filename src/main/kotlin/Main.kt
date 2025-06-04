package com.sunnygg95.mathplay

fun main() {
    val a = Multivector(s=-1.5,x=3.0,y=-2.0,z=1.0,xyz=2.5)
    val b = Multivector(xy=0.5)
    println(a.leftCon(b))
    println(a.rightCon(b))
}