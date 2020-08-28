package com.jorkoh.graphicsExperiments.screens.boids

import com.badlogic.gdx.math.Vector2
import ktx.math.*
import kotlin.math.max

class Predator(
        override var position: Vector2,
        var velocity: Vector2,
        var velocityComponents: MutableList<Vector2> = mutableListOf()
) : Agent {

    companion object {
        const val PERCEPTION_RADIUS = 180f
        const val PERCEPTION_CONE_DEGREES = 120f

        const val INITIAL_SPEED = 60f
        const val MINIMUM_SPEED = 40f
        const val MAXIMUM_SPEED = 100f

        const val MAX_TURN_RATE_ANGLE = 280f

        const val WALL_AVOIDANCE_FACTOR = 2e5f
        const val CHASE_FACTOR = 200f
    }

    fun interactWithEnvironment(prey: Boid?, timeDelta: Float) {
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

        if (prey != null) {
            velocityComponents.add((prey.position - position).nor() * timeDelta * CHASE_FACTOR)
        } else {
            // TODO move randomly to increase chance of finding stuff
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