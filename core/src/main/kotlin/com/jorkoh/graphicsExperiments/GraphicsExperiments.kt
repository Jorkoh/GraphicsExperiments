package com.jorkoh.graphicsExperiments

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.jorkoh.graphicsExperiments.screens.SelectionScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen

class GraphicsExperiments : KtxGame<KtxScreen>(clearScreen = false) {
    val batch by lazy { SpriteBatch() }
    val shapeRenderer by lazy { ShapeRenderer() }

    // TODO avoid font pixelation
    val font by lazy { BitmapFont().apply { data.scale(2f) } }

    override fun create() {
        addScreen(SelectionScreen(this))
        setScreen<SelectionScreen>()
        super.create()
    }

    override fun dispose() {
        batch.dispose()
        shapeRenderer.dispose()
        font.dispose()
        super.dispose()
    }
}