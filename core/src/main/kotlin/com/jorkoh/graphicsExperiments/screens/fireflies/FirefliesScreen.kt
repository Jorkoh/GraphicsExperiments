package com.jorkoh.graphicsExperiments.screens.fireflies

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.jorkoh.graphicsExperiments.GraphicsExperiments
import com.jorkoh.graphicsExperiments.screens.MainMenuScreen
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.graphics.use
import kotlin.random.Random

class FirefliesScreen(private val main: GraphicsExperiments) : KtxScreen {
    companion object {
        const val CHAOS_RADIUS = 80f
    }

    private val fireflies = mutableListOf<Firefly>()
    private var accumulator = 0f

    override fun show() {
        Gdx.input.inputProcessor = object : KtxInputAdapter {
            override fun keyDown(keycode: Int): Boolean {
                return when (keycode) {
                    Input.Keys.ESCAPE -> {
                        main.addScreen(MainMenuScreen(main))
                        main.setScreen<MainMenuScreen>()
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

        // Add fireflies
        repeat(200) {
            fireflies.add(Firefly(
                    Random.nextFloat() * (Gdx.graphics.width - Firefly.DEFAULT_RADIUS * 2) + Firefly.DEFAULT_RADIUS,
                    Random.nextFloat() * (Gdx.graphics.height - Firefly.DEFAULT_RADIUS * 2) + Firefly.DEFAULT_RADIUS
            ))
        }
    }

    private fun randomizeFirefliesCycleAtPosition(x: Float, y: Float) {
        fireflies.filter { firefly -> Vector2.dst(x, y, firefly.x, firefly.y) <= CHAOS_RADIUS }
                .forEach { it.randomizeCycle() }
    }

    override fun render(delta: Float) {
        accumulator += delta
        if (accumulator >= 0.06f) {
            // The fireflies don't update on every render cycle
            accumulator -= 0.06f
            fireflies.forEach { firefly -> firefly.update() }
            fireflies.forEach { firefly -> firefly.lookAt(fireflies) }
        }

        main.shapeRenderer.use(ShapeRenderer.ShapeType.Filled) { renderer ->
            fireflies.filter { it.isOn }.forEach { firefly -> firefly.draw(renderer) }
        }
        main.shapeRenderer.use(ShapeRenderer.ShapeType.Line) { renderer ->
            fireflies.filter { !it.isOn }.forEach { firefly -> firefly.draw(renderer) }
//            fireflies.forEach { firefly -> firefly.drawPerception(renderer) }
        }
    }

    override fun hide() {
        fireflies.clear()
        Gdx.input.inputProcessor = null
    }
}