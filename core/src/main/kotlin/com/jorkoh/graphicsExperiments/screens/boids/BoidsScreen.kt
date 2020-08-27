package com.jorkoh.graphicsExperiments.screens.boids

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Vector2
import com.jorkoh.graphicsExperiments.GraphicsExperiments
import com.jorkoh.graphicsExperiments.screens.SelectionScreen
import com.jorkoh.graphicsExperiments.screens.clearScreen
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.graphics.arc
import ktx.graphics.use
import ktx.math.*
import kotlin.math.abs
import kotlin.random.Random

class BoidsScreen(private val main: GraphicsExperiments) : KtxScreen {

    companion object {
        const val DEFAULT_ZOOM = 1.4f
        const val MIN_ZOOM = 0.3f
        const val MAX_ZOOM = 2f
        val WATER_COLOR = Color(0.3686f, 0.5725f, 0.8f, 1f)

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

    private val camera = OrthographicCamera().apply {
        setToOrtho(false, 1600f, 900f)
        zoom = DEFAULT_ZOOM
        update()
    }
    private val inputProcessor = object : KtxInputAdapter {
        var middleMouse = false
        var previousDragPosition = vec3()

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
            return when (button) {
                Input.Buttons.LEFT -> {
                    // TODO Optimize this with region algorithm
                    val unprojectedPosition = camera.unproject(vec3(screenX.toFloat(), screenY.toFloat()))
                    selectedBoid = boids.minBy { boid -> boid.position.dst(unprojectedPosition.x, unprojectedPosition.y) }
                    true
                }
                Input.Buttons.MIDDLE -> {
                    middleMouse = true
                    previousDragPosition = vec3(screenX.toFloat(), screenY.toFloat(), 0f)
                    true
                }
                Input.Buttons.RIGHT -> {
                    selectedBoid = null
                    true
                }
                else -> false
            }
        }

        override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            return when (button) {
                Input.Buttons.MIDDLE -> {
                    middleMouse = false
                    true
                }
                else -> false
            }
        }

        override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
            return if (middleMouse) {
                val newDragPosition = vec3(screenX.toFloat(), screenY.toFloat(), 0f)
                camera.unproject(previousDragPosition)
                camera.translate(previousDragPosition - camera.unproject(newDragPosition.cpy()))
                camera.update()
                previousDragPosition = newDragPosition
                true
            } else {
                false
            }
        }

        override fun scrolled(amount: Int): Boolean {
            val newZoom = camera.zoom + amount / 10f
            if (newZoom in MIN_ZOOM..MAX_ZOOM) {
                camera.zoom = newZoom
                camera.update()
            }
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
        clearScreen()
        main.shapeRenderer.use(ShapeRenderer.ShapeType.Filled) { renderer ->
            renderer.color = WATER_COLOR
            renderer.rect(0f, 0f, 1600f, 900f)
        }

        updateBoids(delta)
        renderBoids()
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
        boids.clear()
    }

    private fun addBoids() {
        repeat(300) {
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
            boid.calculateVelocityComponents(boids.filterNeighbors(boid), timeDelta)
            boid.calculateVelocity(timeDelta)
            boid.move(timeDelta)
        }
    }

    // TODO Optimize this with region algorithm
    private fun List<Boid>.filterNeighbors(boid: Boid) = filter { otherBoid ->
        otherBoid != boid && otherBoid.position.dst(boid.position) <= Boid.PERCEPTION_RADIUS
                && abs(boid.velocity.angle(otherBoid.position - boid.position)) <= Boid.PERCEPTION_CONE_DEGREES / 2f
    }

    private fun renderBoids() {
        main.shapeRenderer.projectionMatrix = camera.combined

        main.shapeRenderer.use(ShapeRenderer.ShapeType.Line) { renderer ->
            boids.forEach { boid ->
                renderer.color = Color.BLUE
                renderer.polygon(getFishPolyVertex(boid.position, boid.velocity.angle()))

                if (boid == selectedBoid) {
                    // Debug stuff
                    renderer.color = Color.LIGHT_GRAY
                    renderer.arc(boid.position, Boid.PERCEPTION_RADIUS, boid.velocity.angle() - Boid.PERCEPTION_CONE_DEGREES / 2f, Boid.PERCEPTION_CONE_DEGREES)

                    renderer.color = Color.GREEN
                    renderer.line(boid.position, boid.position + boid.velocity)

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