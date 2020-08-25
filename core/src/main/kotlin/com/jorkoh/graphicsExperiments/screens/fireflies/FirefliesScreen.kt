package com.jorkoh.graphicsExperiments.screens.fireflies

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.jorkoh.graphicsExperiments.GraphicsExperiments
import com.jorkoh.graphicsExperiments.screens.SelectionScreen
import com.jorkoh.graphicsExperiments.screens.clearScreen
import com.jorkoh.graphicsExperiments.screens.printFPS
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.graphics.use
import ktx.math.times
import ktx.math.vec2
import kotlin.random.Random

class FirefliesScreen(private val main: GraphicsExperiments) : KtxScreen {
    companion object {
        const val DEBUG = false
        const val RANDOMIZE_RADIUS_SQUARED = 6400f
        val LIGHT_IMAGE = Texture("light.png")
        val PERCEPTION_COLOR = Color(0x181818ff)
    }

    private val inputProcessor = object : KtxInputAdapter {
        override fun keyDown(keycode: Int): Boolean {
            return when (keycode) {
                Input.Keys.ESCAPE -> {
                    main.addScreen(SelectionScreen(main))
                    main.setScreen<SelectionScreen>()
                    main.removeScreen<FirefliesScreen>()
                    dispose()
                    true
                }
                else -> false
            }
        }

        override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            randomizeFirefliesCycleAtPosition(screenX.toFloat(), (Gdx.graphics.height - screenY).toFloat())
            return true
        }

        override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
            randomizeFirefliesCycleAtPosition(screenX.toFloat(), (Gdx.graphics.height - screenY).toFloat())
            return true
        }
    }
    private val fireflies = mutableListOf<Firefly>()
    private var accumulator = 0f

    override fun show() {
        Gdx.input.inputProcessor = inputProcessor
        addFireflies()
    }

    override fun render(delta: Float) {
        clearScreen()
        if (DEBUG) {
            printFPS(delta)
        }

        updateFireflies(delta)
        renderFireflies()
    }

    private fun addFireflies() {
        repeat(300) {
            val position = vec2(
                    Random.nextFloat() * (Gdx.graphics.width - Firefly.BODY_RADIUS * 2) + Firefly.BODY_RADIUS,
                    Random.nextFloat() * (Gdx.graphics.height - Firefly.BODY_RADIUS * 2) + Firefly.BODY_RADIUS
            )
            val velocity = vec2().setToRandomDirection() * Firefly.SPEED
            fireflies.add(Firefly(position, velocity))
        }
    }

    private fun randomizeFirefliesCycleAtPosition(x: Float, y: Float) {
        fireflies.filter { firefly -> vec2(x, y).dst2(firefly.position) <= RANDOMIZE_RADIUS_SQUARED }
                .forEach { it.randomizeCycle() }
    }

    private fun updateFireflies(delta: Float) {
        fireflies.forEach { firefly -> firefly.updateMovement(delta) }
        accumulator += delta
        if (accumulator >= 0.06f) {
            // The fireflies don't update their cycle on every render cycle
            fireflies.forEach { it.updateCycle() }
            fireflies.forEach { it.lookAtNearby(fireflies) }
            accumulator -= 0.06f
        }
    }

    private fun renderFireflies() {
        main.batch.use { batch ->
            fireflies.filter { it.isOn }.forEach { firefly ->
                batch.draw(LIGHT_IMAGE, firefly.position.x - LIGHT_IMAGE.width / 2f, firefly.position.y - LIGHT_IMAGE.height / 2f)
            }
        }
        main.shapeRenderer.use(ShapeRenderer.ShapeType.Line) { renderer ->
            fireflies.filter { !it.isOn }.forEach { firefly ->
                renderer.color = Color.DARK_GRAY
                renderer.circle(firefly.position.x, firefly.position.y, Firefly.BODY_RADIUS)
                if (DEBUG) {
                    renderer.color = PERCEPTION_COLOR
                    renderer.circle(firefly.position.x, firefly.position.y, Firefly.PERCEPTION_RADIUS)
                }
            }
        }
    }

    override fun hide() {
        fireflies.clear()
        Gdx.input.inputProcessor = null
    }
}