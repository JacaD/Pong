package com.example.pongkotlin

import android.graphics.RectF

import java.util.Random

class Ball(screenX: Int, screenY: Int) {
    val rect: RectF
    private var xVelocity: Float = 0f
    private var yVelocity: Float = 0f
    private val ballWidth: Float
    private var ballHeight: Float = 0f

    init {
        ballHeight = screenX / 75f
        ballWidth = ballHeight
        xVelocity = -screenY / 2f
        yVelocity = xVelocity
        rect = RectF()
    }

    fun update(fps: Float) {
        rect.left = rect.left + xVelocity / fps
        rect.top = rect.top + yVelocity / fps
        rect.right = rect.left + ballWidth
        rect.bottom = rect.top - ballHeight
    }

    fun reverseYVelocity() {
        yVelocity = -yVelocity
    }

    fun reverseXVelocity() {
        xVelocity = -xVelocity
    }

    fun getXVelocity(): Float{
        return xVelocity
    }

    fun getYVelocity(): Float{
        return yVelocity
    }

    fun setRandomXVelocity() {
        val generator = Random()
        val answer = generator.nextInt(2)

        if (answer == 0) {
            reverseXVelocity()
        }
    }

    fun getCenter(): Float{
        return (rect.left + rect.right) / 2f
    }

    fun increaseVelocity() {
        xVelocity += xVelocity / 10
        yVelocity += yVelocity / 10
    }

    fun clearObstacleY(y: Float, isAI: Boolean = true) {
        rect.bottom = y
        rect.top = if(!isAI) y - ballHeight else y + ballHeight
    }

    fun clearObstacleX(x: Float) {
        rect.left = x
        rect.right = x + ballWidth
    }

    fun reset(x: Int, y: Int) {
        rect.left = x / 2f
        rect.top = (y - 20).toFloat()
        rect.right = x / 2f + ballWidth
        rect.bottom = y.toFloat() - 20f - ballHeight
        xVelocity = -y / 2f
        yVelocity = xVelocity
    }
}