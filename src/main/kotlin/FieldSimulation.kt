//FieldSimulation.kt
package com.sunnygg95.mathplay

import kotlin.math.pow

data class FieldPoint(
    val anchor: Multivector,
    val value: Multivector
)

fun Electric(P: Multivector,vararg Charges: Multivector): Multivector {
    //Charges: scalar = charge of particle, vector = position
    val k = 1 //Coulomb constant
    var E = zero
    for (charge in Charges) {
        val r = P - charge
        val distance = r.vectorSize()
        if (distance < 0.01) continue // Skip the core of the wire to prevent division by zero
        E += charge.s*(distance.pow(-3))*(r.grade(1))
    }
    return E * k
}
fun Voltage(P: Multivector,vararg Charges: Multivector): Multivector {
    //Charges: scalar = charge of particle, vector = position
    val k = 1 //Coulomb constant
    var V = zero
    for (charge in Charges) {
        val distance = (P - charge).vectorSize()
        if (distance < 0.01) continue // Skip the core of the wire to prevent division by zero
        V += Multivector(charge.s/distance)
    }
    return V * k
}

fun Magnetic(P: Multivector,l: (Double) -> Multivector,t_min: Double,t_max: Double): Multivector {
    val S = 1.0 // Scalar scaling constant (μ₀ I / 4π)
    val n = 50    // Number of integration segments

    var B = zero
    val del_t = (t_max - t_min) / n

    for (s in 0 until n) {
        val t_s = t_min + s * del_t
        val dl = l(t_s + del_t) - l(t_s)
        val r = P - l(t_s)

        val rSize = r.vectorSize()
        if (rSize < 0.01) continue

        B += (rSize.pow(-3)) * dl.outer(r)
    }

    return S * B
}