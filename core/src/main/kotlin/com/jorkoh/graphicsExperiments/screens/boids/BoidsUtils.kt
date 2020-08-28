package com.jorkoh.graphicsExperiments.screens.boids

import com.badlogic.gdx.math.Vector2


// https://math.stackexchange.com/a/1649850
fun angleDifference(firstAngle: Float, secondAngle: Float): Float {
    return (firstAngle - secondAngle + 540) % 360 - 180
}

fun Vector2.clampTurnRate(previousVector: Vector2, maxTurnRate: Float, timeDelta: Float) {
    val previousVectorAngle = previousVector.angle()
    val turnRate = angleDifference(angle(), previousVectorAngle) / timeDelta
    when {
        turnRate > maxTurnRate -> {
            setAngle(previousVectorAngle + maxTurnRate * timeDelta)
        }
        turnRate < -maxTurnRate -> {
            setAngle(previousVectorAngle - maxTurnRate * timeDelta)
        }
    }
}