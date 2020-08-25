package com.jorkoh.graphicsExperiments.screens.boids

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.jorkoh.graphicsExperiments.GraphicsExperiments
import com.jorkoh.graphicsExperiments.screens.SelectionScreen
import com.jorkoh.graphicsExperiments.screens.clearScreen
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen

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

    override fun show() {
        Gdx.input.inputProcessor = inputProcessor
    }

    override fun render(delta: Float) {
        clearScreen(0.3686f, 0.5725f, 0.8f)
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
    }
}