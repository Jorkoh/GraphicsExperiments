package com.jorkoh.graphicsExperiments.screens.boids

import com.badlogic.gdx.math.Vector2
import ktx.math.*
import kotlin.math.max

class Boid(
        override var position: Vector2,
        var velocity: Vector2,
        var velocityComponents: MutableList<Vector2> = mutableListOf()
) : Agent {

    companion object {
        const val PERCEPTION_RADIUS = 60f
        // TODO the fact that they can't sense the predator when it's right behind is not very
        //  realistic in the case of fish... maybe it should have different perception cones for
        //  different interactions
        const val PERCEPTION_CONE_DEGREES = 360f

        const val INITIAL_SPEED = 60f
        const val MINIMUM_SPEED = 40f
        const val MAXIMUM_SPEED = 120f

        const val MAX_TURN_RATE_ANGLE = 320f

        const val WALL_AVOIDANCE_FACTOR = 2e5f
        const val SEPARATION_FACTOR = 1.8e4f
        const val ALIGNMENT_FACTOR = 200f
        const val COHESION_FACTOR = 4f
        const val FLEE_FACTOR = 8e10f
    }

    fun interactWithEnvironment(neighbors: List<Boid>, predators: List<Predator>, timeDelta: Float) {
        velocityComponents.clear()

        // Avoid walls
        val dstToLeft = max(position.x, 0.000001f)
        velocityComponents.add(vec2(1f, 0f) * (1 / (dstToLeft * dstToLeft)) * timeDelta * WALL_AVOIDANCE_FACTOR)
        val dstToRight = max(BoidsScreen.AREA_WIDTH - position.x, 0.000001f)
        velocityComponents.add(vec2(-1f, 0f) * (1 / (dstToRight * dstToRight)) * timeDelta * WALL_AVOIDANCE_FACTOR)
        val dstToBottom = max(position.y, 0.000001f)
        velocityComponents.add(vec2(0f, 1f) * (1 / (dstToBottom * dstToBottom)) * timeDelta * WALL_AVOIDANCE_FACTOR)
        val dstToTop = max(BoidsScreen.AREA_HEIGHT - position.y, 0.000001f)
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
            val cohesion = vec2()
            neighbors.forEach { otherBoid ->
                cohesion += otherBoid.position
            }
            velocityComponents.add((cohesion / neighbors.size - position) * timeDelta * COHESION_FACTOR)
        }

        // Avoid predator
        if (predators.isNotEmpty()){
            val flee = vec2()
            predators.forEach { predator ->
                val positionDifference = position - predator.position
                val positionDistance = positionDifference.len()
                flee += positionDifference.nor() * (1 / (positionDistance * positionDistance))
            }
            velocityComponents.add(flee * timeDelta * FLEE_FACTOR)
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

    fun move(timeDelta: Float) {
        position = position + velocity * timeDelta
    }
}