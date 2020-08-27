package com.jorkoh.graphicsExperiments.lwjgl3

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.jorkoh.graphicsExperiments.GraphicsExperiments

/** Launches the desktop (LWJGL3) application.  */
object Lwjgl3Launcher {
    @JvmStatic
    fun main(args: Array<String>) {
        Lwjgl3Application(GraphicsExperiments(), Lwjgl3ApplicationConfiguration().apply {
            setTitle("GraphicsExperiments")
            setWindowedMode(1600, 900)
            setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png")
        })
    }
}