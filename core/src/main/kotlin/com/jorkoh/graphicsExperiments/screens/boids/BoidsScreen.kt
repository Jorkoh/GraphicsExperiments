package com.jorkoh.graphicsExperiments.screens.boids

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.jorkoh.graphicsExperiments.GraphicsExperiments
import com.jorkoh.graphicsExperiments.screens.SelectionScreen
import com.jorkoh.graphicsExperiments.screens.clearScreen
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.graphics.circle
import ktx.graphics.use
import ktx.math.times
import ktx.math.vec2
import kotlin.random.Random

class BoidsScreen(private val main: GraphicsExperiments) : KtxScreen {

    private val inputProcessor = object : KtxInputAdapter {
        override fun keyDown(keycode: Int): Boolean {
            return when (keycode) {
                Input.Keys.ESCAPE -> {
                    main.addScreen(SelectionScreen(main))
                    main.setScreen<SelectionScreen>()
                    main.removeScreen<BoidsScreen>()
                    dispose()
                    true
                }
                else -> false
            }
        }
    }
    private val boids = mutableListOf<Boid>()

    override fun show() {
        Gdx.input.inputProcessor = inputProcessor
        addBoids()
    }

    override fun render(delta: Float) {
        clearScreen(0.3686f, 0.5725f, 0.8f)

        boids.forEach { boid -> boid.updateMovement(delta) }
        main.shapeRenderer.use(ShapeRenderer.ShapeType.Line) { renderer ->
            renderer.color = Color.BLUE
            boids.forEach { boid -> renderer.circle(boid.position, 3f) }
        }
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
        boids.clear()
    }

    private fun addBoids() {
        repeat(100) {
            val position = vec2(
                    Random.nextFloat() * Gdx.graphics.width,
                    Random.nextFloat() * Gdx.graphics.height
            )
            val velocity = vec2().setToRandomDirection() * Boid.INITIAL_SPEED
            boids.add(Boid(position, velocity))
        }
    }
}