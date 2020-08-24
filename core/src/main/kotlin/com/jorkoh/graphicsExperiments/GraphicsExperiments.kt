package com.jorkoh.graphicsExperiments;

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.jorkoh.graphicsExperiments.screens.MainMenuScreen
import ktx.app.KtxGame;
import ktx.app.KtxScreen;

class GraphicsExperiments : KtxGame<KtxScreen>() {
    val batch by lazy { SpriteBatch() }
    val shapeRenderer by lazy { ShapeRenderer() }
    val font by lazy { BitmapFont().apply { data.scale(2f) } }

    override fun create() {
        addScreen(MainMenuScreen(this))
        setScreen<MainMenuScreen>()
        super.create()
    }
}