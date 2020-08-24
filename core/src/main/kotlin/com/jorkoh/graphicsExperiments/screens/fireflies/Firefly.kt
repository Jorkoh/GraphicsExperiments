package com.jorkoh.graphicsExperiments.screens.fireflies

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import kotlin.random.Random

class Firefly(
        var x: Float,
        var y: Float
) {
    companion object {
        const val DEFAULT_RADIUS = 7.5f
        const val CYCLE_LENGTH = 16
        const val PERCEPTION_RADIUS = 80f
        val PERCEPTION_COLOR = Color(0x181818ff)
    }

    private val radius = DEFAULT_RADIUS
    private var cyclePosition = Random.nextInt(CYCLE_LENGTH)
    var isOn = cyclePosition == 0

    fun update() {
        // Movement
        val xPositive = when {
            x - radius <= 0 -> true
            x + radius >= Gdx.graphics.width -> false
            else -> Random.nextBoolean()
        }
        val yPositive = when {
            y - radius <= 0 -> true
            y + radius >= Gdx.graphics.height -> false
            else -> Random.nextBoolean()
        }

        x += Random.nextFloat() * 15f * if (xPositive) 1 else -1
        y += Random.nextFloat() * 15f * if (yPositive) 1 else -1

        // Cycle
        cyclePosition = (cyclePosition + 1) % CYCLE_LENGTH
        isOn = cyclePosition == 0
    }

    fun lookAt(fireflies: List<Firefly>) {
        if (!isOn && fireflies.filter { firefly -> firefly != this && Vector2.dst(x, y, firefly.x, firefly.y) <= PERCEPTION_RADIUS }.any { it.isOn }) {
            cyclePosition = 0
        }
    }

    fun draw(shapeRenderer: ShapeRenderer) {
        shapeRenderer.color = if (isOn) Color.YELLOW else Color.DARK_GRAY
        shapeRenderer.circle(x, y, radius)
    }

    fun drawPerception(shapeRenderer: ShapeRenderer) {
        shapeRenderer.color = PERCEPTION_COLOR
        shapeRenderer.circle(x, y, PERCEPTION_RADIUS)
    }

    fun randomizeCycle(){
        cyclePosition = Random.nextInt(CYCLE_LENGTH)
    }
}