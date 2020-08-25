package com.jorkoh.graphicsExperiments.screens.boids

import com.badlogic.gdx.math.Vector2
import ktx.math.plusAssign
import ktx.math.times

class Boid(
        var position: Vector2,
        var velocity: Vector2
){

    companion object{
        const val INITIAL_SPEED = 6f
    }

    fun updateMovement(timeDelta : Float){
        position += velocity * timeDelta
    }
}