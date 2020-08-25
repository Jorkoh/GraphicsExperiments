package com.jorkoh.graphicsExperiments.screens.boids

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.jorkoh.graphicsExperiments.GraphicsExperiments
import com.jorkoh.graphicsExperiments.screens.SelectionScreen
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen

class BoidsScreen(private val main: GraphicsExperiments) : KtxScreen {

    override fun show() {
        Gdx.input.inputProcessor = object : KtxInputAdapter {
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
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
    }
}