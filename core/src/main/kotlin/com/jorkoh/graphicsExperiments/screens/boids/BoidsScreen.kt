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
        const val DEFAULT_ZOOM = 1.2f
        const val MIN_ZOOM = 0.3f
        const val MAX_ZOOM = 2f
        val WATER_COLOR = Color(0.3686f, 0.5725f, 0.8f, 1f)

        const val AREA_WIDTH = 1920f
        const val AREA_HEIGHT = 1080f
        const val SPAWN_MARGIN = 100

        const val FISH_POLY_SCALE = 6f
        val fishPolyVertex = mutableListOf(
                -1.4f, 0f,
                -0.8f, 0f,
                0.4f, -0.3f,
                0.6f, 0f,
                0.4f, 0.3f,
                -0.8f, 0f,
        ).map { it * FISH_POLY_SCALE }.toFloatArray()
        val fishPoly = Polygon()

        const val SHARK_POLY_SCALE = 12f
        val sharkPolyVertex = mutableListOf(
                -1.4f, 0f,
                -0.8f, 0f,
                0.4f, -0.3f,
                0.6f, 0f,
                0.4f, 0.3f,
                -0.8f, 0f,
        ).map { it * SHARK_POLY_SCALE }.toFloatArray()
        val sharkPoly = Polygon()
    }

    private val camera = OrthographicCamera().apply {
        setToOrtho(false, AREA_WIDTH, AREA_HEIGHT)
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
                    selectedAgent = agents.minBy { agent -> agent.position.dst(unprojectedPosition.x, unprojectedPosition.y) }
                    true
                }
                Input.Buttons.MIDDLE -> {
                    middleMouse = true
                    previousDragPosition = vec3(screenX.toFloat(), screenY.toFloat(), 0f)
                    true
                }
                Input.Buttons.RIGHT -> {
                    selectedAgent = null
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

    // TODO generalize both predator and boids into agent interface?
    private val agents
        get() = boids + predators
    private val predators = mutableListOf<Predator>()
    private val boids = mutableListOf<Boid>()
    private var selectedAgent: Agent? = null

    override fun show() {
        Gdx.input.inputProcessor = inputProcessor
        addBoids()
        addPredators()
    }

    override fun render(delta: Float) {
        clearScreen()

        // TODO figure out how to manage the order of update on the agents
        updateBoids(delta)
        updatePredators(delta)

        renderArea()
        renderBoids()
        renderPredators()
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
        boids.clear()
    }

    private fun addBoids() {
        repeat(400) {
            val position = vec2(
                    Random.nextFloat() * (AREA_WIDTH - SPAWN_MARGIN) + SPAWN_MARGIN / 2f,
                    Random.nextFloat() * (AREA_HEIGHT - SPAWN_MARGIN) + SPAWN_MARGIN / 2f
            )
            val velocity = vec2().setToRandomDirection() * Boid.INITIAL_SPEED
            boids.add(Boid(position, velocity))
        }
    }

    private fun addPredators() {
        repeat(2) {
            val position = vec2(
                    Random.nextFloat() * (AREA_WIDTH - SPAWN_MARGIN) + SPAWN_MARGIN / 2f,
                    Random.nextFloat() * (AREA_HEIGHT - SPAWN_MARGIN) + SPAWN_MARGIN / 2f
            )
            val velocity = vec2().setToRandomDirection() * Predator.INITIAL_SPEED
            predators.add(Predator(position, velocity))
        }
    }

    private fun renderArea() {
        main.shapeRenderer.projectionMatrix = camera.combined
        main.shapeRenderer.use(ShapeRenderer.ShapeType.Filled) { renderer ->
            renderer.color = WATER_COLOR
            renderer.rect(0f, 0f, AREA_WIDTH, AREA_HEIGHT)
        }
    }

    private fun updateBoids(timeDelta: Float) {
        boids.forEach { boid ->
            boid.interactWithEnvironment(boids.findNeighbors(boid), predators.findPredators(boid), timeDelta)
            boid.calculateVelocity(timeDelta)
            boid.move(timeDelta)
        }
    }

    private fun updatePredators(timeDelta: Float) {
        predators.forEach { predator ->
            predator.interactWithEnvironment(boids.findPrey(predator), timeDelta)
            predator.calculateVelocity(timeDelta)
            predator.move(timeDelta)
        }
    }

    // TODO Optimize this with region algorithm
    // TODO better naming
    // TODO move this into the class?
    private fun List<Boid>.findNeighbors(boid: Boid) = filter { otherBoid ->
        otherBoid != boid && otherBoid.position.dst(boid.position) <= Boid.PERCEPTION_RADIUS
                && abs(boid.velocity.angle(otherBoid.position - boid.position)) <= Boid.PERCEPTION_CONE_DEGREES / 2f
    }

    // TODO Optimize this with region algorithm
    // TODO better naming
    // TODO move this into the class?
    private fun List<Predator>.findPredators(boid: Boid) = filter { predator ->
        predator.position.dst(boid.position) <= Boid.PERCEPTION_RADIUS
                && abs(boid.velocity.angle(predator.position - boid.position)) <= Boid.PERCEPTION_CONE_DEGREES / 2f
    }

    // TODO Optimize this with region algorithm
    // TODO better naming
    // TODO move this into the class?
    // TODO don't calculate distance twice
    private fun List<Boid>.findPrey(predator: Predator) = filter { prey ->
        prey.position.dst(predator.position) <= Predator.PERCEPTION_RADIUS
                && abs(predator.velocity.angle(prey.position - predator.position)) <= Predator.PERCEPTION_CONE_DEGREES / 2f
    }.minBy { prey -> prey.position.dst(predator.position) }

    private fun renderBoids() {
        main.shapeRenderer.use(ShapeRenderer.ShapeType.Line) { renderer ->
            boids.forEach { boid ->
                renderer.color = Color.BLUE
                renderer.polygon(getFishPolyVertex(boid.position, boid.velocity.angle()))

                // TODO this stuff should be moved into the interface
                if (boid == selectedAgent) {
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
                            6 -> Color.PINK
                            else -> Color.BLACK
                        }
                        renderer.line(boid.position, boid.position + component * 75f)
                    }
                }
            }
        }
    }


    private fun renderPredators() {
        main.shapeRenderer.use(ShapeRenderer.ShapeType.Line) { renderer ->
            predators.forEach { predator ->
                renderer.color = Color.RED
                renderer.polygon(getSharkPolyVertex(predator.position, predator.velocity.angle()))

                // TODO this stuff should be moved into the interface
                if (predator == selectedAgent) {
                    // Debug stuff
                    renderer.color = Color.LIGHT_GRAY
                    renderer.arc(predator.position, Predator.PERCEPTION_RADIUS, predator.velocity.angle() - Predator.PERCEPTION_CONE_DEGREES / 2f, Predator.PERCEPTION_CONE_DEGREES)

                    renderer.color = Color.GREEN
                    renderer.line(predator.position, predator.position + predator.velocity)

                    predator.velocityComponents.forEachIndexed { index, component ->
                        renderer.color = when (index) {
                            in 0..3 -> Color.RED
                            4 -> Color.YELLOW
                            else -> Color.BLACK
                        }
                        renderer.line(predator.position, predator.position + component * 75f)
                    }
                }
            }
        }
    }

    // TODO this two methods do the same, make them one
    private fun getFishPolyVertex(position: Vector2, angle: Float) = fishPoly.apply {
        setPosition(0f, 0f)
        rotation = 0f
        vertices = fishPolyVertex.copyOf()
        translate(position.x, position.y)
        rotate(angle)
    }.transformedVertices

    private fun getSharkPolyVertex(position: Vector2, angle: Float) = sharkPoly.apply {
        setPosition(0f, 0f)
        rotation = 0f
        vertices = sharkPolyVertex.copyOf()
        translate(position.x, position.y)
        rotate(angle)
    }.transformedVertices
}