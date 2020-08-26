package com.jorkoh.graphicsExperiments.screens.boids

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Vector2
import com.jorkoh.graphicsExperiments.GraphicsExperiments
import com.jorkoh.graphicsExperiments.screens.SelectionScreen
import com.jorkoh.graphicsExperiments.screens.clearScreen
import com.jorkoh.graphicsExperiments.screens.screenPosToVec2
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.graphics.use
import ktx.math.plus
import ktx.math.times
import ktx.math.vec2
import kotlin.random.Random

class BoidsScreen(private val main: GraphicsExperiments) : KtxScreen {

    companion object {
        const val SPAWN_MARGIN = 100
        const val POLY_SCALE = 6f

        val fishPolyVertex = mutableListOf(
                -1.4f, 0f,
                -0.8f, 0f,
                0.4f, -0.3f,
                0.6f, 0f,
                0.4f, 0.3f,
                -0.8f, 0f,
        ).map { it * POLY_SCALE }.toFloatArray()
        val fishPoly = Polygon()
    }

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

        override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            // TODO Use only neighbors with optimization algorithm
            selectedBoid = boids.minBy { boid -> boid.position.dst(screenPosToVec2(screenX, screenY)) }
            return true
        }
    }
    private val boids = mutableListOf<Boid>()
    private var selectedBoid: Boid? = null

    override fun show() {
        Gdx.input.inputProcessor = inputProcessor
        addBoids()
    }

    override fun render(delta: Float) {
        clearScreen(0.3686f, 0.5725f, 0.8f)

        updateBoids(delta)
        renderBoids()
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
        boids.clear()
    }

    private fun addBoids() {
        repeat(600) {
            val position = vec2(
                    Random.nextFloat() * (Gdx.graphics.width - SPAWN_MARGIN) + SPAWN_MARGIN / 2f,
                    Random.nextFloat() * (Gdx.graphics.height - SPAWN_MARGIN) + SPAWN_MARGIN / 2f
            )
            val velocity = vec2().setToRandomDirection() * Boid.INITIAL_SPEED
            boids.add(Boid(position, velocity))
        }
    }

    private fun updateBoids(timeDelta: Float) {
        boids.forEach { boid ->
            // TODO Pass only neighbors with optimization algorithm
            boid.calculateVelocityComponents(boids.filter { otherBoid ->
                otherBoid != boid && otherBoid.position.dst(boid.position) < Boid.PERCEPTION_RADIUS
            }, timeDelta)
            boid.calculateVelocity()
            boid.move(timeDelta)
        }
    }

    private fun renderBoids() {
        main.shapeRenderer.use(ShapeRenderer.ShapeType.Line) { renderer ->
            boids.forEach { boid ->
                renderer.color = Color.BLUE
                renderer.polygon(getFishPolyVertex(boid.position, boid.velocity.angle()))

                if (boid == selectedBoid) {
                    // Debug stuff
                    renderer.color = Color.LIGHT_GRAY
                    renderer.circle(boid.position.x, boid.position.y, Boid.PERCEPTION_RADIUS)

                    renderer.color = Color.GREEN
                    renderer.line(boid.position, boid.position + boid.velocity)
                    println("Speed: " + boid.velocity.len())

                    boid.velocityComponents.forEachIndexed { index, component ->
                        renderer.color = when (index) {
                            in 0..3 -> Color.RED
                            4 -> Color.YELLOW
                            5 -> Color.BROWN
                            else -> Color.BLACK
                        }
                        renderer.line(boid.position, boid.position + component * 75f)
                    }
                }
            }
        }
    }

    private fun getFishPolyVertex(position: Vector2, angle: Float) = fishPoly.apply {
        setPosition(0f, 0f)
        rotation = 0f
        vertices = fishPolyVertex.copyOf()
        translate(position.x, position.y)
        rotate(angle)
    }.transformedVertices
}