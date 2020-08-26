package com.jorkoh.graphicsExperiments.screens.boids

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import ktx.math.*

class Boid(
        var position: Vector2,
        var velocity: Vector2,
        var velocityComponents: MutableList<Vector2> = mutableListOf()
) {

    companion object {
        const val PERCEPTION_RADIUS = 100f
        const val INITIAL_SPEED = 60f

        const val WALL_AVOIDANCE_FACTOR = 2e5f
        const val SEPARATION_FACTOR = 2e4f
        const val ALIGNMENT_FACTOR = 50f
        const val COHESION_FACTOR = 5f
    }

    fun calculateVelocityComponents(neighbors: List<Boid>, timeDelta: Float) {
        velocityComponents.clear()

        // Avoid walls
        val dstToLeft = position.x
        velocityComponents.add(vec2(1f, 0f) * (1 / (dstToLeft * dstToLeft)) * timeDelta * WALL_AVOIDANCE_FACTOR)
        val dstToRight = Gdx.graphics.width - position.x
        velocityComponents.add(vec2(-1f, 0f) * (1 / (dstToRight * dstToRight)) * timeDelta * WALL_AVOIDANCE_FACTOR)
        val dstToBottom = position.y
        velocityComponents.add(vec2(0f, 1f) * (1 / (dstToBottom * dstToBottom)) * timeDelta * WALL_AVOIDANCE_FACTOR)
        val dstToTop = Gdx.graphics.height - position.y
        velocityComponents.add(vec2(0f, -1f) * (1 / (dstToTop * dstToTop)) * timeDelta * WALL_AVOIDANCE_FACTOR)

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
    }

    fun calculateVelocity() {
        velocityComponents.forEach { velocityVector -> velocity = velocity + velocityVector }

        // :concern:
        velocity.limit(100f)
    }


    fun move(timeDelta: Float) {
        position = position + velocity * timeDelta
    }
}