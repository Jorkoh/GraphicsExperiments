package com.jorkoh.graphicsExperiments.screens;

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.jorkoh.graphicsExperiments.GraphicsExperiments
import com.jorkoh.graphicsExperiments.screens.fireflies.FirefliesScreen
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen

class MainMenuScreen(private val main: GraphicsExperiments) : KtxScreen {

    override fun show() {
        Gdx.input.inputProcessor = object : KtxInputAdapter {
            override fun keyDown(keycode: Int): Boolean {
                return when (keycode) {
                    Input.Keys.F1 -> {
                        main.addScreen(FirefliesScreen(main))
                        main.setScreen<FirefliesScreen>()
                        main.removeScreen<MainMenuScreen>()
                        dispose()
                        true
                    }
                    Input.Keys.ESCAPE -> {
                        dispose()
                        Gdx.app.exit()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    override fun render(delta: Float) {
        val text = GlyphLayout(main.font, "F1 for fireflies")

        main.batch.begin()
        main.font.draw(main.batch, text,
                Gdx.graphics.width / 2f - text.width / 2f,
                Gdx.graphics.height / 2f + text.height / 2f)
        main.batch.end()
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
    }
}