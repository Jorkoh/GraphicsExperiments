package com.jorkoh.graphicsExperiments.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20

fun clearScreen(red: Float = 0f, green: Float = 0f, blue: Float = 0f) {
    Gdx.gl.glClearColor(red, green, blue, 1f)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
}

fun printFPS(delta: Float) {
    println("FPS: ${1 / delta}")
}