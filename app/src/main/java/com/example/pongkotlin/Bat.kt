package com.example.pongkotlin

import android.graphics.RectF

class Bat(private val screenX: Int,
          private val screenY: Int,
          private val isAI: Boolean,
          private val length: Float = screenX / 6.0f,
          private val speed: Float = 1.0f) {

    val rect: RectF
//    private val length: Float = screenX / 6.0f
    private val height: Float = screenY / 25.0f
    private var xCoord: Float = 0f
    private val yCoord: Float
    private var batSpeed: Float = 0f
    private val batBaseSpeed: Float
    private var batMoving = STOPPED

    init {

        xCoord = screenX / 2.0f
        yCoord = if(isAI){
            -10f
        } else{
            (screenY - 30).toFloat()
        }

        rect = RectF(xCoord, yCoord, xCoord + length, yCoord + height)
        batBaseSpeed = screenX / 2.0f
        batSpeed = screenX / 2.0f
    }

    fun setmBatSpeed(acceleratorValue: Double) {
        batSpeed = batBaseSpeed * Math.abs(acceleratorValue).toFloat()
    }

    fun setMovementState(state: Int) {
        batMoving = state
    }

    fun getCenter(): Float{
        return (rect.left + rect.right) / 2f
    }

    fun update(fps: Float) {
        if (batMoving == LEFT) {
            xCoord -= batSpeed / fps * speed
        }

        if (batMoving == RIGHT) {
            xCoord += batSpeed / fps * speed
        }
        if (rect.left < 0) {
            xCoord = 0f
        }
        if (rect.right > screenX) {
            xCoord = screenX - (rect.right - rect.left)
        }
        rect.left = xCoord
        rect.right = xCoord + length
    }
    companion object {
        const val STOPPED = 0
        const val LEFT = 1
        const val RIGHT = 2
    }
}
