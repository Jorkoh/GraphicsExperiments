package com.jorkoh.graphicsExperiments.screens.fireflies

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import ktx.math.plusAssign
import ktx.math.times
import ktx.math.vec2
import kotlin.random.Random

class Firefly(
        var position: Vector2,
        var velocity: Vector2
) {
    companion object {
        const val BODY_RADIUS = 7.5f

        const val CYCLE_LENGTH = 16

        const val SPEED = 40f
        const val TURNING_ANGLE = 10

        const val PERCEPTION_RADIUS_SQUARED = 6400f
        val PERCEPTION_COLOR = Color(0x181818ff)
    }

    private var turningDirection = 0

    private var cyclePosition = Random.nextInt(CYCLE_LENGTH)
    var isOn = cyclePosition == 0

    fun update(timeDelta: Float) {
        // Avoid walls
        velocity = when {
            position.x - BODY_RADIUS <= 0 -> vec2(1f, 0f) * SPEED
            position.x + BODY_RADIUS >= Gdx.graphics.width -> vec2(-1f, 0f) * SPEED
            position.y - BODY_RADIUS <= 0 -> vec2(0f, 1f) * SPEED
            position.y + BODY_RADIUS >= Gdx.graphics.height -> vec2(0f, -1f) * SPEED
            else -> velocity
        }
        // Update turning direction
        val randomTurning = Random.nextInt(25)
        turningDirection = when (randomTurning) {
            0 -> -1
            in 1..2 -> 0
            4 -> 1
            else -> turningDirection
        }
        velocity.setAngle(velocity.angle() + TURNING_ANGLE * turningDirection)
        // Update position
        position += velocity * timeDelta
        // Update cycle
        cyclePosition = (cyclePosition + 1) % CYCLE_LENGTH
        isOn = cyclePosition == 0
    }

    fun lookAt(fireflies: List<Firefly>) {
        if (!isOn && fireflies.filterNearbyFireflies(this).any { it.isOn }) {
            cyclePosition = 0
        }
    }

    private fun List<Firefly>.filterNearbyFireflies(firefly: Firefly) = this.filter { otherFirefly ->
        otherFirefly != firefly && firefly.position.dst2(otherFirefly.position) <= PERCEPTION_RADIUS_SQUARED
    }

    fun draw(shapeRenderer: ShapeRenderer) {
        shapeRenderer.color = if (isOn) Color.YELLOW else Color.DARK_GRAY
        shapeRenderer.circle(position.x, position.y, BODY_RADIUS)
    }

    fun drawPerception(shapeRenderer: ShapeRenderer) {
        shapeRenderer.color = PERCEPTION_COLOR
        shapeRenderer.circle(position.x, position.y, PERCEPTION_RADIUS_SQUARED)
    }

    fun randomizeCycle() {
        cyclePosition = Random.nextInt(CYCLE_LENGTH)
    }
}