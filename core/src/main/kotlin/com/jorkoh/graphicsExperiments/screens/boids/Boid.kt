package com.jorkoh.graphicsExperiments.screens.boids

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.jorkoh.graphicsExperiments.screens.angleDifference
import ktx.math.*
import kotlin.math.max

class Boid(
        var position: Vector2,
        var velocity: Vector2,
        var velocityComponents: MutableList<Vector2> = mutableListOf()
) {

    companion object {
        const val PERCEPTION_RADIUS = 60f
        const val PERCEPTION_CONE_DEGREES = 260f

        const val INITIAL_SPEED = 60f
        const val MINIMUM_SPEED = 40f
        const val MAXIMUM_SPEED = 100f

        const val MAX_TURN_RATE_ANGLE = 280f

        const val WALL_AVOIDANCE_FACTOR = 2e5f
        const val SEPARATION_FACTOR = 1.8e4f
        const val ALIGNMENT_FACTOR = 80f
        const val COHESION_FACTOR = 4f
    }

    fun calculateVelocityComponents(neighbors: List<Boid>, timeDelta: Float) {
        velocityComponents.clear()

        // Avoid walls
        val dstToLeft = max(position.x, 0.000001f)
        velocityComponents.add(vec2(1f, 0f) * (1 / (dstToLeft * dstToLeft)) * timeDelta * WALL_AVOIDANCE_FACTOR)
        val dstToRight = max(Gdx.graphics.width - position.x, 0.000001f)
        velocityComponents.add(vec2(-1f, 0f) * (1 / (dstToRight * dstToRight)) * timeDelta * WALL_AVOIDANCE_FACTOR)
        val dstToBottom = max(position.y, 0.000001f)
        velocityComponents.add(vec2(0f, 1f) * (1 / (dstToBottom * dstToBottom)) * timeDelta * WALL_AVOIDANCE_FACTOR)
        val dstToTop = max(Gdx.graphics.height - position.y, 0.000001f)
        velocityComponents.add(vec2(0f, -1f) * (1 / (dstToTop * dstToTop)) * timeDelta * WALL_AVOIDANCE_FACTOR)

        // Flock rules
        if (neighbors.isNotEmpty()) {
            // Separation rule
            val separation = vec2()
            neighbors.forEach { otherBoid ->
                val positionDifference = position - otherBoid.position
                val positionDistance = positionDifference.len()
                separation += positionDifference.nor() * (1 / (positionDistance * positionDistance))
            }
            velocityComponents.add(separation * timeDelta * SEPARATION_FACTOR)

            // Alignment rule
            val alignment = vec2()
            neighbors.forEach { otherBoid ->
                alignment += vec2(1f, 0f).setAngle(otherBoid.velocity.angle())
            }
            velocityComponents.add(alignment.nor() * timeDelta * ALIGNMENT_FACTOR)

            // Cohesion rule
            // TODO go over this cohesion rule
            val cohesion = vec2()
            neighbors.forEach { otherBoid ->
                cohesion += otherBoid.position
            }
            velocityComponents.add((cohesion / neighbors.size - position) * timeDelta * COHESION_FACTOR)
        }
    }

    fun calculateVelocity(timeDelta: Float) {
        velocity = velocity.cpy().also { newVelocity ->
            // Add the new components
            velocityComponents.forEach { component -> newVelocity += component }
            // Clamp the turn rate
            newVelocity.clampTurnRate(velocity, MAX_TURN_RATE_ANGLE, timeDelta)
            // Clamp the speed
            newVelocity.clamp(MINIMUM_SPEED, MAXIMUM_SPEED)
        }
    }

    private fun Vector2.clampTurnRate(previousVector: Vector2, maxTurnRate: Float, timeDelta: Float) {
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

    fun move(timeDelta: Float) {
        position = position + velocity * timeDelta
    }
}