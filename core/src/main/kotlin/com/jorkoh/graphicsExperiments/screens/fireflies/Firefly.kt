package com.jorkoh.graphicsExperiments.screens.fireflies

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import kotlin.random.Random

class Firefly(
        var position: Vector2
) {
    companion object {
        const val BODY_RADIUS = 7.5f
        const val CYCLE_LENGTH = 16
        const val PERCEPTION_RADIUS_SQUARED = 6400f
        val PERCEPTION_COLOR = Color(0x181818ff)
    }

    private var cyclePosition = Random.nextInt(CYCLE_LENGTH)
    var isOn = cyclePosition == 0

    fun update() {
        // Movement
        val xPositive = when {
            position.x - BODY_RADIUS <= 0 -> true
            position.x + BODY_RADIUS >= Gdx.graphics.width -> false
            else -> Random.nextBoolean()
        }
        val yPositive = when {
            position.y - BODY_RADIUS <= 0 -> true
            position.y + BODY_RADIUS >= Gdx.graphics.height -> false
            else -> Random.nextBoolean()
        }

        position.x += Random.nextFloat() * 15f * if (xPositive) 1 else -1
        position.y += Random.nextFloat() * 15f * if (yPositive) 1 else -1

        // Cycle
        cyclePosition = (cyclePosition + 1) % CYCLE_LENGTH
        isOn = cyclePosition == 0
    }

    fun lookAt(fireflies: List<Firefly>) {
        if (!isOn && fireflies.getNearbyFireflies(this).any { it.isOn }) {
            cyclePosition = 0
        }
    }

    private fun List<Firefly>.getNearbyFireflies(firefly: Firefly) = this.filter { otherFirefly ->
        otherFirefly != firefly && Vector2.dst2(
                firefly.position.x, firefly.position.y,
                otherFirefly.position.x, otherFirefly.position.y
        ) <= PERCEPTION_RADIUS_SQUARED
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