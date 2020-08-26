package com.jorkoh.graphicsExperiments.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import ktx.math.vec2


fun clearScreen(red: Float = 0f, green: Float = 0f, blue: Float = 0f) {
    Gdx.gl.glClearColor(red, green, blue, 1f)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
}

fun printFPS(delta: Float) {
    println("FPS: ${1 / delta}")
}

fun screenPosToVec2(screenX : Int, screenY : Int) = vec2(screenX.toFloat(), (Gdx.graphics.height - screenY).toFloat())

fun ShapeRenderer.strokeArc(position : Vector2, radius: Float, start: Float, degrees: Float) {
    val segments = (6 * Math.cbrt(radius.toDouble()).toFloat() * (degrees / 360.0f)).toInt()

    require(segments > 0) { "segments must be > 0." }
    val colorBits = color.toFloatBits()
    val theta = 2 * MathUtils.PI * (degrees / 360.0f) / segments
    val cos = MathUtils.cos(theta)
    val sin = MathUtils.sin(theta)
    var cx = radius * MathUtils.cos(start * MathUtils.degreesToRadians)
    var cy = radius * MathUtils.sin(start * MathUtils.degreesToRadians)

    for (i in 0 until segments) {
        renderer.color(colorBits)
        renderer.vertex(position.x + cx, position.y + cy, 0f)
        val temp = cx
        cx = cos * cx - sin * cy
        cy = sin * temp + cos * cy
        renderer.color(colorBits)
        renderer.vertex(position.x + cx, position.y + cy, 0f)
    }
}