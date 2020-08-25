package com.jorkoh.graphicsExperiments.screens;

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.jorkoh.graphicsExperiments.GraphicsExperiments
import com.jorkoh.graphicsExperiments.screens.boids.BoidsScreen
import com.jorkoh.graphicsExperiments.screens.fireflies.FirefliesScreen
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.graphics.use

class SelectionScreen(private val main: GraphicsExperiments) : KtxScreen {

    companion object {
        const val SELECTION_TEXT = """
            |[1] Fireflies syncing
            |[2] Boids
        """
    }

    override fun show() {
        Gdx.input.inputProcessor = object : KtxInputAdapter {
            override fun keyDown(keycode: Int): Boolean {
                return when (keycode) {
                    Input.Keys.NUM_1, Input.Keys.NUMPAD_1 -> {
                        main.addScreen(FirefliesScreen(main))
                        main.setScreen<FirefliesScreen>()
                        main.removeScreen<SelectionScreen>()
                        dispose()
                        true
                    }
                    Input.Keys.NUM_2, Input.Keys.NUMPAD_2 -> {
                        main.addScreen(BoidsScreen(main))
                        main.setScreen<BoidsScreen>()
                        main.removeScreen<SelectionScreen>()
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
        clearScreen()
        val text = GlyphLayout(main.font, SELECTION_TEXT.trimMargin())

        main.batch.use { batch ->
            main.font.draw(batch, text, Gdx.graphics.width / 2f - text.width / 2f, Gdx.graphics.height / 2f + text.height / 2f)
        }
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
    }
}